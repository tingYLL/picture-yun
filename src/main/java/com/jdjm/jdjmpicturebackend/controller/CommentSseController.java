package com.jdjm.jdjmpicturebackend.controller;

import com.jdjm.jdjmpicturebackend.exception.BusinessException;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.manager.auth.StpKit;
import com.jdjm.jdjmpicturebackend.model.entity.User;
import com.jdjm.jdjmpicturebackend.service.CommentNotificationService;
import com.jdjm.jdjmpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 评论通知SSE接口
 * 使用Server-Sent Events实现实时通知推送
 */
@RestController
@RequestMapping("/comment/sse")
@Slf4j
public class CommentSseController {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private CommentNotificationService commentNotificationService;

    /**
     * 存储用户的SSE连接
     * Key: userId, Value: SseEmitter
     */
    private static final Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    /**
     * 计数器，用于生成连接ID
     */
    private static final AtomicLong connectionCounter = new AtomicLong(0);

    /**
     * 建立SSE连接
     *
     * @param request HTTP请求
     * @return SSE连接
     */
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(HttpServletRequest request) {
        // 登录校验
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Long userId = loginUser.getId();
        Long connectionId = connectionCounter.incrementAndGet();

        log.info("用户 {} 建立SSE连接，连接ID: {}", userId, connectionId);

        // 如果用户已有连接，先关闭旧连接
        SseEmitter oldEmitter = sseEmitters.get(userId);
        if (oldEmitter != null) {
            log.info("用户 {} 存在旧连接，先关闭旧连接", userId);
            try {
                oldEmitter.complete();
            } catch (Exception e) {
                log.error("关闭旧连接失败", e);
            }
            sseEmitters.remove(userId);
        }

        // 创建SSE连接，设置超时时间（30分钟） 超过30分钟，执行下面的onTimeout回调,从 sseEmitters Map 中移除该用户的连接
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        // 保存连接
        sseEmitters.put(userId, emitter);

        try {
            // 发送连接成功消息
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"connectionId\":" + connectionId + ",\"message\":\"连接成功\"}")
                    .id(String.valueOf(connectionId)));

            // 发送当前未读通知数量
            Long unreadCount = commentNotificationService.getUnreadCount(userId);
            emitter.send(SseEmitter.event()
                    .name("unreadCount")
                    .data(String.valueOf(unreadCount)));
        } catch (IOException e) {
            log.error("SSE连接发送初始消息失败", e);
            // 初始化失败，移除连接并关闭 emitter
            sseEmitters.remove(userId);
            emitter.completeWithError(e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "SSE连接初始化失败");
        }

        // 设置连接完成和超时时的处理
        emitter.onCompletion(() -> {
            log.info("用户 {} SSE连接完成，连接ID: {}", userId, connectionId);
            // 只有当前Map中的emitter是当前这个emitter时，才移除
            // 避免移除了后续建立的新连接
            SseEmitter currentEmitter = sseEmitters.get(userId);
            if (currentEmitter == emitter) {
                sseEmitters.remove(userId);
                log.info("用户 {} 的连接 {} 已从Map中移除", userId, connectionId);
            } else {
                log.info("用户 {} 的连接 {} 已被新连接替换，跳过移除", userId, connectionId);
            }
        });

        emitter.onTimeout(() -> {
            log.info("用户 {} SSE连接超时，连接ID: {}", userId, connectionId);
            // 只有当前Map中的emitter是当前这个emitter时，才移除
            SseEmitter currentEmitter = sseEmitters.get(userId);
            if (currentEmitter == emitter) {
                sseEmitters.remove(userId);
                log.info("用户 {} 的连接 {} 超时已从Map中移除", userId, connectionId);
            } else {
                log.info("用户 {} 的连接 {} 已被新连接替换，跳过移除", userId, connectionId);
            }
            emitter.complete();
        });

        // 添加错误处理回调 - 关键修复点
        emitter.onError((throwable) -> {
            log.error("用户 {} SSE连接发生错误，连接ID: {}，错误信息: {}", userId, connectionId, throwable.getMessage());
            // 只有当前Map中的emitter是当前这个emitter时，才移除
            SseEmitter currentEmitter = sseEmitters.get(userId);
            if (currentEmitter == emitter) {
                sseEmitters.remove(userId);
                log.error("用户 {} 的连接 {} 错误已从Map中移除", userId, connectionId);
            } else {
                log.info("用户 {} 的连接 {} 已被新连接替换，跳过移除", userId, connectionId);
            }
            emitter.complete();
        });

        return emitter;
    }

    /**
     * 断开SSE连接
     *
     * @param request HTTP请求
     * @return 是否断开成功
     */
    @PostMapping("/disconnect")
    public boolean disconnect(HttpServletRequest request) {
        // 登录校验
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Long userId = loginUser.getId();
        SseEmitter emitter = sseEmitters.remove(userId);

        if (emitter != null) {
            try {
                emitter.complete();
                log.info("用户 {} 主动断开SSE连接", userId);
                return true;
            } catch (Exception e) {
                log.error("断开SSE连接失败", e);
                return false;
            }
        }

        return false;
    }

    /**
     * 向指定用户推送通知
     *
     * @param userId 用户ID
     * @param eventName 事件名称
     * @param data 数据
     */
    public static void pushNotificationToUser(Long userId, String eventName, String data) {
        SseEmitter emitter = sseEmitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                log.info("向用户 {} 推送通知成功: {}", userId, eventName);
            } catch (IOException e) {
                log.error("向用户 {} 推送通知失败: {}", userId, eventName, e);
                // 推送失败时先移除连接，再关闭 emitter
                sseEmitters.remove(userId);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.error("关闭SSE连接失败", ex);
                }
            }
        } else {
            log.debug("用户 {} 未建立SSE连接，无法推送通知: {}", userId, eventName);
        }
    }

    /**
     * 向所有在线用户推送广播通知
     *
     * @param eventName 事件名称
     * @param data 数据
     */
    public static void broadcastNotification(String eventName, String data) {
        log.info("向所有在线用户推送广播通知: {}", eventName);

        sseEmitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.error("向用户 {} 推送广播通知失败: {}", userId, eventName, e);
                // 推送失败时先移除连接，再关闭 emitter
                sseEmitters.remove(userId);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.error("关闭SSE连接失败", ex);
                }
            }
        });
    }

    /**
     * 获取当前在线用户数量
     *
     * @return 在线用户数量
     */
    @GetMapping("/online/count")
    public int getOnlineCount() {
        return sseEmitters.size();
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    public static boolean isUserOnline(Long userId) {
        return sseEmitters.containsKey(userId);
    }
}