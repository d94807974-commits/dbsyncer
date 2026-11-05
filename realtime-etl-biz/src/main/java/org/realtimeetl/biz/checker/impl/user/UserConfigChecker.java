/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.biz.checker.impl.user;

import org.realtimeetl.biz.BizException;
import org.realtimeetl.biz.checker.AbstractChecker;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.parser.model.UserConfig;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author AE86
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
