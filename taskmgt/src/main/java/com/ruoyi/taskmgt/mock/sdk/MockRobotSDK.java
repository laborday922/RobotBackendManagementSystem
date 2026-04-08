//package com.ruoyi.taskmgt.mock.sdk;
//
//import lombok.Builder;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.*;
//import java.util.concurrent.*;
//
///**
// * 模拟机器人SDK - 模拟真实机器人的各种操作
// */
//@Slf4j
//public class MockRobotSDK {
//
//    private final Long robotId;
//    private final String robotIp;
//    private final ExecutorService executor = Executors.newCachedThreadPool();
//    private final Map<String, MockTask> runningTasks = new ConcurrentHashMap<>();
//    private final Random random = new Random();
//
//    // 模拟机器人状态
//    private volatile MockRobotStatus status = MockRobotStatus.IDLE;
//    private volatile int battery = 80;
//    private volatile double currentX = 0, currentY = 0, currentZ = 0;
//
//    public MockRobotSDK(Long robotId, String robotIp) {
//        this.robotId = robotId;
//        this.robotIp = robotIp;
//        log.info("[模拟SDK] 机器人{}初始化完成, IP={}", robotId, robotIp);
//    }
//
//    // ==================== 移动操作 ====================
//
//    @Data
//    @Builder
//    public static class MoveRequest {
//        private double x, y, z;
//        private int speed;  // 1-100
//        private String coordinateType; // ABSOLUTE/RELATIVE
//    }
//
//    @Data
//    @Builder
//    public static class MoveResult {
//        private boolean success;
//        private String taskId;
//        private boolean async;
//        private int estimatedTime; // 预计耗时秒
//        private String message;
//        private double finalX, finalY, finalZ;
//    }
//
//    /**
//     * 同步移动 - 阻塞直到完成
//     */
//    public MoveResult syncMove(MoveRequest req, int timeoutMs) {
//        log.info("[模拟SDK-{}] 开始同步移动: ({}, {}, {}), 速度={}",
//                robotId, req.x, req.y, req.z, req.speed);
//
//        status = MockRobotStatus.MOVING;
//
//        // 模拟移动耗时：距离/速度 * 系数
//        double distance = Math.sqrt(
//                Math.pow(req.x - currentX, 2) +
//                        Math.pow(req.y - currentY, 2) +
//                        Math.pow(req.z - currentZ, 2)
//        );
//        int moveTime = (int) (distance / req.speed * 1000);
//        moveTime = Math.max(moveTime, 1000); // 最少1秒
//
//        if (moveTime > timeoutMs) {
//            status = MockRobotStatus.IDLE;
//            return MoveResult.builder()
//                    .success(false)
//                    .message("移动超时，预计需要" + moveTime + "ms")
//                    .build();
//        }
//
//        // 模拟执行时间
//        sleep(moveTime);
//
//        // 更新位置
//        currentX = req.x;
//        currentY = req.y;
//        currentZ = req.z;
//        status = MockRobotStatus.IDLE;
//
//        // 模拟偶尔的失败（10%概率）
//        if (random.nextInt(10) == 0) {
//            return MoveResult.builder()
//                    .success(false)
//                    .message("移动过程中遇到障碍物")
//                    .build();
//        }
//
//        return MoveResult.builder()
//                .success(true)
//                .async(false)
//                .message("移动完成")
//                .finalX(currentX)
//                .finalY(currentY)
//                .finalZ(currentZ)
//                .build();
//    }
//
//    /**
//     * 异步移动 - 立即返回taskId，后台执行
//     */
//    public MoveResult asyncMove(MoveRequest req, MoveCallback callback) {
//        String taskId = "MOVE-" + robotId + "-" + System.currentTimeMillis();
//
//        double distance = Math.sqrt(
//                Math.pow(req.x - currentX, 2) +
//                        Math.pow(req.y - currentY, 2)
//        );
//        int estimatedTime = (int) (distance / req.speed * 1000) + 2000; // 加2秒缓冲
//
//        MockTask task = MockTask.builder()
//                .taskId(taskId)
//                .type("MOVE")
//                .status("RUNNING")
//                .progress(0)
//                .build();
//        runningTasks.put(taskId, task);
//
//        // 后台执行
//        executor.submit(() -> {
//            try {
//                status = MockRobotStatus.MOVING;
//
//                // 模拟进度更新
//                int steps = 10;
//                for (int i = 0; i <= steps; i++) {
//                    sleep(estimatedTime / steps);
//                    task.setProgress(i * 10);
//
//                    if (callback != null) {
//                        callback.onProgress(taskId, i * 10, "MOVING");
//                    }
//                }
//
//                // 更新位置
//                currentX = req.x;
//                currentY = req.y;
//                currentZ = req.z;
//
//                task.setStatus("COMPLETED");
//                task.setProgress(100);
//
//                if (callback != null) {
//                    callback.onComplete(taskId, Map.of(
//                            "x", currentX, "y", currentY, "z", currentZ
//                    ));
//                }
//
//            } catch (Exception e) {
//                task.setStatus("FAILED");
//                if (callback != null) {
//                    callback.onError(taskId, "500", e.getMessage());
//                }
//            } finally {
//                status = MockRobotStatus.IDLE;
//                runningTasks.remove(taskId);
//            }
//        });
//
//        return MoveResult.builder()
//                .success(true)
//                .async(true)
//                .taskId(taskId)
//                .estimatedTime(estimatedTime)
//                .message("异步移动任务已提交")
//                .build();
//    }
//
//    // ==================== 机械臂操作 ====================
//
//    @Data
//    @Builder
//    public static class ArmRequest {
//        private String action;  // GRAB/RELEASE/ROTATE
//        private String targetType;
//        private double force;
//        private Map<String, Object> position;
//    }
//
//    public boolean syncGrab(ArmRequest req, int timeoutMs) {
//        log.info("[模拟SDK-{}] 开始抓取: target={}, force={}",
//                robotId, req.targetType, req.force);
//
//        status = MockRobotStatus.ARM_BUSY;
//        sleep(3000); // 抓取固定3秒
//
//        status = MockRobotStatus.IDLE;
//
//        // 模拟5%失败率
//        return random.nextInt(20) != 0;
//    }
//
//    // ==================== 视觉识别 ====================
//
//    @Data
//    @Builder
//    public static class VisionResult {
//        private boolean recognized;
//        private List<DetectedObject> objects;
//        private String imageBase64; // 模拟图片
//    }
//
//    @Data
//    public static class DetectedObject {
//        private String type;
//        private double confidence;
//        private int x, y, width, height;
//    }
//
//    public VisionResult recognize(String algorithmType, int timeoutMs) {
//        log.info("[模拟SDK-{}] 开始视觉识别: algorithm={}", robotId, algorithmType);
//
//        status = MockRobotStatus.CAMERA_BUSY;
//        sleep(1500); // 识别耗时
//
//        status = MockRobotStatus.IDLE;
//
//        // 模拟识别结果
//        List<DetectedObject> objects = new ArrayList<>();
//        if (random.nextBoolean()) {
//            objects.add(new DetectedObject() {{
//                setType(random.nextBoolean() ? "box" : "ball");
//                setConfidence(0.85 + random.nextDouble() * 0.14);
//                setX(100); setY(200);
//                setWidth(50); setHeight(50);
//            }});
//        }
//
//        return VisionResult.builder()
//                .recognized(!objects.isEmpty())
//                .objects(objects)
//                .imageBase64("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==")
//                .build();
//    }
//
//    // ==================== 状态查询 ====================
//
//    public MockRobotStatus getStatus() {
//        // 模拟电量消耗
//        if (random.nextInt(10) == 0 && battery > 0) {
//            battery--;
//        }
//        return status;
//    }
//
//    public MockTask queryTask(String taskId) {
//        return runningTasks.get(taskId);
//    }
//
//    public boolean cancelTask(String taskId) {
//        MockTask task = runningTasks.get(taskId);
//        if (task != null) {
//            task.setStatus("CANCELLED");
//            runningTasks.remove(taskId);
//            status = MockRobotStatus.IDLE;
//            log.info("[模拟SDK-{}] 任务{}已取消", robotId, taskId);
//            return true;
//        }
//        return false;
//    }
//
//    // ==================== 回调接口 ====================
//
//    public interface MoveCallback {
//        void onProgress(String taskId, int progress, String status);
//        void onComplete(String taskId, Map<String, Object> result);
//        void onError(String taskId, String errorCode, String message);
//    }
//
//    // ==================== 内部方法 ====================
//
//    private void sleep(long ms) {
//        try {
//            Thread.sleep(ms);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//
//    @Data
//    @Builder
//    public static class MockTask {
//        private String taskId;
//        private String type;
//        private String status; // RUNNING/COMPLETED/FAILED/CANCELLED
//        private int progress; // 0-100
//    }
//
//    public enum MockRobotStatus {
//        IDLE, MOVING, ARM_BUSY, CAMERA_BUSY, ERROR, CHARGING
//    }
//}