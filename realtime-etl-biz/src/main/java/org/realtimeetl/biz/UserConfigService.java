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
package org.realtimeetl.biz;

import org.realtimeetl.biz.vo.UserInfoVO;
import org.realtimeetl.parser.model.UserConfig;
import org.realtimeetl.parser.model.UserInfo;

import java.util.List;
import java.util.Map;

/**
 * UserConfigureService
 *
 * @author Realtime ETL
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
