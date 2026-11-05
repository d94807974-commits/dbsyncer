/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.spi;

import org.realtimeetl.common.enums.CommonTaskTypeEnum;
import org.realtimeetl.common.message.CommonMessage;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.common.model.Paging;
import org.realtimeetl.sdk.model.ClusterNode;
import org.realtimeetl.sdk.model.PluginFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Cluster services:NodesManagementandTasksClass dispatch.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-08-18
 */
public interface ClusterService {

    default void init() {
    }

    default boolean isStandalone() {
        return true;
    }

    default String getLocalNodeId() {
        return "standalone";
    }

    default ClusterNode getNode(String nodeId) {
        return null;
    }

    default Paging<ClusterNode> query(Map<String, String> params) {
        return null;
    }

    default void updateNodeName(String nodeId, String name) {
    }

    default void removeNode(String nodeId) {
    }

    default void start(ConfigModel configModel, boolean autoRecovery) {
    }

    /**
     * StopTasks。
     *
     * @param taskId   Tasks ID
     * @param taskType Task Type
     */
    default void stop(String taskId, CommonTaskTypeEnum taskType) {
    }

    default Object receiveMessage(String message) {
        return null;
    }

    default void pushMessage(CommonMessage message) {
    }

    /**
     * Put the machine on.PluginDocumentationSync toOtherNodes。
     *
     * @param fileName PluginFilename
     */
    default void publishPlugin(String fileName) {
    }

    /**
     * AnnouncementsOtherNodesDeleteAssignPluginDocumentation。
     *
     * @param fileName PluginFilename
     */
    default void removePlugin(String fileName) {
    }

    /**
     * Here.PluginIn DirectoryJAR List.
     *
     * @return Filename、/5/andSummary
     */
    default List<PluginFile> listPluginFiles() {
        return Collections.emptyList();
    }

    /**
     * Read HomePluginDocumentation。
     *
     * @param fileName Filename
     * @return Documentationbytes;when they do not existBack null
     */
    default byte[] readPluginFile(String fileName) {
        return null;
    }

    /**
     * ReceivedOtherNodesIt's a gift.PluginDocumentationAnd load.
     *
     * @param fileName Filename
     * @param content  DocumentationBytes
     * @param relay    Yestrue OrganisationNodesGo on.minSendOtherNodes
     */
    default void acceptPluginFile(String fileName, byte[] content, boolean relay) {
        throw new UnsupportedOperationException("No plugin file received for current deployment");
    }

    /**
     * ReceivedOtherNodesYes.PluginDeleteAnnouncements.
     *
     * @param fileName Filename
     * @param relay    Yestrue OrganisationNodesKeep calling.OtherNodes
     */
    default void acceptPluginRemove(String fileName, boolean relay) {
        throw new UnsupportedOperationException("Do not accept plugins for current deployment");
    }

    default long forceExpireGracePeriod() {
        return 0L;
    }
}
