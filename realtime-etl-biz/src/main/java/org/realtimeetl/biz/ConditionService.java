/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.biz;

import org.realtimeetl.biz.vo.ConditionVO;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2020/1/19 16:02
 */
public interface ConditionService {

    /**
     * AccessFilter Conditions
     *
     * @return
     */
    ConditionVO getCondition();

}