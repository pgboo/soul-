package com.soul.social.client;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储抽象。默认本地磁盘实现；OSS 实现预留。
 */
public interface FileStorageService {

    /**
     * 上传文件，返回可访问 URL（相对路径或全路径）。
     *
     * @param file    文件
     * @param bizType 业务类型目录（avatar/dynamic）
     * @return 可访问 URL
     */
    String upload(MultipartFile file, String bizType);

    /**
     * 删除文件（按上传返回的 URL）。
     */
    void delete(String url);
}
