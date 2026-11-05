/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz.vo;

import org.realtimeetl.parser.model.Permission;

/**
 * PermissionsPoint show.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-21
 */
public class PermissionVO extends Permission {

    /**
     * ModulePresentation Name
     */
    private String moduleName;

    /**
     * YesNoGranted to CurrentEditRole
     */
    private boolean granted;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }
}
