package com.soul.social.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * MyBatis-Plus 配置：分页插件 + 公共字段自动填充。
 * <p>逻辑删除通过 application.yml 全局配置 {@code logic-delete-field=isDelete}。
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 分页插件。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 公共字段自动填充：create_time / update_time。
     */
    @Component
    public static class MyMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            LocalDateTime now = LocalDateTime.now();
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
            // 逻辑删除字段默认 0（若实体未显式赋值）
            this.strictInsertFill(metaObject, "isDelete", Integer.class, 0);
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }
    }
}
