package com.jdjm.jdjmpicturebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jdjm.jdjmpicturebackend.model.entity.VIPMembership;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VIPMembershipMapper extends BaseMapper<VIPMembership> {
    @Select("SELECT * FROM vip_memberships WHERE user_id = #{userId}")
    VIPMembership selectByUserId(Long userId);
}