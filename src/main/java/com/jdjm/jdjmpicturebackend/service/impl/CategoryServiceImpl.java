package com.jdjm.jdjmpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jdjm.jdjmpicturebackend.config.LocalCacheConfig;
import com.jdjm.jdjmpicturebackend.constant.CacheKeyConstant;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.exception.ThrowUtils;
import com.jdjm.jdjmpicturebackend.model.dto.category.CategoryAddRequest;
import com.jdjm.jdjmpicturebackend.model.dto.category.CategoryQueryRequest;
import com.jdjm.jdjmpicturebackend.model.entity.Category;
import com.jdjm.jdjmpicturebackend.model.entity.Picture;
import com.jdjm.jdjmpicturebackend.model.entity.User;
import com.jdjm.jdjmpicturebackend.model.vo.CategoryVO;
import com.jdjm.jdjmpicturebackend.service.CategoryService;
import com.jdjm.jdjmpicturebackend.mapper.CategoryMapper;
import com.jdjm.jdjmpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author jdjm
* @description 针对表【category(分类表)】的数据库操作Service实现
* @createDate 2025-09-22 23:59:56
*/
@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService {

    @Resource
    private UserService userService;

    @Override
    public void addCategory(CategoryAddRequest categoryAddRequest, HttpServletRequest
                             request) {
        User loginUser = userService.getLoginUser(request);
        Category category = new Category();
        BeanUtil.copyProperties(categoryAddRequest,category);
        category.setUserId(loginUser.getId());
        boolean result = this.save(category);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR,"新增分类失败");
    }

    @Override
    public List<Category> getCategoryListAsHome() {
        List<Category> categoryList;
        String localData = LocalCacheConfig.HOME_PICTURE_LOCAL_CACHE.getIfPresent(CacheKeyConstant.HOME_CATEGORY);
        if (StrUtil.isNotEmpty(localData)) {
            log.info("首页分类列表[Local 缓存]");
            categoryList = JSONUtil.toBean(localData, new TypeReference<List<Category>>() {
            }, true);
        } else {
            log.info("首页分类列表[MySQL 查询]");
            categoryList = this.list();
            LocalCacheConfig.HOME_PICTURE_LOCAL_CACHE.put(CacheKeyConstant.HOME_CATEGORY, JSONUtil.toJsonStr(categoryList));
        }
        return categoryList;
    }

    @Override
    public QueryWrapper<Category> getQueryWrapper(CategoryQueryRequest categoryQueryRequest) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        if (categoryQueryRequest == null) {
            return queryWrapper;
        }
        String sortField = categoryQueryRequest.getSortField();
        String sortOrder = categoryQueryRequest.getSortOrder();
        String name = categoryQueryRequest.getName();
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public Page<CategoryVO> getCategoryVOPage(Page<Category> categoryPage) {
        List<Category> categoryList = categoryPage.getRecords();
        Page<CategoryVO> categoryVOPage = new Page<>(categoryPage.getCurrent(),categoryPage.getSize(),categoryPage.getTotal());
        if (CollUtil.isEmpty(categoryList)){
            return categoryVOPage;
        }
        List<CategoryVO> collect = categoryList.stream().map(CategoryVO::objToVo).collect(Collectors.toList());
        categoryVOPage.setRecords(collect);
        return categoryVOPage;
    }
}




