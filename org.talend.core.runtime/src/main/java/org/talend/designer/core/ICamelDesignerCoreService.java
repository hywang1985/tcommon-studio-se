// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core;

import org.eclipse.jface.action.IAction;
import org.talend.core.IService;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public interface ICamelDesignerCoreService extends IService {

    public IAction getCreateProcessAction(boolean isToolbar);
}
