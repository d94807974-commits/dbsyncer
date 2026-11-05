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
 * API KeyConfigure（Client voucherConfigure）
 * <p>
 * SupportINTERVersionKey Recommends: Key Management, for smooth rotation.
 * Client uses this key to authenticate, accessJWT Token。
 * </p>
 *
 * @author Realtime ETL.
 * @version 2.0.0
 */
public class ApiKeyConfig {

    /**
     * API KeyThere could be more.Version，Support smooth rotation
     */
    private final List<SecretVersion> secrets = new ArrayList<>();

    public List<SecretVersion> getSecrets() {
        return secrets;
    }
}
