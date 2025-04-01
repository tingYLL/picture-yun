package com.jdjm.jdjmpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.jdjm.jdjmpicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class JdjmPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(JdjmPictureBackendApplication.class, args);
    }

}
