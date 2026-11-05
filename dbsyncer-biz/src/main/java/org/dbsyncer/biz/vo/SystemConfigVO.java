package org.dbsyncer.biz.vo;

import org.dbsyncer.common.model.RsaVersion;
import org.dbsyncer.common.model.SecretVersion;
import org.dbsyncer.common.util.CollectionUtils;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.parser.model.SystemConfig;

import java.util.List;

public class SystemConfigVO extends SystemConfig {

    /**
     * IP White list.ListChange to Line BreakminSplit strings for frontendtextarea Presentation
     */
    public String getIpWhitelistText() {
        if (getIpWhitelistConfig() == null || getIpWhitelistConfig().getWhitelist() == null) {
            return "";
        }
        List<String> list = getIpWhitelistConfig().getWhitelist();
        return CollectionUtils.isEmpty(list) ? "" : String.join("\n", list);
    }

    /**
     * GetPoliceAPI Key
     */
    public RsaVersion getCurrentRsa() {
        if (getRsaConfig() != null) {
            List<RsaVersion> versions = getRsaConfig().getRsaVersions();
            if (!CollectionUtils.isEmpty(versions)) {
                return versions.get(versions.size() - 1);
            }
        }
        return null;
    }

    /**
     * GetPoliceAPI Key
     */
    public String getCurrentSecret() {
        if (getApiKeyConfig() != null) {
            List<SecretVersion> versions = getApiKeyConfig().getSecrets();
            if (!CollectionUtils.isEmpty(versions)) {
                return versions.get(versions.size() - 1).getSecret();
            }
        }
        return StringUtil.EMPTY;
    }
}