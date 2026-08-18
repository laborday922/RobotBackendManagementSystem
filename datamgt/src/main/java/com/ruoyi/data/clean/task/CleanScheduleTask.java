package com.ruoyi.data.clean.task;

import com.ruoyi.data.clean.mapper.CleanRuleMapper;
import com.ruoyi.data.clean.mapper.po.CleanRulePo;
import com.ruoyi.data.clean.service.ICleanRuleExecuteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class CleanScheduleTask {

    private static final Logger log = LoggerFactory.getLogger(CleanScheduleTask.class);

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
            try {
                if (rule.getCronExpression() == null || rule.getCronExpression().isEmpty()) {
                    log.warn("定时任务[{}]缺少 cron 表达式，跳过执行", rule.getId());
                    continue;
                }

                CronExpression cron = CronExpression.parse(rule.getCronExpression());
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime last = cron.next(now.minusMinutes(1));

                if (last != null && !last.isAfter(now)) {
                    executeService.executeScheduled(rule.getId());
                }
            } catch (Exception e) {
                log.error("定时任务[{}]执行失败", rule.getId(), e);
            }
        }
    }
}
