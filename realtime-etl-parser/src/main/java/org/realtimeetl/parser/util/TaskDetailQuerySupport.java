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
package org.realtimeetl.parser.util;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.ParserException;
import org.realtimeetl.parser.enums.TaskDetailOrderEnum;
import org.realtimeetl.parser.enums.TaskDetailStatusEnum;
import org.realtimeetl.parser.model.TaskDetailQuery;
import org.springframework.util.Assert;

import java.util.List;
import java.util.regex.Pattern;

/**
 * TasksDetailed queriesSQL Snippet Collapse()WHERE / ORDER BY / taskId Similarly, it can be easily measured.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-07-31 18:20
 */
public final class TaskDetailQuerySupport {

    private static final Pattern TASK_ID_PATTERN = Pattern.compile("^[0-9A-Za-z]+$");

    private TaskDetailQuerySupport() {
    }

    /**
     * Verify INTRODUCTIONTasks ID（Allow alphanumeric only to preventminTable nameInjection.
     */
    public static void assertTaskId(String taskId) {
        if (StringUtil.isBlank(taskId) || !TASK_ID_PATTERN.matcher(taskId).matches()) {
            throw new ParserException("Illegal task ID");
        }
    }

    /**
     * ParsingORDER BY：VisibleorderBy &gt; statusMetric Default Sorting&gt; UPDATE_TIME。
     */
    public static String resolveOrderSql(TaskDetailQuery query) {
        Assert.notNull(query, "Query parameters cannot be empty");
        if (query.getOrderBy() != null) {
            return query.getOrderBy().getSql();
        }
        if (query.getStatusMetric() != null) {
            return query.getStatusMetric().getOrderSql();
        }
        return TaskDetailOrderEnum.UPDATE_TIME.getSql();
    }

    /**
     * CollapseWHERE，Andargs Additional positionParameter。
     */
    public static String buildWhere(TaskDetailQuery query, List<Object> args) {
        Assert.notNull(query, "Query parameters cannot be empty");
        Assert.notNull(args, "The argument list cannot be empty");
        StringBuilder where = new StringBuilder("WHERE tg.TASK_ID = ? ");
        args.add(query.getTaskId());
        if (StringUtil.isNotBlank(query.getDetailId())) {
            where.append("AND d.ID = ? ");
            args.add(query.getDetailId());
        }
        if (StringUtil.isNotBlank(query.getDetailType())) {
            where.append("AND d.TYPE = ? ");
            args.add(query.getDetailType());
        }
        if (query.getDetailStatus() != null) {
            Assert.notNull(query.getStatusMetric(), "StatusMetric cannot be empty during status filtering");
            String column = query.getStatusMetric().getColumn();
            if (query.getDetailStatus() == TaskDetailStatusEnum.SUCCESS) {
                where.append("AND ").append(column).append(" = 0 ");
            } else if (query.getDetailStatus() == TaskDetailStatusEnum.FAIL) {
                where.append("AND ").append(column).append(" > 0 ");
            }
        }
        return where.toString();
    }
}
