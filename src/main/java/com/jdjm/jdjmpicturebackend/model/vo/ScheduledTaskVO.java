package com.jdjm.jdjmpicturebackend.model.vo;

import com.jdjm.jdjmpicturebackend.model.entity.ScheduledTask;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 定时任务领域对象
 */
@Data
public class ScheduledTaskVO implements Serializable {

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

	public static ScheduledTaskVO objToVo(ScheduledTask scheduledTask){
		if(scheduledTask == null){
			return null;
		}
		ScheduledTaskVO scheduledTaskVO = new ScheduledTaskVO();
		BeanUtils.copyProperties(scheduledTask,scheduledTaskVO);
		return scheduledTaskVO;
	}
}
