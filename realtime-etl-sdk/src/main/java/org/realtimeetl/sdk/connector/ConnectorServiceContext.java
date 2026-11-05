/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.realtimeetl.sdk.connector;

import org.realtimeetl.sdk.model.Table;

import java.util.List;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2025-10-25 16:32
 */
public interface ConnectorServiceContext {

    String getCatalog();

    String getSchema();

    List<Table> getTablePatterns();
}
