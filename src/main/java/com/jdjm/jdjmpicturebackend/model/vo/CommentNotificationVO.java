package com.jdjm.jdjmpicturebackend.model.vo;

import com.jdjm.jdjmpicturebackend.model.entity.CommentNotification;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论通知视图对象
 */
@Data
public class CommentNotificationVO implements Serializable {

    /**
     * 通知ID
     */
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
     * 通知类型
     */
    private Integer notificationType;

    /**
     * 通知类型描述
     */
    private String notificationTypeDesc;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 触发通知的用户ID
     */
    private Long triggerUserId;

    /**
     * 触发用户信息
     */
    private UserVO triggerUser;

    /**
     * 是否已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;

    /**
     * 对象转封装类
     */
    public static CommentNotificationVO objToVo(CommentNotification notification) {
        if (notification == null) {
            return null;
        }
        CommentNotificationVO notificationVO = new CommentNotificationVO();
        BeanUtils.copyProperties(notification, notificationVO);
        return notificationVO;
    }
}