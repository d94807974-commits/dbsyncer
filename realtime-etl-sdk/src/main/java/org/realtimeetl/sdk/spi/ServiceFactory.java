/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.realtimeetl.sdk.spi;

/**
 * SPITRC Service plant, centralized management, increased load efficiency
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2025-08-09 00:48
 */
public interface ServiceFactory {

    <T> T get(Class<T> serviceClass);
}
