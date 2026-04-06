package com.ruoyi.data.clean.service.impl;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.data.clean.domain.RuleRegistry;
import com.ruoyi.data.clean.domain.bo.CleanRule;
import com.ruoyi.data.clean.domain.context.DataContext;
import com.ruoyi.data.clean.domain.enums.ExecuteMode;
import com.ruoyi.data.clean.mapper.CleanRuleMapper;
import com.ruoyi.data.clean.mapper.po.CleanResultPo;
import com.ruoyi.data.clean.mapper.po.CleanRulePo;
import com.ruoyi.data.clean.service.ICleanRuleExecuteService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.ruoyi.common.utils.SecurityUtils.getUserId;
import static com.ruoyi.common.utils.SecurityUtils.isAdmin;

@Service
public class CleanRuleExecuteServiceImpl implements ICleanRuleExecuteService {

    @Autowired
    private CleanRuleMapper cleanRuleMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeTask(Long id) {

        System.out.println("===== executeTask 开始，id: " + id + " =====");

        // ================================
        // 0️⃣ 获取 tenantId + 权限控制
        // ================================
        Long tenantId = TenantContext.get();

        if (tenantId == null) {
            throw new RuntimeException("tenantId不能为空");
        }

        // 是否管理员
        boolean isAdmin = isAdmin(getUserId());

        // 👉 查询用的 tenantId（核心）
        Long queryTenantId = isAdmin ? null : tenantId;

        // ================================
        // 1️⃣ 查询任务
        // ================================
        CleanRulePo po = cleanRuleMapper.selectById(id, queryTenantId);

        if (po == null) {
            throw new RuntimeException("清洗任务不存在");
        }

        // ================================
        // 2️⃣ 构建领域对象
        // ================================
        CleanRule rule = RuleRegistry.build(po);
        System.out.println("数据源数量: " + rule.getDataSources().size());

        // ================================
        // 3️⃣ 执行模式校验
        // ================================
        ExecuteMode mode = rule.getExecuteMode();

        if (mode == ExecuteMode.SCHEDULED) {

            if (rule.getCronExpression() == null) {
                throw new RuntimeException("定时任务未配置cron");
            }

            CronExpression cron = CronExpression.parse(rule.getCronExpression());

            LocalDateTime lastRun = rule.getRunTime() == null
                    ? null
                    : LocalDateTime.from(rule.getRunTime());

            LocalDateTime base = lastRun == null
                    ? LocalDateTime.now().minusMinutes(1)
                    : lastRun;

            LocalDateTime next = cron.next(base);

            if (next == null || next.isAfter(LocalDateTime.now())) {
                System.out.println("未到执行时间");
                return;
            }
        }

        // ================================
        // 4️⃣ 构建上下文
        // ================================
        DataContext context = new DataContext(rule.getDataSources());

        // ================================
        // 5️⃣ 查询原始数据
        // ================================
        List<Map<String, Object>> rawData =
                cleanRuleMapper.selectRawInteractionData(queryTenantId);

        context.setRawData(rawData);
        context.setTaskId(id);
        context.setConfigId(rule.getId());

        // ================================
        // 6️⃣ 执行清洗规则
        // ================================
        rule.execute(context);

        // ================================
        // 7️⃣ 转换结果
        // ================================
        List<CleanResultPo> poList = context.getResultList().stream()
                .map(r -> {
                    CleanResultPo resultPo = new CleanResultPo();
                    BeanUtils.copyProperties(r, resultPo);
                    return resultPo;
                })
                .toList();

        // ================================
        // 8️⃣ 批量入库（必须带 tenantId）
        // ================================
        if (poList != null && !poList.isEmpty()) {

            // ❗写入必须属于当前租户（即使是管理员）
            poList.forEach(item -> item.setTenantId(tenantId));

            cleanRuleMapper.batchInsertResults(poList);
        }

        // ================================
        // 9️⃣ 更新运行时间（建议限制租户）
        // ================================
        cleanRuleMapper.updateRuntime(id, LocalDateTime.now(), tenantId);

        System.out.println("===== executeTask 结束 =====");
    }
}