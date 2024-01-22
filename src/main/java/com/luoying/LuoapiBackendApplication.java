package com.luoying;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.luoying.mapper")
@EnableDubbo
@EnableScheduling
public class LuoapiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuoapiBackendApplication.class, args);
    }

}
