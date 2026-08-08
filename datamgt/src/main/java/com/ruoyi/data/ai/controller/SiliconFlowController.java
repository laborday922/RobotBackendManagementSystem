package com.ruoyi.data.ai.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.data.ai.service.SiliconFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data/ai")
public class SiliconFlowController extends BaseController {

    @Autowired
    private SiliconFlowService siliconFlowService;

    /**
     * AI 对话
     * @param question 用户问题
     * @return AI回复
     */
    @PostMapping("/chat")
    public AjaxResult chat(@RequestParam("question") String question) {
        try {
            String answer = siliconFlowService.chat(question);
            return success(answer);
        } catch (Exception e) {
            return error("AI服务调用失败：" + e.getMessage());
        }
    }
}
