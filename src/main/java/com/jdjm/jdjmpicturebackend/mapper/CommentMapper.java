package com.jdjm.jdjmpicturebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jdjm.jdjmpicturebackend.model.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author jdjm
* @description 针对表【comment(评论表)】的数据库操作Mapper
* @createDate 2025-10-31
* @Entity com.jdjm.jdjmpicturebackend.model.entity.Comment
*/
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 根据根评论ID获取所有子评论（递归查询）
     * @param rootId 根评论ID
     * @param spaceId 空间ID（可选）
     * @return 子评论列表
     */
    List<Comment> getCommentsByRootId(@Param("rootId") Long rootId, @Param("spaceId") Long spaceId);

    /**
     * 根据图片ID获取所有评论（包含子评论）
     * @param pictureId 图片ID
     * @param spaceId 空间ID（可选）
     * @return 评论列表
     */
    List<Comment> getAllCommentsByPictureId(@Param("pictureId") Long pictureId, @Param("spaceId") Long spaceId);

    /**
     * 获取根评论列表（一级评论）
     * @param pictureId 图片ID
     * @param spaceId 空间ID（可选）
     * @param status 评论状态（可选）
     * @return 根评论列表
     */
    List<Comment> getRootComments(@Param("pictureId") Long pictureId,
                                 @Param("spaceId") Long spaceId,
                                 @Param("status") Integer status);

    /**
     * 增加评论的回复数
     * @param commentId 评论ID
     * @return 影响行数
     */
    int incrementReplyCount(@Param("commentId") Long commentId);

    /**
     * 减少评论的回复数
     * @param commentId 评论ID
     * @return 影响行数
     */
    int decrementReplyCount(@Param("commentId") Long commentId);

}