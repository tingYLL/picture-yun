package com.jdjm.jdjmpicturebackend.model.dto.comment;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论查询请求
 */
@Data
public class CommentQueryRequest implements Serializable {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 图片ID
     */
    private Long pictureId;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 父评论ID
     */
    private Long parentId;

    /**
     * 根评论ID
     */
    private Long rootId;

    /**
     * 评论状态
     */
    private Integer status;

    /**
     * 空间ID
     */
    private Long spaceId;

    /**
     * 最小创建时间
     */
    private Date minCreateTime;

    /**
     * 最大创建时间
     */
    private Date maxCreateTime;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序
     */
    private String sortOrder;

    /**
     * 当前页码
     */
    private Integer current;

    /**
     * 每页显示数量
     */
    private Integer pageSize;

    private static final long serialVersionUID = 1L;
}