package com.carpe.aicodemother.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * AI应用名称生成服务
 * 根据用户提示词智能生成简洁、有意义的应用名称
 */
public interface AppNameGeneratorService {

    /**
     * 根据用户提示词生成应用名称
     *
     * @param userPrompt 用户输入的需求描述
     * @return 生成的应用名称（简洁、有意义、不超过20个字符）
     */
    @SystemMessage(fromResource = "prompt/app-name-generator-system-prompt.txt")
    String generateAppName(@UserMessage String userPrompt);
}