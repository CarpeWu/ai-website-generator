# deploy.sh - 通用部署脚本

## 📋 脚本说明

适用于标准Linux环境的通用部署脚本，使用systemd服务管理。

## 🎯 适用环境

- 标准Linux服务器（Ubuntu/CentOS等）
- 使用systemd系统服务管理
- 标准目录结构部署

## 📁 目标目录

```
/opt/ai-code-mother/
├── ai-code-mother.jar
├── config/
│   └── .env
├── logs/
└── start.sh
```

## 🔧 功能特性

- ✅ 创建标准目录结构
- ✅ 配置systemd服务
- ✅ 设置文件权限和所有权
- ✅ 日志目录管理
- ✅ 服务自动启动

## 🚀 使用方式

```bash
# 给脚本执行权限
chmod +x deploy.sh

# 运行部署脚本
./deploy.sh
```

## 📝 部署流程

1. 创建项目目录：`/opt/ai-code-mother/`
2. 设置目录权限和所有权
3. 上传JAR文件和配置文件
4. 创建systemd服务文件
5. 启用并启动服务
6. 配置日志轮转

## ⚙️ 配置文件

- `ai-code-mother.jar` - 应用程序jar包
- `config/.env` - 环境变量配置文件
- `logs/` - 应用日志目录

## 🔒 安全设置

- 配置文件权限：`chmod 600 config/.env`
- 目录权限：`chmod 755 /opt/ai-code-mother`
- 服务用户：专用的系统用户

## 🛠️ Systemd服务

创建服务文件：`/etc/systemd/system/ai-code-mother.service`

```ini
[Unit]
Description=AI Code Mother Application
After=network.target

[Service]
User=ai-code-mother
WorkingDirectory=/opt/ai-code-mother
ExecStart=/usr/bin/java -jar ai-code-mother.jar --spring.config.location=file:config/.env
Restart=always

[Install]
WantedBy=multi-user.target
```

## 📊 日志管理

- 系统日志：`journalctl -u ai-code-mother.service`
- 应用日志：`/opt/ai-code-mother/logs/`
- 日志轮转：配置logrotate

## ❓ 常见问题

### 1. 权限问题
确保运行脚本的用户有sudo权限

### 2. 服务启动失败
检查journalctl日志：`journalctl -u ai-code-mother.service`

### 3. 端口冲突
修改应用配置中的SERVER_PORT

## 📞 支持

适用于标准的Linux服务器环境，如需1Panel环境请使用deploy-1panel.sh。