# AI Code Mother

AI代码生成器，基于通义千问API实现。

## 功能特性

- 基于通义千问API的代码生成
- 支持HTML代码生成
- 支持多文件代码生成（HTML、CSS、JavaScript）
- 用户管理和权限控制
- 完整的日志记录功能

## 通义千问API日志记录

### 问题描述

通义千问API不支持原生的 `log-requests` 和 `log-responses` 配置选项：

```yaml
langchain4j:
  community:
    dashscope:
      chat-model:
        api-key: your-api-key
        model-name: qwen3-235b-a22b
        # 这些配置在通义千问中不支持
        # log-requests: true
        # log-responses: true
```

### 解决方案

我们通过Spring AOP实现了自定义的日志记录功能，可以记录：

1. **请求日志**：记录发送给通义千问API的请求内容
2. **响应日志**：记录通义千问API的响应内容
3. **性能监控**：记录请求耗时
4. **错误日志**：记录请求失败的错误信息

### 配置方法

在 `application-local.yml` 中启用日志记录：

```yaml
langchain4j:
  community:
    dashscope:
      chat-model:
        api-key: your-api-key
        model-name: qwen3-235b-a22b
        # 启用请求日志记录
        log-requests: true
        # 启用响应日志记录
        log-responses: true
```

### 日志格式

#### 请求日志格式

```
=== REQUEST [req_1703123456789_123] ===
Timestamp: 2023-12-21 10:30:45.123
Messages Count: 2
Messages:
  [1] SystemMessage: (System) 你是一位资深的Web前端开发专家...
  [2] UserMessage: (User) 做个程序员鱼皮的工作记录小工具 不超过 50行
```

#### 响应日志格式

```
=== RESPONSE [req_1703123456789_123] ===
Timestamp: 2023-12-21 10:30:46.456
Duration: 1333ms
Content: 生成的HTML代码内容...
Token Usage: {inputTokenCount=150, outputTokenCount=300}
Finish Reason: STOP
```

### 实现原理

1. **AOP切面**：使用Spring AOP拦截所有ChatModel的generate方法调用
2. **反射机制**：通过反射安全地获取消息内容，兼容不同版本的langchain4j
3. **配置驱动**：通过配置文件控制日志记录的开启/关闭
4. **性能优化**：只在需要时记录日志，不影响正常性能

### 优势

- ✅ 完全兼容通义千问API
- ✅ 不影响原有功能
- ✅ 可配置的日志级别
- ✅ 详细的请求和响应信息
- ✅ 性能监控和错误追踪
- ✅ 易于扩展和维护

## 开发环境

- Java 17
- Spring Boot 3.5.4
- LangChain4j 1.1.0
- MySQL 8.0+

## 快速开始

1. 克隆项目
2. 配置数据库连接
3. 配置通义千问API密钥
4. 运行项目
5. 访问 http://localhost:8123/api

## 许可证

MIT License
