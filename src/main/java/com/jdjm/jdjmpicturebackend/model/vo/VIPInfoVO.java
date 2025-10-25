package com.jdjm.jdjmpicturebackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * VIP 信息视图
 */
@Data
public class VIPInfoVO implements Serializable {

    /**
     * 是否是 VIP
     */
    private Boolean isVip;

    /**
     * VIP 开始时间
     */
    private LocalDateTime vipStartDate;

    /**
     * VIP 到期时间
     */
    private LocalDateTime vipEndDate;

    /**
     * 剩余天数
     */
    private Long remainingDays;

    /**
     * 是否已过期
     */
    private Boolean isExpired;

    private static final long serialVersionUID = 1L;
}