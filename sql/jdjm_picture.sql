/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 80027 (8.0.27)
 Source Host           : localhost:3306
 Source Schema         : bear_picture_test

 Target Server Type    : MySQL
 Target Server Version : 80027 (8.0.27)
 File Encoding         : 65001

 Date: 08/11/2025 12:25:45
*/

-- ----------------------------
-- 创建数据库
-- ----------------------------
CREATE DATABASE IF NOT EXISTS `jdjm_picture` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 使用数据库
USE `jdjm_picture`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
                             `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
                             `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
                             `parentId` bigint UNSIGNED NULL DEFAULT 0 COMMENT '父分类 ID（0-表示顶层分类）',
                             `useNum` int NOT NULL DEFAULT 0 COMMENT '使用数量',
                             `userId` bigint NOT NULL COMMENT '创建用户 ID',
                             `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常, 1-删除）',
                             `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
                             `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (`id`) USING BTREE,
                             INDEX `idx_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1985725241489367043 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论主键ID',
                            `pictureId` bigint NOT NULL COMMENT '图片ID',
                            `userId` bigint NOT NULL COMMENT '评论用户ID',
                            `parentId` bigint NULL DEFAULT NULL COMMENT '父评论ID（null表示一级评论）',
                            `rootId` bigint NULL DEFAULT NULL COMMENT '根评论ID（用于嵌套回复）',
                            `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
                            `status` tinyint NOT NULL DEFAULT 0 COMMENT '评论状态：0-正常, 1-已删除, 2-违规被屏蔽',
                            `likeCount` int NOT NULL DEFAULT 0 COMMENT '点赞数',
                            `replyCount` int NOT NULL DEFAULT 0 COMMENT '回复数',
                            `spaceId` bigint NULL DEFAULT NULL COMMENT '空间ID（为null表示公共图库）',
                            `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
                            `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常, 1-删除）',
                            PRIMARY KEY (`id`) USING BTREE,
                            INDEX `idx_pictureId`(`pictureId` ASC) USING BTREE,
                            INDEX `idx_userId`(`userId` ASC) USING BTREE,
                            INDEX `idx_parentId`(`parentId` ASC) USING BTREE,
                            INDEX `idx_rootId`(`rootId` ASC) USING BTREE,
                            INDEX `idx_spaceId`(`spaceId` ASC) USING BTREE,
                            INDEX `idx_createTime`(`createTime` ASC) USING BTREE,
                            INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1986792240701280259 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for comment_notification
-- ----------------------------
DROP TABLE IF EXISTS `comment_notification`;
CREATE TABLE `comment_notification`  (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知主键ID',
                                         `userId` bigint NOT NULL COMMENT '接收通知的用户ID',
                                         `commentId` bigint NOT NULL COMMENT '评论ID',
                                         `pictureId` bigint NULL DEFAULT NULL COMMENT '相关图片ID',
                                         `notificationType` tinyint NOT NULL COMMENT '通知类型：0-新评论, 1-回复评论, 2-评论审核通过, 3-评论审核拒绝',
                                         `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
                                         `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '通知内容',
                                         `triggerUserId` bigint NULL DEFAULT NULL COMMENT '触发通知的用户ID（谁评论/回复的）',
                                         `isRead` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读, 1-已读',
                                         `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常, 1-删除）',
                                         `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         PRIMARY KEY (`id`) USING BTREE,
                                         INDEX `idx_userId`(`userId` ASC) USING BTREE,
                                         INDEX `idx_commentId`(`commentId` ASC) USING BTREE,
                                         INDEX `idx_pictureId`(`pictureId` ASC) USING BTREE,
                                         INDEX `idx_notificationType`(`notificationType` ASC) USING BTREE,
                                         INDEX `idx_isRead`(`isRead` ASC) USING BTREE,
                                         INDEX `idx_createTime`(`createTime` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1986792240701280260 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for download_logs
-- ----------------------------
DROP TABLE IF EXISTS `download_logs`;
CREATE TABLE `download_logs`  (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `user_id` bigint NOT NULL,
                                  `file_id` bigint NOT NULL,
                                  `downloaded_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                  `space_id` bigint NULL DEFAULT NULL COMMENT '空间 id（为 null 表示公共图库）',
                                  `consume_quota` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否消耗下载配额：1-消耗，0-不消耗',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  INDEX `idx_user_consume_quota_time`(`user_id` ASC, `consume_quota` ASC, `downloaded_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 63 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for picture
-- ----------------------------
DROP TABLE IF EXISTS `picture`;
CREATE TABLE `picture`  (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                            `url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片 url',
                            `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片名称',
                            `introduction` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '简介',
                            `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类',
                            `tags` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签（JSON 数组）',
                            `picSize` bigint NULL DEFAULT NULL COMMENT '图片体积',
                            `picWidth` int NULL DEFAULT NULL COMMENT '图片宽度',
                            `picHeight` int NULL DEFAULT NULL COMMENT '图片高度',
                            `picScale` double NULL DEFAULT NULL COMMENT '图片宽高比例',
                            `picFormat` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片格式',
                            `userId` bigint NOT NULL COMMENT '创建用户 id',
                            `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
                            `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
                            `reviewStatus` int NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
                            `reviewMessage` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核信息',
                            `reviewerId` bigint NULL DEFAULT NULL COMMENT '审核人 ID',
                            `reviewTime` datetime NULL DEFAULT NULL COMMENT '审核时间',
                            `thumbnailUrl` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '缩略图 url',
                            `spaceId` bigint NULL DEFAULT NULL COMMENT '空间 id（为空表示公共空间）',
                            `picColor` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片主色调',
                            `categoryId` bigint NULL DEFAULT NULL COMMENT '分类 ID',
                            `resourceStatus` tinyint NOT NULL DEFAULT 0 COMMENT '资源状态（0-存在 COS, 1-不存在 COS）',
                            `viewQuantity` int NOT NULL DEFAULT 0 COMMENT '查看数量',
                            `likeQuantity` int NOT NULL DEFAULT 0 COMMENT '点赞数量',
                            `collectQuantity` int NOT NULL DEFAULT 0 COMMENT '收藏数量',
                            `downloadQuantity` int NOT NULL DEFAULT 0 COMMENT '下载数量',
                            `shareQuantity` int NOT NULL DEFAULT 0 COMMENT '分享数量',
                            `isShare` tinyint NOT NULL DEFAULT 0 COMMENT '是否分享（0-分享, 1-不分享）',
                            `expandStatus` tinyint NOT NULL DEFAULT 0 COMMENT '扩图状态（0-普通图片, 1-扩图图片, 2-扩图成功后的图片）',
                            `recommendScore` decimal(10, 4) NOT NULL DEFAULT 0.0000 COMMENT '推荐综合得分',
                            PRIMARY KEY (`id`) USING BTREE,
                            INDEX `idx_name`(`name` ASC) USING BTREE,
                            INDEX `idx_introduction`(`introduction` ASC) USING BTREE,
                            INDEX `idx_category`(`category` ASC) USING BTREE,
                            INDEX `idx_tags`(`tags` ASC) USING BTREE,
                            INDEX `idx_userId`(`userId` ASC) USING BTREE,
                            INDEX `idx_reviewStatus`(`reviewStatus` ASC) USING BTREE,
                            INDEX `idx_spaceId`(`spaceId` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1986817430344740867 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '图片' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for picture_interaction
-- ----------------------------
DROP TABLE IF EXISTS `picture_interaction`;
CREATE TABLE `picture_interaction`  (
                                        `userId` bigint NOT NULL COMMENT '用户 ID',
                                        `pictureId` bigint NOT NULL COMMENT '图片 ID',
                                        `interactionType` tinyint NOT NULL COMMENT '交互类型（0-点赞, 1-收藏）',
                                        `interactionStatus` tinyint NOT NULL COMMENT '交互状态（0-存在, 1-取消）',
                                        `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常, 1-删除）',
                                        `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
                                        `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                        PRIMARY KEY (`userId`, `pictureId`, `interactionType`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '图片交互表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for scheduled_task
-- ----------------------------
DROP TABLE IF EXISTS `scheduled_task`;
CREATE TABLE `scheduled_task`  (
                                   `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `taskKey` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务 KEY（存在内存中）',
                                   `taskName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
                                   `taskCron` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务 corn 表达式',
                                   `taskDesc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务描述',
                                   `taskBean` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务 Bean 名称（执行任务的 bean）',
                                   `taskStatus` tinyint NOT NULL DEFAULT 0 COMMENT '任务状态（0-关闭, 1-开启）',
                                   `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常, 1-删除）',
                                   `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
                                   `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '定时任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for space
-- ----------------------------
DROP TABLE IF EXISTS `space`;
CREATE TABLE `space`  (
                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                          `spaceName` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '空间名称',
                          `spaceLevel` int NULL DEFAULT 0 COMMENT '空间级别：0-普通版 1-专业版 2-旗舰版',
                          `maxSize` bigint NULL DEFAULT 0 COMMENT '空间图片的最大总大小',
                          `maxCount` bigint NULL DEFAULT 0 COMMENT '空间图片的最大数量',
                          `totalSize` bigint NULL DEFAULT 0 COMMENT '当前空间下图片的总大小',
                          `totalCount` bigint NULL DEFAULT 0 COMMENT '当前空间下的图片数量',
                          `userId` bigint NOT NULL COMMENT '创建用户 id',
                          `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
                          `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
                          `spaceType` int NOT NULL DEFAULT 0 COMMENT '空间类型：0-私有 1-团队',
                          PRIMARY KEY (`id`) USING BTREE,
                          INDEX `idx_userId`(`userId` ASC) USING BTREE,
                          INDEX `idx_spaceName`(`spaceName` ASC) USING BTREE,
                          INDEX `idx_spaceLevel`(`spaceLevel` ASC) USING BTREE,
                          INDEX `idx_spaceType`(`spaceType` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1986435203723190275 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '空间' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for space_user
-- ----------------------------
DROP TABLE IF EXISTS `space_user`;
CREATE TABLE `space_user`  (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                               `spaceId` bigint NOT NULL COMMENT '空间 id',
                               `userId` bigint NOT NULL COMMENT '用户 id',
                               `spaceRole` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'viewer' COMMENT '空间角色：viewer/editor/admin',
                               `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`) USING BTREE,
                               UNIQUE INDEX `uk_spaceId_userId`(`spaceId` ASC, `userId` ASC) USING BTREE,
                               INDEX `idx_spaceId`(`spaceId` ASC) USING BTREE,
                               INDEX `idx_userId`(`userId` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '空间用户关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                         `userAccount` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
                         `userPassword` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
                         `userName` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户昵称',
                         `userAvatar` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像',
                         `userProfile` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户简介',
                         `userRole` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin',
                         `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
                         `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
                         `userEmail` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                         `userPhone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户手机号',
                         `birthday` date NULL DEFAULT NULL COMMENT '出生日期',
                         `vipNnumber` bigint NULL DEFAULT NULL COMMENT '会员编号',
                         `vipExpireTime` datetime NULL DEFAULT NULL COMMENT '会员过期时间',
                         `vipCode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '会员兑换码',
                         `vipSign` tinyint(1) NULL DEFAULT 0 COMMENT '会员标识：0-非VIP, 1-VIP',
                         `shareCode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分享码',
                         `inviteUserId` bigint NULL DEFAULT NULL COMMENT '邀请用户 ID',
                         `isDisabled` tinyint NOT NULL DEFAULT 0 COMMENT '是否禁用（0-正常, 1-禁用）',
                         `balance` tinyint NOT NULL DEFAULT 0 COMMENT '余额',
                         PRIMARY KEY (`id`) USING BTREE,
                         UNIQUE INDEX `uk_userAccount`(`userAccount` ASC) USING BTREE,
                         UNIQUE INDEX `uk_userPhone`(`userPhone` ASC) USING BTREE,
                         INDEX `idx_userName`(`userName` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1986058607090847747 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for vip_memberships
-- ----------------------------
DROP TABLE IF EXISTS `vip_memberships`;
CREATE TABLE `vip_memberships`  (
                                    `id` bigint NOT NULL AUTO_INCREMENT,
                                    `user_id` bigint NOT NULL,
                                    `is_vip` tinyint(1) NULL DEFAULT 0,
                                    `vip_start_date` timestamp NULL DEFAULT NULL,
                                    `vip_end_date` timestamp NULL DEFAULT NULL,
                                    `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                    `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for vip_redemption_codes
-- ----------------------------
DROP TABLE IF EXISTS `vip_redemption_codes`;
CREATE TABLE `vip_redemption_codes`  (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                         `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '兑换码',
                                         `is_used` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已使用',
                                         `user_id` bigint NULL DEFAULT NULL COMMENT '使用兑换码的用户ID',
                                         `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         `used_at` datetime NULL DEFAULT NULL COMMENT '使用时间',
                                         PRIMARY KEY (`id`) USING BTREE,
                                         UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
                                         INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'VIP兑换码' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
