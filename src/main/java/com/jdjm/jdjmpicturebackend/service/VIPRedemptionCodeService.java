package com.jdjm.jdjmpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jdjm.jdjmpicturebackend.model.entity.VIPRedemptionCode;

import java.util.List;
import java.util.Map;

public interface VIPRedemptionCodeService extends IService<VIPRedemptionCode> {

    VIPRedemptionCode findByCode(String code);

    boolean isCodeUsed(String code);

    boolean markCodeAsUsed(String code, Long userId);

    /**
     * 批量生成VIP兑换码
     * @param type VIP类型
     * @param count 生成数量
     * @return 生成的兑换码信息
     */
    Map<String, Object> generateBatchCodes(String type, int count);
}