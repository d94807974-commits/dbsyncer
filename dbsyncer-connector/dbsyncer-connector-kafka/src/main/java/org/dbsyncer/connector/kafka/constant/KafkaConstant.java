/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.kafka.constant;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-06-06 22:30
 */
public final class KafkaConstant {

    public static final String TOPIC = "topic";

    /**
     * MessageBody：Source Table Name
     */
    public static final String FIELD_TABLE = "table";

    /**
     * MessageBody：ActionsType（INSERT/UPDATE/DELETE）
     */
    public static final String FIELD_EVENT = "event";

    /**
     * MessageBody：Row data
     */
    public static final String FIELD_DATA = "data";

    private KafkaConstant() {
    }
}
