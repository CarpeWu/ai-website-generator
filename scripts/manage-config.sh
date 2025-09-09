#!/bin/bash

# AI Code Mother 配置管理脚本
# 用于在1panel面板中管理配置

CONFIG_DIR="/opt/ai-code-mother/config"
CONFIG_FILE="$CONFIG_DIR/.env"

# 检查配置文件是否存在
if [ ! -f "$CONFIG_FILE" ]; then
    echo "配置文件不存在，正在创建..."
    mkdir -p $CONFIG_DIR
    cp config/.env.template $CONFIG_FILE
    echo "请编辑 $CONFIG_FILE 文件，然后重新运行此脚本"
    exit 1
fi

# 显示当前配置
show_config() {
    echo "=== 当前配置 ==="
    grep -E "^(#|$)" $CONFIG_FILE | grep -v "^#" | while read line; do
        if [[ ! -z "$line" ]]; then
            key=$(echo $line | cut -d'=' -f1)
            value=$(echo $line | cut -d'=' -f2-)
            # 隐藏敏感信息的值
            if [[ $key == *"PASSWORD"* ]] || [[ $key == *"SECRET"* ]] || [[ $key == *"API_KEY"* ]]; then
                if [ ${#value} -gt 4 ]; then
                    masked_value="${value:0:2}****${value: -2}"
                else
                    masked_value="****"
                fi
                echo "$key=$masked_value"
            else
                echo "$line"
            fi
        fi
    done
}

# 更新配置
update_config() {
    key=$1
    value=$2
    
    # 检查key是否存在
    if grep -q "^$key=" $CONFIG_FILE; then
        # 更新现有配置
        sed -i "s|^$key=.*|$key=$value|" $CONFIG_FILE
        echo "已更新: $key"
    else
        # 添加新配置
        echo "$key=$value" >> $CONFIG_FILE
        echo "已添加: $key"
    fi
}

# 重启服务
restart_service() {
    echo "重启服务..."
    sudo systemctl restart ai-code-mother.service
    echo "服务已重启"
}

# 显示帮助
show_help() {
    echo "使用方法: $0 [选项]"
    echo "选项:"
    echo "  show                显示当前配置"
    echo "  update KEY VALUE   更新配置项"
    echo "  restart            重启服务"
    echo "  help               显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 show"
    echo "  $0 update AI_API_KEY sk-your-new-key"
    echo "  $0 restart"
}

# 主逻辑
case $1 in
    "show")
        show_config
        ;;
    "update")
        if [ $# -ne 3 ]; then
            echo "错误: 需要提供key和value参数"
            echo "用法: $0 update KEY VALUE"
            exit 1
        fi
        update_config "$2" "$3"
        restart_service
        ;;
    "restart")
        restart_service
        ;;
    "help"|"")
        show_help
        ;;
    *)
        echo "错误: 未知选项 '$1'"
        show_help
        exit 1
        ;;
esac