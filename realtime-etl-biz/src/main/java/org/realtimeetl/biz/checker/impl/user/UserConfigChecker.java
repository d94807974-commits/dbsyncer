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
package org.realtimeetl.biz.checker.impl.user;

import org.realtimeetl.biz.BizException;
import org.realtimeetl.biz.checker.AbstractChecker;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.parser.model.UserConfig;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2020/1/8 15:17
 */
@Component
public class UserConfigChecker extends AbstractChecker {

    @Override
    public ConfigModel checkAddConfigModel(Map<String, String> params) {
        UserConfig config = new UserConfig();
        config.setName("User Configuration");

        // UpdateBasicConfigure
        this.modifyConfigModel(config, params);
        return config;
    }

    @Override
    public ConfigModel checkEditConfigModel(Map<String, String> params) {
        throw new BizException("Unsupported method");
    }
}
