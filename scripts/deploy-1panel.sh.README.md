# deploy-1panel.sh - 1Panel专用部署脚本

## 📋 脚本说明

专为1Panel OpenResty环境设计的部署脚本，优化了目录结构和部署流程。

## 🎯 适用环境

- 1Panel控制面板
- OpenResty (Nginx) 环境  
- Ubuntu/Linux系统
- 项目部署在1Panel站点目录中

## 📁 目标目录

```
/opt/1panel/apps/openresty/openresty/www/sites/ai-code-mother/
```

## 🔧 功能特性

- ✅ 自动备份现有文件
- ✅ 检查目录权限
- ✅ 创建启动脚本
- ✅ 设置配置文件权限
- ✅ 交互式部署引导

## 🚀 使用方式

```bash
# 给脚本执行权限
chmod +x deploy-1panel.sh

# 运行部署脚本
./deploy-1panel.sh
```

## 📝 部署流程

1. 检查目标目录是否存在
2. 备份现有JAR文件和配置
3. 停止正在运行的应用
4. 等待用户上传新文件
5. 创建启动脚本 `start.sh`
6. 设置配置文件权限
7. 显示部署完成信息

## ⚙️ 配置文件

脚本会自动处理以下文件：
- `ai-code-mother-0.0.1-SNAPSHOT.jar` - 应用程序
- `.env` - 环境变量配置文件
- `.env.template` - 配置模板（可选）
- `start.sh` - 应用启动脚本

## 🔒 安全设置

- 配置文件权限：`chmod 600 .env`
- 启动脚本权限：`chmod +x start.sh`

## 📊 备份机制

自动创建带时间戳的备份文件：
- `ai-code-mother-0.0.1-SNAPSHOT.jar.backup.20250101_120000`
- `.env.backup.20250101_120000`

## ❓ 常见问题

### 1. 目录不存在错误
确保1Panel中已创建ai-code-mother网站

### 2. 权限不足
使用sudo或以正确用户身份运行

### 3. 文件上传失败
手动将文件上传到目标目录后继续

## 📞 支持

如需帮助，请检查脚本输出信息或联系开发团队。