# 部署和管理脚本说明

本目录包含AI Code Mother项目的部署和配置管理脚本，分为通用版本和1Panel专用版本。

## 📋 脚本文件说明

### 通用脚本（适用于标准Linux环境）
- **`deploy.sh`** - 通用部署脚本（systemd服务管理）
- **`manage-config.sh`** - 通用配置管理工具

### 1Panel专用脚本（针对1Panel OpenResty环境优化）
- **`deploy-1panel.sh`** - 1Panel专用部署脚本
- **`manage-config-1panel.sh`** - 1Panel专用配置管理工具

## 🎯 使用建议

### 对于1Panel环境（推荐）
```bash
# 首次部署
./deploy-1panel.sh

# 配置管理
./manage-config-1panel.sh show        # 查看配置
./manage-config-1panel.sh update KEY VALUE  # 更新配置
./manage-config-1panel.sh restart     # 重启应用
```

### 对于标准Linux环境
```bash
# 首次部署
./deploy.sh

# 配置管理  
./manage-config.sh show
./manage-config.sh update KEY VALUE
./manage-config.sh restart
```

## 🔧 脚本功能对比

| 功能 | 通用脚本 | 1Panel专用脚本 |
|------|----------|---------------|
| 目标环境 | 标准Linux服务器 | 1Panel OpenResty |
| 目录结构 | `/opt/ai-code-mother/` | 1Panel站点目录 |
| 服务管理 | systemd服务 | 进程管理 |
| 配置备份 | 需要手动备份 | 自动备份 |
| 日志管理 | journalctl | 文件日志 |

## 📝 使用示例

### 1. 部署应用
```bash
chmod +x deploy-1panel.sh
./deploy-1panel.sh
```

### 2. 查看当前配置
```bash
./manage-config-1panel.sh show
```

### 3. 更新API密钥
```bash
./manage-config-1panel.sh update CHAT_MODEL_API_KEY sk-your-new-key
```

### 4. 重启应用
```bash
./manage-config-1panel.sh restart
```

## ⚠️ 注意事项

1. 首次使用前给脚本执行权限：`chmod +x *.sh`
2. 1Panel环境推荐使用专用脚本（带`-1panel`后缀）
3. 配置文件中包含敏感信息，不要提交到代码仓库
4. 定期备份您的`.env`配置文件

## 📞 支持

如有问题，请参考各脚本文件的详细说明文档。