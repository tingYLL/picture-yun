package com.jdjm.jdjmpicturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jdjm.jdjmpicturebackend.annotation.AuthCheck;
import com.jdjm.jdjmpicturebackend.common.BaseResponse;
import com.jdjm.jdjmpicturebackend.common.DeleteRequest;
import com.jdjm.jdjmpicturebackend.common.ResultUtils;
import com.jdjm.jdjmpicturebackend.exception.BusinessException;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.model.entity.User;
import com.jdjm.jdjmpicturebackend.model.vo.CommentNotificationVO;
import com.jdjm.jdjmpicturebackend.service.CommentNotificationService;
import com.jdjm.jdjmpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 评论通知接口
 */
@RestController
@RequestMapping("/comment/notification")
@Slf4j
public class CommentNotificationController {

    @Resource
    private CommentNotificationService commentNotificationService;

    @Resource
    private UserService userService;

    /**
     * 获取用户通知列表（分页）
     *
     * @param isRead 是否已读（可选）
     * @param current 当前页码
     * @param pageSize 每页大小
     * @param request HTTP请求
     * @return 通知列表
     */
    @GetMapping("/list")
    public BaseResponse<Page<CommentNotificationVO>> getUserNotifications(
            @RequestParam(value = "isRead", required = false) Integer isRead,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        // 登录校验
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 分页参数校验
        if (current == null || current <= 0) {
            current = 1;
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }

        Page<CommentNotificationVO> notificationPage = commentNotificationService.getUserNotifications(
                loginUser.getId(), isRead, current, pageSize);
        return ResultUtils.success(notificationPage);
    }

    /**
     * 获取用户未读通知数量
     *
     * @param request HTTP请求
     * @return 未读通知数量
     */
    @GetMapping("/unread/count")
    public BaseResponse<Long> getUnreadCount(HttpServletRequest request) {
        // 登录校验
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Long unreadCount = commentNotificationService.getUnreadCount(loginUser.getId());
        return ResultUtils.success(unreadCount);
    }

    /**
     * 标记通知为已读
     *
     * @param id 通知ID
     * @param request HTTP请求
     * @return 是否标记成功
     */
    @PostMapping("/read")
    public BaseResponse<Boolean> markAsRead(@RequestParam("id") Long id,
                                           HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 登录校验
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        boolean result = commentNotificationService.markAsRead(id, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 批量标记通知为已读
     *
     * @param ids 通知ID列表
     * @param request HTTP请求
     * @return 是否标记成功
     */
    @PostMapping("/read/batch")
    public BaseResponse<Boolean> batchMarkAsRead(@RequestBody List<Long> ids,
                                                HttpServletRequest request) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "通知ID列表不能为空");
        }

        // 登录校验
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        boolean result = commentNotificationService.batchMarkAsRead(ids, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 标记所有通知为已读
     *
     * @param request HTTP请求
     * @return 是否标记成功
     */
    @PostMapping("/read/all")
    public BaseResponse<Boolean> markAllAsRead(HttpServletRequest request) {
        // 登录校验
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 获取所有未读通知ID
        Page<CommentNotificationVO> unreadNotifications = commentNotificationService.getUserNotifications(
                loginUser.getId(), 0, 1, 1000);
        List<Long> ids = unreadNotifications.getRecords().stream()
                .map(CommentNotificationVO::getId)
                .collect(java.util.stream.Collectors.toList());

        boolean result = false;
        if (!ids.isEmpty()) {
            result = commentNotificationService.batchMarkAsRead(ids, loginUser.getId());
        } else {
            result = true; // 没有未读通知，直接返回成功
        }

        return ResultUtils.success(result);
    }

    /**
     * 删除通知
     *
     * @param deleteRequest 删除请求
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteNotification(@RequestBody DeleteRequest deleteRequest,
                                                    HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 登录校验
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        boolean result = commentNotificationService.deleteNotification(
                deleteRequest.getId(), loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 批量删除通知
     *
     * @param ids 通知ID列表
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @PostMapping("/delete/batch")
    public BaseResponse<Boolean> batchDeleteNotifications(@RequestBody List<Long> ids,
                                                        HttpServletRequest request) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "通知ID列表不能为空");
        }

        // 登录校验
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        boolean result = true;
        for (Long id : ids) {
            result = result && commentNotificationService.deleteNotification(id, loginUser.getId());
        }

        return ResultUtils.success(result);
    }

    /**
     * 清空所有已读通知
     *
     * @param request HTTP请求
     * @return 是否清空成功
     */
    @PostMapping("/clear")
    public BaseResponse<Boolean> clearReadNotifications(HttpServletRequest request) {
        // 登录校验
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 获取所有已读通知ID
        Page<CommentNotificationVO> readNotifications = commentNotificationService.getUserNotifications(
                loginUser.getId(), 1, 1, 1000);
        List<Long> ids = readNotifications.getRecords().stream()
                .map(CommentNotificationVO::getId)
                .collect(java.util.stream.Collectors.toList());

        boolean result = true;
        if (!ids.isEmpty()) {
            for (Long id : ids) {
                result = result && commentNotificationService.deleteNotification(id, loginUser.getId());
            }
        }

        return ResultUtils.success(result);
    }
}