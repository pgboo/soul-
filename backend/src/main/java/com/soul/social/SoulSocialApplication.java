package com.soul.social;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Soul 风格年轻人社交系统一期 — 启动类。
 *
 * @author Alex(寇豆码)
 */
@EnableScheduling
@MapperScan("com.soul.social.mapper")
@SpringBootApplication
public class SoulSocialApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoulSocialApplication.class, args);
    }
}
