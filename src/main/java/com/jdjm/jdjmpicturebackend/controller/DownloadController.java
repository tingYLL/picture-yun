package com.jdjm.jdjmpicturebackend.controller;

import cn.hutool.core.util.ObjectUtil;
import com.jdjm.jdjmpicturebackend.common.BaseResponse;
import com.jdjm.jdjmpicturebackend.common.ResultUtils;
import com.jdjm.jdjmpicturebackend.exception.BusinessException;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.exception.ThrowUtils;
import com.jdjm.jdjmpicturebackend.model.dto.download.DownloadRequest;
import com.jdjm.jdjmpicturebackend.model.entity.User;
import com.jdjm.jdjmpicturebackend.model.vo.DownloadHistoryVO;
import com.jdjm.jdjmpicturebackend.service.DownloadService;
import com.jdjm.jdjmpicturebackend.service.UserService;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/download")
public class DownloadController {
    @Autowired
    private DownloadService downloadService;
    @Resource
    private UserService userService;


    //    @Limit(key = "PictureDownload:", count = 1, limitType = LimitType.IP, errMsg = "下载操作太频繁，请稍后再试!")
    @PostMapping
    public BaseResponse<Boolean> downloadFile(HttpServletRequest request, Long fileId) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(fileId), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long id = loginUser.getId();
        if (downloadService.canDownload(id)) {
            downloadService.logDownload(id, fileId);
            return ResultUtils.success(true);
        } else {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"下载次数不足");
        }
    }

    /**
     * 查看剩余下载次数
     * @param request
     * @return
     */
    @GetMapping("/remaining")
    public BaseResponse<Integer> getRemainingDownloads(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long id = loginUser.getId();
        return ResultUtils.success(downloadService.getRemainingDownloads(id));
    }

    /**
     * 分页获取用户下载历史记录
     * @param request HTTP请求
     * @param downloadRequest 查询参数（包含时间范围和分页）
     * @return 分页用户下载历史记录
     */
    @PostMapping("/history")
    public BaseResponse<Page<DownloadHistoryVO>> getDownloadHistory(HttpServletRequest request, @RequestBody DownloadRequest downloadRequest) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        Page<DownloadHistoryVO> downloadHistory = downloadService.getUserDownloadHistoryPage(userId, downloadRequest);
        return ResultUtils.success(downloadHistory);
    }
}