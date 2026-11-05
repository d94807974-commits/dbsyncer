/**
 * Copyright 2026 Realtime ETL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.realtimeetl.sdk.connector;

import org.realtimeetl.sdk.model.ConnectorConfig;

/**
 * ConnectorExamples, managementConnectLife cycle
 *
 * @param <K> Configure
 * @param <V> Examples
 * @author Realtime ETL
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
