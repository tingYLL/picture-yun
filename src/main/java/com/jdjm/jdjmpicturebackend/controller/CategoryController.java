package com.jdjm.jdjmpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jdjm.jdjmpicturebackend.annotation.AuthCheck;
import com.jdjm.jdjmpicturebackend.common.BaseResponse;
import com.jdjm.jdjmpicturebackend.common.DeleteRequest;
import com.jdjm.jdjmpicturebackend.common.ResultUtils;
import com.jdjm.jdjmpicturebackend.constant.UserConstant;
import com.jdjm.jdjmpicturebackend.exception.BusinessException;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.exception.ThrowUtils;
import com.jdjm.jdjmpicturebackend.model.dto.category.CategoryAddRequest;
import com.jdjm.jdjmpicturebackend.model.dto.category.CategoryQueryRequest;
import com.jdjm.jdjmpicturebackend.model.dto.category.CategoryUpdateRequest;
import com.jdjm.jdjmpicturebackend.model.entity.Category;
import com.jdjm.jdjmpicturebackend.model.entity.Picture;
import com.jdjm.jdjmpicturebackend.model.vo.CategoryVO;
import com.jdjm.jdjmpicturebackend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 分类表 (category) - 接口
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    /**
     * 新增分类
     *
     * @param categoryAddRequest 分类新增请求
     * @return 新增结果
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> addCategory(@RequestBody CategoryAddRequest categoryAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(categoryAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isEmpty(categoryAddRequest.getName()), ErrorCode.PARAMS_ERROR, "分类名称不能为空");
        categoryService.addCategory(categoryAddRequest,request);
        return ResultUtils.success(true);
    }

    /**
     * 删除分类
     *
     * @param deleteRequest 分类删除请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteCategory(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Long categoryId = deleteRequest.getId();
        ThrowUtils.throwIf(ObjectUtil.isEmpty(categoryId), ErrorCode.PARAMS_ERROR);
        boolean result = categoryService.removeById(categoryId);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR,"删除分类失败");
        return ResultUtils.success(true);
    }

    /**
     * 更新分类
     *
     * @param categoryUpdateRequest 分类更新请求
     * @return 更新结果
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateCategory(@RequestBody CategoryUpdateRequest categoryUpdateRequest) {
        ThrowUtils.throwIf(categoryUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjectUtil.isEmpty(categoryUpdateRequest.getId()), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isEmpty(categoryUpdateRequest.getName()), ErrorCode.PARAMS_ERROR, "分类名称不能为空");
        Category category = new Category();
        BeanUtil.copyProperties(categoryUpdateRequest,category);
        boolean result = categoryService.updateById(category);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR,"更新分类失败");
        return ResultUtils.success(true);
    }

    /**
     * 获取首页分类列表
     *
     * @return 首页分类列表
     */
    @GetMapping("/home/list")
    public BaseResponse<List<CategoryVO>> getCategoryListAsHome() {
        List<Category> categoryList = categoryService.getCategoryListAsHome();
        List<CategoryVO> collect = Optional.ofNullable(categoryList)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(category -> CategoryVO.objToVo(category))
                .collect(Collectors.toList());
        return ResultUtils.success(collect);
    }

    /**
     * 管理页面获取分类列表
     *
     * @return 首页分类列表
     */
    @PostMapping("/manage/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<CategoryVO>> getCategoryListAsManage(@RequestBody CategoryQueryRequest categoryQueryRequest) {
        int size = categoryQueryRequest.getPageSize();
        int current = categoryQueryRequest.getCurrent();
        Page<Category> categoryPage = categoryService.page(new Page<>(current, size),categoryService.getQueryWrapper(categoryQueryRequest));
        Page<CategoryVO> categoryVOPage = categoryService.getCategoryVOPage(categoryPage);
        return ResultUtils.success(categoryVOPage);
    }

}
