/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz.vo;

import org.realtimeetl.parser.model.ResourceAcl;

/**
 * Resource authorizationShow.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-21
 */
public class ResourceAclVO extends ResourceAcl {

    private String subjectName;
    private String subjectTypeName;
    private String resourceName;
    private String permissionName;

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectTypeName() {
        return subjectTypeName;
    }

    public void setSubjectTypeName(String subjectTypeName) {
        this.subjectTypeName = subjectTypeName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }
}
