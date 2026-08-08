package com.ruoyi.taskmgt.domain.vo;

import lombok.Data;

@Data
public class SumOfInteractionHistoryVo {
    //总交互次数
   private Long totalTimes;
    //好评数
    private Long goodTimes;
    //中评数
    private Long midTimes;
    //差评数
    private Long badTimes;
    //平均分
    private Double averageRating;

    public void calculateAverageRating() {
        // 1. 防止总次数为0导致除零异常
        if (totalTimes == null || totalTimes == 0) {
            this.averageRating = 0.0;
            return;
        }

        // 2. 空值处理：避免null值参与计算（数据库查询结果可能为null）
        long good = goodTimes == null ? 0 : goodTimes;
        long mid = midTimes == null ? 0 : midTimes;
        long bad = badTimes == null ? 0 : badTimes;

        // 3. 计算总评分（核心：好评*10 + 中评*5 + 差评*0）
        long totalScore = good * 10 + mid * 5 + bad * 0;

        // 4. 计算平均分并保留1位小数（四舍五入）
        double rawAverage = (double) totalScore / totalTimes;
        this.averageRating = Math.round(rawAverage * 10) / 10.0;
    }
}
