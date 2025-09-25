#!/bin/bash

show_usage() {
    echo "RAG Service 管理工具"
    echo ""
    echo "本地命令:"
    echo "  $0 build                          # 本地打包"
    echo "  $0 clean                          # 清理文件"
    echo ""
    echo "部署命令:"
    echo "  $0 deploy <服务器IP> <用户名>        # 部署服务"
    echo "  $0 update <服务器IP> <用户名>        # 更新服务"
    echo ""
    echo "远程管理:"
    echo "  $0 status <服务器IP> <用户名>        # 查看状态"
    echo "  $0 start <服务器IP> <用户名>         # 启动服务"
    echo "  $0 stop <服务器IP> <用户名>          # 停止服务"
    echo "  $0 restart <服务器IP> <用户名>       # 重启服务"
    echo "  $0 logs <服务器IP> <用户名>          # 查看日志"
    echo "  $0 health <服务器IP> <用户名>        # 健康检查"
}

build_local() {
    echo "🚀 开始本地构建..."
    ./build.sh
}

clean_local() {
    echo "🧹 清理构建文件..."
    rm -rf ../target/ ../deploy/ ../rag-service-deploy.tar.gz
    echo "✅ 清理完成"
}

deploy_service() {
    if [ -z "$2" ] || [ -z "$3" ]; then
        echo "❌ 错误: 部署需要指定服务器IP和用户名"
        show_usage
        exit 1
    fi

    echo "🚀 部署到服务器: $2"
    ./quick-deploy.sh "$2" "$3"
}

update_service() {
    if [ -z "$2" ] || [ -z "$3" ]; then
        echo "❌ 错误: 更新需要指定服务器IP和用户名"
        show_usage
        exit 1
    fi

    echo "🔄 更新服务器: $2"
    ./build.sh
    ./quick-deploy.sh "$2" "$3"
}

remote_command() {
    local cmd=$1
    local host=$2
    local user=$3

    if [ -z "$host" ] || [ -z "$user" ]; then
        echo "❌ 错误: 远程命令需要指定服务器IP和用户名"
        show_usage
        exit 1
    fi

    case $cmd in
        "status")
            ssh "$user@$host" "systemctl status rag-service --no-pager"
            ;;
        "start")
            ssh "$user@$host" "systemctl start rag-service"
            ;;
        "stop")
            ssh "$user@$host" "systemctl stop rag-service"
            ;;
        "restart")
            ssh "$user@$host" "systemctl restart rag-service"
            ;;
        "logs")
            ssh "$user@$host" "journalctl -u rag-service -f"
            ;;
        "health")
            echo "🩺 检查服务健康状态..."
            if curl -f -s "http://$host:8080/api/health" > /dev/null; then
                echo "✅ 服务正常"
                curl -s "http://$host:8080/api/health" | python -m json.tool 2>/dev/null || curl -s "http://$host:8080/api/health"
            else
                echo "❌ 服务异常"
                exit 1
            fi
            ;;
    esac
}

main() {
    if [ $# -eq 0 ]; then
        show_usage
        exit 1
    fi

    COMMAND=$1

    case $COMMAND in
        "build")
            build_local
            ;;
        "clean")
            clean_local
            ;;
        "deploy")
            deploy_service "$@"
            ;;
        "update")
            update_service "$@"
            ;;
        "status"|"start"|"stop"|"restart"|"logs"|"health")
            remote_command "$COMMAND" "$2" "$3"
            ;;
        *)
            echo "❌ 未知命令: $COMMAND"
            show_usage
            exit 1
            ;;
    esac
}

main "$@"