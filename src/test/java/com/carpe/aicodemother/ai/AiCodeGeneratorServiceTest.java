package com.carpe.aicodemother.ai;

import com.carpe.aicodemother.ai.model.HtmlCodeResult;
import com.carpe.aicodemother.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("local")
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        // 切面会自动记录请求和响应日志
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个程序员鱼皮的工作记录小工具 不超过 50行");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        // 切面会自动记录请求和响应日志
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("做个程序员鱼皮的留言板 不超过 50行");
        Assertions.assertNotNull(multiFileCode);
    }
}
