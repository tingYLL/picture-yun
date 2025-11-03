package com.jdjm.jdjmpicturebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jdjm.jdjmpicturebackend.exception.BusinessException;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.mapper.CommentMapper;
import com.jdjm.jdjmpicturebackend.model.dto.comment.CommentAddRequest;
import com.jdjm.jdjmpicturebackend.model.dto.comment.CommentEditRequest;
import com.jdjm.jdjmpicturebackend.model.dto.comment.CommentQueryRequest;
import com.jdjm.jdjmpicturebackend.model.dto.comment.CommentReviewRequest;
import com.jdjm.jdjmpicturebackend.model.entity.Comment;
import com.jdjm.jdjmpicturebackend.model.entity.Picture;
import com.jdjm.jdjmpicturebackend.model.entity.User;
import com.jdjm.jdjmpicturebackend.model.enums.CommentStatusEnum;
import com.jdjm.jdjmpicturebackend.model.vo.CommentVO;
import com.jdjm.jdjmpicturebackend.service.CommentNotificationService;
import com.jdjm.jdjmpicturebackend.service.CommentService;
import com.jdjm.jdjmpicturebackend.service.PictureService;
import com.jdjm.jdjmpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author jdjm
* @description 针对表【comment(评论表)】的数据库操作Service实现
* @createDate 2025-10-31
*/
@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Autowired
    @Lazy
    private CommentNotificationService commentNotificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(CommentAddRequest commentAddRequest, HttpServletRequest request) {
        if (commentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 参数校验
        Long pictureId = commentAddRequest.getPictureId();
        String content = commentAddRequest.getContent();
        Long parentId = commentAddRequest.getParentId();
        Long spaceId = commentAddRequest.getSpaceId();

        if (pictureId == null || StringUtils.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片ID和评论内容不能为空");
        }

        if (content.length() > 1000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容过长，最多1000字符");
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 验证图片是否存在
        Picture picture = pictureService.getById(pictureId);
        if (picture == null || picture.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }

        // 如果是回复评论，验证父评论是否存在
        Comment parentComment = null;
        Long rootId = null;
        if (parentId != null) {
            parentComment = this.getById(parentId);
            if (parentComment == null || parentComment.getIsDelete() == 1) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "父评论不存在");
            }
            rootId = parentComment.getRootId() != null ? parentComment.getRootId() : parentId;
        }

        // 创建评论
        Comment comment = new Comment();
        comment.setPictureId(pictureId);
        comment.setUserId(loginUser.getId());
        comment.setParentId(parentId);
        comment.setRootId(rootId);
        comment.setContent(content.trim());
        comment.setStatus(CommentStatusEnum.NORMAL.getCode());
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setSpaceId(spaceId);

        // 保存评论
        boolean result = this.save(comment);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "评论保存失败");
        }

        // 如果是回复评论，更新父评论的回复数
        if (parentComment != null) {
            commentMapper.incrementReplyCount(parentComment.getId());
            // 通知父评论作者
            if (!parentComment.getUserId().equals(loginUser.getId())) {
                commentNotificationService.createNotification(
                    parentComment.getUserId(),
                    comment.getId(),
                    pictureId,
                    1, // 回复类型
                    "您的评论收到新回复",
                    content.trim(),
                    loginUser.getId()
                );
            }
        } else {
            // 一级评论，设置根评论ID为自己
            comment.setRootId(comment.getId());
            this.updateById(comment);
            // 通知图片作者
            if (!picture.getUserId().equals(loginUser.getId())) {
                commentNotificationService.createNotification(
                    picture.getUserId(),
                    comment.getId(),
                    pictureId,
                    0, // 新评论类型
                    "您的图片收到新评论",
                    content.trim(),
                    loginUser.getId()
                );
            }
        }

        return comment.getId();
    }

    @Override
    public boolean editComment(CommentEditRequest commentEditRequest, HttpServletRequest request) {
        if (commentEditRequest == null || commentEditRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long commentId = commentEditRequest.getId();
        String content = commentEditRequest.getContent();

        if (StringUtils.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容不能为空");
        }

        if (content.length() > 1000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容过长，最多1000字符");
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 获取评论
        Comment comment = this.getById(commentId);
        if (comment == null || comment.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }

        // 检查权限（只有评论作者可以编辑）
        if (!comment.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限编辑此评论");
        }

        // 更新评论
        UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", commentId);
        updateWrapper.set("content", content.trim());
        updateWrapper.set("editTime", new Date());

        return this.update(updateWrapper);
    }

    @Override
    public boolean deleteComment(Long id, HttpServletRequest request) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 获取评论
        Comment comment = this.getById(id);
        if (comment == null || comment.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }

        // 检查权限（评论作者或管理员可以删除）
        boolean isAdmin = userService.isAdmin(loginUser);
        if (!comment.getUserId().equals(loginUser.getId()) && !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除此评论");
        }

        // 软删除评论
        boolean result = this.removeById(id);
        if (result && comment.getParentId() != null) {
            // 如果是回复评论，减少父评论的回复数
            commentMapper.decrementReplyCount(comment.getParentId());
        }

        return result;
    }

    @Override
    public CommentVO getCommentById(Long id, HttpServletRequest request) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Comment comment = this.getById(id);
        if (comment == null || comment.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }

        return getCommentVO(comment);
    }

    @Override
    public Page<CommentVO> getCommentsByPictureId(Long pictureId, Long spaceId,
                                                  Integer current, Integer pageSize,
                                                  HttpServletRequest request) {
        if (pictureId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 参数设置
        current = current == null || current <= 0 ? 1 : current;
        pageSize = pageSize == null || pageSize <= 0 ? 10 : pageSize;

        // 获取根评论列表
        List<Comment> rootComments = commentMapper.getRootComments(pictureId, spaceId, CommentStatusEnum.NORMAL.getCode());

        // 构建树形结构
        List<CommentVO> commentVOList = buildCommentTree(rootComments);

        // 分页处理
        Page<CommentVO> page = new Page<>(current, pageSize);
        int total = commentVOList.size();
        int startIndex = (current - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);

        List<CommentVO> pageList = startIndex < total ?
            commentVOList.subList(startIndex, endIndex) : new ArrayList<>();

        page.setRecords(pageList);
        page.setTotal(total);

        return page;
    }

    @Override
    public List<CommentVO> getCommentTree(Long pictureId, Long spaceId) {
        if (pictureId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取根评论列表
        List<Comment> rootComments = commentMapper.getRootComments(pictureId, spaceId, CommentStatusEnum.NORMAL.getCode());

        // 构建树形结构
        return buildCommentTree(rootComments);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewComment(CommentReviewRequest commentReviewRequest, HttpServletRequest request) {
        if (commentReviewRequest == null || commentReviewRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 检查是否为管理员
        if (!userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有管理员可以审核评论");
        }

        // 获取评论
        Comment comment = this.getById(commentReviewRequest.getId());
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }

        // 更新审核状态
        UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", commentReviewRequest.getId());
        updateWrapper.set("status", commentReviewRequest.getReviewStatus());
        updateWrapper.set("updateTime", new Date());

        boolean result = this.update(updateWrapper);

        // 发送审核结果通知
        if (result && commentReviewRequest.getReviewStatus() != null) {
            Integer notificationType = commentReviewRequest.getReviewStatus() == 1 ? 2 : 3; // 通过或拒绝
            String title = commentReviewRequest.getReviewStatus() == 1 ? "评论审核通过" : "评论审核拒绝";
            String content = StringUtils.isNotBlank(commentReviewRequest.getReviewMessage())
                ? commentReviewRequest.getReviewMessage() : "您的评论已被" + title;

            commentNotificationService.createNotification(
                comment.getUserId(),
                comment.getId(),
                comment.getPictureId(),
                notificationType,
                title,
                content,
                loginUser.getId()
            );
        }

        return result;
    }

    @Override
    public Page<CommentVO> listCommentsByPage(CommentQueryRequest commentQueryRequest, HttpServletRequest request) {
        if (commentQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 检查是否为管理员
        if (!userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有管理员可以查看所有评论");
        }

        // 构建查询条件
        QueryWrapper<Comment> queryWrapper = getQueryWrapper(commentQueryRequest);

        // 分页查询
        Page<Comment> commentPage = this.page(new Page<>(commentQueryRequest.getCurrent(), commentQueryRequest.getPageSize()), queryWrapper);

        // 转换为VO
        Page<CommentVO> commentVOPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        List<CommentVO> commentVOList = getCommentVOList(commentPage.getRecords());
        commentVOPage.setRecords(commentVOList);

        return commentVOPage;
    }

    @Override
    public QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();

        if (commentQueryRequest == null) {
            return queryWrapper;
        }

        Long id = commentQueryRequest.getId();
        Long pictureId = commentQueryRequest.getPictureId();
        Long userId = commentQueryRequest.getUserId();
        Long parentId = commentQueryRequest.getParentId();
        Long rootId = commentQueryRequest.getRootId();
        Integer status = commentQueryRequest.getStatus();
        Long spaceId = commentQueryRequest.getSpaceId();
        Date minCreateTime = commentQueryRequest.getMinCreateTime();
        Date maxCreateTime = commentQueryRequest.getMaxCreateTime();
        String sortField = commentQueryRequest.getSortField();
        String sortOrder = commentQueryRequest.getSortOrder();

        // 构建查询条件
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(pictureId != null, "pictureId", pictureId);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.eq(parentId != null, "parentId", parentId);
        queryWrapper.eq(rootId != null, "rootId", rootId);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.eq(spaceId != null, "spaceId", spaceId);
        queryWrapper.ge(minCreateTime != null, "createTime", minCreateTime);
        queryWrapper.le(maxCreateTime != null, "createTime", maxCreateTime);

        // 排序
        if (StringUtils.isNotBlank(sortField)) {
            if ("desc".equals(sortOrder)) {
                queryWrapper.orderByDesc(sortField);
            } else {
                queryWrapper.orderByAsc(sortField);
            }
        } else {
            queryWrapper.orderByDesc("createTime");
        }

        return queryWrapper;
    }

    @Override
    public boolean checkCommentPermission(Long commentId, Long userId) {
        if (commentId == null || userId == null) {
            return false;
        }

        Comment comment = this.getById(commentId);
        if (comment == null || comment.getIsDelete() == 1) {
            return false;
        }

        return comment.getUserId().equals(userId);
    }

    @Override
    public CommentVO getCommentVO(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentVO commentVO = CommentVO.objToVo(comment);

        // 获取用户信息
        User user = userService.getById(comment.getUserId());
        if (user != null) {
            commentVO.setUser(userService.getUserVO(user));
        }

        // 设置状态描述
        CommentStatusEnum statusEnum = CommentStatusEnum.getByCode(comment.getStatus());
        if (statusEnum != null) {
            commentVO.setStatusDesc(statusEnum.getDesc());
        }

        // 设置层级
        if (comment.getParentId() == null) {
            commentVO.setLevel(0);
        } else {
            commentVO.setLevel(1);
        }

        return commentVO;
    }

    @Override
    public List<CommentVO> getCommentVOList(List<Comment> commentList) {
        if (commentList == null || commentList.isEmpty()) {
            return new ArrayList<>();
        }

        return commentList.stream()
                .map(this::getCommentVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentVO> buildCommentTree(List<Comment> rootComments) {
        if (rootComments == null || rootComments.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为VO列表
        List<CommentVO> rootCommentVOList = getCommentVOList(rootComments);

        // 为每个根评论递归构建子评论树
        for (CommentVO rootComment : rootCommentVOList) {
            buildChildCommentsRecursive(rootComment);
        }

        // 对根评论按创建时间排序
        rootCommentVOList.sort(Comparator.comparing(CommentVO::getCreateTime).reversed());

        return rootCommentVOList;
    }

    /**
     * 递归构建子评论树
     */
    private void buildChildCommentsRecursive(CommentVO parentComment) {
        if (parentComment == null) {
            return;
        }

        // 获取父评论下的所有直接子评论
        List<Comment> childComments = commentMapper.selectList(
            new QueryWrapper<Comment>()
                    .eq("parentId", parentComment.getId())
                    .eq("isDelete", 0)
                    .eq("status", CommentStatusEnum.NORMAL.getCode())
                    .orderByAsc("createTime")
        );

        if (childComments.isEmpty()) {
            return;
        }

        // 转换为VO并递归构建
        List<CommentVO> childCommentVOs = getCommentVOList(childComments);
        parentComment.setChildren(childCommentVOs);

        // 递归构建每个子评论的子评论
        for (CommentVO childComment : childCommentVOs) {
            buildChildCommentsRecursive(childComment);
        }
    }

    @Override
    public void buildChildComments(CommentVO parentComment, List<Comment> allComments) {
        // 这个方法在buildCommentTree中已经实现了
    }

    /**
     * 递归排序评论树
     */
    private void sortCommentTree(List<CommentVO> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        // 按创建时间正序排序
        comments.sort(Comparator.comparing(CommentVO::getCreateTime));

        // 递归排序子评论
        for (CommentVO comment : comments) {
            if (comment.getChildren() != null && !comment.getChildren().isEmpty()) {
                sortCommentTree(comment.getChildren());
            }
        }
    }
}