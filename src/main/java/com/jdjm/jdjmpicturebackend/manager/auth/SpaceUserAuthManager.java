package com.jdjm.jdjmpicturebackend.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jdjm.jdjmpicturebackend.manager.auth.model.SpaceUserAuthConfig;
import com.jdjm.jdjmpicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.jdjm.jdjmpicturebackend.manager.auth.model.SpaceUserRole;
import com.jdjm.jdjmpicturebackend.model.entity.Picture;
import com.jdjm.jdjmpicturebackend.model.entity.Space;
import com.jdjm.jdjmpicturebackend.model.entity.SpaceUser;
import com.jdjm.jdjmpicturebackend.model.entity.User;
import com.jdjm.jdjmpicturebackend.model.enums.SpaceRoleEnum;
import com.jdjm.jdjmpicturebackend.model.enums.SpaceTypeEnum;
import com.jdjm.jdjmpicturebackend.service.SpaceService;
import com.jdjm.jdjmpicturebackend.service.SpaceUserService;
import com.jdjm.jdjmpicturebackend.service.UserService;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 空间成员权限管理
 */
@Component
public class SpaceUserAuthManager {

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private SpaceService spaceService;

    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    static {
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     *
     * @param spaceUserRole
     * @return
     */
    public List<String> getPermissionsByRole(String spaceUserRole) {
        if (StrUtil.isBlank(spaceUserRole)) {
            return new ArrayList<>();
        }
        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles()
                .stream()
                .filter(r -> r.getKey().equals(spaceUserRole))
                .findFirst()
                .orElse(null);
        if (role == null) {
            return new ArrayList<>();
        }
        return role.getPermissions();
    }


    /**
     * 获取权限列表
     *
     * @param space
     * @param loginUser
     * @return
     */
    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 管理员权限
        List<String> ADMIN_PERMISSIONS = getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        // 根据空间获取对应的权限
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间，仅本人或管理员有所有权限
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                // 团队空间，查询 SpaceUser 并获取角色和权限
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return getPermissionsByRole(spaceUser.getSpaceRole());
                }
        }
        return new ArrayList<>();
    }

    /**
     * 检查用户对指定图片是否拥有某项权限
     *
     * @param picture 图片对象
     * @param loginUser 当前登录用户
     * @param permission 需要校验的权限
     * @return 是否拥有该权限
     */
    public boolean checkPicturePermission(Picture picture, User loginUser, String permission) {
        if (picture == null || loginUser == null) {
            return false;
        }

        Long spaceId = picture.getSpaceId();
        Space space = null;

        // 如果图片属于某个空间，查询空间信息
        if (spaceId != null) {
            space = spaceService.getById(spaceId);
        }

        // 公共图库的图片，仅本人或管理员可操作
        if (space == null) {
            if (picture.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                return true;
            }
            // 如果是查看权限，允许所有人查看
            return SpaceUserPermissionConstant.PICTURE_VIEW.equals(permission);
        }

        // 获取用户在该空间的权限列表
        List<String> permissions = getPermissionList(space, loginUser);

        // 检查是否包含所需权限
        return permissions.contains(permission);
    }
}
