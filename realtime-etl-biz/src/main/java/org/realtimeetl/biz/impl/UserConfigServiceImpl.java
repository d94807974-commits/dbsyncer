/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.biz.impl;

import org.realtimeetl.biz.BizException;
import org.realtimeetl.biz.PermissionChecker;
import org.realtimeetl.biz.PermissionService;
import org.realtimeetl.biz.ResourceAclService;
import org.realtimeetl.biz.UserConfigService;
import org.realtimeetl.biz.checker.impl.user.UserConfigChecker;
import org.realtimeetl.biz.enums.ResourceAclSubjectEnum;
import org.realtimeetl.biz.vo.UserInfoVO;
import org.realtimeetl.common.util.SHA1Util;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.LogService;
import org.realtimeetl.parser.LogType;
import org.realtimeetl.parser.RbacProfile;
import org.realtimeetl.parser.UserProfile;
import org.realtimeetl.parser.model.Role;
import org.realtimeetl.parser.model.UserConfig;
import org.realtimeetl.parser.model.UserInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2022/11/17 0:16
 */
@Service
public class UserConfigServiceImpl implements UserConfigService {

    /**
     * Default superbarAccount（and {@link PermissionService#isAdmin} (d) Unanimously.
     */
    public static final String DEFAULT_USERNAME = "admin";

    private static final String DEFAULT_PASSWORD = "0DPiKuNIrrVmD8IUCuw1hQxNqZc=";

    private static final String PERM_USER_VIEW = "user:view";

    private static final String PERM_USER_EDIT = "user:edit";

    @Resource
    private UserProfile userProfile;

    @Resource
    private RbacProfile rbacProfile;

    @Resource
    private UserConfigChecker userConfigChecker;

    @Resource
    private LogService logService;

    @Resource
    private PermissionService permissionService;

    @Resource
    private PermissionChecker permissionChecker;

    @Resource
    private ResourceAclService resourceAclService;

    @Override
    public synchronized String add(Map<String, String> params) {
        String username = params.get("username");
        Assert.hasText(username, "The username is null.");
        String nickname = params.get("nickname");
        Assert.hasText(nickname, "The nickname is null.");
        String password = params.get("password");
        Assert.hasText(password, "The password is null.");

        String currentUserName = params.get(UserConfigService.CURRENT_USER_NAME);
        permissionChecker.requirePermission(currentUserName, PERM_USER_EDIT);

        UserConfig userConfig = getUserConfig();
        UserInfo currentUser = userConfig.getUserInfo(currentUserName);
        Assert.notNull(currentUser, "Unable to get logged-in user.");
        Assert.isNull(userConfig.getUserInfo(username), "User already exists, change account number");

        Assert.hasText(params.get("roleId"), "Please select a role");
        String roleId = resolveAssignableRoleId(params.get("roleId"), null);

        userConfig.getUserInfoList().add(new UserInfo(username, nickname, SHA1Util.b64_sha1(password),
                StringUtil.EMPTY));

        String result = userProfile.saveUserConfig(userConfig);
        UserInfo created = getUserConfig().getUserInfo(username);
        if (created != null && StringUtil.isNotBlank(created.getId())) {
            permissionService.replaceUserRole(created.getId(), roleId);
        }
        return result;
    }

    @Override
    public synchronized String edit(Map<String, String> params) {
        String username = params.get("username");
        Assert.hasText(username, "The username is null.");
        String nickname = params.get("nickname");
        Assert.hasText(nickname, "The nickname is null.");
        String newPwd = params.get("newPwd");

        String currentUserName = params.get(UserConfigService.CURRENT_USER_NAME);
        UserConfig userConfig = getUserConfig();
        UserInfo currentUser = userConfig.getUserInfo(currentUserName);
        Assert.notNull(currentUser, "Unable to get logged-in user.");
        boolean canEditOthers = permissionService.hasPermission(currentUserName, PERM_USER_EDIT);
        boolean self = StringUtil.equals(currentUser.getUsername(), username);
        Assert.isTrue(canEditOthers || self, "No permission.");

        UserInfo updateUser = self ? currentUser : userConfig.getUserInfo(username);
        Assert.notNull(updateUser, "User does not exist");

        updateUser.setNickname(nickname);
        if (StringUtil.isNotBlank(newPwd)) {
            if (self) {
                String oldPwd = params.get("oldPwd");
                Assert.hasText(oldPwd, "The old code can't be empty.");
                if (!StringUtil.equals(SHA1Util.b64_sha1(oldPwd), updateUser.getPassword())) {
                    logService.log(LogType.SystemLog.ERROR, String.format("Could not close temporary folder Utilizing password failed", username));
                    throw new BizException("Change password failed.");
                }
            }
            newPwd = SHA1Util.b64_sha1(newPwd);
            Assert.isTrue(!StringUtil.equals(newPwd, updateUser.getPassword()), "New and old codes can't be exactly the same.");
            updateUser.setPassword(newPwd);
        }

        if (canEditOthers && params.containsKey("roleId")) {
            String roleId = resolveAssignableRoleId(params.get("roleId"),
                    permissionService.resolvePrimaryRoleId(updateUser.getId()));
            String oldRoleId = permissionService.resolvePrimaryRoleId(updateUser.getId());
            if (!StringUtil.equals(oldRoleId, roleId)) {
                if (StringUtil.isNotBlank(updateUser.getId())) {
                    permissionService.replaceUserRole(updateUser.getId(), roleId);
                }
            }
        }

        return userProfile.saveUserConfig(userConfig);
    }

    @Override
    public synchronized String remove(Map<String, String> params) {
        String username = params.get("username");
        Assert.hasText(username, "The username is null.");

        String currentUserName = params.get(UserConfigService.CURRENT_USER_NAME);
        permissionChecker.requirePermission(currentUserName, PERM_USER_EDIT);

        UserConfig userConfig = getUserConfig();
        UserInfo currentUser = userConfig.getUserInfo(currentUserName);
        Assert.notNull(currentUser, "Unable to get logged-in user.");
        Assert.isTrue(!StringUtil.equals(currentUser.getUsername(), username), "Can't delete yourself.");
        Assert.isTrue(!StringUtil.equals(DEFAULT_USERNAME, username), "The default administrator account cannot be deleted.");

        UserInfo deleteUser = userConfig.getUserInfo(username);
        Assert.notNull(deleteUser, "User deleted.");
        if (StringUtil.isNotBlank(deleteUser.getId())) {
            permissionService.unbindAllUserRoles(deleteUser.getId());
            resourceAclService.revokeAllForSubject(ResourceAclSubjectEnum.USER.getCode(), deleteUser.getId());
        }
        userConfig.removeUserInfo(username);
        userProfile.saveUserConfig(userConfig);
        return "Delete user success!";
    }

    @Override
    public UserInfo getUserInfo(String currentUserName) {
        return getUserConfig().getUserInfo(currentUserName);
    }

    @Override
    public UserInfoVO getUserInfoVo(String currentUserName, String username) {
        UserConfig userConfig = getUserConfig();
        UserInfo currentUser = userConfig.getUserInfo(currentUserName);
        Assert.notNull(currentUser, "Unable to get logged-in user.");
        boolean self = StringUtil.equals(currentUser.getUsername(), username);
        boolean canEdit = permissionService.hasPermission(currentUserName, PERM_USER_EDIT);
        Assert.isTrue(canEdit || self, "No permission.");

        UserInfo userInfo = getUserConfig().getUserInfo(username);
        return convertUserInfo2Vo(userInfo);
    }

    @Override
    public List<UserInfoVO> getUserInfoAll(String currentUserName) {
        UserConfig userConfig = getUserConfig();
        UserInfo currentUser = userConfig.getUserInfo(currentUserName);
        Assert.notNull(currentUser, "Unable to get logged-in user.");
        if (permissionService.hasPermission(currentUserName, PERM_USER_VIEW)
                || permissionService.isAdmin(currentUserName)) {
            return getUserConfig().getUserInfoList().stream().map(this::convertUserInfo2Vo).collect(Collectors.toList());
        }

        List<UserInfoVO> list = new ArrayList<>();
        list.add(convertUserInfo2Vo(currentUser));
        return list;
    }

    @Override
    public UserConfig getUserConfig() {
        UserConfig config = userProfile.getUserConfig();
        if (null != config) {
            return config;
        }

        synchronized (this) {
            config = userProfile.getUserConfig();
            if (null == config) {
                config = (UserConfig) userConfigChecker.checkAddConfigModel(new HashMap<>());
                config.getUserInfoList().add(getDefaultUser());
                userProfile.saveUserConfig(config);
            }
            return config;
        }
    }

    @Override
    public UserInfo getDefaultUser() {
        return new UserInfo(DEFAULT_USERNAME, DEFAULT_USERNAME, DEFAULT_PASSWORD, StringUtil.EMPTY);
    }

    private UserInfoVO convertUserInfo2Vo(UserInfo userInfo) {
        UserInfoVO userInfoVo = new UserInfoVO();
        if (null != userInfo) {
            BeanUtils.copyProperties(userInfo, userInfoVo);
            userInfoVo.setPassword("***");
            String roleId = permissionService.resolvePrimaryRoleId(userInfo.getId());
            String roleName = permissionService.resolvePrimaryRoleName(userInfo.getId());
            if (StringUtil.isNotBlank(roleId)) {
                userInfoVo.setRoleId(roleId);
            }
            userInfoVo.setRoleName(StringUtil.isNotBlank(roleName) ? roleName : StringUtil.EMPTY);
            userInfoVo.setAdmin(permissionService.isAdmin(userInfo.getUsername()));
        }
        return userInfoVo;
    }

    private String resolveAssignableRoleId(String roleId, String defaultId) {
        String id = StringUtil.isBlank(roleId) ? defaultId : roleId.trim();
        Assert.hasText(id, "Please select a role");
        Role role = rbacProfile.getRoleById(id);
        Assert.notNull(role, "Role does not exist");
        return role.getId();
    }

}
