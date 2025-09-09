package com.carpe.aicodemother.ai;

import com.carpe.aicodemother.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI应用名称生成服务工厂
 * 遵循项目现有的AI服务架构模式
 */
@Slf4j
@Configuration
public class AppNameGeneratorServiceFactory {

    /**
     * 创建AI应用名称生成服务实例
     * 使用路由ChatModel支持并发调用
     */
    public AppNameGeneratorService createAppNameGeneratorService() {
        // 动态获取多例的路由 ChatModel，支持并发
        ChatModel chatModel = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AppNameGeneratorService.class)
                .chatModel(chatModel)
                .build();
    }

    /**
     * 默认提供一个 Bean
     */
    @Bean
    public AppNameGeneratorService appNameGeneratorService() {
        return createAppNameGeneratorService();
    }
}