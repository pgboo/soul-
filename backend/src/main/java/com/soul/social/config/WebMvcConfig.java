package com.soul.social.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：静态资源映射（本地文件）+ 跨域 CORS。
 * <p>拦截器注册在 {@link com.soul.social.config.WebInterceptorConfig}（需依赖拦截器 Bean）。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${soul.file.local.base-dir:./upload}")
    private String baseDir;

    @Value("${soul.cors.allowed-origins:*}")
    private String allowedOrigins;

    /**
     * 本地上传文件的静态资源映射：/upload/** → file:{baseDir}/
     * <p>baseDir 解析为绝对路径，与文件写入端保持一致，避免因工作目录差异映射到错误位置。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = Paths.get(baseDir).toAbsolutePath().normalize();
        String location = dir.toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(location);
    }

    /**
     * 跨域配置。允许前端 Vite dev server 访问。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
