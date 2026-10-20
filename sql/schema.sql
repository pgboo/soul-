-- =====================================================================
-- Soul 风格年轻人社交系统一期 — 数据库 DDL
-- 版本: v1.0  日期: 2026-10-14  编写: 高见远(架构师)
-- 依据: docs/01-需求文档.md §9 + docs/02-架构设计.md
-- MySQL 8.0.35  localhost:3306  root/123456
-- 规范: 每表必含 id / create_time / update_time / is_delete
--       逻辑删除 is_delete=1，禁止物理删除
--       IM 沿用 im_conversation / im_message，严禁 chat 系表
-- =====================================================================

CREATE DATABASE IF NOT EXISTS soul_social
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE soul_social;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET sql_mode = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION';

-- =====================================================================
-- 一、用户域 (6 表)
-- =====================================================================

-- 1. user 用户主表（不含手机号/openid）
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_no`               VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '对外用户编号(可展示)',
  `status`                TINYINT      NOT NULL DEFAULT 1  COMMENT '用户状态:1正常 2封禁',
  `risk_level`            TINYINT      NOT NULL DEFAULT 0  COMMENT '风控等级:0正常 1观察',
  `mute_end_time`         DATETIME     NULL                 COMMENT '禁言截止时间(NULL=未禁言)',
  `profile_complete_flag` TINYINT      NOT NULL DEFAULT 0  COMMENT '资料完善:0否 1是',
  `tag_flag`              TINYINT      NOT NULL DEFAULT 0  COMMENT '兴趣标签完成:0否 1是',
  `test_flag`             TINYINT      NOT NULL DEFAULT 0  COMMENT '灵魂测试完成:0否 1是',
  `last_active_time`      DATETIME     NULL                 COMMENT '最后活跃时间',
  `create_time`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`             TINYINT      NOT NULL DEFAULT 0  COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_no` (`user_no`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户主表(敏感信息已分离)';

-- 2. user_sensitive_info 用户敏感信息（禁返回）
DROP TABLE IF EXISTS `user_sensitive_info`;
CREATE TABLE `user_sensitive_info` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`      BIGINT       NOT NULL COMMENT '用户ID',
  -- 注意: phone/openid/unionid 三者均可为 NULL。
  -- 未绑定该渠道时必须存 NULL 而非 ''，否则多个用户会在唯一索引上撞车
  -- (MySQL 唯一索引允许多个 NULL，但只允许一个 '')。
  `phone`        VARCHAR(20)  NULL DEFAULT NULL COMMENT '手机号(敏感)',
  `openid`       VARCHAR(64)  NULL DEFAULT NULL COMMENT '微信openid(敏感)',
  `unionid`      VARCHAR(64)  NULL DEFAULT NULL COMMENT '微信unionid(敏感)',
  `password`     VARCHAR(100) NOT NULL DEFAULT '' COMMENT '密码(仅后台账号用,BCrypt)',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户敏感信息表(手机号/openid,禁在Resp返回)';

-- 3. user_profile 用户资料
DROP TABLE IF EXISTS `user_profile`;
CREATE TABLE `user_profile` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`          BIGINT       NOT NULL COMMENT '用户ID',
  `nickname`         VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '昵称',
  `avatar`           VARCHAR(255) NOT NULL DEFAULT '' COMMENT '头像URL',
  `gender`           TINYINT      NOT NULL DEFAULT 0  COMMENT '性别:0未知 1男 2女',
  `birthday`         DATE         NULL COMMENT '生日',
  `city`             VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '城市',
  `sign`             VARCHAR(255) NOT NULL DEFAULT '' COMMENT '个性签名',
  `intro`            TEXT         NULL COMMENT '个人简介',
  `emotion_status`   TINYINT      NOT NULL DEFAULT 0  COMMENT '情感状态:0未设置 1单身 2恋爱中',
  `friend_goal`      TINYINT      NOT NULL DEFAULT 0  COMMENT '交友目的:0未设置 1找朋友 2找对象 3随便聊聊',
  `soul_id`          VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '灵魂号(可展示)',
  `complete_percent` TINYINT      NOT NULL DEFAULT 0  COMMENT '资料完整度百分比',
  `nickname_modify_count` TINYINT NOT NULL DEFAULT 0  COMMENT '昵称修改次数',
  `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_city` (`city`),
  KEY `idx_gender` (`gender`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资料表';

-- 4. user_privacy_setting 隐私设置
DROP TABLE IF EXISTS `user_privacy_setting`;
CREATE TABLE `user_privacy_setting` (
  `id`                 BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`            BIGINT   NOT NULL COMMENT '用户ID',
  `show_age`           TINYINT  NOT NULL DEFAULT 1 COMMENT '展示年龄:0否 1是',
  `show_city`          TINYINT  NOT NULL DEFAULT 1 COMMENT '展示城市:0否 1是',
  `show_online`        TINYINT  NOT NULL DEFAULT 1 COMMENT '展示在线状态:0否 1是',
  `allow_recommend`    TINYINT  NOT NULL DEFAULT 1 COMMENT '允许被推荐:0否 1是',
  `allow_stranger_msg` TINYINT  NOT NULL DEFAULT 1 COMMENT '允许陌生人私信:0否 1是',
  `create_time`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`          TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户隐私设置表';

-- 5. user_blacklist 黑名单
DROP TABLE IF EXISTS `user_blacklist`;
CREATE TABLE `user_blacklist` (
  `id`             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`        BIGINT   NOT NULL COMMENT '拉黑发起人',
  `target_user_id` BIGINT   NOT NULL COMMENT '被拉黑人',
  `status`         TINYINT  NOT NULL DEFAULT 1 COMMENT '状态:0取消 1生效',
  `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`      TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`,`target_user_id`),
  KEY `idx_target` (`target_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户黑名单表';

-- 6. user_login_log 登录日志
DROP TABLE IF EXISTS `user_login_log`;
CREATE TABLE `user_login_log` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
  `login_type`  TINYINT     NOT NULL DEFAULT 1 COMMENT '登录方式:1手机号 2微信 3后台账号',
  `login_ip`    VARCHAR(64) NOT NULL DEFAULT '' COMMENT '登录IP',
  `device_info` VARCHAR(255)NOT NULL DEFAULT '' COMMENT '设备信息',
  `login_status`TINYINT     NOT NULL DEFAULT 1 COMMENT '结果:1成功 0失败',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`   TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录日志表';

-- =====================================================================
-- 二、标签域 (3 表)
-- =====================================================================

DROP TABLE IF EXISTS `tag_category`;
CREATE TABLE `tag_category` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(32) NOT NULL DEFAULT '' COMMENT '分类名',
  `icon`        VARCHAR(255)NOT NULL DEFAULT '' COMMENT '图标URL',
  `sort`        INT         NOT NULL DEFAULT 0  COMMENT '排序(小在前)',
  `status`      TINYINT     NOT NULL DEFAULT 1  COMMENT '状态:0禁用 1启用',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`   TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签分类表';

DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_id`    BIGINT      NOT NULL COMMENT '所属分类ID',
  `name`           VARCHAR(32) NOT NULL DEFAULT '' COMMENT '标签名',
  `hot_flag`       TINYINT     NOT NULL DEFAULT 0  COMMENT '热门标签:0否 1是',
  `match_flag`     TINYINT     NOT NULL DEFAULT 1  COMMENT '用于匹配:0否 1是',
  `recommend_flag` TINYINT     NOT NULL DEFAULT 1  COMMENT '用于推荐:0否 1是',
  `sort`           INT         NOT NULL DEFAULT 0  COMMENT '排序',
  `status`         TINYINT     NOT NULL DEFAULT 1  COMMENT '状态:0禁用 1启用',
  `create_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`      TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_hot` (`hot_flag`),
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

DROP TABLE IF EXISTS `user_tag`;
CREATE TABLE `user_tag` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
  `tag_id`      BIGINT   NOT NULL COMMENT '标签ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`   TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tag` (`user_id`,`tag_id`),
  KEY `idx_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户标签关联表';

-- =====================================================================
-- 三、测试域 (4 表)
-- =====================================================================

DROP TABLE IF EXISTS `soul_test_question`;
CREATE TABLE `soul_test_question` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`       VARCHAR(255) NOT NULL DEFAULT '' COMMENT '题目标题',
  `description` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '题目描述',
  `sort`        INT          NOT NULL DEFAULT 0  COMMENT '排序',
  `status`      TINYINT      NOT NULL DEFAULT 1  COMMENT '状态:0禁用 1启用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='灵魂测试题目表';

DROP TABLE IF EXISTS `soul_test_option`;
CREATE TABLE `soul_test_option` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `question_id` BIGINT       NOT NULL COMMENT '题目ID',
  `content`     VARCHAR(255) NOT NULL DEFAULT '' COMMENT '选项内容',
  `result_code` VARCHAR(16)  NOT NULL DEFAULT '' COMMENT '倾向结果编码(如INFP)',
  `score`       INT          NOT NULL DEFAULT 0  COMMENT '分值',
  `sort`        INT          NOT NULL DEFAULT 0  COMMENT '排序',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='灵魂测试选项表';

DROP TABLE IF EXISTS `soul_test_result`;
CREATE TABLE `soul_test_result` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `result_code`   VARCHAR(16)  NOT NULL DEFAULT '' COMMENT '结果编码',
  `name`          VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '结果名称(如温柔倾听者)',
  `icon`          VARCHAR(255) NOT NULL DEFAULT '' COMMENT '结果图标URL',
  `description`   VARCHAR(500) NOT NULL DEFAULT '' COMMENT '结果描述',
  `social_advice` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '社交建议',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_result_code` (`result_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='灵魂测试结果类型表';

DROP TABLE IF EXISTS `user_soul_test_record`;
CREATE TABLE `user_soul_test_record` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
  `result_code` VARCHAR(16) NOT NULL DEFAULT '' COMMENT '结果编码',
  `answer_json` TEXT        NULL COMMENT '答题明细JSON',
  `score_json`  TEXT        NULL COMMENT '分值统计JSON',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`   TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户灵魂测试记录表(仅保留最后一次)';

-- =====================================================================
-- 四、动态域 (7 表)
-- =====================================================================

DROP TABLE IF EXISTS `dynamic_content`;
CREATE TABLE `dynamic_content` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`        BIGINT       NOT NULL COMMENT '作者ID',
  `content`        TEXT         NULL COMMENT '正文',
  `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1正常 2下架',
  `audit_status`   TINYINT      NOT NULL DEFAULT 0 COMMENT '审核:0已过审 1待审核 2拒绝',
  `anonymous_flag` TINYINT      NOT NULL DEFAULT 0 COMMENT '匿名:0否 1是',
  `visible_scope`  TINYINT      NOT NULL DEFAULT 1 COMMENT '可见范围:1公开 2仅好友 3仅自己',
  `like_count`     INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count`  INT          NOT NULL DEFAULT 0 COMMENT '评论数',
  `collect_count`  INT          NOT NULL DEFAULT 0 COMMENT '收藏数',
  `report_count`   INT          NOT NULL DEFAULT 0 COMMENT '举报数',
  `city`           VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '发布城市(同城频道用)',
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status_audit` (`status`,`audit_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_city` (`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态正文表';

DROP TABLE IF EXISTS `dynamic_media`;
CREATE TABLE `dynamic_media` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dynamic_id`  BIGINT       NOT NULL COMMENT '动态ID',
  `media_type`  TINYINT      NOT NULL DEFAULT 1 COMMENT '媒体类型:1图片 2视频',
  `media_url`   VARCHAR(500) NOT NULL DEFAULT '' COMMENT '媒体URL',
  `sort`        INT          NOT NULL DEFAULT 0  COMMENT '排序',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_dynamic` (`dynamic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态媒体表';

DROP TABLE IF EXISTS `dynamic_comment`;
CREATE TABLE `dynamic_comment` (
  `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dynamic_id`    BIGINT   NOT NULL COMMENT '动态ID',
  `user_id`       BIGINT   NOT NULL COMMENT '评论人',
  `parent_id`     BIGINT   NOT NULL DEFAULT 0 COMMENT '父评论ID(0=一级)',
  `reply_user_id` BIGINT   NOT NULL DEFAULT 0 COMMENT '被回复人ID',
  `content`       VARCHAR(500) NOT NULL DEFAULT '' COMMENT '评论内容',
  `status`        TINYINT  NOT NULL DEFAULT 1 COMMENT '状态:1正常 2删除/下架',
  `report_count`  INT      NOT NULL DEFAULT 0 COMMENT '举报数',
  `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_dynamic` (`dynamic_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态评论表';

DROP TABLE IF EXISTS `dynamic_like`;
CREATE TABLE `dynamic_like` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dynamic_id`  BIGINT   NOT NULL COMMENT '动态ID',
  `user_id`     BIGINT   NOT NULL COMMENT '点赞用户',
  `status`      TINYINT  NOT NULL DEFAULT 1 COMMENT '状态:0取消 1有效',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`   TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dynamic_user` (`dynamic_id`,`user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态点赞表';

DROP TABLE IF EXISTS `dynamic_collect`;
CREATE TABLE `dynamic_collect` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dynamic_id`  BIGINT   NOT NULL COMMENT '动态ID',
  `user_id`     BIGINT   NOT NULL COMMENT '收藏用户',
  `status`      TINYINT  NOT NULL DEFAULT 1 COMMENT '状态:0取消 1有效',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`   TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dynamic_user` (`dynamic_id`,`user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态收藏表';

DROP TABLE IF EXISTS `dynamic_topic`;
CREATE TABLE `dynamic_topic` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '话题名',
  `description` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '话题描述',
  `cover`       VARCHAR(255) NOT NULL DEFAULT '' COMMENT '封面URL',
  `hot_value`   INT          NOT NULL DEFAULT 0  COMMENT '热度值',
  `status`      TINYINT      NOT NULL DEFAULT 1  COMMENT '状态:0禁用 1启用',
  `sort`        INT          NOT NULL DEFAULT 0  COMMENT '排序',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题表';

DROP TABLE IF EXISTS `dynamic_topic_relation`;
CREATE TABLE `dynamic_topic_relation` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dynamic_id`  BIGINT   NOT NULL COMMENT '动态ID',
  `topic_id`    BIGINT   NOT NULL COMMENT '话题ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`   TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dynamic_topic` (`dynamic_id`,`topic_id`),
  KEY `idx_topic` (`topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态-话题关联表';

-- =====================================================================
-- 五、匹配域 (3 表)
-- =====================================================================

DROP TABLE IF EXISTS `match_record`;
CREATE TABLE `match_record` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`        BIGINT       NOT NULL COMMENT '用户ID',
  `target_user_id` BIGINT       NOT NULL COMMENT '被推荐用户ID',
  `match_score`    INT          NOT NULL DEFAULT 0 COMMENT '匹配度分值(0-100)',
  `source`         TINYINT      NOT NULL DEFAULT 1 COMMENT '来源:1推荐 2搜索',
  `expose_time`    DATETIME     NULL COMMENT '曝光时间',
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_target` (`target_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='匹配记录表';

DROP TABLE IF EXISTS `user_dislike_record`;
CREATE TABLE `user_dislike_record` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`        BIGINT       NOT NULL COMMENT '用户ID',
  `target_user_id` BIGINT       NOT NULL COMMENT '不感兴趣目标',
  `reason`         VARCHAR(128) NOT NULL DEFAULT '' COMMENT '原因(可选)',
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`,`target_user_id`),
  KEY `idx_target` (`target_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户不感兴趣记录表';

DROP TABLE IF EXISTS `greet_record`;
CREATE TABLE `greet_record` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `from_user_id` BIGINT       NOT NULL COMMENT '打招呼发起人',
  `to_user_id`   BIGINT       NOT NULL COMMENT '打招呼对象',
  `content`      VARCHAR(255) NOT NULL DEFAULT '' COMMENT '话术内容',
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1已发送',
  `read_flag`    TINYINT      NOT NULL DEFAULT 0 COMMENT '对方已读:0否 1是',
  `reply_flag`   TINYINT      NOT NULL DEFAULT 0 COMMENT '对方已回复:0否 1是',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_to_status` (`to_user_id`,`status`),
  KEY `idx_from_time` (`from_user_id`,`create_time`),
  KEY `uk_daily` (`from_user_id`,`to_user_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打招呼记录表';

-- =====================================================================
-- 六、IM 域 (2 表，沿用既有表名)
-- =====================================================================

DROP TABLE IF EXISTS `im_conversation`;
CREATE TABLE `im_conversation` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_uuid`    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '会话唯一标识',
  `user_id`         BIGINT       NOT NULL COMMENT '会话归属用户(本人)',
  `target_user_id`  BIGINT       NOT NULL COMMENT '对方用户',
  `session_type`    TINYINT      NOT NULL DEFAULT 1 COMMENT '类型:1普通 2打招呼 3AI',
  `unread_count`    INT          NOT NULL DEFAULT 0 COMMENT '未读数',
  `last_msg_preview`VARCHAR(255) NOT NULL DEFAULT '' COMMENT '最后消息预览',
  `last_msg_time`   DATETIME     NULL COMMENT '最后消息时间',
  `sort_time`       DATETIME     NULL COMMENT '会话排序时间',
  `top_flag`        TINYINT      NOT NULL DEFAULT 0 COMMENT '置顶:0否 1是',
  `hide_flag`       TINYINT      NOT NULL DEFAULT 0 COMMENT '隐藏:0否 1是',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_session` (`user_id`,`session_uuid`),
  KEY `idx_user_sort` (`user_id`,`sort_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM会话表(沿用既有表)';

DROP TABLE IF EXISTS `im_message`;
CREATE TABLE `im_message` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `message_uuid`  VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '消息唯一标识',
  `session_uuid`  VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '会话标识',
  `scene`         TINYINT      NOT NULL DEFAULT 1 COMMENT '场景:1单聊',
  `from_user_id`  BIGINT       NOT NULL COMMENT '发送人',
  `to_user_id`    BIGINT       NOT NULL COMMENT '接收人',
  `content`       TEXT         NULL COMMENT '消息内容',
  `content_type`  VARCHAR(16)  NOT NULL DEFAULT 'text' COMMENT '类型:text/image/voice(本期仅text)',
  `send_status`   TINYINT      NOT NULL DEFAULT 1 COMMENT '发送状态:0失败 1成功 2发送中',
  `read_flag`     TINYINT      NOT NULL DEFAULT 0 COMMENT '已读:0否 1是',
  `recall_flag`   TINYINT      NOT NULL DEFAULT 0 COMMENT '撤回:0否 1是',
  `from_delete_flag` TINYINT   NOT NULL DEFAULT 0 COMMENT '发送者已删除:0否 1是',
  `to_delete_flag`   TINYINT   NOT NULL DEFAULT 0 COMMENT '接收者已删除:0否 1是',
  `send_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_uuid` (`message_uuid`),
  KEY `idx_session_time` (`session_uuid`,`send_time`),
  KEY `idx_to_read` (`to_user_id`,`read_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM消息表(沿用既有表)';

-- =====================================================================
-- 七、通知域 (2 表)
-- =====================================================================

DROP TABLE IF EXISTS `user_notice`;
CREATE TABLE `user_notice` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT       NOT NULL COMMENT '接收用户',
  `notice_type` TINYINT      NOT NULL DEFAULT 1 COMMENT '类型:1点赞 2评论 3回复 4打招呼 5系统 6审核',
  `title`       VARCHAR(128) NOT NULL DEFAULT '' COMMENT '标题',
  `content`     VARCHAR(500) NOT NULL DEFAULT '' COMMENT '内容',
  `from_user_id`BIGINT       NOT NULL DEFAULT 0 COMMENT '触发用户',
  `target_type` TINYINT      NOT NULL DEFAULT 0 COMMENT '跳转目标类型:1动态 2会话 3主页 0无',
  `target_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '跳转目标ID',
  `read_flag`   TINYINT      NOT NULL DEFAULT 0 COMMENT '已读:0否 1是',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`,`read_flag`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内用户通知表';

DROP TABLE IF EXISTS `system_notice`;
CREATE TABLE `system_notice` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`        VARCHAR(128) NOT NULL DEFAULT '' COMMENT '标题',
  `content`      VARCHAR(1000)NOT NULL DEFAULT '' COMMENT '内容',
  `scope`        TINYINT      NOT NULL DEFAULT 1 COMMENT '范围:1全体 2指定用户',
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0草稿 1已发布',
  `publish_time` DATETIME     NULL COMMENT '发布时间',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- =====================================================================
-- 八、配置域 (1 表)
-- =====================================================================

DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_key`   VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '配置键',
  `config_value` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '配置值',
  `description`  VARCHAR(255) NOT NULL DEFAULT '' COMMENT '说明',
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0禁用 1启用',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- =====================================================================
-- 九、治理域 (4 表，本期建表，部分使用)
-- =====================================================================

DROP TABLE IF EXISTS `sensitive_word`;
CREATE TABLE `sensitive_word` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `word`        VARCHAR(64) NOT NULL DEFAULT '' COMMENT '敏感词',
  `category`    VARCHAR(32) NOT NULL DEFAULT '' COMMENT '分类',
  `level`       TINYINT     NOT NULL DEFAULT 1 COMMENT '级别:1替换 2拦截',
  `status`      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态:0禁用 1启用',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`   TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  -- 注意：不加 word 唯一索引。敏感词采用逻辑删除，若加 UNIQUE(word)，
  -- 删除后的行仍占用唯一键，导致「删除后无法重新添加同一个词」（撞 uk_word）。
  -- 唯一性由应用层 AdminSensitiveWordServiceImpl 校验（查询含已删除记录）。
  KEY `idx_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

DROP TABLE IF EXISTS `audit_record`;
CREATE TABLE `audit_record` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_type`      TINYINT      NOT NULL DEFAULT 1 COMMENT '业务类型:1动态 2评论 3资料 4昵称',
  `biz_id`        BIGINT       NOT NULL COMMENT '业务ID',
  `user_id`       BIGINT       NOT NULL COMMENT '提交用户',
  `audit_status`  TINYINT      NOT NULL DEFAULT 1 COMMENT '审核结果:1待审 2通过 3拒绝',
  `audit_reason`  VARCHAR(255) NOT NULL DEFAULT '' COMMENT '审核原因',
  `auditor_id`    BIGINT       NOT NULL DEFAULT 0 COMMENT '审核人',
  `audit_time`    DATETIME     NULL COMMENT '审核时间',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_biz` (`biz_type`,`biz_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录表';

DROP TABLE IF EXISTS `report_record`;
CREATE TABLE `report_record` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `reporter_id`   BIGINT       NOT NULL COMMENT '举报人',
  `target_type`   TINYINT      NOT NULL DEFAULT 1 COMMENT '目标类型:1动态 2评论 3用户',
  `target_id`     BIGINT       NOT NULL COMMENT '目标ID',
  `reason_type`   TINYINT      NOT NULL DEFAULT 1 COMMENT '举报原因类型',
  `reason`        VARCHAR(255) NOT NULL DEFAULT '' COMMENT '补充说明',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1待处理 2已处理 3驳回',
  `handle_result` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '处理结果',
  `handler_id`    BIGINT       NOT NULL DEFAULT 0 COMMENT '处理人ID(0=未处理)',
  `handle_time`   DATETIME     NULL COMMENT '处理时间(NULL=未处理)',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`,`target_id`),
  KEY `idx_reporter` (`reporter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报记录表';

DROP TABLE IF EXISTS `risk_user_record`;
CREATE TABLE `risk_user_record` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
  `risk_type`   TINYINT      NOT NULL DEFAULT 1 COMMENT '类型:1禁言 2封禁 3观察',
  `reason`      VARCHAR(255) NOT NULL DEFAULT '' COMMENT '原因',
  `start_time`  DATETIME     NULL COMMENT '开始时间',
  `end_time`    DATETIME     NULL COMMENT '结束时间(NULL=永久)',
  `operator_id` BIGINT       NOT NULL DEFAULT 0 COMMENT '操作人',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0解除 1生效',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_type_status` (`risk_type`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控用户记录表(禁言/封禁/观察)';

-- =====================================================================
-- 十、AI 域 (4 表，本期建表不用)
-- =====================================================================

DROP TABLE IF EXISTS `ai_role`;
CREATE TABLE `ai_role` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`             VARCHAR(32)  NOT NULL DEFAULT '' COMMENT 'AI角色名',
  `avatar`           VARCHAR(255) NOT NULL DEFAULT '' COMMENT '头像URL',
  `welcome_text`     VARCHAR(500) NOT NULL DEFAULT '' COMMENT '欢迎语',
  `prompt`           TEXT         NULL COMMENT '角色Prompt',
  `status`           TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0禁用 1启用',
  `sort`             INT          NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI角色表(本期不用)';

DROP TABLE IF EXISTS `ai_chat_message`;
CREATE TABLE `ai_chat_message` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
  `role_id`     BIGINT      NOT NULL COMMENT 'AI角色ID',
  `sender_type` TINYINT     NOT NULL DEFAULT 1 COMMENT '发送方:1用户 2AI',
  `content`     TEXT        NULL COMMENT '内容',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`   TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_role` (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天消息表(本期不用)';

DROP TABLE IF EXISTS `ai_call_log`;
CREATE TABLE `ai_call_log` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT       NOT NULL DEFAULT 0 COMMENT '用户ID',
  `biz_type`      TINYINT      NOT NULL DEFAULT 1 COMMENT '业务:1聊天 2话术',
  `request_body`  TEXT         NULL COMMENT '请求内容',
  `response_body` TEXT         NULL COMMENT '响应内容',
  `success_flag`  TINYINT      NOT NULL DEFAULT 1 COMMENT '成败:0失败 1成功',
  `cost_ms`       INT          NOT NULL DEFAULT 0 COMMENT '耗时ms',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI调用日志表(本期不用)';

DROP TABLE IF EXISTS `ai_reply_suggestion`;
CREATE TABLE `ai_reply_suggestion` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
  `session_uuid`VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '会话标识',
  `suggestion`  VARCHAR(500) NOT NULL DEFAULT '' COMMENT '建议话术',
  `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态:0未用 1已用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete`   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI回复建议表(本期不用)';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
--  种子数据 (Seed Data)
-- =====================================================================

-- 1) 标签分类 (7)
INSERT INTO `tag_category` (`id`,`name`,`icon`,`sort`,`status`) VALUES
(1,'生活方式','',1,1),
(2,'兴趣爱好','',2,1),
(3,'性格社交','',3,1),
(4,'音乐现场','',4,1),
(5,'美食口味','',5,1),
(6,'运动户外','',6,1),
(7,'生活日常','',7,1);

-- 2) 标签 (71)
INSERT INTO `tag` (`id`,`category_id`,`name`,`hot_flag`,`match_flag`,`recommend_flag`,`sort`,`status`) VALUES
(1,1,'咖啡',1,1,1,1,1),
(2,1,'健身',1,1,1,2,1),
(3,1,'旅行',1,1,1,3,1),
(4,1,'美食',0,1,1,4,1),
(5,1,'摄影',0,1,1,5,1),
(6,1,'宠物',0,1,1,6,1),
(7,1,'熬夜',0,1,0,7,1),
(8,2,'电影',1,1,1,8,1),
(9,2,'音乐',1,1,1,9,1),
(10,2,'读书',0,1,1,10,1),
(11,2,'游戏',1,1,1,11,1),
(12,2,'动漫',0,1,1,12,1),
(13,2,'手作',0,1,0,13,1),
(14,2,'桌游',0,1,1,14,1),
(15,3,'社恐',1,1,1,15,1),
(16,3,'话痨',0,1,1,16,1),
(17,3,'慢热',0,1,1,17,1),
(18,3,'幽默',1,1,1,18,1),
(19,3,'倾听者',0,1,1,19,1),
(20,3,'元气',0,1,1,20,1),
(21,4,'流行',1,1,1,21,1),
(22,4,'摇滚',0,1,1,22,1),
(23,4,'说唱',1,1,1,23,1),
(24,4,'民谣',0,1,1,24,1),
(25,4,'电子',0,1,1,25,1),
(26,4,'爵士',0,1,1,26,1),
(27,4,'古典',0,1,1,27,1),
(28,4,'古风',0,1,1,28,1),
(29,4,'R&B',0,1,1,29,1),
(30,4,'二次元',0,1,1,30,1),
(31,4,'现场演出',1,1,1,31,1),
(32,4,'音乐节',0,1,1,32,1),
(33,4,'乐器',0,1,0,33,1),
(34,5,'火锅',1,1,1,34,1),
(35,5,'烧烤',1,1,1,35,1),
(36,5,'日料',0,1,1,36,1),
(37,5,'西餐',0,1,1,37,1),
(38,5,'川菜',0,1,1,38,1),
(39,5,'粤菜',0,1,1,39,1),
(40,5,'甜品',1,1,1,40,1),
(41,5,'奶茶',0,1,1,41,1),
(42,5,'精酿',0,1,1,42,1),
(43,5,'素食',0,1,1,43,1),
(44,5,'探店',0,1,1,44,1),
(45,5,'深夜食堂',0,1,0,45,1),
(46,6,'跑步',0,1,1,46,1),
(47,6,'游泳',0,1,1,47,1),
(48,6,'篮球',0,1,1,48,1),
(49,6,'足球',0,1,1,49,1),
(50,6,'羽毛球',0,1,1,50,1),
(51,6,'网球',0,1,1,51,1),
(52,6,'瑜伽',0,1,1,52,1),
(53,6,'滑雪',1,1,1,53,1),
(54,6,'爬山',1,1,1,54,1),
(55,6,'骑行',0,1,1,55,1),
(56,6,'潜水',0,1,1,56,1),
(57,6,'攀岩',0,1,1,57,1),
(58,6,'滑板',0,1,1,58,1),
(59,7,'猫奴',1,1,1,59,1),
(60,7,'狗奴',0,1,1,60,1),
(61,7,'云吸猫',0,1,1,61,1),
(62,7,'养花',0,1,1,62,1),
(63,7,'做饭',0,1,1,63,1),
(64,7,'烘焙',0,1,1,64,1),
(65,7,'早睡早起',0,1,1,65,1),
(66,7,'追剧',1,1,1,66,1),
(67,7,'手冲咖啡',0,1,1,67,1),
(68,7,'逛街',0,1,1,68,1),
(69,7,'宅家',1,1,1,69,1),
(70,7,'极简主义',0,1,0,70,1),
(71,7,'断舍离',0,1,0,71,1);

-- 3) 灵魂测试题目 (8 题)
INSERT INTO `soul_test_question` (`id`,`title`,`description`,`sort`,`status`) VALUES
(1,'周末的下午，你更想做？','选择最接近你状态的一项',1,1),
(2,'面对一群陌生人，你通常？','选择最接近你状态的一项',2,1),
(3,'当朋友向你倾诉烦恼，你会？','选择最接近你状态的一项',3,1),
(4,'你更喜欢哪种社交场合？','选择最接近你状态的一项',4,1),
(5,'遇到分歧时，你的第一反应？','选择最接近你状态的一项',5,1),
(6,'独处时你最常做的事？','选择最接近你状态的一项',6,1),
(7,'你希望认识怎样的朋友？','选择最接近你状态的一项',7,1),
(8,'别人眼中的你更像？','选择最接近你状态的一项',8,1);

-- 4) 灵魂测试选项 (每题 3-4 选项，result_code 对应 4 种结果)
INSERT INTO `soul_test_option` (`question_id`,`content`,`result_code`,`score`,`sort`) VALUES
-- Q1
(1,'约朋友出门浪','E',3,1),(1,'宅家看剧充电','I',3,2),(1,'看心情随机','M',2,3),
-- Q2
(2,'主动破冰聊天','E',3,1),(2,'安静观察等待','I',3,2),(2,'别人先开口','M',2,3),
-- Q3
(3,'认真倾听给建议','S',3,1),(3,'陪着一起吐槽','E',3,2),(3,'讲个笑话哄开心','M',2,3),
-- Q4
(4,'热闹的派对','E',3,1),(4,'三两人的小聚','S',3,2),(4,'线上聊更自在','I',3,3),
-- Q5
(5,'直接表达立场','E',3,1),(5,'先理解对方','S',3,2),(5,'回避冷处理','I',3,3),
-- Q6
(6,'追剧打游戏','I',3,1),(6,'看书或听歌','S',3,2),(6,'约人出去逛','E',3,3),
-- Q7
(7,'有趣能玩到一起','E',3,1),(7,'温柔能聊心事','S',3,2),(7,'有共同爱好','M',2,3),
-- Q8
(8,'活泼的开心果','E',3,1),(8,'温暖的倾听者','S',3,2),(8,'安静的文艺青年','I',3,3),(8,'靠谱的实干家','M',3,4);

-- 5) 灵魂测试结果 (4 种)
INSERT INTO `soul_test_result` (`result_code`,`name`,`icon`,`description`,`social_advice`) VALUES
('E','热烈发光体','','你热情外向，是天生的气氛制造者，喜欢热闹与被需要的感觉。','适合加入兴趣社群，主动发起话题；注意给慢热的朋友留空间。'),
('I','安静星球居民','','你内敛温柔，享受独处，喜欢在安静中积蓄能量，对少数人敞开心扉。','适合一对一深聊；从共同爱好切入会更容易打开话匣子。'),
('S','温柔倾听者','','你善解人意，是最好的树洞，总能让身边的人感到被理解。','适合承担陪伴角色；记得也要表达自己的需求。'),
('M','百变共生体','','你随和灵活，能融入不同圈子，是团队里的黏合剂。','适合做连接者；明确自己的边界会让你更轻松。');

-- 6) 话题 (4)
INSERT INTO `dynamic_topic` (`id`,`name`,`description`,`cover`,`hot_value`,`status`,`sort`) VALUES
(1,'深夜emo','今天也有好好活着', '',520,1,1),
(2,'周末去哪儿','分享你的周末计划','',380,1,2),
(3,'今天吃什么','干饭人的快乐','',450,1,3),
(4,'我的小众爱好','安利你的宝藏兴趣','',210,1,4);

-- 7) AI 角色 (1)
INSERT INTO `ai_role` (`id`,`name`,`avatar`,`welcome_text`,`prompt`,`status`,`sort`) VALUES
(1,'小灵','','你好呀，我是小灵，很高兴遇见你～','你是温柔体贴的AI伙伴小灵，善解人意，会认真倾听用户的心事。',1,1);

-- 8) 系统配置 (1)
INSERT INTO `system_config` (`config_key`,`config_value`,`description`,`status`) VALUES
('sms_enable','0','短信通道开关:0开发模式固定验证码 1启用真实短信',1);

-- 9) 敏感词种子
INSERT INTO `sensitive_word` (`word`,`category`,`level`,`status`) VALUES
('广告','marketing',2,1),
('代刷','marketing',2,1),
('加微信','marketing',1,1),
('傻逼','abuse',2,1),
('滚','abuse',1,1),
('诈骗','illegal',2,1),
('涉黄','illegal',2,1),
('赌博','illegal',2,1);

-- 10) 默认管理员账号 (admin / 123456)
--    说明: 后台账号用 phone 字段存账号名(唯一)。openid/unionid 显式置 NULL 避免唯一索引冲突。
--    明文 123456 的 BCrypt(已用 bcrypt.checkpw 实测校验通过):
--    $2a$10$M7ylmEgCCQ.JQaQBYXe66.CXc8Dv5ShxxEXkF7x/g2nf2j/Dc9pVK
INSERT INTO `user` (`id`,`user_no`,`status`,`profile_complete_flag`,`tag_flag`,`test_flag`) VALUES
(1,'ADMIN0001',1,1,1,1);
INSERT INTO `user_sensitive_info` (`user_id`,`phone`,`openid`,`unionid`,`password`) VALUES
(1,'admin',NULL,NULL,'$2a$10$M7ylmEgCCQ.JQaQBYXe66.CXc8Dv5ShxxEXkF7x/g2nf2j/Dc9pVK');
INSERT INTO `user_profile` (`user_id`,`nickname`,`gender`,`city`,`complete_percent`) VALUES
(1,'系统管理员',0,'',100);
INSERT INTO `user_privacy_setting` (`user_id`) VALUES (1);

-- =====================================================================
-- DDL 结束 — 共 36 张表
-- 用户域6 + 标签域3 + 测试域4 + 动态域7 + 匹配域3 + IM域2
-- + 通知域2 + 配置域1 + 治理域4 + AI域4 = 36
-- =====================================================================
