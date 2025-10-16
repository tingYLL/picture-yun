package com.jdjm.jdjmpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("vip_redemption_codes")
public class VIPRedemptionCode implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    @TableField("is_used")
    private Boolean isUsed;

    @TableField("user_id")
    private Long userId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("used_at")
    private LocalDateTime usedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
