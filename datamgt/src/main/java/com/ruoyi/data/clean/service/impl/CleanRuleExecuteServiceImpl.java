package com.ruoyi.data.clean.service.impl;

import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.data.clean.domain.CleanExecuteRecord;
import com.ruoyi.data.clean.domain.RuleRegistry;
import com.ruoyi.data.clean.domain.bo.CleanRule;
import com.ruoyi.data.clean.domain.context.DataContext;
import com.ruoyi.data.clean.mapper.CleanExecuteRecordMapper;
import com.ruoyi.data.clean.mapper.CleanRuleMapper;
import com.ruoyi.data.clean.mapper.po.CleanResultPo;
import com.ruoyi.data.clean.mapper.po.CleanRulePo;
import com.ruoyi.data.clean.service.ICleanRuleExecuteService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private CleanExecuteRecordMapper executeRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeScheduled(Long taskId) {

        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new RuntimeException("tenantId不能为空");
        }

        CleanRulePo po = cleanRuleMapper.selectById(taskId, tenantId);
        if (po == null) {
            throw new RuntimeException("清洗任务不存在");
        }

        CleanRule rule = RuleRegistry.build(po);

        LocalDateTime runTime = LocalDateTime.now();
        CleanExecuteRecord record = new CleanExecuteRecord();
        record.setTaskId(taskId);
        record.setExecuteMode("SCHEDULED");
        record.setRunTime(runTime);
        record.setCreateTime(runTime);
        record.setTenantId(tenantId);

        try {
            doExecute(rule, taskId, tenantId);
            record.setSuccess(1);
        } catch (Exception e) {
            record.setSuccess(0);
            record.setMessage(e.getMessage());
        }

        executeRecordMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeManual(String configJson, String applyDataSource) {

        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new RuntimeException("tenantId不能为空");
        }

        CleanRulePo po = new CleanRulePo();
        po.setExecuteMode("IMMEDIATE");
        po.setApplyDataSource(applyDataSource);
        po.setConfigJson(configJson);

        CleanRule rule = RuleRegistry.build(po);

        LocalDateTime runTime = LocalDateTime.now();
        CleanExecuteRecord record = new CleanExecuteRecord();
        record.setTaskId(null);
        record.setExecuteMode("IMMEDIATE");
        record.setRunTime(runTime);
        record.setCreateTime(runTime);
        record.setTenantId(tenantId);

        try {
            doExecute(rule, null, tenantId);
            record.setSuccess(1);
        } catch (Exception e) {
            record.setSuccess(0);
            record.setMessage(e.getMessage());
        }

        executeRecordMapper.insert(record);
    }

    /**
     * 执行清洗核心流程（查原始数据 -> 执行规则 -> 批量入库）
     */
    private void doExecute(CleanRule rule, Long taskId, Long tenantId) {

        DataContext context = new DataContext(rule.getDataSources());

        List<Map<String, Object>> rawData = cleanRuleMapper.selectRawInteractionData();
        context.setRawData(rawData);
        context.setTaskId(taskId);
        context.setConfigId(rule.getId());

        rule.execute(context);

        List<CleanResultPo> poList = context.getResultList().stream()
                .map(r -> {
                    CleanResultPo resultPo = new CleanResultPo();
                    BeanUtils.copyProperties(r, resultPo);
                    return resultPo;
                })
                .toList();

        if (poList != null && !poList.isEmpty()) {
            poList.forEach(item -> item.setTenantId(tenantId));
            cleanRuleMapper.batchInsertResults(poList);
        }
    }
}
