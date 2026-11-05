package org.realtimeetl.biz.checker.impl.mapping;

import org.realtimeetl.biz.checker.MappingConfigChecker;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.sdk.config.ListenerConfig;
import org.realtimeetl.sdk.enums.ListenerTypeEnum;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * LogConfigure
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2020/1/8 15:17
 */
@Component
public class LogConfigChecker implements MappingConfigChecker {

    @Override
    public void modify(Mapping mapping, Map<String, String> params) {
        ListenerConfig listener = mapping.getListener();
        Assert.notNull(listener, "ListenerConfig can not be null.");

        listener.setListenerType(ListenerTypeEnum.LOG.getType());
    }
}
