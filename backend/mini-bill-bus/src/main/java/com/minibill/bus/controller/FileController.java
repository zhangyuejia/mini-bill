package com.minibill.bus.controller;

import com.minibill.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传下载控制器
 */
@Slf4j
@Tag(name = "文件管理")
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${minibill.file.upload-path:./upload}")
    private String uploadPath;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(required = false) String prefix) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        try {
            // 确保目录存在
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名（带业务前缀）
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String bizPrefix = (prefix != null && !prefix.isEmpty()) ? prefix + "_" : "";
            String fileName = bizPrefix + UUID.randomUUID().toString().replace("-", "") + ext;

            // 保存文件
            Path targetPath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath);

            String fileUrl = "/bus/api/file/download/" + fileName;
            log.info("文件上传成功: {} -> {}", originalName, fileUrl);

            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败");
        }
    }

    @Operation(summary = "下载/预览文件")
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable String fileName, HttpServletResponse response) {
        try {
            Path filePath = Paths.get(uploadPath).resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            log.error("文件下载失败", e);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("文件类型检测失败", e);
            return ResponseEntity.ok().build();
        }
    }
}
