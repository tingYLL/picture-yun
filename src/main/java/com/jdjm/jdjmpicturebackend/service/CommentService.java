package com.jdjm.jdjmpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jdjm.jdjmpicturebackend.model.dto.comment.CommentAddRequest;
import com.jdjm.jdjmpicturebackend.model.dto.comment.CommentEditRequest;
import com.jdjm.jdjmpicturebackend.model.dto.comment.CommentQueryRequest;
import com.jdjm.jdjmpicturebackend.model.dto.comment.CommentReviewRequest;
import com.jdjm.jdjmpicturebackend.model.entity.Comment;
import com.jdjm.jdjmpicturebackend.model.vo.CommentVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author jdjm
* @description 针对表【comment(评论表)】的数据库操作Service
* @createDate 2025-10-31
*/
public interface CommentService extends IService<Comment> {

    /**
     * 添加评论
     *
     * @param commentAddRequest 评论添加请求
     * @param request HTTP请求
     * @return 评论ID
     */
    Long addComment(CommentAddRequest commentAddRequest, HttpServletRequest request);

    /**
     * 编辑评论
     *
     * @param commentEditRequest 评论编辑请求
     * @param request HTTP请求
     * @return 是否编辑成功
     */
    boolean editComment(CommentEditRequest commentEditRequest, HttpServletRequest request);

    /**
     * 删除评论
     *
     * @param id 评论ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    boolean deleteComment(Long id, HttpServletRequest request);

    /**
     * 根据ID获取评论（树形结构）
     *
     * @param id 评论ID
     * @param request HTTP请求
     * @return 评论VO
     */
    CommentVO getCommentById(Long id, HttpServletRequest request);

    /**
     * 根据图片ID获取评论列表（树形结构）
     *
     * @param pictureId 图片ID
     * @param spaceId 空间ID（可选）
     * @param current 当前页码
     * @param pageSize 每页大小
     * @param request HTTP请求
     * @return 评论列表
     */
    Page<CommentVO> getCommentsByPictureId(Long pictureId, Long spaceId,
                                          Integer current, Integer pageSize,
                                          HttpServletRequest request);

    /**
     * 获取评论树形结构列表
     *
     * @param pictureId 图片ID
     * @param spaceId 空间ID（可选）
     * @return 评论树形结构列表
     */
    List<CommentVO> getCommentTree(Long pictureId, Long spaceId);

    /**
     * 管理员审核评论
     *
     * @param commentReviewRequest 评论审核请求
     * @param request HTTP请求
     * @return 是否审核成功
     */
    boolean reviewComment(CommentReviewRequest commentReviewRequest, HttpServletRequest request);

    /**
     * 分页查询评论（管理员使用）
     *
     * @param commentQueryRequest 评论查询请求
     * @param request HTTP请求
     * @return 评论分页列表
     */
    Page<CommentVO> listCommentsByPage(CommentQueryRequest commentQueryRequest, HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param commentQueryRequest 评论查询请求
     * @return 查询包装器
     */
    QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest);

    /**
     * 检查用户是否有权限操作评论
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean checkCommentPermission(Long commentId, Long userId);

    /**
     * 获取评论VO
     *
     * @param comment 评论实体
     * @return 评论VO
     */
    CommentVO getCommentVO(Comment comment);

    /**
     * 获取评论VO列表
     *
     * @param commentList 评论实体列表
     * @return 评论VO列表
     */
    List<CommentVO> getCommentVOList(List<Comment> commentList);

    /**
     * 构建评论树形结构
     *
     * @param commentList 评论列表
     * @return 树形结构评论列表
     */
    List<CommentVO> buildCommentTree(List<Comment> commentList);

    /**
     * 递归构建子评论
     *
     * @param parentComment 父评论
     * @param allComments 所有评论列表
     */
    void buildChildComments(CommentVO parentComment, List<Comment> allComments);
}