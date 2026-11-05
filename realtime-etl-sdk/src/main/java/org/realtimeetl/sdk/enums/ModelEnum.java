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
package org.realtimeetl.sdk.enums;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.SdkException;

/**
 * Sync Enumeration
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2023-11-16 00:51
 */
public enum ModelEnum {

    /**
     * Full
     */
    FULL("full", "Full"),
    /**
     * Incremental
     */
    INCREMENT("increment", "Incremental"),
    /**
     * Full + incremental (Full + incremental)
     */
    FULL_INCREMENT("fullIncrement", "Full + incremental");

    private final String code;
    private final String name;

    ModelEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ModelEnum getModelEnum(String code) throws SdkException {
        for (ModelEnum e : ModelEnum.values()) {
            if (StringUtil.equals(code, e.getCode())) {
                return e;
            }
        }
        throw new SdkException(String.format("Model code \"%s\" does not exist.", code));
    }

    public static boolean isFull(String model) {
        return StringUtil.equals(FULL.getCode(), model) || StringUtil.equals(FULL_INCREMENT.getCode(), model);
    }

    public static boolean isIncrement(String code) {
        return StringUtil.equals(INCREMENT.getCode(), code);
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
