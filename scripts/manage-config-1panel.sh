#!/bin/bash

# 1Panel OpenResty 环境配置管理脚本
# 用于管理 /opt/1panel/apps/openresty/openresty/www/sites/ai-code-mother/.env 配置文件

CONFIG_FILE="/opt/1panel/apps/openresty/openresty/www/sites/ai-code-mother/.env"
BACKUP_DIR="/opt/1panel/apps/openresty/openresty/www/sites/ai-code-mother/config-backups"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查配置文件是否存在
check_config_file() {
    if [ ! -f "$CONFIG_FILE" ]; then
        echo -e "${RED}错误: 配置文件不存在: $CONFIG_FILE${NC}"
        echo "请先运行部署脚本或手动创建配置文件"
        exit 1
    fi
}

# 创建备份
create_backup() {
    local timestamp=$(date +%Y%m%d_%H%M%S)
    mkdir -p "$BACKUP_DIR"
    local backup_file="$BACKUP_DIR/.env.backup.$timestamp"
    cp "$CONFIG_FILE" "$backup_file"
    echo -e "${GREEN}已创建备份: $backup_file${NC}"
}

# 显示配置（脱敏敏感信息）
show_config() {
    check_config_file
    echo -e "${BLUE}=== 当前配置（敏感信息已脱敏） ===${NC}"
    
    while IFS= read -r line; do
        if [[ "$line" =~ ^[[:space:]]*# ]] || [[ -z "$line" ]]; then
            # 注释行或空行，直接显示
            echo "$line"
        elif [[ "$line" =~ (API_KEY|KEY|SECRET|PASSWORD|TOKEN)= ]]; then
            # 敏感信息，脱敏显示
            key=$(echo "$line" | cut -d'=' -f1)
            value=$(echo "$line" | cut -d'=' -f2-)
            if [ -n "$value" ] && [ "$value" != "your_"* ]; then
                # 显示部分信息（前2个字符和最后2个字符）
                masked_value="${value:0:2}****${value: -2}"
                echo "${key}=${masked_value}"
            else
                echo "$line"
            fi
        else
            # 普通配置行
            echo "$line"
        fi
    done < "$CONFIG_FILE"
}

# 更新配置项
update_config() {
    local key="$1"
    local value="$2"
    
    check_config_file
    create_backup
    
    if grep -q "^$key=" "$CONFIG_FILE"; then
        # 更新现有配置
        sed -i.bak "s|^$key=.*|$key=$value|" "$CONFIG_FILE"
        echo -e "${GREEN}已更新配置: $key=${value:0:2}****${value: -2}${NC}"
    else
        # 添加新配置
        echo "$key=$value" >> "$CONFIG_FILE"
        echo -e "${GREEN}已添加配置: $key=${value:0:2}****${value: -2}${NC}"
    fi
}

# 重启应用
restart_app() {
    echo -e "${YELLOW}重启应用中...${NC}"
    
    # 停止现有应用
    pkill -f "ai-code-mother" || true
    sleep 2
    
    # 确保进程已停止
    if pgrep -f "ai-code-mother" > /dev/null; then
        echo -e "${YELLOW}强制停止残留进程...${NC}"
        pkill -9 -f "ai-code-mother"
        sleep 1
    fi
    
    # 切换到应用目录
    cd "/opt/1panel/apps/openresty/openresty/www/sites/ai-code-mother"
    
    # 检查start.sh脚本是否存在
    if [ ! -f "./start.sh" ]; then
        echo -e "${RED}错误: start.sh脚本不存在，请先运行部署脚本${NC}"
        return 1
    fi
    
    # 确保start.sh有执行权限
    chmod +x ./start.sh
    
    # 启动新应用
    echo -e "${GREEN}启动应用...${NC}"
    nohup ./start.sh > app.log 2>&1 &
    
    # 等待应用启动
    sleep 3
    
    # 检查应用是否成功启动
    if pgrep -f "ai-code-mother" > /dev/null; then
        echo -e "${GREEN}应用已成功重启${NC}"
        echo -e "查看日志: tail -f /opt/1panel/apps/openresty/openresty/www/sites/ai-code-mother/app.log"
    else
        echo -e "${RED}应用启动失败，请查看日志文件${NC}"
        echo -e "查看错误日志: tail -20 /opt/1panel/apps/openresty/openresty/www/sites/ai-code-mother/app.log"
    fi
}

# 显示帮助信息
show_help() {
    echo -e "${BLUE}=== AI Code Mother 配置管理工具 ===${NC}"
    echo "用法: $0 [命令] [参数]"
    echo ""
    echo "命令:"
    echo "  show                显示当前配置（脱敏敏感信息）"
    echo "  update KEY VALUE   更新或添加配置项"
    echo "  restart            重启应用"
    echo "  help               显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 show"
    echo "  $0 update AI_API_KEY sk-your-new-api-key"
    echo "  $0 restart"
    echo ""
    echo "配置文件位置: $CONFIG_FILE"
}

# 主程序
case "$1" in
    "show")
        show_config
        ;;
    "update")
        if [ $# -ne 3 ]; then
            echo -e "${RED}错误: 需要提供键和值参数${NC}"
            echo "用法: $0 update KEY VALUE"
            exit 1
        fi
        update_config "$2" "$3"
        ;;
    "restart")
        restart_app
        ;;
    "help"|"")
        show_help
        ;;
    *)
        echo -e "${RED}错误: 未知命令: $1${NC}"
        show_help
        exit 1
        ;;
esac