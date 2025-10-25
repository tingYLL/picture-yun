package com.jdjm.jdjmpicturebackend.manager.task;

import com.jdjm.jdjmpicturebackend.service.VIPService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * VIP 定时任务类
 *
 * @author Baolong 2025年03月19 13:55
 * @version 1.0
 * @since 1.8
 */
@Slf4j
@Component
public class VIPTask {

	@Resource
	private VIPService vipService;

	/**
	 * 检查并更新过期的 VIP 会员状态
	 *
	 * @return Task实例 - 使用 Lambda 表达式实现
	 */
	@Bean
	public Task checkAndUpdateExpiredVIPTask() {
		return () -> {
			log.info("开始执行定时任务：检查并更新过期的VIP会员状态");
			try {
				int updatedCount = vipService.updateExpiredVIPStatus();
				if (updatedCount > 0) {
					log.info("成功更新 {} 个过期VIP会员的状态", updatedCount);
				} else {
					log.info("暂无过期的VIP会员需要更新");
				}
			} catch (Exception e) {
				log.error("执行VIP过期检查任务时发生异常", e);
			}
			log.info("定时任务执行完毕");
		};
	}
}