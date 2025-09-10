#!/bin/bash

# 1Panel OpenResty 环境部署脚本
# 适用于部署到 /opt/1panel/apps/openresty/openresty/www/sites/ai-code-mother/

set -e

# 配置变量
TARGET_DIR="/opt/1panel/apps/openresty/openresty/www/sites/ai-code-mother"
JAR_FILE="ai-code-mother-0.0.1-SNAPSHOT.jar"
CONFIG_FILE=".env"
START_SCRIPT="start.sh"

echo "=== AI Code Mother 1Panel 部署脚本 ==="

# 检查目标目录是否存在
if [ ! -d "$TARGET_DIR" ]; then
    echo "错误: 目标目录不存在: $TARGET_DIR"
    echo "请确保1panel OpenResty已正确安装，并且ai-code-mother网站已创建"
    exit 1
fi

cd "$TARGET_DIR"

# 备份现有文件
echo "备份现有文件..."
if [ -f "$JAR_FILE" ]; then
    backup_jar="${JAR_FILE}.backup.$(date +%Y%m%d_%H%M%S)"
    cp "$JAR_FILE" "$backup_jar"
    echo "已备份: $backup_jar"
fi

if [ -f "$CONFIG_FILE" ]; then
    backup_env="${CONFIG_FILE}.backup.$(date +%Y%m%d_%H%M%S)"
    cp "$CONFIG_FILE" "$backup_env"
    echo "已备份: $backup_env"
fi

# 停止正在运行的应用
echo "停止正在运行的应用..."
pkill -f "ai-code-mother" || true
sleep 2

# 检查是否还有进程在运行
if pgrep -f "ai-code-mother" > /dev/null; then
    echo "警告: 仍有ai-code-mother进程在运行，强制停止..."
    pkill -9 -f "ai-code-mother"
fi

# 等待用户上传新文件
echo ""
echo "请将以下文件上传到 $TARGET_DIR 目录:"
echo "1. 新的JAR文件: $JAR_FILE"
echo "2. 配置文件模板: .env.template (可选)"
echo ""
read -p "按回车键继续，确认文件已上传..."

# 检查新JAR文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo "错误: 未找到JAR文件: $JAR_FILE"
    echo "请将新的JAR文件上传到 $TARGET_DIR 目录"
    exit 1
fi

# 创建或更新配置文件
if [ ! -f "$CONFIG_FILE" ] && [ -f ".env.template" ]; then
    echo "创建配置文件..."
    cp .env.template "$CONFIG_FILE"
    echo "请编辑 $CONFIG_FILE 文件配置您的环境变量"
elif [ -f "$CONFIG_FILE" ]; then
    echo "使用现有的配置文件: $CONFIG_FILE"
else
    echo "警告: 未找到配置文件，请手动创建 $CONFIG_FILE"
fi

# 创建启动脚本
echo "创建启动脚本..."
cat > "$START_SCRIPT" << 'EOF'
#!/bin/bash
cd "/opt/1panel/apps/openresty/openresty/www/sites/ai-code-mother"
java -jar ai-code-mother-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
EOF

chmod +x "$START_SCRIPT"

# 设置配置文件权限
if [ -f "$CONFIG_FILE" ]; then
    chmod 600 "$CONFIG_FILE"
fi

echo ""
echo "=== 部署完成 ==="
echo "目录: $TARGET_DIR"
echo "文件:"
ls -la "$JAR_FILE" "$CONFIG_FILE" "$START_SCRIPT" 2>/dev/null || true

echo ""
echo "下一步操作:"
echo "1. 编辑配置文件: nano $TARGET_DIR/$CONFIG_FILE"
echo "2. 启动应用: cd $TARGET_DIR && nohup ./start.sh > app.log 2>&1 &"
echo "3. 查看日志: tail -f $TARGET_DIR/app.log"
echo "4. 配置1panel网站反向代理（如需要）"

echo ""
echo "快速启动命令:"
echo "  cd $TARGET_DIR && nohup ./start.sh > app.log 2>&1 & && tail -f app.log"