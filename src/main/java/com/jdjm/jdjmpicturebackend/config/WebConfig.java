package com.jdjm.jdjmpicturebackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${image.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /images/** 这个URL路径映射到本地目录
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadDir + File.separator);

        // 将 /images/static/** 映射到 classpath:/static/ 目录，用于访问默认头像等静态资源
        registry.addResourceHandler("/images/static/**")
                .addResourceLocations("classpath:/static/");
    }
}