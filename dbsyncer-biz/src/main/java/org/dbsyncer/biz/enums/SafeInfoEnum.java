package org.dbsyncer.biz.enums;

import org.dbsyncer.common.util.StringUtil;

/**
 * Clear.InfoEnumeration
 *
 * @author AE86
 * @version 1.0.0
 * @date 2022/8/26 23:21
 */
public enum SafeInfoEnum {

    /**
     * Password
     */
    PASSWORD("password"),
    /**
     * API Key
     */
    API_SECRET("apiSecret"),
    /**
     * Cluster Key
     */
    CLUSTER_SECRET("clusterSecret"),
    /**
     * RSA private key
     */
    RSA_PRIVATE_KEY("rsaPrivateKey"),
    /**
     * RSA Public Key
     */
    RSA_PUBLIC_KEY("rsaPublicKey"),
    /**
     * Corporate Wisdom@UserCell phone number
     */
    WECHAT_AT_MOBILES("wechatAtMobiles"),
    /**
     * Corporate WisdomWebhookAddress
     */
    WECHAT_WebhookUrl("wechatWebhookUrl"),
    /**
     * Nail.@UserCell phone number
     */
    DINGTALK_AT_MOBILES("dingTalkAtMobiles"),
    /**
     * Nail.WebhookAddress
     */
    DINGTALK_WebhookUrl("dingTalkWebhookUrl"),
    /**
     * Old password
     */
    OLD_PWD("oldpwd"),
    /**
     * New Password
     */
    NEW_PWD("newpwd");

    private final String code;

    SafeInfoEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * YesNoSecurity sensitivityField（Ignores case.
     */
    public static boolean contains(String key) {
        if (StringUtil.isBlank(key)) {
            return false;
        }
        for (SafeInfoEnum e : values()) {
            if (StringUtil.equalsIgnoreCase(e.code, key)) {
                return true;
            }
        }
        return false;
    }
}
