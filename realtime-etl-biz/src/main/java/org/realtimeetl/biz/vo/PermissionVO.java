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
package org.realtimeetl.biz.vo;

import org.realtimeetl.parser.model.Permission;

/**
 * PermissionsPoint show.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-21
 */
public class PermissionVO extends Permission {

    /**
     * ModulePresentation Name
     */
    private String moduleName;

    /**
     * YesNoGranted to CurrentEditRole
     */
    private boolean granted;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }
}
