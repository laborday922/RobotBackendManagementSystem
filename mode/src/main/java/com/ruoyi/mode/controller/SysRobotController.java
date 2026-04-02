package com.ruoyi.mode.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mode.domain.SysRobot;
import com.ruoyi.mode.mapper.SysRobotMapper;
import com.ruoyi.mode.service.ISysRobotService;
import com.ruoyi.common.core.page.TableDataInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 机器人Controller
 *
 * @author ruoyi
 */
@Api(value = "机器人管理", tags = {"机器人管理接口"})
@RestController
@RequestMapping("/system/robot")
public class SysRobotController extends BaseController
{
    @Autowired
    private ISysRobotService sysRobotService;

    @Autowired
    private SysRobotMapper sysRobotMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询机器人列表
     */
    @PreAuthorize("@ss.hasPermi('system:robot:list')")
    @ApiOperation(value = "查询机器人列表", notes = "分页查询机器人列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页码", dataType = "int", paramType = "query", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", dataType = "int", paramType = "query", defaultValue = "10")
    })
    @GetMapping("/list")
    public TableDataInfo list(@ApiParam(value = "机器人查询条件") SysRobot sysRobot)
    {
        startPage();
        List<SysRobot> list = sysRobotService.selectSysRobotList(sysRobot);
        return getDataTable(list);
    }

    /**
     * 获取机器人详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:robot:query')")
    @ApiOperation(value = "获取机器人详细信息", notes = "根据机器人ID获取详细信息")
    @ApiImplicitParam(name = "robotId", value = "机器人ID", required = true, dataType = "Long", paramType = "path")
    @GetMapping(value = "/{robotId}")
    public AjaxResult getInfo(@PathVariable("robotId") Long robotId)
    {
        return success(sysRobotService.selectSysRobotById(robotId));
    }

    /**
     * 新增机器人
     */
    @PreAuthorize("@ss.hasPermi('system:robot:add')")
    @Log(title = "机器人", businessType = BusinessType.INSERT)
    @ApiOperation(value = "新增机器人", notes = "创建新的机器人")
    @PostMapping
    public AjaxResult add(@ApiParam(value = "机器人信息", required = true) @RequestBody SysRobot sysRobot)
    {
        return toAjax(sysRobotService.insertSysRobot(sysRobot));
    }

    /**
     * 修改机器人
     */
    @PreAuthorize("@ss.hasPermi('system:robot:edit')")
    @Log(title = "机器人", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "修改机器人", notes = "修改现有的机器人信息")
    @PutMapping
    public AjaxResult edit(@ApiParam(value = "机器人信息", required = true) @RequestBody SysRobot sysRobot)
    {
        return toAjax(sysRobotService.updateSysRobot(sysRobot));
    }

    /**
     * 删除机器人
     */
    @PreAuthorize("@ss.hasPermi('system:robot:remove')")
    @Log(title = "机器人", businessType = BusinessType.DELETE)
    @ApiOperation(value = "删除机器人", notes = "根据机器人ID数组批量删除机器人")
    @ApiImplicitParam(name = "robotIds", value = "机器人ID数组", required = true, dataType = "Long[]", paramType = "path")
    @DeleteMapping("/{robotIds}")
    public AjaxResult remove(@PathVariable Long[] robotIds)
    {
        return toAjax(sysRobotService.deleteSysRobotByIds(robotIds));
    }

    /**
     * 更新机器人当前模式
     */
    @PreAuthorize("@ss.hasPermi('system:robot:edit')")
    @Log(title = "机器人", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "更新机器人当前模式", notes = "更新单个机器人的当前工作模式")
    @PutMapping("/updateMode")
    public AjaxResult updateMode(@ApiParam(value = "机器人信息（包含robotId和currentMode）", required = true)
                                 @RequestBody SysRobot sysRobot)
    {
        return toAjax(sysRobotService.updateRobotMode(sysRobot.getRobotId(), sysRobot.getCurrentMode()));
    }

    /**
     * 批量更新机器人模式
     */
    @PreAuthorize("@ss.hasPermi('system:robot:edit')")
    @Log(title = "机器人", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "批量更新机器人模式", notes = "批量更新多个机器人的工作模式")
    @PutMapping("/batchUpdateMode")
    public AjaxResult batchUpdateMode(@ApiParam(value = "批量更新请求参数", required = true)
                                      @RequestBody BatchModeUpdateRequest request)
    {
        int success = 0;
        for (Long robotId : request.getRobotIds()) {
            success += sysRobotService.updateRobotMode(robotId, request.getModeId());
        }
        return success > 0 ? success() : error();
    }

    /**
     * 批量重启机器人 - 异步执行，立即返回
     */
    @PreAuthorize("@ss.hasPermi('system:robot:edit')")
    @Log(title = "机器人", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "批量重启机器人", notes = "批量重启指定的机器人（异步执行）")
    @PutMapping("/batchRestart")
    public AjaxResult batchRestart(@ApiParam(value = "机器人ID数组", required = true) @RequestBody Long[] robotIds)
    {
        logger.info("收到批量重启请求: robotIds={}", Arrays.toString(robotIds));

        int submittedCount = sysRobotService.batchRestartAsync(robotIds);

        Map<String, Object> result = new HashMap<>();
        result.put("submitted", submittedCount);
        result.put("total", robotIds.length);
        result.put("message", "已提交 " + submittedCount + " 个机器人的重启任务，请稍后查看状态");

        if (submittedCount == 0) {
            return error("没有成功提交任何重启任务，请检查机器人状态");
        }

        return success(result);
    }

    /**
     * 紧急停止机器人
     */
    @PreAuthorize("@ss.hasPermi('system:robot:edit')")
    @Log(title = "机器人", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "紧急停止机器人", notes = "紧急停止指定的机器人")
    @PutMapping("/emergencyStop")
    public AjaxResult emergencyStop(@ApiParam(value = "机器人ID数组", required = true) @RequestBody Long[] robotIds)
    {
        return success(sysRobotService.emergencyStop(robotIds));
    }

    /**
     * 刷新机器人状态
     */
    @PreAuthorize("@ss.hasPermi('system:robot:edit')")
    @Log(title = "机器人", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "刷新机器人状态", notes = "刷新指定机器人的实时状态")
    @PutMapping("/refreshStatus")
    public AjaxResult refreshStatus(@ApiParam(value = "机器人ID数组", required = true) @RequestBody Long[] robotIds)
    {
        return success(sysRobotService.refreshStatus(robotIds));
    }

    /**
     * 测试告警
     */
    @PreAuthorize("@ss.hasPermi('system:robot:edit')")
    @Log(title = "机器人", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "测试告警", notes = "测试指定机器人的告警功能")
    @PutMapping("/testAlert")
    public AjaxResult testAlert(@ApiParam(value = "机器人ID数组", required = true) @RequestBody Long[] robotIds)
    {
        return success(sysRobotService.testAlert(robotIds));
    }

    /**
     * 清除告警
     */
    @PreAuthorize("@ss.hasPermi('system:robot:edit')")
    @Log(title = "机器人", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "清除告警", notes = "清除指定机器人的告警状态")
    @PutMapping("/clearAlerts")
    public AjaxResult clearAlerts(@ApiParam(value = "机器人ID数组", required = true) @RequestBody Long[] robotIds)
    {
        return success(sysRobotService.clearAlerts(robotIds));
    }

    // ==================== 模式切换操作接口 ====================

    /**
     * 切换待机模式
     */
    @PreAuthorize("@ss.hasPermi('system:robot:edit')")
    @Log(title = "机器人", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "切换待机模式", notes = "将机器人切换为待机模式")
    @PutMapping("/standbyMode")
    public AjaxResult standbyMode(@ApiParam(value = "机器人ID数组", required = true) @RequestBody Long[] robotIds)
    {
        int result = sysRobotService.standbyMode(robotIds);
        return result > 0 ? success(result) : error("切换待机模式失败");
    }

    /**
     * 切换维护模式
     */
    @PreAuthorize("@ss.hasPermi('system:robot:edit')")
    @Log(title = "机器人", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "切换维护模式", notes = "将机器人切换为维护模式")
    @PutMapping("/maintenanceMode")
    public AjaxResult maintenanceMode(@ApiParam(value = "机器人ID数组", required = true) @RequestBody Long[] robotIds)
    {
        int result = sysRobotService.maintenanceMode(robotIds);
        return result > 0 ? success(result) : error("切换维护模式失败");
    }

    /**
     * 切换充电模式
     */
    @PreAuthorize("@ss.hasPermi('system:robot:edit')")
    @Log(title = "机器人", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "切换充电模式", notes = "将机器人切换为充电模式")
    @PutMapping("/chargeMode")
    public AjaxResult chargeMode(@ApiParam(value = "机器人ID数组", required = true) @RequestBody Long[] robotIds)
    {
        int result = sysRobotService.chargeMode(robotIds);
        return result > 0 ? success(result) : error("切换充电模式失败");
    }

    /**
     * 返回充电
     */
    @PreAuthorize("@ss.hasPermi('system:robot:edit')")
    @Log(title = "机器人", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "返回充电", notes = "让机器人返回充电站充电")
    @PutMapping("/returnCharge")
    public AjaxResult returnCharge(@ApiParam(value = "机器人ID数组", required = true) @RequestBody Long[] robotIds)
    {
        int result = sysRobotService.returnCharge(robotIds);
        return result > 0 ? success(result) : error("返回充电操作失败");
    }

    // ==================== 机器人模式配置相关接口 ====================

    /**
     * 保存机器人模式配置
     */
    @PreAuthorize("@ss.hasPermi('system:robot:edit')")
    @Log(title = "机器人", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "保存机器人模式配置", notes = "保存机器人在特定模式下的配置参数")
    @PostMapping("/saveModeConfig")
    public AjaxResult saveModeConfig(@RequestBody SaveModeConfigRequest request)
    {
        logger.info("保存机器人模式配置: robotId={}, modeId={}, config={}",
                request.getRobotId(), request.getModeId(), request.getConfig());

        int result = sysRobotService.saveRobotModeConfig(request.getRobotId(), request.getModeId(), request.getConfig());
        return toAjax(result);
    }

    /**
     * 批量保存机器人模式配置
     */
    @PreAuthorize("@ss.hasPermi('system:robot:edit')")
    @Log(title = "机器人", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "批量保存机器人模式配置", notes = "批量保存多个机器人在特定模式下的配置参数")
    @PostMapping("/batchSaveModeConfig")
    public AjaxResult batchSaveModeConfig(@RequestBody BatchSaveModeConfigRequest request)
    {
        int successCount = 0;
        for (Long robotId : request.getRobotIds()) {
            int result = sysRobotService.saveRobotModeConfig(robotId, request.getModeId(), request.getConfig());
            if (result > 0) {
                successCount++;
            }
        }
        return success(successCount);
    }

    /**
     * 获取机器人模式配置
     */
    @PreAuthorize("@ss.hasPermi('system:robot:query')")
    @ApiOperation(value = "获取机器人模式配置", notes = "获取机器人在特定模式下的配置参数")
    @GetMapping("/modeConfig")
    public AjaxResult getModeConfig(@RequestParam Long robotId, @RequestParam Long modeId)
    {
        Map<String, Object> config = sysRobotService.getRobotModeConfig(robotId, modeId);
        return success(config);
    }

    /**
     * 删除机器人模式配置
     */
    @PreAuthorize("@ss.hasPermi('system:robot:edit')")
    @Log(title = "机器人", businessType = BusinessType.DELETE)
    @ApiOperation(value = "删除机器人模式配置", notes = "删除机器人在特定模式下的配置参数")
    @DeleteMapping("/deleteModeConfig")
    public AjaxResult deleteModeConfig(@RequestParam Long robotId, @RequestParam Long modeId)
    {
        int result = sysRobotService.deleteRobotModeConfig(robotId, modeId);
        return toAjax(result);
    }

    /**
     * 复制机器人模式配置
     */
    @PreAuthorize("@ss.hasPermi('system:robot:edit')")
    @Log(title = "机器人", businessType = BusinessType.INSERT)
    @ApiOperation(value = "复制机器人模式配置", notes = "将源机器人的模式配置复制到目标机器人")
    @PostMapping("/copyModeConfig")
    public AjaxResult copyModeConfig(@RequestBody CopyModeConfigRequest request)
    {
        int result = sysRobotService.copyRobotModeConfig(
                request.getSourceRobotId(),
                request.getTargetRobotId(),
                request.getModeId()
        );
        return toAjax(result);
    }

    // ==================== 调试接口 ====================

    /**
     * 【调试接口】获取机器人详细状态
     */
    @GetMapping("/debug/{robotId}")
    public AjaxResult debugRobot(@PathVariable Long robotId)
    {
        try {
            SysRobot robot = sysRobotMapper.selectSysRobotById(robotId);
            if (robot == null) {
                return error("机器人不存在: " + robotId);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("robotId", robot.getRobotId());
            result.put("robotName", robot.getRobotName());
            result.put("robotCode", robot.getRobotCode());
            result.put("status", robot.getStatus());
            result.put("taskStatus", robot.getTaskStatus());
            result.put("battery", robot.getBattery());
            result.put("currentMode", robot.getCurrentMode());
            result.put("area", robot.getArea());
            result.put("createTime", robot.getCreateTime());
            result.put("updateTime", robot.getUpdateTime());

            return success(result);
        } catch (Exception e) {
            logger.error("调试接口异常", e);
            return error("调试失败: " + e.getMessage());
        }
    }

    /**
     * 【调试接口】获取所有机器人状态
     */
    @PreAuthorize("@ss.hasPermi('system:robot:list')")
    @GetMapping("/debug/all")
    public AjaxResult debugAllRobots()
    {
        try {
            List<SysRobot> robots = sysRobotMapper.selectSysRobotList(new SysRobot());
            List<Map<String, Object>> result = new ArrayList<>();

            for (SysRobot robot : robots) {
                Map<String, Object> item = new HashMap<>();
                item.put("robotId", robot.getRobotId());
                item.put("robotName", robot.getRobotName());
                item.put("robotCode", robot.getRobotCode());
                item.put("status", robot.getStatus());
                item.put("taskStatus", robot.getTaskStatus());
                item.put("battery", robot.getBattery());
                item.put("currentMode", robot.getCurrentMode());
                item.put("area", robot.getArea());
                result.add(item);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("count", result.size());
            response.put("robots", result);

            return success(response);
        } catch (Exception e) {
            logger.error("调试接口异常", e);
            return error("调试失败: " + e.getMessage());
        }
    }

    /**
     * 【直接SQL查询】获取所有机器人 - 绕过所有过滤
     */
    @GetMapping("/debug/simple")
    public AjaxResult debugSimple() {
        try {
            List<SysRobot> robots = sysRobotMapper.selectSysRobotList(null);

            List<Map<String, Object>> result = new ArrayList<>();
            for (SysRobot robot : robots) {
                Map<String, Object> item = new HashMap<>();
                item.put("robotId", robot.getRobotId());
                item.put("robotName", robot.getRobotName());
                item.put("robotCode", robot.getRobotCode());
                item.put("status", robot.getStatus());
                item.put("taskStatus", robot.getTaskStatus());
                item.put("battery", robot.getBattery());
                item.put("area", robot.getArea());
                result.add(item);
            }

            return success(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 【数据库信息】获取数据库连接信息
     */
    @GetMapping("/debug/dbinfo")
    public AjaxResult dbInfo() {
        Map<String, Object> result = new HashMap<>();
        try {
            String db = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
            String user = jdbcTemplate.queryForObject("SELECT USER()", String.class);
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM robots WHERE del_flag = '0'", Integer.class);
            result.put("database", db);
            result.put("user", user);
            result.put("rowCount", count);
            result.put("tableName", "robots");
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return success(result);
    }

    /**
     * 【直接查询】查询机器人表
     */
    @GetMapping("/debug/direct")
    public AjaxResult directQuery() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM robots WHERE del_flag = '0'", Integer.class);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id, code, name, area, status, task_status, battery, current_mode FROM robots WHERE del_flag = '0' ORDER BY id"
            );
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            result.put("rows", rows);
            return success(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ==================== 内部请求类 ====================

    @ApiModel(value = "BatchModeUpdateRequest", description = "批量更新机器人模式请求参数")
    static class BatchModeUpdateRequest {
        @ApiModelProperty(value = "机器人ID列表", required = true)
        private Long[] robotIds;
        @ApiModelProperty(value = "模式ID", required = true)
        private Long modeId;
        public Long[] getRobotIds() { return robotIds; }
        public void setRobotIds(Long[] robotIds) { this.robotIds = robotIds; }
        public Long getModeId() { return modeId; }
        public void setModeId(Long modeId) { this.modeId = modeId; }
    }

    @ApiModel(value = "SaveModeConfigRequest", description = "保存机器人模式配置请求参数")
    static class SaveModeConfigRequest {
        private Long robotId;
        private Long modeId;
        private Map<String, Object> config;
        public Long getRobotId() { return robotId; }
        public void setRobotId(Long robotId) { this.robotId = robotId; }
        public Long getModeId() { return modeId; }
        public void setModeId(Long modeId) { this.modeId = modeId; }
        public Map<String, Object> getConfig() { return config; }
        public void setConfig(Map<String, Object> config) { this.config = config; }
    }

    @ApiModel(value = "BatchSaveModeConfigRequest", description = "批量保存机器人模式配置请求参数")
    static class BatchSaveModeConfigRequest {
        private Long[] robotIds;
        private Long modeId;
        private Map<String, Object> config;
        public Long[] getRobotIds() { return robotIds; }
        public void setRobotIds(Long[] robotIds) { this.robotIds = robotIds; }
        public Long getModeId() { return modeId; }
        public void setModeId(Long modeId) { this.modeId = modeId; }
        public Map<String, Object> getConfig() { return config; }
        public void setConfig(Map<String, Object> config) { this.config = config; }
    }

    @ApiModel(value = "CopyModeConfigRequest", description = "复制机器人模式配置请求参数")
    static class CopyModeConfigRequest {
        private Long sourceRobotId;
        private Long targetRobotId;
        private Long modeId;
        public Long getSourceRobotId() { return sourceRobotId; }
        public void setSourceRobotId(Long sourceRobotId) { this.sourceRobotId = sourceRobotId; }
        public Long getTargetRobotId() { return targetRobotId; }
        public void setTargetRobotId(Long targetRobotId) { this.targetRobotId = targetRobotId; }
        public Long getModeId() { return modeId; }
        public void setModeId(Long modeId) { this.modeId = modeId; }
    }
}