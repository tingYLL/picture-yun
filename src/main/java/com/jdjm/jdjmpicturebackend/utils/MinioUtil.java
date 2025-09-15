package com.jdjm.jdjmpicturebackend.utils;

import com.jdjm.jdjmpicturebackend.config.MinioConfig;
import com.qcloud.cos.utils.IOUtils;
import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

//@Service
public class MinioUtil {
    private static final Logger log = LoggerFactory.getLogger(MinioUtil.class);

    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioConfig minioConfig;

    @PostConstruct
    public void init() {
        existBucket(minioConfig.getBucketName());
    }


    /**
     * 判断bucket是否存在，不存在则创建
     */
    public boolean existBucket(String bucketName) {
        boolean exists;
        try {
            exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                exists = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            exists = false;
        }
        return exists;
    }

    /**
     * 上传文件
     */
    public void upload(MultipartFile file, String fileName) {
        // 使用putObject上传一个文件到存储桶中。
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文件访问地址
     */
    public String getFileUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 下载一个文件（返回文件流）
     */
    public InputStream download(String objectName) throws Exception {
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(minioConfig.getBucketName()).object(objectName).build());
        return stream;
    }


    /**
     * 下载文件
     */
    public void download(HttpServletResponse response, String newFileName, String saveFileName) {
        InputStream in = null;
        try {
            // 获取对象信息
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(saveFileName)
                    .build());

            // 设置请求头Content-Type
            response.setContentType(stat.contentType());

            // 确保使用 UTF-8 编码
//            String encodedFileName = encodeFilename(newFileName);
            String encodedFileName = URLEncoder.encode(newFileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

            // 设置禁用缓存
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            // 设置文件大小
            long fileSize = stat.size();
            response.setContentLengthLong(fileSize);

            // 获取文件输入流
            in = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(saveFileName)
                    .build());

            // 文件下载
            IOUtils.copy(in, response.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "File download failed: " + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 删除文件
     */
    public void delete(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(minioConfig.getBucketName()).object(fileName).build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 判断文件是否存在
     *
     * @param objectName
     */
    public boolean isFileExist(String objectName) {
        boolean exist = true;
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(minioConfig.getBucketName()).object(objectName).build());
        } catch (Exception e) {
            log.error("[Minio工具类]>>>> 判断文件是否存在, 异常：", e);
            exist = false;
        }
        return exist;
    }

}
