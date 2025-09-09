# manage-config.sh - 通用配置管理工具

## 📋 脚本说明

适用于标准Linux环境的通用配置管理工具，支持配置查看、更新和systemd服务管理。

## 🎯 适用环境

- 标准Linux服务器环境
- 使用systemd服务管理
- 标准目录结构部署

## 🔧 功能特性

- ✅ **安全查看配置** - 敏感信息自动脱敏显示
- ✅ **配置更新** - 更新单个或多个配置项  
- ✅ **自动备份** - 修改前自动创建备份
- ✅ **服务管理** - systemd服务重启
- ✅ **颜色输出** - 友好的彩色终端输出

## 🚀 使用方式

```bash
# 给脚本执行权限
chmod +x manage-config.sh

# 查看帮助信息
./manage-config.sh help

# 查看当前配置（脱敏）
./manage-config.sh show

# 更新配置项
./manage-config.sh update KEY VALUE

# 重启应用服务
./manage-config.sh restart
```

## 📝 命令详解

### 查看配置
```bash
./manage-config.sh show
```
- 显示所有配置项
- API密钥等敏感信息脱敏显示
- 保持配置文件的注释和格式

### 更新配置
```bash
# 更新数据库配置
./manage-config.sh update DATASOURCE_URL jdbc:mysql://new-db:3306/mydb

# 更新API密钥
./manage-config.sh update AI_API_KEY sk-new-key-12345

# 更新Redis配置
./manage-config.sh update REDIS_HOST redis-server
```

### 重启服务
```bash
./manage-config.sh restart
```
- 使用systemctl重启服务
- 显示服务状态信息
- 查看启动日志

## 🔒 安全特性

### 敏感信息脱敏
- `sk-real-api-key-12345` → `sk****45`
- `secret-password` → `se****rd`
- 只显示前2个和后2个字符

### 自动备份
- 每次修改前自动创建备份
- 备份文件：`config/.env.backup.20250101_120000`
- 保留修改历史记录

### 配置文件保护
- 配置文件权限：`chmod 600 config/.env`
- 安全的配置更新机制

## 📁 文件结构

```
/opt/ai-code-mother/
├── config/
│   ├── .env                    # 主配置文件
│   └── .env.backup.20250101    # 备份文件
├── manage-config.sh            # 本脚本
└── ai-code-mother.jar          # 应用程序
```

## 🛠️ 技术实现

### 配置文件位置
```bash
CONFIG_FILE="/opt/ai-code-mother/config/.env"
```

### 服务管理
- 使用 `systemctl restart ai-code-mother.service`
- 服务状态检查：`systemctl status ai-code-mother.service`

### 备份机制
- 自动创建带时间戳的备份
- 备份文件保存在config目录中

## ❓ 常见问题

### 1. 权限错误
确保脚本有执行权限，用户有配置文件读写权限

### 2. 服务重启失败
检查systemd服务状态：`systemctl status ai-code-mother.service`

### 3. 配置不生效
确认已重启服务使新配置生效

## 📞 支持

适用于标准Linux服务器环境，如需1Panel环境请使用manage-config-1panel.sh。