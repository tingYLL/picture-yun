package com.jdjm.jdjmpicturebackend.model.dto.comment;

import lombok.Data;

import java.io.Serializable;

/**
 * 评论添加请求
 */
@Data
public class CommentAddRequest implements Serializable {

    /**
     * 图片ID
     */
    private Long pictureId;

    /**
     * 父评论ID（回复某条评论时使用）
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 空间ID（私有空间图片时使用）
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}