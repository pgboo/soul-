package com.soul.social.controller.mini;

import com.soul.social.client.FileStorageService;
import com.soul.social.common.result.Result;
import com.soul.social.dto.resp.UploadResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户端文件上传接口 /api/mini/upload。
 */
@RestController
@RequestMapping("/api/mini/upload")
public class UploadController {

    @Resource
    private FileStorageService fileStorageService;

    /** 上传图片（multipart/form-data） */
    @PostMapping("/image")
    public Result<UploadResp> uploadImage(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "bizType", required = false) String bizType) {
        String url = fileStorageService.upload(file, bizType);
        UploadResp resp = new UploadResp();
        resp.setUrl(url);
        return Result.success("上传成功", resp);
    }
}
