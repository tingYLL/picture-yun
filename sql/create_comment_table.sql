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

 Date: 31/10/2025 11:00:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

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
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '通知内容',
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论通知表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 添加外键约束（可选，根据实际需要）
-- ----------------------------
-- ALTER TABLE `comment` ADD CONSTRAINT `fk_comment_picture` FOREIGN KEY (`pictureId`) REFERENCES `picture` (`id`) ON DELETE CASCADE;
-- ALTER TABLE `comment` ADD CONSTRAINT `fk_comment_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE CASCADE;
-- ALTER TABLE `comment_notification` ADD CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
-- ALTER TABLE `comment_notification` ADD CONSTRAINT `fk_notification_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE;
-- ALTER TABLE `comment_notification` ADD CONSTRAINT `fk_notification_picture` FOREIGN KEY (`picture_id`) REFERENCES `picture` (`id`) ON DELETE CASCADE;

-- ----------------------------
-- 插入示例数据（可选）
-- ----------------------------
-- INSERT INTO `comment` (`pictureId`, `userId`, `content`, `status`, `spaceId`)
-- VALUES (1, 1, '这是一张很棒的图片！', 0, NULL);
-- INSERT INTO `comment` (`pictureId`, `userId`, `parentId`, `rootId`, `content`, `status`, `spaceId`)
-- VALUES (1, 2, 1, 1, '谢谢你的赞美！', 0, NULL);