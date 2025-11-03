package com.jdjm.jdjmpicturebackend.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知类型枚举
 */
public enum NotificationTypeEnum {

    NEW_COMMENT(0, "新评论"),
    REPLY_COMMENT(1, "回复评论"),
    COMMENT_APPROVED(2, "评论审核通过"),
    COMMENT_REJECTED(3, "评论审核拒绝");

    private final Integer code;
    private final String desc;

    NotificationTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static NotificationTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NotificationTypeEnum notificationTypeEnum : values()) {
            if (notificationTypeEnum.getCode().equals(code)) {
                return notificationTypeEnum;
            }
        }
        return null;
    }

    /**
     * 获取所有有效的 code
     */
    public static List<Integer> getAllCodes() {
        return Arrays.stream(values()).map(NotificationTypeEnum::getCode).collect(Collectors.toList());
    }
}