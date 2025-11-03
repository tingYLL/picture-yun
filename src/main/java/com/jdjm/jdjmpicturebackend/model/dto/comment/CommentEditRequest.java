package com.jdjm.jdjmpicturebackend.model.dto.comment;

import lombok.Data;

import java.io.Serializable;

/**
 * 评论编辑请求
 */
@Data
public class CommentEditRequest implements Serializable {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    private static final long serialVersionUID = 1L;
}