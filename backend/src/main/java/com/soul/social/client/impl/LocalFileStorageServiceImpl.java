package com.soul.social.client.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.soul.social.client.FileStorageService;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 本地磁盘文件存储实现（默认启用）。
 * <p>目录：{baseDir}/{bizType}/{yyyyMM}/{uuid}.{ext}
 * <p>URL：{domain}/upload/{bizType}/{yyyyMM}/{uuid}.{ext}
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "soul.file.storage", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageServiceImpl implements FileStorageService {

    /** 允许的图片后缀白名单 */
    private static final List<String> ALLOWED_EXT =
            Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    @Value("${soul.file.local.base-dir:./upload}")
    private String baseDir;

    @Value("${soul.file.local.domain:}")
    private String domain;

    @Value("${soul.file.local.max-size:5242880}")
    private long maxSize;

    /**
     * 解析为绝对路径的基准目录。
     * <p>必须使用绝对路径：{@code MultipartFile#transferTo} 对相对路径是按
     * Servlet 容器的临时工作目录解析的（Tomcat 下为
     * {@code {java.io.tmpdir}/tomcat.xxx/work/Tomcat/localhost/ROOT}），
     * 与 {@code Files.createDirectories} 基于 JVM 工作目录的解析结果并不一致，
     * 会导致文件写入必然抛 FileNotFoundException。
     */
    private Path basePath;

    @PostConstruct
    public void init() {
        basePath = Paths.get(baseDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            log.warn("本地文件存储目录创建失败: {}", basePath, e);
        }
        log.info("LocalFileStorageServiceImpl 已启用，baseDir={}", basePath);
    }

    @Override
    public String upload(MultipartFile file, String bizType) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "上传文件不能为空");
        }
        if (file.getSize() > maxSize) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "文件大小超过限制（" + (maxSize / 1024 / 1024) + "MB）");
        }
        String originalName = file.getOriginalFilename();
        String ext = StrUtil.isBlank(originalName)
                ? "" : FileUtil.extName(originalName).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "仅支持图片格式: " + String.join("/", ALLOWED_EXT));
        }

        String safeBizType = StrUtil.isBlank(bizType) ? "common" : bizType.replaceAll("[^a-zA-Z0-9_\\-]", "");
        String yyyyMM = DateUtil.format(new Date(), "yyyyMM");
        String fileName = IdUtil.fastSimpleUUID() + "." + ext;

        Path targetDir = basePath.resolve(safeBizType).resolve(yyyyMM);
        Path target = targetDir.resolve(fileName);
        try {
            Files.createDirectories(targetDir);
            // 用流拷贝而非 transferTo：transferTo 的相对路径语义依赖 Servlet 容器实现，
            // 会被拼到容器临时目录下从而抛 FileNotFoundException。
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("文件写入失败: {}", target, e);
            throw new BizException(ResultCode.SYSTEM_ERROR.getCode(), "文件上传失败");
        }

        String relative = "/upload/" + safeBizType + "/" + yyyyMM + "/" + fileName;
        String url = StrUtil.isBlank(domain) ? relative : domain + relative;
        log.info("文件上传成功: {}", url);
        return url;
    }

    @Override
    public void delete(String url) {
        if (StrUtil.isBlank(url)) {
            return;
        }
        // 仅处理本地相对路径（/upload/...）
        int idx = url.indexOf("/upload/");
        if (idx < 0) {
            return;
        }
        String relative = url.substring(idx + "/upload/".length());
        try {
            Path path = basePath.resolve(relative);
            Files.deleteIfExists(path);
            log.info("文件已删除: {}", path);
        } catch (IOException e) {
            log.warn("文件删除失败: {}", url, e);
        }
    }
}
