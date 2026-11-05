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

import org.realtimeetl.parser.model.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * RolePresentation (included)PermissionsThe code.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-21
 */
public class RoleVO extends Role {

    /**
     * boundPermissionsVolume
     */
    private int permissionCount;

    /**
     * boundPermissionsCode
     */
    private List<String> permissionCodes = new ArrayList<>();

    public int getPermissionCount() {
        return permissionCount;
    }

    public void setPermissionCount(int permissionCount) {
        this.permissionCount = permissionCount;
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes;
    }
}
