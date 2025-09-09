package com.carpe.aicodemother.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
public class AiModelMetricsCollector {

    @Resource
    private MeterRegistry meterRegistry;

    // 缓存已创建的指标，避免重复创建（按指标类型分离缓存）
    private final ConcurrentMap<String, Counter> requestCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> errorCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> tokenCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> responseTimersCache = new ConcurrentHashMap<>();

    /**
     * 记录请求次数
     */
    public void recordRequest(String userId, String appId, String modelName, String status) {
        try {
            String key = String.format("%s_%s_%s_%s", userId, appId, modelName, status);
            Counter counter = requestCountersCache.computeIfAbsent(key, k ->
                    Counter.builder("ai_model_requests_total")
                            .description("AI模型总请求次数")
                            .tag("user_id", userId)
                            .tag("app_id", appId)
                            .tag("model_name", modelName)
                            .tag("status", status)
                            .register(meterRegistry)
            );
            counter.increment();
        } catch (Exception e) {
            log.warn("Failed to record request metric: {}", e.getMessage());
            // 监控功能失败不应影响主要业务流程
        }
    }

    /**
     * 记录错误
     */
    public void recordError(String userId, String appId, String modelName, String errorMessage) {
        try {
            String key = String.format("%s_%s_%s_%s", userId, appId, modelName, errorMessage);
            Counter counter = errorCountersCache.computeIfAbsent(key, k ->
                    Counter.builder("ai_model_errors_total")
                            .description("AI模型错误次数")
                            .tag("user_id", userId)
                            .tag("app_id", appId)
                            .tag("model_name", modelName)
                            .tag("error_message", errorMessage)
                            .register(meterRegistry)
            );
            counter.increment();
        } catch (Exception e) {
            log.warn("Failed to record error metric: {}", e.getMessage());
            // 监控功能失败不应影响主要业务流程
        }
    }

    /**
     * 记录Token消耗
     */
    public void recordTokenUsage(String userId, String appId, String modelName,
                                 String tokenType, long tokenCount) {
        try {
            String key = String.format("%s_%s_%s_%s", userId, appId, modelName, tokenType);
            Counter counter = tokenCountersCache.computeIfAbsent(key, k ->
                    Counter.builder("ai_model_tokens_total")
                            .description("AI模型Token消耗总数")
                            .tag("user_id", userId)
                            .tag("app_id", appId)
                            .tag("model_name", modelName)
                            .tag("token_type", tokenType)
                            .register(meterRegistry)
            );
            counter.increment(tokenCount);
        } catch (Exception e) {
            log.warn("Failed to record token usage metric: {}", e.getMessage());
            // 监控功能失败不应影响主要业务流程
        }
    }

    /**
     * 记录响应时间
     */
    public void recordResponseTime(String userId, String appId, String modelName, Duration duration) {
        try {
            String key = String.format("%s_%s_%s", userId, appId, modelName);
            Timer timer = responseTimersCache.computeIfAbsent(key, k ->
                    Timer.builder("ai_model_response_duration_seconds")
                            .description("AI模型响应时间")
                            .tag("user_id", userId)
                            .tag("app_id", appId)
                            .tag("model_name", modelName)
                            .register(meterRegistry)
            );
            timer.record(duration);
        } catch (Exception e) {
            log.warn("Failed to record response time metric: {}", e.getMessage());
            // 监控功能失败不应影响主要业务流程
        }
    }
}
