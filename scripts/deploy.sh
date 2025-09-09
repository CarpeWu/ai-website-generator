#!/bin/bash

# AI Code Mother 部署脚本
# 适用于1panel面板的Ubuntu系统

set -e

# 配置目录
CONFIG_DIR="/opt/ai-code-mother/config"
APP_DIR="/opt/ai-code-mother"
LOG_DIR="/opt/ai-code-mother/logs"

# 创建目录
mkdir -p $CONFIG_DIR $APP_DIR $LOG_DIR

echo "=== AI Code Mother 部署脚本 ==="

# 检查是否已有配置文件
if [ -f "$CONFIG_DIR/.env" ]; then
    echo "发现现有配置文件，将进行备份..."
    cp $CONFIG_DIR/.env $CONFIG_DIR/.env.backup.$(date +%Y%m%d%H%M%S)
fi

# 复制配置文件模板
if [ ! -f "$CONFIG_DIR/.env" ]; then
    echo "创建新的配置文件..."
    cp config/.env.template $CONFIG_DIR/.env
    echo "请编辑 $CONFIG_DIR/.env 文件，填写您的配置信息"
    echo "然后重新运行此脚本完成部署"
    exit 0
fi

# 读取配置文件
source $CONFIG_DIR/.env

echo "正在部署 AI Code Mother..."

# 停止现有服务
echo "停止现有服务..."
pkill -f "ai-code-mother" || true

# 等待进程停止
sleep 3

# 备份旧版本
if [ -f "$APP_DIR/ai-code-mother.jar" ]; then
    echo "备份旧版本..."
    mv $APP_DIR/ai-code-mother.jar $APP_DIR/ai-code-mother.jar.backup.$(date +%Y%m%d%H%M%S)
fi

# 复制新的jar包（假设jar包在当前目录）
if [ -f "target/ai-code-mother-0.0.1-SNAPSHOT.jar" ]; then
    echo "复制新的应用程序..."
    cp target/ai-code-mother-0.0.1-SNAPSHOT.jar $APP_DIR/ai-code-mother.jar
fi

# 创建启动脚本
cat > $APP_DIR/start.sh << 'EOF'
#!/bin/bash

# 加载环境变量
source /opt/ai-code-mother/config/.env

# 设置Java选项
export JAVA_OPTS="-Xmx2g -Xms1g -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"

# 启动应用
cd /opt/ai-code-mother
java $JAVA_OPTS -jar ai-code-mother.jar
EOF

chmod +x $APP_DIR/start.sh

# 创建systemd服务文件
if [ ! -f "/etc/systemd/system/ai-code-mother.service" ]; then
    echo "创建systemd服务..."
    sudo cat > /tmp/ai-code-mother.service << EOF
[Unit]
Description=AI Code Mother Backend Service
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/ai-code-mother
EnvironmentFile=/opt/ai-code-mother/config/.env
ExecStart=/bin/bash /opt/ai-code-mother/start.sh
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

    sudo mv /tmp/ai-code-mother.service /etc/systemd/system/
    sudo systemctl daemon-reload
fi

# 启动服务
echo "启动服务..."
sudo systemctl enable ai-code-mother.service
sudo systemctl restart ai-code-mother.service

echo "=== 部署完成 ==="
echo "应用日志: $LOG_DIR"
echo "配置文件: $CONFIG_DIR/.env"
echo "服务状态: sudo systemctl status ai-code-mother.service"
echo "查看日志: sudo journalctl -u ai-code-mother.service -f"