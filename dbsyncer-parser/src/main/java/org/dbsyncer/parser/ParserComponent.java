package org.dbsyncer.parser;

import org.dbsyncer.common.model.Result;
import org.dbsyncer.parser.model.Mapping;
import org.dbsyncer.parser.model.TableGroup;
import org.dbsyncer.sdk.connector.DefaultConnectorServiceContext;
import org.dbsyncer.sdk.model.MetaInfo;
import org.dbsyncer.sdk.model.Task;
import org.dbsyncer.sdk.model.ValidateSyncTask;
import org.dbsyncer.sdk.plugin.PluginContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * <pre>
 *     AchievedFullandIncrementalSync task，andConnectorRead and write,PluginConvert，TableMetaInfo，Heart rate.Test
 * </pre>
 *
 * @author AE86
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
