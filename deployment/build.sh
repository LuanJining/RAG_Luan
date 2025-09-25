#!/bin/bash

# RAG Service 本地打包脚本

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}🚀 RAG Service 本地打包脚本${NC}"
echo -e "${BLUE}=========================================${NC}"

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_tools() {
    log_info "检查构建工具..."

    if ! command -v java &> /dev/null; then
        log_error "Java未安装或不在PATH中"
        exit 1
    fi

    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装或不在PATH中"
        exit 1
    fi

    log_info "Java版本: $(java -version 2>&1 | head -n 1)"
    log_info "Maven版本: $(mvn --version | head -n 1)"
}

clean_build() {
    log_info "清理旧的构建文件..."
    cd ..
    mvn clean
    rm -rf deploy/
    mkdir -p deploy/scripts deploy/config deploy/logs
    cd deployment
}

build_jar() {
    log_info "开始编译打包..."

    cd ..
    mvn package -DskipTests -Dmaven.test.skip=true

    if [ $? -eq 0 ]; then
        log_info "项目编译成功 ✅"
    else
        log_error "项目编译失败 ❌"
        exit 1
    fi

    cd deployment
}

prepare_deployment() {
    log_info "准备部署文件..."

    # 复制JAR文件
    cp ../target/*.jar ../deploy/ 2>/dev/null || true

    # 复制生产配置
    cp ../src/main/resources/application-prod.yml ../deploy/config/

    # 创建启动脚本
    cat > ../deploy/scripts/start.sh << 'EOF'
#!/bin/bash

APP_NAME="rag-service"
APP_HOME="/opt/rag-service"
JAR_FILE="$APP_HOME/RAG-0.0.1-SNAPSHOT.jar"
CONFIG_FILE="$APP_HOME/config/application-prod.yml"
PID_FILE="$APP_HOME/$APP_NAME.pid"
LOG_FILE="$APP_HOME/logs/startup.log"

mkdir -p "$APP_HOME/logs"

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

if ! command -v java &> /dev/null; then
    log_error "Java未安装或不在PATH中"
    exit 1
fi

if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null 2>&1; then
        log_error "$APP_NAME 已经在运行中 (PID: $PID)"
        exit 1
    else
        rm -f "$PID_FILE"
    fi
fi

if [ ! -f "$JAR_FILE" ]; then
    log_error "JAR文件不存在: $JAR_FILE"
    exit 1
fi

JVM_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC"
JVM_OPTS="$JVM_OPTS -Dspring.config.location=file:$CONFIG_FILE"
JVM_OPTS="$JVM_OPTS -Dspring.profiles.active=prod"

log_info "启动 $APP_NAME..."
log_info "JAR文件: $JAR_FILE"
log_info "配置文件: $CONFIG_FILE"

nohup java $JVM_OPTS -jar "$JAR_FILE" > "$APP_HOME/logs/console.log" 2>&1 &

echo $! > "$PID_FILE"

sleep 5

PID=$(cat "$PID_FILE")
if ps -p $PID > /dev/null 2>&1; then
    log_info "$APP_NAME 启动成功 ✅"
    log_info "PID: $PID"
    log_info "访问地址: http://localhost:8080/api/health"
else
    log_error "$APP_NAME 启动失败 ❌"
    rm -f "$PID_FILE"
    exit 1
fi
EOF

    # 创建停止脚本
    cat > ../deploy/scripts/stop.sh << 'EOF'
#!/bin/bash

APP_NAME="rag-service"
APP_HOME="/opt/rag-service"
PID_FILE="$APP_HOME/$APP_NAME.pid"
LOG_FILE="$APP_HOME/logs/startup.log"

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

if [ ! -f "$PID_FILE" ]; then
    log_info "$APP_NAME 未运行"
    exit 0
fi

PID=$(cat "$PID_FILE")

if ! ps -p $PID > /dev/null 2>&1; then
    log_info "$APP_NAME 进程不存在，清理PID文件"
    rm -f "$PID_FILE"
    exit 0
fi

log_info "正在停止 $APP_NAME (PID: $PID)..."

kill $PID

for i in {1..30}; do
    if ! ps -p $PID > /dev/null 2>&1; then
        log_info "$APP_NAME 已停止 ✅"
        rm -f "$PID_FILE"
        exit 0
    fi
    sleep 1
done

log_info "强制终止进程..."
kill -9 $PID

if ! ps -p $PID > /dev/null 2>&1; then
    log_info "$APP_NAME 已强制停止 ✅"
    rm -f "$PID_FILE"
else
    log_info "$APP_NAME 停止失败 ❌"
    exit 1
fi
EOF

    # 创建重启脚本
    cat > ../deploy/scripts/restart.sh << 'EOF'
#!/bin/bash

APP_HOME="/opt/rag-service"

echo "重启 RAG Service..."

$APP_HOME/scripts/stop.sh
sleep 2
$APP_HOME/scripts/start.sh
EOF

    # 创建状态检查脚本
    cat > ../deploy/scripts/status.sh << 'EOF'
#!/bin/bash

APP_NAME="rag-service"
APP_HOME="/opt/rag-service"
PID_FILE="$APP_HOME/$APP_NAME.pid"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}📊 RAG Service 状态检查${NC}"
echo -e "${BLUE}=========================================${NC}"

if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    echo -e "PID文件: ${GREEN}存在${NC} (PID: $PID)"

    if ps -p $PID > /dev/null 2>&1; then
        echo -e "进程状态: ${GREEN}运行中${NC}"

        echo -e "\n${YELLOW}进程详情:${NC}"
        ps -p $PID -o pid,ppid,user,%cpu,%mem,start,command

        echo -e "\n${YELLOW}端口状态:${NC}"
        netstat -tlnp 2>/dev/null | grep :8080 || ss -tlnp | grep :8080

        echo -e "\n${YELLOW}健康检查:${NC}"
        if command -v curl &> /dev/null; then
            HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/health)
            if [ "$HTTP_CODE" = "200" ]; then
                echo -e "服务健康: ${GREEN}正常${NC} (HTTP 200)"
            else
                echo -e "服务健康: ${RED}异常${NC} (HTTP $HTTP_CODE)"
            fi
        else
            echo "curl未安装，跳过健康检查"
        fi

    else
        echo -e "进程状态: ${RED}未运行${NC}"
    fi
else
    echo -e "PID文件: ${RED}不存在${NC}"
    echo -e "进程状态: ${RED}未运行${NC}"
fi

echo -e "\n${BLUE}=========================================${NC}"
EOF

    # 创建服务器部署脚本
    cat > ../deploy/server-deploy.sh << 'EOF'
#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

APP_NAME="rag-service"
APP_HOME="/opt/rag-service"
SERVICE_USER="rag"

log_info() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

if [ "$EUID" -ne 0 ]; then
    log_error "请使用sudo运行此脚本"
    exit 1
fi

log_info "检查Java环境..."
if ! command -v java &> /dev/null; then
    log_info "安装Java..."
    if command -v yum &> /dev/null; then
        yum update -y && yum install -y java-17-openjdk
    elif command -v apt &> /dev/null; then
        apt update && apt install -y openjdk-17-jdk
    else
        log_error "请手动安装Java 17"
        exit 1
    fi
fi

if ! id "$SERVICE_USER" &>/dev/null; then
    log_info "创建服务用户: $SERVICE_USER"
    useradd -r -s /bin/false -d "$APP_HOME" "$SERVICE_USER"
fi

if [ -f "$APP_HOME/scripts/stop.sh" ]; then
    log_info "停止现有服务..."
    su - $SERVICE_USER -s /bin/bash -c "$APP_HOME/scripts/stop.sh" || true
fi

log_info "安装应用文件..."
mkdir -p "$APP_HOME"/{config,logs,scripts}

cp *.jar "$APP_HOME/" 2>/dev/null || log_error "未找到JAR文件"
cp -r config/* "$APP_HOME/config/" 2>/dev/null || log_error "未找到配置文件"
cp -r scripts/* "$APP_HOME/scripts/" 2>/dev/null || log_error "未找到脚本文件"

chown -R "$SERVICE_USER:$SERVICE_USER" "$APP_HOME"
chmod +x "$APP_HOME"/scripts/*.sh

log_info "创建systemd服务..."
cat > /etc/systemd/system/$APP_NAME.service << EOSERVICE
[Unit]
Description=RAG Knowledge Base Service
After=network.target

[Service]
Type=forking
User=$SERVICE_USER
Group=$SERVICE_USER
WorkingDirectory=$APP_HOME
ExecStart=$APP_HOME/scripts/start.sh
ExecStop=$APP_HOME/scripts/stop.sh
ExecReload=$APP_HOME/scripts/restart.sh
PIDFile=$APP_HOME/$APP_NAME.pid
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOSERVICE

systemctl daemon-reload
systemctl enable $APP_NAME

log_info "启动服务..."
systemctl start $APP_NAME
sleep 5

if systemctl is-active --quiet $APP_NAME; then
    log_info "🎉 部署成功！"
    echo ""
    echo -e "${BLUE}🌐 服务访问:${NC}"
    echo "   健康检查: http://$(hostname -I | awk '{print $1}'):8080/api/health"
    echo ""
    echo -e "${BLUE}🔧 管理命令:${NC}"
    echo "   systemctl start/stop/restart $APP_NAME"
    echo "   $APP_HOME/scripts/status.sh"
else
    log_error "服务启动失败"
    systemctl status $APP_NAME --no-pager
    exit 1
fi
EOF

    # 设置权限
    chmod +x ../deploy/scripts/*.sh
    chmod +x ../deploy/server-deploy.sh

    log_info "部署文件准备完成 ✅"
}

create_package() {
    log_info "创建部署包..."

    cd ../deploy
    tar -czf "../rag-service-deploy.tar.gz" .
    cd ../deployment

    log_info "部署包创建完成: rag-service-deploy.tar.gz"
}

main() {
    check_tools
    clean_build
    build_jar
    prepare_deployment
    create_package

    echo ""
    log_info "🎉 打包完成！"
    echo -e "${YELLOW}📦 部署包:${NC} rag-service-deploy.tar.gz"
    echo ""
    echo -e "${BLUE}🚀 服务器部署步骤:${NC}"
    echo "1. 上传: scp rag-service-deploy.tar.gz user@server:/tmp/"
    echo "2. 部署: cd /tmp && tar -xzf rag-service-deploy.tar.gz && sudo ./server-deploy.sh"
    echo "3. 验证: curl http://服务器IP:8080/api/health"
}

main "$@"