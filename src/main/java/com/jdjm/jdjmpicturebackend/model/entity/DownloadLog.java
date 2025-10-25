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
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}