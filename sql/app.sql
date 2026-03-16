
-- 1. 应用库表：存储应用库基础信息
CREATE TABLE IF NOT EXISTS t_app_library (
                                             id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键，自增ID',
                                             app_id VARCHAR(64) NOT NULL COMMENT '应用唯一标识（业务ID）',
                                             app_name VARCHAR(128) NOT NULL COMMENT '应用名称',
                                             app_type TINYINT NOT NULL COMMENT '应用类型：0(交互类)、1(控制类)、2(监控类)',
                                             enabled TINYINT(1) NOT NULL COMMENT '启用状态：1(启用)、0(禁用)',
                                             create_time DATETIME NOT NULL COMMENT '创建时间',
                                             update_time DATETIME COMMENT '最后更新时间',
                                             description VARCHAR(512) COMMENT '应用描述',
                                             version VARCHAR(32) COMMENT '应用版本号（如1.0.0）',
                                             PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用库表';

-- 2. 应用配置表：存储应用详细配置信息
CREATE TABLE IF NOT EXISTS t_app_config (
                                            id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键，自增ID',
                                            app_id VARCHAR(64) NOT NULL COMMENT '关联应用库的app_id',
                                            config_key VARCHAR(128) NOT NULL COMMENT '配置项键名（如：timeout、max_retry）',
                                            config_value TEXT COMMENT '配置项值（支持复杂内容）',
                                            config_type TINYINT COMMENT '配置类型：0(字符串)、1(数字)、2(JSON)、3(布尔值)',
                                            config_label VARCHAR(64) COMMENT '配置项展示名称（前端显示）',
                                            config_desc VARCHAR(256) COMMENT '配置项描述',
                                            create_time DATETIME NOT NULL COMMENT '创建时间',
                                            update_time DATETIME COMMENT '最后更新时间',
                                            PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用配置表';

-- 3. 应用更新记录表：存储应用更新相关记录
CREATE TABLE IF NOT EXISTS t_app_update (
                                            id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键，自增ID',
                                            app_id VARCHAR(64) NOT NULL COMMENT '关联应用库的app_id',
                                            update_version VARCHAR(32) NOT NULL COMMENT '更新版本号（如2.0.0）',
                                            update_content TEXT NOT NULL COMMENT '更新内容（更新说明）',
                                            update_status TINYINT NOT NULL COMMENT '更新状态：0(待更新)、1(更新中)、2(更新成功)、3(更新失败)',
                                            robot_id VARCHAR(512) COMMENT '关联机器人ID',
                                            update_time DATETIME COMMENT '执行更新时间',
                                            fail_reason VARCHAR(512) COMMENT '更新失败原因（状态为3时填充）',
                                            create_time DATETIME NOT NULL COMMENT '创建时间（更新任务创建时间）',
                                            PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用更新记录表';

-- 4. 交互历史表：存储应用交互历史记录
CREATE TABLE IF NOT EXISTS t_interaction_history (
                                                     id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键，自增ID',
                                                     task_id VARCHAR(64) NOT NULL COMMENT '任务id',
                                                     interaction_id VARCHAR(64) NOT NULL COMMENT '记录id',
                                                     robot_id bigint COMMENT '交互的机器人ID',
                                                     user_id VARCHAR(64) COMMENT '操作用户ID（如管理员/终端用户）',
                                                     user_name VARCHAR(64) COMMENT '操作用户名称',
                                                     interaction_type TINYINT COMMENT '交互类型：0(配送)、1(清洁)、2(巡检)、3(维保)、4(安防)',
                                                     interaction_content TEXT COMMENT '交互内容（请求/响应数据）',
                                                     interaction_time DATETIME NOT NULL COMMENT '交互发生时间',
                                                     duration INT COMMENT '交互耗时（秒）',
                                                     rating TINYINT COMMENT '交互评分：0(差)、1(中等)、2(良好)',
                                                     evaluation_text VARCHAR(512) COMMENT '评价文本（用户/系统评价）',
                                                     status TINYINT COMMENT '交互状态：0(成功)、1(失败)、2(超时)',
                                                     ext_info TEXT COMMENT '扩展信息（JSON格式，存储额外字段）',
                                                     PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交互历史表';

