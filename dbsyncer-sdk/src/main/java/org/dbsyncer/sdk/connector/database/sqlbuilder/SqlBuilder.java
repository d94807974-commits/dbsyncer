/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.dbsyncer.sdk.connector.database.sqlbuilder;

import org.dbsyncer.sdk.config.SqlBuilderConfig;

/**
 * SQLGenerateDevice
 *
 * @author AE86
 * @version 1.0.0
 * @date 2019/9/26 23:49
 */
public interface SqlBuilder {

    /**
     * GenerateSQL
     * @param config
     * @return
     */
    String buildSql(SqlBuilderConfig config);

    /**
     * GenerateQuestionSQL
     * @param config
     * @return
     */
    String buildQuerySql(SqlBuilderConfig config);
}
