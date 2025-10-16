package com.jdjm.jdjmpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("vip_memberships")
public class VIPMembership  implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("is_vip")
    private Boolean isVip;

    @TableField("vip_start_date")
    private LocalDateTime vipStartDate;

    @TableField("vip_end_date")
    private LocalDateTime vipEndDate;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}