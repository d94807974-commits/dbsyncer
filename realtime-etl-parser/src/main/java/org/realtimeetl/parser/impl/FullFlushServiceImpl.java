/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser.impl;

import org.realtimeetl.common.model.Result;
import org.realtimeetl.parser.event.FullRefreshEvent;
import org.realtimeetl.parser.strategy.FlushStrategy;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.Task;
import org.realtimeetl.sdk.schema.SchemaResolver;
import org.realtimeetl.sdk.spi.FullFlushService;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;
import java.util.Map;

/**
 * UnitFullPageLevelLibrary：Brush.Meta/Details and options for release progressEvent。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-14
 */
public class FullFlushServiceImpl implements FullFlushService {

    @Resource
    private FlushStrategy flushStrategy;

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public void flush(Task task, Result result, SchemaResolver targetSchemaResolver, Map<String, Field> targetFieldMap) {
        result.setTaskId(task.getId());
        result.setEvent(ConnectorConstant.OPERTION_INSERT);
        flushStrategy.flushFullData(result, targetSchemaResolver, targetFieldMap);
        if (!task.isSkipTableProgressEvent()) {
            applicationContext.publishEvent(new FullRefreshEvent(applicationContext, task));
        }
    }
}
