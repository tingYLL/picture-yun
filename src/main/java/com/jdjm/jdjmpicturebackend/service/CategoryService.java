package com.jdjm.jdjmpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jdjm.jdjmpicturebackend.model.dto.category.CategoryAddRequest;
import com.jdjm.jdjmpicturebackend.model.dto.category.CategoryQueryRequest;
import com.jdjm.jdjmpicturebackend.model.dto.picture.PictureQueryRequest;
import com.jdjm.jdjmpicturebackend.model.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jdjm.jdjmpicturebackend.model.entity.Picture;
import com.jdjm.jdjmpicturebackend.model.vo.CategoryVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author jdjm
* @description 针对表【category(分类表)】的数据库操作Service
* @createDate 2025-09-22 23:59:56
*/
public interface CategoryService extends IService<Category> {

    void addCategory(CategoryAddRequest categoryAddRequest, HttpServletRequest request);

    List<Category> getCategoryListAsHome();

    /**
     * 获取查询对象
     *
     * @param categoryQueryRequest
     * @return
     */
    QueryWrapper<Category> getQueryWrapper(CategoryQueryRequest categoryQueryRequest);

    Page<CategoryVO> getCategoryVOPage(Page<Category> categoryPage);
}
