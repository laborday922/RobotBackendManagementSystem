package com.ruoyi.function.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class MapFileUtil {

    private static final Logger log = LoggerFactory.getLogger(MapFileUtil.class);

    public static String saveFile(MultipartFile file, String uploadPath) throws IOException {
        // 确保目录存在
        Path path = Paths.get(uploadPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

        // 保存文件
        Path filePath = path.resolve(fileName);
        file.transferTo(filePath.toFile());

        return fileName;
    }

    public static void deleteFile(String uploadPath, String fileName) {
        try {
            Path filePath = Paths.get(uploadPath, fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("删除文件失败: {}", fileName, e);
        }
    }

    public static String getFileUrl(String uploadPath, String fileName) {
        return "/uploads/map/" + fileName;
    }
}