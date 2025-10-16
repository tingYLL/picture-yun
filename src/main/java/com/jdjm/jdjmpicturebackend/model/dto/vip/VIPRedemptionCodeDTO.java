package com.jdjm.jdjmpicturebackend.model.dto.vip;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VIPRedemptionCodeDTO {
    private Long id;

    private String code;

    private Boolean isUsed;
    private Long userId;
    private LocalDateTime usedAt;
}