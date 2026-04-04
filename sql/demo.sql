DROP TABLE IF EXISTS `robot_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `robot_task` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID（主键，自增）',
                              `battery_threshold` int DEFAULT NULL COMMENT '电量阈值（低于该值任务暂停/终止，单位：%）',
                              `cron_expression` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '定时任务Cron表达式（如每分钟执行：0 * * * * ?）',
                              `duration` int DEFAULT NULL COMMENT '任务预计执行时长（单位：分钟）',
                              `idle_time` int DEFAULT NULL COMMENT '任务闲置时长（单位：分钟，任务未执行的空闲时间）',
                              `is_group_task` int DEFAULT NULL COMMENT '是否为分组任务（0-否，1-是）',
                              `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务名称（唯一）',
                              `pause_snapshot` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '暂停快照（任务暂停时的状态快照JSON）',
                              `pending_order` int DEFAULT NULL COMMENT '待执行队列排序号（越小优先级越高）',
                              `priority` int DEFAULT NULL COMMENT '任务优先级（1-最高，5-最低）',
                              `risk_level` int DEFAULT NULL COMMENT '任务风险等级（0-无风险，1-低风险，2-中风险，3-高风险）',
                              `robot_group_id` bigint DEFAULT NULL COMMENT '所属机器人分组ID（关联robot_groups表id）',
                              `robot_id` bigint DEFAULT NULL COMMENT '绑定机器人ID（关联robots表id，分组任务时为NULL）',
                              `scheduled_time` datetime DEFAULT NULL COMMENT '任务计划执行时间（单次任务生效）',
                              `status` tinyint DEFAULT NULL COMMENT '任务状态（0-未开始，1-执行中，2-暂停，3-已完成，4-已终止，5-待执行）',
                              `task_type` int DEFAULT NULL COMMENT '任务类型（0-单次任务，1-定时任务，2-循环任务）',
                              `template_id` bigint DEFAULT NULL COMMENT '任务模板ID（关联task_template表id）',
                              `terminate_reason` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务终止原因（如电量不足、手动终止、异常报错）',
                              `create_by` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人（用户名/工号）',
                              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                              `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务备注说明',
                              `search_value` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '搜索关键字（冗余字段，用于快速检索）',
                              `update_by` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人（用户名/工号）',
                              `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                              `description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务详细描述',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `task_name_index` (`name`) COMMENT '任务名称唯一索引'
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='机器人任务表';