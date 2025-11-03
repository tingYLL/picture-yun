package com.jdjm.jdjmpicturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jdjm.jdjmpicturebackend.annotation.AuthCheck;
import com.jdjm.jdjmpicturebackend.common.BaseResponse;
import com.jdjm.jdjmpicturebackend.common.DeleteRequest;
import com.jdjm.jdjmpicturebackend.common.ResultUtils;
import com.jdjm.jdjmpicturebackend.constant.UserConstant;
import com.jdjm.jdjmpicturebackend.exception.BusinessException;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.model.dto.comment.CommentAddRequest;
import com.jdjm.jdjmpicturebackend.model.dto.comment.CommentEditRequest;
import com.jdjm.jdjmpicturebackend.model.dto.comment.CommentQueryRequest;
import com.jdjm.jdjmpicturebackend.model.dto.comment.CommentReviewRequest;
import com.jdjm.jdjmpicturebackend.model.entity.User;
import com.jdjm.jdjmpicturebackend.model.vo.CommentVO;
import com.jdjm.jdjmpicturebackend.service.CommentService;
import com.jdjm.jdjmpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 评论接口
 */
@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {

    @Resource
    private CommentService commentService;

    @Resource
    private UserService userService;

    /**
     * 添加评论
     *
     * @param commentAddRequest 评论添加请求
     * @param request HTTP请求
     * @return 评论ID
     */
    @PostMapping("/add")
    public BaseResponse<Long> addComment(@RequestBody CommentAddRequest commentAddRequest,
                                         HttpServletRequest request) {
        if (commentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 参数校验
        Long pictureId = commentAddRequest.getPictureId();
        String content = commentAddRequest.getContent();
        if (pictureId == null || content == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片ID和评论内容不能为空");
        }

        // 内容长度校验
        if (content.length() > 1000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容过长，最多1000字符");
        }

        // 登录校验
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Long commentId = commentService.addComment(commentAddRequest, request);
        return ResultUtils.success(commentId);
    }

    /**
     * 编辑评论
     *
     * @param commentEditRequest 评论编辑请求
     * @param request HTTP请求
     * @return 是否编辑成功
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editComment(@RequestBody CommentEditRequest commentEditRequest,
                                            HttpServletRequest request) {
        if (commentEditRequest == null || commentEditRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 登录校验
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        boolean result = commentService.editComment(commentEditRequest, request);
        return ResultUtils.success(result);
    }

    /**
     * 删除评论
     *
     * @param deleteRequest 删除请求
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteComment(@RequestBody DeleteRequest deleteRequest,
                                             HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 登录校验
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        boolean result = commentService.deleteComment(deleteRequest.getId(), request);
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取评论
     *
     * @param id 评论ID
     * @param request HTTP请求
     * @return 评论详情
     */
    @GetMapping("/get")
    public BaseResponse<CommentVO> getCommentById(@RequestParam("id") Long id,
                                                HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        CommentVO commentVO = commentService.getCommentById(id, request);
        return ResultUtils.success(commentVO);
    }

    /**
     * 根据图片ID获取评论列表（分页）
     *
     * @param pictureId 图片ID
     * @param spaceId 空间ID（可选）
     * @param current 当前页码
     * @param pageSize 每页大小
     * @param request HTTP请求
     * @return 评论列表
     */
    @GetMapping("/list/by-picture")
    public BaseResponse<Page<CommentVO>> getCommentsByPictureId(
            @RequestParam("pictureId") Long pictureId,
            @RequestParam(value = "spaceId", required = false) Long spaceId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        if (pictureId == null || pictureId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片ID不能为空");
        }

        // 分页参数校验
        if (current == null || current <= 0) {
            current = 1;
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }

        Page<CommentVO> commentPage = commentService.getCommentsByPictureId(
                pictureId, spaceId, current, pageSize, request);
        return ResultUtils.success(commentPage);
    }

    /**
     * 获取评论树形结构（不分页）
     *
     * @param pictureId 图片ID
     * @param spaceId 空间ID（可选）
     * @param request HTTP请求
     * @return 评论树形列表
     */
    @GetMapping("/tree")
    public BaseResponse<List<CommentVO>> getCommentTree(
            @RequestParam("pictureId") Long pictureId,
            @RequestParam(value = "spaceId", required = false) Long spaceId,
            HttpServletRequest request) {

        if (pictureId == null || pictureId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片ID不能为空");
        }

        List<CommentVO> commentTree = commentService.getCommentTree(pictureId, spaceId);
        return ResultUtils.success(commentTree);
    }

    /**
     * 管理员审核评论
     *
     * @param commentReviewRequest 评论审核请求
     * @param request HTTP请求
     * @return 是否审核成功
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> reviewComment(@RequestBody CommentReviewRequest commentReviewRequest,
                                              HttpServletRequest request) {
        if (commentReviewRequest == null || commentReviewRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 审核状态校验
        Integer reviewStatus = commentReviewRequest.getReviewStatus();
        if (reviewStatus == null || (reviewStatus != 1 && reviewStatus != 2)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "审核状态只能是通过(1)或拒绝(2)");
        }

        boolean result = commentService.reviewComment(commentReviewRequest, request);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询评论（管理员使用）
     *
     * @param commentQueryRequest 评论查询请求
     * @param request HTTP请求
     * @return 评论分页列表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<CommentVO>> listCommentsByPage(@RequestBody CommentQueryRequest commentQueryRequest,
                                                           HttpServletRequest request) {
        if (commentQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 分页参数校验
        Integer current = commentQueryRequest.getCurrent();
        Integer pageSize = commentQueryRequest.getPageSize();
        if (current == null || current <= 0) {
            current = 1;
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        commentQueryRequest.setCurrent(current);
        commentQueryRequest.setPageSize(pageSize);

        Page<CommentVO> commentPage = commentService.listCommentsByPage(commentQueryRequest, request);
        return ResultUtils.success(commentPage);
    }

    /**
     * 获取当前登录用户的评论列表
     *
     * @param current 当前页码
     * @param pageSize 每页大小
     * @param request HTTP请求
     * @return 评论列表
     */
    @GetMapping("/list/my")
    public BaseResponse<Page<CommentVO>> listMyComments(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        // 登录校验
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 分页参数校验
        if (current == null || current <= 0) {
            current = 1;
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }

        // 构建查询条件
        CommentQueryRequest commentQueryRequest = new CommentQueryRequest();
        commentQueryRequest.setUserId(loginUser.getId());
        commentQueryRequest.setCurrent(current);
        commentQueryRequest.setPageSize(pageSize);

        Page<CommentVO> commentPage = commentService.listCommentsByPage(commentQueryRequest, request);
        return ResultUtils.success(commentPage);
    }
}