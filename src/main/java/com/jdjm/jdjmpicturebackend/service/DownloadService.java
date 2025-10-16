package com.jdjm.jdjmpicturebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jdjm.jdjmpicturebackend.model.dto.download.DownloadRequest;
import com.jdjm.jdjmpicturebackend.model.entity.DownloadLog;
import com.jdjm.jdjmpicturebackend.model.vo.DownloadHistoryVO;

import java.util.List;

public interface DownloadService extends IService<DownloadLog> {


    boolean canDownload(Long userId);

    int getRemainingDownloads(Long userId);

    void logDownload(Long userId, Long fileId);

    /**
     * 分页获取用户下载历史记录（带时间查询）
     * @param userId 用户ID
     * @param downloadRequest 查询参数（包含时间范围和分页）
     * @return 分页下载历史记录
     */
    Page<DownloadHistoryVO> getUserDownloadHistoryPage(Long userId, DownloadRequest downloadRequest);

    /**
     * 获取用户下载历史记录（不带时间查询）
     * @param userId 用户ID
     * @return 下载历史记录列表
     */
    List<DownloadHistoryVO> getUserDownloadHistory(Long userId);
}
