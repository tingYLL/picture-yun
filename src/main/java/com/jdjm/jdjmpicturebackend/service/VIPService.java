package com.jdjm.jdjmpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jdjm.jdjmpicturebackend.model.entity.User;
import com.jdjm.jdjmpicturebackend.model.entity.VIPMembership;
import com.jdjm.jdjmpicturebackend.model.entity.VIPRedemptionCode;
import com.jdjm.jdjmpicturebackend.model.vo.VIPInfoVO;
import com.jdjm.jdjmpicturebackend.utils.RedemptionCodeGenerator;

public interface VIPService extends IService<VIPMembership> {
    boolean isVIP(Long userId);

    void activateVIP(VIPRedemptionCode vipRedemptionCode, User user,RedemptionCodeGenerator.VIPType type);

    VIPMembership getVIPMembership(Long userId);

    /**
     * 获取用户 VIP 信息
     * @param userId 用户ID
     * @return VIP 信息视图对象
     */
    VIPInfoVO getVIPInfo(Long userId);

    /**
     * 批量更新过期的 VIP 会员状态
     * @return 更新的记录数
     */
    int updateExpiredVIPStatus();
}
