package com.jdjm.jdjmpicturebackend.model.dto.task;

import com.jdjm.jdjmpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 定时任务查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScheduledTaskQueryRequest extends PageRequest implements Serializable {

	/**
	 * 任务ID
	 */
	private Long id;

	/**
	 * 任务 KEY（存在内存中）
	 */
	private String taskKey;

	/**
	 * 任务名称
	 */
	private String taskName;

	/**
	 * 任务 corn 表达式
	 */
	private String taskCron;

	/**
	 * 任务描述
	 */
	private String taskDesc;

	/**
	 * 任务 Bean 名称（执行任务的 bean）
	 */
	private String taskBean;

	/**
	 * 任务状态（0-关闭, 1-开启）
	 */
	private Integer taskStatus;

	/**
	 * 编辑时间
	 */
	private Date editTime;

	/**
	 * 创建时间
	 */
	private Date createTime;

	private static final long serialVersionUID = 1L;
}
