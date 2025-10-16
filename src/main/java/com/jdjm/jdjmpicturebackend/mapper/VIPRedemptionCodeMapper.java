package com.jdjm.jdjmpicturebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jdjm.jdjmpicturebackend.model.entity.VIPRedemptionCode;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VIPRedemptionCodeMapper extends BaseMapper<VIPRedemptionCode> {
    VIPRedemptionCode selectByCode(String code);
}