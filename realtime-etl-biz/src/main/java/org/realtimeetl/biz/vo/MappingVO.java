package org.realtimeetl.biz.vo;

import org.realtimeetl.parser.model.Connector;
import org.realtimeetl.parser.model.Mapping;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2020/01/03 17:20
 */
public class MappingVO extends Mapping {

    // Connector
    private final Connector sourceConnector;
    private final Connector targetConnector;

    // MetaInfo
    private final MetaVO meta;

    public MappingVO(Connector sourceConnector, Connector targetConnector, MetaVO meta) {
        this.sourceConnector = sourceConnector;
        this.targetConnector = targetConnector;
        this.meta = meta;
    }

    public Connector getSourceConnector() {
        return sourceConnector;
    }

    public Connector getTargetConnector() {
        return targetConnector;
    }

    public MetaVO getMeta() {
        return meta;
    }
}
