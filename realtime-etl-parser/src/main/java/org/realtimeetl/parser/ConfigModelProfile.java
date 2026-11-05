/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser;

import org.realtimeetl.common.model.ConfigModel;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-09-22 21:02
 */
public interface ConfigModelProfile {

    ConfigModel getCache(String id);

    void removeCacheAndNotice(String id);

    void removeCache(String id);

    void pushMessage(String id);

}