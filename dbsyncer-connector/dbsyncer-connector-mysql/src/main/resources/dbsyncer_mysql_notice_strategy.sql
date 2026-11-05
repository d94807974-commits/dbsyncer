CREATE TABLE `dbsyncer_notice_strategy` (
  `ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Unique ID',
  `CREATE_TIME` bigint NOT NULL COMMENT 'Created at',
  `UPDATE_TIME` bigint NOT NULL COMMENT 'Updated at',
  `NAME` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Policy name',
  `NOTICE_TYPES` int NOT NULL COMMENT 'Notification type code, deposit',
  `ENABLED` tinyint(1) NOT NULL DEFAULT 1 COMMENT 'Enable: 0 or 1 yes',
  `FREQUENCY` tinyint NOT NULL COMMENT 'Frequency: 0-IMMEDIATE; 1-INTERVAL',
  `CRON` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'Cycle Cron. Immediate strategy empty.',
  `REMARK` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'Remarks',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='Warning strategy forms' ROW_FORMAT=Dynamic;
