# manage-config-1panel.sh - 1Panel专用配置管理工具

## 📋 脚本说明

专为1Panel环境优化的配置管理工具，提供安全的配置查看、更新和应用重启功能。

## 🎯 适用环境

- 1Panel OpenResty环境
- 项目部署在1Panel站点目录中
- 使用外部环境变量配置

## 🔧 功能特性

- ✅ **安全查看配置** - 敏感信息自动脱敏显示
- ✅ **配置更新** - 更新单个或多个配置项
- ✅ **自动备份** - 修改前自动创建备份
- ✅ **应用重启** - 一键重启应用服务
- ✅ **颜色输出** - 友好的彩色终端输出

## 🚀 使用方式

```bash
# 给脚本执行权限
chmod +x manage-config-1panel.sh

# 查看帮助信息
./manage-config-1panel.sh help

# 查看当前配置（脱敏）
./manage-config-1panel.sh show

# 更新配置项
./manage-config-1panel.sh update KEY VALUE

# 重启应用
./manage-config-1panel.sh restart
```

## 📝 命令详解

### 查看配置
```bash
./manage-config-1panel.sh show
```
- 显示所有配置项
- API密钥等敏感信息会脱敏显示（前2后2字符）
- 注释和空行保持原样显示

### 更新配置
```bash
# 更新API密钥
./manage-config-1panel.sh update CHAT_MODEL_API_KEY sk-new-key-12345

# 更新模型名称  
./manage-config-1panel.sh update CHAT_MODEL_NAME gpt-4-turbo

# 更新基础URL
./manage-config-1panel.sh update CHAT_MODEL_BASE_URL https://api.openai.com/v1
```

### 重启应用
```bash
./manage-config-1panel.sh restart
```
- 安全停止当前应用
- 等待进程完全停止
- 重新启动应用
- 显示启动日志位置

## 🔒 安全特性

### 敏感信息脱敏
- `sk-real-api-key-12345` → `sk****45`
- `secret-password` → `se****rd`
- 只显示前2个和后2个字符

### 自动备份
- 每次修改前自动创建备份
- 备份文件：`config-backups/.env.backup.20250101_120000`
- 保留修改历史，便于回滚

### 配置文件保护
- 配置文件权限：`chmod 600 .env`
- 备份目录自动创建

## 📁 文件结构

```
/opt/1panel/apps/openresty/openresty/www/sites/ai-code-mother/
├── .env                    # 主配置文件
├── config-backups/         # 配置备份目录
│   └── .env.backup.20250101_120000
└── manage-config-1panel.sh # 本脚本
```

## 🛠️ 技术实现

### 配置文件位置
```bash
CONFIG_FILE="/opt/1panel/apps/openresty/openresty/www/sites/ai-code-mother/.env"
```

### 备份机制
```bash
BACKUP_DIR="/opt/1panel/apps/openresty/openresty/www/sites/ai-code-mother/config-backups"
```

### 进程管理
- 使用 `pkill -f "ai-code-mother"` 停止应用
- 使用 `nohup ./start.sh` 启动应用

## ❓ 常见问题

### 1. 配置更新后不生效
确保运行了 `./manage-config-1panel.sh restart`

### 2. 权限错误
检查脚本和执行用户是否有文件读写权限

### 3. 进程无法停止
使用 `pkill -9 -f "ai-code-mother"` 强制停止

## 📞 支持

使用 `./manage-config-1panel.sh help` 查看详细帮助信息。