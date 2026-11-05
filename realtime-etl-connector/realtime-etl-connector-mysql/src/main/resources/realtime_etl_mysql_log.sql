CREATE TABLE `realtime_etl_log` (
  `ID`          varchar(64)  CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Unique ID',
  `TYPE`        varchar(24)  CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Log type (see LogType)',
  `USERNAME`    varchar(50)  CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT 'Operator/entry account',
  `IP`          varchar(64)  CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT 'Login/operate client IP',
  `STATUS`      tinyint      NOT NULL DEFAULT 1 COMMENT 'Status: 0 failed 1 successful',
  `CREATE_TIME` bigint       NOT NULL COMMENT 'Creation time (ms)',
  `JSON`        mediumtext   CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Log contents (summary or structured JSON)',
  PRIMARY KEY (`ID`) USING BTREE,
  KEY `IDX_CREATE_TIME` (`CREATE_TIME`) USING BTREE,
  KEY `IDX_TYPE_TIME` (`TYPE`, `CREATE_TIME`) USING BTREE,
  KEY `IDX_USERNAME_TIME` (`USERNAME`, `CREATE_TIME`) USING BTREE,
  KEY `IDX_IP_TIME` (`IP`, `CREATE_TIME`) USING BTREE,
  KEY `IDX_STATUS_TIME` (`STATUS`, `CREATE_TIME`) USING BTREE
) ENGINE=InnoDB
  CHARACTER SET=utf8mb4 COLLATE=utf8mb4_bin
  COMMENT='Log Table'
  ROW_FORMAT=Dynamic;
