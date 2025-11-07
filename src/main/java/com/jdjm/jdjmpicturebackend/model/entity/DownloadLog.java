package com.jdjm.jdjmpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("download_logs")
public class DownloadLog implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("file_id")
    private Long fileId;

    @TableField("space_id")
    private Long spaceId;

    @TableField("downloaded_at")
    private LocalDateTime downloadedAt;

    /**
     * 是否消耗下载配额：1-消耗，0-不消耗
     * 不消耗的情况包括：重复下载、下载自己发布的图片
     */
    @TableField("consume_quota")
    private Integer consumeQuota;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}