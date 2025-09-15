package com.jdjm.jdjmpicturebackend.model.vo;

import cn.hutool.json.JSONUtil;
import com.jdjm.jdjmpicturebackend.model.entity.Picture;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Component
public class PictureVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图片 url
     */
    private String url;

    /**
     * 缩略图 url
     */
    private String thumbnailUrl;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 分类
     */
    private String category;

    /**
     * 文件体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 图片主色调
     */
    private String picColor;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 是否本机存储 (静态字段)
     */
    private static Boolean isLocalStore;
    private static String port;
    private static String contextPath;

    // 用于接收配置值的非静态字段（名称可任意）或直接通过Setter注入到静态字段
    @Value("${image.local.enable}")
    private Boolean localEnableTemp;
    @Value("${server.port}")
    private String portTemp;
    @Value("${server.servlet.context-path}")
    private String contextPathTemp;


    /**
     * 在Bean初始化完成后，将注入的值赋值给静态变量
     */
    @PostConstruct
    public void initConfig() {
        isLocalStore = localEnableTemp;
        port = portTemp;
        contextPath = contextPathTemp;
    }
    /**
     * 权限列表
     */
    private List<String> permissionList = new ArrayList<>();

    private static final long serialVersionUID = 1L;

    /**
     * 封装类转对象
     */
    public static Picture voToObj(PictureVO pictureVO) {
        if (pictureVO == null) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureVO, picture);
        // 类型不同，需要转换
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;
    }

    /**
     * 对象转封装类
     */
    public static PictureVO objToVo(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtils.copyProperties(picture, pictureVO);
        // 类型不同，需要转换
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        //判断图片存储位置 如果是Local 需补充完整url
        if(isLocalStore){
            pictureVO.setUrl("http://localhost:"+port+contextPath+picture.getUrl());
        }
        return pictureVO;
    }
}