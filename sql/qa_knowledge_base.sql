CREATE TABLE `qa_knowledge_base` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '知识库ID',
  `kb_name` varchar(100) NOT NULL COMMENT '知识库名称',
  `kb_desc` varchar(500) DEFAULT NULL COMMENT '知识库描述',
  `dify_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用Dify知识库',
  `dify_dataset_id` varchar(128) DEFAULT NULL COMMENT 'Dify知识库ID',
  `dify_dataset_api_key` varchar(255) DEFAULT NULL COMMENT 'Dify Dataset API Key',
  `dify_indexing_technique` varchar(64) DEFAULT NULL COMMENT 'Dify索引方式',
  `dify_doc_form` varchar(64) DEFAULT NULL COMMENT 'Dify文档模式',
  `dify_doc_language` varchar(64) DEFAULT NULL COMMENT 'Dify文档语言',
  `dify_process_rule_mode` varchar(64) DEFAULT NULL COMMENT 'Dify处理模式',
  `dify_rule_separator` varchar(32) DEFAULT NULL COMMENT 'Dify分段分隔符',
  `dify_rule_max_tokens` int DEFAULT NULL COMMENT 'Dify分段最大长度',
  `dify_rule_chunk_overlap` int DEFAULT NULL COMMENT 'Dify分段重叠长度',
  `dify_remove_extra_spaces` tinyint(1) DEFAULT NULL COMMENT 'Dify去除多余空格',
  `dify_remove_urls_emails` tinyint(1) DEFAULT NULL COMMENT 'Dify去除URL和邮箱',
  `api_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用外部API',
  `api_base_url` varchar(255) DEFAULT NULL COMMENT 'API基础地址',
  `api_auth_token` varchar(255) DEFAULT NULL COMMENT 'API认证Token',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_qa_knowledge_base_name` (`kb_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='QA知识库配置表';

ALTER TABLE `qa_file`
  ADD COLUMN `knowledge_base_id` bigint DEFAULT NULL COMMENT '所属知识库ID' AFTER `dify_document_id`,
  ADD KEY `idx_qa_file_knowledge_base_id` (`knowledge_base_id`);
