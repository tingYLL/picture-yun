package com.jdjm.jdjmpicturebackend.config;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class ImageDownloadFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();

        // 判断是否为图片请求（根据路径特征）
        if (isImageRequest(requestURI)) {
            // 设置强制下载头
            String filename = extractFilename(requestURI);
            httpResponse.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            // 可选：设置缓存控制头，避免浏览器缓存干扰下载行为
            httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Expires", "0");
        }

        chain.doFilter(request, response);
    }

    /**
     * 判断是否为图片请求
     */
    private boolean isImageRequest(String uri) {
        // 根据你的实际路由结构调整这里的判断条件
        return uri.startsWith("/api/images/") &&
                uri.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$");
    }

    /**
     * 从URI中提取文件名
     */
    private String extractFilename(String uri) {
        return uri.substring(uri.lastIndexOf("/") + 1);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化方法，可按需实现
    }

    @Override
    public void destroy() {
        // 销毁方法，可按需实现
    }
}