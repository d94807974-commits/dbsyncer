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
package org.realtimeetl.parser.message;

import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.plugin.model.MappingFullCompleteContent;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.ModelEnum;
import org.realtimeetl.sdk.notice.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * PureFullNaturally.FinishSend notification after (single/)minPlankTotalUse it.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-27
 */
@Component
public class NoticeMappingCompletedPublisher {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private MessageService messageService;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    /**
     * SendFull SyncNotification;FailedJust rememberLog，Does not affect the main process.
     */
    public void publish(Mapping mapping) {
        if (mapping == null) {
            return;
        }
        try {
            MappingFullCompleteContent content = new MappingFullCompleteContent();
            content.setTitle("Full Sync");
            content.setName(mapping.getName());
            content.setModel(ModelEnum.getModelEnum(mapping.getModel()));
            content.setResourceType(ConfigConstant.MAPPING);
            content.setResourceId(mapping.getId());
            Meta meta = taskMetaProfile.getMeta(mapping.getId());
            if (meta != null) {
                content.setSuccess(meta.getSuccess().get());
                content.setFail(meta.getFail().get());
                content.setTotal(meta.getTotal().get());
            }
            messageService.sendMessage(content);
        } catch (Exception e) {
            logger.error("Could not close temporary folder: %s", e.getMessage(), e);
        }
    }
}
