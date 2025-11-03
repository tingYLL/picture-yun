package com.jdjm.jdjmpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论通知
 * @TableName comment_notification
 */
@TableName(value = "comment_notification")
@Data
public class CommentNotification implements Serializable {
    /**
     * 通知主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接收通知的用户ID
     */
    private Long userId;

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 相关图片ID
     */
    private Long pictureId;

    /**
     * 通知类型：0-新评论, 1-回复评论, 2-评论审核通过, 3-评论审核拒绝
     */
    private Integer notificationType;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 触发通知的用户ID（谁评论/回复的）
     */
    private Long triggerUserId;

    /**
     * 是否已读：0-未读, 1-已读
     */
    private Integer isRead;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}