package com.jdjm.jdjmpicturebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jdjm.jdjmpicturebackend.model.dto.task.ScheduledTaskQueryRequest;
import com.jdjm.jdjmpicturebackend.model.entity.ScheduledTask;
import com.jdjm.jdjmpicturebackend.model.vo.ScheduledTaskVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author jdjm
* @description 针对表【scheduled_task(定时任务表)】的数据库操作Service
* @createDate 2025-10-02 18:57:41
*/
public interface ScheduledTaskService extends IService<ScheduledTask> {

    void addScheduledTask(ScheduledTask scheduledTask);

    /**
     * 更新定时任务
     * @param scheduledTask
     */
    void updateScheduledTask(ScheduledTask scheduledTask);

    public ScheduledTask getScheduledTaskByTaskBean(String taskBean);


    /**
     * 修改定时任务状态
     *
     * @param scheduledTask 定时任务领域对象
     */
    void editTaskStatus(ScheduledTask scheduledTask);


    /**
     * 获取定时任务管理分页列表
     *
     * @param scheduledTaskQueryRequest 定时任务领域对象
     * @return 定时任务管理分页列表
     */
    Page<ScheduledTaskVO> getScheduledTaskPage(ScheduledTaskQueryRequest scheduledTaskQueryRequest);
}
