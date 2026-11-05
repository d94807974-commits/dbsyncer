package org.dbsyncer.parser.model;

import org.dbsyncer.common.model.ConfigModel;
import org.dbsyncer.sdk.constant.ConfigConstant;
import org.dbsyncer.sdk.model.ConnectorConfig;

import java.util.List;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2019/10/1 12:40
 */
public class Connector extends ConfigModel {

    public Connector() {
        super.setType(ConfigConstant.CONNECTOR);
    }

    /**
     * ConnectorConfigure
     */
    private ConnectorConfig config;

    /**
     * Data FactLibraryList
     */
    private List<String> databases;

    /**
     * AsSourceEnd, DefaultOn
     */
    private boolean source = true;

    /**
     * AsObjective CombinedEnd, DefaultOn
     */
    private boolean target = true;

    /**
     * ConnectStatus：1-Online，0-Offline；DefaultOnline
     */
    private int status = 1;

    public ConnectorConfig getConfig() {
        return config;
    }

    public Connector setConfig(ConnectorConfig config) {
        this.config = config;
        return this;
    }

    public List<String> getDatabases() {
        return databases;
    }

    public void setDatabases(List<String> databases) {
        this.databases = databases;
    }

    public boolean isSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    public boolean isTarget() {
        return target;
    }

    public void setTarget(boolean target) {
        this.target = target;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
