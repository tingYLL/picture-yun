package com.jdjm.jdjmpicturebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jdjm.jdjmpicturebackend.model.entity.DownloadLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DownloadLogMapper extends BaseMapper<DownloadLog> {
    @Select("SELECT COUNT(*) FROM download_logs WHERE user_id = #{userId} AND downloaded_at BETWEEN #{startDate} AND #{endDate}")
    Long countDownloadsByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 统计用户在指定时间范围内下载公共图库图片且消耗配额的次数（space_id 为 null 且 consume_quota = 1）
     */
    @Select("SELECT COUNT(*) FROM download_logs WHERE user_id = #{userId} AND space_id IS NULL AND consume_quota = 1 AND downloaded_at BETWEEN #{startDate} AND #{endDate}")
    Long countPublicDownloadsByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Select("SELECT COUNT(*) FROM download_logs WHERE user_id = #{userId} AND file_id = #{fileId} AND downloaded_at BETWEEN #{startDate} AND #{endDate}")
    Long countDownloadsByUserAndFileAndDateRange(
            @Param("userId") Long userId,
            @Param("fileId") Long fileId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Select("SELECT COUNT(*) FROM download_logs WHERE user_id = #{userId} AND file_id = #{fileId}")
    Long countDownloadsByUserAndFile(
            @Param("userId") Long userId,
            @Param("fileId") Long fileId
    );

    @Select("SELECT * FROM download_logs WHERE user_id = #{userId} ORDER BY downloaded_at DESC")
    List<DownloadLog> listDownloadHistoryByUserId(
            @Param("userId") Long userId
    );

    /**
     * 根据用户ID和时间范围查询下载历史记录
     * @param userId 用户ID
     * @param startTime 开始时间（可为null）
     * @param endTime 结束时间（可为null）
     * @return 下载历史记录列表
     */
    List<DownloadLog> listDownloadHistoryByUserIdAndTimeRange(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 分页根据用户ID和时间范围查询下载历史记录
     * @param page 分页参数
     * @param userId 用户ID
     * @param startTime 开始时间（可为null）
     * @param endTime 结束时间（可为null）
     * @return 分页下载历史记录
     */
    Page<DownloadLog> listDownloadHistoryByUserIdAndTimeRangePage(
            Page<DownloadLog> page,
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}