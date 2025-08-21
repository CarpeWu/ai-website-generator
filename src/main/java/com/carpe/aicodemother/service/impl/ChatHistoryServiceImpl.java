package com.carpe.aicodemother.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.carpe.aicodemother.constant.UserConstant;
import com.carpe.aicodemother.exception.ErrorCode;
import com.carpe.aicodemother.exception.ThrowUtils;
import com.carpe.aicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.carpe.aicodemother.model.entity.App;
import com.carpe.aicodemother.model.entity.User;
import com.carpe.aicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.carpe.aicodemother.service.AppService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.carpe.aicodemother.model.entity.ChatHistory;
import com.carpe.aicodemother.mapper.ChatHistoryMapper;
import com.carpe.aicodemother.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author jaeger
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService;

    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户 ID 不能为空");
        // 验证消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        return this.save(chatHistory);
    }

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    /**
     * 将聊天历史记录加载到内存中
     *
     * @param appId
     * @param chatMemory
     * @param maxCount   最多加载条数, 限制加载的历史消息数量
     * @return 实际加载的消息条数
     */
    @Override
    public int loadChatHistory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 构架数据库查询条件
            // 注意: limit(1, maxCount) 中的 1 表示偏移量, 跳过最新的 1 条记录
            // 这样做是为了排除当前正在处理的用户消息, 避免重复加载
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            // 执行查询, 获取历史记录列表
            List<ChatHistory> historyList = this.list(queryWrapper);
            // 检查查询结果是否为空
            if (CollUtil.isEmpty(historyList)) {
                log.info("appId: {} 没有找到历史对话记录", appId);
                return 0; // 返回 0 条记录
            }
            // 由于查询是按时间倒序的, 需要反转列表确保时间正序
            // 这样可以保证在聊天记忆中, 老的消息在前, 新的消息在后
            historyList = historyList.reversed();
            // 计数器: 记录实际加载的消息数量
            int loadedCount = 0;
            // 清理聊天记忆中的现有内容, 防止重复加载和内存泄露
            chatMemory.clear();
            // 遍历历史记录, 按消息类型转转并添加到聊天记忆中
            for (ChatHistory history : historyList) {
                // 处理用户消息
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    // 创建用户消息对象并添加聊天记忆
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                }
                // 处理AI消息
                else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    // 创建用户消息对象并添加聊天记忆
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++;
                }
                // 注意: 如果消息类型不是 USER 或 AI, 则跳过该消息 (可能是系统消息等)
            }
            // 记录成功加载的日志
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            // 异常处理: 记录错误日志但不抛出异常
            // 这样设计是因为历史记录加载失败不应该影响还需要业务流程
            log.error("加载历史对话失败, appId : {}, error: {}", appId, e.getMessage());
            // 返回 0 表示没有加载任何历史记录
            // 系统可以继续运行, 只是没有历史上下文信息
            return 0;
        }


    }

    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("app_id", appId);
        return this.remove(queryWrapper);
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }


}
