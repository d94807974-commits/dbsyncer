package org.realtimeetl.biz.checker;

import org.realtimeetl.common.model.ConfigModel;

import java.util.Map;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2020/1/7 23:17
 */
public interface Checker {

    /**
     * InsertConfigure
     */
    ConfigModel checkAddConfigModel(Map<String, String> params);

    /**
     * UpdateConfigure
     */
    ConfigModel checkEditConfigModel(Map<String, String> params);
}
