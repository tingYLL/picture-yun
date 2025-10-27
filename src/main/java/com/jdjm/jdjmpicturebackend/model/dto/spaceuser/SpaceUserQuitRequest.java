package com.jdjm.jdjmpicturebackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户退出空间请求
 */
@Data
public class SpaceUserQuitRequest implements Serializable {

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 用户 ID
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}