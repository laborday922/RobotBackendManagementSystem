package com.ruoyi.function.util;

import com.ruoyi.function.TestApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("地图文件工具类测试")
class MapFileUtilTest {

    private static final String TEST_UPLOAD_PATH = "./test-uploads/map/";
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        testFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        // 清理测试文件
        Path uploadDir = Paths.get(TEST_UPLOAD_PATH);
        if (Files.exists(uploadDir)) {
            Files.walk(uploadDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // ignore
                        }
                    });
            Files.deleteIfExists(uploadDir);
        }
    }

    @Test
    @DisplayName("保存文件 - 成功")
    void testSaveFile() throws IOException {
        String fileName = MapFileUtil.saveFile(testFile, TEST_UPLOAD_PATH);
        assertThat(fileName).isNotNull();
        assertThat(fileName).endsWith(".jpg");

        Path savedFile = Paths.get(TEST_UPLOAD_PATH, fileName);
        assertThat(Files.exists(savedFile)).isTrue();
    }

    @Test
    @DisplayName("删除文件 - 成功")
    void testDeleteFile() throws IOException {
        String fileName = MapFileUtil.saveFile(testFile, TEST_UPLOAD_PATH);
        Path savedFile = Paths.get(TEST_UPLOAD_PATH, fileName);
        assertThat(Files.exists(savedFile)).isTrue();

        MapFileUtil.deleteFile(TEST_UPLOAD_PATH, fileName);
        assertThat(Files.exists(savedFile)).isFalse();
    }

    @Test
    @DisplayName("删除不存在的文件 - 无异常")
    void testDeleteNonExistentFile() {
        // 不应该抛出异常
        MapFileUtil.deleteFile(TEST_UPLOAD_PATH, "nonexistent.jpg");
    }

    @Test
    @DisplayName("获取文件URL - 成功")
    void testGetFileUrl() {
        String fileName = "test.jpg";
        String url = MapFileUtil.getFileUrl(TEST_UPLOAD_PATH, fileName);
        assertThat(url).isEqualTo("/uploads/map/test.jpg");
    }
}