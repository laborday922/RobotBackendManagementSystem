package com.ruoyi.taskmgt.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.CloneFactory;
import com.ruoyi.common.validation.NewGroup;
import com.ruoyi.taskmgt.controller.dto.TemplateDto;
import com.ruoyi.taskmgt.domain.bo.Template;
import com.ruoyi.taskmgt.service.ITemplateService;
import com.ruoyi.taskmgt.service.vo.TemplateVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "任务模板管理")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/taskmgt")
public class TemplateController extends BaseController {
    @Autowired
    private final ITemplateService templateService;

    @ApiOperation("获取模板列表")
    @Log(title = "查询模板列表", businessType = BusinessType.OTHER)
    @GetMapping("templates")
    public TableDataInfo retrieveTemplates(
            @RequestParam(required = false) Long appId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) Long robotGroupId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        startPage(pageNum, pageSize);
        List<TemplateVo> list = templateService.retrieveTemplates(appId,name, status, robotGroupId);
        return getDataTable(list);
    }

    @ApiOperation("获取模板详情")
    @Log(title = "获取模板详情", businessType = BusinessType.OTHER)
    @GetMapping("templates/{id}")
    public AjaxResult getTemplate(@PathVariable Long id) {
        TemplateVo vo = templateService.getTemplate(id);
        return success(vo);
    }

    @ApiOperation("创建模板")
    @Log(title = "创建模板", businessType = BusinessType.INSERT)
    @PostMapping("template")
    public AjaxResult createTemplate(@Validated(NewGroup.class) @RequestBody TemplateDto dto) {
        Template template = CloneFactory.copy(new Template(), dto);
        TemplateVo result = templateService.createTemplate(template);
        return success(result);
    }

    @ApiOperation("修改模板")
    @Log(title = "修改模板", businessType = BusinessType.UPDATE)
    @PutMapping("templates/{id}")
    public AjaxResult updateTemplate(@PathVariable Long id, @Validated @RequestBody TemplateDto dto) {
        Template template = CloneFactory.copy(new Template(), dto);
        template.setId(id);
        templateService.updateTemplate(template);
        return success();
    }

    @ApiOperation("删除模板")
    @Log(title = "删除模板", businessType = BusinessType.DELETE)
    @DeleteMapping("templates/{id}")
    public AjaxResult deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return success();
    }

    @ApiOperation("禁用模板")
    @Log(title = "禁用模板", businessType = BusinessType.UPDATE)
    @PutMapping("templates/{id}/ban")
    public AjaxResult banTemplate(@PathVariable Long id) {
        templateService.banTemplate(id);
        return success();
    }

    @ApiOperation("恢复模板")
    @Log(title = "恢复模板", businessType = BusinessType.UPDATE)
    @PutMapping("templates/{id}/resume")
    public AjaxResult resumeTemplate(@PathVariable Long id) {
        templateService.resumeTemplate(id);
        return success();
    }
}