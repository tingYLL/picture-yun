package com.jdjm.jdjmpicturebackend.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量删除请求类
 */
@Data
public class DeleteBatchRequest implements Serializable {

    /**
     * 图片ID列表
     */
    private List<Long> ids;

    private static final long serialVersionUID = 1L;
}