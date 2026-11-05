package org.realtimeetl.plugin.impl;

import org.realtimeetl.sdk.enums.ModelEnum;
import org.realtimeetl.sdk.plugin.AbstractPluginContext;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2022/6/30 16:06
 */
public final class IncrementPluginContext extends AbstractPluginContext {

    @Override
    public ModelEnum getModelEnum() {
        return ModelEnum.INCREMENT;
    }
}
