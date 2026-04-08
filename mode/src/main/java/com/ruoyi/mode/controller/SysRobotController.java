package com.ruoyi.mode.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mode.domain.SysRobot;
import com.ruoyi.mode.service.ISysRobotService;
import com.ruoyi.robots.service.IRobotsService;
import com.ruoyi.robots.domain.Robot;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 机器人模式管理Controller
 *
 * @author xiaocai
 * @date 2026-03-07
 */
@Api(value = "机器人模式管理", tags = {"机器人模式管理接口"})
@RestController
@RequestMapping("/mode/robots")
public class SysRobotController extends BaseController
{
    @Autowired
    private ISysRobotService sysRobotService;

    @Autowired
    private IRobotsService robotsService;

    /**
     * 查询机器人基础信息列表（复用robot模块）
     */
    @PreAuthorize("@ss.hasPermi('robots:robots:list')")
    @GetMapping("/list")
    @ApiOperation("查询机器人列表")
    public TableDataInfo list(Robot robot)
    {
        startPage();
        List<Robot> list = robotsService.selectRobotsList(robot);
        return getDataTable(list);
    }

    /**
     * 获取机器人基础信息详细信息（复用robot模块）
     */
    @ApiOperation("获取机器人基础信息")
    @PreAuthorize("@ss.hasPermi('robots:robots:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(robotsService.selectRobotsById(id));
    }

    /**
     * 更新机器人当前模式
     */
    @PreAuthorize("@ss.hasPermi('mode:robots:edit')")
    @Log(title = "机器人模式", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "更新机器人当前模式", notes = "更新单个机器人的当前工作模式")
    @PutMapping("/updateMode")
    public AjaxResult updateMode(@Validated @RequestBody ModeUpdateDto modeUpdateDto)
    {
        return toAjax(sysRobotService.updateRobotMode(modeUpdateDto.getRobotId(), modeUpdateDto.getModeId()));
    }

    /**
     * 批量更新机器人模式
     */
    @PreAuthorize("@ss.hasPermi('mode:robots:edit')")
    @Log(title = "机器人模式", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "批量更新机器人模式", notes = "批量更新多个机器人的工作模式")
    @PutMapping("/batchUpdateMode")
    public AjaxResult batchUpdateMode(@Validated @RequestBody BatchModeUpdateDto request)
    {
        int successCount = 0;
        for (Long robotId : request.getRobotIds()) {
            successCount += sysRobotService.updateRobotMode(robotId, request.getModeId());
        }
        return successCount > 0 ? success(successCount) : error("批量更新模式失败");
    }

    /**
     * 批量重启机器人 - 异步执行
     */
    @PreAuthorize("@ss.hasPermi('mode:robots:edit')")
    @Log(title = "机器人模式", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "批量重启机器人", notes = "批量重启指定的机器人（异步执行）")
    @PutMapping("/batchRestart")
    public AjaxResult batchRestart(@RequestBody Long[] robotIds)
    {
        logger.info("收到批量重启请求: robotIds={}", Arrays.toString(robotIds));

        int submittedCount = sysRobotService.batchRestartAsync(robotIds);

        Map<String, Object> result = new HashMap<>();
        result.put("submitted", submittedCount);
        result.put("total", robotIds.length);
        result.put("message", "已提交 " + submittedCount + " 个机器人的重启任务");

        return submittedCount > 0 ? success(result) : error("没有成功提交任何重启任务");
    }

    /**
     * 紧急停止机器人
     */
    @PreAuthorize("@ss.hasPermi('mode:robots:edit')")
    @Log(title = "机器人模式", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "紧急停止机器人", notes = "紧急停止指定的机器人")
    @PutMapping("/emergencyStop")
    public AjaxResult emergencyStop(@RequestBody Long[] robotIds)
    {
        return success(sysRobotService.emergencyStop(robotIds));
    }

    /**
     * 切换待机模式
     */
    @PreAuthorize("@ss.hasPermi('mode:robots:edit')")
    @Log(title = "机器人模式", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "切换待机模式", notes = "将机器人切换为待机模式")
    @PutMapping("/standbyMode")
    public AjaxResult standbyMode(@RequestBody Long[] robotIds)
    {
        int result = sysRobotService.standbyMode(robotIds);
        return result > 0 ? success(result) : error("切换待机模式失败");
    }

    /**
     * 切换维护模式
     */
    @PreAuthorize("@ss.hasPermi('mode:robots:edit')")
    @Log(title = "机器人模式", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "切换维护模式", notes = "将机器人切换为维护模式")
    @PutMapping("/maintenanceMode")
    public AjaxResult maintenanceMode(@RequestBody Long[] robotIds)
    {
        int result = sysRobotService.maintenanceMode(robotIds);
        return result > 0 ? success(result) : error("切换维护模式失败");
    }

    /**
     * 切换充电模式
     */
    @PreAuthorize("@ss.hasPermi('mode:robots:edit')")
    @Log(title = "机器人模式", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "切换充电模式", notes = "将机器人切换为充电模式")
    @PutMapping("/chargeMode")
    public AjaxResult chargeMode(@RequestBody Long[] robotIds)
    {
        int result = sysRobotService.chargeMode(robotIds);
        return result > 0 ? success(result) : error("切换充电模式失败");
    }

    /**
     * 返回充电
     */
    @PreAuthorize("@ss.hasPermi('mode:robots:edit')")
    @Log(title = "机器人模式", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "返回充电", notes = "让机器人返回充电站充电")
    @PutMapping("/returnCharge")
    public AjaxResult returnCharge(@RequestBody Long[] robotIds)
    {
        int result = sysRobotService.returnCharge(robotIds);
        return result > 0 ? success(result) : error("返回充电操作失败");
    }

    /**
     * 保存机器人模式配置
     */
    @PreAuthorize("@ss.hasPermi('mode:robots:edit')")
    @Log(title = "机器人模式", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "保存机器人模式配置", notes = "保存机器人在特定模式下的配置参数")
    @PostMapping("/saveModeConfig")
    public AjaxResult saveModeConfig(@Validated @RequestBody SaveModeConfigDto request)
    {
        int result = sysRobotService.saveRobotModeConfig(
                request.getRobotId(),
                request.getModeId(),
                request.getConfig()
        );
        return toAjax(result);
    }

    /**
     * 获取机器人模式配置
     */
    @PreAuthorize("@ss.hasPermi('mode:robots:query')")
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
    @PreAuthorize("@ss.hasPermi('mode:robots:edit')")
    @Log(title = "机器人模式", businessType = BusinessType.DELETE)
    @ApiOperation(value = "删除机器人模式配置", notes = "删除机器人在特定模式下的配置参数")
    @DeleteMapping("/deleteModeConfig")
    public AjaxResult deleteModeConfig(@RequestParam Long robotId, @RequestParam Long modeId)
    {
        int result = sysRobotService.deleteRobotModeConfig(robotId, modeId);
        return toAjax(result);
    }

    /**
     * 刷新机器人状态
     */
    @PreAuthorize("@ss.hasPermi('mode:robots:edit')")
    @Log(title = "机器人模式", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "刷新机器人状态", notes = "刷新指定机器人的状态信息")
    @PutMapping("/refreshStatus")
    public AjaxResult refreshStatus(@RequestBody Long[] robotIds)
    {
        int result = sysRobotService.refreshStatus(robotIds);
        return success(result);
    }

    /**
     * 测试告警
     */
    @PreAuthorize("@ss.hasPermi('mode:robots:edit')")
    @Log(title = "机器人模式", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "测试告警", notes = "触发机器人测试告警")
    @PutMapping("/testAlert")
    public AjaxResult testAlert(@RequestBody Long[] robotIds)
    {
        int result = sysRobotService.testAlert(robotIds);
        return success(result);
    }

    /**
     * 清除告警
     */
    @PreAuthorize("@ss.hasPermi('mode:robots:edit')")
    @Log(title = "机器人模式", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "清除告警", notes = "清除机器人的告警状态")
    @PutMapping("/clearAlerts")
    public AjaxResult clearAlerts(@RequestBody Long[] robotIds)
    {
        int result = sysRobotService.clearAlerts(robotIds);
        return success(result);
    }

    /**
     * 获取在线机器人列表
     */
    @PreAuthorize("@ss.hasPermi('mode:robots:list')")
    @ApiOperation(value = "获取在线机器人列表", notes = "获取当前在线的机器人列表")
    @GetMapping("/online")
    public AjaxResult getOnlineRobots()
    {
        List<SysRobot> robots = sysRobotService.selectOnlineRobots();
        return success(robots);
    }

    /**
     * 获取低电量机器人列表
     */
    @PreAuthorize("@ss.hasPermi('mode:robots:list')")
    @ApiOperation(value = "获取低电量机器人列表", notes = "获取电量低于阈值的机器人列表")
    @GetMapping("/lowBattery")
    public AjaxResult getLowBatteryRobots(@RequestParam(defaultValue = "20") Integer threshold)
    {
        List<SysRobot> robots = sysRobotService.selectLowBatteryRobots(threshold);
        return success(robots);
    }

    // ==================== 内部DTO类 ====================

    /**
     * 模式更新DTO
     */
    @ApiModel(value = "模式更新请求参数")
    static class ModeUpdateDto {
        @ApiModelProperty(value = "机器人ID", required = true)
        private Long robotId;

        @ApiModelProperty(value = "模式ID", required = true)
        private Long modeId;

        public Long getRobotId() { return robotId; }
        public void setRobotId(Long robotId) { this.robotId = robotId; }
        public Long getModeId() { return modeId; }
        public void setModeId(Long modeId) { this.modeId = modeId; }
    }

    /**
     * 批量模式更新DTO
     */
    @ApiModel(value = "批量模式更新请求参数")
    static class BatchModeUpdateDto {
        @ApiModelProperty(value = "机器人ID列表", required = true)
        private Long[] robotIds;

        @ApiModelProperty(value = "模式ID", required = true)
        private Long modeId;

        public Long[] getRobotIds() { return robotIds; }
        public void setRobotIds(Long[] robotIds) { this.robotIds = robotIds; }
        public Long getModeId() { return modeId; }
        public void setModeId(Long modeId) { this.modeId = modeId; }
    }

    /**
     * 保存模式配置DTO
     */
    @ApiModel(value = "保存模式配置请求参数")
    static class SaveModeConfigDto {
        @ApiModelProperty(value = "机器人ID", required = true)
        private Long robotId;

        @ApiModelProperty(value = "模式ID", required = true)
        private Long modeId;

        @ApiModelProperty(value = "配置参数", required = true)
        private Map<String, Object> config;

        public Long getRobotId() { return robotId; }
        public void setRobotId(Long robotId) { this.robotId = robotId; }
        public Long getModeId() { return modeId; }
        public void setModeId(Long modeId) { this.modeId = modeId; }
        public Map<String, Object> getConfig() { return config; }
        public void setConfig(Map<String, Object> config) { this.config = config; }
    }
}