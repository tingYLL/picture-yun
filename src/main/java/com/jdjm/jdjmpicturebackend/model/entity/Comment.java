package com.jdjm.jdjmpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论
 * @TableName comment
 */
@TableName(value = "comment")
@Data
public class Comment implements Serializable {
    /**
     * 评论主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 图片ID
     */
    private Long pictureId;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 父评论ID（null表示一级评论）
     */
    private Long parentId;

    /**
     * 根评论ID（用于嵌套回复）
     */
    private Long rootId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论状态：0-正常, 1-已删除, 2-违规被屏蔽
     */
    private Integer status;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 回复数
     */
    private Integer replyCount;

    /**
     * 空间ID（为null表示公共图库）
     */
    private Long spaceId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}