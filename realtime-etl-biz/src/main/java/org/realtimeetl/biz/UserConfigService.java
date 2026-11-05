/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.biz;

import org.realtimeetl.biz.vo.UserInfoVO;
import org.realtimeetl.parser.model.UserConfig;
import org.realtimeetl.parser.model.UserInfo;

import java.util.List;
import java.util.Map;

/**
 * UserConfigureService
 *
 * @author AE86
 * @version 1.0.0
 * @date 2022/11/17 0:16
 */
public interface UserConfigService {

    /**
     * AccessLoginUserPropertiesKEY
     */
    String CURRENT_USER_NAME = "currentUserName";

    /**
     * InsertUser(Only administrators canInsertUser)
     *
     * @param params
     */
    String add(Map<String, String> params);

    /**
     * UpdateUser(The administrator can.UpdateAllUser，NormalUserOnlyUpdateFor yourself.)
     *
     * @param params
     */
    String edit(Map<String, String> params);

    /**
     * DeleteUser(Only administrators canDeleteNormalUser)
     *
     * @param params
     */
    String remove(Map<String, String> params);

    /**
     * AccessLoginUserPassword
     *
     * @param currentUserName LoginUser
     * @return
     */
    UserInfo getUserInfo(String currentUserName);

    /**
     * AccessLoginUserInfoVO(The administrator can.ViewAllUser，NormalUserOnlyViewFor yourself.)
     *
     * @param currentUserName LoginUser
     * @param username        QueryUser
     * @return
     */
    UserInfoVO getUserInfoVo(String currentUserName, String username);

    /**
     * Get AllUserInfoVO(SystemThe administrator can.ViewAllUser，OtherUserOnlyViewFor yourself.)
     *
     * @param currentUserName LoginUser
     * @return
     */
    List<UserInfoVO> getUserInfoAll(String currentUserName);

    /**
     * AccessUser Configuration
     *
     * @return
     */
    UserConfig getUserConfig();

    /**
     * 71. Get DefaultUser(admin)
     *
     * @return
     */
    UserInfo getDefaultUser();
}
