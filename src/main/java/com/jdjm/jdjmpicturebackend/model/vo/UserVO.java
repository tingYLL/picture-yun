package com.jdjm.jdjmpicturebackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户视图（脱敏）
 */
@Data
public class UserVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

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
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 出生日期
     */
    private Date birthday;

    /**
     * 会员编号
     */
    private Long vipNnumber;

    /**
     * 会员过期时间
     */
    private Date vipExpireTime;

    /**
     * 会员兑换码
     */
    private String vipCode;

    /**
     * 是否为 VIP（从 vip_memberships 表计算得出）
     */
    private Boolean isVip;

    /**
     * 分享码
     */
    private String shareCode;
    /**
     * 是否禁用（0-正常, 1-禁用）
     */
    private Integer isDisabled;
    /**
     * 邀请用户 ID
     */
    private Long inviteUserId;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}