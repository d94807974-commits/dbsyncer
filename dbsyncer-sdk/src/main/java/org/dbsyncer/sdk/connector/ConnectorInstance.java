/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.dbsyncer.sdk.connector;

import org.dbsyncer.sdk.model.ConnectorConfig;

/**
 * ConnectorExamples, managementConnectLife cycle
 *
 * @param <K> Configure
 * @param <V> Examples
 * @author AE86
 * @version 1.0.0
 * @date 2022/3/20 23:00
 */
public interface ConnectorInstance<K extends ConnectorConfig, V> extends Cloneable {

    /**
     * Access to service addresses
     *
     * @return
     */
    String getServiceUrl();

    /**
     * AccessConnectConfigure
     *
     * @return
     */
    K getConfig();

    /**
     * Settings
     *
     * @param k
     */
    void setConfig(K k);

    /**
     * AccessConnectChannel Examples
     *
     * @return
     * @throws Exception
     */
    V getConnection() throws Exception;

    /**
     * CloseConnector
     */
    void close();

    /**
     * Light copyConnector
     *
     * @return
     * @throws CloneNotSupportedException
     */
    Object clone() throws CloneNotSupportedException;
}
