# Flink Event Trigger Framework - Architecture Documentation

## Overview

This document describes the large-scale architecture of the Flink Event Trigger Framework for analyzing user tracking events. The system is designed to handle high-throughput event processing with stateful operations, where each user's state can grow up to 16MB.

## Table of Contents

1. [High-Level Architecture](#high-level-architecture)
2. [Flink Cluster Architecture](#flink-cluster-architecture)
3. [Event Flow and Data Pipeline](#event-flow-and-data-pipeline)
4. [State Management and Storage](#state-management-and-storage)
5. [Scaling Considerations](#scaling-considerations)

---

## High-Level Architecture

```mermaid
graph TB
    subgraph "Data Sources"
        KafkaCluster[Kafka Cluster<br/>Topic: user-tracking-events<br/>Partitions: 4+]
    end
    
    subgraph "Flink Cluster"
        JM[JobManager<br/>Coordination & Scheduling]
        TM1[TaskManager 1<br/>Task Slots: 4]
        TM2[TaskManager 2<br/>Task Slots: 4]
        TM3[TaskManager 3<br/>Task Slots: 4]
        TMN[TaskManager N<br/>Task Slots: 4]
        
        JM -->|Deploy Tasks| TM1
        JM -->|Deploy Tasks| TM2
        JM -->|Deploy Tasks| TM3
        JM -->|Deploy Tasks| TMN
    end
    
    subgraph "State Storage"
        RocksDB[(RocksDB State Backend<br/>Local: SSD/NVMe<br/>Checkpoints: S3/HDFS)]
    end
    
    subgraph "External Services"
        ProfileAPI[Profile API<br/>User Data Service]
        Webhook[Webhook Endpoints<br/>Action Handlers]
    end
    
    KafkaCluster -->|Consume Events| TM1
    KafkaCluster -->|Consume Events| TM2
    KafkaCluster -->|Consume Events| TM3
    KafkaCluster -->|Consume Events| TMN
    
    TM1 <-->|Read/Write State| RocksDB
    TM2 <-->|Read/Write State| RocksDB
    TM3 <-->|Read/Write State| RocksDB
    TMN <-->|Read/Write State| RocksDB
    
    TM1 -->|Fetch User Data| ProfileAPI
    TM2 -->|Fetch User Data| ProfileAPI
    TM3 -->|Fetch User Data| ProfileAPI
    TMN -->|Fetch User Data| ProfileAPI
    
    TM1 -->|Send Actions| Webhook
    TM2 -->|Send Actions| Webhook
    TM3 -->|Send Actions| Webhook
    TMN -->|Send Actions| Webhook
    
    style JM fill:#ff9999
    style TM1 fill:#99ccff
    style TM2 fill:#99ccff
    style TM3 fill:#99ccff
    style TMN fill:#99ccff
    style RocksDB fill:#ffcc99
```

---

## Flink Cluster Architecture

### JobManager and TaskManager Relationship

```mermaid
graph TB
    subgraph "JobManager (Master Node)"
        JM_Scheduler[Job Scheduler]
        JM_Checkpoint[Checkpoint Coordinator]
        JM_ResourceMgr[Resource Manager]
        JM_Dispatcher[Dispatcher]
        
        JM_Scheduler --> JM_Checkpoint
        JM_Scheduler --> JM_ResourceMgr
        JM_Dispatcher --> JM_Scheduler
    end
    
    subgraph "TaskManager 1 (Worker Node)"
        TM1_Slot1[Task Slot 1<br/>Parallelism Instance]
        TM1_Slot2[Task Slot 2<br/>Parallelism Instance]
        TM1_Slot3[Task Slot 3<br/>Parallelism Instance]
        TM1_Slot4[Task Slot 4<br/>Parallelism Instance]
        TM1_Network[Network Buffers]
        TM1_Memory[Managed Memory<br/>RocksDB Cache]
    end
    
    subgraph "TaskManager 2 (Worker Node)"
        TM2_Slot1[Task Slot 1]
        TM2_Slot2[Task Slot 2]
        TM2_Slot3[Task Slot 3]
        TM2_Slot4[Task Slot 4]
        TM2_Network[Network Buffers]
        TM2_Memory[Managed Memory]
    end
    
    subgraph "TaskManager N (Worker Node)"
        TMN_Slot1[Task Slot 1]
        TMN_Slot2[Task Slot 2]
        TMN_Slot3[Task Slot 3]
        TMN_Slot4[Task Slot 4]
        TMN_Network[Network Buffers]
        TMN_Memory[Managed Memory]
    end
    
    JM_ResourceMgr -->|Allocate Slots| TM1_Slot1
    JM_ResourceMgr -->|Allocate Slots| TM1_Slot2
    JM_ResourceMgr -->|Allocate Slots| TM2_Slot1
    JM_ResourceMgr -->|Allocate Slots| TMN_Slot1
    
    JM_Checkpoint -->|Trigger Checkpoint| TM1_Slot1
    JM_Checkpoint -->|Trigger Checkpoint| TM2_Slot1
    JM_Checkpoint -->|Trigger Checkpoint| TMN_Slot1
    
    TM1_Slot1 -.->|Share Resources| TM1_Memory
    TM1_Slot2 -.->|Share Resources| TM1_Memory
    TM2_Slot1 -.->|Share Resources| TM2_Memory
    TMN_Slot1 -.->|Share Resources| TMN_Memory
    
    style JM_Scheduler fill:#ff9999
    style JM_Checkpoint fill:#ff9999
    style TM1_Slot1 fill:#99ccff
    style TM2_Slot1 fill:#99ccff
    style TMN_Slot1 fill:#99ccff
    style TM1_Memory fill:#ffcc99
    style TM2_Memory fill:#ffcc99
    style TMN_Memory fill:#ffcc99
```

### Job Deployment and Task Distribution

```mermaid
graph LR
    subgraph "Job Submission"
        Client[Flink Client<br/>Submit JAR]
    end
    
    subgraph "JobManager"
        JobGraph[Job Graph<br/>Logical Plan]
        ExecutionGraph[Execution Graph<br/>Physical Plan]
        TaskDeployer[Task Deployer]
    end
    
    subgraph "TaskManager Slots"
        Task1[Source Task<br/>Kafka Consumer<br/>Parallelism: 4]
        Task2[KeyedProcess Task<br/>UserStateManager<br/>Parallelism: 4]
        Task3[Filter Task<br/>EventFilter<br/>Parallelism: 4]
        Task4[Map Task<br/>ActionExecutor<br/>Parallelism: 4]
    end
    
    Client -->|Submit Job| JobGraph
    JobGraph -->|Optimize| ExecutionGraph
    ExecutionGraph -->|Deploy| TaskDeployer
    
    TaskDeployer -->|Assign to Slot| Task1
    TaskDeployer -->|Assign to Slot| Task2
    TaskDeployer -->|Assign to Slot| Task3
    TaskDeployer -->|Assign to Slot| Task4
    
    Task1 -->|Data Stream| Task2
    Task2 -->|Data Stream| Task3
    Task3 -->|Data Stream| Task4
    
    style JobGraph fill:#ff9999
    style ExecutionGraph fill:#ff9999
    style Task1 fill:#99ccff
    style Task2 fill:#99ccff
    style Task3 fill:#99ccff
    style Task4 fill:#99ccff
```

---

## Event Flow and Data Pipeline

### Kafka to Flink Event Dispatch

```mermaid
sequenceDiagram
    participant K as Kafka Broker
    participant KP1 as Kafka Partition 0
    participant KP2 as Kafka Partition 1
    participant KP3 as Kafka Partition 2
    participant KP4 as Kafka Partition 3
    participant FC1 as Flink Consumer 1<br/>(TM1-Slot1)
    participant FC2 as Flink Consumer 2<br/>(TM1-Slot2)
    participant FC3 as Flink Consumer 3<br/>(TM2-Slot1)
    participant FC4 as Flink Consumer 4<br/>(TM2-Slot2)
    
    Note over K: Events arrive at Kafka
    K->>KP1: Route by partition key
    K->>KP2: Route by partition key
    K->>KP3: Route by partition key
    K->>KP4: Route by partition key
    
    Note over FC1,FC4: Flink Source Operator<br/>Parallelism = 4
    
    KP1->>FC1: Poll events (user_id: 1,5,9...)
    KP2->>FC2: Poll events (user_id: 2,6,10...)
    KP3->>FC3: Poll events (user_id: 3,7,11...)
    KP4->>FC4: Poll events (user_id: 4,8,12...)
    
    FC1->>FC1: Deserialize JSON
    FC2->>FC2: Deserialize JSON
    FC3->>FC3: Deserialize JSON
    FC4->>FC4: Deserialize JSON
    
    Note over FC1,FC4: Events forwarded to<br/>KeyedProcessFunction
```

### Complete Event Processing Pipeline

```mermaid
graph TB
    subgraph "Stage 1: Event Ingestion"
        KafkaSource[Kafka Source Operator<br/>Parallelism: 4<br/>Consumes from 4 partitions]
        Deserializer[UserEventDeserializer<br/>JSON → UserEvent POJO]
        
        KafkaSource --> Deserializer
    end
    
    subgraph "Stage 2: Event Routing (KeyBy)"
        KeyBy[KeyBy user_id<br/>Hash Partitioning<br/>Routes to correct state]
        
        Deserializer --> KeyBy
    end
    
    subgraph "Stage 3: State Enrichment"
        StateManager[UserStateManager<br/>KeyedProcessFunction<br/>Parallelism: 4]
        StateBackend[(RocksDB State<br/>Per-user checkpoint<br/>Up to 16MB per user)]
        ProfileAPI[Profile API<br/>External Service]
        
        KeyBy --> StateManager
        StateManager <-->|Read/Write| StateBackend
        StateManager -->|Fetch on miss| ProfileAPI
    end
    
    subgraph "Stage 4: Event Filtering"
        Filter[EventFilterFunction<br/>AviatorScript Evaluation<br/>Parallelism: 4]
        FilterScript[filter.av<br/>Business Rules]
        
        StateManager --> Filter
        Filter -.->|Load Script| FilterScript
    end
    
    subgraph "Stage 5: Action Execution"
        ActionExecutor[ActionExecutorFunction<br/>Map Operator<br/>Parallelism: 4]
        DebugHandler[Debug Print Handler]
        WebhookHandler[Webhook Handler]
        
        Filter --> ActionExecutor
        ActionExecutor -->|Debug Mode| DebugHandler
        ActionExecutor -->|Production| WebhookHandler
    end
    
    style KafkaSource fill:#99ccff
    style StateManager fill:#99ccff
    style Filter fill:#99ccff
    style ActionExecutor fill:#99ccff
    style StateBackend fill:#ffcc99
```

### Event Payload Flow Detail

```mermaid
sequenceDiagram
    participant Kafka as Kafka Topic<br/>user-tracking-events
    participant Source as Source Operator<br/>(Slot 1)
    participant KeyBy as KeyBy Operator
    participant State as State Manager<br/>(Slot 2)
    participant RocksDB as RocksDB State
    participant API as Profile API
    participant Filter as Filter Operator<br/>(Slot 3)
    participant Action as Action Operator<br/>(Slot 4)
    
    Note over Kafka: Event arrives<br/>{user_id: "user_123", event_name: "page_view"}
    
    Kafka->>Source: Poll event from partition
    Source->>Source: Deserialize JSON to UserEvent
    
    Source->>KeyBy: Forward UserEvent
    Note over KeyBy: Hash(user_id) % parallelism<br/>Determines target partition
    
    KeyBy->>State: Route to correct instance<br/>(based on user_id hash)
    
    State->>RocksDB: Get UserCheckpoint(user_123)
    
    alt Checkpoint exists (cache hit)
        RocksDB-->>State: Return cached checkpoint
    else Checkpoint missing (cache miss)
        State->>API: GET /users/user_123/profile
        State->>API: GET /users/user_123/visit
        State->>API: GET /users/user_123/history
        API-->>State: Return user data
        State->>RocksDB: Store new checkpoint
    end
    
    State->>State: Create EnrichedEvent<br/>{event, userProfile, visit, history}
    State->>Filter: Forward EnrichedEvent
    
    Filter->>Filter: Evaluate filter script<br/>if (event.triggerable && user.country == "US")
    
    alt Filter passes
        Filter->>Action: Forward EnrichedEvent
        Action->>Action: Execute action handler
        
        alt Debug mode
            Action->>Action: Print DEBUG EVENT to logs
        else Production mode
            Action->>Action: Send HTTP POST to webhook
        end
    else Filter rejects
        Note over Filter: Event dropped
    end
```

---

## State Management and Storage

### State Storage Architecture

```mermaid
graph TB
    subgraph "TaskManager 1"
        Slot1[Task Slot 1<br/>Processes user_id: 1,5,9,13...]
        Slot2[Task Slot 2<br/>Processes user_id: 2,6,10,14...]
        
        RocksDB1[(RocksDB Instance 1<br/>Local SSD/NVMe<br/>State for Slot 1)]
        RocksDB2[(RocksDB Instance 2<br/>Local SSD/NVMe<br/>State for Slot 2)]
        
        Slot1 <--> RocksDB1
        Slot2 <--> RocksDB2
    end
    
    subgraph "TaskManager 2"
        Slot3[Task Slot 3<br/>Processes user_id: 3,7,11,15...]
        Slot4[Task Slot 4<br/>Processes user_id: 4,8,12,16...]
        
        RocksDB3[(RocksDB Instance 3<br/>Local SSD/NVMe<br/>State for Slot 3)]
        RocksDB4[(RocksDB Instance 4<br/>Local SSD/NVMe<br/>State for Slot 4)]
        
        Slot3 <--> RocksDB3
        Slot4 <--> RocksDB4
    end
    
    subgraph "Checkpoint Storage (Distributed)"
        S3[(S3 / HDFS / NFS<br/>Checkpoint Snapshots<br/>Incremental Backups)]
    end
    
    subgraph "JobManager"
        CheckpointCoord[Checkpoint Coordinator<br/>Triggers every 60s]
    end
    
    CheckpointCoord -->|Trigger Checkpoint| Slot1
    CheckpointCoord -->|Trigger Checkpoint| Slot2
    CheckpointCoord -->|Trigger Checkpoint| Slot3
    CheckpointCoord -->|Trigger Checkpoint| Slot4
    
    RocksDB1 -->|Upload Snapshot| S3
    RocksDB2 -->|Upload Snapshot| S3
    RocksDB3 -->|Upload Snapshot| S3
    RocksDB4 -->|Upload Snapshot| S3
    
    style RocksDB1 fill:#ffcc99
    style RocksDB2 fill:#ffcc99
    style RocksDB3 fill:#ffcc99
    style RocksDB4 fill:#ffcc99
    style S3 fill:#ff9999
```

### State Size and Storage Requirements

```mermaid
graph LR
    subgraph "Per-User State Structure"
        UserCheckpoint[UserCheckpoint Object]
        Profile[UserProfile<br/>~2KB]
        Visit[Current Visit<br/>~1KB]
        History[Event History<br/>~13KB<br/>Last 100 events]
        
        UserCheckpoint --> Profile
        UserCheckpoint --> Visit
        UserCheckpoint --> History
    end
    
    subgraph "State Size Calculation"
        TotalPerUser[Total per user: ~16KB]
        Users[Active Users: 1M]
        TotalState[Total State: ~16GB]
        
        TotalPerUser --> Users
        Users --> TotalState
    end
    
    subgraph "Storage Requirements"
        LocalSSD[Local SSD per TM<br/>Recommended: 100GB+<br/>For working set + overhead]
        CheckpointStorage[Checkpoint Storage<br/>Recommended: 500GB+<br/>For snapshots + history]
    end
    
    TotalState --> LocalSSD
    TotalState --> CheckpointStorage
    
    style TotalState fill:#ff9999
    style LocalSSD fill:#ffcc99
    style CheckpointStorage fill:#ffcc99
```

---

## Scaling Considerations

### Horizontal Scaling Strategy

```mermaid
graph TB
    subgraph "Small Scale (Development)"
        JM1[JobManager: 1]
        TM1[TaskManager: 1<br/>Slots: 4<br/>Memory: 4GB]
        Parallelism1[Parallelism: 2-4]
        Throughput1[Throughput: ~10K events/sec]
    end
    
    subgraph "Medium Scale (Production)"
        JM2[JobManager: 1<br/>+ Standby: 1]
        TM2[TaskManager: 4-8<br/>Slots: 4 each<br/>Memory: 8GB each]
        Parallelism2[Parallelism: 16-32]
        Throughput2[Throughput: ~100K events/sec]
    end
    
    subgraph "Large Scale (Enterprise)"
        JM3[JobManager: 1<br/>+ Standby: 2]
        TM3[TaskManager: 20-50<br/>Slots: 4 each<br/>Memory: 16GB each]
        Parallelism3[Parallelism: 80-200]
        Throughput3[Throughput: ~1M events/sec]
    end
    
    style JM1 fill:#ff9999
    style JM2 fill:#ff9999
    style JM3 fill:#ff9999
    style TM1 fill:#99ccff
    style TM2 fill:#99ccff
    style TM3 fill:#99ccff
```

### Resource Allocation Guidelines

| Component | Small Scale | Medium Scale | Large Scale |
|-----------|-------------|--------------|-------------|
| **JobManager** | 1 instance<br/>2 CPU, 4GB RAM | 1 active + 1 standby<br/>4 CPU, 8GB RAM | 1 active + 2 standby<br/>8 CPU, 16GB RAM |
| **TaskManager** | 1 instance<br/>4 slots, 4GB RAM | 4-8 instances<br/>4 slots, 8GB RAM each | 20-50 instances<br/>4 slots, 16GB RAM each |
| **Parallelism** | 2-4 | 16-32 | 80-200 |
| **Kafka Partitions** | 4 | 16-32 | 80-200 |
| **Local State Storage** | 50GB SSD | 100GB SSD per TM | 200GB NVMe per TM |
| **Checkpoint Storage** | 100GB | 500GB | 2TB+ |
| **Network Bandwidth** | 1 Gbps | 10 Gbps | 25 Gbps |

### State TTL and Cleanup

```mermaid
graph LR
    subgraph "State Lifecycle"
        EventArrival[Event Arrives<br/>for user_123]
        StateAccess[Access State<br/>Reset TTL Timer]
        StateActive[State Active<br/>TTL: 10 minutes]
        StateExpired[State Expired<br/>After 10 min idle]
        StateCleanup[State Cleanup<br/>Remove from RocksDB]
        
        EventArrival --> StateAccess
        StateAccess --> StateActive
        StateActive -->|No activity| StateExpired
        StateExpired --> StateCleanup
        StateActive -->|New event| StateAccess
    end
    
    subgraph "Benefits"
        MemorySaving[Memory Savings<br/>Remove inactive users]
        Performance[Better Performance<br/>Smaller working set]
        
        StateCleanup --> MemorySaving
        StateCleanup --> Performance
    end
    
    style StateActive fill:#99ccff
    style StateExpired fill:#ff9999
    style MemorySaving fill:#99ff99
    style Performance fill:#99ff99
```

---

## Deployment Configuration

### Recommended Flink Configuration for Large Scale

```yaml
# JobManager Configuration
jobmanager:
  memory:
    process.size: 8g
    jvm-overhead.fraction: 0.1
  rpc.address: flink-jobmanager
  rpc.port: 6123
  web.port: 8081

# TaskManager Configuration  
taskmanager:
  memory:
    process.size: 16g
    managed.fraction: 0.4  # 6.4GB for RocksDB
    network.fraction: 0.1   # 1.6GB for network buffers
  numberOfTaskSlots: 4
  
# State Backend Configuration
state:
  backend: rocksdb
  backend.incremental: true
  backend.rocksdb.localdir: /data/rocksdb
  checkpoints.dir: s3://flink-checkpoints/
  savepoints.dir: s3://flink-savepoints/
  
# Checkpoint Configuration
execution:
  checkpointing:
    interval: 60000  # 60 seconds
    mode: EXACTLY_ONCE
    timeout: 600000  # 10 minutes
    max-concurrent-checkpoints: 1
    min-pause: 30000  # 30 seconds between checkpoints
```

### Volume Requirements

```mermaid
graph TB
    subgraph "TaskManager Storage"
        LocalState[Local State Storage<br/>Path: /data/rocksdb<br/>Type: SSD/NVMe<br/>Size: 200GB per TM]
        TempStorage[Temp Storage<br/>Path: /tmp<br/>Type: SSD<br/>Size: 50GB per TM]
    end
    
    subgraph "Shared Storage"
        Checkpoints[Checkpoint Storage<br/>Path: s3://flink-checkpoints/<br/>Type: S3/HDFS<br/>Size: 2TB+]
        Savepoints[Savepoint Storage<br/>Path: s3://flink-savepoints/<br/>Type: S3/HDFS<br/>Size: 500GB+]
        Logs[Log Storage<br/>Path: /var/log/flink<br/>Type: Standard Disk<br/>Size: 100GB]
    end
    
    LocalState -.->|Snapshot| Checkpoints
    LocalState -.->|Manual Save| Savepoints
    
    style LocalState fill:#ffcc99
    style Checkpoints fill:#ff9999
    style Savepoints fill:#ff9999
```

---

## Monitoring and Observability

### Key Metrics to Monitor

```mermaid
graph TB
    subgraph "JobManager Metrics"
        JM_Jobs[Active Jobs]
        JM_Checkpoints[Checkpoint Success Rate]
        JM_Restarts[Job Restart Count]
    end
    
    subgraph "TaskManager Metrics"
        TM_CPU[CPU Usage per TM]
        TM_Memory[Memory Usage per TM]
        TM_Network[Network I/O]
        TM_Disk[Disk I/O]
    end
    
    subgraph "Application Metrics"
        APP_Throughput[Events/sec Processed]
        APP_Latency[End-to-end Latency]
        APP_Backpressure[Backpressure Indicators]
        APP_StateSize[State Size per User]
    end
    
    subgraph "State Backend Metrics"
        STATE_Size[Total State Size]
        STATE_Checkpoint[Checkpoint Duration]
        STATE_RocksDB[RocksDB Compaction]
    end
    
    style JM_Checkpoints fill:#99ff99
    style APP_Throughput fill:#99ff99
    style STATE_Checkpoint fill:#ffcc99
```

---

## Failure Recovery

### Checkpoint and Recovery Flow

```mermaid
sequenceDiagram
    participant JM as JobManager
    participant TM1 as TaskManager 1
    participant TM2 as TaskManager 2
    participant S3 as S3 Storage
    
    Note over JM: Normal Operation
    JM->>TM1: Trigger Checkpoint (ID: 100)
    JM->>TM2: Trigger Checkpoint (ID: 100)
    
    TM1->>TM1: Snapshot local state
    TM2->>TM2: Snapshot local state
    
    TM1->>S3: Upload state snapshot
    TM2->>S3: Upload state snapshot
    
    TM1->>JM: Checkpoint complete
    TM2->>JM: Checkpoint complete
    
    JM->>JM: Mark checkpoint 100 as complete
    
    Note over TM1: TaskManager 1 Fails!
    
    JM->>JM: Detect failure
    JM->>JM: Restart job from checkpoint 100
    
    JM->>TM2: Cancel current tasks
    JM->>TM2: Deploy new tasks
    
    TM2->>S3: Download state snapshot
    S3->>TM2: Restore state
    
    Note over TM2: Resume processing from checkpoint 100
```

---

## Summary

This architecture supports:

- **High Throughput**: Process millions of events per second
- **Large State**: Handle up to 16MB per user with efficient storage
- **Fault Tolerance**: Exactly-once processing with checkpoint recovery
- **Horizontal Scaling**: Add TaskManagers to increase capacity
- **State Management**: Efficient RocksDB backend with TTL cleanup
- **Monitoring**: Comprehensive metrics for operations

For production deployment, ensure:
1. Adequate local SSD storage for RocksDB state
2. Reliable distributed storage (S3/HDFS) for checkpoints
3. Sufficient network bandwidth between components
4. Proper resource allocation based on expected load
5. Monitoring and alerting on key metrics
