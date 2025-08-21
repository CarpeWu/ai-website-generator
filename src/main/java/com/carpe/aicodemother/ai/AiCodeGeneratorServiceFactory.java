package com.carpe.aicodemother.ai;

import com.carpe.aicodemother.ai.tools.FileWriteTool;
import com.carpe.aicodemother.exception.BusinessException;
import com.carpe.aicodemother.exception.ErrorCode;
import com.carpe.aicodemother.model.enums.CodeGenTypeEnum;
import com.carpe.aicodemother.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 代码生成服务工厂类
 * 职责：
 * 1. 管理不同类型的 AI 代码生成服务实例
 * 2. 提供基于 appId 和代码生成类型的服务缓存机制
 * 3. 根据不同的代码生成类型配置相应的 AI 模型和工具
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel qwenStreamingChatModel;

    @Resource
    private StreamingChatModel reasoningStreamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * AI 服务实例缓存
     * 作用: 缓存不同 appId 和代码生成类型组合对应的 AI 服务对象，避免重复创建，提高性能
     *
     * 缓存键策略: 使用 "appId_codeGenType" 格式的组合键
     * 例如: "123_HTML", "123_VUE_PROJECT", "456_MULTI_FILE"
     *
     * 缓存策略:
     * - 最大缓存 1000 个实例 (避免无限增长导致内存溢出)
     * - 写入后 30 分钟过期 (保证服务不会长期占用内存)
     * - 最后一次访问后 10 分钟过期 (清理冷门、不再活跃的实例)
     */
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            // 缓存最多存储 1000 个 AI 服务实例
            .maximumSize(1000)
            // 从写入时间算起, 30 分钟后过期 (即使频繁访问, 也会在写入 30 分钟后被清理)
            .expireAfterWrite(Duration.ofMinutes(30))
            // 从最后一次访问时间算起, 10 分钟后过期 (如果 10 分钟没人用, 就清理掉)
            .expireAfterAccess(Duration.ofMinutes(10))
            // 监听器: 当缓存里的实例被移除时, 打印日志, 记录移除的缓存键和原因
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除, 缓存键: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 根据 appId 获取 AI 服务实例（兼容老版本接口）
     * 默认使用 HTML 代码生成类型，保证向下兼容
     *
     * @param appId 应用 ID
     * @return AiCodeGeneratorService 对象（HTML 类型的 AI 服务）
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * 根据 appId 和代码生成类型获取 AI 服务实例（带缓存机制）
     * 支持多种代码生成类型，每种类型使用不同的 AI 模型配置
     *
     * 缓存逻辑:
     * - 命中缓存 -> 直接返回缓存的服务实例
     * - 未命中缓存 -> 创建新的服务实例并存入缓存
     *
     * @param appId       应用 ID（不同 appId 对应不同的应用）
     * @param codeGenType 代码生成类型（HTML、VUE_PROJECT、MULTI_FILE 等）
     * @return AiCodeGeneratorService 对象（带有独立记忆能力的 AI 服务）
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        String cacheKey = buildCacheKey(appId, codeGenType);
        // get(key, mappingFunction) 方法:
        // - 命中缓存 -> 返回缓存的值
        // - 未命中缓存 -> 执行 lambda 表达式创建新实例，并存入缓存
        return serviceCache.get(cacheKey, unusedKey -> createAiCodeGeneratorService(appId, codeGenType));
    }

    /**
     * 创建新的 AI 服务实例
     * 根据不同的代码生成类型配置相应的 AI 模型、工具和策略
     *
     * 服务特性:
     * - 每个 appId 拥有独立的 AI 服务对象
     * - 每个服务对象带有独立的对话记忆（MessageWindowChatMemory）
     * - 根据代码生成类型选择合适的模型和工具配置
     *
     * @param appId       应用 ID（唯一标识）
     * @param codeGenType 代码生成类型（决定使用哪种 AI 配置）
     * @return 新创建的 AiCodeGeneratorService 实例
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        log.info("为 appId: {}, 代码生成类型: {} 创建新的 AI 服务实例", appId, codeGenType.getValue());

        // 构建独立的对话记忆对象
        // 每个 appId 拥有独立的 MessageWindowChatMemory（相当于"聊天记录"）
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)                            // 记忆与 appId 绑定，保证不同应用的对话不混淆
                .chatMemoryStore(redisChatMemoryStore) // 使用 Redis 存储记忆，保证数据持久化
                .maxMessages(20)                      // 最多保存最近 20 条消息，控制内存占用
                .build();

        // 从数据库加载历史对话到记忆中
        chatHistoryService.loadChatHistory(appId, chatMemory, 20);

        // 根据代码生成类型选择相应的 AI 服务配置
        return switch (codeGenType) {
            // Vue 项目生成：复杂项目，需要推理能力强的模型 + 文件操作工具
            case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
                    .chatModel(chatModel)                           // 基础对话模型
                    .streamingChatModel(reasoningStreamingChatModel) // 推理能力强的流式模型
                    .chatMemoryProvider(memoryId -> chatMemory)     // 独立的记忆提供者
                    .tools(new FileWriteTool())                     // 文件写入工具（Vue项目需要创建多个文件）
                    // 处理工具调用幻觉问题：当 AI 尝试调用不存在的工具时的处理策略
                    .hallucinatedToolNameStrategy(toolExecutionRequest ->
                            ToolExecutionResultMessage.from(toolExecutionRequest,
                                    "Error: there is no tool called " + toolExecutionRequest.name())
                    )
                    .build();

            // HTML 单页面和多文件生成：相对简单，使用普通流式模型即可
            case HTML, MULTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)
                    .chatModel(chatModel)                    // 基础对话模型
                    .streamingChatModel(qwenStreamingChatModel) // 普通流式对话模型
                    .chatMemory(chatMemory)                  // 独立的对话记忆
                    .build();

            // 不支持的代码生成类型，抛出业务异常
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "不支持的代码生成类型: " + codeGenType.getValue());
        };
    }

    /**
     * 构造缓存键
     * 将 appId 和代码生成类型组合成唯一的缓存键
     *
     * 格式: "appId_codeGenType"
     * 例如: "123_HTML", "456_VUE_PROJECT"
     *
     * @param appId       应用 ID
     * @param codeGenType 代码生成类型枚举
     * @return 组合后的缓存键字符串
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();
    }
}