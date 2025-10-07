package com.jdjm.jdjmpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jdjm.jdjmpicturebackend.common.PageRequest;
import com.jdjm.jdjmpicturebackend.exception.BusinessException;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.manager.task.ScheduledTaskManger;
import com.jdjm.jdjmpicturebackend.model.dto.task.ScheduledTaskQueryRequest;
import com.jdjm.jdjmpicturebackend.model.entity.ScheduledTask;
import com.jdjm.jdjmpicturebackend.model.enums.TaskStatusEnum;
import com.jdjm.jdjmpicturebackend.model.vo.ScheduledTaskVO;
import com.jdjm.jdjmpicturebackend.service.ScheduledTaskService;
import com.jdjm.jdjmpicturebackend.mapper.ScheduledTaskMapper;
import com.jdjm.jdjmpicturebackend.utils.SFLambdaUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author jdjm
* @description 针对表【scheduled_task(定时任务表)】的数据库操作Service实现
* @createDate 2025-10-02 18:57:41
*/
@Service
public class ScheduledTaskServiceImpl extends ServiceImpl<ScheduledTaskMapper, ScheduledTask>
    implements ScheduledTaskService {

    @Resource
    @Lazy
    private ScheduledTaskManger scheduledTaskManger;

    @Override
    public void addScheduledTask(ScheduledTask scheduledTask) {
        boolean existed = getBaseMapper().exists(new LambdaQueryWrapper<ScheduledTask>()
                .eq(ScheduledTask::getTaskBean, scheduledTask.getTaskBean())
                .or()
                .eq(ScheduledTask::getTaskKey, scheduledTask.getTaskKey()));
        if (existed) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "任务方法已存在!");
        }
        scheduledTask.setTaskKey(scheduledTask.getTaskBean());
        boolean result = this.save(scheduledTask);
        if (result) return;
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "新增定时任务失败!");
    }

    @Override
    public void updateScheduledTask(ScheduledTask scheduledTask) {
        Long id = scheduledTask.getId();
        boolean existed = this.getBaseMapper().exists(new LambdaQueryWrapper<ScheduledTask>().eq(ScheduledTask::getId, id));
        if (!existed) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "定时任务不存在!");
        }
        ScheduledTask oldScheduledTask = getScheduledTaskByTaskBean(scheduledTask.getTaskBean());
        if (oldScheduledTask != null && !oldScheduledTask.getId().equals(id)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "任务方法已存在!");
        }
        scheduledTask.setEditTime(new Date());
        boolean result = this.updateById(scheduledTask);
        if (result) return;
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新定时任务失败!");
    }

    /**
     * 根据任务方法获取定时任务
     *
     * @param taskBean 任务方法
     * @return 定时任务
     */
    @Override
    public ScheduledTask getScheduledTaskByTaskBean(String taskBean) {
        ScheduledTask scheduledTask = this.getOne(
                new LambdaQueryWrapper<ScheduledTask>().eq(ScheduledTask::getTaskBean, taskBean)
        );
        if (scheduledTask == null) return null;
        return scheduledTask;
    }

    /**
     * 修改定时任务状态
     *
     * @param scheduledTask 定时任务领域对象
     */
    @Override
    public void editTaskStatus(ScheduledTask scheduledTask) {
        Long taskId = scheduledTask.getId();
        boolean existed = this.getBaseMapper().exists(new LambdaQueryWrapper<ScheduledTask>().eq(ScheduledTask::getId,taskId));
        if (!existed) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "定时任务不存在!");
        }
        scheduledTask.setEditTime(new Date());
        boolean result = updateById(scheduledTask);
        if (result) {
            scheduledTaskManger.refresh(taskId);
            return;
        }
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "定时任务" +
                (TaskStatusEnum.isOpen(scheduledTask.getTaskStatus()) ? "开启" : "关闭")
                + "失败!");
    }


    /**
     * 获取定时任务管理分页列表
     *
     * @param scheduledTaskQueryRequest 定时任务领域对象
     * @return 定时任务管理分页列表
     */
    @Override
    public Page<ScheduledTaskVO> getScheduledTaskPage(ScheduledTaskQueryRequest scheduledTaskQueryRequest) {
        LambdaQueryWrapper<ScheduledTask> lambdaQueryWrapper = this.lambdaQueryWrapper(scheduledTaskQueryRequest);
        Page<ScheduledTask> page = this.page(new Page<>(1,10), lambdaQueryWrapper);
        List<ScheduledTask> scheduledTaskList = page.getRecords();
        List<ScheduledTaskVO> scheduledTaskVOList = scheduledTaskList.stream().map(ScheduledTaskVO::objToVo).collect(Collectors.toList());
        Page<ScheduledTaskVO> scheduledTaskVOPage = new Page<>(page.getCurrent(),page.getSize(),page.getTotal());
        if(CollUtil.isEmpty(scheduledTaskList)){
            return scheduledTaskVOPage;
        }
        scheduledTaskVOPage.setRecords(scheduledTaskVOList);
        return scheduledTaskVOPage ;
    }

    /**
     * 查询条件对象（Lambda）
     *
     * @param scheduledTaskQueryRequest 定时任务领域对象
     * @return 查询条件对象（Lambda）
     */
    private LambdaQueryWrapper<ScheduledTask> lambdaQueryWrapper(ScheduledTaskQueryRequest scheduledTaskQueryRequest) {
        LambdaQueryWrapper<ScheduledTask> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        Long taskId = scheduledTaskQueryRequest.getId();
        String jobKey = scheduledTaskQueryRequest.getTaskKey();
        String jobName = scheduledTaskQueryRequest.getTaskName();
        String jobCron = scheduledTaskQueryRequest.getTaskCron();
        String jobDesc = scheduledTaskQueryRequest.getTaskDesc();
        String jobBean = scheduledTaskQueryRequest.getTaskBean();
        Integer jobStatus = scheduledTaskQueryRequest.getTaskStatus();
        lambdaQueryWrapper.eq(ObjUtil.isNotNull(taskId), ScheduledTask::getId, taskId);
        lambdaQueryWrapper.eq(StrUtil.isNotEmpty(jobKey), ScheduledTask::getTaskKey, jobKey);
        lambdaQueryWrapper.like(StrUtil.isNotEmpty(jobName), ScheduledTask::getTaskName, jobName);
        lambdaQueryWrapper.eq(StrUtil.isNotEmpty(jobCron), ScheduledTask::getTaskCron, jobCron);
        lambdaQueryWrapper.like(StrUtil.isNotEmpty(jobDesc), ScheduledTask::getTaskDesc, jobDesc);
        lambdaQueryWrapper.eq(StrUtil.isNotEmpty(jobBean), ScheduledTask::getTaskBean, jobBean);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(jobStatus), ScheduledTask::getTaskStatus, jobStatus);
        // 处理排序规则
        if (scheduledTaskQueryRequest.isMultipleSort()) {
            List<PageRequest.Sort> sorts = scheduledTaskQueryRequest.getSorts();
            if (CollUtil.isNotEmpty(sorts)) {
                sorts.forEach(sort -> {
                    String sortField = sort.getField();
                    boolean sortAsc = sort.isAsc();
                    lambdaQueryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortAsc, SFLambdaUtil.getSFunction(ScheduledTask.class, sortField));
                });
            }
        } else {
            PageRequest.Sort sort = scheduledTaskQueryRequest.getSort();
            if (sort != null) {
                String sortField = sort.getField();
                boolean sortAsc = sort.isAsc();
                lambdaQueryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortAsc, SFLambdaUtil.getSFunction(ScheduledTask.class, sortField));
            } else {
                lambdaQueryWrapper.orderByDesc(ScheduledTask::getCreateTime);
            }
        }
        return lambdaQueryWrapper;
    }

}




