package com.jdjm.jdjmpicturebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jdjm.jdjmpicturebackend.exception.BusinessException;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.mapper.CommentMapper;
import com.jdjm.jdjmpicturebackend.mapper.CommentNotificationMapper;
import com.jdjm.jdjmpicturebackend.model.entity.Comment;
import com.jdjm.jdjmpicturebackend.model.entity.CommentNotification;
import com.jdjm.jdjmpicturebackend.model.entity.User;
import com.jdjm.jdjmpicturebackend.model.enums.NotificationTypeEnum;
import com.jdjm.jdjmpicturebackend.model.vo.CommentNotificationVO;
import com.jdjm.jdjmpicturebackend.model.vo.UserVO;
import com.jdjm.jdjmpicturebackend.service.CommentNotificationService;
import com.jdjm.jdjmpicturebackend.service.CommentService;
import com.jdjm.jdjmpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author jdjm
* @description 针对表【comment_notification(评论通知)】的数据库操作Service实现
* @createDate 2025-10-31
*/
@Service
@Slf4j
public class CommentNotificationServiceImpl extends ServiceImpl<CommentNotificationMapper, CommentNotification>
    implements CommentNotificationService {

    @Resource
    private CommentNotificationMapper commentNotificationMapper;

    @Resource
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Override
    public boolean createNotification(Long userId, Long commentId, Long pictureId,
                                      Integer notificationType, String title,
                                      String content, Long triggerUserId) {
        if (userId == null || commentId == null || notificationType == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 检查用户是否存在
        User user = userService.getById(userId);
        if (user == null || user.getIsDelete() == 1) {
            log.warn("通知接收用户不存在或已删除: userId={}", userId);
            return false;
        }

        // 避免自己给自己发通知
        if (userId.equals(triggerUserId)) {
            return false;
        }

        CommentNotification notification = new CommentNotification();
        notification.setUserId(userId);
        notification.setCommentId(commentId);
        notification.setPictureId(pictureId);
        notification.setNotificationType(notificationType);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTriggerUserId(triggerUserId);
        notification.setIsRead(0);

        boolean result = this.save(notification);

        // 通过SSE推送实时通知
        if (result) {
            try {
                String notificationData = String.format(
                    "{\"id\":%d,\"type\":%d,\"title\":\"%s\",\"content\":\"%s\",\"pictureId\":%d}",
                    notification.getId(),
                    notificationType,
                    title,
                    content.replace("\"", "\\\""),
                    pictureId != null ? pictureId : 0
                );
                com.jdjm.jdjmpicturebackend.controller.CommentSseController.pushNotificationToUser(
                    userId, "newNotification", notificationData);

                // 更新未读数量
                Long unreadCount = commentNotificationMapper.getUnreadCount(userId);
                com.jdjm.jdjmpicturebackend.controller.CommentSseController.pushNotificationToUser(
                    userId, "unreadCount", String.valueOf(unreadCount));
            } catch (Exception e) {
                log.error("SSE推送通知失败", e);
            }
        }

        return result;
    }

    @Override
    public Page<CommentNotificationVO> getUserNotifications(Long userId, Integer isRead,
                                                            Integer current, Integer pageSize) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 参数设置
        current = current == null || current <= 0 ? 1 : current;
        pageSize = pageSize == null || pageSize <= 0 ? 10 : pageSize;

        // 构建查询条件
        QueryWrapper<CommentNotification> queryWrapper = getQueryWrapper(userId, isRead);

        // 分页查询
        Page<CommentNotification> notificationPage = this.page(new Page<>(current, pageSize), queryWrapper);

        // 转换为VO
        Page<CommentNotificationVO> notificationVOPage = new Page<>(notificationPage.getCurrent(), notificationPage.getSize(), notificationPage.getTotal());
        List<CommentNotificationVO> notificationVOList = getNotificationVOList(notificationPage.getRecords());
        notificationVOPage.setRecords(notificationVOList);

        return notificationVOPage;
    }

    @Override
    public boolean markAsRead(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean result = commentNotificationMapper.markAsRead(id, userId) > 0;

        // 通过SSE推送未读数量更新
        if (result) {
            try {
                Long unreadCount = commentNotificationMapper.getUnreadCount(userId);
                com.jdjm.jdjmpicturebackend.controller.CommentSseController.pushNotificationToUser(
                    userId, "unreadCount", String.valueOf(unreadCount));
            } catch (Exception e) {
                log.error("SSE推送未读数量更新失败", e);
            }
        }

        return result;
    }

    @Override
    public boolean batchMarkAsRead(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty() || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean result = commentNotificationMapper.batchMarkAsRead(ids, userId) > 0;

        // 通过SSE推送未读数量更新
        if (result) {
            try {
                Long unreadCount = commentNotificationMapper.getUnreadCount(userId);
                com.jdjm.jdjmpicturebackend.controller.CommentSseController.pushNotificationToUser(
                    userId, "unreadCount", String.valueOf(unreadCount));
            } catch (Exception e) {
                log.error("SSE推送未读数量更新失败", e);
            }
        }

        return result;
    }

    @Override
    public Long getUnreadCount(Long userId) {
        if (userId == null) {
            return 0L;
        }

        return commentNotificationMapper.getUnreadCount(userId);
    }

    @Override
    public boolean deleteNotification(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 构建删除条件
        QueryWrapper<CommentNotification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.eq("userId", userId);

        return this.remove(queryWrapper);
    }

    @Override
    public CommentNotificationVO getNotificationVO(CommentNotification notification) {
        if (notification == null) {
            return null;
        }

        CommentNotificationVO notificationVO = CommentNotificationVO.objToVo(notification);

        // 设置通知类型描述
        NotificationTypeEnum typeEnum = NotificationTypeEnum.getByCode(notification.getNotificationType());
        if (typeEnum != null) {
            notificationVO.setNotificationTypeDesc(typeEnum.getDesc());
        }

        // 获取触发用户信息
        if (notification.getTriggerUserId() != null) {
            User triggerUser = userService.getById(notification.getTriggerUserId());
            if (triggerUser != null) {
                notificationVO.setTriggerUser(userService.getUserVO(triggerUser));
            }
        }

        return notificationVO;
    }

    @Override
    public List<CommentNotificationVO> getNotificationVOList(List<CommentNotification> notificationList) {
        if (notificationList == null || notificationList.isEmpty()) {
            return new ArrayList<>();
        }

        return notificationList.stream()
                .map(this::getNotificationVO)
                .collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<CommentNotification> getQueryWrapper(Long userId, Integer isRead) {
        QueryWrapper<CommentNotification> queryWrapper = new QueryWrapper<>();

        if (userId != null) {
            queryWrapper.eq("userId", userId);
        }

        if (isRead != null) {
            queryWrapper.eq("isRead", isRead);
        }

        queryWrapper.orderByDesc("createTime");

        return queryWrapper;
    }
}