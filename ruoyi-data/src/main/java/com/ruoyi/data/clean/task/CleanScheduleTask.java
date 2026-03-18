package com.ruoyi.data.clean.task;

import com.ruoyi.data.clean.mapper.CleanRuleMapper;
import com.ruoyi.data.clean.mapper.po.CleanRulePo;
import com.ruoyi.data.clean.service.ICleanRuleExecuteService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class CleanScheduleTask {

    @Resource
    private CleanRuleMapper cleanRuleMapper;

    @Resource
    private ICleanRuleExecuteService executeService;

    /**
     * 每分钟扫描一次定时任务
     */
    @Scheduled(fixedDelay = 60000)
    public void scan() {

        List<CleanRulePo> rules = cleanRuleMapper.selectScheduledRules();

        for (CleanRulePo rule : rules) {

            CronExpression cron = CronExpression.parse(rule.getCronExpression());

            LocalDateTime now = LocalDateTime.now();

            LocalDateTime last = cron.next(now.minusMinutes(1));

            if (last != null && last.isBefore(now)) {

                executeService.executeTask(rule.getId());
            }
        }
    }
}