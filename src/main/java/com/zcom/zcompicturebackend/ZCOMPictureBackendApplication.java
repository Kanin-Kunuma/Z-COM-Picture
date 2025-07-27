package com.zcom.zcompicturebackend;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})
@EnableAsync
@MapperScan("com.zcom.zcompicturebackend.mapper")

public class ZCOMPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZCOMPictureBackendApplication.class, args);
    }

}
