#!/bin/bash

SERVER_HOST=""
SERVER_USER=""
DEPLOY_PACKAGE="rag-service-deploy.tar.gz"

if [ $# -ne 2 ]; then
    echo "使用方法: $0 <服务器IP> <用户名>"
    echo "示例: $0 192.168.1.100 root"
    exit 1
fi

SERVER_HOST=$1
SERVER_USER=$2

echo "🚀 一键部署到服务器: $SERVER_HOST"

if [ ! -f "$DEPLOY_PACKAGE" ]; then
    echo "❌ 部署包不存在，请先运行 ./build.sh"
    exit 1
fi

echo "📦 上传部署包..."
scp "$DEPLOY_PACKAGE" "$SERVER_USER@$SERVER_HOST:/tmp/" || exit 1

echo "🔧 远程部署..."
ssh "$SERVER_USER@$SERVER_HOST" << 'ENDSSH'
cd /tmp
tar -xzf rag-service-deploy.tar.gz
chmod +x server-deploy.sh
./server-deploy.sh
ENDSSH

if [ $? -eq 0 ]; then
    echo "🎉 部署成功!"
    echo "🌐 访问地址: http://$SERVER_HOST:8080/api/health"
else
    echo "❌ 部署失败"
    exit 1
fi