#!/bin/bash

# Flink Event Trigger Framework - Kafka 主題建立腳本
# 此腳本用於建立 Kafka 主題（如果自動建立功能未啟用）

set -e

echo "=========================================="
echo "Kafka 主題建立腳本"
echo "=========================================="
echo ""

# 預設配置
TOPIC_NAME="${TOPIC_NAME:-user-tracking-events}"
PARTITIONS="${PARTITIONS:-4}"
REPLICATION_FACTOR="${REPLICATION_FACTOR:-1}"
KAFKA_CONTAINER="${KAFKA_CONTAINER:-kafka}"
BOOTSTRAP_SERVER="${BOOTSTRAP_SERVER:-localhost:9092}"

# 檢查 Kafka 容器是否運行
if ! docker ps | grep -q "$KAFKA_CONTAINER"; then
    echo "❌ 錯誤: Kafka 容器未運行"
    echo "請先執行 ./start.sh 啟動 Docker Compose 環境"
    exit 1
fi

echo "✅ Kafka 容器正在運行"
echo ""

# 等待 Kafka 準備就緒
echo "⏳ 等待 Kafka 準備就緒..."
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if docker exec $KAFKA_CONTAINER kafka-broker-api-versions.sh --bootstrap-server localhost:9092 &> /dev/null; then
        echo "✅ Kafka 已準備就緒"
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo "等待中... ($RETRY_COUNT/$MAX_RETRIES)"
    sleep 2
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "❌ 錯誤: Kafka 啟動超時"
    exit 1
fi

echo ""

# 檢查主題是否已存在
echo "🔍 檢查主題是否已存在..."
if docker exec $KAFKA_CONTAINER kafka-topics.sh \
    --bootstrap-server localhost:9092 \
    --list | grep -q "^${TOPIC_NAME}$"; then
    echo "ℹ️  主題 '$TOPIC_NAME' 已存在"
    echo ""
    echo "主題詳細資訊："
    docker exec $KAFKA_CONTAINER kafka-topics.sh \
        --bootstrap-server localhost:9092 \
        --describe \
        --topic "$TOPIC_NAME"
    echo ""
    echo "如需重新建立主題，請先刪除："
    echo "  docker exec $KAFKA_CONTAINER kafka-topics.sh \\"
    echo "    --bootstrap-server localhost:9092 \\"
    echo "    --delete \\"
    echo "    --topic $TOPIC_NAME"
    exit 0
fi

# 建立主題
echo "📝 建立 Kafka 主題..."
echo "  主題名稱: $TOPIC_NAME"
echo "  分區數: $PARTITIONS"
echo "  複製因子: $REPLICATION_FACTOR"
echo ""

docker exec $KAFKA_CONTAINER kafka-topics.sh \
    --bootstrap-server localhost:9092 \
    --create \
    --topic "$TOPIC_NAME" \
    --partitions "$PARTITIONS" \
    --replication-factor "$REPLICATION_FACTOR"

echo ""
echo "✅ 主題建立成功！"
echo ""

# 顯示主題詳細資訊
echo "主題詳細資訊："
docker exec $KAFKA_CONTAINER kafka-topics.sh \
    --bootstrap-server localhost:9092 \
    --describe \
    --topic "$TOPIC_NAME"

echo ""
echo "=========================================="
echo "✅ Kafka 主題設定完成！"
echo "=========================================="
echo ""
echo "測試主題："
echo "  # 發送測試訊息"
echo "  docker exec -it $KAFKA_CONTAINER kafka-console-producer.sh \\"
echo "    --bootstrap-server localhost:9092 \\"
echo "    --topic $TOPIC_NAME"
echo ""
echo "  # 消費訊息"
echo "  docker exec -it $KAFKA_CONTAINER kafka-console-consumer.sh \\"
echo "    --bootstrap-server localhost:9092 \\"
echo "    --topic $TOPIC_NAME \\"
echo "    --from-beginning"
echo ""
