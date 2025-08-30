package com.carpe.aicodemother.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Data
public class ReasoningStreamingChatModelConfig {

    private String baseUrl;

    private String apiKey;

    /**
     * 推理流式模型（用于 Vue 项目生成，带工具调用）
     */
    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {

        // 通义
//        // 为了测试方便临时修改
//        final String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
//        final String modelName = "qwen-plus-2025-04-28";
//        final int maxTokens = 8192;
        // 生产环境使用：
//         final String modelName = "qwen3-235b-a22b-thinking-2507";
//         final int maxTokens = 32768;


        final String baseUrl = "https://api.deepseek.com";
        final String modelName = "deepseek-chat";
        final String apiKey = "YOUR_DEEPSEEK_API_KEY";
        final int maxTokens = 8192;
        // 生产环境使用
        // final String modelName = "deepseek-reasoner";
        // final int maxTokens = 32768;
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}