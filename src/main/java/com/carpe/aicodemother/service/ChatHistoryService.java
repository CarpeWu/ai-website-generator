package com.carpe.aicodemother.service;

import com.carpe.aicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.carpe.aicodemother.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.carpe.aicodemother.model.entity.ChatHistory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author jaeger
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     *  将聊天历史记录加载到内存中
     * @param appId
     * @param chatMemory
     * @param maxCount   最多加载条数, 限制加载的历史消息数量
     * @return 实际加载的消息条数
     */
    int loadChatHistory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);


    /**
     * 根据应用 id 删除对话历史
     *
     * @param appId 应用 is
     * @return
     */
    boolean deleteByAppId(Long appId);

    /**
     * 添加对话历史
     *
     * @param appId       应用 id
     * @param message     消息
     * @param messageType 消息类型
     * @param userId      用户 id
     * @return
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 分页查询某 APP 的对话记录
     *
     * @param appId
     * @param pageSize
     * @param lastCreateTime
     * @param loginUser
     * @return
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);
}
