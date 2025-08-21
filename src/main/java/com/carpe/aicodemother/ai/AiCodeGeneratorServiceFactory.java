package com.carpe.aicodemother.ai;

import com.carpe.aicodemother.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;
    /**
     * AI 服务实例缓存
     * 作用: 缓存不同 appId 对应的 AI 服务对象(AiCodeGeneratorService), 避免重复创建, 提高性能
     * 缓存策略:
     * - 最大缓存 1000 个实例 (避免无线增长导致内存溢出)
     * - 写入后 30 分钟 过期 (保证服务不会长期占用内存)
     * - 最后一次访问后 10 分钟过期 (清理冷门, 不再活跃的实例)
     */
    private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            // 缓存最多存储 1000 个 AI 服务实例
            .maximumSize(1000)
            // 从写入时间算起, 30 分钟后过期 (即使频繁访问, 也会在写入 30 分钟后被清理)
            .expireAfterWrite(Duration.ofMinutes(30))
            // 从最后一次访问时间算起, 10 分钟后过期 (如果 10 分钟没人用, 就清理掉)
            .expireAfterAccess(Duration.ofMinutes(10))
            // 监听器: 当缓存里的实例被移除时, 打印日志, 记录移除的 appId 和原因
            // value: AiCodeGeneratorService(这个 appId 服务的 AI 助手对象) 这里没做处理
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除, appId: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 根据 appId 获取 AI 服务实例 (带缓存机制)
     * - 如果缓存里已经有该 appId 对应的实例 -> 直接返回
     * - 如果缓存里没有 -> 调用 createAICodeGeneratorService 创建一个新的, 并放入缓存
     *
     * @param appId 应用 ID (不同 appId 对应不同的 AI 服务实例)
     * @return AiCodeGeneratorService 对象 (带有记忆能力的 AI 服务)
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        // get (key, mappingFunction) 方法:
        // - 命中缓存 -> 返回缓存的值
        // - 未命中缓存 -> 执行 mappingFunction (这里是 this::createAiCodeGeneratorService), 生成新的值,并存入缓存
        return serviceCache.get(appId, this::createAiCodeGeneratorService);

    }

    /**
     * 创建新的 AI 服务实例
     * - 每个 appId 都会拥有一个独立的 AI 服务对象
     * - 服务对象带有独立的对话记忆 (MessageWindowChatMemory)
     *
     * @param appId 应用 ID (唯一标识)
     * @return 新创建的 AiCodeGeneratorService
     */
    public AiCodeGeneratorService createAiCodeGeneratorService(long appId) {
        // 打印日志, 提示系统正在为该 appId 创建新的服务实例
        log.info("为 appId: {} 创建新的 AI 服务实例", appId);
        // 根据 appId 构建独立的对话记忆对象
        // 每个 appId 拥有独立的 MessageWindowChatMemory (相当于"聊天记录")
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)                          // 记忆与 appId 绑定, 保证不同用户的对话不混淆
                .chatMemoryStore(redisChatMemoryStore) // 使用 Redis 存储记忆, 保证数据不会因为 JVM 重启而丢失
                .maxMessages(20)                    // 最多保存最近 20 条信息, 避免内存占用过多
                .build();
        // 从数据库加载历史对话到记忆中
        chatHistoryService.loadChatHistory(appId, chatMemory, 20);
        // 使用 AiServices 构建一个新的 AiCodeGeneratorService 服务实例
        // - 配置对话模型 (chatModel)
        // - 配置流式对话模型 (StreamingChatModel)
        // - 配置该 appId 对应的对话记忆 (chatMemory)
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)                 // 普通对话模型 (一次性返回)
                .streamingChatModel(streamingChatModel) // 流式对话模型 (边生成边返回)
                .chatMemory(chatMemory)                 // 设置独立的记忆对象
                .build();
    }
}

