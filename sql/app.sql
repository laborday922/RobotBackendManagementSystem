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
-- Table structure for table `t_app_api`
--

DROP TABLE IF EXISTS `t_app_api`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_app_api` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `api_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `api_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `params_schema` json NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_app_api` (`app_id`,`api_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_app_api`
--

LOCK TABLES `t_app_api` WRITE;
/*!40000 ALTER TABLE `t_app_api` DISABLE KEYS */;
INSERT INTO `t_app_api` (`id`, `app_id`, `api_key`, `api_name`, `description`, `params_schema`, `created_at`) VALUES (1,'APP001','robot.move','机器人移动','控制机器人移动到指定目标位置','[{\"name\": \"target\", \"type\": \"string\", \"required\": true}, {\"name\": \"x\", \"type\": \"number\", \"required\": true}, {\"name\": \"y\", \"type\": \"number\", \"required\": true}]','2026-03-28 18:00:25'),(2,'APP001','robot.navigate','机器人导航','控制机器人导航至指定目标点','{\"speed\": {\"type\": \"number\", \"required\": false, \"description\": \"移动速度\", \"valueSource\": \"INPUT\", \"defaultValue\": 1.0}, \"weight\": {\"type\": \"number\", \"required\": true, \"description\": \"载重\", \"valueSource\": \"INPUT\"}, \"position\": {\"type\": \"integer\", \"required\": true, \"description\": \"目标位置ID\", \"valueSource\": \"DYNAMIC\", \"dynamicConfig\": {\"listKey\": \"id\", \"dataSource\": \"ROBOT_POSITION\", \"displayKey\": \"name\", \"refreshOnOpen\": true}}}','2026-04-21 01:22:43');
/*!40000 ALTER TABLE `t_app_api` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_app_constraint`
--

DROP TABLE IF EXISTS `t_app_constraint`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_app_constraint` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `expression` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `severity` enum('error','warning') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'error',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_app_constraint`
--

LOCK TABLES `t_app_constraint` WRITE;
/*!40000 ALTER TABLE `t_app_constraint` DISABLE KEYS */;
INSERT INTO `t_app_constraint` (`id`, `app_id`, `name`, `expression`, `error_message`, `severity`, `created_at`) VALUES (1,'APP001','载重限制校验','form_data.weight <= app_param.max_load_weight','餐品重量不能超过机器人最大载重','error','2026-03-28 19:53:23');
/*!40000 ALTER TABLE `t_app_constraint` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_app_library`
--

DROP TABLE IF EXISTS `t_app_library`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_app_library` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增ID',
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '应用唯一标识（业务ID）',
  `app_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '应用名称',
  `app_type` tinyint NOT NULL COMMENT '应用类型：0(交互类)、1(控制类)、2(监控类)',
  `enabled` tinyint(1) NOT NULL COMMENT '启用状态：1(启用)、0(禁用)',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '最后更新时间',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '应用描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应用库表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_app_library`
--

LOCK TABLES `t_app_library` WRITE;
/*!40000 ALTER TABLE `t_app_library` DISABLE KEYS */;
INSERT INTO `t_app_library` (`id`, `app_id`, `app_name`, `app_type`, `enabled`, `create_time`, `update_time`, `description`) VALUES (6,'APP001','机器人配送管理系统',1,1,'2024-01-01 00:00:00','2025-01-01 00:00:00','管理配送机器人的任务分配、路径规划、状态监控'),(7,'APP002','机器人清洁调度系统',1,0,'2024-01-05 00:00:00','2025-02-01 00:00:00','调度清洁机器人的清洁任务、区域分配、清洁效果统计'),(8,'APP003','机器人巡检监控平台',2,0,'2024-01-10 00:00:00','2025-01-15 00:00:00','实时监控巡检机器人的位置、状态、巡检数据上报'),(9,'APP004','导诊机器人交互系统',0,0,'2024-01-15 00:00:00','2025-02-10 00:00:00','实现导诊机器人与患者的语音交互、导航引导、问题解答');
/*!40000 ALTER TABLE `t_app_library` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_app_param`
--

DROP TABLE IF EXISTS `t_app_param`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_app_param` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `param_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `param_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `param_type` enum('number','string','boolean','enum') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `default_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `validation_rule` json DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_app_param` (`app_id`,`param_key`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_app_param`
--

LOCK TABLES `t_app_param` WRITE;
/*!40000 ALTER TABLE `t_app_param` DISABLE KEYS */;
INSERT INTO `t_app_param` (`id`, `app_id`, `param_key`, `param_name`, `param_type`, `default_value`, `validation_rule`, `created_at`) VALUES (2,'APP001','max_speed','最大速度','number','1.3','{\"max\": 1.5, \"min\": 0.5}','2026-04-19 22:45:56'),(4,'APP001','speed','移动速度','number','1.0','{\"max\": 1.5, \"min\": 0.5}','2026-04-19 22:45:56'),(5,'APP001','max_load_weight','最大载重','number','50',NULL,'2026-04-21 01:05:14');
/*!40000 ALTER TABLE `t_app_param` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-06 17:01:29
