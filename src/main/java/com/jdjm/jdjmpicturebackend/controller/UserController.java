package com.jdjm.jdjmpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jdjm.jdjmpicturebackend.annotation.AuthCheck;
import com.jdjm.jdjmpicturebackend.common.BaseResponse;
import com.jdjm.jdjmpicturebackend.common.DeleteRequest;
import com.jdjm.jdjmpicturebackend.common.ResultUtils;
import com.jdjm.jdjmpicturebackend.constant.UserConstant;
import com.jdjm.jdjmpicturebackend.exception.BusinessException;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.exception.ThrowUtils;
import com.jdjm.jdjmpicturebackend.model.dto.user.*;
import com.jdjm.jdjmpicturebackend.model.entity.User;
import com.jdjm.jdjmpicturebackend.model.vo.LoginUserVO;
import com.jdjm.jdjmpicturebackend.model.vo.UserVO;
import com.jdjm.jdjmpicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        UserVO userVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(userVO);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        UserVO userVO = userService.getUserVO(user);
        //返回脱敏后的用户信息
        return ResultUtils.success(userVO);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 上传头像
     *
     * @param avatarFile 头像文件
     * @return 头像地址
     */
    @PostMapping("/uploadAvatar")
    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile avatarFile,HttpServletRequest request) {
        ThrowUtils.throwIf(avatarFile == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.uploadAvatar(avatarFile,loginUser));
    }

    @PostMapping("/password")
    public BaseResponse<Boolean> editUserPassword(@RequestBody UserEditPasswordRequest userEditPasswordRequest,HttpServletRequest request){
        ThrowUtils.throwIf(userEditPasswordRequest == null, ErrorCode.PARAMS_ERROR);
        if (StrUtil.hasBlank(userEditPasswordRequest.getOriginPassword(), userEditPasswordRequest.getNewPassword(),
                userEditPasswordRequest.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能为空");
        }
        if (!userEditPasswordRequest.getNewPassword().equals(userEditPasswordRequest.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次新密码不一致");
        }
        User loginUser = userService.getLoginUser(request);
        userService.editUserPassword(userEditPasswordRequest,loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 重置用户密码
     *
     * @param userUpdateRequest 用户更新请求
     * @return 新密码
     */
//    @PostMapping("/resetPassword")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<String> resetPassword(@RequestBody UserUpdateRequest userUpdateRequest) {
//        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
//        Long id = userUpdateRequest.getId();
//        ThrowUtils.throwIf(ObjectUtil.isEmpty(id), ErrorCode.PARAMS_ERROR);
//        return ResultUtils.success(userService.resetPassword(id));
//    }
    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        // 默认密码
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        // 插入数据库
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        userService.convertUserAvatar(user);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
//        BaseResponse<User> response = getUserById(id);
//        User user = response.getData();
        User user = userService.getById(id);
        UserVO userVO = userService.getUserVO(user);
        return ResultUtils.success(userVO);
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userPhone = userUpdateRequest.getUserPhone();
        if (StrUtil.isNotEmpty(userPhone) && userPhone.length() != 11) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号码格式错误");
        }
        String userProfile = userUpdateRequest.getUserProfile();
        if (StrUtil.isNotEmpty(userProfile) && userProfile.length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户简介过长");
        }
        String userAccount = userUpdateRequest.getUserAccount();
        if (userAccount.length() <= 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号长度必须大于4位");
        }

        // 检查用户账号和手机号的唯一性
        userService.checkUserAccountAndPhoneUnique(userUpdateRequest);

        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 禁用用户
     *
     * @param userUpdateRequest 用户更新请求
     * @return 是否成功
     */
    @PostMapping("/disabledUser")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> disabledUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = userUpdateRequest.getId();
        ThrowUtils.throwIf(ObjectUtil.isEmpty(id), ErrorCode.PARAMS_ERROR);
        Integer isDisabled = userUpdateRequest.getIsDisabled();
        ThrowUtils.throwIf(ObjectUtil.isEmpty(isDisabled), ErrorCode.PARAMS_ERROR);
        userService.disabledUser(id, isDisabled);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 兑换码兑换余额
     */
    @PostMapping("/redeemCode")
    public BaseResponse<String> redeemCode(@RequestParam String code, HttpServletRequest request) {
        ThrowUtils.throwIf(StrUtil.isBlank(code), ErrorCode.PARAMS_ERROR, "兑换码不能为空");
        User loginUser = userService.getLoginUser(request);
        // 兑换码为 "jdjm"，兑换成功增加 10 元）
        if ("jdjm".equals(code)) {
            loginUser.setBalance(loginUser.getBalance() + 10);
            userService.updateById(loginUser);
            return ResultUtils.success("兑换成功");
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的兑换码");
        }
    }

}
