package com.jdjm.jdjmpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jdjm.jdjmpicturebackend.api.aliyunai.AliYunAiApi;
import com.jdjm.jdjmpicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.jdjm.jdjmpicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.jdjm.jdjmpicturebackend.common.PageRequest;
import com.jdjm.jdjmpicturebackend.constant.CacheKeyConstant;
import com.jdjm.jdjmpicturebackend.constant.UserConstant;
import com.jdjm.jdjmpicturebackend.exception.BusinessException;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.exception.ThrowUtils;
import com.jdjm.jdjmpicturebackend.manager.CosManager;
import com.jdjm.jdjmpicturebackend.manager.FileManager;
import com.jdjm.jdjmpicturebackend.manager.auth.SpaceUserAuthManager;
import com.jdjm.jdjmpicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.jdjm.jdjmpicturebackend.manager.redis.RedisCache;
import com.jdjm.jdjmpicturebackend.manager.upload.FilePictureUpload;
import com.jdjm.jdjmpicturebackend.manager.upload.PictureUploadTemplate;
import com.jdjm.jdjmpicturebackend.manager.upload.UrlPictureUpload;
import com.jdjm.jdjmpicturebackend.model.dto.file.UploadPictureResult;
import com.jdjm.jdjmpicturebackend.model.dto.picture.*;
import com.jdjm.jdjmpicturebackend.model.entity.*;
import com.jdjm.jdjmpicturebackend.model.enums.PictureInteractionStatusEnum;
import com.jdjm.jdjmpicturebackend.model.enums.PictureInteractionTypeEnum;
import com.jdjm.jdjmpicturebackend.model.enums.PictureReviewStatusEnum;
import com.jdjm.jdjmpicturebackend.model.enums.UserRoleEnum;
import com.jdjm.jdjmpicturebackend.model.vo.PictureVO;
import com.jdjm.jdjmpicturebackend.model.vo.UserVO;
import com.jdjm.jdjmpicturebackend.service.*;
import com.jdjm.jdjmpicturebackend.mapper.PictureMapper;
import com.jdjm.jdjmpicturebackend.utils.ColorSimilarUtils;
import com.jdjm.jdjmpicturebackend.utils.ColorTransformUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author jdjm
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-03-30 18:00:15
*/
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    private FileManager fileManager;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private UserService userService;
    @Resource
    private CategoryService categoryService;

    @Resource
    private SpaceService spaceService;

    @Autowired
    private CosManager cosManager;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private AliYunAiApi aliYunAiApi;
    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;
    @Value("${image.local.enable}")
    private Boolean isLocalStore;
    @Value("${server.port}")
    private String port;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${image.upload.dir}")
    private String uploadDir; // 注入配置的上传目录

    @Resource
    private RedisCache redisCache;
    private PictureMapper pictureMapper;
    @Autowired
    private PictureInteractionService pictureInteractionService;
    @Autowired
    private PictureVO pictureVO;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        // 如果传递了 url，才校验
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 300, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public PictureVO uploadLocal(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 校验空间是否存在
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
//            // 校验是否有空间的权限，仅空间管理员才能上传
//            if (!loginUser.getId().equals(space.getUserId())) {
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
//            }
            //校验额度
            if (space.getTotalCount() >= space.getMaxCount()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数不足");
            }
            if (space.getTotalSize() >= space.getMaxSize()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
            }
        }
        //判断是新增还是删除
        Long pictureId = null;
        if(pictureUploadRequest !=null){
            pictureId = pictureUploadRequest.getId();
        }
        //如果是更新，先判断图片是否存在
        if(pictureId != null){
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR,"图片不存在");
            //仅图片创建者本人或管理员可编辑图片
//            if(!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)){
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//            }
            // 校验空间是否一致
            // 没传 spaceId，则复用原有图片的 spaceId（这样也兼容了公共图库）
            if (spaceId == null) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                // 传了 spaceId，必须和原图片的空间 id 一致
                if (ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间 id 不一致");
                }
            }
        }
        //上传图片
        // 按照用户 id 划分目录 => 按照空间划分目录
        String uploadPathPrefix;
        if (spaceId == null) {
            // 公共图库
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        } else {
            // 空间
            uploadPathPrefix = String.format("space/%s", spaceId);
        }

        //校验图片
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 1. 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024;
        if(userService.isAdmin(loginUser)){
            ThrowUtils.throwIf(fileSize > 5 * ONE_M, ErrorCode.PARAMS_ERROR, "管理员上传的文件大小不能超过 5MB");
        }else{
            ThrowUtils.throwIf(fileSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2MB");
        }
        // 2. 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 允许上传的文件后缀列表（或者集合）
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "png", "jpg", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");

        int width = 0;
        int height = 0;
        try {
            BufferedImage image = ImageIO.read(multipartFile.getInputStream());
            if (image == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传的文件不是有效的图片格式");
            }
            width = image.getWidth();
            height = image.getHeight();

            // 可以添加图片尺寸的校验逻辑
            if (width <= 0 || height <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片尺寸异常");
            }

            // 可选：添加最大最小尺寸限制
            if (width > 10000 || height > 10000) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片尺寸过大");
            }

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "读取图片信息失败");
        }

        File destFile;
        String uploadPath;
        String originalFilename;
        try{
            //拼接图片上传地址
            String uuid = RandomUtil.randomString(16);
            originalFilename = multipartFile.getOriginalFilename();
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
            multipartFile.transferTo(destFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"文件上传失败");
        }
        //构造要入库的图片信息
        Picture picture = new Picture();
        picture.setName(originalFilename);
        picture.setSpaceId(spaceId); //指定空间id
        picture.setUrl("/images"+uploadPath);
        picture.setUserId(loginUser.getId());
        picture.setPicSize(multipartFile.getSize());
        picture.setPicFormat(FileUtil.getSuffix(originalFilename));
        picture.setPicWidth(width);
        picture.setPicHeight(height);
        //补充审核参数
        fillReviewParams(picture,loginUser);
        //操作数据库
        if(pictureId != null){
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            boolean result =this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"图片上传失败，数据库操作失败");
            if(finalSpaceId !=null){
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalCount = totalCount + 1")
                        .setSql("totalSize = totalSize + " + picture.getPicSize())
                        .update();
                ThrowUtils.throwIf(!update,ErrorCode.OPERATION_ERROR,"额度更新失败" );
            }
            return picture;
        });
        //清理首页图片缓存
        if(spaceId == null){
            //说明是上传至公共图库 需要清空图片缓存
            Set<String> keys = redisTemplate.keys("HOME_PICTURE_LIST_KEY:" + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
        return  PictureVO.objToVo(picture);
    }

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 校验空间是否存在
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
//            // 校验是否有空间的权限，仅空间管理员才能上传
//            if (!loginUser.getId().equals(space.getUserId())) {
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
//            }
            //校验额度
            if (space.getTotalCount() >= space.getMaxCount()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数不足");
            }
            if (space.getTotalSize() >= space.getMaxSize()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
            }
        }
        //判断是新增还是删除
        Long pictureId = null;
        if(pictureUploadRequest !=null){
            pictureId = pictureUploadRequest.getId();
        }
        //如果是更新，先判断图片是否存在
        if(pictureId != null){
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR,"图片不存在");
            //仅图片创建者本人或管理员可编辑图片
//            if(!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)){
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//            }
            // 校验空间是否一致
            // 没传 spaceId，则复用原有图片的 spaceId（这样也兼容了公共图库）
            if (spaceId == null) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                // 传了 spaceId，必须和原图片的空间 id 一致
                if (ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间 id 不一致");
                }
            }
        }
        //上传图片
        // 按照用户 id 划分目录 => 按照空间划分目录
        String uploadPathPrefix;
        if (spaceId == null) {
            // 公共图库
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        } else {
            // 空间
            uploadPathPrefix = String.format("space/%s", spaceId);
        }
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if(inputSource instanceof  String){
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix,loginUser);
        //构造要入库的图片信息
        Picture picture = new Picture();
        picture.setSpaceId(spaceId); //指定空间id
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        //默认从图片解析服务中获取名称，如果外层传入了图片名称，则使用传入的名称
        String picName = uploadPictureResult.getPicName();
        if(pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())){
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
//        picture.setPicColor(uploadPictureResult.getPicColor());
        picture.setPicColor(ColorTransformUtils.getStandardColor(uploadPictureResult.getPicColor()));
        picture.setUserId(loginUser.getId());
        //补充审核参数
        fillReviewParams(picture,loginUser);
        //操作数据库
        if(pictureId != null){
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            boolean result =this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"图片上传失败，数据库操作失败");
            if(finalSpaceId !=null){
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalCount = totalCount + 1")
                        .setSql("totalSize = totalSize + " + picture.getPicSize())
                        .update();
                ThrowUtils.throwIf(!update,ErrorCode.OPERATION_ERROR,"额度更新失败" );
            }
            return picture;
        });
        return PictureVO.objToVo(picture);
    }


    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        // 校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "最多 30 条");
        //批量抓取的图片名称可能五花八门，这里需要统一一个图片名称前缀，默认为搜索关键词
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        // 抓取内容
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        // 解析内容
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isEmpty(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElementList = div.select("img.mimg");
        // 遍历元素，依次处理上传图片
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过：{}", fileUrl);
                continue;
            }
            // 处理图片的地址，防止转义或者和对象存储冲突的问题
            // www.baidu.com?name=dog，应该只保留 www.baidu.com
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(fileUrl);
            pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功，id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }


    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        Long categoryId = pictureQueryRequest.getCategoryId();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        // 从多字段中搜索
//        if (StrUtil.isNotBlank(searchText)) {
//            // 需要拼接查询条件
//            // and (name like "%xxx%" or introduction like "%xxx%")
//            queryWrapper.and(
//                    qw -> qw.like("name", searchText)
//                            .or()
//                            .like("introduction", searchText)
//            );
//        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotNull(categoryId), "categoryId", categoryId);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        // >= startEditTime
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        // < endEditTime
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);
//        if (CollUtil.isNotEmpty(tags)) {
////            多次调用like会默认以AND连接   最终生成条件 and tags like '%"Java"%' and  tags like '%"Python"%'
//            for (String tag : tags) {
//                queryWrapper.like("tags", "\"" + tag + "\"");
//            }
//        }

//        queryWrapper.and(StrUtil.isNotEmpty(searchText), qw ->
//                qw.like("name", searchText)
//                        .or().like("name", searchText)
//                        .or().like("introduction", searchText)
//                        .or().apply("FIND_IN_SET ('" + searchText + "', tags) > 0")
//        );

        queryWrapper.and(StrUtil.isNotEmpty(searchText), qw -> {
            String lowerSearchText = searchText.toLowerCase();
            // 对于 name 和 introduction 字段
            qw.like("LOWER(name)", lowerSearchText)
                    .or().like("LOWER(introduction)", lowerSearchText)
                    // 对于 tags 字段，使用更安全的参数化方式
                    .or().apply("FIND_IN_SET ({0}, LOWER(tags)) > 0", lowerSearchText);
        });


        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapperMultiple(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        Long categoryId = pictureQueryRequest.getCategoryId();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
//        String sortField = pictureQueryRequest.getSortField();
//        String sortOrder = pictureQueryRequest.getSortOrder();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotNull(categoryId), "categoryId", categoryId);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        // >= startEditTime
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        // < endEditTime
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);
        queryWrapper.and(StrUtil.isNotEmpty(searchText), qw -> {
            String lowerSearchText = searchText.toLowerCase();
            // 对于 name 和 introduction 字段
            qw.like("LOWER(name)", lowerSearchText)
                    .or().like("LOWER(introduction)", lowerSearchText)
                    // 对于 tags 字段，使用更安全的参数化方式
                    .or().apply("FIND_IN_SET ({0}, LOWER(tags)) > 0", lowerSearchText);
        });


        // 排序
        // 处理排序规则
        if (pictureQueryRequest.isMultipleSort()) {
            List<PageRequest.Sort> sorts = pictureQueryRequest.getSorts();
            if (CollUtil.isNotEmpty(sorts)) {
                sorts.forEach(sort -> {
                    String sortField = sort.getField();
                    boolean sortAsc = sort.isAsc();
                    queryWrapper.orderBy(
                            StrUtil.isNotEmpty(sortField), sortAsc, sortField
                    );
                });
            }
        }else {
            PageRequest.Sort sort = pictureQueryRequest.getSort();
            if (sort != null) {
                String sortField = sort.getField();
                boolean sortAsc = sort.isAsc();
                queryWrapper.orderBy(
                        StrUtil.isNotEmpty(sortField), sortAsc, sortField
                );
            } else {
                queryWrapper.orderByDesc("createTime");
            }
        }
        return queryWrapper;
    }


    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        //查询并填充分类信息
        Long categoryId = pictureVO.getCategoryId();
        if(categoryId!=null){
            Category category = categoryService.getById(categoryId);
            pictureVO.setCategoryInfo(category);
        }
        return pictureVO;
    }

    /**
     * 分页获取图片封装
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
        //查询分类信息
        Set<Long> categoryIds = pictureList.stream().map(Picture::getCategoryId).collect(Collectors.toSet());
        List<Category> categoryList = categoryService.listByIds(categoryIds);
        Map<Long, List<Category>> categoryListMap = categoryList.stream().collect(Collectors.groupingBy(Category::getId));

        //查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        // 1 => user1, 2 => user2
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 查询当前登录用户对该图片的 点赞和收藏 信息
        Map<Long, Boolean> likeMap = new HashMap<>();
        Map<Long, Boolean> collectMap = new HashMap<>();
//        避免未登录导致直接抛出异常
//        User loginUser = userService.getLoginUser(request);
        // 判断是否已经登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User loginUser = (User) userObj;
        if (loginUser != null && loginUser.getId() != null) {
            List<Long> pictureIds = pictureVOList.stream().map(PictureVO::getId).collect(Collectors.toList());
            List<PictureInteraction> pictureInteractions = pictureInteractionService.lambdaQuery()
                    .in(PictureInteraction::getPictureId, pictureIds)
                    .eq(PictureInteraction::getUserId, loginUser.getId()).list();
            if(CollUtil.isNotEmpty(pictureInteractions)){
                for (PictureInteraction p : pictureInteractions) {
                    if (PictureInteractionTypeEnum.LIKE.getKey().equals(p.getInteractionType()) &&
                            PictureInteractionStatusEnum.isExisted(p.getInteractionStatus())) {
                        likeMap.put(p.getPictureId(), true);
                    }
                    if (PictureInteractionTypeEnum.COLLECT.getKey().equals(p.getInteractionType()) &&
                            PictureInteractionStatusEnum.isExisted(p.getInteractionStatus())) {
                        collectMap.put(p.getPictureId(), true);
                    }
                }
            }
        }
        //填充信息
        pictureVOList.forEach(pictureVO -> {
            //填充用户信息
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
            //填充分类信息
            Long categoryId = pictureVO.getCategoryId();
            if(categoryListMap.containsKey(categoryId)){
                pictureVO.setCategoryInfo(categoryListMap.get(categoryId).get(0));
            }
            // 设置当前登录用户点赞和收藏信息
            pictureVO.setLoginUserIsLike(likeMap.getOrDefault(pictureVO.getId(), false));
            pictureVO.setLoginUserIsCollect(collectMap.getOrDefault(pictureVO.getId(), false));
            fillPictureInteraction(pictureVO);
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 判断图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 3. 校验审核状态是否重复，已是改状态
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
        }
        // 4. 数据库操作
        Picture updatePicture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        //清理首页图片缓存
        Set<String> keys = redisTemplate.keys("HOME_PICTURE_LIST_KEY:" + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }


    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(new Date());
        } else {
            // 非管理员，无论是编辑还是创建默认都是待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }


    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        Long loginUserId = loginUser.getId();
        if (spaceId == null) {
            // 公共图库，仅本人或管理员可操作
            if (!picture.getUserId().equals(loginUserId) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            // 私有空间，仅空间管理员可操作
            if (!picture.getUserId().equals(loginUserId)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

    @Override
    public void deletePicture(long pictureId, User loginUser) {
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 判断是否存在
        Picture oldPicture = this.getById(pictureId);
        Long spaceId = oldPicture.getSpaceId();
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //!!!已经改为使用注解鉴权
//        checkPictureAuth(loginUser, oldPicture);
        // 开启事务
        transactionTemplate.execute(status -> {
            // 操作数据库
            boolean result = this.removeById(pictureId);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            // 删除图片交互记录
            boolean interactionDeleteResult = pictureInteractionService.lambdaUpdate()
                    .eq(PictureInteraction::getPictureId, pictureId)
                    .remove();
            //返回false 并不代表删除失败 可能是picture_interaction表中没有对应记录
//            ThrowUtils.throwIf(!interactionDeleteResult, ErrorCode.OPERATION_ERROR, "删除图片交互记录失败");
            if(spaceId != null){
                // 更新空间的使用额度，释放额度
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, oldPicture.getSpaceId())
                        .setSql("totalSize = totalSize - " + oldPicture.getPicSize())
                        .setSql("totalCount = totalCount - 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return true;
        });
        // 异步清理文件
        this.clearPictureFileLocal(oldPicture);
        //清理首页图片缓存
        if(spaceId == null){
            //公共图库才需要删除首页缓存
            Set<String> keys = redisTemplate.keys("HOME_PICTURE_LIST_KEY:" + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
        //清除redis中图片交互数据
        String key = CacheKeyConstant.PICTURE_INTERACTION_KEY_PREFIX + pictureId;
        redisTemplate.delete(key);
    }

    /**
     * 批量删除图片
     * @param pictureIds 图片ID列表
     * @param loginUser 当前登录用户
     */
    @Override
    public void deletePictureByBatch(List<Long> pictureIds, User loginUser) {
        ThrowUtils.throwIf(CollUtil.isEmpty(pictureIds), ErrorCode.PARAMS_ERROR, "图片ID列表不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户未登录");

        // 逐个校验每张图片的删除权限
        List<Picture> picturesToDelete = new ArrayList<>();
        for (Long pictureId : pictureIds) {
            ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR, "图片ID不合法");

            // 查询图片信息
            Picture picture = this.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .select(Picture::getId, Picture::getSpaceId, Picture::getUserId, Picture::getPicSize, Picture::getUrl)
                    .one();

            ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片ID " + pictureId + " 不存在");

            // 编程式权限校验
            boolean hasPermission = spaceUserAuthManager.checkPicturePermission(
                    picture, loginUser, SpaceUserPermissionConstant.PICTURE_DELETE);

            ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR,
                    "没有权限删除图片ID: " + pictureId);

            picturesToDelete.add(picture);
        }

        // 开启事务，批量删除
        transactionTemplate.execute(status -> {
            // 批量删除图片记录
            boolean result = this.removeByIds(pictureIds);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "批量删除图片失败");

            // 批量删除图片交互记录
            boolean interactionDeleteResult = pictureInteractionService.lambdaUpdate()
                    .in(PictureInteraction::getPictureId, pictureIds)
                    .remove();
            //跟单条删除一样，如果一条记录都没有删除 不应该报错 说明用户没有对这张图片进行过点赞or收藏操作， 是正常的
//            ThrowUtils.throwIf(!interactionDeleteResult, ErrorCode.OPERATION_ERROR, "批量删除图片交互记录失败");
            // 按空间分组，更新空间使用额度
            Map<Long, List<Picture>> spaceGroupMap = picturesToDelete.stream()
                    .filter(p -> p.getSpaceId() != null)
                    .collect(Collectors.groupingBy(Picture::getSpaceId));

            for (Map.Entry<Long, List<Picture>> entry : spaceGroupMap.entrySet()) {
                Long spaceId = entry.getKey();
                List<Picture> pictures = entry.getValue();

                // 计算该空间下删除的图片总大小
                long totalSize = pictures.stream()
                        .mapToLong(p -> p.getPicSize() != null ? p.getPicSize() : 0L)
                        .sum();
                int count = pictures.size();

                // 更新空间的使用额度
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, spaceId)
                        .setSql("totalSize = totalSize - " + totalSize)
                        .setSql("totalCount = totalCount - " + count)
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "空间额度更新失败");
            }

            return true;
        });

        // 异步清理文件和缓存
        for (Picture picture : picturesToDelete) {
            // 异步清理文件
            this.clearPictureFileLocal(picture);

            // 清理首页图片缓存（公共图库）
            if (picture.getSpaceId() == null) {
                Set<String> keys = redisTemplate.keys("HOME_PICTURE_LIST_KEY:" + "*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            }

            // 清除redis中图片交互数据
            String interactionKey = CacheKeyConstant.PICTURE_INTERACTION_KEY_PREFIX + picture.getId();
            redisTemplate.delete(interactionKey);
        }
    }

    /**
     * 删除本地图片
     * @param oldPicture
     */
    @Async
    @Override
    public void clearPictureFileLocal(Picture oldPicture) {
        String pictureUrl = oldPicture.getUrl();
        try {
            // 提取相对路径
            String relativePath = pictureUrl.startsWith("/images")
                    ? pictureUrl.substring("/images".length())
                    : pictureUrl;

            // 构建完整路径
            Path filePath = buildFilePath(relativePath);

            // 使用Files.deleteIfExists()，更安全的方法
            boolean deleted = Files.deleteIfExists(filePath);

            if (deleted) {
                log.info("图片文件删除成功: {}", filePath);
            } else {
                log.info("图片文件不存在，无需删除: {}", filePath);
            }

        } catch (IOException e) {
            log.error("删除图片文件时发生IO异常: {}", pictureUrl, e);
        } catch (Exception e) {
            log.error("删除图片文件时发生未知错误: {}", pictureUrl, e);
        }
    }

    /**
     * 使用NIO Path构建文件路径
     */
    private Path buildFilePath(String relativePath) {
        // 清理路径中的开头斜杠
        if (relativePath.startsWith("/") || relativePath.startsWith(File.separator)) {
            relativePath = relativePath.substring(1);
        }

        return Paths.get(uploadDir, relativePath.split("/"));
    }

    //腾讯云cos
    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        // 判断改图片是否被多条记录使用
        String pictureUrl = oldPicture.getUrl();
        long count = this.lambdaQuery()
                .eq(Picture::getUrl, pictureUrl)
                .count();
        // 有不止一条记录用到了该图片，不清理
        if (count > 1) {
            return;
        }
        // 删除图片
        cosManager.deleteObject(pictureUrl);
        // 删除缩略图
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }

    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
//        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        List<String> tags = pictureEditRequest.getTags();
        if (CollUtil.isNotEmpty(tags)) {
            picture.setTags(String.join(",", tags));
        }
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        this.validPicture(picture);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //!!!已经改为使用注解鉴权
//        checkPictureAuth(loginUser, oldPicture);
        // 补充审核参数
        this.fillReviewParams(picture, loginUser);
        // 操作数据库
//        boolean result = this.updateById(picture);
        boolean result = lambdaUpdate().eq(Picture::getId, pictureEditRequest.getId())
                .set(Picture::getName, pictureEditRequest.getName())
                .set(Picture::getCategoryId, pictureEditRequest.getCategoryId())
                .set(Picture::getTags, picture.getTags())
                .set(Picture::getIntroduction, pictureEditRequest.getIntroduction())
                .set(Picture::getEditTime, new Date()).update();
//        if(CollUtil.isEmpty(pictureEditRequest.getTags())){
//            LambdaUpdateWrapper<Picture> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
//            lambdaUpdateWrapper.eq(Picture::getId,picture.getId()).set(Picture::getTags,null);
//            this.update(lambdaUpdateWrapper);
//        }
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        //清理首页图片缓存
        if(oldPicture.getSpaceId()==null){
            Set<String> keys = redisTemplate.keys("HOME_PICTURE_LIST_KEY:" + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
    }

    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(spaceId == null || StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 2. 校验空间权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        if (!space.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        }
        // 3. 查询该空间下的所有图片（必须要有主色调）
        List<Picture> pictureList = this.lambdaQuery()
                .eq(Picture::getSpaceId, spaceId)
                .isNotNull(Picture::getPicColor)
                .list();
        // 如果没有图片，直接返回空列表
        if (CollUtil.isEmpty(pictureList)) {
            return new ArrayList<>();
        }
        // 将颜色字符串转换为主色调
        Color targetColor = Color.decode(picColor);
        // 4. 计算相似度并排序
        List<Picture> sortedPictureList = pictureList.stream()
                .sorted(Comparator.comparingDouble(picture -> {
                    String hexColor = picture.getPicColor();
                    // 前面已经过滤掉了数据库中color列为null的记录，但是有些记录的color可能是空字符串“”，将这种没有主色调的图片排序到最后
                    if (StrUtil.isBlank(hexColor)) {
                        return Double.MAX_VALUE;
                    }
                    Color pictureColor = Color.decode(hexColor);
                    // 计算相似度
                    // Comparator的比较，默认数越大排在越后面，所以这里取反
                    return -ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor);
                }))
                .limit(12) // 取前 12 个
                .collect(Collectors.toList());
        // 5. 返回结果
        return sortedPictureList.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
    }

    @Override
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        // 1. 获取和校验参数
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
        Long spaceId = pictureEditByBatchRequest.getSpaceId();
        Long categoryId = pictureEditByBatchRequest.getCategoryId();
        List<String> tags = pictureEditByBatchRequest.getTags();
        ThrowUtils.throwIf(CollUtil.isEmpty(pictureIdList), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 2. 查询指定图片（需要查询 userId 字段用于权限校验）
        List<Picture> pictureList = this.lambdaQuery()
                .select(Picture::getId, Picture::getSpaceId, Picture::getUserId)
                .eq(spaceId != null, Picture::getSpaceId, spaceId)
                .isNull(spaceId == null, Picture::getSpaceId)
                .in(Picture::getId, pictureIdList)
                .list();
        if (pictureList.isEmpty()) {
            return;
        }

        // 3. 校验权限
        if (spaceId != null) {
            // 私有空间：校验空间权限
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!space.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
            }
        } else {
            // 公共图库：管理员可以编辑所有图片，普通用户只能编辑自己上传的图片
            boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
            if (!isAdmin) {
                // 校验所有图片是否都是当前用户上传的
                boolean hasNoAuth = pictureList.stream()
                        .anyMatch(picture -> !picture.getUserId().equals(loginUser.getId()));
                if (hasNoAuth) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能编辑自己上传的图片");
                }
            }
        }

        // 4. 更新分类和标签
        pictureList.forEach(picture -> {
            if (categoryId != null) {
                picture.setCategoryId(categoryId);
            }
            if (CollUtil.isNotEmpty(tags)) {
                // 将标签列表转换为逗号分隔的字符串，与 editPicture 方法保持一致
                picture.setTags(String.join(",", tags));
            }
        });
//        // 批量重命名
        String nameRule = pictureEditByBatchRequest.getNameRule();
        fillPictureWithNameRule(pictureList, nameRule);
        // 5. 操作数据库进行批量更新
        boolean result = this.updateBatchById(pictureList);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "批量编辑失败");
    }

    /**
     * nameRule 格式：图片{序号}
     *
     * @param pictureList
     * @param nameRule
     */
    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if (StrUtil.isBlank(nameRule) || CollUtil.isEmpty(pictureList)) {
            return;
        }
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                String pictureName = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
                picture.setName(pictureName);
            }
        } catch (Exception e) {
            log.error("名称解析错误", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "名称解析错误");
        }
    }

    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        // 获取图片信息
        Long pictureId = createPictureOutPaintingTaskRequest.getPictureId();
        Picture picture = Optional.ofNullable(this.getById(pictureId))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在"));
        //!!!已经改为使用注解鉴权
//        checkPictureAuth(loginUser, picture);
        // 创建扩图任务
        CreateOutPaintingTaskRequest createOutPaintingTaskRequest = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
        input.setImageUrl(picture.getUrl());
        createOutPaintingTaskRequest.setInput(input);
        createOutPaintingTaskRequest.setParameters(createPictureOutPaintingTaskRequest.getParameters());
        // 创建任务
        return aliYunAiApi.createOutPaintingTask(createOutPaintingTaskRequest);
    }

    @Override
    public PictureVO getPictureDetailById(long id,HttpServletRequest request) {
        Picture picture = this.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        PictureVO pictureVO = this.getPictureVO(picture,request);
        List<String> permissionList;
        Long spaceId = picture.getSpaceId();
        // 判断是否已经登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;

        if (currentUser == null || currentUser.getId() == null) {
            //没登录的用户，在公共图库中，只能查看图片和分享图片链接，不能删除、编辑、下载图片
            if (spaceId == null) {
                //公共图库 默认只有查看权限
            } else {
                //未登录用户 访问不了私有空间（包括个人私有空间和团队空间
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }else{
            if(spaceId == null){
                //已经登录的用户，对公共图库中自己上传的图片拥有所有权限 ； 系统管理员也拥有所有权限，
                permissionList = spaceUserAuthManager.getPermissionList(null,currentUser);
                permissionList = new ArrayList<>(permissionList);
                if (picture.getUserId().equals(currentUser.getId())) {
                    permissionList.add(SpaceUserPermissionConstant.PICTURE_EDIT);
                    permissionList.add(SpaceUserPermissionConstant.PICTURE_DELETE);
                }
            }else{
                Space space = spaceService.getById(spaceId);
                ThrowUtils.throwIf(space == null,ErrorCode.NOT_FOUND_ERROR,"空间不存在");
                permissionList = spaceUserAuthManager.getPermissionList(space,currentUser);
                if(permissionList.size() == 0)
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            //查询用户与这张图片的交互信息
            List<PictureInteraction> pictureInteractions = pictureInteractionService.lambdaQuery().eq(PictureInteraction::getUserId, currentUser.getId())
                    .eq(PictureInteraction::getPictureId, id).list();
            if(CollUtil.isNotEmpty(pictureInteractions)){
                for (PictureInteraction p : pictureInteractions) {
                    if (PictureInteractionTypeEnum.LIKE.getKey().equals(p.getInteractionType()) &&
                            PictureInteractionStatusEnum.isExisted(p.getInteractionStatus())) {
                        pictureVO.setLoginUserIsLike(true);
                    }
                    if (PictureInteractionTypeEnum.COLLECT.getKey().equals(p.getInteractionType()) &&
                            PictureInteractionStatusEnum.isExisted(p.getInteractionStatus())) {
                        pictureVO.setLoginUserIsCollect(true);
                    }
                }
            }
            pictureVO.setPermissionList(permissionList);
        }
        if(PictureReviewStatusEnum.PASS.getValue().equals(picture.getReviewStatus())){
            // 操作redis 初始化图片互动数据
            this.initPictureInteraction(id);
            //更新redis缓存中的图片操作类型数量
            this.updateInteractionNumByRedis(id, PictureInteractionTypeEnum.VIEW.getKey(), 1);
            //填充交互数据给VO
            this.fillPictureInteraction(pictureVO);
        }

        return pictureVO;
    }

    @Override
    public void pictureLikeOrCollect(PictureInteractionRequest pictureInteractionRequest,User user) {
        Long pictureId = pictureInteractionRequest.getPictureId();
        // 判断是否存在
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        Integer interactionStatus = pictureInteractionRequest.getInteractionStatus();
        Integer interactionType = pictureInteractionRequest.getInteractionType();
        LambdaUpdateWrapper<PictureInteraction> lambdaQueryWrapper = new LambdaUpdateWrapper<PictureInteraction>()
                .eq(PictureInteraction::getPictureId,pictureInteractionRequest.getPictureId())
                .eq(PictureInteraction::getUserId,user.getId());
        if (PictureInteractionTypeEnum.LIKE.getKey().equals(interactionType)) {
            lambdaQueryWrapper.eq(PictureInteraction::getInteractionType, PictureInteractionTypeEnum.LIKE.getKey());
        } else if (PictureInteractionTypeEnum.COLLECT.getKey().equals(interactionType)) {
            lambdaQueryWrapper.eq(PictureInteraction::getInteractionType, PictureInteractionTypeEnum.COLLECT.getKey());
        }
        PictureInteraction pictureInteraction = pictureInteractionService.getOne(lambdaQueryWrapper);
        if(pictureInteraction == null){
            pictureInteraction = new PictureInteraction();
            pictureInteraction.setPictureId(pictureId);
            pictureInteraction.setUserId(user.getId());
            pictureInteraction.setInteractionType(interactionType);
            pictureInteraction.setInteractionStatus(interactionStatus);
            boolean save = pictureInteractionService.save(pictureInteraction);
            ThrowUtils.throwIf(!save,ErrorCode.OPERATION_ERROR,"图片互动操作失败!");
        }else{
            pictureInteraction.setInteractionStatus(interactionStatus);
            boolean update = pictureInteractionService.update(new LambdaUpdateWrapper<PictureInteraction>()
                    .set(PictureInteraction::getInteractionStatus, interactionStatus)
                    .eq(PictureInteraction::getUserId, user.getId())
                    .eq(PictureInteraction::getPictureId, pictureId)
                    .eq(PictureInteraction::getInteractionType, interactionType)
            );
            ThrowUtils.throwIf(!update,ErrorCode.OPERATION_ERROR,"图片互动操作失败!");
        }

        // 更新互动类型数量到redis
        updateInteractionNumByRedis(pictureId, interactionType,
                PictureInteractionStatusEnum.CANCEL.getKey().equals(pictureInteraction.getInteractionStatus()) ?
                        -1 : 1);
    }

    @Override
    public void pictureShare(Long pictureId) {
        Picture picture = getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR,"图片不存在");
        if (PictureReviewStatusEnum.PASS.getValue().equals(picture.getReviewStatus())) {
            // 更新图片操作类型数量
            this.updateInteractionNumByRedis(pictureId, PictureInteractionTypeEnum.SHARE.getKey(), 1);
        }
    }

    @Override
    public String pictureDownload(Long pictureId) {
        Picture picture = getById(pictureId);
        updateInteractionNumByRedis(pictureId, PictureInteractionTypeEnum.DOWNLOAD.getKey(), 1);
        return picture.getName();
    }

    @Override
    public Page<PictureVO> getPicturePageListAsHome(PictureQueryRequest pictureQueryRequest,HttpServletRequest request) {
        //从redis缓存中或者数据库中获取基本图片信息
        Page<PictureVO> pictureVOPage = getBasicPictureVOInfo(pictureQueryRequest);
        //补充与图片有关的点赞收藏信息、用户信息
        getMorePictureVOInfo(pictureVOPage,request);
        return pictureVOPage;
    }

    @Override
    public Page<PictureVO> getPicturePageListAsPersonRelease(PictureQueryRequest pictureQueryRequest,HttpServletRequest request) {
        pictureQueryRequest.setNullSpaceId(true);
        User loginUser = userService.getLoginUser(request);
        pictureQueryRequest.setUserId(loginUser.getId());
        QueryWrapper<Picture> queryWrapper = this.getQueryWrapperMultiple(pictureQueryRequest);
        Page<Picture> picturePage = page(new Page<>(pictureQueryRequest.getCurrent(),pictureQueryRequest.getPageSize()), queryWrapper);
        //转化为Page<PictureVO>
        Page<PictureVO> pictureVOPage = getPictureVOPage(picturePage, request);
        return pictureVOPage;
    }


    private void getMorePictureVOInfo(Page<PictureVO> pictureVOPage, HttpServletRequest request) {
        List<PictureVO> pictureVOList = pictureVOPage.getRecords();
        if(CollUtil.isEmpty(pictureVOList)) return;
        //分类信息
        Set<Long> categoryIds = pictureVOList.stream().map(PictureVO::getCategoryId).collect(Collectors.toSet());
        List<Category> categoryList = categoryService.listByIds(categoryIds);
        Map<Long, List<Category>> categoryListMap = categoryList.stream().collect(Collectors.groupingBy(Category::getId));
        //用户信息
        Set<Long> userIdSet = pictureVOList.stream().map(PictureVO::getUserId).collect(Collectors.toSet());
        // 1 => user1, 2 => user2
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        Map<Long, Boolean> likeMap = new HashMap<>();
        Map<Long, Boolean> collectMap = new HashMap<>();
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User loginUser = (User) userObj;
        if (loginUser != null && loginUser.getId() != null) {
            List<Long> pictureIds = pictureVOList.stream().map(PictureVO::getId).collect(Collectors.toList());
            List<PictureInteraction> pictureInteractions = pictureInteractionService.lambdaQuery()
                    .in(PictureInteraction::getPictureId, pictureIds)
                    .eq(PictureInteraction::getUserId, loginUser.getId()).list();
            if(CollUtil.isNotEmpty(pictureInteractions)){
                for (PictureInteraction p : pictureInteractions) {
                    if (PictureInteractionTypeEnum.LIKE.getKey().equals(p.getInteractionType()) &&
                            PictureInteractionStatusEnum.isExisted(p.getInteractionStatus())) {
                        likeMap.put(p.getPictureId(), true);
                    }
                    if (PictureInteractionTypeEnum.COLLECT.getKey().equals(p.getInteractionType()) &&
                            PictureInteractionStatusEnum.isExisted(p.getInteractionStatus())) {
                        collectMap.put(p.getPictureId(), true);
                    }
                }
            }
        }
        //填充信息
        pictureVOList.forEach(pictureVO -> {
            //填充用户信息
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
            //填充分类信息
            Long categoryId = pictureVO.getCategoryId();
            if(categoryListMap.containsKey(categoryId)){
                pictureVO.setCategoryInfo(categoryListMap.get(categoryId).get(0));
            }
            // 设置当前登录用户点赞和收藏信息
            pictureVO.setLoginUserIsLike(likeMap.getOrDefault(pictureVO.getId(), false));
            pictureVO.setLoginUserIsCollect(collectMap.getOrDefault(pictureVO.getId(), false));
            fillPictureInteraction(pictureVO);
        });
    }

    private Page<PictureVO> getBasicPictureVOInfo(PictureQueryRequest pictureQueryRequest) {
        Page<PictureVO> pictureVOPage = null;
        List<PictureVO> pictureVOList = null;
        // 构建缓存 KEY 内容
        String cacheKeyContent = pictureQueryRequest.getCurrent() + "_" +pictureQueryRequest.getPageSize();
        Long categoryId = pictureQueryRequest.getCategoryId();
        if(ObjUtil.isNotEmpty(categoryId)){
            cacheKeyContent = cacheKeyContent + "_" + categoryId;
        }
        // 1.构建缓存 KEY
        String KEY = String.format(CacheKeyConstant.HOME_PICTURE_LIST_KEY
                , DigestUtils.md5DigestAsHex(cacheKeyContent.getBytes())
        );
        if(StrUtil.isBlank(pictureQueryRequest.getSearchText())){
            // 3.查询 Redis, 如果 Redis 命中，返回结果
            String redisData = this.redisCache.get(KEY);
            if (StrUtil.isNotEmpty(redisData)) {
                log.info("首页图片列表[Redis 缓存]");
//                pictureVOPage = JSONUtil.toBean(redisData,Page.class);
                pictureVOPage = JSONUtil.toBean(redisData, new TypeReference<Page<PictureVO>>() {
                }, true);
            }
        }

        // 4.查询数据库, 存入 Redis 和 本地缓存
        if (pictureVOPage == null) {
            Page<Picture> picturePage = this.page(new Page<>(pictureQueryRequest.getCurrent(), pictureQueryRequest.getPageSize()),
                    this.getQueryWrapper(pictureQueryRequest));
            List<Picture> pictureList = picturePage.getRecords();
            pictureVOPage = new Page<>(picturePage.getCurrent(),picturePage.getSize(),picturePage.getTotal());
            if(CollUtil.isEmpty(pictureList)){
                return pictureVOPage;
            }
            pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
            pictureVOPage.setRecords(pictureVOList);
            log.info("首页图片列表[MySQL 查询]");
            if (StrUtil.isEmpty(pictureQueryRequest.getSearchText())) {
                // 存入 Redis, 5 分钟过期
                this.redisCache.set(KEY, JSONUtil.toJsonStr(pictureVOPage), 5, TimeUnit.MINUTES);
            }
        }

        // 动态设置图片的互动数据
        for (PictureVO vo : pictureVOPage.getRecords()) {
            fillPictureInteraction(vo);
        }
        return pictureVOPage;
    }


    @Async
    protected void initPictureInteraction(long pictureId) {
        String key = CacheKeyConstant.PICTURE_INTERACTION_KEY_PREFIX + pictureId;
        Map<String, Object> interactions = redisCache.hGet(key);
        if (interactions == null || interactions.isEmpty()) {
            // 先创建一个可变的 HashMap
            Map<String, Object> tempMap = new HashMap<>();
            tempMap.put("0", 0);
            tempMap.put("1", 0);
            tempMap.put("2", 0);
            tempMap.put("3", 0);
            tempMap.put("4", 0);
            tempMap.put("5", new Date().getTime());
            // 然后包装成一个不可变的 Map
            redisCache.hSets(key, Collections.unmodifiableMap(tempMap));
//            redisCache.hSets(key, Map.of(
//                    "0", 0,
//                    "1", 0,
//                    "2", 0,
//                    "3", 0,
//                    "4", 0,
//                    "5", new Date().getTime()
//            ));
        }
    }

    /**
     * 更新互动数量到Redis
     *
     * @param pictureId       图片 ID
     * @param interactionType 互动类型
     * @param num             变更数量
     */
    public void updateInteractionNumByRedis(Long pictureId, Integer interactionType, int num) {
        String KEY = CacheKeyConstant.PICTURE_INTERACTION_KEY_PREFIX + pictureId;
        // 存储并递增
        redisCache.hIncrBy(KEY, String.valueOf(interactionType), num);
        // this.updateInteractionNum(pictureId, interactionType, num);
    }

    /**
     * 填充图片互动数据
     *
     * @param pictureVO 图片领域对象
     */
    private void fillPictureInteraction(PictureVO pictureVO) {
        String key = CacheKeyConstant.PICTURE_INTERACTION_KEY_PREFIX + pictureVO.getId();
        Map<String, Object> interactions = redisCache.hGet(key);
        if (interactions != null) {
            if (ObjectUtil.isNotEmpty(interactions.get("0"))) {
                pictureVO.setLikeQuantity(Integer.parseInt(interactions.get("0").toString()));
            }
            if (ObjectUtil.isNotEmpty(interactions.get("1"))) {
                pictureVO.setCollectQuantity(Integer.parseInt(interactions.get("1").toString()));
            }
            if (ObjectUtil.isNotEmpty(interactions.get("2"))) {
                pictureVO.setDownloadQuantity(Integer.parseInt(interactions.get("2").toString()));
            }
            if (ObjectUtil.isNotEmpty(interactions.get("3"))) {
                pictureVO.setShareQuantity(Integer.parseInt(interactions.get("3").toString()));
            }
            if (ObjectUtil.isNotEmpty(interactions.get("4"))) {
                pictureVO.setViewQuantity(Integer.parseInt(interactions.get("4").toString()));
            }
        }
    }

    @Override
    public Page<PictureVO> getMyCollectPicturePage(PictureQueryRequest pictureQueryRequest, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();

        // 2. 查询当前用户收藏的图片交互记录,按收藏时间倒序
        Page<PictureInteraction> interactionPage = pictureInteractionService.lambdaQuery()
                .eq(PictureInteraction::getUserId, loginUser.getId())
                .eq(PictureInteraction::getInteractionType, PictureInteractionTypeEnum.COLLECT.getKey())
                .eq(PictureInteraction::getInteractionStatus, PictureInteractionStatusEnum.EXISTED.getKey())
                .orderByDesc(PictureInteraction::getCreateTime)
                .page(new Page<>(current, size));

        // 3. 如果没有收藏记录,直接返回空页面
        List<PictureInteraction> interactionList = interactionPage.getRecords();
        if (CollUtil.isEmpty(interactionList)) {
            return new Page<>(current, size, 0);
        }

        // 4. 获取图片ID列表
        List<Long> pictureIds = interactionList.stream()
                .map(PictureInteraction::getPictureId)
                .collect(Collectors.toList());

        // 5. 查询图片信息
        List<Picture> pictureList = this.listByIds(pictureIds);
        if (CollUtil.isEmpty(pictureList)) {
            return new Page<>(current, size, 0);
        }

        // 6. 将图片列表按照收藏时间的顺序排序
        // 创建图片ID到图片对象的映射
        Map<Long, Picture> pictureMap = pictureList.stream()
                .collect(Collectors.toMap(Picture::getId, picture -> picture));

        // 按照收藏记录的顺序重新排列图片列表
        List<Picture> sortedPictureList = interactionList.stream()
                .map(interaction -> pictureMap.get(interaction.getPictureId()))
                .filter(picture -> picture != null)
                .collect(Collectors.toList());

        // 7. 转换为 PictureVO 列表
        List<PictureVO> pictureVOList = sortedPictureList.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());

        // 8. 填充分类信息
        Set<Long> categoryIds = pictureVOList.stream()
                .map(PictureVO::getCategoryId)
                .filter(categoryId -> categoryId != null)
                .collect(Collectors.toSet());
        if (CollUtil.isNotEmpty(categoryIds)) {
            List<Category> categoryList = categoryService.listByIds(categoryIds);
            Map<Long, Category> categoryMap = categoryList.stream()
                    .collect(Collectors.toMap(Category::getId, category -> category));
            pictureVOList.forEach(pictureVO -> {
                Long categoryId = pictureVO.getCategoryId();
                if (categoryId != null && categoryMap.containsKey(categoryId)) {
                    pictureVO.setCategoryInfo(categoryMap.get(categoryId));
                }
            });
        }

        // 9. 填充用户信息
        Set<Long> userIdSet = pictureVOList.stream()
                .map(PictureVO::getUserId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            if (userMap.containsKey(userId)) {
                pictureVO.setUser(userService.getUserVO(userMap.get(userId)));
            }
            // 设置收藏状态为true(因为这是收藏列表)
            pictureVO.setLoginUserIsCollect(true);
            // 填充图片互动数据
            fillPictureInteraction(pictureVO);
        });

        // 10. 构建返回的分页对象
        Page<PictureVO> pictureVOPage = new Page<>(current, size, interactionPage.getTotal());
        pictureVOPage.setRecords(pictureVOList);

        return pictureVOPage;
    }
}




