CREATE TABLE `realtime_etl_cluster_task` (
  `ID`                bigint NOT NULL AUTO_INCREMENT COMMENT 'AutoAddition Primary Key',
  `CREATE_TIME`       bigint NOT NULL COMMENT 'Created at',
  `UPDATE_TIME`       bigint NOT NULL COMMENT 'Updated at',
  `TASK_ID`           varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Task ID',
  `TASK_TYPE`         varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Task Type',
  `NODE_ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'Schedule nodes assigned',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_TASK` (`TASK_ID`),
  KEY `IDX_SCHEDULER` (`NODE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='Cluster task schedule';
