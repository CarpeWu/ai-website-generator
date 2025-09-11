# 零代码应用生成平台详细技术文档

## 📖 项目概述

**AI Code Mother** 是一个基于 Spring Boot 3 + LangChain4j + Vue 3 开发的企业级 AI 代码生成平台。用户可以通过自然语言描述需求，AI 自动分析并生成完整的 Web 应用程序，支持可视化编辑、一键部署分享等功能。

## 🏗️ 技术栈分析

### 后端技术栈
- **Java 21** - 开发语言
- **Spring Boot 3.5.4** - 企业级 Java 框架
- **LangChain4j 1.1.0** - AI 应用开发框架
- **LangGraph4j 1.6.0** - AI 工作流实现
- **MyBatis-Flex 1.11.1** - ORM 框架
- **MySQL 8.0+** - 关系型数据库
- **Redis + Redisson** - 缓存和分布式锁
- **Spring Session** - 会话管理
- **Caffeine** - 本地缓存
- **Hutool 5.8.38** - 工具类库
- **Selenium 4.33.0** - 网页自动化测试
- **Prometheus + Micrometer** - 监控系统

### 前端技术栈
- **Vue 3** - 渐进式 JavaScript 框架
- **TypeScript** - 类型安全的 JavaScript
- **Vite** - 快速构建工具
- **Element Plus** - UI 组件库
- **Axios** - HTTP 客户端

### AI 服务提供商
- **阿里云 DashScope** - 主要 AI 模型提供商
- **智谱 AI GLM-4.5** - 推理任务模型
- **腾讯云 COS** - 对象存储服务
- **Pexels** - 图片搜索服务

## 📁 项目结构

```
ai-code-mother/
├── src/main/java/com/carpe/aicodemother/     # 后端源码
│   ├── ai/                                   # AI 相关模块
│   │   ├── agent/                           # AI 智能体
│   │   ├── model/                           # AI 数据模型
│   │   ├── service/                         # AI 服务
│   │   └── tools/                           # AI 工具集
│   ├── controller/                          # 控制器层
│   ├── service/                             # 服务层
│   ├── mapper/                              # 数据访问层
│   ├── model/                               # 数据模型
│   ├── core/                                # 核心业务逻辑
│   ├── langgraph4j/                         # AI 工作流
│   ├── config/                              # 配置类
│   ├── common/                              # 通用组件
│   ├── exception/                           # 异常处理
│   ├── utils/                               # 工具类
│   └── monitor/                            # 监控相关
├── src/main/resources/                      # 配置文件
│   ├── application.yml                      # 主配置文件
│   ├── application-local.yml                # 本地环境配置
│   ├── application-prod.yml                 # 生产环境配置
│   └── prompt/                              # AI 提示词模板
├── ai-code-mother-frontend/                 # 前端源码
│   ├── src/                                 # 源代码
│   │   ├── api/                             # API 接口
│   │   ├── components/                      # 组件库
│   │   ├── pages/                           # 页面
│   │   ├── stores/                          # 状态管理
│   │   └── utils/                           # 工具函数
│   └── package.json                         # 依赖配置
├── scripts/                                 # 部署脚本
│   ├── deploy.sh                            # 部署脚本
│   ├── deploy-1panel.sh                     # 1Panel 部署脚本
│   └── manage-config.sh                     # 配置管理脚本
└── 配置.yml                                  # 环境变量配置
```

## 🔧 核心功能模块

### 1. 智能代码生成系统

#### 功能描述
- 用户输入需求描述，AI 自动分析并选择合适的生成策略
- 通过工具调用生成代码文件
- 流式输出让用户实时看到 AI 的执行过程

#### 技术实现
- **路由服务**：`AiCodeGenTypeRoutingService` 根据用户需求选择合适的代码生成策略
- **代码生成器工厂**：`AiCodeGeneratorServiceFactory` 提供多种代码生成器
- **工具管理器**：`ToolManager` 统一管理所有 AI 工具
- **流式响应**：使用 `Flux<ServerSentEvent<String>>` 实现实时响应

#### 关键类
- `AppController.java:59-86` - 代码生成接口
- `AppService.java:31` - 代码生成服务方法
- `CodeGenWorkflow.java` - 代码生成工作流

### 2. AI 工作流系统

#### 工作流节点
1. **ImageCollectorNode** - 图片收集节点
2. **PromptEnhancerNode** - 提示词增强节点
3. **RouterNode** - 路由决策节点
4. **CodeGeneratorNode** - 代码生成节点
5. **CodeQualityCheckNode** - 代码质量检查节点
6. **ProjectBuilderNode** - 项目构建节点

#### 技术特点
- 基于 LangGraph4j 实现复杂 AI 工作流
- 支持条件分支和并行处理
- 状态管理和错误恢复机制

### 3. 可视化编辑系统

#### 功能描述
- 生成的应用实时展示
- 支持元素选择和 AI 对话修改
- 实时预览和调试

#### 技术实现
- 前端使用 Vue 3 + Element Plus
- 实时通信使用 WebSocket
- 代码编辑器集成

### 4. 一键部署系统

#### 功能描述
- 将生成的应用一键部署到云端
- 自动截取封面图
- 获得可访问的地址进行分享
- 支持完整项目源码下载

#### 技术实现
- **部署服务**：`AppService.java:49` 部署方法
- **项目下载**：`ProjectDownloadService` 提供源码下载
- **截图服务**：使用 Selenium 实现网页截图
- **对象存储**：集成腾讯云 COS 存储封面图

### 5. 企业级管理系统

#### 用户管理
- 用户注册、登录、权限管理
- 基于角色的访问控制 (RBAC)
- 会话管理和安全防护

#### 应用管理
- 应用创建、编辑、删除
- 分页查询和筛选
- 精选应用设置

#### 系统监控
- Prometheus 监控指标
- AI 调用统计
- 性能监控和告警

## 🏛️ 架构设计

### 整体架构
```
┌─────────────────────────────────────────────────────────────────┐
│                        前端层 (Vue 3)                          │
├─────────────────────────────────────────────────────────────────┤
│                        API 网关层                               │
├─────────────────────────────────────────────────────────────────┤
│                      业务服务层                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐           │
│  │   用户服务   │  │   应用服务   │  │   AI 服务    │           │
│  └─────────────┘  └─────────────┘  └─────────────┘           │
├─────────────────────────────────────────────────────────────────┤
│                      核心引擎层                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐           │
│  │ 代码生成引擎 │  │ AI 工作流    │  │  工具管理器  │           │
│  └─────────────┘  └─────────────┘  └─────────────┘           │
├─────────────────────────────────────────────────────────────────┤
│                      数据存储层                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐           │
│  │   MySQL    │  │    Redis    │  │   对象存储   │           │
│  └─────────────┘  └─────────────┘  └─────────────┘           │
└─────────────────────────────────────────────────────────────────┘
```

### AI 服务架构
```
┌─────────────────────────────────────────────────────────────────┐
│                      AI 服务层                                  │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐           │
│  │  路由模型    │  │  生成模型    │  │  推理模型    │           │
│  │ (qwen-coder) │  │ (qwen-coder) │  │ (GLM-4.5)   │           │
│  └─────────────┘  └─────────────┘  └─────────────┘           │
├─────────────────────────────────────────────────────────────────┤
│                    LangChain4j 中间层                           │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐           │
│  │  工具调用    │  │  记忆管理    │  │  流式输出    │           │
│  └─────────────┘  └─────────────┘  └─────────────┘           │
└─────────────────────────────────────────────────────────────────┘
```

### 数据模型设计

#### 核心实体
- **User** - 用户信息
- **App** - 应用信息
- **ChatHistory** - 聊天历史
- **SystemMonitor** - 系统监控数据

#### 数据关系
- 用户 -> 应用：一对多关系
- 应用 -> 聊天历史：一对多关系
- 应用 -> 监控数据：一对一关系

## 🔌 接口设计

### RESTful API 设计
- `/api/app/chat/gen/code` - 代码生成接口 (SSE)
- `/api/app/add` - 创建应用
- `/api/app/update` - 更新应用
- `/api/app/delete` - 删除应用
- `/api/app/deploy` - 部署应用
- `/api/app/download/{appId}` - 下载应用代码
- `/api/app/my/list/page/vo` - 获取我的应用列表
- `/api/app/good/list/page/vo` - 获取精选应用列表

### 响应格式
```json
{
  "code": 0,
  "data": {},
  "message": "success"
}
```

## 📊 监控指标

### 系统监控
- **健康检查**：`/actuator/health`
- **Prometheus 指标**：`/actuator/prometheus`
- **应用信息**：`/actuator/info`

### 业务监控
- AI 调用次数和成功率
- Token 消耗统计
- 用户活跃度
- 应用生成成功率

## 🚀 部署配置

### 环境配置
项目支持多环境配置：
- **本地环境**：`application-local.yml`
- **生产环境**：`application-prod.yml`
- **测试环境**：`application-test.yml`

### 关键配置项
```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_code_mother
    username: root
    password: ${DATASOURCE_PASSWORD}

# Redis 配置
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}

# AI 模型配置
langchain4j:
  open-ai:
    chat-model:
      base-url: ${CHAT_MODEL_BASE_URL}
      api-key: ${CHAT_MODEL_API_KEY}
      model-name: ${CHAT_MODEL_NAME}
```

### 部署脚本
- `deploy.sh` - 通用部署脚本
- `deploy-1panel.sh` - 1Panel 面板部署脚本
- `manage-config.sh` - 配置管理脚本

## 🔒 安全设计

### 认证授权
- 基于 Session 的用户认证
- 角色权限控制（用户/管理员）
- 接口权限注解 `@AuthCheck`

### 数据安全
- 密码加密存储
- API 密钥加密管理
- 敏感信息脱敏

### 访问控制
- 用户只能操作自己的应用
- 管理员拥有全部权限
- API 接口限流 `@RateLimit`

## 🎯 性能优化

### 缓存策略
- **多级缓存**：Redis + Caffeine
- **缓存注解**：`@Cacheable`
- **缓存键生成**：`CacheKeyUtils`

### 并发处理
- **响应式编程**：使用 Reactor 和 Flux
- **异步处理**：AI 调用异步执行
- **连接池**：数据库连接池和 HTTP 连接池

### 资源管理
- **限流控制**：用户级别的请求限流
- **超时控制**：AI 调用超时设置
- **内存管理**：代码生成目录定期清理

## 🔧 开发指南

### 本地开发环境
1. **JDK 17+**
2. **Maven 3.6+**
3. **MySQL 8.0+**
4. **Redis 6.0+**
5. **Node.js 18+**

### 启动步骤
```bash
# 后端启动
./mvnw clean install
./mvnw spring-boot:run

# 前端启动
cd ai-code-mother-frontend
npm install
npm run dev
```

### 开发规范
- 代码风格：遵循阿里巴巴 Java 开发规范
- Git 提交：使用 Conventional Commits
- API 文档：使用 Knife4j 自动生成
- 单元测试：使用 JUnit 5

## 📈 扩展性设计

### 模块化设计
- AI 工具可插拔扩展
- 代码生成器可独立扩展
- 监控指标可自定义

### 微服务支持
- 服务注册发现预留接口
- 分布式事务支持
- 配置中心集成

### 多租户架构
- 用户数据隔离
- AI 服务隔离
- 资源配额管理

## 🚨 故障处理

### 常见问题
1. **AI 服务不可用**：检查 API 密钥和网络连接
2. **数据库连接失败**：检查数据库配置和连接池
3. **Redis 连接失败**：检查 Redis 服务和密码配置
4. **代码生成失败**：检查 AI 模型配置和提示词

### 日志管理
- 使用 SLF4J + Logback
- 分环境配置日志级别
- 结构化日志输出

### 错误处理
- 统一异常处理 `GlobalExceptionHandler`
- 错误码规范 `ErrorCode`
- 业务异常 `BusinessException`

## 📝 更新日志

### 最近更新
- **2025-09-11**: 修复主页查看应用逻辑
- **2025-09-10**: 优化 Prometheus 监控配置
- **2025-09-08**: 实现管理员和应用管理页面默认排序
- **2025-09-07**: 根据用户角色实现聊天历史访问控制

### 功能迭代
- **V1.0.0**: 基础代码生成功能
- **V1.1.0**: 可视化编辑功能
- **V1.2.0**: 一键部署功能
- **V1.3.0**: 企业级管理功能
- **V1.4.0**: AI 工作流优化

## 🤝 贡献指南

### 开发流程
1. Fork 项目
2. 创建功能分支
3. 提交代码变更
4. 创建 Pull Request
5. 代码审查和合并

### 代码规范
- 遵循项目现有的代码风格
- 编写单元测试
- 更新相关文档

## 📄 许可证

本项目采用 MIT 许可证 - 查看 LICENSE 文件了解详情。

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [LangChain4j](https://docs.langchain4j.dev/)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [智谱 AI](https://open.bigmodel.cn/)
- [阿里云](https://www.aliyun.com/)

---

**文档生成时间**: 2025-09-11
**项目版本**: 0.0.1-SNAPSHOT
**最后更新**: 基于最新代码分析生成