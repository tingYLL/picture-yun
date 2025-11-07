package com.jdjm.jdjmpicturebackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 文件上传配置属性
 * 统一管理文件上传相关的配置
 */
@Component
@ConfigurationProperties(prefix = "image.upload")
@Data
public class UploadProperties {

    /**
     * 文件上传目录
     * 默认为项目根目录下的 uploads 文件夹
     * 用户可以在 application.yml 中通过 image.upload.dir 配置自定义路径
     */
    private String dir = System.getProperty("user.dir") + File.separator + "uploads";

    /**
     * 获取上传目录，如果目录不存在则自动创建
     * @return 上传目录的绝对路径
     */
    public String getDir() {
        File directory = new File(dir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return dir;
    }
}