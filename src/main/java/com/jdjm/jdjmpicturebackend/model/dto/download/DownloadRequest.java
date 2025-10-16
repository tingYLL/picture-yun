package com.jdjm.jdjmpicturebackend.model.dto.download;

import com.jdjm.jdjmpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 下载查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DownloadRequest extends PageRequest implements Serializable {

    /**
     * 图片 ID
     */
    private Long id;

    /**
     * 交互类型
     */
    private Integer interactionType;

    /**
     * 交互状态（0-存在, 1-取消）
     */
    private Integer interactionStatus;

    /**
     * 查询开始时间（用于下载历史记录查询）
     */
    private LocalDateTime startTime;

    /**
     * 查询结束时间（用于下载历史记录查询）
     */
    private LocalDateTime endTime;

    private static final long serialVersionUID = 1L;
}
