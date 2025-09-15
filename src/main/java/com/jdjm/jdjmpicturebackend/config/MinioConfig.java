package com.jdjm.jdjmpicturebackend.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.context.annotation.Bean;

@Data
//@Configuration
//@ConfigurationProperties(prefix = "minio")
public class MinioConfig {
    private String accessKey;

    private String secretKey;

    private String url;

    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .region("cn-north-1")
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }
}
