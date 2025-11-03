package com.jdjm.jdjmpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jdjm.jdjmpicturebackend.model.entity.CommentNotification;
import com.jdjm.jdjmpicturebackend.model.vo.CommentNotificationVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author jdjm
* @description 针对表【comment_notification(评论通知)】的数据库操作Service
* @createDate 2025-10-31
*/
public interface CommentNotificationService extends IService<CommentNotification> {

    /**
     * 创建评论通知
     *
     * @param userId 接收通知的用户ID
     * @param commentId 评论ID
     * @param pictureId 图片ID
     * @param notificationType 通知类型
     * @param title 通知标题
     * @param content 通知内容
     * @param triggerUserId 触发通知的用户ID
     * @return 是否创建成功
     */
    boolean createNotification(Long userId, Long commentId, Long pictureId,
                              Integer notificationType, String title,
                              String content, Long triggerUserId);

    /**
     * 获取用户通知列表
     *
     * @param userId 用户ID
     * @param isRead 是否已读（可选）
     * @param current 当前页码
     * @param pageSize 每页大小
     * @return 通知分页列表
     */
    Page<CommentNotificationVO> getUserNotifications(Long userId, Integer isRead,
                                                    Integer current, Integer pageSize);

    /**
     * 标记通知为已读
     *
     * @param id 通知ID
     * @param userId 用户ID
     * @return 是否标记成功
     */
    boolean markAsRead(Long id, Long userId);

    /**
     * 批量标记通知为已读
     *
     * @param ids 通知ID列表
     * @param userId 用户ID
     * @return 是否标记成功
     */
    boolean batchMarkAsRead(List<Long> ids, Long userId);

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 删除通知
     *
     * @param id 通知ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteNotification(Long id, Long userId);

    /**
     * 获取通知VO
     *
     * @param notification 通知实体
     * @return 通知VO
     */
    CommentNotificationVO getNotificationVO(CommentNotification notification);

    /**
     * 获取通知VO列表
     *
     * @param notificationList 通知实体列表
     * @return 通知VO列表
     */
    List<CommentNotificationVO> getNotificationVOList(List<CommentNotification> notificationList);

    /**
     * 获取查询条件
     *
     * @param userId 用户ID
     * @param isRead 是否已读
     * @return 查询包装器
     */
    QueryWrapper<CommentNotification> getQueryWrapper(Long userId, Integer isRead);
}