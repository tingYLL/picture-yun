package com.jdjm.jdjmpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jdjm.jdjmpicturebackend.constant.UserConstant;
import com.jdjm.jdjmpicturebackend.exception.BusinessException;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.manager.auth.StpKit;
import com.jdjm.jdjmpicturebackend.model.dto.user.UserEditPasswordRequest;
import com.jdjm.jdjmpicturebackend.model.dto.user.UserQueryRequest;
import com.jdjm.jdjmpicturebackend.model.dto.user.UserUpdateRequest;
import com.jdjm.jdjmpicturebackend.model.entity.User;
import com.jdjm.jdjmpicturebackend.model.enums.UserDisabledEnum;
import com.jdjm.jdjmpicturebackend.model.enums.UserRoleEnum;
import com.jdjm.jdjmpicturebackend.model.vo.LoginUserVO;
import com.jdjm.jdjmpicturebackend.model.vo.UserVO;
import com.jdjm.jdjmpicturebackend.service.UserService;
import com.jdjm.jdjmpicturebackend.mapper.UserMapper;
import com.jdjm.jdjmpicturebackend.service.VIPService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jdjm.jdjmpicturebackend.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author jdjm
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-03-22 17:57:20
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Value("${image.upload.dir}")
    private String uploadDir; // 注入配置的上传目录

    @Value("${server.port}")
    private String port;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${image.local.enable}")
    private Boolean isLocalStore;

    @Resource
    private VIPService vipService;
//    @Resource
//    private RedisTemplate<String, Object> redisTemplate;
//    private RedisOperationsSessionRepository sessionRepository; // Spring Session提供的操作接口
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() <= 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号长度必须大于4位");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2. 检查用户账号是否和数据库中已有的重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3. 密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据到数据库中
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
//        String random = RandomUtil.randomString(6);
//        user.setUserName("用户_"+random);
        user.setUserName(userAccount);
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码错误");
        }
        // 2. 对用户传递的密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 查询数据库中的用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 不存在，抛异常
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或者密码错误");
        }
        // 判断是否被禁用
        if (UserDisabledEnum.isDisabled(user.getIsDisabled())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "用户已被禁用");
        }
        // 4. 记录用户登录态到Sa-Token，统一使用Sa-Token管理会话
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);
        return this.getUserVO(user);
    }

    /**
     * 获取加密后的密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 加盐，混淆密码
        final String SALT = "Bear";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if(user==null)
        {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user,loginUserVO);
        if(isLocalStore||!user.getUserAvatar().startsWith("http")){
            //如果开启了本地存储，或者用户头像不是外链，那么需要拼接上本地存储路径
            loginUserVO.setUserAvatar("http://localhost:"+port+contextPath+loginUserVO.getUserAvatar());
        }
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if(user==null)
        {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user,userVO);
        if(isLocalStore||!user.getUserAvatar().startsWith("http")){
            //如果开启了本地存储，或者用户头像不是外链，那么需要拼接上本地存储路径
            userVO.setUserAvatar("http://localhost:"+port+contextPath+user.getUserAvatar());
        }
        // 从 vip_memberships 表获取 VIP 状态
        userVO.setIsVip(vipService.isVIP(user.getId()));
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if(CollectionUtil.isEmpty(userList))
        {
            return new ArrayList<>();
        }
        List<UserVO> collect = userList.stream().map(user -> getUserVO(user)).collect(Collectors.toList());
        return collect;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 优先从Sa-Token Session中获取用户信息
        Object userObj = null;
        try {
            userObj = StpKit.SPACE.getSession().get(USER_LOGIN_STATE);
        } catch (Exception e) {
            // Sa-Token未登录，忽略异常
        }

        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库中查询（追求性能的话可以注释，直接返回上述结果）
        //为什么这里需要重新查找？因为session会话里面的用户信息可能是过期的，如用户在页面上修改了个人信息，数据库中信息有更新，但是session中还是旧数据
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 优先从Sa-Token中获取登录状态
        User loginUser = null;
        try {
            Object userObj = StpKit.SPACE.getSession().get(USER_LOGIN_STATE);
            loginUser = (User) userObj;
        } catch (Exception e) {
            // Sa-Token未登录，尝试从传统Session获取
            Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
            loginUser = (User) userObj;
        }

        if (loginUser == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }

        // 移除Sa-Token登录态
        try {
            StpKit.SPACE.logout(loginUser.getId());
        } catch (Exception e) {
            // 忽略Sa-Token登出异常
        }

        // 移除传统Session登录态（兼容性处理，但不应该再被执行）
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        String userEmail = userQueryRequest.getUserEmail();
        String userPhone = userQueryRequest.getUserPhone();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.eq(StrUtil.isNotBlank(userEmail), "userEmail", userEmail);
        queryWrapper.eq(StrUtil.isNotBlank(userPhone), "userPhone", userPhone);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public void editUserPassword(UserEditPasswordRequest userEditPasswordRequest,User user) {

        String encryptOriginPassword = getEncryptPassword(userEditPasswordRequest.getOriginPassword());
        if(!user.getUserPassword().equals(encryptOriginPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "原密码错误");
        }
        user.setUserPassword(getEncryptPassword(userEditPasswordRequest.getNewPassword()));
        try{
            saveOrUpdate(user);
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            userLogout(request);
        }catch (DataAccessException e){
            log.error("更新用户密码失败. User: {}", user.getId(), e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统数据错误，修改密码失败");
        }catch (Exception e){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"修改密码失败");
        }
    }

    @Override
    public String uploadAvatar(MultipartFile avatarFile,User user) {
        Long userId = user.getId();
        String uploadPathPrefix = String.format("avatar/%s", userId);
        File destFile;
        String uploadPath;
        String originalFilename;
        try{
            //拼接图片上传地址
            String uuid = RandomUtil.randomString(16);
            originalFilename = avatarFile.getOriginalFilename();
            // 为避免用户上传图片的名称中带有特殊字符，如 & ? 引起浏览器url解析异常
            // 转换文件上传路径，而不是使用原始文件名称，增强安全性
            String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                    FileUtil.getSuffix(originalFilename));
            uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
            destFile = new File(Paths.get(uploadDir, uploadPath).toString());

            // 确保目录存在
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }

            // 保存文件
            avatarFile.transferTo(destFile);
            user.setUserAvatar("/images"+uploadPath);
            boolean result = this.updateById(user);
            if(!result)
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"头像更新失败");
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"头像更新失败");
        }
//        redisCache.delete(UserConstant.USER_LOGIN_STATE + StpUtil.getLoginIdAsLong());
        return "http://localhost:"+ port + contextPath + "/images"+uploadPath;
    }


    @Override
    public void convertUserAvatar(Object object) {
        if (object instanceof LoginUserVO){
            LoginUserVO loginUserVO = (LoginUserVO) object;
            String userAvatar = "http://localhost:"+port+contextPath+loginUserVO.getUserAvatar();
            loginUserVO.setUserAvatar(userAvatar);
        }else if (object instanceof UserVO){
            UserVO userVO = (UserVO) object;
            String userAvatar = "http://localhost:"+port+contextPath+userVO.getUserAvatar();
            userVO.setUserAvatar(userAvatar);
        }else {
            User user = (User) object;
            String userAvatar = "http://localhost:"+port+contextPath+user.getUserAvatar();
            user.setUserAvatar(userAvatar);
        }
    }

    @Override
    public void disabledUser(Long id, Integer isDisabled) {
        boolean existed = this.getBaseMapper()
                .exists(new QueryWrapper<User>()
                        .eq("id", id)
                );
        if (!existed) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在!");
        }
        User user = new User();
        user.setId(id);
        user.setIsDisabled(isDisabled);
        boolean result = this.updateById(user);
        if (result) {
//            redisCache.delete(UserConstant.USER_LOGIN_STATE + StpUtil.getLoginIdAsLong());
            return;
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "禁用失败!");
    }

    @Override
    public void checkUserAccountAndPhoneUnique(UserUpdateRequest userUpdateRequest) {
        Long userId = userUpdateRequest.getId();
        String userAccount = userUpdateRequest.getUserAccount();
        String userPhone = userUpdateRequest.getUserPhone();

        // 检查用户账号唯一性（如果要更新的账号不为空）
        if (StrUtil.isNotBlank(userAccount)) {
            QueryWrapper<User> accountQuery = new QueryWrapper<>();
            accountQuery.eq("userAccount", userAccount);
            accountQuery.ne("id", userId); // 排除当前用户
            long accountCount = this.count(accountQuery);
            if (accountCount > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号已存在");
            }
        }

        // 检查手机号唯一性（如果要更新的手机号不为空）
        if (StrUtil.isNotBlank(userPhone)) {
            QueryWrapper<User> phoneQuery = new QueryWrapper<>();
            phoneQuery.eq("userPhone", userPhone);
            phoneQuery.ne("id", userId); // 排除当前用户
            long phoneCount = this.count(phoneQuery);
            if (phoneCount > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号已存在");
            }
        }
    }

//    @Override
//    public String resetPassword(Long id) {
//        boolean existed = this.getBaseMapper()
//                .exists(new QueryWrapper<User>()
//                        .eq("id", id)
//                );
//        if (!existed) {
//            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在!");
//        }
//        String tempPassword = RandomUtil.randomString(8);
//        User user = new User();
//        user.setId(id);
//        // 3. 密码一定要加密
//        String encryptPassword = getEncryptPassword(tempPassword);
//        user.setUserPassword(encryptPassword);
//        boolean result = this.updateById(user);
//        if (!result) {
//            throw new BusinessException(ErrorCode.OPERATION_ERROR, "重置密码失败");
//        }
//        User loginUser = this.getById(id);
//        StpKit.SPACE.logout(loginUser.getId());
//        String userSessionKey = "user:sessions:" + loginUser.getId();
//        //获取被重置密码的用户的SessionId
//        Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionKey);
//        if(sessionIds != null){
//            //移除该用户的所有会话
//            for (Object sessionIdObj : sessionIds) {
//                String sessionId = (String) sessionIdObj;
//                sessionRepository.deleteById(sessionId);
//            }
//        }
//        //最后删除这个记录集合本身
//        redisTemplate.delete(userSessionKey);
//        return tempPassword;
//    }

}




