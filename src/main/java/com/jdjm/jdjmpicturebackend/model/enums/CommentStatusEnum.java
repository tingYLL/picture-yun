package com.jdjm.jdjmpicturebackend.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论状态枚举
 */
public enum CommentStatusEnum {

    NORMAL(0, "正常"),
    DELETED(1, "已删除"),
    BLOCKED(2, "违规被屏蔽");

    private final Integer code;
    private final String desc;

    CommentStatusEnum(Integer code, String desc) {
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
    public static CommentStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CommentStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

    /**
     * 获取所有有效的 code
     */
    public static List<Integer> getAllCodes() {
        return Arrays.stream(values()).map(CommentStatusEnum::getCode).collect(Collectors.toList());
    }
}