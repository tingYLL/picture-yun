package com.jdjm.jdjmpicturebackend.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * 通用的分页请求类
 */
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = "descend";


    /**
     * 是否多个排序, true 为多个排序, false 为单个排序
     */
    private boolean multipleSort = false;

    /**
     * 排序参数
     */
    private Sort sort;

    /**
     * 排序参数列表
     */
    private List<Sort> sorts;

    /**
     * 排序类
     */
    @Data
    public static class Sort {
        /**
         * 是否升序, true 为升序, false 为降序
         */
        private boolean asc = false;
        /**
         * 排序字段
         */
        private String field;
    }

    /**
     * 获取分页对象
     *
     * @param clazz 类
     * @param <T>   泛型
     * @return 分页对象, MyBatisPlus 的分页
     */
    public <T> Page<T> getPage(Class<T> clazz) {
        return new Page<T>(this.current, this.pageSize);
    }

    private static final long serialVersionUID = 1L;
}