package com.jdjm.jdjmpicturebackend.model.dto.comment;

import lombok.Data;

import java.io.Serializable;

/**
 * 评论审核请求
 */
@Data
public class CommentReviewRequest implements Serializable {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}