package com.jdjm.jdjmpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 更新用户请求
 */
@Data
public class UserUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 用户昵称
     */
    private String userName;
    /**
     * 用户手机号
     */
    private String userPhone;


    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 出生日期
     */
    private Date birthday;
    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 是否禁用（0-正常, 1-禁用）
     */
    private Integer isDisabled = 0;

    private static final long serialVersionUID = 1L;
}