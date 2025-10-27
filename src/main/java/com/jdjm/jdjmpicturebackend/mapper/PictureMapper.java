package com.jdjm.jdjmpicturebackend.mapper;

import com.jdjm.jdjmpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
* @author jdjm
* @description 针对表【picture(图片)】的数据库操作Mapper
* @createDate 2025-03-30 18:00:15
* @Entity com.jdjm.jdjmpicturebackend.model.entity.Picture
*/
public interface PictureMapper extends BaseMapper<Picture> {

    /**
     * 根据分类统计图片信息
     * @param spaceId 空间ID（可选）
     * @param queryPublic 是否查询公共图片
     * @return 分类统计结果列表
     */
    List<Map<String, Object>> getCategoryStatistics(@Param("spaceId") Long spaceId,
                                                   @Param("queryPublic") Boolean queryPublic);

}




