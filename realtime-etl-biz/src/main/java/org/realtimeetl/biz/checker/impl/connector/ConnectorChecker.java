/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.biz.checker.impl.connector;

import org.realtimeetl.biz.BizException;
import org.realtimeetl.biz.checker.AbstractChecker;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.base.ConnectorFactory;
import org.realtimeetl.parser.ConnectorProfile;
import org.realtimeetl.parser.model.Connector;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.model.ConnectorConfig;
import org.realtimeetl.sdk.spi.ConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2020-01-08 15:17
 */
@Component
public class ConnectorChecker extends AbstractChecker {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ConnectorProfile connectorProfile;

    @Resource
    private ConnectorFactory connectorFactory;

    @Override
    public Connector checkAddConfigModel(Map<String, String> params) {
        printParams(params);
        String name = params.get(ConfigConstant.CONFIG_MODEL_NAME);
        String connectorType = params.get("connectorType");
        Assert.hasText(name, "connector name is empty.");
        Assert.hasText(connectorType, "connector connectorType is empty.");

        Connector connector = new Connector();
        connector.setName(name);
        ConnectorConfig config = getConfig(connectorType);
        connector.setConfig(config);
        // UpdateBasicConfigure
        this.modifyConfigModel(connector, params);
        applySourceTargetFlags(connector, params);
        // VerifyUpdateConfigure
        validateAndModifyConfig(config, params);
        // ConnectAnd get the data.LibraryList
        connectAndLoadDatabases(connector, config);

        return connector;
    }

    @Override
    public Connector checkEditConfigModel(Map<String, String> params) {
        printParams(params);
        Assert.notEmpty(params, "ConnectorChecker check params is null.");
        String id = params.get(ConfigConstant.CONFIG_MODEL_ID);
        Connector connector = connectorProfile.getConnector(id);
        Assert.notNull(connector, "Can not find connector.");
        ConnectorConfig config = connector.getConfig();
        // UpdateBasicConfigure
        this.modifyConfigModel(connector, params);
        applySourceTargetFlags(connector, params);
        // VerifyUpdateConfigure
        validateAndModifyConfig(config, params);
        // Getting dataLibraryList
        connectAndLoadDatabases(connector, config);

        return connector;
    }

    /**
     * Parsing and Verifying「As Source / AsObjective Combined」Switches (at least)OnOne.
     */
    private void applySourceTargetFlags(Connector connector, Map<String, String> params) {
        boolean isSource = StringUtil.isNotBlank(params.get(ConfigConstant.CONNECTOR_IS_SOURCE));
        boolean isTarget = StringUtil.isNotBlank(params.get(ConfigConstant.CONNECTOR_IS_TARGET));
        Assert.isTrue(isSource || isTarget, "At least open as a source or as a target.");
        connector.setSource(isSource);
        connector.setTarget(isTarget);
    }

    /**
     * VerifyUpdateConfigure
     */
    private void validateAndModifyConfig(ConnectorConfig config, Map<String, String> params) {
        ConnectorService connectorService = connectorFactory.getConnectorService(config.getConnectorType());
        ConfigValidator configValidator = connectorService.getConfigValidator();
        Assert.notNull(configValidator, "ConfigValidator can not be null.");
        configValidator.modify(connectorService, config, params);
    }

    /**
     * Connectand load dataLibraryList
     */
    private void connectAndLoadDatabases(Connector connector, ConnectorConfig config) {
        ConnectorInstance connectorInstance = connectorFactory.connect(connector.getId(), config, StringUtil.EMPTY, StringUtil.EMPTY);
        ConnectorService connectorService = connectorFactory.getConnectorService(config.getConnectorType());
        connector.setDatabases(connectorService.getDatabases(connectorInstance));
    }

    private ConnectorConfig getConfig(String connectorType) {
        try {
            ConnectorService connectorService = connectorFactory.getConnectorService(connectorType);
            Class<ConnectorConfig> configClass = connectorService.getConfigClass();
            ConnectorConfig config = configClass.newInstance();
            config.setConnectorType(connectorService.getConnectorType());
            return config;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new BizException("Get connector configuration abnormal.");
        }
    }
}
