CREATE TABLE `realtime_etl_cluster_node` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT 'AutoAddition Primary Key',
  `CREATE_TIME` bigint NOT NULL COMMENT 'Created at',
  `UPDATE_TIME` bigint NOT NULL COMMENT 'Updated at',
  `NODE_ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Node ID, {p}: {httpPort}',
  `NAME` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Node Name',
  `IP` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Laws',
  `HTTP_PORT` int NOT NULL COMMENT 'HTTP port',
  `WORKER_ID` int NULL COMMENT 'Cluster unique ID (0~31)',
  `ROLE` tinyint NOT NULL DEFAULT 0 COMMENT 'No Authorized role: 0-Follower, 1-Leader',
  `TERM` bigint NOT NULL DEFAULT 0 COMMENT 'Term (one-to-one, add +1, default 0)',
  `HEARTBEAT_TIME` bigint NOT NULL DEFAULT 0 COMMENT 'Last heartbeat Contributions',
  `START_TIME` bigint NOT NULL DEFAULT 0 COMMENT 'Node Start Time',
  PRIMARY KEY (`ID`) USING BTREE,
  UNIQUE KEY `UK_NODE_ID` (`NODE_ID`) USING BTREE,
  UNIQUE KEY `UK_WORKER_ID` (`WORKER_ID`) USING BTREE,
  KEY `IDX_CLUSTER_HEARTBEAT` (`HEARTBEAT_TIME`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'Cluster nodes' ROW_FORMAT = Dynamic;
