/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz.impl;

import org.realtimeetl.biz.enums.PermissionModuleEnum;
import org.realtimeetl.parser.RbacProfile;
import org.realtimeetl.parser.model.Permission;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * RBAC Seeds: Filling Menu with ThreadsPermissionsPoint. Superbar pressusername=admin Identification, no presetRole。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-21
 */
@Component
public class RbacSeedService implements ApplicationListener<ContextRefreshedEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private RbacProfile rbacProfile;

    private volatile boolean seeded;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() != null || seeded) {
            return;
        }
        seeded = true;
        try {
            seed();
        } catch (Exception e) {
            logger.error("Initialization of the RBAC torrent failed", e);
        }
    }

    private void seed() {
        List<PermissionDef> defs = permissionDefs();
        for (PermissionDef def : defs) {
            ensurePermission(def);
        }
        logger.info("RBAC Feed Completion: missions=}", defs.size());
    }

    private Permission ensurePermission(PermissionDef def) {
        Permission existing = rbacProfile.getPermissionByCode(def.code);
        if (existing != null) {
            return existing;
        }
        long now = System.currentTimeMillis();
        Permission permission = new Permission();
        permission.setType(ConfigConstant.PERMISSION);
        permission.setCode(def.code);
        permission.setName(def.name);
        permission.setModule(def.module);
        permission.setCreateTime(now);
        permission.setUpdateTime(now);
        rbacProfile.addPermission(permission);
        return rbacProfile.getPermissionByCode(def.code);
    }

    private static List<PermissionDef> permissionDefs() {
        return Arrays.asList(
                new PermissionDef("user:view", "View Users", PermissionModuleEnum.USER),
                new PermissionDef("user:edit", "Edit User", PermissionModuleEnum.USER),
                new PermissionDef("connector:view", "View Connector", PermissionModuleEnum.CONNECTOR),
                new PermissionDef("connector:edit", "Edit Connector", PermissionModuleEnum.CONNECTOR),
                new PermissionDef("mapping:view", "View Sync Tasks", PermissionModuleEnum.MAPPING),
                new PermissionDef("mapping:edit", "Edit Sync Tasks", PermissionModuleEnum.MAPPING),
                new PermissionDef("mapping:operate", "Synchronising folder", PermissionModuleEnum.MAPPING),
                new PermissionDef("notice:view", "View alarm configuration", PermissionModuleEnum.NOTICE),
                new PermissionDef("notice:edit", "Edit alarm configuration", PermissionModuleEnum.NOTICE),
                new PermissionDef("license:view", "View Conflicts", PermissionModuleEnum.LICENSE),
                new PermissionDef("license:edit", "Management delegation of authority", PermissionModuleEnum.LICENSE),
                new PermissionDef("system:view", "Extensive System Configuration", PermissionModuleEnum.SYSTEM),
                new PermissionDef("system:edit", "Edit System Configuration", PermissionModuleEnum.SYSTEM),
                new PermissionDef("acl:view", "View Resource Authorizations", PermissionModuleEnum.ACL),
                new PermissionDef("acl:edit", "Edit Resource Authorization", PermissionModuleEnum.ACL)
        );
    }

    private static final class PermissionDef {
        private final String code;
        private final String name;
        private final String module;

        private PermissionDef(String code, String name, PermissionModuleEnum module) {
            this.code = code;
            this.name = name;
            this.module = module.getCode();
        }
    }
}
