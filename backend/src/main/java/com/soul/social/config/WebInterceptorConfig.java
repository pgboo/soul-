package com.soul.social.config;

import com.soul.social.interceptor.AdminAuthInterceptor;
import com.soul.social.interceptor.AuthInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器注册：用户端 {@link AuthInterceptor} 与后台 {@link AdminAuthInterceptor}。
 */
@Configuration
public class WebInterceptorConfig implements WebMvcConfigurer {

    @Resource
    private AuthInterceptor authInterceptor;

    @Resource
    private AdminAuthInterceptor adminAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 用户端
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/mini/**")
                .excludePathPatterns(
                        "/api/mini/auth/login",
                        "/api/mini/auth/code",
                        "/api/mini/auth/logout"
                );

        // 后台
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/login");
    }
}
