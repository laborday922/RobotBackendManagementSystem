package com.ruoyi.function.constants;

/**
 * 功能模块常量
 */
public class FunctionConstants {

    /** 地图文件上传路径 */
    public static final String MAP_UPLOAD_PATH = "/map";

    /** 音频文件上传路径 */
    public static final String AUDIO_UPLOAD_PATH = "/audio";

    /** 媒体文件上传路径 */
    public static final String MEDIA_UPLOAD_PATH = "/media";

    /** 允许的图片扩展名 */
    public static final String[] ALLOW_IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};

    /** 允许的音频扩展名 */
    public static final String[] ALLOW_AUDIO_EXTENSIONS = {".mp3", ".wav", ".aac", ".ogg"};

    /** 允许的视频扩展名 */
    public static final String[] ALLOW_VIDEO_EXTENSIONS = {".mp4", ".avi", ".mov", ".flv"};

    /** 默认地图名称前缀 */
    public static final String DEFAULT_MAP_NAME_PREFIX = "地图_";

    /** 默认点位顺序起始值 */
    public static final int DEFAULT_ORDER_NUM = 0;

    /** 默认语速 */
    public static final int DEFAULT_SPEECH_RATE = 100;

    /** 默认间隔时间（毫秒） */
    public static final int DEFAULT_INTERVAL_TIME = 1000;

    /** 缓冲区大小 */
    public static final int BUFFER_SIZE = 4096;
}