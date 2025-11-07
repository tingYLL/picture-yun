-- 为 download_logs 表添加 consume_quota 字段
-- 用于标识该下载记录是否消耗用户的下载配额
-- 1: 消耗配额（默认），0: 不消耗配额（重复下载或自己发布的图片）

ALTER TABLE download_logs
ADD COLUMN consume_quota TINYINT(1) NOT NULL DEFAULT 1
COMMENT '是否消耗下载配额：1-消耗，0-不消耗';

-- 为该字段添加索引，优化查询性能
CREATE INDEX idx_user_consume_quota_time ON download_logs(user_id, consume_quota, downloaded_at);