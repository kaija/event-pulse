#!/bin/bash

# Flink Event Trigger Framework - 啟動腳本
# 此腳本用於啟動 Docker Compose 環境

set -e

echo "=========================================="
echo "Flink Event Trigger Framework"
echo "啟動 Docker Compose 環境"
echo "=========================================="
echo ""

# 檢查 Docker 是否安裝
if ! command -v docker &> /dev/null; then
    echo "❌ 錯誤: Docker 未安裝"
    echo "請先安裝 Docker: https://docs.docker.com/get-docker/"
    exit 1
fi

# 檢查 Docker Compose 是否安裝
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "❌ 錯誤: Docker Compose 未安裝"
    echo "請先安裝 Docker Compose: https://docs.docker.com/compose/install/"
    exit 1
fi

# 檢查 docker-compose.yml 是否存在
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ 錯誤: docker-compose.yml 檔案不存在"
    exit 1
fi

echo "✅ Docker 和 Docker Compose 已安裝"
echo ""

# 停止並移除舊的容器（如果存在）
echo "🧹 清理舊的容器..."
docker-compose down -v 2>/dev/null || docker compose down -v 2>/dev/null || true
echo ""

# 啟動 Docker Compose
echo "🚀 啟動 Docker Compose 服務..."
if command -v docker-compose &> /dev/null; then
    docker-compose up -d
else
    docker compose up -d
fi

echo ""
echo "⏳ 等待服務啟動..."
sleep 5

# 檢查服務狀態
echo ""
echo "📊 檢查服務狀態..."
if command -v docker-compose &> /dev/null; then
    docker-compose ps
else
    docker compose ps
fi

echo ""
echo "=========================================="
echo "✅ Docker Compose 環境啟動完成！"
echo "=========================================="
echo ""
echo "服務資訊："
echo "  - Kafka: localhost:9092"
echo "  - Flink JobManager UI: http://localhost:8081"
echo ""
echo "下一步："
echo "  1. 等待約 30 秒讓 Kafka 完全啟動"
echo "  2. 執行 ./create-topic.sh 建立 Kafka 主題（如需要）"
echo "  3. 編譯並提交 Flink 作業"
echo ""
echo "查看日誌："
if command -v docker-compose &> /dev/null; then
    echo "  docker-compose logs -f [service-name]"
else
    echo "  docker compose logs -f [service-name]"
fi
echo ""
echo "停止服務："
if command -v docker-compose &> /dev/null; then
    echo "  docker-compose down"
else
    echo "  docker compose down"
fi
echo ""
