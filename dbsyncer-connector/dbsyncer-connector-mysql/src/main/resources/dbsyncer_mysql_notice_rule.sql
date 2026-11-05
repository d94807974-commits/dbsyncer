CREATE TABLE `dbsyncer_notice_rule` (
  `ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Unique ID',
  `CREATE_TIME` bigint NOT NULL COMMENT 'Created at',
  `UPDATE_TIME` bigint NOT NULL COMMENT 'Updated at',
  `SCOPE_TYPE` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Scope: group/mapping',
  `SCOPE_ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Group ID or Sync Task ID',
  `AT_ALL` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Whether or not',
  `AT_PHONES` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '@ Cell phone number, comma separated',
  `EMAILS` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'Mail, commas separated.',
  PRIMARY KEY (`ID`) USING BTREE,
  UNIQUE KEY `UK_NOTICE_RULE` (`SCOPE_TYPE`, `SCOPE_ID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='Rule sheet for alerts' ROW_FORMAT=Dynamic;
