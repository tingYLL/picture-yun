package com.jdjm.jdjmpicturebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jdjm.jdjmpicturebackend.model.entity.CommentNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author jdjm
* @description 针对表【comment_notification(评论通知)】的数据库操作Mapper
* @createDate 2025-10-31
* @Entity com.jdjm.jdjmpicturebackend.model.entity.CommentNotification
*/
@Mapper
public interface CommentNotificationMapper extends BaseMapper<CommentNotification> {

    /**
     * 标记通知为已读
     * @param id 通知ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 批量标记通知为已读
     * @param ids 通知ID列表
     * @param userId 用户ID
     * @return 影响行数
     */
    int batchMarkAsRead(@Param("ids") java.util.List<Long> ids, @Param("userId") Long userId);

    /**
     * 获取用户未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    Long getUnreadCount(@Param("userId") Long userId);

}