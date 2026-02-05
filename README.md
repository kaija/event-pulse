# Flink Event Trigger Framework

基於 Apache Flink 的即時事件處理框架，用於處理使用者追蹤事件的觸發機制。

## 專案概述

本系統從 Kafka 接收使用者事件，透過動態過濾條件篩選事件，並執行相應的動作（如 Webhook 呼叫或除錯輸出）。系統使用 Flink 的 Keyed State 和 TTL 機制管理使用者狀態，並在使用者閒置後自動清理。

## 核心功能

- **Kafka 事件接收**: 從 Kafka 主題消費使用者追蹤事件
- **使用者資料初始化**: 首次事件時自動載入使用者資料和歷史
- **動態事件過濾**: 使用 AviatorScript 實現靈活的過濾邏輯
- **事件動作執行**: 支援 Webhook 和 Debug Print 動作
- **狀態管理**: 使用 Flink Keyed State 和 TTL 自動管理使用者狀態
- **自動清理**: 閒置 10 分鐘後自動清理使用者狀態

## 技術棧

- **Apache Flink 1.20.0**: 串流處理引擎
- **Apache Kafka 3.8.0**: 訊息佇列
- **Jackson 2.17.0**: JSON 序列化/反序列化
- **AviatorScript 5.4.3**: 動態腳本引擎
- **RocksDB**: Flink State 後端
- **JUnit 5 & jqwik**: 單元測試和屬性測試

## 專案結構

```
flink-event-trigger/
├── src/
│   ├── main/
│   │   ├── java/com/example/flink/
│   │   │   ├── model/              # 資料模型
│   │   │   │   ├── UserEvent.java
│   │   │   │   ├── EventParameters.java
│   │   │   │   ├── UserProfile.java
│   │   │   │   ├── Visit.java
│   │   │   │   ├── EventHistory.java
│   │   │   │   ├── UserCheckpoint.java
│   │   │   │   └── EnrichedEvent.java
│   │   │   └── config/             # 配置類別
│   │   │       ├── AppConfig.java
│   │   │       ├── KafkaConfig.java
│   │   │       ├── ProfileApiConfig.java
│   │   │       ├── FilterConfig.java
│   │   │       ├── ActionsConfig.java
│   │   │       ├── FlinkConfig.java
│   │   │       └── ConfigLoader.java
│   │   └── resources/
│   │       ├── application.yml     # 應用程式配置
│   │       └── log4j2.xml         # 日誌配置
│   └── test/
│       └── java/com/example/flink/
│           ├── config/
│           │   └── ConfigLoaderTest.java
│           └── model/
│               └── UserEventTest.java
├── pom.xml                         # Maven 配置
└── README.md
```

## 資料模型

### UserEvent
使用者追蹤事件，包含事件 ID、使用者 ID、事件類型、時間戳記等資訊。

### EventParameters
事件參數，包含自訂參數和 UTM 追蹤資訊。

### UserProfile
使用者資料，包含國家、城市、語言、時區等資訊。

### Visit
使用者訪問資訊，包含裝置、瀏覽器、來源等資訊。

### EventHistory
歷史事件記錄。

### UserCheckpoint
使用者檢查點，儲存使用者資料、訪問資訊和事件歷史，由 Flink Keyed State 管理。

### EnrichedEvent
豐富化事件，包含原始事件和使用者相關資料。

## 配置說明

配置檔案位於 `src/main/resources/application.yml`：

```yaml
# Kafka 配置
kafka:
  bootstrap-servers: localhost:9092
  topic: user-tracking-events
  consumer-group-id: flink-event-trigger

# Profile API 配置
profile-api:
  base-url: http://localhost:8080
  timeout-ms: 5000
  retry-attempts: 3

# 過濾器配置
filter:
  script-path: ./scripts/filter.av

# 動作配置
actions:
  webhook:
    url: http://localhost:8080/webhook
    timeout-ms: 3000
  debug:
    enabled: true

# Flink 配置
flink:
  parallelism: 4
  checkpoint-interval-ms: 60000
  state-backend: rocksdb
  state-ttl-minutes: 10
```

## 快速開始

### 前置需求

- **Java 11** 或更高版本
- **Maven 3.6** 或更高版本
- **Docker** 和 **Docker Compose**

### 步驟 0: 啟動 Mock Profile API Server（可選，用於測試）

為了測試系統，您可以啟動內建的 Mock Profile API Server，它會模擬外部 Profile API 並返回範例資料：

```bash
# Linux/Mac
./start-mock-api.sh

# Windows
start-mock-api.bat

# 或使用自訂連接埠
./start-mock-api.sh 9090
```

Mock API Server 提供三個端點：
- `GET /users/{userId}/profile` - 使用者資料
- `GET /users/{userId}/visit` - 訪問資訊
- `GET /users/{userId}/history` - 事件歷史

詳細說明請參考 [`MOCK_API.md`](MOCK_API.md)。

**注意：** 如果您有真實的 Profile API，請在 `application.yml` 中配置正確的 URL。

### 步驟 1: 啟動 Docker Compose 環境

使用提供的啟動腳本快速啟動所有服務（Kafka、Flink JobManager、Flink TaskManager）：

```bash
./start.sh
```

此腳本會：
- 檢查 Docker 和 Docker Compose 是否已安裝
- 清理舊的容器和卷
- 啟動所有服務
- 顯示服務狀態

**服務資訊：**
- **Kafka**: `localhost:9092`
- **Flink JobManager UI**: `http://localhost:8081`

### 步驟 2: 建立 Kafka 主題（可選）

如果 Kafka 的自動建立主題功能未啟用，或您想自訂主題配置，可以執行：

```bash
./create-topic.sh
```

此腳本會：
- 檢查 Kafka 是否準備就緒
- 建立 `user-tracking-events` 主題（4 個分區，複製因子為 1）
- 顯示主題詳細資訊

**自訂主題配置：**

```bash
# 自訂主題名稱、分區數和複製因子
TOPIC_NAME=my-events PARTITIONS=8 REPLICATION_FACTOR=1 ./create-topic.sh
```

### 步驟 3: 編譯專案

```bash
mvn clean package
```

打包後的 JAR 檔案位於 `target/flink-event-trigger-1.0-SNAPSHOT.jar`。

### 步驟 4: 提交 Flink 作業

將編譯好的 JAR 檔案提交到 Flink 叢集：

```bash
# 複製 JAR 到 Flink JobManager 容器
docker cp target/flink-event-trigger-1.0-SNAPSHOT.jar flink-jobmanager:/opt/flink/usrlib/

# 提交作業
docker exec flink-jobmanager flink run /opt/flink/usrlib/flink-event-trigger-1.0-SNAPSHOT.jar
```

### 步驟 5: 發送測試事件

使用內建的 Kafka 事件生產者工具發送測試事件：

```bash
# 發送單一測試事件
./send-test-event.sh test-data/sample-event-1.json

# 發送所有測試事件（每個事件間隔 1 秒）
./send-test-event.sh test-data/ 1000
```

**測試事件說明：**
- `sample-event-1.json`: 頁面瀏覽事件（使用者首次訪問）
- `sample-event-2.json`: 按鈕點擊事件（加入購物車）
- `sample-event-3.json`: 行動裝置頁面瀏覽
- `sample-event-4.json`: 表單提交事件
- `sample-event-purchase.json`: 購買完成事件

詳細的測試事件說明請參考 [`test-data/README.md`](test-data/README.md)。

**完整測試指南：**

更多測試場景和使用方式請參考 [`TESTING.md`](TESTING.md)，包含：
- 詳細的工具使用說明
- 多種測試場景範例
- 監控和除錯技巧
- 故障排除指南

### 步驟 6: 驗證系統運作

**查看 Flink UI：**

開啟瀏覽器訪問 `http://localhost:8081`，您應該能看到正在運行的 Flink 作業。

**查看事件處理日誌：**

```bash
# 查看 TaskManager 日誌（包含事件處理輸出）
docker-compose logs -f flink-taskmanager

# 查看 Debug Print 輸出
docker-compose logs flink-taskmanager | grep "DEBUG EVENT"
```

**消費 Kafka 訊息：**

```bash
# 查看 Kafka 中的事件
docker exec -it kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic user-tracking-events \
  --from-beginning
```

## 建置專案

### 編譯

```bash
mvn clean compile
```

### 執行測試

```bash
# 執行所有測試
mvn test

# 執行特定測試類別
mvn test -Dtest=UserEventDeserializerTest

# 執行屬性測試（jqwik）
mvn test -Dtest=*PropertyTest
```

### 打包

```bash
mvn clean package
```

打包後的 JAR 檔案位於 `target/flink-event-trigger-1.0-SNAPSHOT.jar`。

## 配置說明

### 應用程式配置

配置檔案位於 `src/main/resources/application.yml`：

```yaml
# Kafka 配置
kafka:
  bootstrap-servers: localhost:9092      # Kafka 連接位址
  topic: user-tracking-events            # 主題名稱
  consumer-group-id: flink-event-trigger # 消費者群組 ID

# Profile API 配置
profile-api:
  base-url: http://localhost:8080        # API 基礎 URL
  timeout-ms: 5000                       # 請求逾時（毫秒）
  retry-attempts: 3                      # 重試次數

# 過濾器配置
filter:
  script-path: ./scripts/filter.av       # AviatorScript 腳本路徑

# 動作配置
actions:
  webhook:
    url: http://localhost:8080/webhook   # Webhook URL
    timeout-ms: 3000                     # 請求逾時（毫秒）
  debug:
    enabled: true                        # 啟用除錯輸出

# Flink 配置
flink:
  parallelism: 4                         # 並行度
  checkpoint-interval-ms: 60000          # Checkpoint 間隔（毫秒）
  state-backend: rocksdb                 # State 後端
  state-ttl-minutes: 10                  # State TTL（分鐘）
```

### 過濾腳本

過濾腳本使用 AviatorScript 語言，位於 `scripts/filter.av`。

**可用變數：**
- `event`: 當前事件物件（UserEvent）
- `user`: 使用者資料（UserProfile）
- `visit`: 訪問資訊（Visit）
- `history`: 事件歷史列表（List<EventHistory>）

**範例腳本：**

```javascript
// 過濾條件：事件類型為 "pageview" 且使用者來自台灣
event.eventType == "pageview" && user.country == "TW"
```

**更多範例：**

詳細的過濾腳本範例和說明請參考 [`scripts/FILTER_EXAMPLES.md`](scripts/FILTER_EXAMPLES.md)，包含：
- 30+ 個實用的過濾範例
- 所有可用變數的完整欄位說明
- AviatorScript 內建函數說明
- 如何測試和修改過濾腳本

## 常見操作

### 停止服務

```bash
# 停止所有服務
docker-compose down

# 停止並刪除卷（清除所有資料）
docker-compose down -v
```

### 重啟服務

```bash
# 重啟所有服務
docker-compose restart

# 重啟特定服務
docker-compose restart kafka
```

### 查看 Kafka 主題

```bash
# 列出所有主題
docker exec kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list

# 查看主題詳細資訊
docker exec kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic user-tracking-events
```

### 消費 Kafka 訊息

```bash
# 從頭開始消費訊息
docker exec -it kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic user-tracking-events \
  --from-beginning

# 消費最新訊息
docker exec -it kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic user-tracking-events
```

### 取消 Flink 作業

```bash
# 列出所有作業
docker exec flink-jobmanager flink list

# 取消特定作業（使用作業 ID）
docker exec flink-jobmanager flink cancel <job-id>
```

## 故障排除

### Kafka 無法啟動

**問題：** Kafka 容器啟動失敗或無法連接。

**解決方案：**
1. 檢查 Docker 日誌：`docker-compose logs kafka`
2. 確保連接埠 9092 和 9093 未被佔用
3. 清除舊的卷並重新啟動：`docker-compose down -v && ./start.sh`

### Flink 作業提交失敗

**問題：** 無法提交 Flink 作業。

**解決方案：**
1. 確保 Flink JobManager 正在運行：`docker ps | grep flink-jobmanager`
2. 檢查 JAR 檔案是否存在：`ls -lh target/flink-event-trigger-1.0-SNAPSHOT.jar`
3. 查看 Flink 日誌：`docker-compose logs flink-jobmanager`

### 事件未被處理

**問題：** 發送到 Kafka 的事件未被 Flink 處理。

**解決方案：**
1. 確認 Flink 作業正在運行：訪問 `http://localhost:8081`
2. 檢查主題名稱是否正確：`application.yml` 中的 `kafka.topic`
3. 查看 Flink TaskManager 日誌：`docker-compose logs flink-taskmanager`
4. 驗證事件 JSON 格式是否正確

### 過濾腳本錯誤

**問題：** AviatorScript 過濾腳本執行失敗。

**解決方案：**
1. 檢查腳本語法是否正確
2. 確認腳本檔案路徑：`filter.script-path` 在 `application.yml` 中
3. 查看 Flink 日誌中的錯誤訊息

## 開發狀態

### 已完成

- ✅ Maven 專案結構建立
- ✅ 所有資料模型類別定義
- ✅ 配置類別和配置載入器
- ✅ Kafka 事件來源和反序列化
- ✅ Profile API 客戶端
- ✅ Mock Profile API Server（測試工具）
- ✅ 使用者狀態管理（Flink Keyed State with TTL）
- ✅ 事件過濾功能（AviatorScript）
- ✅ 事件動作處理器（Webhook 和 Debug Print）
- ✅ 主要 Flink 應用程式
- ✅ Docker Compose 環境
- ✅ 啟動腳本和文件

### 待實作

- ⏳ 屬性測試（Property-Based Testing）
- ⏳ 整合測試
- ⏳ 效能調優

## 架構圖

```
┌─────────────┐
│   Kafka     │
│   Topic     │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────┐
│         Flink Streaming Job             │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │  Kafka Source                    │  │
│  │  (UserEventDeserializer)         │  │
│  └────────────┬─────────────────────┘  │
│               │                         │
│               ▼                         │
│  ┌──────────────────────────────────┐  │
│  │  KeyBy (userId)                  │  │
│  └────────────┬─────────────────────┘  │
│               │                         │
│               ▼                         │
│  ┌──────────────────────────────────┐  │
│  │  UserStateManager                │  │
│  │  (Keyed State + TTL)             │  │
│  │  - Load/Create Checkpoint        │  │
│  │  - Call Profile API              │  │
│  │  - Enrich Event                  │  │
│  └────────────┬─────────────────────┘  │
│               │                         │
│               ▼                         │
│  ┌──────────────────────────────────┐  │
│  │  EventFilterFunction             │  │
│  │  (AviatorScript)                 │  │
│  └────────────┬─────────────────────┘  │
│               │                         │
│               ▼                         │
│  ┌──────────────────────────────────┐  │
│  │  ActionHandler                   │  │
│  │  - WebhookActionHandler          │  │
│  │  - DebugPrintActionHandler       │  │
│  └──────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

## 貢獻

歡迎提交 Issue 和 Pull Request！

## 授權

本專案僅供學習和開發使用。
