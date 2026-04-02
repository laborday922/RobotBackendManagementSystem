package com.ruoyi.taskmgt.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.taskmgt.service.vo.OperationParam;
import com.ruoyi.taskmgt.service.vo.OperationVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

// Controller 提供操作列表查询
@RestController
@RequestMapping("/taskmgt/operation")
public class OperationController {

    // 模拟数据，后续替换为数据库查询
    @GetMapping("/list")
    public AjaxResult list() {
        List<OperationVo> operations = Arrays.asList(
                OperationVo.builder()
                        .id(1001L)
                        .name("移动到点")
                        .description("控制机器人移动到指定坐标位置")
                        .type("HTTP")
                        .params(Arrays.asList(
                                OperationParam.builder()
                                        .name("x")
                                        .label("X坐标")
                                        .type("field_ref")  // 可从表单字段选择
                                        .required(true)
                                        .description("目标位置X坐标，单位：米")
                                        .build(),
                                OperationParam.builder()
                                        .name("y")
                                        .label("Y坐标")
                                        .type("field_ref")
                                        .required(true)
                                        .description("目标位置Y坐标，单位：米")
                                        .build(),
                                OperationParam.builder()
                                        .name("speed")
                                        .label("移动速度")
                                        .type("number")
                                        .required(false)
                                        .defaultValue("50")
                                        .description("移动速度百分比，1-100")
                                        .build(),
                                OperationParam.builder()
                                        .name("async")
                                        .label("异步执行")
                                        .type("boolean")
                                        .required(false)
                                        .defaultValue("false")
                                        .description("是否异步执行")
                                        .build()
                        ))
                        .build(),
                OperationVo.builder()
                        .id(1002L)
                        .name("机械臂抓取")
                        .description("控制机械臂执行抓取动作")
                        .type("SDK")
                        .params(Arrays.asList(
                                OperationParam.builder()
                                        .name("targetType")
                                        .label("目标类型")
                                        .type("select")
                                        .required(true)
                                        .options(Arrays.asList("box", "ball", "cylinder"))
                                        .description("抓取目标类型")
                                        .build(),
                                OperationParam.builder()
                                        .name("force")
                                        .label("抓取力度")
                                        .type("number")
                                        .required(false)
                                        .defaultValue("10")
                                        .description("抓取力度，单位：牛顿")
                                        .build()
                        ))
                        .build(),
                OperationVo.builder()
                        .id(1003L)
                        .name("视觉识别")
                        .description("调用摄像头进行视觉识别")
                        .type("HTTP")
                        .params(Arrays.asList(
                                OperationParam.builder()
                                        .name("algorithmType")
                                        .label("算法类型")
                                        .type("select")
                                        .required(true)
                                        .options(Arrays.asList("object_detection", "qr_code", "face_recognition"))
                                        .description("视觉算法类型")
                                        .build(),
                                OperationParam.builder()
                                        .name("timeout")
                                        .label("超时时间")
                                        .type("number")
                                        .required(false)
                                        .defaultValue("5000")
                                        .description("识别超时时间，单位：毫秒")
                                        .build()
                        ))
                        .build()
        );
        return AjaxResult.success(operations);
    }

    @GetMapping("/{id}")
    public AjaxResult getById(@PathVariable Long id) {
        // 根据ID返回操作详情
        return AjaxResult.success(/* ... */);
    }
}