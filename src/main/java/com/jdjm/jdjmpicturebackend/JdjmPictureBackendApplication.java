package com.jdjm.jdjmpicturebackend;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication(exclude = ShardingSphereAutoConfiguration.class)
@MapperScan("com.jdjm.jdjmpicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800) // 设置 Session 超时时间（秒），这里设为30分钟
public class JdjmPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(JdjmPictureBackendApplication.class, args);
    }

}
