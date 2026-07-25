CREATE TABLE `qa_chat` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '问答ID',
  `chat_name` varchar(100) NOT NULL COMMENT '问答名称',
  `chat_desc` varchar(500) DEFAULT NULL COMMENT '问答描述',
  `dify_api_key` varchar(255) NOT NULL COMMENT 'Dify应用Key',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_qa_chat_name` (`chat_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='问答配置表';

CREATE TABLE `qa_robot_chat_rel` (
  `robot_id` bigint NOT NULL COMMENT '机器人ID',
  `chat_id` bigint NOT NULL COMMENT '问答ID',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`robot_id`),
  KEY `idx_qa_robot_chat_rel_chat_id` (`chat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人问答绑定表';

INSERT INTO `ry-vue`.`sys_menu`
(`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2008, '问答管理', 2000, 2, 'RobotChatManage', 'qa/chat/manage', NULL, '', 1, 0, 'C', '0', '0', '', '#', 'admin', NOW(), 'admin', NOW(), '问答管理页面');
