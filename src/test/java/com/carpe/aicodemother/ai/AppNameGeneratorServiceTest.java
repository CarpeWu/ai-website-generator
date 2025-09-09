package com.carpe.aicodemother.ai;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 应用名称生成服务测试
 */
@Slf4j
@SpringBootTest
public class AppNameGeneratorServiceTest {

    @Resource
    private AppNameGeneratorService appNameGeneratorService;

    @Test
    public void testGenerateAppName() {
        // 测试不同类型的用户需求
        String[] testPrompts = {
                "做一个简单的个人介绍页面",
                "创建一个在线商城，卖各种商品",
                "做一个公司官网，展示公司信息和产品",
                "开发一个博客系统，支持文章发布和评论",
                "制作一个餐厅点餐系统",
                "建立一个学习管理平台",
                "设计一个音乐播放器应用",
                "构建一个任务管理工具"
        };

        for (String prompt : testPrompts) {
            String appName = appNameGeneratorService.generateAppName(prompt);
            log.info("用户需求: {} -> 生成应用名称: {}", prompt, appName);
            
            // 验证生成的名称长度
            if (appName.length() > 20) {
                log.warn("应用名称过长: {} ({}字符)", appName, appName.length());
            }
        }
    }

    @Test
    public void testGenerateAppNameWithEdgeCases() {
        // 测试边界情况
        String[] edgeCases = {
                "", // 空字符串
                "a", // 单字符
                "这是一个非常非常非常长的用户需求描述，包含了很多很多的细节和要求，看看AI能否生成合适的应用名称", // 超长描述
                "Create an English website for international users", // 英文需求
                "做一个系统平台网站应用程序" // 包含通用词汇
        };

        for (String prompt : edgeCases) {
            try {
                String appName = appNameGeneratorService.generateAppName(prompt);
                log.info("边界测试 - 用户需求: {} -> 生成应用名称: {}", prompt, appName);
            } catch (Exception e) {
                log.error("生成应用名称失败，用户需求: {}, 错误: {}", prompt, e.getMessage());
            }
        }
    }
}