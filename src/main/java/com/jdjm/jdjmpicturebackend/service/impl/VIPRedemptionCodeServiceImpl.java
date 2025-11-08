package com.jdjm.jdjmpicturebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.exception.ThrowUtils;
import com.jdjm.jdjmpicturebackend.mapper.VIPRedemptionCodeMapper;
import com.jdjm.jdjmpicturebackend.model.entity.VIPRedemptionCode;
import com.jdjm.jdjmpicturebackend.service.VIPRedemptionCodeService;
import com.jdjm.jdjmpicturebackend.utils.RedemptionCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VIPRedemptionCodeServiceImpl extends ServiceImpl<VIPRedemptionCodeMapper, VIPRedemptionCode> implements VIPRedemptionCodeService {

    @Override
    public VIPRedemptionCode findByCode(String code) {
        QueryWrapper<VIPRedemptionCode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isCodeUsed(String code) {
        VIPRedemptionCode redemptionCode = findByCode(code);
        return redemptionCode != null && redemptionCode.getIsUsed();
    }

    @Override
    public boolean markCodeAsUsed(String code, Long userId) {
        VIPRedemptionCode redemptionCode = findByCode(code);
        if (redemptionCode == null || redemptionCode.getIsUsed()) {
            return false;
        }

        redemptionCode.setIsUsed(true);
        redemptionCode.setUserId(userId);
        redemptionCode.setUsedAt(LocalDateTime.now());
        redemptionCode.setUpdatedAt(LocalDateTime.now());

        return this.updateById(redemptionCode);
    }

    @Override
    public boolean save(VIPRedemptionCode entity) {
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        if (entity.getUpdatedAt() == null) {
            entity.setUpdatedAt(LocalDateTime.now());
        }
        return super.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generateBatchCodes(String type, int count) {
        ThrowUtils.throwIf(count <= 0 || count > 100, ErrorCode.PARAMS_ERROR, "生成数量必须在1-100之间");

        RedemptionCodeGenerator.VIPType vipType = RedemptionCodeGenerator.VIPType.valueOf(type.toUpperCase());
        List<String> generatedCodes = new ArrayList<>();
        List<VIPRedemptionCode> redemptionCodes = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 批量生成兑换码实体
        for (int i = 0; i < count; i++) {
            String code = RedemptionCodeGenerator.generate(vipType);
            generatedCodes.add(code);

            VIPRedemptionCode redemptionCode = new VIPRedemptionCode();
            redemptionCode.setCode(code);
            redemptionCode.setIsUsed(false);
            redemptionCode.setCreatedAt(now);
            redemptionCode.setUpdatedAt(now);
            redemptionCodes.add(redemptionCode);
        }

        // 批量保存到数据库
        boolean saveResult = this.saveBatch(redemptionCodes);
        ThrowUtils.throwIf(!saveResult, ErrorCode.OPERATION_ERROR, "批量保存兑换码失败");

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("codes", generatedCodes);
        result.put("type", vipType.name());
        result.put("count", count);
        result.put("generatedAt", now);
        result.put("durationDays", getDurationByType(vipType));

        return result;
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