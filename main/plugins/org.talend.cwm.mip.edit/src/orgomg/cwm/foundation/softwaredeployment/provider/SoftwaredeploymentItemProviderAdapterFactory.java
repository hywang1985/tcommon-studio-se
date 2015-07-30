/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package orgomg.cwm.foundation.softwaredeployment.provider;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.edit.provider.ChangeNotifier;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.IChangeNotifier;
import org.eclipse.emf.edit.provider.IDisposable;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.INotifyChangedListener;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import orgomg.cwm.foundation.softwaredeployment.util.SoftwaredeploymentAdapterFactory;

/**
 * This is the factory that is used to provide the interfaces needed to support Viewers.
 * The adapters generated by this factory convert EMF adapter notifications into calls to {@link #fireNotifyChanged fireNotifyChanged}.
 * The adapters also support Eclipse property sheets.
 * Note that most of the adapters are shared among multiple instances.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class SoftwaredeploymentItemProviderAdapterFactory extends SoftwaredeploymentAdapterFactory implements ComposeableAdapterFactory, IChangeNotifier, IDisposable {
    /**
     * This keeps track of the root adapter factory that delegates to this adapter factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected ComposedAdapterFactory parentAdapterFactory;

    /**
     * This is used to implement {@link org.eclipse.emf.edit.provider.IChangeNotifier}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected IChangeNotifier changeNotifier = new ChangeNotifier();

    /**
     * This keeps track of all the supported types checked by {@link #isFactoryForType isFactoryForType}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected Collection<Object> supportedTypes = new ArrayList<Object>();

    /**
     * This constructs an instance.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public SoftwaredeploymentItemProviderAdapterFactory() {
        supportedTypes.add(IEditingDomainItemProvider.class);
        supportedTypes.add(IStructuredItemContentProvider.class);
        supportedTypes.add(ITreeItemContentProvider.class);
        supportedTypes.add(IItemLabelProvider.class);
        supportedTypes.add(IItemPropertySource.class);
    }

    /**
     * This keeps track of the one adapter used for all {@link orgomg.cwm.foundation.softwaredeployment.Site} instances.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected SiteItemProvider siteItemProvider;

    /**
     * This creates an adapter for a {@link orgomg.cwm.foundation.softwaredeployment.Site}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Adapter createSiteAdapter() {
        if (siteItemProvider == null) {
            siteItemProvider = new SiteItemProvider(this);
        }

        return siteItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link orgomg.cwm.foundation.softwaredeployment.Machine} instances.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected MachineItemProvider machineItemProvider;

    /**
     * This creates an adapter for a {@link orgomg.cwm.foundation.softwaredeployment.Machine}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Adapter createMachineAdapter() {
        if (machineItemProvider == null) {
            machineItemProvider = new MachineItemProvider(this);
        }

        return machineItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link orgomg.cwm.foundation.softwaredeployment.SoftwareSystem} instances.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected SoftwareSystemItemProvider softwareSystemItemProvider;

    /**
     * This creates an adapter for a {@link orgomg.cwm.foundation.softwaredeployment.SoftwareSystem}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Adapter createSoftwareSystemAdapter() {
        if (softwareSystemItemProvider == null) {
            softwareSystemItemProvider = new SoftwareSystemItemProvider(this);
        }

        return softwareSystemItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link orgomg.cwm.foundation.softwaredeployment.DeployedComponent} instances.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected DeployedComponentItemProvider deployedComponentItemProvider;

    /**
     * This creates an adapter for a {@link orgomg.cwm.foundation.softwaredeployment.DeployedComponent}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Adapter createDeployedComponentAdapter() {
        if (deployedComponentItemProvider == null) {
            deployedComponentItemProvider = new DeployedComponentItemProvider(this);
        }

        return deployedComponentItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link orgomg.cwm.foundation.softwaredeployment.DeployedSoftwareSystem} instances.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected DeployedSoftwareSystemItemProvider deployedSoftwareSystemItemProvider;

    /**
     * This creates an adapter for a {@link orgomg.cwm.foundation.softwaredeployment.DeployedSoftwareSystem}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Adapter createDeployedSoftwareSystemAdapter() {
        if (deployedSoftwareSystemItemProvider == null) {
            deployedSoftwareSystemItemProvider = new DeployedSoftwareSystemItemProvider(this);
        }

        return deployedSoftwareSystemItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link orgomg.cwm.foundation.softwaredeployment.DataManager} instances.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected DataManagerItemProvider dataManagerItemProvider;

    /**
     * This creates an adapter for a {@link orgomg.cwm.foundation.softwaredeployment.DataManager}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Adapter createDataManagerAdapter() {
        if (dataManagerItemProvider == null) {
            dataManagerItemProvider = new DataManagerItemProvider(this);
        }

        return dataManagerItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link orgomg.cwm.foundation.softwaredeployment.DataProvider} instances.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected DataProviderItemProvider dataProviderItemProvider;

    /**
     * This creates an adapter for a {@link orgomg.cwm.foundation.softwaredeployment.DataProvider}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Adapter createDataProviderAdapter() {
        if (dataProviderItemProvider == null) {
            dataProviderItemProvider = new DataProviderItemProvider(this);
        }

        return dataProviderItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link orgomg.cwm.foundation.softwaredeployment.ProviderConnection} instances.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected ProviderConnectionItemProvider providerConnectionItemProvider;

    /**
     * This creates an adapter for a {@link orgomg.cwm.foundation.softwaredeployment.ProviderConnection}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Adapter createProviderConnectionAdapter() {
        if (providerConnectionItemProvider == null) {
            providerConnectionItemProvider = new ProviderConnectionItemProvider(this);
        }

        return providerConnectionItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link orgomg.cwm.foundation.softwaredeployment.Component} instances.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected ComponentItemProvider componentItemProvider;

    /**
     * This creates an adapter for a {@link orgomg.cwm.foundation.softwaredeployment.Component}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Adapter createComponentAdapter() {
        if (componentItemProvider == null) {
            componentItemProvider = new ComponentItemProvider(this);
        }

        return componentItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link orgomg.cwm.foundation.softwaredeployment.PackageUsage} instances.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected PackageUsageItemProvider packageUsageItemProvider;

    /**
     * This creates an adapter for a {@link orgomg.cwm.foundation.softwaredeployment.PackageUsage}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Adapter createPackageUsageAdapter() {
        if (packageUsageItemProvider == null) {
            packageUsageItemProvider = new PackageUsageItemProvider(this);
        }

        return packageUsageItemProvider;
    }

    /**
     * This returns the root adapter factory that contains this factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ComposeableAdapterFactory getRootAdapterFactory() {
        return parentAdapterFactory == null ? this : parentAdapterFactory.getRootAdapterFactory();
    }

    /**
     * This sets the composed adapter factory that contains this factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setParentAdapterFactory(ComposedAdapterFactory parentAdapterFactory) {
        this.parentAdapterFactory = parentAdapterFactory;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean isFactoryForType(Object type) {
        return supportedTypes.contains(type) || super.isFactoryForType(type);
    }

    /**
     * This implementation substitutes the factory itself as the key for the adapter.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Adapter adapt(Notifier notifier, Object type) {
        return super.adapt(notifier, this);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object adapt(Object object, Object type) {
        if (isFactoryForType(type)) {
            Object adapter = super.adapt(object, type);
            if (!(type instanceof Class) || (((Class<?>)type).isInstance(adapter))) {
                return adapter;
            }
        }

        return null;
    }

    /**
     * This adds a listener.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void addListener(INotifyChangedListener notifyChangedListener) {
        changeNotifier.addListener(notifyChangedListener);
    }

    /**
     * This removes a listener.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void removeListener(INotifyChangedListener notifyChangedListener) {
        changeNotifier.removeListener(notifyChangedListener);
    }

    /**
     * This delegates to {@link #changeNotifier} and to {@link #parentAdapterFactory}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void fireNotifyChanged(Notification notification) {
        changeNotifier.fireNotifyChanged(notification);

        if (parentAdapterFactory != null) {
            parentAdapterFactory.fireNotifyChanged(notification);
        }
    }

    /**
     * This disposes all of the item providers created by this factory. 
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void dispose() {
        if (siteItemProvider != null) siteItemProvider.dispose();
        if (machineItemProvider != null) machineItemProvider.dispose();
        if (softwareSystemItemProvider != null) softwareSystemItemProvider.dispose();
        if (deployedComponentItemProvider != null) deployedComponentItemProvider.dispose();
        if (deployedSoftwareSystemItemProvider != null) deployedSoftwareSystemItemProvider.dispose();
        if (dataManagerItemProvider != null) dataManagerItemProvider.dispose();
        if (dataProviderItemProvider != null) dataProviderItemProvider.dispose();
        if (providerConnectionItemProvider != null) providerConnectionItemProvider.dispose();
        if (componentItemProvider != null) componentItemProvider.dispose();
        if (packageUsageItemProvider != null) packageUsageItemProvider.dispose();
    }

}