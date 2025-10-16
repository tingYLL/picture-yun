package com.jdjm.jdjmpicturebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jdjm.jdjmpicturebackend.exception.BusinessException;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.mapper.UserMapper;
import com.jdjm.jdjmpicturebackend.mapper.VIPRedemptionCodeMapper;
import com.jdjm.jdjmpicturebackend.model.entity.User;
import com.jdjm.jdjmpicturebackend.model.entity.VIPMembership;
import com.jdjm.jdjmpicturebackend.mapper.VIPMembershipMapper;
import com.jdjm.jdjmpicturebackend.model.entity.VIPRedemptionCode;
import com.jdjm.jdjmpicturebackend.service.VIPRedemptionCodeService;
import com.jdjm.jdjmpicturebackend.service.VIPService;
import com.jdjm.jdjmpicturebackend.util.RedemptionCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class VIPServiceImpl extends ServiceImpl<VIPMembershipMapper,VIPMembership> implements VIPService {

    @Autowired
    private VIPMembershipMapper vipMembershipMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VIPRedemptionCodeMapper vipRedemptionCodeMapper;
    @Resource
    private VIPRedemptionCodeService vipRedemptionCodeService;

    @Transactional
    public boolean validateRedemptionCode(String code, Long userId) {
        VIPRedemptionCode redemptionCode = vipRedemptionCodeMapper.selectByCode(code);
        if (redemptionCode == null || redemptionCode.getIsUsed()) {
            return false;
        }
        return true;
    }

    @Transactional
    public void markRedemptionCodeAsUsed(String code, Long userId) {
        VIPRedemptionCode redemptionCode = vipRedemptionCodeMapper.selectByCode(code);
        if (redemptionCode != null) {
            redemptionCode.setIsUsed(true);
            redemptionCode.setUserId(userId);
            redemptionCode.setUsedAt(LocalDateTime.now());
            vipRedemptionCodeMapper.updateById(redemptionCode);
        }
    }

    @Transactional
    public void activateVIP(User user, int durationInDays) {
        Long userId = user.getId();
        VIPMembership vipMembership = vipMembershipMapper.selectByUserId(userId);
        if (vipMembership == null) {
            vipMembership = new VIPMembership();
            vipMembership.setUserId(userId);
        }
        vipMembership.setIsVip(true);
        vipMembership.setVipStartDate(LocalDateTime.now());
        vipMembership.setVipEndDate(LocalDateTime.now().plusDays(durationInDays));
        vipMembershipMapper.insertOrUpdate(vipMembership);
    }

    @Transactional
    public void activateVIPWithRedemptionCode(User user, int durationInDays, String redemptionCode) {
        // 1. 验证兑换码
        if (!validateRedemptionCode(redemptionCode, user.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "兑换码无效或已被使用");
        }

        // 2. 开通 VIP
        activateVIP(user, durationInDays);

        // 3. 标记兑换码为已使用
        markRedemptionCodeAsUsed(redemptionCode, user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateVIP(VIPRedemptionCode vipRedemptionCode,User user, RedemptionCodeGenerator.VIPType type) {
        Long userId = user.getId();
        vipRedemptionCode.setIsUsed(true);
        vipRedemptionCode.setUserId(userId);
        vipRedemptionCode.setUsedAt(LocalDateTime.now());
        vipRedemptionCode.setUpdatedAt(LocalDateTime.now());

        vipRedemptionCodeService.updateById(vipRedemptionCode);
        VIPMembership vipMembership = getVIPMembership(userId);

        int durationInDays = getDurationByType(type);
        LocalDateTime now = LocalDateTime.now();

        if (vipMembership == null) {
            vipMembership = new VIPMembership();
            vipMembership.setUserId(userId);
            vipMembership.setIsVip(true);
            vipMembership.setVipStartDate(now);
            vipMembership.setVipEndDate(now.plusDays(durationInDays));
            vipMembership.setCreatedAt(now);
            vipMembership.setUpdatedAt(now);
            this.save(vipMembership);
        } else {
            if (vipMembership.getIsVip() && vipMembership.getVipEndDate().isAfter(now)) {
                vipMembership.setVipEndDate(vipMembership.getVipEndDate().plusDays(durationInDays));
            } else {
                vipMembership.setIsVip(true);
                vipMembership.setVipStartDate(now);
                vipMembership.setVipEndDate(now.plusDays(durationInDays));
            }
            vipMembership.setUpdatedAt(now);
            this.updateById(vipMembership);
        }
    }

    @Override
    public VIPMembership getVIPMembership(Long userId) {
        QueryWrapper<VIPMembership> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isVIP(Long userId) {
        VIPMembership vipMembership = getVIPMembership(userId);
        return vipMembership != null && vipMembership.getIsVip() && vipMembership.getVipEndDate().isAfter(LocalDateTime.now());
    }

    private int getDurationByType(RedemptionCodeGenerator.VIPType type) {
        switch (type) {
            case MONTHLY:
                return 30;
            case QUARTERLY:
                return 90;
            case YEARLY:
                return 365;
            default:
                return 30;
        }
    }
}