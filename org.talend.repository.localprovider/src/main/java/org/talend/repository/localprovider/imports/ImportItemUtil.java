// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.localprovider.imports;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.migration.IProjectMigrationTask;
import org.talend.core.model.migration.IProjectMigrationTask.ExecutionResult;
import org.talend.core.model.properties.FileItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.User;
import org.talend.core.model.properties.helper.ByteArrayResource;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.migrationtool.model.GetTasksHelper;
import org.talend.repository.constants.FileConstants;
import org.talend.repository.documentation.IDocumentationService;
import org.talend.repository.localprovider.RepositoryLocalProviderPlugin;
import org.talend.repository.localprovider.i18n.Messages;
import org.talend.repository.localprovider.imports.ItemRecord.State;
import org.talend.repository.localprovider.imports.TreeBuilder.ProjectNode;
import org.talend.repository.localprovider.model.XmiResourceManager;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.ProxyRepositoryFactory;

/**
 */
public class ImportItemUtil {

    private static Logger log = Logger.getLogger(ImportItemUtil.class);

    private static final String SEGMENT_PARENT = ".."; //$NON-NLS-1$

    private XmiResourceManager xmiResourceManager = new XmiResourceManager();

    private boolean hasErrors = false;

    private int usedItems = 0;

    private RepositoryObjectCache cache = new RepositoryObjectCache();

    private TreeBuilder treeBuilder = new TreeBuilder();

    private Set<String> deletedItems = new HashSet<String>();

    public void clear() {
        deletedItems.clear();
    }

    public void setErrors(boolean errors) {
        this.hasErrors = errors;
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    private boolean checkItem(ItemRecord itemRecord, boolean overwrite) {
        boolean result = false;

        try {
            boolean nameAvailable = ProxyRepositoryFactory.getInstance().isNameAvailable(itemRecord.getItem(),
                    itemRecord.getProperty().getLabel());
            boolean idAvailable = ProxyRepositoryFactory.getInstance().getLastVersion(itemRecord.getProperty().getId()) == null;

            boolean isSystemRoutine = false;
            // we do not import built in routines
            if (itemRecord.getItem().eClass().equals(PropertiesPackage.eINSTANCE.getRoutineItem())) {
                RoutineItem routineItem = (RoutineItem) itemRecord.getItem();
                if (routineItem.isBuiltIn()) {
                    isSystemRoutine = true;
                }
            }

            if (nameAvailable) {
                if (idAvailable) {
                    if (!isSystemRoutine) {
                        result = true;
                    } else {
                        itemRecord.addError(Messages.getString("RepositoryUtil.isSystemRoutine")); //$NON-NLS-1$ 
                    }
                } else {
                    // same id but different name
                    itemRecord.setState(State.ID_EXISTED);
                    if (overwrite) {
                        result = true;
                    } else {
                        itemRecord.addError(Messages.getString("RepositoryUtil.idUsed")); //$NON-NLS-1$
                    }
                }
            } else {
                if (idAvailable) {
                    // same name but different id
                    itemRecord.setState(State.NAME_EXISTED);
                } else {
                    // same name and same id
                    itemRecord.setState(State.ID_EXISTED);
                    if (overwrite) {
                        result = true;
                    }
                }
                if (!result) {
                    itemRecord.addError(Messages.getString("RepositoryUtil.nameUsed")); //$NON-NLS-1$
                }
            }

            if (result && overwrite && itemRecord.getState() == State.ID_EXISTED) {
                // if item is locked, cannot overwrite
                if (checkIfLocked(itemRecord)) {
                    itemRecord.addError(Messages.getString("RepositoryUtil.itemLocked")); //$NON-NLS-1$
                    result = false;
                }
            }
        } catch (Exception e) {
            // ignore
        }

        return result;
    }

    /**
     * DOC hcw Comment method "checkIfLocked".
     * 
     * @param itemRecord
     * @return
     * @throws PersistenceException
     */
    private boolean checkIfLocked(ItemRecord itemRecord) throws PersistenceException {
        Boolean lockState = cache.getItemLockState(itemRecord);
        if (lockState != null) {
            return lockState.booleanValue();
        }

        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        List<IRepositoryObject> list = cache.findObjectsByItem(itemRecord);

        for (IRepositoryObject obj : list) {
            ERepositoryStatus status = factory.getStatus(obj);
            if (status == ERepositoryStatus.LOCK_BY_OTHER || status == ERepositoryStatus.LOCK_BY_USER) {
                itemRecord.setLocked(true);
                cache.setItemLockState(itemRecord, true);
                return true;
            }
        }

        cache.setItemLockState(itemRecord, false);
        return false;
    }

    public List<ItemRecord> importItemRecords(ResourcesManager manager, List<ItemRecord> itemRecords, IProgressMonitor monitor,
            boolean overwrite) {
        monitor.beginTask(Messages.getString("ImportItemWizardPage.ImportSelectedItems"), itemRecords.size() + 1); //$NON-NLS-1$
        for (ItemRecord itemRecord : itemRecords) {
            if (!monitor.isCanceled()) {
                monitor.subTask(Messages.getString("ImportItemWizardPage.Importing") + itemRecord.getItemName()); //$NON-NLS-1$
                if (itemRecord.isValid()) {
                    reinitRepository();
                    importItemRecord(manager, itemRecord, overwrite);
                    monitor.worked(1);
                }
            }
        }
        monitor.done();

        // cannot cancel this part
        monitor.beginTask(Messages.getString("ImportItemWizardPage.ApplyMigrationTasks"), itemRecords.size() + 1); //$NON-NLS-1$
        for (ItemRecord itemRecord : itemRecords) {
            if (itemRecord.isImported()) {
                reinitRepository();
                applyMigrationTasks(itemRecord, monitor);
            }
            monitor.worked(1);
        }
        monitor.done();

        return itemRecords;
    }

    private void reinitRepository() {
        ProxyRepositoryFactory repFactory = ProxyRepositoryFactory.getInstance();
        if (usedItems++ > 2) {
            usedItems = 0;
            try {
                repFactory.initialize();
            } catch (PersistenceException e) {
            }
        }
    }

    private void importItemRecord(ResourcesManager manager, ItemRecord itemRecord, boolean overwrite) {
        resolveItem(manager, itemRecord);
        if (itemRecord.getItem() != null) {
            ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(itemRecord.getItem());
            IPath path = new Path(itemRecord.getItem().getState().getPath());
            ProxyRepositoryFactory repFactory = ProxyRepositoryFactory.getInstance();

            try {
                repFactory.createParentFoldersRecursively(itemType, path);
            } catch (Exception e) {
                logError(e);
                path = new Path(""); //$NON-NLS-1$
            }

            try {
                Item tmpItem = itemRecord.getItem();

                // delete existing items before importing, this should be done once for a different id
                String id = itemRecord.getProperty().getId();
                if (overwrite && !itemRecord.isLocked() && itemRecord.getState() == State.ID_EXISTED
                        && !deletedItems.contains(id)) {
                    List<IRepositoryObject> list = cache.findObjectsByItem(itemRecord);
                    if (!list.isEmpty()) {
                        // this code will delete all version of item with same id
                        repFactory.forceDeleteObjectPhysical(list.get(0));
                        deletedItems.add(id);
                    }
                }

                IRepositoryObject lastVersion = repFactory.getLastVersion(tmpItem.getProperty().getId());

                User author = itemRecord.getProperty().getAuthor();
                if (author != null) {
                    if (!repFactory.setAuthorByLogin(tmpItem, author.getLogin())) {
                        tmpItem.getProperty().setAuthor(null); // author will be the logged user in create method
                    }
                }

                if (lastVersion == null) {
                    repFactory.create(tmpItem, path, true);
                    itemRecord.setImported(true);
                } else if (VersionUtils.compareTo(lastVersion.getProperty().getVersion(), tmpItem.getProperty().getVersion()) < 0) {
                    repFactory.forceCreate(tmpItem, path);
                    itemRecord.setImported(true);
                } else {
                    PersistenceException e = new PersistenceException("A newer version of " + tmpItem.getProperty()
                            + " already exist.");
                    itemRecord.addError(e.getMessage());
                    logError(e);
                }
            } catch (Exception e) {
                itemRecord.addError(e.getMessage());
                logError(e);
            }
        }
    }

    private void applyMigrationTasks(ItemRecord itemRecord, IProgressMonitor monitor) {
        Context ctx = CorePlugin.getContext();
        RepositoryContext repositoryContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
        ITalendSynchronizer routineSynchronizer = getRoutineSynchronizer();

        Item item = null;
        try {
            item = ProxyRepositoryFactory.getInstance().getUptodateProperty(itemRecord.getItem().getProperty()).getItem();
        } catch (Exception e) {
            logError(e);
        }

        for (String taskId : itemRecord.getMigrationTasksToApply()) {
            IProjectMigrationTask task = GetTasksHelper.getProjectTask(taskId);
            if (task == null) {
                log.warn("Task " + taskId + " found in project doesn't exist anymore !");
            } else {
                monitor.subTask("apply migration task " + task.getName() + " on item " + itemRecord.getItemName());

                try {
                    if (item != null) {
                        ExecutionResult executionResult = task.execute(repositoryContext.getProject(), item);
                        if (executionResult == ExecutionResult.FAILURE) {
                            log.warn("Incomplete import item " + itemRecord.getItemName() + " (migration task " + task.getName()
                                    + " failed)");
                            // TODO smallet add a warning/error to the job using model
                        }
                    }
                } catch (Exception e) {
                    log.warn("Incomplete import item " + itemRecord.getItemName() + " (migration task " + task.getName()
                            + " failed)");
                }
            }
        }

        try {
            // Generated documentaiton for imported item.
            if (item != null && PluginChecker.isDocumentationPluginLoaded()) {
                IDocumentationService service = (IDocumentationService) GlobalServiceRegister.getDefault().getService(
                        IDocumentationService.class);
                if (item instanceof ProcessItem) {
                    service.saveDocumentNode(item);
                } else if (item instanceof JobletProcessItem && PluginChecker.isJobLetPluginLoaded()) {
                    service.saveDocumentNode(item);
                }

            }
        } catch (Exception e) {
            logError(e);
        }

        try {
            if (item != null && item instanceof RoutineItem) {
                RoutineItem routineItem = (RoutineItem) item;
                routineSynchronizer.syncRoutine(routineItem, true);
                routineSynchronizer.getFile(routineItem);
            }
        } catch (Exception e) {
            logError(e);
        }

    }

    private ITalendSynchronizer getRoutineSynchronizer() {

        ICodeGeneratorService service = (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                ICodeGeneratorService.class);

        ECodeLanguage lang = ((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                .getProject().getLanguage();
        ITalendSynchronizer routineSynchronizer = null;
        switch (lang) {
        case JAVA:
            routineSynchronizer = service.createJavaRoutineSynchronizer();
            break;
        case PERL:
            routineSynchronizer = service.createPerlRoutineSynchronizer();
            break;
        default:
            throw new UnsupportedOperationException("Unknow language: " + lang);
        }

        return routineSynchronizer;

    }

    private void logError(Exception e) {
        hasErrors = true;
        IStatus status;
        String messageStatus = e.getMessage() != null ? e.getMessage() : ""; //$NON-NLS-1$
        status = new Status(IStatus.ERROR, RepositoryLocalProviderPlugin.PLUGIN_ID, IStatus.OK, messageStatus, e);
        RepositoryLocalProviderPlugin.getDefault().getLog().log(status);
    }

    public List<ProjectNode> getTreeViewInput() {
        return treeBuilder.getInput();
    }

    /**
     * need to returns sorted items by version to correctly import them later.
     */
    public List<ItemRecord> populateItems(ResourcesManager collector, boolean overwrite) {
        treeBuilder.clear();
        cache.clear();
        List<ItemRecord> items = new ArrayList<ItemRecord>();

        for (IPath path : collector.getPaths()) {
            if (isPropertyPath(path)) {
                IPath itemPath = getItemPath(path);
                if (collector.getPaths().contains(itemPath)) {
                    Property property = computeProperty(collector, path);
                    if (property != null) {
                        ItemRecord itemRecord = new ItemRecord(path, property);
                        items.add(itemRecord);

                        if (checkItem(itemRecord, overwrite)) {
                            InternalEObject author = (InternalEObject) property.getAuthor();
                            URI uri = null;
                            if (author != null) {
                                uri = author.eProxyURI();
                            }

                            IPath projectFilePath = getValidProjectFilePath(collector, path, uri);
                            if (projectFilePath != null) {
                                Project project = computeProject(collector, itemRecord, projectFilePath);
                                if (checkProject(project, itemRecord)) {
                                    treeBuilder.addItem(project, itemRecord);
                                    // we can try to import item
                                    // and we will try to resolve user
                                    User user = (User) project.eResource().getEObject(uri.fragment());
                                    property.setAuthor(user);
                                }
                            } else {
                                itemRecord.addError(Messages.getString("RepositoryUtil.ProjectNotFound")); //$NON-NLS-1$
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(items, new Comparator<ItemRecord>() {

            public int compare(ItemRecord o1, ItemRecord o2) {
                return VersionUtils.compareTo(o1.getProperty().getVersion(), o2.getProperty().getVersion());
            }
        });

        return items;
    }

    private boolean checkProject(Project project, ItemRecord itemRecord) {
        boolean checkProject = false;

        Context ctx = CorePlugin.getContext();
        RepositoryContext repositoryContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
        Project currentProject = repositoryContext.getProject().getEmfProject();

        if (project != null) {
            if (project.getLanguage().equals(currentProject.getLanguage())) {
                if (checkMigrationTasks(project, itemRecord, currentProject)) {
                    checkProject = true;
                }
            } else {
                itemRecord.addError(Messages.getString("RepositoryUtil.DifferentLanguage", project.getLanguage(), currentProject //$NON-NLS-1$
                        .getLanguage()));
            }
        } else {
            itemRecord.addError(Messages.getString("RepositoryUtil.ProjectNotFound")); //$NON-NLS-1$
        }

        return checkProject;
    }

    private List<String> getOptionnalMigrationTasks() {
        List<String> toReturn = new ArrayList<String>();

        toReturn.add("org.talend.repository.documentation.migrationtask.generatejobdocmigrationtask");

        return toReturn;
    }

    @SuppressWarnings("unchecked")
    private boolean checkMigrationTasks(Project project, ItemRecord itemRecord, Project currentProject) {
        List<String> itemMigrationTasks = new ArrayList(project.getMigrationTasks());
        List<String> projectMigrationTasks = new ArrayList(currentProject.getMigrationTasks());

        itemMigrationTasks.removeAll(getOptionnalMigrationTasks());

        // 1. Check if all the migration tasks of the items are done in the project:
        // if not, the item use a more recent version of TOS: impossible to import (forward compatibility)
        if (!projectMigrationTasks.containsAll(itemMigrationTasks)) {
            itemMigrationTasks.removeAll(projectMigrationTasks);

            String message = "Cannot import item " + itemRecord.getItemName() + " -> unknow task(s) " + itemMigrationTasks;
            itemRecord.addError(message);
            log.info(message);

            return false;
        }

        // 2. Get all the migration tasks to apply on this item on import (backwards compatibility)
        // (those that are in the project but not in the item)
        projectMigrationTasks.removeAll(itemMigrationTasks);
        itemRecord.setMigrationTasksToApply(projectMigrationTasks);

        return true;
    }

    private IPath getValidProjectFilePath(ResourcesManager collector, IPath path, URI uri) {
        IPath projectFilePath = getSiblingProjectFilePath(path);
        if (!collector.getPaths().contains(projectFilePath)) {
            projectFilePath = getAuthorProjectFilePath(uri, path);
            if (!collector.getPaths().contains(projectFilePath)) {
                return null;
            }
        }

        return projectFilePath;
    }

    private IPath getAuthorProjectFilePath(URI uri, IPath path) {
        IPath projectFilePath = path.removeLastSegments(1);

        if (uri != null) {
            for (int i = 0; i < uri.segments().length; i++) {
                String segment = uri.segments()[i];
                if (segment.equals(SEGMENT_PARENT)) {
                    projectFilePath = projectFilePath.removeLastSegments(1);
                } else {
                    projectFilePath = projectFilePath.append(FileConstants.LOCAL_PROJECT_FILENAME);
                }
            }
        }

        return projectFilePath;
    }

    // usefull when you export a job with source in an archive
    private IPath getSiblingProjectFilePath(IPath path) {
        IPath projectFilePath = path.removeLastSegments(1);
        projectFilePath = projectFilePath.append(FileConstants.LOCAL_PROJECT_FILENAME);

        return projectFilePath;
    }

    private Property computeProperty(ResourcesManager manager, IPath path) {
        InputStream stream = null;
        try {
            ResourceSet resourceSet = new ResourceSetImpl();
            stream = manager.getStream(path);
            Resource resource = createResource(resourceSet, path, false);
            resource.load(stream, null);
            return (Property) EcoreUtil.getObjectByType(resource.getContents(), PropertiesPackage.eINSTANCE.getProperty());
        } catch (IOException e) {
            // ignore
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return null;
    }

    public void resolveItem(ResourcesManager manager, ItemRecord itemRecord) {
        if (itemRecord.isResolved()) {
            return;
        }

        InputStream stream = null;
        ResourceSet resourceSet = itemRecord.getProperty().eResource().getResourceSet();

        try {
            boolean byteArray = (itemRecord.getItem() instanceof FileItem);
            IPath itemPath = getItemPath(itemRecord.getPath());
            stream = manager.getStream(itemPath);
            Resource resource = createResource(resourceSet, itemPath, byteArray);
            resource.load(stream, null);
            EcoreUtil.resolveAll(resourceSet);
        } catch (IOException e) {
            // ignore
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        itemRecord.setResolved(true);
    }

    private Project computeProject(ResourcesManager manager, ItemRecord itemRecord, IPath path) {
        InputStream stream = null;
        ResourceSet resourceSet = itemRecord.getProperty().eResource().getResourceSet();

        try {
            stream = manager.getStream(path);
            Resource resource = createResource(resourceSet, path, false);
            resource.load(stream, null);
            return (Project) EcoreUtil.getObjectByType(resource.getContents(), PropertiesPackage.eINSTANCE.getProject());
        } catch (IOException e) {
            // ignore
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return null;
    }

    private Resource createResource(ResourceSet resourceSet, IPath path, boolean byteArrayResource) throws FileNotFoundException {
        Resource resource;
        if (byteArrayResource) {
            resource = new ByteArrayResource(getURI(path));
            resourceSet.getResources().add(resource);
        } else {
            resource = resourceSet.createResource(getURI(path));
        }
        return resource;
    }

    private URI getURI(IPath path) {
        return URI.createURI(path.lastSegment());
    }

    private boolean isPropertyPath(IPath path) {
        return xmiResourceManager.isPropertyFile(path.lastSegment());
    }

    private IPath getItemPath(IPath path) {
        return path.removeFileExtension().addFileExtension(FileConstants.ITEM_EXTENSION);
    }

    /**
     * 
     * DOC hcw ImportItemUtil class global comment. Detailled comment
     */
    static class RepositoryObjectCache {

        static ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        private Set<ERepositoryObjectType> types = new HashSet<ERepositoryObjectType>();

        private Map<String, Boolean> lockState = new HashMap<String, Boolean>();

        // key is id of IRepositoryObject, value is a list of IRepositoryObject with same id
        private Map<String, List<IRepositoryObject>> cache = new HashMap<String, List<IRepositoryObject>>();

        public List<IRepositoryObject> findObjectsByItem(ItemRecord itemRecord) throws PersistenceException {
            Item item = itemRecord.getItem();
            ERepositoryObjectType type = ERepositoryObjectType.getItemType(item);
            if (!types.contains(type)) {
                types.add(type);
                // load object by type
                List<IRepositoryObject> list = factory.getAll(type, true, true);
                for (IRepositoryObject obj : list) {
                    // items with same id
                    List<IRepositoryObject> items = cache.get(obj.getId());
                    if (items == null) {
                        items = new ArrayList<IRepositoryObject>();
                        cache.put(obj.getId(), items);
                    }
                    items.add(obj);
                }
            }

            List<IRepositoryObject> result = cache.get(itemRecord.getProperty().getId());
            if (result == null) {
                result = Collections.EMPTY_LIST;
            }
            return result;
        }

        public void setItemLockState(ItemRecord itemRecord, boolean state) {
            lockState.put(itemRecord.getProperty().getId(), state);
        }

        public Boolean getItemLockState(ItemRecord itemRecord) {
            return lockState.get(itemRecord.getProperty().getId());
        }

        public void clear() {
            types.clear();
            cache.clear();
            lockState.clear();
        }
    }
}
