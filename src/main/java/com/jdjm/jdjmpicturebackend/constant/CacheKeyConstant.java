package com.jdjm.jdjmpicturebackend.constant;

/**
 * 缓存 KEY 常量接口
 */
public interface CacheKeyConstant {
    /**
     * 邮箱验证码缓存 KEY 前缀
     */
    String EMAIL_CODE_KEY = "EMAIL_CODE_KEY:%s:%s";

    /**
     * 图形验证码缓存 KEY 前缀
     */
    String CAPTCHA_CODE_KEY = "CAPTCHA_CODE_KEY:%s";

    /**
     * 首页分类缓存 KEY
     */
    String HOME_CATEGORY = "HOME_CATEGORY";

    /**
     * 首页图片列表缓存 KEY
     */
    String HOME_PICTURE_LIST_KEY = "HOME_PICTURE_LIST_KEY:%s";

    /**
     * 图片互动 KEY 前缀
     */
    String PICTURE_INTERACTION_KEY_PREFIX = "picture:interactions:";

    /**
     * 百炼任务 KEY 前缀
     */
    String BAI_LIAN_TASK_COUNT_KEY_PREFIX = "bai_lian:task:%s:%s";
}
