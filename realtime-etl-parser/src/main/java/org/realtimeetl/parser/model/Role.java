/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser.model;

import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.sdk.constant.ConfigConstant;

/**
 * Role。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-21
 */
public class Role extends ConfigModel {

    public Role() {
        super.setType(ConfigConstant.ROLE);
    }
}
