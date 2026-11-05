/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.parser.model;

import org.dbsyncer.common.model.ConfigModel;
import org.dbsyncer.sdk.constant.ConfigConstant;

/**
 * Business group（{@code dbsyncer_group}）。
 * <p>Don't.andMap{@link TableGroup} Confusion.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-14 18:00
 */
public class Group extends ConfigModel {

    public Group() {
        super.setType(ConfigConstant.GROUP);
    }
}
