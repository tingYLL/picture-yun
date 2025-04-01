package com.jdjm.jdjmpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jdjm.jdjmpicturebackend.model.dto.picture.PictureQueryRequest;
import com.jdjm.jdjmpicturebackend.model.dto.picture.PictureUploadRequest;
import com.jdjm.jdjmpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jdjm.jdjmpicturebackend.model.entity.User;
import com.jdjm.jdjmpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author jdjm
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-03-30 18:00:15
*/
public interface PictureService extends IService<Picture> {
    /**
     * 校验图片
     *
     * @param picture
     */
    void validPicture(Picture picture);

    PictureVO uploadPicture (MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取查询对象
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片包装类（单条）
     *
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片包装类（分页）
     *
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);
}
