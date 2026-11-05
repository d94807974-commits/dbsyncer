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
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Count sync tasksTasks
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2025-06-13 00:00
 */
@Service
public final class MappingCountTask extends AbstractCountTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private TableGroupProfile tableGroupProfile;

    @Resource
    private TableGroupService tableGroupService;

    private String mappingId;

    private String metaSnapshot;

    @Override
    public String getUniqueId() {
        return mappingId;
    }

    @Override
    public DispatchTaskEnum getType() {
        return DispatchTaskEnum.MAPPING_COUNT;
    }

    @Override
    public void execute() {
        Mapping mapping = mappingProfile.get(mappingId);
        int groupCount = tableGroupProfile.getTableGroupCount(mappingId);
        logger.info("Counting {}: {} tables", mapping.getName(), groupCount);
        if (groupCount > 0) {
            tableGroupProfile.pageScanTableGroups(mappingId, ConfigConstant.PAGE_SIZE, page -> {
                if (CollectionUtils.isEmpty(page)) {
                    return;
                }
                for (TableGroup tableGroup : page) {
                    if (tableGroup == null) {
                        continue;
                    }
                    // Sync taskTypeSwitches, early release.Tasks
                    if (shouldStop(mappingId)) {
                        return;
                    }
                    updateTableGroupCount(mapping, tableGroup);
                }
            });
            Meta meta = tableGroupService.refreshMetaTotal(mapping, metaSnapshot);
            logger.info("Completed statistics: }, } sheets, total: {", mapping.getName(), groupCount, meta.getTotal());
        }
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }

    public void setMetaSnapshot(String metaSnapshot) {
        this.metaSnapshot = metaSnapshot;
    }
}
