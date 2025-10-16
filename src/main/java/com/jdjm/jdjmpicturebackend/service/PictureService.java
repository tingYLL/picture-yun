package com.jdjm.jdjmpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jdjm.jdjmpicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.jdjm.jdjmpicturebackend.model.dto.picture.*;
import com.jdjm.jdjmpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jdjm.jdjmpicturebackend.model.entity.User;
import com.jdjm.jdjmpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    PictureVO uploadLocal(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);

    PictureVO uploadPicture (Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                 User loginUser);

    /**
     * 获取查询对象
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取查询对象
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapperMultiple(PictureQueryRequest pictureQueryRequest);
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

    /**
     * 编辑图片
     *
     * @param pictureEditRequest
     * @param loginUser
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 图片审核
     *
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);


    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 校验空间图片的权限
     *
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 删除图片
     *
     * @param pictureId
     * @param loginUser
     */
    void deletePicture(long pictureId, User loginUser);

    /**
     * 清理图片文件 (cos）
     *
     * @param oldPicture
     */
    void clearPictureFile(Picture oldPicture);

    /**
     * 清理图片文件 (cos）
     *
     * @param oldPicture
     */
    void clearPictureFileLocal(Picture oldPicture);

    /**
     * 根据颜色搜索图片
     *
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 批量编辑图片
     *
     * @param pictureEditByBatchRequest
     * @param loginUser
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    /**
     * 创建扩图任务
     *
     * @param createPictureOutPaintingTaskRequest
     * @param loginUser
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);

    PictureVO getPictureDetailById(long id,HttpServletRequest request);

    /**
     * 图片点赞、收藏
     *
     * @param pictureInteractionRequest 图片互动请求
     */
    void pictureLikeOrCollect(PictureInteractionRequest pictureInteractionRequest,User user);

    void pictureShare(Long pictureId);

    String pictureDownload(Long pictureId);

    Page<PictureVO> getPicturePageListAsHome(PictureQueryRequest pictureQueryRequest,HttpServletRequest request);

    Page<PictureVO> getPicturePageListAsPersonRelease(PictureQueryRequest pictureQueryRequest,HttpServletRequest request);

    public void updateInteractionNumByRedis(Long pictureId, Integer interactionType, int num);

    /**
     * 获取我的收藏列表
     *
     * @param pictureQueryRequest 图片查询请求
     * @param loginUser 当前登录用户
     * @return 收藏的图片分页列表
     */
    Page<PictureVO> getMyCollectPicturePage(PictureQueryRequest pictureQueryRequest, User loginUser);
}
