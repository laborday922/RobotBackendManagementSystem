package com.ruoyi.data.clean.service.impl;

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

@Service
public class CleanRuleExecuteServiceImpl implements ICleanRuleExecuteService {

    @Autowired
    private CleanRuleMapper cleanRuleMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeTask(Long id) {
        //测试
        System.out.println("===== executeTask 开始，id: " + id + " =====");

        // 1️⃣ 查询任务
        CleanRulePo po = cleanRuleMapper.selectById(id);

        if (po == null) {
            throw new RuntimeException("清洗任务不存在");
        }

        // 2️⃣ 构建领域对象
        CleanRule rule = RuleRegistry.build(po);
        System.out.println("数据源数量: " + rule.getDataSources().size());

        // 3️⃣ 校验执行模式

//        if (!ExecuteMode.IMMEDIATE.equals(rule.getExecuteMode())) {
//            throw new RuntimeException("当前版本仅支持立即执行模式");
//        }
        ExecuteMode mode = rule.getExecuteMode();

        if (mode == ExecuteMode.SCHEDULED) {

            if (rule.getCronExpression() == null) {
                throw new RuntimeException("定时任务未配置cron");
            }

            CronExpression cron = CronExpression.parse(rule.getCronExpression());

            LocalDateTime lastRun = LocalDateTime.from(rule.getRunTime());

            LocalDateTime base = lastRun == null
                    ? LocalDateTime.now().minusMinutes(1)
                    : lastRun;

            LocalDateTime next = cron.next(base);

            if (next == null || next.isAfter(LocalDateTime.now())) {
                System.out.println("未到执行时间");
                return;
            }

            if (next == null || next.isAfter(LocalDateTime.now())) {
                System.out.println("未到执行时间");
                return;
            }
        }

        // 4️⃣ 构建上下文
        DataContext context = new DataContext(rule.getDataSources());

        // 5️⃣ 查原始数据（🔥新增）
        List<Map<String, Object>> rawData = cleanRuleMapper.selectRawInteractionData();

        context.setRawData(rawData);
        context.setTaskId(id);
        context.setConfigId(rule.getId()); // 或你配置ID

        // ⭐⭐⭐ 关键新增逻辑 ⭐⭐⭐
        // 5️⃣ 查询每个表的字段（排除id）
//        rule.getDataSources().forEach(source -> {
//
//            String tableName = source.getTableName();
//            System.out.println("正在处理表: " + tableName);
//
//            List<String> columns = cleanRuleMapper.getTableColumns(tableName);
//            System.out.println("表 " + tableName + " 的字段（原始）: " + columns);
//
//            if (columns == null || columns.isEmpty()) {
//                throw new RuntimeException("表 " + tableName + " 未获取到字段信息");
//            }
//
//            context.setTableColumns(tableName, columns);
//        });

        // 6️⃣ 规则执行
        rule.execute(context);

        List<CleanResultPo> poList = context.getResultList().stream()
                .map(r -> {
                    CleanResultPo ResultPo = new CleanResultPo();
                    BeanUtils.copyProperties(r, ResultPo);
                    return ResultPo;
                })
                .toList();
        // 7️⃣ 批量保存结果
        if (context.getResultList() != null && !context.getResultList().isEmpty()) {
            cleanRuleMapper.batchInsertResults(poList);
        }
        cleanRuleMapper.updateRuntime(id, LocalDateTime.now());
    }

}