package com.ruoyi.data.clean.task;

import com.ruoyi.common.threadlocal.TenantContext;
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

        // 管理员视角：查所有租户任务
        List<CleanRulePo> rules = cleanRuleMapper.selectScheduledRules(null);

        for (CleanRulePo rule : rules) {

            try {
                // 设置当前租户上下文
                TenantContext.set(rule.getTenantId());

                CronExpression cron = CronExpression.parse(rule.getCronExpression());

                LocalDateTime now = LocalDateTime.now();

                LocalDateTime last = cron.next(now.minusMinutes(1));

                if (last != null && last.isBefore(now)) {

                    executeService.executeTask(rule.getId());
                }

            } finally {
                // 清理（防线程污染）
                TenantContext.clear();
            }
        }
    }
}