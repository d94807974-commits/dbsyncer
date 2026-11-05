/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.parser.ddl;

import net.sf.jsqlparser.JSQLParserException;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.sdk.config.DDLConfig;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.spi.ConnectorService;

public interface DDLParser {

    DDLConfig parse(ConnectorInstance connectorInstance, ConnectorService connectorService, TableGroup tableGroup, String sql) throws JSQLParserException;

    void refreshFiledMappings(TableGroup tableGroup, DDLConfig targetDDLConfig);
}
