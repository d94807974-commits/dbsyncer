package org.realtimeetl.parser;

import org.realtimeetl.common.model.Result;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.sdk.connector.DefaultConnectorServiceContext;
import org.realtimeetl.sdk.model.MetaInfo;
import org.realtimeetl.sdk.model.Task;
import org.realtimeetl.sdk.model.ValidateSyncTask;
import org.realtimeetl.sdk.plugin.PluginContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * <pre>
 *     AchievedFullandIncrementalSync task，andConnectorRead and write,PluginConvert，TableMetaInfo，Heart rate.Test
 * </pre>
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019/10/20 21:11
 */
public interface ParserComponent {

    /**
     * Get TableMetaInfo
     */
    List<MetaInfo> getMetaInfo(DefaultConnectorServiceContext context);

    /**
     * AccessMappingExecute Command
     */
    Map<String, String> getCommand(Mapping mapping, TableGroup tableGroup);

    /**
     * Revised verification tasks：By source/Target ConnectionDeviceGenerateEndosulfan
     */
    Map<String, String> getCommand(ValidateSyncTask task, TableGroup tableGroup);

    /**
     * Full sync
     */
    void execute(Task task, Mapping mapping, TableGroup tableGroup, Executor executor);

    /**
     * Batch execution
     */
    Result writeBatch(PluginContext pluginContext, Executor executor);
}
