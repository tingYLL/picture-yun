package com.jdjm.jdjmpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserEditRequest implements Serializable {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 出生日期
     */
    private Date birthday;

    /**
     * 分享码
     */
    private String shareCode;

    private static final long serialVersionUID = 1L;
}
