package com.jdjm.jdjmpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jdjm.jdjmpicturebackend.model.entity.User;
import com.jdjm.jdjmpicturebackend.model.entity.VIPMembership;
import com.jdjm.jdjmpicturebackend.model.entity.VIPRedemptionCode;
import com.jdjm.jdjmpicturebackend.util.RedemptionCodeGenerator;

public interface VIPService extends IService<VIPMembership> {
    boolean isVIP(Long userId);

    void activateVIP(VIPRedemptionCode vipRedemptionCode, User user,RedemptionCodeGenerator.VIPType type);

    VIPMembership getVIPMembership(Long userId);
}
