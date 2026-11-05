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
package org.realtimeetl.biz.task;

import org.realtimeetl.biz.TableGroupService;
import org.realtimeetl.common.enums.DispatchTaskEnum;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.TableGroupProfile;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Count sync task tablesTasks
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2025-06-24 01:23
 */
@Service
public final class TableGroupCountTask extends AbstractCountTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private TableGroupProfile tableGroupProfile;

    @Resource
    private TableGroupService tableGroupService;

    private String mappingId;

    private List<String> tableGroups;

    @Override
    public void execute() throws Exception {
        Mapping mapping = mappingProfile.get(mappingId);
        int groupCount = tableGroupProfile.getTableGroupCount(mappingId);
        logger.info("Counting {}: {} tables", mapping.getName(), groupCount);
        if (!CollectionUtils.isEmpty(tableGroups)) {
            for (String tableGroupId : tableGroups) {
                // Task TypeSwitches, early release.Tasks
                if (shouldStop(mappingId)) {
                    return;
                }
                updateTableGroupCount(mapping, tableGroupProfile.getTableGroup(tableGroupId));
            }
        }
        Meta meta = tableGroupService.refreshMetaTotal(mapping, null);
        logger.info("Completed statistics: }, } sheets, total: {", mapping.getName(), groupCount, meta.getTotal());
    }

    @Override
    public String getUniqueId() {
        return CollectionUtils.isEmpty(tableGroups) ? mappingId : Integer.toHexString(tableGroups.hashCode());
    }

    @Override
    public DispatchTaskEnum getType() {
        return DispatchTaskEnum.TABLE_GROUP_COUNT;
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }

    public void setTableGroups(List<String> tableGroups) {
        this.tableGroups = tableGroups;
    }
}
