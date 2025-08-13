package com.carpe.aicodemother.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ChatModel日志记录切面
 * 用于记录通义千问API的请求和响应日志
 */
@Slf4j
@Aspect
@Component
public class ChatModelLoggingAspect {

    @Value("${langchain4j.community.dashscope.chat-model.log-requests:false}")
    private boolean logRequests;

    @Value("${langchain4j.community.dashscope.chat-model.log-responses:false}")
    private boolean logResponses;

    public ChatModelLoggingAspect() {
        log.info("ChatModelLoggingAspect 已创建，logRequests: {}, logResponses: {}", logRequests, logResponses);
    }

    @Around("execution(* dev.langchain4j.model.chat.ChatModel.chat(..)) || execution(* dev.langchain4j.model.chat.ChatModel.generate(..))")
    public Object logChatModelCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        String requestId = generateRequestId();
        
        // 添加调试日志
        log.debug("ChatModelLoggingAspect intercepted call: {}.{} with requestId: {}, logRequests: {}, logResponses: {}", 
                 joinPoint.getTarget().getClass().getSimpleName(),
                 joinPoint.getSignature().getName(),
                 requestId, logRequests, logResponses);
        
        // 记录请求日志
        if (logRequests) {
            logRequest(requestId, joinPoint.getArgs());
        }
        
        long startTime = System.currentTimeMillis();
        Object result;
        
        try {
            result = joinPoint.proceed();
            
            // 记录响应日志
            if (logResponses && result instanceof Response) {
                logResponse(requestId, (Response<AiMessage>) result, System.currentTimeMillis() - startTime);
            } else if (logResponses) {
                // 对于chat方法，结果可能不是Response类型
                logResponseForChat(requestId, result, System.currentTimeMillis() - startTime);
            }
            
        } catch (Exception e) {
            // 记录错误日志
            log.error("[{}] ChatModel request failed after {}ms", requestId, System.currentTimeMillis() - startTime, e);
            throw e;
        }
        
        return result;
    }

    private void logRequest(String requestId, Object[] args) {
        if (args.length == 0 || !(args[0] instanceof List)) {
            return;
        }
        
        @SuppressWarnings("unchecked")
        List<ChatMessage> messages = (List<ChatMessage>) args[0];
        
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("\n=== REQUEST [").append(requestId).append("] ===\n");
        logBuilder.append("Timestamp: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))).append("\n");
        logBuilder.append("Messages Count: ").append(messages.size()).append("\n");
        logBuilder.append("Messages:\n");
        
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage message = messages.get(i);
            logBuilder.append("  [").append(i + 1).append("] ").append(message.getClass().getSimpleName()).append(": ");
            
            String messageText = getMessageText(message);
            logBuilder.append(messageText).append("\n");
        }
        
        log.info(logBuilder.toString());
    }

    private String getMessageText(ChatMessage message) {
        try {
            if (message instanceof SystemMessage) {
                return "(System) " + ((SystemMessage) message).text();
            } else if (message instanceof UserMessage) {
                return "(User) " + getTextFromUserMessage((UserMessage) message);
            } else if (message instanceof AiMessage) {
                return "(AI) " + ((AiMessage) message).text();
            } else {
                return message.toString();
            }
        } catch (Exception e) {
            // 如果text()方法不存在，使用toString()
            return message.toString();
        }
    }

    private String getTextFromUserMessage(UserMessage userMessage) {
        try {
            // 尝试使用反射获取text方法
            java.lang.reflect.Method textMethod = userMessage.getClass().getMethod("text");
            return (String) textMethod.invoke(userMessage);
        } catch (Exception e) {
            // 如果反射失败，使用toString()
            return userMessage.toString();
        }
    }

    private void logResponse(String requestId, Response<AiMessage> response, long duration) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("\n=== RESPONSE [").append(requestId).append("] ===\n");
        logBuilder.append("Timestamp: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))).append("\n");
        logBuilder.append("Duration: ").append(duration).append("ms\n");
        logBuilder.append("Content: ").append(response.content().text()).append("\n");
        
        if (response.tokenUsage() != null) {
            logBuilder.append("Token Usage: ").append(response.tokenUsage()).append("\n");
        }
        
        if (response.finishReason() != null) {
            logBuilder.append("Finish Reason: ").append(response.finishReason()).append("\n");
        }
        
        log.info(logBuilder.toString());
    }

    private void logResponseForChat(String requestId, Object result, long duration) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("\n=== RESPONSE [").append(requestId).append("] ===\n");
        logBuilder.append("Timestamp: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))).append("\n");
        logBuilder.append("Duration: ").append(duration).append("ms\n");
        logBuilder.append("Content: ").append(result.toString()).append("\n"); // Assuming result is the final response object
        
        log.info(logBuilder.toString());
    }

    private String generateRequestId() {
        return "req_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }
}
