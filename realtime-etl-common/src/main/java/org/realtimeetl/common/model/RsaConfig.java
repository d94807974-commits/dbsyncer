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
package org.realtimeetl.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * RSAConfigure
 *
 * @version 1.0.0
 * @Author Realtime ETL.
 * @Date 2026-01-15 08:30
 */
public class RsaConfig {

    private final List<RsaVersion> rsaVersions = new ArrayList<>();

    public List<RsaVersion> getRsaVersions() {
        return rsaVersions;
    }
}