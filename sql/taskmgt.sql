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
-- Table structure for table `tm_task`
--

DROP TABLE IF EXISTS `tm_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tm_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `battery_threshold` int DEFAULT NULL,
  `cron_expression` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `duration` int DEFAULT NULL,
  `idle_time` int DEFAULT NULL,
  `is_group_task` int DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `pause_snapshot` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `pending_order` int DEFAULT NULL,
  `priority` int DEFAULT NULL,
  `risk_level` int DEFAULT NULL,
  `robot_group_id` bigint DEFAULT NULL,
  `robot_id` bigint DEFAULT NULL,
  `scheduled_time` datetime DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `task_type` int DEFAULT NULL,
  `template_id` bigint DEFAULT NULL,
  `terminate_reason` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_by` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `search_value` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_by` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `form_content` text COLLATE utf8mb4_general_ci,
  `global_pending_order` int DEFAULT NULL,
  `tenant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `task_name_index` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1057 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tm_task`
--

LOCK TABLES `tm_task` WRITE;
/*!40000 ALTER TABLE `tm_task` DISABLE KEYS */;
INSERT INTO `tm_task` VALUES (1,NULL,'0 30 9 * * ?',16,NULL,0,'3号病房药品配送',NULL,NULL,1,0,NULL,1,NULL,3,1,1,NULL,NULL,'2025-03-19 08:00:00',NULL,NULL,NULL,'2026-03-19 23:25:19',NULL,'{\"patientName\":\"张三\",\"roomNumber\":\"301\",\"medicineList\":\"/uploads/med1.pdf\"}',NULL,NULL),(2,80,NULL,30,NULL,1,'大会议室清洁',NULL,3,2,0,2,NULL,NULL,3,2,2,NULL,NULL,'2025-03-19 09:00:00',NULL,NULL,'admin','2026-03-21 01:30:32',NULL,'{\"roomName\":\"大会议室\",\"cleanLevel\":\"深度\"}',0,NULL),(3,NULL,NULL,62,15,0,'夜间巡检2025-03-18',NULL,NULL,3,0,NULL,3,NULL,6,3,3,NULL,NULL,'2025-03-18 22:00:00',NULL,NULL,NULL,'2025-03-19 00:02:00',NULL,'{\"checkPoints\":\"A区,B区,C区\"}',NULL,NULL),(4,NULL,'0 0 10 * * ?',20,NULL,0,'机房设备巡检',NULL,NULL,1,0,NULL,5,NULL,0,1,4,NULL,NULL,'2025-03-19 10:00:00',NULL,NULL,'system','2026-03-19 23:33:00',NULL,'{\"deviceIds\":\"SRV-01,SRV-02\"}',NULL,NULL),(5,20,NULL,16,NULL,0,'急诊药品配送',NULL,1,2,0,NULL,2,NULL,1,2,1,NULL,NULL,'2025-03-19 08:30:00',NULL,NULL,'admin','2026-03-24 10:41:31',NULL,'{\"patientName\":\"李四\",\"roomNumber\":\"E102\",\"medicineList\":\"/uploads/emergency.pdf\"}',1,NULL),(1005,80,'',30,30,0,'测试任务',NULL,NULL,2,0,NULL,1,NULL,7,3,4,NULL,'admin','2026-03-19 17:54:49',NULL,NULL,NULL,'2026-03-24 23:56:47',NULL,'{\"deviceIds\":\"01\"}',NULL,NULL),(1006,NULL,'0 30 9 * * ?',20,NULL,0,'药品配送-住院部A区-001',NULL,0,1,0,NULL,1,NULL,0,1,1,NULL,'admin','2026-03-20 08:00:00',NULL,NULL,'system','2026-03-20 22:11:11',NULL,'{\"patientName\":\"王五\",\"roomNumber\":\"302\",\"medicineList\":\"/uploads/med2.pdf\"}',0,NULL),(1007,NULL,'0 30 9 * * ?',25,NULL,0,'药品配送-住院部A区-002',NULL,1,2,0,NULL,1,NULL,1,1,1,NULL,'admin','2026-03-20 08:05:00',NULL,NULL,'admin','2026-03-24 10:41:31',NULL,'{\"patientName\":\"赵六\",\"roomNumber\":\"303\",\"medicineList\":\"/uploads/med3.pdf\"}',6,NULL),(1008,NULL,'0 30 9 * * ?',15,NULL,0,'药品配送-住院部A区-003',NULL,2,3,0,NULL,1,NULL,4,1,1,NULL,'admin','2026-03-20 08:10:00',NULL,NULL,'admin','2026-03-21 00:30:47',NULL,'{\"patientName\":\"孙二\",\"roomNumber\":\"305\",\"medicineList\":\"/uploads/med4.pdf\"}',2,NULL),(1009,NULL,'0 30 10 * * ?',30,NULL,0,'药品配送-住院部B区-001',NULL,0,1,0,NULL,2,NULL,0,1,1,NULL,'admin','2026-03-20 08:15:00',NULL,NULL,'system','2026-03-20 22:11:11',NULL,'{\"patientName\":\"周八\",\"roomNumber\":\"401\",\"medicineList\":\"/uploads/med5.pdf\"}',3,NULL),(1010,NULL,'0 30 10 * * ?',20,NULL,0,'药品配送-住院部B区-002',NULL,3,2,0,NULL,2,NULL,1,1,1,NULL,'admin','2026-03-20 08:20:00',NULL,NULL,'admin','2026-03-24 10:41:31',NULL,'{\"patientName\":\"吴九\",\"roomNumber\":\"402\",\"medicineList\":\"/uploads/med6.pdf\"}',3,NULL),(1011,NULL,'0 30 10 * * ?',35,NULL,0,'药品配送-住院部B区-003',NULL,0,1,0,NULL,2,NULL,1,1,1,NULL,'admin','2026-03-20 08:25:00',NULL,NULL,'admin','2026-03-24 10:41:31',NULL,'{\"patientName\":\"郑十\",\"roomNumber\":\"403\",\"medicineList\":\"/uploads/med7.pdf\"}',0,NULL),(1012,NULL,'0 30 10 * * ?',25,NULL,0,'药品配送-住院部B区-004',NULL,2,3,0,NULL,2,NULL,1,1,1,NULL,'admin','2026-03-20 08:30:00',NULL,NULL,'admin','2026-03-24 10:41:31',NULL,'{\"patientName\":\"钱十一\",\"roomNumber\":\"405\",\"medicineList\":\"/uploads/med8.pdf\"}',2,NULL),(1013,NULL,'0 0 11 * * ?',15,NULL,0,'药房快速配送-001',NULL,0,2,0,NULL,5,NULL,1,1,1,NULL,'admin','2026-03-20 08:35:00',NULL,NULL,'admin','2026-03-24 10:41:31',NULL,'{\"patientName\":\"冯十二\",\"roomNumber\":\"201\",\"medicineList\":\"/uploads/med9.pdf\"}',4,NULL),(1014,NULL,'0 0 11 * * ?',20,NULL,0,'药房快速配送-002',NULL,1,1,0,NULL,5,NULL,1,1,1,NULL,'admin','2026-03-20 08:40:00',NULL,NULL,'admin','2026-03-24 10:41:31',NULL,'{\"patientName\":\"陈十三\",\"roomNumber\":\"202\",\"medicineList\":\"/uploads/med10.pdf\"}',5,NULL),(1015,NULL,NULL,45,NULL,1,'门诊楼深度清洁-周一',NULL,0,1,0,2,NULL,NULL,0,2,2,NULL,'admin','2026-03-20 07:00:00',NULL,NULL,'admin','2026-04-05 04:45:31',NULL,'{\"roomName\":\"门诊大厅\",\"cleanLevel\":\"深度\"}',6,NULL),(1016,NULL,NULL,60,NULL,1,'急诊楼全面消毒',NULL,1,2,0,2,NULL,NULL,1,2,2,NULL,'admin','2026-03-20 07:30:00',NULL,NULL,'admin','2026-04-05 04:45:21',NULL,'{\"roomName\":\"急诊楼\",\"cleanLevel\":\"标准\"}',7,NULL),(1017,NULL,NULL,30,NULL,1,'走廊日常保洁',NULL,2,3,0,2,NULL,NULL,1,2,2,NULL,'admin','2026-03-20 08:00:00',NULL,NULL,'admin','2026-03-24 10:41:31',NULL,'{\"roomName\":\"走廊区域\",\"cleanLevel\":\"标准\"}',9,NULL),(1018,NULL,NULL,120,20,1,'夜间安全巡检-全区域A',NULL,0,1,0,3,NULL,NULL,5,3,3,'机器人异常','admin','2026-03-20 06:00:00',NULL,NULL,'admin','2026-03-21 01:22:31',NULL,'{\"checkPoints\":\"A区,B区,C区,D区\"}',12,NULL),(1019,NULL,NULL,90,15,1,'停车场安全巡查',NULL,1,2,0,3,NULL,NULL,3,3,3,NULL,'admin','2026-03-20 06:30:00',NULL,NULL,'admin','2026-03-24 09:26:01',NULL,'{\"checkPoints\":\"P1,P2,P3\"}',10,NULL),(1020,NULL,NULL,60,10,1,'重点机房巡检',NULL,2,1,0,3,NULL,NULL,1,3,3,NULL,'admin','2026-03-20 07:00:00',NULL,NULL,'admin','2026-04-05 04:45:24',NULL,'{\"checkPoints\":\"机房A,机房B,机房C\"}',8,NULL),(1021,NULL,NULL,45,15,1,'外围围墙巡检',NULL,3,3,0,3,NULL,NULL,3,3,3,NULL,'admin','2026-03-20 07:30:00',NULL,NULL,'admin','2026-04-05 04:40:10',NULL,'{\"checkPoints\":\"围墙东,围墙西,围墙南,围墙北\"}',11,NULL),(1022,NULL,NULL,40,NULL,0,'门诊楼定点清洁-窗口',NULL,0,2,0,NULL,3,NULL,2,2,2,NULL,'admin','2026-03-20 08:45:00',NULL,NULL,'system','2026-03-24 09:23:24',NULL,'{\"roomName\":\"挂号窗口\",\"cleanLevel\":\"标准\"}',16,NULL),(1023,NULL,NULL,35,NULL,0,'门诊楼定点清洁-座椅',NULL,1,3,0,NULL,3,NULL,0,2,2,NULL,'admin','2026-03-20 08:50:00',NULL,NULL,'system','2026-03-24 09:23:28',NULL,'{\"roomName\":\"候诊区座椅\",\"cleanLevel\":\"标准\"}',11,NULL),(1024,NULL,NULL,50,NULL,0,'停车场入口监控检查',NULL,0,1,2,NULL,7,NULL,0,3,3,NULL,'admin','2026-03-20 08:55:00',NULL,NULL,'system','2026-03-20 22:11:12',NULL,'{\"checkPoints\":\"入口闸机,出口闸机\"}',18,NULL),(1025,NULL,NULL,40,NULL,0,'停车场消防设施巡检',NULL,0,2,1,NULL,7,NULL,1,3,3,NULL,'admin','2026-03-20 09:00:00',NULL,NULL,'admin','2026-03-24 10:41:31',NULL,'{\"checkPoints\":\"消防栓,灭火器,应急通道\"}',10,NULL),(1026,NULL,NULL,30,NULL,0,'停车场照明系统检查',NULL,1,3,0,NULL,7,NULL,1,3,3,NULL,'admin','2026-03-20 09:05:00',NULL,NULL,'admin','2026-03-24 10:41:31',NULL,'{\"checkPoints\":\"照明设备A区,照明设备B区\"}',12,NULL),(1027,NULL,'0 0 14 * * ?',90,NULL,1,'全院药品集中配送',NULL,0,1,0,1,NULL,NULL,3,1,1,NULL,'admin','2026-03-20 05:00:00',NULL,NULL,'admin','2026-03-24 09:26:18',NULL,'{\"patientName\":\"多科室\",\"roomNumber\":\"全院\",\"medicineList\":\"/uploads/batch_med.pdf\"}',14,NULL),(1028,NULL,'0 0 15 * * ?',60,NULL,1,'检验样本运送任务',NULL,1,2,0,1,NULL,NULL,6,1,1,NULL,'admin','2026-03-20 05:30:00',NULL,NULL,'admin','2026-04-05 04:45:37',NULL,'{\"patientName\":\"检验科\",\"roomNumber\":\"各科室\",\"medicineList\":\"/uploads/samples.pdf\"}',13,NULL),(1029,NULL,'0 0 16 * * ?',45,NULL,1,'医疗废物回收运输',NULL,2,1,0,1,NULL,NULL,6,1,1,NULL,'admin','2026-03-20 06:00:00',NULL,NULL,'admin','2026-04-05 04:45:40',NULL,'{\"patientName\":\"废物处理\",\"roomNumber\":\"全院\",\"medicineList\":\"/uploads/waste.pdf\"}',14,NULL),(1030,80,'',30,30,0,'测试任务1',NULL,NULL,2,0,NULL,5,NULL,7,3,6,NULL,'admin','2026-03-21 02:09:43',NULL,NULL,NULL,'2026-03-24 23:56:42',NULL,'{\"image\":[{\"name\":\"屏幕截图 2025-04-20 000925.png\",\"url\":\"http://localhost:8080/profile/upload/2026/03/21/屏幕截图 2025-04-20 000925_20260321020856A001.png\",\"fileName\":\"/profile/upload/2026/03/21/屏幕截图 2025-04-20 000925_20260321020856A001.png\"}],\"file\":[{\"name\":\"神机百炼 12.05汇报.pptx\",\"url\":\"http://localhost:8080/profile/upload/2026/03/21/神机百炼 12.05汇报_20260321020915A002.pptx\",\"fileName\":\"/profile/upload/2026/03/21/神机百炼 12.05汇报_20260321020915A002.pptx\"}]}',NULL,NULL),(1032,80,'',30,30,1,'测试',NULL,NULL,2,0,1,NULL,NULL,4,2,6,NULL,'admin','2026-03-25 00:03:38',NULL,NULL,NULL,'2026-03-25 01:03:25',NULL,'{\"image\":[],\"file\":[],\"x\":\"12\",\"y\":\"14\",\"speed\":\"50\"}',NULL,NULL);
/*!40000 ALTER TABLE `tm_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tm_task_log`
--

DROP TABLE IF EXISTS `tm_task_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tm_task_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `task_id` bigint NOT NULL COMMENT '关联任务ID',
  `step_id` bigint DEFAULT NULL COMMENT '关联步骤ID（可为空，表示任务级事件）',
  `event_type` varchar(50) NOT NULL COMMENT '事件类型（start/complete/pause/resume/terminate/error/update等）',
  `content` text COMMENT '事件描述或详细信息',
  `operator` varchar(50) DEFAULT NULL COMMENT '操作人（如果是系统自动，可为system）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '日志时间',
  `create_by` varchar(255) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `search_value` varchar(255) DEFAULT NULL,
  `update_by` varchar(255) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `tenant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2831 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务执行日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tm_task_log`
--

LOCK TABLES `tm_task_log` WRITE;
/*!40000 ALTER TABLE `tm_task_log` DISABLE KEYS */;
INSERT INTO `tm_task_log` VALUES (1,1,1,'STEP_START','开始执行取药步骤','system','2025-03-19 09:00:00',NULL,NULL,NULL,NULL,NULL,NULL),(2,1,1,'STEP_COMPLETE','取药完成','system','2025-03-19 09:05:00',NULL,NULL,NULL,NULL,NULL,NULL),(3,1,2,'STEP_START','开始运输','system','2025-03-19 09:05:00',NULL,NULL,NULL,NULL,NULL,NULL),(4,1,2,'STEP_COMPLETE','到达病房','system','2025-03-19 09:13:00',NULL,NULL,NULL,NULL,NULL,NULL),(5,1,3,'STEP_START','开始交付','system','2025-03-19 09:13:00',NULL,NULL,NULL,NULL,NULL,NULL),(6,2,4,'STEP_START','准备工具','system','2025-03-19 10:00:00',NULL,NULL,NULL,NULL,NULL,NULL),(7,2,4,'STEP_COMPLETE','工具准备完成','system','2025-03-19 10:03:00',NULL,NULL,NULL,NULL,NULL,NULL),(8,2,5,'STEP_START','开始清洁桌面','system','2025-03-19 10:03:00',NULL,NULL,NULL,NULL,NULL,NULL),(9,3,8,'STEP_START','启动巡检','system','2025-03-18 22:00:00',NULL,NULL,NULL,NULL,NULL,NULL),(10,3,8,'STEP_COMPLETE','自检完成','system','2025-03-18 22:02:00',NULL,NULL,NULL,NULL,NULL,NULL),(11,3,9,'STEP_START','区域1巡检','system','2025-03-18 22:02:00',NULL,NULL,NULL,NULL,NULL,NULL),(12,3,9,'STEP_COMPLETE','区域1正常','system','2025-03-18 22:22:00',NULL,NULL,NULL,NULL,NULL,NULL),(13,3,10,'STEP_START','区域2巡检','system','2025-03-18 22:22:00',NULL,NULL,NULL,NULL,NULL,NULL),(14,3,10,'STEP_COMPLETE','区域2正常','system','2025-03-18 22:42:00',NULL,NULL,NULL,NULL,NULL,NULL),(15,3,11,'STEP_START','区域3巡检','system','2025-03-18 22:42:00',NULL,NULL,NULL,NULL,NULL,NULL),(16,3,11,'STEP_COMPLETE','区域3正常','system','2025-03-18 22:57:00',NULL,NULL,NULL,NULL,NULL,NULL),(17,3,12,'STEP_START','生成报告','system','2025-03-18 22:57:00',NULL,NULL,NULL,NULL,NULL,NULL),(18,3,12,'STEP_COMPLETE','报告生成','system','2025-03-18 23:02:00',NULL,NULL,NULL,NULL,NULL,NULL),(19,5,15,'STEP_START','急诊取药','system','2025-03-19 08:30:00',NULL,NULL,NULL,NULL,NULL,NULL),(20,5,15,'STEP_COMPLETE','取药完成','system','2025-03-19 08:32:00',NULL,NULL,NULL,NULL,NULL,NULL),(21,5,16,'STEP_START','急诊运输','system','2025-03-19 08:32:00',NULL,NULL,NULL,NULL,NULL,NULL),(921,1005,NULL,'TASK_CREATE','创建任务：测试任务','admin','2026-03-19 17:54:49',NULL,NULL,NULL,NULL,NULL,NULL),(2724,4,NULL,'TASK_START','任务开始执行','system','2026-03-19 23:33:00',NULL,NULL,NULL,NULL,NULL,NULL),(2725,4,13,'STEP_START','步骤 服务器A 开始执行','system','2026-03-19 23:33:00',NULL,NULL,NULL,NULL,NULL,NULL),(2730,2,NULL,'TASK_PAUSE',' 任务大会议室清洁已暂停','admin','2026-03-20 02:39:48',NULL,NULL,NULL,NULL,NULL,NULL),(2731,2,NULL,'TASK_RESUME',' 任务大会议室清洁已继续','admin','2026-03-20 02:48:37',NULL,NULL,NULL,NULL,NULL,NULL),(2732,1006,NULL,'TASK_START','任务开始执行','system','2026-03-20 22:11:11',NULL,NULL,NULL,NULL,NULL,NULL),(2733,1009,NULL,'TASK_START','任务开始执行','system','2026-03-20 22:11:11',NULL,NULL,NULL,NULL,NULL,NULL),(2734,1018,NULL,'TASK_START','任务开始执行','system','2026-03-20 22:11:11',NULL,NULL,NULL,NULL,NULL,NULL),(2735,1022,NULL,'TASK_START','任务开始执行','system','2026-03-20 22:11:11',NULL,NULL,NULL,NULL,NULL,NULL),(2736,1024,NULL,'TASK_START','任务开始执行','system','2026-03-20 22:11:12',NULL,NULL,NULL,NULL,NULL,NULL),(2737,2,NULL,'TASK_PAUSE',' 任务大会议室清洁已暂停','admin','2026-03-20 22:53:07',NULL,NULL,NULL,NULL,NULL,NULL),(2738,1008,NULL,'TASK_CANCEL',' 任务药品配送-住院部A区-003已取消','admin','2026-03-20 23:17:00',NULL,NULL,NULL,NULL,NULL,NULL),(2739,1019,NULL,'TASK_CANCEL',' 任务停车场安全巡查已取消','admin','2026-03-20 23:20:29',NULL,NULL,NULL,NULL,NULL,NULL),(2740,2,NULL,'TASK_CANCEL',' 任务大会议室清洁已取消','admin','2026-03-21 01:04:25',NULL,NULL,NULL,NULL,NULL,NULL),(2741,1018,NULL,'TASK_PAUSE',' 任务夜间安全巡检-全区域A已暂停','admin','2026-03-21 01:05:37',NULL,NULL,NULL,NULL,NULL,NULL),(2742,1018,NULL,'TASK_TERMINATE',' 任务夜间安全巡检-全区域A已终止 终止原因：机器人异常','admin','2026-03-21 01:05:53',NULL,NULL,NULL,NULL,NULL,NULL),(2743,1018,NULL,'RISK_RESOLVED','管理员手动解决任务风险','admin','2026-03-21 01:22:31',NULL,NULL,NULL,NULL,NULL,NULL),(2744,2,NULL,'RISK_RESOLVED','管理员手动解决任务风险','admin','2026-03-21 01:30:32',NULL,NULL,NULL,NULL,NULL,NULL),(2745,1030,NULL,'TASK_CREATE','创建任务：测试任务1','admin','2026-03-21 02:09:43',NULL,NULL,NULL,NULL,NULL,NULL),(2746,1031,NULL,'TASK_CREATE','创建任务：test','admin','2026-03-24 09:22:09',NULL,NULL,NULL,NULL,NULL,NULL),(2748,1027,NULL,'TASK_CANCEL',' 任务全院药品集中配送已取消','admin','2026-03-24 09:23:08',NULL,NULL,NULL,NULL,NULL,NULL),(2749,1022,NULL,'TASK_PAUSE',' 任务门诊楼定点清洁-窗口已暂停','admin','2026-03-24 09:23:24',NULL,NULL,NULL,NULL,NULL,NULL),(2750,1015,NULL,'TASK_START','任务开始执行','system','2026-03-24 09:23:28',NULL,NULL,NULL,NULL,NULL,NULL),(2751,1023,NULL,'TASK_START','任务开始执行','system','2026-03-24 09:23:28',NULL,NULL,NULL,NULL,NULL,NULL),(2754,1019,NULL,'RISK_RESOLVED','管理员手动解决任务风险','admin','2026-03-24 09:26:01',NULL,NULL,NULL,NULL,NULL,NULL),(2755,1027,NULL,'RISK_RESOLVED','管理员手动解决任务风险','admin','2026-03-24 09:26:18',NULL,NULL,NULL,NULL,NULL,NULL),(2814,1031,NULL,'TASK_PENDING','任务达到触发条件，进入准备队列','system','2026-03-24 23:56:00',NULL,NULL,NULL,NULL,NULL,NULL),(2815,1030,NULL,'TASK_DELETE',' 任务测试任务1已删除 终止原因：','admin','2026-03-24 23:56:42',NULL,NULL,NULL,NULL,NULL,NULL),(2816,1005,NULL,'TASK_DELETE',' 任务测试任务已删除 终止原因：','admin','2026-03-24 23:56:47',NULL,NULL,NULL,NULL,NULL,NULL),(2817,1032,NULL,'TASK_CREATE','创建任务：测试','admin','2026-03-25 00:03:38',NULL,NULL,NULL,NULL,NULL,NULL),(2818,1056,NULL,'TASK_CREATE','创建任务：tt','admin','2026-03-31 02:18:34',NULL,NULL,NULL,NULL,NULL,NULL),(2819,1056,NULL,'TASK_DELETE',' 任务tt已删除 终止原因：','admin','2026-03-31 02:19:12',NULL,NULL,NULL,NULL,NULL,NULL),(2820,1029,NULL,'TASK_START','任务开始执行','system','2026-04-05 04:31:59',NULL,NULL,NULL,NULL,NULL,NULL),(2821,1029,NULL,'TASK_COMPLETE','任务执行完成','system','2026-04-05 04:31:59',NULL,NULL,NULL,NULL,NULL,NULL),(2822,1028,NULL,'TASK_START','任务开始执行','system','2026-04-05 04:31:59',NULL,NULL,NULL,NULL,NULL,NULL),(2823,1028,NULL,'TASK_COMPLETE','任务执行完成','system','2026-04-05 04:31:59',NULL,NULL,NULL,NULL,NULL,NULL),(2824,1021,NULL,'TASK_CANCEL',' 任务外围围墙巡检已取消','admin','2026-04-05 04:40:04',NULL,NULL,NULL,NULL,NULL,NULL),(2825,1021,NULL,'RISK_RESOLVED','手动解决任务风险','admin','2026-04-05 04:40:10',NULL,NULL,NULL,NULL,NULL,NULL),(2826,1016,NULL,'RISK_RESOLVED','手动解决任务风险','admin','2026-04-05 04:45:21',NULL,NULL,NULL,NULL,NULL,NULL),(2827,1020,NULL,'RISK_RESOLVED','手动解决任务风险','admin','2026-04-05 04:45:24',NULL,NULL,NULL,NULL,NULL,NULL),(2828,1015,NULL,'RISK_RESOLVED','手动解决任务风险','admin','2026-04-05 04:45:31',NULL,NULL,NULL,NULL,NULL,NULL),(2829,1028,NULL,'RISK_RESOLVED','手动解决任务风险','admin','2026-04-05 04:45:37',NULL,NULL,NULL,NULL,NULL,NULL),(2830,1029,NULL,'RISK_RESOLVED','手动解决任务风险','admin','2026-04-05 04:45:40',NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `tm_task_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tm_task_step`
--

DROP TABLE IF EXISTS `tm_task_step`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tm_task_step` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `end_time` datetime DEFAULT NULL,
  `log` text COLLATE utf8mb4_general_ci,
  `order_num` int DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `step_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `task_id` bigint DEFAULT NULL,
  `create_by` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `search_value` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_by` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `assigned_robot_id` bigint DEFAULT NULL,
  `error_msg` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `operation_id` bigint DEFAULT NULL,
  `operation_json` text COLLATE utf8mb4_general_ci,
  `result_data` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `trace_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `estimated_time` int DEFAULT NULL,
  `tenant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=127 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tm_task_step`
--

LOCK TABLES `tm_task_step` WRITE;
/*!40000 ALTER TABLE `tm_task_step` DISABLE KEYS */;
INSERT INTO `tm_task_step` VALUES (1,'2025-03-19 09:05:00','药品已领取',1,'2025-03-19 09:00:00',2,'取药',1,NULL,NULL,NULL,NULL,NULL,NULL,'在药房领取药品',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(2,'2025-03-19 09:13:00','已到达301病房',2,'2025-03-19 09:05:00',2,'运输',1,NULL,NULL,NULL,NULL,NULL,NULL,'前往目标病房',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(3,NULL,'等待护士签收',3,'2025-03-19 09:13:00',1,'交付',1,NULL,NULL,NULL,NULL,NULL,NULL,'将药品交给护士',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(4,'2025-03-19 10:03:00','工具已准备',1,'2025-03-19 10:00:00',2,'准备工具',2,NULL,NULL,NULL,NULL,NULL,NULL,'准备清洁工具和药剂',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(5,NULL,NULL,2,NULL,0,'清洁桌面',2,NULL,NULL,NULL,NULL,NULL,NULL,'擦拭桌面和椅子',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(6,NULL,NULL,3,NULL,0,'地面清洁',2,NULL,NULL,NULL,NULL,NULL,NULL,'吸尘或拖地',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(7,NULL,NULL,4,NULL,0,'检查验收',2,NULL,NULL,NULL,NULL,NULL,NULL,'检查清洁效果',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(8,'2025-03-18 22:02:00','自检完成',1,'2025-03-18 22:00:00',2,'启动巡检',3,NULL,NULL,NULL,NULL,NULL,NULL,'机器人启动并自检',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(9,'2025-03-18 22:22:00','A区正常',2,'2025-03-18 22:02:00',2,'区域1',3,NULL,NULL,NULL,NULL,NULL,NULL,'检查A区门窗',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(10,'2025-03-18 22:42:00','B区正常',3,'2025-03-18 22:22:00',2,'区域2',3,NULL,NULL,NULL,NULL,NULL,NULL,'检查B区消防设备',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(11,'2025-03-18 22:57:00','C区正常',4,'2025-03-18 22:42:00',2,'区域3',3,NULL,NULL,NULL,NULL,NULL,NULL,'检查C区监控',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(12,'2025-03-18 23:02:00','报告已生成',5,'2025-03-18 22:57:00',2,'生成报告',3,NULL,NULL,NULL,NULL,NULL,NULL,'汇总巡检结果',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(13,NULL,NULL,1,'2026-03-19 23:33:00',1,'服务器A',4,NULL,NULL,NULL,NULL,'system','2026-03-19 23:33:00','检查状态',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(14,NULL,NULL,2,NULL,0,'服务器B',4,NULL,NULL,NULL,NULL,NULL,NULL,'检查温度',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(15,'2025-03-19 08:32:00','已取药',1,'2025-03-19 08:30:00',2,'取药',5,NULL,NULL,NULL,NULL,NULL,NULL,'在药房领取药品',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(16,NULL,'途中',2,'2025-03-19 08:32:00',2,'运输',5,NULL,NULL,NULL,NULL,NULL,NULL,'前往急诊',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(124,NULL,NULL,1,NULL,0,'test',1031,'admin','2026-03-24 09:22:10',NULL,NULL,NULL,NULL,'',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(125,NULL,NULL,1,NULL,0,'step1',1032,'admin','2026-03-25 00:03:39',NULL,NULL,NULL,NULL,'第一步',NULL,NULL,1001,'{}',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `tm_task_step` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tm_template`
--

DROP TABLE IF EXISTS `tm_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tm_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '描述',
  `form_content` text COLLATE utf8mb4_general_ci COMMENT '表单字段',
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  `robot_group_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '机器人组ids',
  `status` tinyint DEFAULT NULL COMMENT '状态',
  `workflow` text COLLATE utf8mb4_general_ci COMMENT '标准任务流程',
  `create_by` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `search_value` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_by` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `app_id` bigint DEFAULT NULL,
  `rule` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `tenant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `template_name_index` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tm_template`
--

LOCK TABLES `tm_template` WRITE;
/*!40000 ALTER TABLE `tm_template` DISABLE KEYS */;
INSERT INTO `tm_template` VALUES (1,'从药房到病房的药品配送流程','{\"fields\":[{\"id\":\"patientName\",\"label\":\"患者\",\"type\":\"text\",\"required\":true},{\"id\":\"roomNumber\",\"label\":\"病房号\",\"type\":\"text\",\"required\":true},{\"id\":\"medicineList\",\"label\":\"药品清单\",\"type\":\"file\",\"maxCount\":5,\"maxSize\":10,\"accept\":[]}]}','常规药品配送','1,2',0,'{\"steps\":[{\"name\":\"取药\",\"description\":\"在药房领取药品\",\"estimatedTime\":5},{\"name\":\"运输\",\"description\":\"前往目标病房\",\"estimatedTime\":8},{\"name\":\"交付\",\"description\":\"将药品交给护士或患者\",\"estimatedTime\":3}]}',NULL,'2026-03-19 10:31:26',NULL,NULL,'admin','2026-03-21 01:03:36',NULL,NULL,NULL),(2,'会议室深度清洁流程','{\"fields\":[{\"id\":\"roomName\",\"label\":\"会议室名称\",\"type\":\"text\",\"required\":true},{\"id\":\"cleanLevel\",\"label\":\"清洁等级\",\"type\":\"select\",\"options\":[\"标准\",\"深度\"],\"required\":true}]}','会议室清洁','2',0,'{\"steps\":[{\"name\":\"准备工具\",\"description\":\"准备清洁工具和药剂\",\"estimatedTime\":3},{\"name\":\"清洁桌面\",\"description\":\"擦拭桌面和椅子\",\"estimatedTime\":10},{\"name\":\"地面清洁\",\"description\":\"吸尘或拖地\",\"estimatedTime\":15},{\"name\":\"检查验收\",\"description\":\"检查清洁效果\",\"estimatedTime\":2}]}',NULL,'2026-03-19 10:31:26',NULL,NULL,NULL,'2026-03-19 10:31:26',NULL,NULL,NULL),(3,'全区域安全巡检','{\"fields\":[{\"id\":\"checkPoints\",\"label\":\"巡检点列表\",\"type\":\"text\",\"required\":true}]}','夜间巡检','3',0,'{\"steps\":[{\"name\":\"启动巡检\",\"description\":\"机器人启动并自检\",\"estimatedTime\":2},{\"name\":\"区域1\",\"description\":\"检查A区门窗\",\"estimatedTime\":20},{\"name\":\"区域2\",\"description\":\"检查B区消防设备\",\"estimatedTime\":20},{\"name\":\"区域3\",\"description\":\"检查C区监控\",\"estimatedTime\":15},{\"name\":\"生成报告\",\"description\":\"汇总巡检结果\",\"estimatedTime\":5}]}',NULL,'2026-03-19 10:31:26',NULL,NULL,NULL,'2026-03-19 10:31:26',NULL,NULL,NULL),(4,'数据中心设备巡检','{\"fields\":[{\"id\":\"deviceIds\",\"label\":\"设备编号\",\"type\":\"text\",\"required\":true}]}','设备巡检','1,3',0,'{\"steps\":[{\"name\":\"服务器A\",\"description\":\"检查状态\",\"estimatedTime\":10},{\"name\":\"服务器B\",\"description\":\"检查温度\",\"estimatedTime\":10}]}',NULL,'2026-03-19 10:31:26',NULL,NULL,NULL,'2026-03-19 10:31:26',NULL,NULL,NULL),(6,'','{\"fields\":[{\"id\":\"image\",\"label\":\"图片\",\"type\":\"image\",\"required\":false,\"maxCount\":10,\"maxSize\":100,\"accept\":[]},{\"id\":\"file\",\"label\":\"文件\",\"type\":\"file\",\"required\":false,\"maxCount\":1,\"maxSize\":100,\"accept\":[\".ppt,.pptx\"]},{\"id\":\"x\",\"label\":\"x坐标\",\"type\":\"text\",\"required\":true,\"maxCount\":1,\"maxSize\":10,\"accept\":[]},{\"id\":\"y\",\"label\":\"y坐标\",\"type\":\"text\",\"required\":true,\"maxCount\":1,\"maxSize\":10,\"accept\":[]},{\"id\":\"speed\",\"label\":\"移动速度\",\"type\":\"text\",\"required\":false,\"maxCount\":1,\"maxSize\":10,\"accept\":[]}]}','测试模板','1,2,3',0,'{\"steps\":[{\"name\":\"step1\",\"description\":\"第一步\",\"estimatedTime\":5,\"operationId\":1001,\"paramMapping\":[{\"paramName\":\"x\",\"paramLabel\":\"X坐标\",\"paramType\":\"field_ref\",\"required\":true,\"options\":[],\"description\":\"目标位置X坐标，单位：米\",\"sourceType\":\"field\",\"sourceValue\":\"x\"},{\"paramName\":\"y\",\"paramLabel\":\"Y坐标\",\"paramType\":\"field_ref\",\"required\":true,\"options\":[],\"description\":\"目标位置Y坐标，单位：米\",\"sourceType\":\"field\",\"sourceValue\":\"y\"},{\"paramName\":\"speed\",\"paramLabel\":\"移动速度\",\"paramType\":\"number\",\"required\":false,\"options\":[],\"description\":\"移动速度百分比，1-100\",\"sourceType\":\"fixed\",\"sourceValue\":50},{\"paramName\":\"async\",\"paramLabel\":\"异步执行\",\"paramType\":\"boolean\",\"required\":false,\"options\":[],\"description\":\"是否异步执行\",\"sourceType\":\"fixed\",\"sourceValue\":\"false\"}]}]}','admin','2026-03-19 17:56:35',NULL,NULL,'admin','2026-03-24 23:59:28',NULL,NULL,NULL),(11,'','{\"fields\":[{\"id\":\"weight\",\"label\":\"重量\",\"type\":\"number\",\"required\":false,\"maxCount\":1,\"maxSize\":10,\"accept\":[]}]}','tt','1,2,3',2,'{\"steps\":[{\"name\":\"步骤\",\"description\":\"\",\"estimatedTime\":5,\"apiId\":1,\"paramMapping\":[{\"paramName\":\"name\",\"paramLabel\":\"name\",\"paramType\":\"string\",\"required\":false,\"sourceType\":\"field\",\"sourceValue\":\"\"},{\"paramName\":\"type\",\"paramLabel\":\"type\",\"paramType\":\"string\",\"required\":false,\"sourceType\":\"field\",\"sourceValue\":\"weight\"},{\"paramName\":\"required\",\"paramLabel\":\"required\",\"paramType\":\"string\",\"required\":false,\"sourceType\":\"field\",\"sourceValue\":\"\"}]}]}','admin','2026-03-31 01:01:29',NULL,NULL,'admin','2026-03-31 01:35:10',6,'',NULL),(13,'','{\"fields\":[{\"id\":\"weight\",\"label\":\"重量\",\"type\":\"number\",\"required\":false,\"maxCount\":1,\"maxSize\":10,\"accept\":[]},{\"id\":\"speed\",\"label\":\"速度\",\"type\":\"number\",\"required\":false,\"maxCount\":1,\"maxSize\":10,\"accept\":[]},{\"id\":\"target\",\"label\":\"目的地\",\"type\":\"text\",\"required\":false,\"maxCount\":1,\"maxSize\":10,\"accept\":[]}]}','t','1,2,3',1,'{\"steps\":[{\"name\":\"移动到取货点\",\"description\":\"\",\"estimatedTime\":5,\"apiId\":1,\"paramMapping\":[{\"paramName\":\"speed\",\"paramLabel\":\"速度\",\"paramType\":\"speed\",\"required\":true,\"sourceType\":\"field\",\"sourceValue\":\"speed\"},{\"paramName\":\"target\",\"paramLabel\":\"target\",\"paramType\":\"string\",\"required\":true,\"sourceType\":\"field\",\"sourceValue\":\"target\"},{\"paramName\":\"weight\",\"paramLabel\":\"重量\",\"paramType\":\"number\",\"required\":true,\"sourceType\":\"field\",\"sourceValue\":\"weight\"}]}]}','admin','2026-03-31 01:37:40',NULL,NULL,'admin','2026-03-31 01:41:43',6,'4',NULL);
/*!40000 ALTER TABLE `tm_template` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-05 12:00:39
