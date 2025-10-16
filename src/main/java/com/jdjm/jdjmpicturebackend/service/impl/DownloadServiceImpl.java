package com.jdjm.jdjmpicturebackend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jdjm.jdjmpicturebackend.mapper.DownloadLogMapper;
import com.jdjm.jdjmpicturebackend.model.dto.download.DownloadRequest;
import com.jdjm.jdjmpicturebackend.model.entity.DownloadLog;
import com.jdjm.jdjmpicturebackend.model.enums.PictureInteractionTypeEnum;
import com.jdjm.jdjmpicturebackend.model.entity.Picture;
import com.jdjm.jdjmpicturebackend.model.vo.DownloadHistoryVO;
import com.jdjm.jdjmpicturebackend.model.vo.PictureVO;
import com.jdjm.jdjmpicturebackend.service.DownloadService;
import com.jdjm.jdjmpicturebackend.service.PictureService;
import com.jdjm.jdjmpicturebackend.service.VIPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DownloadServiceImpl extends ServiceImpl<DownloadLogMapper,DownloadLog>
    implements DownloadService {

    @Resource
    private VIPService vipService;
    @Resource
    private DownloadLogMapper downloadLogMapper;
    @Resource
    private PictureService pictureService;

    private static final int MAX_DAILY_DOWNLOADS_FOR_NORMAL_USER = 5;
    private static final int MAX_DAILY_DOWNLOADS_FOR_VIP_USER = 20;

    public boolean canDownload(Long userId) {
            return getRemainingDownloads(userId) > 0;
    }

    public int getRemainingDownloads(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        //计算用户今天可用下载次数
        long downloadsToday = downloadLogMapper.countDownloadsByUserAndDateRange(
                userId, startOfDay, endOfDay);
        boolean isVIP = vipService.isVIP(userId);
        //根据用户是否是会员，获取用户一天内可用的下载次
        int maxDownloads = isVIP ? MAX_DAILY_DOWNLOADS_FOR_VIP_USER : MAX_DAILY_DOWNLOADS_FOR_NORMAL_USER;
        //返回今日剩余可用下载次数
        return (int) (maxDownloads - downloadsToday);
    }

    public boolean hasDownloadedFile(Long userId, Long fileId) {
        // 检查用户是否曾经下载过这个文件（永久有效）
        return downloadLogMapper.countDownloadsByUserAndFile(userId, fileId) > 0;
    }

    public void logDownload(Long userId, Long fileId) {
        // 如果用户曾经下载过这个文件，则不记录新的下载记录，也不消耗下载次数（永久有效）
        if (hasDownloadedFile(userId, fileId)) {
            return;
        }

        DownloadLog downloadLog = new DownloadLog();
        downloadLog.setUserId(userId);
        downloadLog.setFileId(fileId);
        downloadLog.setDownloadedAt(LocalDateTime.now());
        int count = downloadLogMapper.insert(downloadLog);
        //更新redis中的图片下载次数
        if (count >0){
            pictureService.updateInteractionNumByRedis(fileId, PictureInteractionTypeEnum.DOWNLOAD.getKey(), 1);
        }
    }

    @Override
    public Page<DownloadHistoryVO> getUserDownloadHistoryPage(Long userId, DownloadRequest downloadRequest) {
        // 获取分页对象
        Page<DownloadLog> downloadLogPage = downloadLogMapper.listDownloadHistoryByUserIdAndTimeRangePage(
                downloadRequest.getPage(DownloadLog.class),
                userId,
                downloadRequest.getStartTime(),
                downloadRequest.getEndTime()
        );

        // 转换为 VO 分页对象
        Page<DownloadHistoryVO> downloadHistoryVOPage = new Page<>(
                downloadLogPage.getCurrent(),
                downloadLogPage.getSize(),
                downloadLogPage.getTotal()
        );

        // 转换记录
        List<DownloadHistoryVO> records = downloadLogPage.getRecords().stream().map(downloadLog -> {
            DownloadHistoryVO downloadHistoryVO = new DownloadHistoryVO();
            downloadHistoryVO.setId(downloadLog.getId());
            downloadHistoryVO.setDownloadedAt(downloadLog.getDownloadedAt());

            // 获取图片信息（由于SQL已经JOIN过滤，这里picture一定存在） 不用判空
            Picture picture = pictureService.getById(downloadLog.getFileId());
            PictureVO pictureVO = PictureVO.objToVo(picture);
            downloadHistoryVO.setPicture(pictureVO);

            return downloadHistoryVO;
        }).collect(Collectors.toList());

        downloadHistoryVOPage.setRecords(records);
        return downloadHistoryVOPage;
    }

    @Override
    public List<DownloadHistoryVO> getUserDownloadHistory(Long userId) {
        // 兼容旧版本，不带时间查询的接口
        DownloadRequest downloadRequest = new DownloadRequest();
        downloadRequest.setCurrent(1);
        downloadRequest.setPageSize(Integer.MAX_VALUE);
        return getUserDownloadHistoryPage(userId, downloadRequest).getRecords();
    }
}