package com.jdjm.jdjmpicturebackend.model.dto.vip;

import lombok.Data;


@Data
public class RedeemCodeRequest {

    private String code;

    private Long userId;
}