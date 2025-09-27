package com.jdjm.jdjmpicturebackend.model.vo;

import cn.hutool.json.JSONUtil;
import com.jdjm.jdjmpicturebackend.model.entity.Category;
import com.jdjm.jdjmpicturebackend.model.entity.Picture;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 分类响应对象
 */
@Data
public class CategoryVO implements Serializable {

	/**
	 * 分类ID
	 */
	private Long id;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 父分类 ID（0-表示顶层分类）
	 */
	private Long parentId;

	/**
	 * 使用数量
	 */
	private Integer useNum;

	/**
	 * 创建用户 ID
	 */
	private Long userId;

	/**
	 * 创建时间
	 */
	private Date createTime;

	private static final long serialVersionUID = 1L;

	/**
	 * 对象转封装类
	 */
	public static CategoryVO objToVo(Category category) {
		if (category == null) {
			return null;
		}
		CategoryVO categoryVO = new CategoryVO();
		BeanUtils.copyProperties(category, categoryVO);
		return categoryVO;
	}
}
