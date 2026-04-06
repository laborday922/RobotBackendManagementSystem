package com.ruoyi.function.util;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.function.constants.FunctionConstants;
import com.ruoyi.function.exception.FunctionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 * 地图文件工具类
 */
public class MapFileUtil {

    private static final Logger log = LoggerFactory.getLogger(MapFileUtil.class);

    /**
     * 验证图片文件扩展名
     */
    public static boolean isValidImageExtension(String fileName) {
        if (fileName == null) {
            return false;
        }
        String extension = getExtension(fileName);
        return Arrays.asList(FunctionConstants.ALLOW_IMAGE_EXTENSIONS).contains(extension.toLowerCase());
    }

    /**
     * 验证音频文件扩展名
     */
    public static boolean isValidAudioExtension(String fileName) {
        if (fileName == null) {
            return false;
        }
        String extension = getExtension(fileName);
        return Arrays.asList(FunctionConstants.ALLOW_AUDIO_EXTENSIONS).contains(extension.toLowerCase());
    }

    /**
     * 获取文件扩展名
     */
    public static String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 生成唯一文件名
     */
    public static String generateUniqueFileName(String originalFilename) {
        String extension = getExtension(originalFilename);
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    /**
     * 获取文件完整存储路径
     */
    public static String getFullPath(String subPath, String fileName) {
        return RuoYiConfig.getUploadPath() + subPath + File.separator + fileName;
    }

    /**
     * 确保目录存在
     */
    public static void ensureDirectoryExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 保存文件
     */
    public static String saveFile(MultipartFile file, String subPath) throws IOException {
        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (!isValidImageExtension(originalFilename)) {
            throw FunctionException.unsupportedFileType(getExtension(originalFilename));
        }

        // 生成文件名
        String newFileName = generateUniqueFileName(originalFilename);

        // 构建上传路径
        String uploadPath = RuoYiConfig.getUploadPath() + subPath;
        ensureDirectoryExists(uploadPath);

        // 保存文件
        File destFile = new File(uploadPath + File.separator + newFileName);
        file.transferTo(destFile);

        log.info("文件保存成功：{}", destFile.getAbsolutePath());
        return newFileName;
    }

    /**
     * 删除文件
     */
    public static boolean deleteFile(String subPath, String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        String fullPath = getFullPath(subPath, fileName);
        File file = new File(fullPath);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                log.info("文件删除成功：{}", fullPath);
            } else {
                log.warn("文件删除失败：{}", fullPath);
            }
            return deleted;
        }
        return false;
    }

    /**
     * 判断文件是否存在
     */
    public static boolean fileExists(String subPath, String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        String fullPath = getFullPath(subPath, fileName);
        return new File(fullPath).exists();
    }

    /**
     * 获取Content-Type
     */
    public static String getContentType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }
        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowerFileName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFileName.endsWith(".bmp")) {
            return "image/bmp";
        } else if (lowerFileName.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFileName.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (lowerFileName.endsWith(".wav")) {
            return "audio/wav";
        } else if (lowerFileName.endsWith(".mp4")) {
            return "video/mp4";
        } else {
            return "application/octet-stream";
        }
    }
}