package com.jdjm.jdjmpicturebackend.model.vo;

import com.jdjm.jdjmpicturebackend.model.entity.DownloadLog;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 用户下载历史记录VO
 */
@Data
public class DownloadHistoryVO implements Serializable {

    /**
     * 下载记录ID
     */
    private Long id;

    /**
     * 图片信息
     */
    private PictureVO picture;

    /**
     * 下载时间
     */
    private LocalDateTime downloadedAt;

    private static final long serialVersionUID = 1L;
}