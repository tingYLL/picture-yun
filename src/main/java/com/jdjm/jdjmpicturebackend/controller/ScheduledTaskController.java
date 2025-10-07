package com.jdjm.jdjmpicturebackend.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jdjm.jdjmpicturebackend.annotation.AuthCheck;
import com.jdjm.jdjmpicturebackend.common.BaseResponse;
import com.jdjm.jdjmpicturebackend.common.DeleteRequest;
import com.jdjm.jdjmpicturebackend.common.ResultUtils;
import com.jdjm.jdjmpicturebackend.constant.UserConstant;
import com.jdjm.jdjmpicturebackend.exception.BusinessException;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.exception.ThrowUtils;
import com.jdjm.jdjmpicturebackend.manager.task.ScheduledTaskManger;
import com.jdjm.jdjmpicturebackend.model.dto.task.ScheduledTaskAddRequest;
import com.jdjm.jdjmpicturebackend.model.dto.task.ScheduledTaskQueryRequest;
import com.jdjm.jdjmpicturebackend.model.dto.task.ScheduledTaskUpdateRequest;
import com.jdjm.jdjmpicturebackend.model.entity.ScheduledTask;
import com.jdjm.jdjmpicturebackend.model.vo.ScheduledTaskVO;
import com.jdjm.jdjmpicturebackend.service.ScheduledTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 定时任务表 (scheduled_task) - 接口
 *
 * @author Silas Yan 2025-03-22:15:57
 */
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class ScheduledTaskController {
	private final ScheduledTaskService scheduledTaskService;
	private ScheduledTaskManger scheduledTaskManger;

	/**
	 * 新增定时任务
	 *
	 * @param scheduledTaskAddRequest 定时任务新增请求
	 * @return 新增结果
	 */
	@PostMapping("/add")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> addScheduledTask(@RequestBody ScheduledTaskAddRequest scheduledTaskAddRequest) {
		ThrowUtils.throwIf(scheduledTaskAddRequest == null, ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(StrUtil.isEmpty(scheduledTaskAddRequest.getTaskName()), ErrorCode.PARAMS_ERROR, "定时任务名称不能为空");
		ThrowUtils.throwIf(StrUtil.isEmpty(scheduledTaskAddRequest.getTaskCron()), ErrorCode.PARAMS_ERROR, "任务表达式不能为空");
		ThrowUtils.throwIf(StrUtil.isEmpty(scheduledTaskAddRequest.getTaskBean()), ErrorCode.PARAMS_ERROR, "目标方法不能为空");
		try {
			new CronTrigger(scheduledTaskAddRequest.getTaskCron());
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务表达式错误");
		}
		ScheduledTask scheduledTask = new ScheduledTask();
		BeanUtils.copyProperties(scheduledTaskAddRequest, scheduledTask);
		scheduledTaskService.addScheduledTask(scheduledTask);
		return ResultUtils.success(true);
	}

	/**
	 * 删除定时任务
	 *
	 * @param deleteRequest 定时任务删除请求
	 * @return 删除结果
	 */
	@PostMapping("/delete")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> deleteScheduledTask(@RequestBody DeleteRequest deleteRequest) {
		ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
		Long scheduledTaskId = deleteRequest.getId();
		ThrowUtils.throwIf(ObjectUtil.isEmpty(scheduledTaskId), ErrorCode.PARAMS_ERROR);
		boolean existed = scheduledTaskService.getBaseMapper()
				.exists(new LambdaQueryWrapper<ScheduledTask>()
						.eq(ScheduledTask::getId,scheduledTaskId));
		if (!existed) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "定时任务不存在!");
		}
		boolean res = scheduledTaskService.removeById(scheduledTaskId);
		ThrowUtils.throwIf(!res,ErrorCode.SYSTEM_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 更新定时任务
	 *
	 * @param scheduledTaskUpdateRequest 定时任务更新请求
	 * @return 更新结果
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateScheduledTask(@RequestBody ScheduledTaskUpdateRequest scheduledTaskUpdateRequest) {
		ThrowUtils.throwIf(scheduledTaskUpdateRequest == null, ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(ObjectUtil.isEmpty(scheduledTaskUpdateRequest.getId()), ErrorCode.PARAMS_ERROR);
		try {
			new CronTrigger(scheduledTaskUpdateRequest.getTaskCron());
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务表达式错误");
		}
		ScheduledTask scheduledTask = new ScheduledTask();
		BeanUtils.copyProperties(scheduledTaskUpdateRequest, scheduledTask);
		scheduledTaskService.updateScheduledTask(scheduledTask);
		return ResultUtils.success(true);
	}

	/**
	 * 修改定时任务状态
	 *
	 * @param scheduledTaskUpdateRequest 定时任务更新请求
	 * @return 修改结果
	 */
	@PostMapping("/editTaskStatus")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> editTaskStatus(@RequestBody ScheduledTaskUpdateRequest scheduledTaskUpdateRequest) {
		ThrowUtils.throwIf(scheduledTaskUpdateRequest == null, ErrorCode.PARAMS_ERROR);
		Long taskId = scheduledTaskUpdateRequest.getId();
		ThrowUtils.throwIf(ObjectUtil.isEmpty(taskId), ErrorCode.PARAMS_ERROR);
		Integer taskStatus = scheduledTaskUpdateRequest.getTaskStatus();
		ThrowUtils.throwIf(ObjectUtil.isEmpty(taskStatus), ErrorCode.PARAMS_ERROR);
		ScheduledTask scheduledTask = new ScheduledTask();
		BeanUtils.copyProperties(scheduledTaskUpdateRequest,scheduledTask);
		scheduledTaskService.editTaskStatus(scheduledTask);
		return ResultUtils.success(true);
	}

	/**
	 * 获取定时任务管理分页列表
	 *
	 * @param scheduledTaskQueryRequest 定时任务查询请求
	 * @return 定时任务管理分页列表
	 */
	@PostMapping("/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<ScheduledTaskVO>> getScheduledTaskPage(
			@RequestBody ScheduledTaskQueryRequest scheduledTaskQueryRequest) {
		ThrowUtils.throwIf(scheduledTaskQueryRequest == null, ErrorCode.PARAMS_ERROR);
		Page<ScheduledTaskVO> scheduledTaskVOPage = scheduledTaskService.getScheduledTaskPage(scheduledTaskQueryRequest);
		return ResultUtils.success(scheduledTaskVOPage);
	}

	/**
	 * 刷新定时任务
	 *
	 * @return 刷新结果
	 */
	@PostMapping("refresh")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> refreshScheduledTask() {
		scheduledTaskManger.refresh();
		return ResultUtils.success(true);
	}
}
