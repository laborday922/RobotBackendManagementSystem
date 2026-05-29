-- MySQL dump 10.13  Distrib 9.0.0, for Win64 (x86_64)
--
-- Host: localhost    Database: ry-vue
-- ------------------------------------------------------
-- Server version	9.0.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `sys_mode`
--

DROP TABLE IF EXISTS `sys_mode`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_mode` (
                            `mode_id` int NOT NULL AUTO_INCREMENT COMMENT '模式ID',
                            `mode_name` varchar(100) NOT NULL COMMENT '模式名称',
                            `mode_type` varchar(50) DEFAULT 'custom' COMMENT '模式类型(system系统/custom自定义)',
                            `category_id` int DEFAULT NULL COMMENT '分类ID',
                            `mode_color` varchar(20) DEFAULT '#1890ff' COMMENT '模式颜色',
                            `mode_icon` varchar(100) DEFAULT 'fa fa-robot' COMMENT '模式图标',
                            `description` varchar(500) DEFAULT NULL COMMENT '模式描述',
                            `enabled` char(1) DEFAULT '1' COMMENT '是否启用(0禁用 1启用)',
                            `usage_count` int DEFAULT '0' COMMENT '使用次数',
                            `robot_count` int DEFAULT '0' COMMENT '关联机器人数量',
                            `order_num` int DEFAULT '0' COMMENT '显示顺序',
                            `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
                            `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                            `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `tenant_id` bigint DEFAULT '0' COMMENT '租户ID',
                            PRIMARY KEY (`mode_id`),
                            KEY `idx_sys_mode_tenant_id` (`tenant_id`),
                            KEY `idx_sys_mode_tenant` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=209 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模式主表';

--
-- Dumping data for table `sys_mode`
--

LOCK TABLES `sys_mode` WRITE;
/*!40000 ALTER TABLE `sys_mode` DISABLE KEYS */;
INSERT INTO `sys_mode` VALUES (0,'无','system',NULL,'#999999','fa fa-ban','无模式（默认占位）','1',0,0,0,'0','system','2026-05-06 23:43:16','',NULL,0),(1,'待机模式','system',1,'#95a5a6','fas fa-pause-circle','机器人进入低功耗状态，等待唤醒指令','1',0,4,1,'0','','2026-03-02 20:37:52','','2026-04-20 22:22:47',1),(2,'维护模式','system',1,'#f1c40f','fas fa-tools','进行系统维护和调试，暂停所有任务','1',0,4,2,'0','','2026-03-02 20:37:52','','2026-04-11 21:58:54',1),(3,'充电模式','system',1,'#2ecc71','fas fa-bolt','低电量时自动返回充电桩充电','1',0,8,3,'0','','2026-03-02 20:37:52','','2026-04-10 17:12:36',1),(207,'1','custom',2,'#1890ff','fa fa-cog','1','1',0,0,0,'2','','2026-04-09 23:44:19','',NULL,1),(208,'2','custom',2,'#1890ff','fa fa-cog',NULL,'1',1,0,4,'2','','2026-04-09 23:47:52','',NULL,1);
/*!40000 ALTER TABLE `sys_mode` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_mode_param`
--

DROP TABLE IF EXISTS `sys_mode_param`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_mode_param` (
                                  `param_id` int NOT NULL AUTO_INCREMENT COMMENT '参数ID',
                                  `mode_id` int NOT NULL COMMENT '所属模式ID',
                                  `param_name` varchar(100) NOT NULL COMMENT '参数名称',
                                  `param_type` varchar(20) DEFAULT 'string' COMMENT '参数类型(string文本/number数字/boolean开关/select下拉/range滑块)',
                                  `param_description` varchar(200) DEFAULT NULL COMMENT '参数描述',
                                  `param_value` text COMMENT '参数默认值',
                                  `param_options` text COMMENT '选项配置(JSON格式)',
                                  `param_min` int DEFAULT NULL COMMENT '最小值',
                                  `param_max` int DEFAULT NULL COMMENT '最大值',
                                  `param_unit` varchar(20) DEFAULT NULL COMMENT '单位',
                                  `order_num` int DEFAULT '0' COMMENT '显示顺序',
                                  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
                                  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                  `tenant_id` bigint DEFAULT '0' COMMENT '租户ID',
                                  PRIMARY KEY (`param_id`),
                                  KEY `idx_mode_id` (`mode_id`),
                                  KEY `idx_sys_mode_param_tenant_id` (`tenant_id`),
                                  KEY `idx_sys_mode_param_tenant` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模式参数表';

--
-- Dumping data for table `sys_mode_param`
--

LOCK TABLES `sys_mode_param` WRITE;
/*!40000 ALTER TABLE `sys_mode_param` DISABLE KEYS */;
INSERT INTO `sys_mode_param` VALUES (1,4,'1','string','1','1',NULL,0,100,'',0,'2','','2026-03-24 17:33:21','',NULL,1),(2,2,'维护人员权限','select',NULL,'option1','[{\"label\":\"选项1\",\"value\":\"option1\"},{\"label\":\"选项2\",\"value\":\"option2\"}]',0,100,'',0,'2','','2026-03-24 19:56:03','',NULL,1),(3,2,'维护人员权限','select',NULL,'option1','[{\"label\":\"选项1\",\"value\":\"option1\"},{\"label\":\"选项2\",\"value\":\"option2\"}]',0,100,'',0,'2','','2026-03-24 19:56:42','',NULL,1),(4,2,'维护时间','number',NULL,'0',NULL,0,100,'分钟',0,'2','','2026-03-24 19:56:42','',NULL,1),(5,3,'唤醒触发条件','select',NULL,'option1','[{\"label\":\"选项1\",\"value\":\"手动唤醒\"},{\"label\":\"选项2\",\"value\":\"自动唤醒\"}]',0,100,'',0,'2','','2026-03-26 09:29:57','',NULL,1),(6,3,'待机损耗','range',NULL,'10',NULL,0,100,'',0,'2','','2026-03-26 09:29:57','',NULL,1),(7,3,'网络保持','boolean',NULL,'false',NULL,0,100,'',0,'2','','2026-03-26 09:29:57','',NULL,1),(8,3,'低温保护','boolean',NULL,'false',NULL,0,100,'',0,'2','','2026-03-26 09:29:57','',NULL,1),(9,3,'唤醒电量阈值','number',NULL,'30',NULL,0,100,'',0,'2','','2026-03-26 09:29:57','',NULL,1),(10,1,'1','string',NULL,'',NULL,0,100,'',0,'2','','2026-03-26 09:30:12','',NULL,1),(11,1,'唤醒触发条件','select',NULL,'option1','[{\"label\":\"选项1\",\"value\":\"自动唤醒\"},{\"label\":\"选项2\",\"value\":\"手动唤醒\"}]',0,100,'',0,'2','','2026-03-26 09:30:51','',NULL,1),(12,1,'唤醒触发条件','select',NULL,'option1','[{\"label\":\"自动唤醒\",\"value\":\"自动唤醒\"},{\"label\":\"手动唤醒\",\"value\":\"手动唤醒\"}]',0,100,'',0,'2','','2026-03-26 09:31:41','',NULL,1),(13,1,'唤醒触发条件','select',NULL,'自动唤醒','[{\"label\":\"自动唤醒\",\"value\":\"自动唤醒\"},{\"label\":\"手动唤醒\",\"value\":\"手动唤醒\"}]',0,100,'',0,'2','','2026-03-26 09:32:00','',NULL,1),(14,1,'唤醒触发条件','select',NULL,'自动唤醒','[{\"label\":\"自动唤醒\",\"value\":\"自动唤醒\"},{\"label\":\"手动唤醒\",\"value\":\"手动唤醒\"}]',0,100,'',0,'2','','2026-03-31 08:43:54','',NULL,1),(15,1,'唤醒电量阈值','range',NULL,'20',NULL,0,100,'%',0,'2','','2026-03-31 08:43:54','',NULL,1),(16,1,'低温保护','boolean',NULL,'false',NULL,0,100,'',0,'2','','2026-03-31 08:43:54','',NULL,1),(17,1,'网络保持','boolean',NULL,'false',NULL,0,100,'',0,'2','','2026-03-31 08:43:54','',NULL,1),(18,1,'唤醒触发条件','select',NULL,'','[{\"label\":\"定时唤醒\",\"value\":\"\"},{\"label\":\"手动唤醒\",\"value\":\"\"}]',0,100,'',0,'2','','2026-03-31 08:44:46','',NULL,1),(19,1,'唤醒电量阈值','range',NULL,'20','[]',0,100,'%',0,'2','','2026-03-31 08:44:46','',NULL,1),(20,1,'低温保护','boolean',NULL,'false','[]',0,100,'',0,'2','','2026-03-31 08:44:46','',NULL,1),(21,1,'网络保持','boolean',NULL,'false','[]',0,100,'',0,'2','','2026-03-31 08:44:46','',NULL,1),(22,1,'唤醒触发条件','select',NULL,'option1','[{\"label\":\"手动唤醒\",\"value\":\"option1\"},{\"label\":\"定时唤醒\",\"value\":\"option2\"},{\"label\":\"远程唤醒\",\"value\":\"option3\"}]',0,100,'',0,'2','','2026-03-31 08:46:10','',NULL,1),(23,1,'唤醒电量阈值','range',NULL,'20','[]',0,100,'%',0,'2','','2026-03-31 08:46:10','',NULL,1),(24,1,'低温保护','boolean',NULL,'false','[]',0,100,'',0,'2','','2026-03-31 08:46:10','',NULL,1),(25,1,'网络保持','boolean',NULL,'false','[]',0,100,'',0,'2','','2026-03-31 08:46:10','',NULL,1),(26,2,'维护人员权限','select',NULL,'option1','[{\"label\":\"基础权限\",\"value\":\"option1\"},{\"label\":\"高级权限\",\"value\":\"option2\"},{\"label\":\"管理员权限\",\"value\":\"option3\"}]',0,100,'',0,'2','','2026-03-31 08:47:07','',NULL,1),(27,2,'维护时间','number',NULL,'0',NULL,0,100,'分钟',0,'2','','2026-03-31 08:47:07','',NULL,1),(28,2,'警告提醒','boolean',NULL,'true',NULL,0,100,'',0,'2','','2026-03-31 08:47:07','',NULL,1),(29,3,'低电量阈值','range',NULL,'20',NULL,0,100,'%',0,'2','','2026-03-31 08:47:40','',NULL,1),(30,3,'低电量阈值','range',NULL,'20','[]',0,100,'%',0,'2','','2026-03-31 08:49:10','',NULL,1),(31,3,'充电优先级','select',NULL,'option1','[{\"label\":\"完成任务后充电\",\"value\":\"option1\"},{\"label\":\"立即充电\",\"value\":\"option2\"},{\"label\":\"定时充电\",\"value\":\"option3\"}]',0,100,'',0,'2','','2026-03-31 08:49:10','',NULL,1),(32,3,'充电完成提醒','boolean',NULL,'true',NULL,0,100,'',0,'2','','2026-03-31 08:49:10','',NULL,1),(33,1,'唤醒触发条件','select',NULL,'option1','[{\"label\":\"手动唤醒\",\"value\":\"option1\"},{\"label\":\"定时唤醒\",\"value\":\"option2\"},{\"label\":\"远程唤醒\",\"value\":\"option3\"}]',0,100,'',0,'2','','2026-03-31 09:21:43','',NULL,1),(34,1,'唤醒电量阈值','range',NULL,'20','[]',0,100,'%',0,'2','','2026-03-31 09:21:43','',NULL,1),(35,1,'低温保护','boolean',NULL,'false','[]',0,100,'',0,'2','','2026-03-31 09:21:43','',NULL,1),(36,1,'网络保持','boolean',NULL,'false','[]',0,100,'',0,'2','','2026-03-31 09:21:43','',NULL,1),(37,1,'1','string',NULL,'',NULL,0,100,'',0,'2','','2026-03-31 09:21:43','',NULL,1),(38,1,'唤醒触发条件','select',NULL,'option1','[{\"label\":\"手动唤醒\",\"value\":\"option1\"},{\"label\":\"定时唤醒\",\"value\":\"option2\"},{\"label\":\"远程唤醒\",\"value\":\"option3\"}]',0,100,'',0,'2','','2026-04-08 10:36:21','',NULL,1),(39,1,'唤醒电量阈值','range',NULL,'20','[]',0,100,'%',0,'2','','2026-04-08 10:36:21','',NULL,1),(40,1,'低温保护','boolean',NULL,'false','[]',0,100,'',0,'2','','2026-04-08 10:36:21','',NULL,1),(41,1,'网络保持','boolean',NULL,'false','[]',0,100,'',0,'2','','2026-04-08 10:36:21','',NULL,1),(42,84,'测试参数','string',NULL,'默认值',NULL,NULL,NULL,NULL,0,'0','','2026-04-08 17:28:35','',NULL,0),(43,95,'测试参数','string',NULL,'默认值',NULL,NULL,NULL,NULL,0,'0','','2026-04-08 22:13:01','',NULL,0),(44,106,'测试参数','string',NULL,'默认值',NULL,NULL,NULL,NULL,0,'0','','2026-04-08 23:43:55','',NULL,0),(45,117,'测试参数','string',NULL,'默认值',NULL,NULL,NULL,NULL,0,'0','','2026-04-08 23:46:20','',NULL,0),(46,128,'测试参数','string',NULL,'默认值',NULL,NULL,NULL,NULL,0,'0','','2026-04-08 23:47:59','',NULL,0),(47,139,'测试参数','string',NULL,'默认值',NULL,NULL,NULL,NULL,0,'0','','2026-04-08 23:49:06','',NULL,0),(48,151,'测试参数','string',NULL,'默认值',NULL,NULL,NULL,NULL,0,'0','','2026-04-08 23:49:39','',NULL,0),(49,161,'测试参数','string',NULL,'默认值',NULL,NULL,NULL,NULL,0,'0','','2026-04-08 23:49:43','',NULL,0),(50,172,'测试参数','string',NULL,'默认值',NULL,NULL,NULL,NULL,0,'0','','2026-04-08 23:51:01','',NULL,0),(51,183,'测试参数','string',NULL,'默认值',NULL,NULL,NULL,NULL,0,'0','','2026-04-08 23:52:27','',NULL,0),(52,194,'测试参数','string',NULL,'默认值',NULL,NULL,NULL,NULL,0,'0','','2026-04-08 23:54:06','',NULL,0),(53,200,'测试参数','string',NULL,'默认值',NULL,NULL,NULL,NULL,0,'0','','2026-04-08 23:56:39','',NULL,0),(54,207,'1','string','1','1',NULL,0,100,'',0,'2','','2026-04-09 23:44:19','',NULL,0),(55,3,'低电量阈值','range',NULL,'20','[]',0,100,'%',0,'0','','2026-04-10 17:12:36','',NULL,0),(56,3,'充电完成提醒','boolean',NULL,'true','[]',0,100,'',0,'0','','2026-04-10 17:12:36','',NULL,0),(57,3,'充电策略','select','选择机器人充电方式：立即充电或完成任务后充电','after_task','[{\"label\":\"立即充电\",\"value\":\"immediate\"},{\"label\":\"完成任务后充电\",\"value\":\"after_task\"}]',NULL,NULL,NULL,0,'0','','2026-04-10 17:12:36','',NULL,0),(58,2,'维护人员权限','select',NULL,'option1','[{\"label\":\"基础权限\",\"value\":\"option1\"},{\"label\":\"高级权限\",\"value\":\"option2\"},{\"label\":\"管理员权限\",\"value\":\"option3\"}]',0,100,'',0,'0','','2026-04-11 21:58:54','',NULL,1),(59,2,'维护时间','number',NULL,'0','[]',0,100,'分钟',0,'0','','2026-04-11 21:58:54','',NULL,1),(60,2,'警告提醒','boolean',NULL,'true','[]',0,100,'',0,'0','','2026-04-11 21:58:54','',NULL,1),(61,1,'唤醒触发条件','select',NULL,'option1','[{\"label\":\"手动唤醒\",\"value\":\"option1\"},{\"label\":\"定时唤醒\",\"value\":\"option2\"},{\"label\":\"远程唤醒\",\"value\":\"option3\"}]',0,100,'',0,'0','','2026-04-20 22:22:47','',NULL,1),(62,1,'唤醒电量阈值','range',NULL,'20','[]',0,100,'%',0,'0','','2026-04-20 22:22:47','',NULL,1),(63,1,'低温保护','boolean',NULL,'false','[]',0,100,'',0,'0','','2026-04-20 22:22:47','',NULL,1),(64,1,'网络保持','boolean',NULL,'false',NULL,0,100,'',0,'0','','2026-04-20 22:22:47','',NULL,1);
/*!40000 ALTER TABLE `sys_mode_param` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_mode_history`
--

DROP TABLE IF EXISTS `sys_mode_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_mode_history` (
                                    `history_id` int NOT NULL AUTO_INCREMENT COMMENT '历史ID',
                                    `operation_time` datetime DEFAULT NULL COMMENT '操作时间',
                                    `operation_type` varchar(50) DEFAULT NULL COMMENT '操作类型(mode-switch/config-change/alert/emergency)',
                                    `robot_id` int DEFAULT NULL COMMENT '机器人ID',
                                    `robot_name` varchar(100) DEFAULT NULL COMMENT '机器人名称',
                                    `mode_id` int DEFAULT NULL COMMENT '模式ID',
                                    `mode_name` varchar(100) DEFAULT NULL COMMENT '模式名称',
                                    `content` varchar(500) DEFAULT NULL COMMENT '操作内容',
                                    `operator` varchar(50) DEFAULT NULL COMMENT '操作人',
                                    `status` varchar(20) DEFAULT 'success' COMMENT '状态(success/warning/danger)',
                                    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                    `tenant_id` bigint DEFAULT '0' COMMENT '租户ID',
                                    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
                                    PRIMARY KEY (`history_id`),
                                    KEY `idx_operation_time` (`operation_time`),
                                    KEY `idx_robot_id` (`robot_id`),
                                    KEY `idx_operation_type` (`operation_type`),
                                    KEY `idx_sys_mode_history_tenant_id` (`tenant_id`),
                                    KEY `idx_sys_mode_history_tenant` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2882 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模式切换历史记录表';

--
-- Dumping data for table `sys_mode_history`
--

LOCK TABLES `sys_mode_history` WRITE;
/*!40000 ALTER TABLE `sys_mode_history` DISABLE KEYS */;
INSERT INTO `sys_mode_history` VALUES (2879,'2026-05-06 23:46:28','mode-switch',5,'闪电侠',NULL,NULL,'切换为待机模式','admin','success','','2026-05-06 23:46:28',1,'0'),(2880,'2026-05-07 00:38:20','maintenance_mode',5,'闪电侠',NULL,NULL,'已将 1 个机器人切换为维护模式','admin','success','','2026-05-07 00:38:20',1,'0'),(2881,'2026-05-07 00:38:33','mode-switch',8,'引路人',NULL,NULL,'已将 1 个机器人切换为充电模式','admin','success','','2026-05-07 00:38:33',1,'0');
/*!40000 ALTER TABLE `sys_mode_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_mode_schedule`
--

DROP TABLE IF EXISTS `sys_mode_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_mode_schedule` (
                                     `schedule_id` int NOT NULL AUTO_INCREMENT COMMENT '排程ID',
                                     `schedule_name` varchar(100) NOT NULL COMMENT '排程名称',
                                     `mode_id` int DEFAULT NULL COMMENT '模式ID',
                                     `mode_name` varchar(100) DEFAULT NULL COMMENT '模式名称',
                                     `execute_time` varchar(100) DEFAULT NULL COMMENT '执行时间描述',
                                     `repeat_type` varchar(20) DEFAULT 'once' COMMENT '重复类型(once/daily/weekly/monthly/weekdays)',
                                     `start_date` date DEFAULT NULL COMMENT '开始日期',
                                     `start_time` time DEFAULT NULL COMMENT '开始时间',
                                     `duration` decimal(5,2) DEFAULT '2.00' COMMENT '持续时间(小时)',
                                     `status` varchar(20) DEFAULT 'pending' COMMENT '状态(running/paused/pending/completed/failed)',
                                     `last_execute_time` datetime DEFAULT NULL COMMENT '上次执行时间',
                                     `last_execute_status` varchar(20) DEFAULT NULL COMMENT '上次执行状态',
                                     `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
                                     `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                     `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                     `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                                     `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                     `tenant_id` bigint DEFAULT '0' COMMENT '租户ID',
                                     `repeat_rule` varchar(255) DEFAULT NULL COMMENT '重复规则JSON',
                                     PRIMARY KEY (`schedule_id`),
                                     KEY `idx_mode_id` (`mode_id`),
                                     KEY `idx_status` (`status`),
                                     KEY `idx_sys_mode_schedule_tenant_id` (`tenant_id`),
                                     KEY `idx_sys_mode_schedule_tenant` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=220 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模式排程表';

--
-- Dumping data for table `sys_mode_schedule`
--

LOCK TABLES `sys_mode_schedule` WRITE;
/*!40000 ALTER TABLE `sys_mode_schedule` DISABLE KEYS */;
INSERT INTO `sys_mode_schedule` VALUES (1,'夜间充电计划',3,'充电模式','2026-03-01 22:00:00','daily','2026-03-01','22:00:00',2.00,'running','2026-04-13 12:35:00','success','2','','2026-03-02 20:37:53','','2026-04-13 12:35:00',1,NULL),(2,'午间待机计划',1,'待机模式','2024-01-01 12:00:00','weekdays','2024-01-01','12:00:00',2.00,'running','2026-04-07 22:53:00','success','2','','2026-03-02 20:37:53','','2026-04-07 22:53:00',1,NULL),(3,'每周维护计划',2,'维护模式','2024-01-01 08:00:00','weekly','2024-01-01','08:00:00',1.00,'running','2026-04-13 16:58:00','success','2','','2026-03-02 20:37:53','','2026-04-13 16:58:00',1,NULL),(211,'工作日晚间计划',1,'待机模式','2026-03-01 09:00:00','weekdays','2026-03-01','09:00:00',3.00,'running','2026-04-13 17:07:00','success','2','','2026-04-13 11:50:22','','2026-04-13 17:07:00',1,NULL),(212,'夜间充电模式',3,'充电模式','2026-03-01 21:00:00','daily','2026-03-01','21:00:00',2.00,'running','2026-04-13 17:07:00','success','2','','2026-04-13 12:35:07','','2026-04-13 17:07:00',1,NULL),(213,'夜间充电',3,'充电模式','2026-03-01 08:00:00','daily','2026-03-01','08:00:00',2.00,'running','2026-04-13 16:58:00','success','2','','2026-04-13 13:35:53','','2026-04-13 16:58:00',1,NULL),(214,'定期维护',2,'维护模式','2026-03-01 15:00','monthly','2026-03-01','15:00:00',2.00,'running',NULL,NULL,'0','','2026-04-13 13:36:30','','2026-05-11 21:25:45',1,'{\"type\":\"monthly\",\"days\":[1]}'),(215,'1',1,'待机模式','2026-04-14 08:00','daily','2026-04-14','08:00:00',2.00,'running',NULL,NULL,'2','','2026-04-13 16:53:09','',NULL,1,NULL),(216,'夜间充电',3,'充电模式','2026-03-02 21:00:00','weekdays','2026-03-02','21:00:00',2.00,'running','2026-05-05 21:33:01','success','0','','2026-04-13 17:06:36','','2026-05-05 21:33:00',1,NULL),(217,'工作日晚间',1,'待机模式','2026-03-01 09:00:00','daily','2026-03-01','09:00:00',2.00,'running','2026-04-13 17:09:00','success','2','','2026-04-13 17:06:56','','2026-04-13 17:09:00',1,NULL),(218,'工作日晚间',1,'待机模式','2026-03-02 09:00:00','weekdays','2026-03-02','09:00:00',2.00,'running','2026-05-05 21:33:00','success','0','','2026-04-13 17:08:55','','2026-05-05 21:33:00',1,NULL),(219,'检修',2,'维护模式','2026-05-28 15:00','once','2026-05-28','15:00:00',2.00,'pending',NULL,NULL,'0','','2026-04-13 17:29:11','','2026-04-27 22:43:45',1,NULL);
/*!40000 ALTER TABLE `sys_mode_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_schedule_robot`
--

DROP TABLE IF EXISTS `sys_schedule_robot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_schedule_robot` (
                                      `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                      `schedule_id` int NOT NULL COMMENT '排程ID',
                                      `robot_id` int NOT NULL COMMENT '机器人ID',
                                      `robot_name` varchar(100) DEFAULT NULL COMMENT '机器人名称',
                                      `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_schedule_id` (`schedule_id`),
                                      KEY `idx_robot_id` (`robot_id`),
                                      KEY `idx_sys_schedule_robot_tenant` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=343 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='排程机器人关联表';

--
-- Dumping data for table `sys_schedule_robot`
--

LOCK TABLES `sys_schedule_robot` WRITE;
/*!40000 ALTER TABLE `sys_schedule_robot` DISABLE KEYS */;
INSERT INTO `sys_schedule_robot` VALUES (333,216,1,'小旋1号',1),(334,216,2,'小旋2号',1),(335,218,7,'巡警1号',1),(341,219,5,'闪电侠',1),(342,214,9,'讲解员',0);
/*!40000 ALTER TABLE `sys_schedule_robot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_robot_ext`
--

DROP TABLE IF EXISTS `sys_robot_ext`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_robot_ext` (
                                 `robot_id` bigint NOT NULL COMMENT '机器人ID',
                                 `current_mode` bigint DEFAULT NULL COMMENT '当前模式ID',
                                 `current_mode_name` varchar(100) DEFAULT NULL COMMENT '当前模式名称',
                                 `last_mode_switch_time` datetime DEFAULT NULL COMMENT '最后模式切换时间',
                                 `mode_switch_count` int DEFAULT '0' COMMENT '模式切换次数',
                                 `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
                                 `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
                                 `need_auto_charge` tinyint DEFAULT '0' COMMENT '是否需要自动充电(0-否 1-是)',
                                 PRIMARY KEY (`robot_id`),
                                 KEY `idx_tenant_id` (`tenant_id`),
                                 KEY `idx_current_mode` (`current_mode`),
                                 KEY `idx_sys_robot_ext_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人模式扩展表';

--
-- Dumping data for table `sys_robot_ext`
--

LOCK TABLES `sys_robot_ext` WRITE;
/*!40000 ALTER TABLE `sys_robot_ext` DISABLE KEYS */;
INSERT INTO `sys_robot_ext` VALUES (1,3,NULL,'2026-05-05 21:33:00',869,1,'','2026-04-08 10:36:02','','2026-05-05 21:33:00','0',0),(2,3,NULL,'2026-05-05 21:33:00',872,1,'','2026-04-09 16:00:58','','2026-05-05 21:33:00','0',1),(3,3,NULL,'2026-04-20 20:44:03',19,1,'','2026-04-09 20:26:50','','2026-04-20 20:44:03','0',0),(4,2,NULL,'2026-04-13 13:38:02',17,1,'','2026-04-09 20:26:54','','2026-04-13 13:38:02','0',0),(5,2,NULL,'2026-05-07 00:38:19',22,1,'','2026-04-09 20:26:57','','2026-05-07 00:38:19','0',0),(6,3,NULL,'2026-04-20 20:44:03',20,1,'','2026-04-09 20:27:00','','2026-04-20 20:44:03','0',0),(7,1,NULL,'2026-05-05 21:34:00',1119,1,'','2026-04-09 20:27:04','','2026-05-05 21:34:00','0',1),(8,3,NULL,'2026-05-07 00:38:33',19,1,'','2026-04-09 20:27:07','','2026-05-07 00:38:33','0',0),(9,3,NULL,'2026-04-20 20:44:03',18,1,'','2026-04-09 20:27:10','','2026-04-20 20:44:03','0',1),(100,1,NULL,NULL,0,1,'','2026-04-08 17:28:35','','2026-04-12 11:32:22','0',0),(989,3,NULL,'2026-04-09 00:08:46',1,1,'','2026-04-09 00:08:46','','2026-04-12 11:32:22','0',0),(990,3,NULL,'2026-04-09 00:08:46',1,1,'','2026-04-09 00:08:46','','2026-04-12 11:32:22','0',0),(991,2,NULL,'2026-04-09 00:08:46',1,1,'','2026-04-09 00:08:46','','2026-04-12 11:32:22','0',0),(992,1,NULL,'2026-04-09 00:08:45',1,1,'','2026-04-09 00:08:45','','2026-04-12 11:32:22','0',0),(993,NULL,NULL,NULL,0,1,'','2026-04-09 00:08:45','','2026-04-12 11:32:22','0',0),(994,NULL,NULL,NULL,0,1,'','2026-04-09 00:08:46','','2026-04-12 11:32:22','0',0),(995,NULL,NULL,NULL,0,1,'','2026-04-09 00:08:46','','2026-04-12 11:32:22','0',0),(996,NULL,NULL,NULL,0,1,'','2026-04-09 00:08:46','','2026-04-12 11:32:22','0',0),(997,2,NULL,'2026-04-09 00:08:46',1,1,'','2026-04-09 00:08:46','','2026-04-12 11:32:22','0',0),(998,NULL,NULL,NULL,0,1,'','2026-04-09 00:08:45','','2026-04-12 11:32:22','0',0),(999,1,NULL,NULL,0,1,'','2026-04-09 00:08:45','','2026-04-12 11:32:22','0',0);
/*!40000 ALTER TABLE `sys_robot_ext` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_robot_mode_config`
--

DROP TABLE IF EXISTS `sys_robot_mode_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_robot_mode_config` (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                         `robot_id` bigint NOT NULL COMMENT '机器人ID',
                                         `mode_id` bigint NOT NULL COMMENT '模式ID',
                                         `config` json DEFAULT NULL COMMENT '配置参数(JSON格式)',
                                         `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
                                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `uk_robot_mode` (`robot_id`,`mode_id`),
                                         KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人模式配置表';

--
-- Dumping data for table `sys_robot_mode_config`
--

LOCK TABLES `sys_robot_mode_config` WRITE;
/*!40000 ALTER TABLE `sys_robot_mode_config` DISABLE KEYS */;
INSERT INTO `sys_robot_mode_config` VALUES (1,2,3,'{\"低电量阈值\": \"20\", \"充电优先级\": \"option1\", \"充电完成提醒\": \"true\"}',0,'2026-04-10 17:12:04','2026-04-10 17:12:04'),(2,1,2,'{\"维护时间\": \"0\", \"警告提醒\": \"true\", \"维护人员权限\": \"option1\"}',0,'2026-04-10 17:33:51','2026-04-10 17:33:51'),(3,2,2,'{\"维护时间\": \"0\", \"警告提醒\": \"true\", \"维护人员权限\": \"option1\"}',0,'2026-04-10 17:33:51','2026-04-10 17:33:51'),(4,3,2,'{\"维护时间\": \"0\", \"警告提醒\": \"true\", \"维护人员权限\": \"option1\"}',0,'2026-04-10 17:33:51','2026-04-10 17:33:51'),(5,4,2,'{\"维护时间\": \"0\", \"警告提醒\": \"true\", \"维护人员权限\": \"option1\"}',0,'2026-04-10 17:33:51','2026-04-10 17:33:51'),(6,5,2,'{\"维护时间\": \"0\", \"警告提醒\": \"true\", \"维护人员权限\": \"option1\"}',0,'2026-04-10 17:33:52','2026-04-10 17:33:52'),(7,6,2,'{\"维护时间\": \"0\", \"警告提醒\": \"true\", \"维护人员权限\": \"option1\"}',0,'2026-04-10 17:33:52','2026-04-10 17:33:52'),(8,7,2,'{\"维护时间\": \"0\", \"警告提醒\": \"true\", \"维护人员权限\": \"option1\"}',0,'2026-04-10 17:33:52','2026-04-10 17:33:52'),(9,8,2,'{\"维护时间\": \"0\", \"警告提醒\": \"true\", \"维护人员权限\": \"option1\"}',0,'2026-04-10 17:33:52','2026-04-10 17:33:52'),(10,9,2,'{\"维护时间\": \"0\", \"警告提醒\": \"true\", \"维护人员权限\": \"option1\"}',0,'2026-04-10 17:33:52','2026-04-10 17:33:52'),(11,9,3,'{\"充电策略\": \"after_task\", \"低电量阈值\": \"20\", \"充电完成提醒\": \"true\"}',0,'2026-04-10 20:07:36','2026-04-10 20:07:36'),(12,7,3,'{\"充电策略\": \"immediate\", \"低电量阈值\": \"20\", \"充电完成提醒\": \"true\"}',0,'2026-04-10 21:14:19','2026-04-10 21:19:11');
/*!40000 ALTER TABLE `sys_robot_mode_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_robot_operation`
--

DROP TABLE IF EXISTS `sys_robot_operation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_robot_operation` (
                                       `operation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '操作ID',
                                       `robot_id` bigint NOT NULL COMMENT '机器人ID',
                                       `robot_name` varchar(100) DEFAULT NULL COMMENT '机器人名称',
                                       `operation_type` varchar(50) DEFAULT NULL COMMENT '操作类型',
                                       `operation_result` varchar(20) DEFAULT NULL COMMENT '操作结果',
                                       `operation_time` datetime DEFAULT NULL COMMENT '操作时间',
                                       `operator` varchar(50) DEFAULT NULL COMMENT '操作人',
                                       `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                       `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
                                       `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       PRIMARY KEY (`operation_id`),
                                       KEY `idx_robot_id` (`robot_id`),
                                       KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人操作记录表';

--
-- Dumping data for table `sys_robot_operation`
--

LOCK TABLES `sys_robot_operation` WRITE;
/*!40000 ALTER TABLE `sys_robot_operation` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_robot_operation` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-28 21:14:42