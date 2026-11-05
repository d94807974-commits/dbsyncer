/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.sdk.listener;

import org.realtimeetl.sdk.enums.ModelEnum;
import org.realtimeetl.sdk.plugin.AbstractPluginContext;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2024-12-05 01:07
 */
public final class QuartzListenerContext extends AbstractPluginContext {

    @Override
    public ModelEnum getModelEnum() {
        return ModelEnum.INCREMENT;
    }
}
