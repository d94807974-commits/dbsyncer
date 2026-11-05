/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.dbsyncer.sdk.storage;

/**
 * Create a collection of data repositoriesID
 * <pre>
 * /data
 * ----/config Connector、Mapping、RunStatus
 * ----/log Connector、Mapping、System
 * ----/data MappingReal timeSync data
 * --------/driver1
 * --------/driver2
 * --------/driver...
 * </pre>
 *
 * @Author AE86
 * @Version 1.0.0
 * @Date 2019-11-15 22:31
 */
public interface Strategy {

    /**
     * CreateminSnippets
     *
     * @param separator
     * @param collectionId
     * @return
     */
    String createSharding(String separator, String collectionId);
}
