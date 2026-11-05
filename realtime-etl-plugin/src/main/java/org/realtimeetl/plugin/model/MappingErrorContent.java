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

import org.realtimeetl.sdk.enums.ModelEnum;
import org.realtimeetl.sdk.enums.NoticeTypeEnum;
import org.realtimeetl.sdk.model.NoticeContent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MappingOrganisation
 *
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2026-03-04 19:00
 */
public final class MappingErrorContent extends NoticeContent {

    private List<ErrorItem> errorItems;

    public MappingErrorContent() {
        setNoticeType(NoticeTypeEnum.MAPPING_ERROR);
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
        private ModelEnum model;
        private AtomicLong total;
        private AtomicLong success;
        private AtomicLong fail;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ModelEnum getModel() {
            return model;
        }

        public void setModel(ModelEnum model) {
            this.model = model;
        }

        public AtomicLong getTotal() {
            return total;
        }

        public void setTotal(AtomicLong total) {
            this.total = total;
        }

        public AtomicLong getSuccess() {
            return success;
        }

        public void setSuccess(AtomicLong success) {
            this.success = success;
        }

        public AtomicLong getFail() {
            return fail;
        }

        public void setFail(AtomicLong fail) {
            this.fail = fail;
        }
    }

}
