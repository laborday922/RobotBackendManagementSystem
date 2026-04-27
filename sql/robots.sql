-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: ry-vue
-- ------------------------------------------------------
-- Server version	8.0.38

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
-- Table structure for table `robot_groups`
--

DROP TABLE IF EXISTS `robot_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `robot_groups` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `name` varchar(50) NOT NULL COMMENT '分组名称',
  `description` varchar(255) DEFAULT '' COMMENT '分组描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`) COMMENT '分组名称唯一'
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人分组表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `robot_groups`
--

LOCK TABLES `robot_groups` WRITE;
/*!40000 ALTER TABLE `robot_groups` DISABLE KEYS */;
INSERT INTO `robot_groups` VALUES (1,'配送组','负责物品配送的机器人组','2026-03-19 10:28:17','2026-03-19 10:28:17'),(2,'清洁组','负责清洁任务的机器人组','2026-03-19 10:28:17','2026-03-19 10:28:17'),(3,'巡检组','负责巡逻巡检的机器人组','2026-03-19 10:28:17','2026-03-19 10:28:17'),(4,'导览组','负责智能导览和导航服务的机器人组','2026-04-02 00:28:54','2026-04-02 00:28:54');
/*!40000 ALTER TABLE `robot_groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `robot_position_history`
--

DROP TABLE IF EXISTS `robot_position_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `robot_position_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `robot_id` bigint unsigned NOT NULL COMMENT '机器人ID',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `location_area` varchar(50) NOT NULL COMMENT '位置区域（如：药房、走廊、病房区）',
  `specific_location` varchar(100) NOT NULL COMMENT '具体位置（如：药房存储区、1号楼3层走廊）',
  `coordinate_x` decimal(10,2) NOT NULL COMMENT '坐标X',
  `coordinate_y` decimal(10,2) NOT NULL COMMENT '坐标Y',
  `move_speed` decimal(5,2) NOT NULL COMMENT '移动速度（单位：m/s）',
  `status_desc` varchar(200) NOT NULL COMMENT '状态描述（含主状态和子状态）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人位置历史信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `robot_position_history`
--

LOCK TABLES `robot_position_history` WRITE;
/*!40000 ALTER TABLE `robot_position_history` DISABLE KEYS */;
INSERT INTO `robot_position_history` VALUES (1,1,'2026-03-19 00:24:26','急诊科','急诊药房',10.50,20.30,1.20,'待命 准备执行配送任务');
/*!40000 ALTER TABLE `robot_position_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `robot_task`
--

DROP TABLE IF EXISTS `robot_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `robot_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID（主键，自增）',
  `battery_threshold` int DEFAULT NULL COMMENT '电量阈值（低于该值任务暂停/终止，单位：%）',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '定时任务Cron表达式（如每分钟执行：0 * * * * ?）',
  `duration` int DEFAULT NULL COMMENT '任务预计执行时长（单位：分钟）',
  `idle_time` int DEFAULT NULL COMMENT '任务闲置时长（单位：分钟，任务未执行的空闲时间）',
  `is_group_task` int DEFAULT NULL COMMENT '是否为分组任务（0-否，1-是）',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务名称（唯一）',
  `pause_snapshot` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '暂停快照（任务暂停时的状态快照JSON）',
  `pending_order` int DEFAULT NULL COMMENT '待执行队列排序号（越小优先级越高）',
  `priority` int DEFAULT NULL COMMENT '任务优先级（1-最高，5-最低）',
  `risk_level` int DEFAULT NULL COMMENT '任务风险等级（0-无风险，1-低风险，2-中风险，3-高风险）',
  `robot_group_id` bigint DEFAULT NULL COMMENT '所属机器人分组ID（关联robot_groups表id）',
  `robot_id` bigint DEFAULT NULL COMMENT '绑定机器人ID（关联robots表id，分组任务时为NULL）',
  `scheduled_time` datetime DEFAULT NULL COMMENT '任务计划执行时间（单次任务生效）',
  `status` tinyint DEFAULT NULL COMMENT '任务状态（0-未开始，1-执行中，2-暂停，3-已完成，4-已终止，5-待执行）',
  `task_type` int DEFAULT NULL COMMENT '任务类型（0-单次任务，1-定时任务，2-循环任务）',
  `template_id` bigint DEFAULT NULL COMMENT '任务模板ID（关联task_template表id）',
  `terminate_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务终止原因（如电量不足、手动终止、异常报错）',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人（用户名/工号）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务备注说明',
  `search_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '搜索关键字（冗余字段，用于快速检索）',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人（用户名/工号）',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务详细描述',
  PRIMARY KEY (`id`),
  UNIQUE KEY `task_name_index` (`name`) COMMENT '任务名称唯一索引'
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='机器人任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `robot_task`
--

LOCK TABLES `robot_task` WRITE;
/*!40000 ALTER TABLE `robot_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `robot_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `robot_warnings`
--

DROP TABLE IF EXISTS `robot_warnings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `robot_warnings` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '预警ID',
  `robot_id` bigint unsigned NOT NULL COMMENT '关联机器人ID',
  `warning_type` tinyint unsigned NOT NULL COMMENT '预警类型（0-低电量，1-硬件故障，2-硬件异常，3-离线）',
  `warning_content` varchar(255) NOT NULL COMMENT '预警描述内容',
  `warning_level` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '预警级别（0-提示，1-警告，2-错误）',
  `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '预警状态（0-待处理，1-已解决）',
  `resolve_time` datetime DEFAULT NULL COMMENT '处理完成时间',
  `resolve_user` varchar(50) DEFAULT '' COMMENT '处理人',
  `resolve_note` varchar(255) DEFAULT '' COMMENT '处理备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预警创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人状态预警表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `robot_warnings`
--

LOCK TABLES `robot_warnings` WRITE;
/*!40000 ALTER TABLE `robot_warnings` DISABLE KEYS */;
INSERT INTO `robot_warnings` VALUES (1,2,0,'机器人小旋2号电量低于15%，当前12%',2,0,NULL,'','','2025-03-19 09:15:00'),(2,3,1,'清洁电机异常，需要维护',1,1,'2025-03-19 10:30:00','admin','已重启清洁电机','2025-03-19 08:20:00'),(3,3,2,'传感器读数漂移',1,0,NULL,'','','2025-03-19 11:00:00'),(4,4,2,'离线超过30分钟',2,0,NULL,'','','2025-03-19 07:00:00'),(5,4,1,'底盘电机故障',2,0,NULL,'','','2025-03-19 07:05:00'),(6,7,2,'摄像头故障，无法获取图像',2,0,NULL,'','','2025-03-19 12:00:00'),(7,7,0,'电量低于50%',1,1,'2025-03-19 13:00:00','system','已自动充电','2025-03-19 12:30:00');
/*!40000 ALTER TABLE `robot_warnings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `robots`
--

DROP TABLE IF EXISTS `robots`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `robots` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '机器人ID',
  `code` varchar(30) NOT NULL COMMENT '机器人编号（唯一标识）',
  `name` varchar(50) NOT NULL COMMENT '机器人名称',
  `group_id` int unsigned DEFAULT NULL COMMENT '所属分组ID',
  `manufacturer` varchar(50) DEFAULT '' COMMENT '生产厂家',
  `production_date` date DEFAULT NULL COMMENT '生产日期',
  `area` varchar(50) DEFAULT '' COMMENT '所属区域',
  `status` tinyint unsigned NOT NULL DEFAULT '2' COMMENT '在线状态（0-离线，1-在线，2-待激活）',
  `hardware_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '硬件状态（0-正常，1-警告，2-故障）',
  `task_status` tinyint unsigned NOT NULL DEFAULT '2' COMMENT '任务状态（0-执行中，1-充电中，2-闲置，3-维护）',
  `battery` tinyint unsigned NOT NULL DEFAULT '100' COMMENT '当前电量（0-100）',
  `current_mode` int DEFAULT NULL COMMENT '当前模式ID',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `idle_start_time` datetime DEFAULT NULL COMMENT '空闲开始时间',
  `last_heartbeat_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_current_mode` (`current_mode`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_status` (`status`),
  KEY `idx_task_status` (`task_status`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人基础信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `robots`
--

LOCK TABLES `robots` WRITE;
/*!40000 ALTER TABLE `robots` DISABLE KEYS */;
INSERT INTO `robots` VALUES (1,'ROB001','小旋1号',1,'极智科技','2024-01-10','药房',0,0,2,85,1,'0','admin','2026-03-02 20:37:52','admin','2026-04-07 02:05:00',NULL,NULL),(2,'ROB002','小旋2号',1,'极智科技','2024-01-15','病房区',1,0,2,12,1,'0','admin','2026-03-02 20:37:52','admin','2026-03-31 09:20:56','2026-03-31 09:20:56',NULL),(3,'ROB003','大白1号',2,'清洁科技','2023-12-01','门诊大厅',2,0,2,76,1,'0','admin','2026-03-02 20:37:52','admin','2026-04-07 02:05:00','2026-03-30 16:34:33',NULL),(4,'ROB004','安巡1号',3,'安防科技','2024-02-20','走廊',0,0,3,30,2,'0','admin','2026-03-02 20:37:52','admin','2026-03-02 20:37:52',NULL,NULL),(5,'ROB005','闪电侠',1,'极智科技','2024-03-01','监控室',1,0,2,95,1,'0','admin','2026-03-02 20:37:52','admin','2026-03-31 08:50:44','2026-03-31 08:50:44',NULL),(6,'ROB006','小黄人',1,'清洁科技','2024-01-05','物流分拣中心',1,0,2,60,1,'0','admin','2026-03-30 21:36:44','admin','2026-03-30 21:36:44','2026-03-30 21:36:44',NULL),(7,'ROB007','巡警1号',2,'安防科技','2023-11-11','东区巡逻路线',0,0,1,45,3,'0','admin','2026-03-30 21:36:44','admin','2026-03-30 21:36:44',NULL,NULL),(8,'ROB008','引路人',4,'极智科技','2024-03-15','门诊大厅导诊台',2,0,2,95,1,'0','admin','2026-03-30 21:36:44','admin','2026-03-31 00:37:14','2026-03-31 00:37:14',NULL),(9,'ROB009','解说员',4,'极智科技','2024-03-20','院史馆/文化长廊',1,0,2,88,1,'0','admin','2026-03-30 21:36:44','admin','2026-03-30 21:36:44','2026-03-30 21:36:44',NULL);
/*!40000 ALTER TABLE `robots` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-11  1:37:23
