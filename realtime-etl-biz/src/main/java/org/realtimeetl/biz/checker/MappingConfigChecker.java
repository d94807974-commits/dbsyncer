package org.realtimeetl.biz.checker;

import org.realtimeetl.parser.model.Mapping;

import java.util.Map;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2020/1/7 23:17
 */
public interface MappingConfigChecker {

    /**
     * UpdateConfigure
     *
     * @param mapping
     * @param params
     * @return
     */
    void modify(Mapping mapping, Map<String, String> params);
}
