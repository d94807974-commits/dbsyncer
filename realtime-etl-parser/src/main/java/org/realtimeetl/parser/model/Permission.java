/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser.model;

import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.sdk.constant.ConfigConstant;

/**
 * MenuPermissions。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-21
 */
public class Permission extends ConfigModel {

    private String code;
    private String module;

    public Permission() {
        super.setType(ConfigConstant.PERMISSION);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
}
