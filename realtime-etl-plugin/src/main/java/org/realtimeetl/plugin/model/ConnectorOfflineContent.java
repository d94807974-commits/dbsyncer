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
package org.realtimeetl.plugin.model;


import org.realtimeetl.sdk.enums.NoticeTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Connect OfflineInfo
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-03-07 19:00
 */
public final class ConnectorOfflineContent extends org.realtimeetl.sdk.model.NoticeContent {

    private List<ErrorItem> errorItems;

    public ConnectorOfflineContent() {
        setNoticeType(NoticeTypeEnum.CONNECTOR_OFFLINE);
    }

    public void addErrorItem(ErrorItem item) {
        if (errorItems == null) {
            errorItems = new ArrayList<>();
        }
        errorItems.add(item);
    }

    public List<ErrorItem> getErrorItems() {
        return errorItems;
    }

    public static class ErrorItem {
        private String name;
        private String type;
        private String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

}
