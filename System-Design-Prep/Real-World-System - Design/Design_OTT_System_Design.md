# OTT Platform - HLD / LLD (Netflix / Amazon Prime / Hotstar Style)

## Table of Contents
- [Requirements](#requirements)
- [High Level Design (HLD)](#high-level-design-hld)
- [Microservices Breakdown](#microservices-breakdown)
- [Low Level Design (LLD)](#low-level-design-lld)
- [Video Ingestion and Processing Pipeline](#video-ingestion-and-processing-pipeline)
- [Database Design](#database-design)
- [Key System Design Decisions](#key-system-design-decisions)
- [Non-Functional Requirements](#non-functional-requirements)
- [Interview Quick Reference](#interview-quick-reference)

---

## Requirements

### Functional Requirements
```
| **Feature**          | **Description**                                          |
|----------------------|----------------------------------------------------------|
| User Management      | Register, login, subscription management                 |
| Content Catalog      | Browse movies, shows, and episodes by genre and language |
| Video Streaming      | Adaptive Bitrate Streaming (ABR)                         |
| Search               | Full-text search across titles, actors, and genres       |
| Continue Watching    | Resume playback from where the user left off             |
| Download             | Offline viewing for mobile devices                       |
| Recommendations      | Personalized content suggestions                         |
| Multiple Profiles    | Support up to 5 profiles per account                     |
| Live Streaming       | Live sports, news, and events (Hotstar-style)            |
| Subtitles & Audio    | Multi-language subtitles and dubbed audio tracks         |
```
### Non-Functional Requirements
```
| **Property**         | **Target**                                                |
|----------------------|-----------------------------------------------------------|
| Availability         | 99.99% uptime                                             |
| Latency              | Video start time under 2 seconds                          |
| Scale                | 200M+ users, 10M concurrent streams                       |
| Throughput           | Petabytes of video served daily                           |
| Consistency          | Eventual consistency for watch history and recommendations|
| Strong Consistency   | Subscriptions and payments                                |
```
---

## High Level Design (HLD)

### System Architecture Overview

```
             Client (Web / iOS / Android / Smart TV)
                              |
                     CDN (CloudFront / Akamai)
                    (video chunks, static assets)
                              |
                       API Gateway / BFF
                  (Auth, Rate Limiting, Routing)
                              |
     +--------+--------+--------+--------+--------+--------+
     |        |        |        |        |        |        |
   User    Content  Streaming  Search  Watch   Recommend
  Service  Service   Service  Service History   Service
     |        |        |        |        |        |
  MySQL    MongoDB  S3+CDN  Elastic  Cassandra  Redis +
  Redis    Redis            Search   + Kafka     ML Model
                              |
                       Video Processing
                          Pipeline
                    (FFmpeg + Transcoding)
                              |
                        Object Storage
                     (S3 / GCS - raw + HLS)
```

### Video Streaming Request Flow

```
User clicks Play
       |
API Gateway (validate JWT + subscription check)
       |
Streaming Service
  - fetch content metadata (Content Service)
  - generate signed CDN URL (time-limited, user-bound)
  - return manifest file URL (m3u8 for HLS)
       |
Client fetches manifest from CDN
  - manifest lists all quality variants (360p, 720p, 1080p, 4K)
       |
Client selects quality based on bandwidth (ABR logic)
  - downloads video chunks (.ts segments) from CDN edge node
       |
Player buffers and plays
  - monitors bandwidth continuously
  - switches quality up/down as network changes
       |
Watch History Service (async)
  - heartbeat every 10s -> stores position in Cassandra
```

---

## Microservices Breakdown

### 1. User Service
- Register, login, OAuth (Google, Apple)
- JWT token issuance, refresh
- Subscription plan management (Basic, Standard, Premium)
- Multiple profile management per account
- **DB:** MySQL (users, subscriptions) + Redis (session cache)

### 2. Content Catalog Service
- CRUD for movies, series, episodes
- Metadata: title, description, cast, genre, language, release year
- Content availability by region (geo-restriction)
- **DB:** MongoDB (flexible content schema) + Redis (hot catalog cache)

### 3. Streaming Service
- Generates signed CDN URLs per user per content
- Returns HLS manifest (.m3u8) pointing to CDN
- Enforces concurrent stream limits per plan
- DRM token generation (Widevine, FairPlay)
- **DB:** Redis (active stream sessions, concurrent count)

### 4. Video Processing Service (Ingestion Pipeline)
- Accepts raw uploaded video from content team
- Transcodes to multiple resolutions and bitrates
- Packages into HLS / DASH segments
- Stores to S3, invalidates CDN cache
- **Queue:** SQS / Kafka for async transcoding jobs

### 5. Search Service
- Full-text search on title, cast, description, genre
- Autocomplete, fuzzy search
- **DB:** Elasticsearch (synced from Content Catalog via Kafka)

### 6. Watch History Service
- Stores per-user per-content playback position
- Powers "Continue Watching" row
- High write throughput (heartbeat every 10s per active stream)
- **DB:** Cassandra (wide column, high write, time-series friendly)

### 7. Recommendation Service
- Collaborative filtering based on watch history
- Content-based filtering based on genres, cast
- Pre-computed daily batch + real-time signals
- **DB:** Redis (pre-computed results per user)

### 8. Notification Service
- New content alerts, subscription expiry reminders
- Consumes Kafka events
- **Channels:** Push (FCM/APNs), Email (SES), SMS

### 9. Payment Service
- Subscription billing, renewal, cancellation
- Integrates with Stripe / Razorpay
- Idempotency for retry-safe billing
- **DB:** MySQL (billing records, immutable)

---

## Low Level Design (LLD)

### Adaptive Bitrate Streaming (ABR)

HLS (HTTP Live Streaming) splits video into small chunks and serves a manifest listing multiple quality variants.

```
Master Manifest (master.m3u8):
  #EXT-X-STREAM-INF:BANDWIDTH=400000,RESOLUTION=640x360
  360p/index.m3u8
  #EXT-X-STREAM-INF:BANDWIDTH=1500000,RESOLUTION=1280x720
  720p/index.m3u8
  #EXT-X-STREAM-INF:BANDWIDTH=4000000,RESOLUTION=1920x1080
  1080p/index.m3u8

Variant Manifest (720p/index.m3u8):
  #EXTINF:6.0,
  segment_001.ts
  #EXTINF:6.0,
  segment_002.ts
  ...

Client logic:
  - measure download speed of last segment
  - if speed > 4 Mbps -> switch to 1080p
  - if speed < 1.5 Mbps -> switch to 360p
```

### Video Ingestion Pipeline

```
Content Team uploads raw video (4K .mp4)
              |
S3 Raw Bucket (trigger Lambda / SQS)
              |
Transcoding Worker (EC2 / ECS with FFmpeg)
  - transcode to: 360p, 480p, 720p, 1080p, 4K
  - generate HLS segments (6s chunks per quality)
  - extract thumbnails at intervals
  - generate subtitle tracks
  - apply DRM encryption (AES-128 / Widevine)
              |
S3 Processed Bucket
  /content/{contentId}/360p/segment_xxx.ts
  /content/{contentId}/720p/segment_xxx.ts
  /content/{contentId}/1080p/segment_xxx.ts
  /content/{contentId}/master.m3u8
              |
CloudFront CDN invalidation -> new content available at edge
              |
Kafka event: content.published -> Content Catalog, Search (ES)
```

### Concurrent Stream Limit (per Plan)

```
User starts stream:
  INCR stream_count:{userId}  (Redis atomic increment)
  SET  stream_count:{userId} EX 86400

  if count > plan_limit:
    return 429 Too Many Streams

User stops stream / session expires:
  DECR stream_count:{userId}
```

### Signed CDN URL (Prevent Unauthorized Access)

```
Client requests video
       |
Streaming Service checks:
  - valid JWT
  - active subscription
  - content available in user's region
       |
Generate signed URL:
  URL = cdn.example.com/content/{id}/master.m3u8
  Params: Expires={unix_ts}, Signature={HMAC(secret, url+expiry+userId)}
  TTL: 4 hours (enough for one movie)
       |
Client uses signed URL directly with CDN
CDN validates signature before serving chunks
```

### Watch History - Cassandra Schema

```sql
-- Wide column: one row per user, columns are content positions
CREATE TABLE watch_history (
  user_id     UUID,
  content_id  UUID,
  profile_id  UUID,
  position_sec INT,         -- seconds watched
  duration_sec INT,         -- total duration
  watched_at  TIMESTAMP,
  completed   BOOLEAN,
  PRIMARY KEY ((user_id), watched_at, content_id)
) WITH CLUSTERING ORDER BY (watched_at DESC);
```

**Why Cassandra?**
- 10M concurrent users x heartbeat every 10s = 1M writes/sec
- Cassandra handles high write throughput natively
- Time-series access pattern (latest watched first)

### DRM (Digital Rights Management)

```
Video chunks encrypted with AES-128 key
       |
Key stored in Key Management Service (KMS)
       |
Client requests license:
  DRM License Server validates:
    - user identity (JWT)
    - active subscription
    - device registered to account
  Returns: decryption key (short-lived, device-bound)
       |
Player decrypts and plays chunk
Key never exposed in plaintext to user
```

---

## Database Design

### Users Table (MySQL)

```sql
CREATE TABLE users (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  email          VARCHAR(255) UNIQUE NOT NULL,
  password_hash  VARCHAR(255),
  created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE profiles (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT NOT NULL,
  name        VARCHAR(100),
  avatar_url  VARCHAR(255),
  kid_mode    BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE subscriptions (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT NOT NULL,
  plan        ENUM('BASIC','STANDARD','PREMIUM'),
  status      ENUM('ACTIVE','CANCELLED','EXPIRED'),
  start_date  DATE,
  end_date    DATE,
  INDEX idx_user_status (user_id, status)
);
```

### Content Document (MongoDB)

```json
{
  "_id": "content_789",
  "title": "Stranger Things",
  "type": "SERIES",
  "genres": ["sci-fi", "thriller", "drama"],
  "cast": ["Millie Bobby Brown", "Finn Wolfhard"],
  "language": "English",
  "dubbed_languages": ["Hindi", "Tamil", "Telugu"],
  "subtitles": ["English", "Hindi", "Spanish"],
  "seasons": [
    {
      "season_number": 1,
      "episodes": [
        {
          "episode_number": 1,
          "title": "The Vanishing of Will Byers",
          "duration_sec": 2880,
          "hls_manifest": "cdn.example.com/content/789/s1e1/master.m3u8"
        }
      ]
    }
  ],
  "ratings": { "imdb": 8.7, "platform": 4.5 },
  "geo_availability": ["IN", "US", "UK"],
  "maturity_rating": "TV-14"
}
```

---

## Key System Design Decisions

### Caching Strategy
```
| **Data**            | **Cache**   | **TTL** | **Reason**                                 |
|---------------------|-------------|---------|--------------------------------------------|
| Content Metadata    | Redis       | 10 min  | High read frequency, infrequent updates    |
| Homepage Rows       | Redis       | 5 min   | Cache expensive aggregation queries        |
| Recommendations     | Redis       | 1 hr    | Cache pre-computed ML recommendations      |
| Active Stream Count | Redis       | 24 hr   | Enforce concurrent streaming limits        |
| Signed CDN URL      | Client-side | 4 hr    | Avoid regenerating signed URLs per chunk   |
| Search Results      | Redis       | 2 min   | Reduce expensive Elasticsearch queries     |
```
### Kafka Topics
```
| **Kafka Topic**     | **Producer**         | **Consumer(s)**                               |
|---------------------|----------------------|-----------------------------------------------|
| content.published   | Video Pipeline       | Search Service (Elasticsearch Sync), Notification Service |
| user.subscribed     | Payment Service      | User Service, Notification Service            |
| user.watched        | Watch History Service| Recommendation Service (Real-time Signal)     |
| stream.started      | Streaming Service    | Analytics Service, Concurrent Limit Service   |
| stream.ended        | Streaming Service    | Analytics Service, Stream Limit Service       |
```
### Live Streaming Architecture (Hotstar Style)

```
Camera / Encoder (OBS / hardware encoder)
       |
RTMP Ingest Server (Wowza / Nginx-RTMP)
       |
Transcoding (real-time, lower latency target: 3-5s)
  - 360p, 480p, 720p, 1080p variants
       |
Low-Latency HLS (LL-HLS) segments (2s chunks)
       |
CDN Push (pre-position at edge nodes)
       |
Millions of viewers fetch from nearest CDN PoP
```

**Scale challenge:** IPL on Hotstar peaked at 25M concurrent viewers.
Solution: aggressive CDN pre-warming, LL-HLS, regional edge caching, autoscale ingest servers.

### Storage Tiering

```
Hot (S3 Standard):    content released in last 3 months (frequent access)
Warm (S3 IA):         content 3-12 months old (infrequent access)
Cold (S3 Glacier):    content older than 1 year or low-view titles
Deleted:              expired licensed content auto-tiered via S3 Lifecycle Policy
```

### Recommendation System

```
Batch Layer (runs nightly):
  - Apache Spark reads watch history from Cassandra
  - Collaborative filtering (ALS algorithm)
  - Output: top 50 recommendations per user -> Redis

Real-Time Layer (streaming):
  - Kafka stream of user.watched events
  - Flink / Spark Streaming updates signals
  - Blends with batch output for freshness

Serving:
  GET /recommendations/{userId}
  -> Redis cache hit: return pre-computed list
  -> Cache miss: fallback to popularity-based ranking
```

---

## Non-Functional Requirements

### Scalability

```
Global Load Balancer (AWS Route53 + ALB)
        |
API Gateway cluster (Kubernetes, HPA)
        |
Microservices (Docker + K8s)
        |
Data Layer:
  MySQL:        Read replicas per region
  MongoDB:      Sharded cluster + replica sets
  Cassandra:    Multi-DC replication
  Redis:        Redis Cluster mode
  Elasticsearch: Multi-node, dedicated master nodes
  S3 + CDN:     Global distribution (200+ PoPs)
```

### Fault Tolerance

```
| **Reliability Strategy**              | **Applied To**                               | **Purpose**                                                  |
|---------------------------------------|----------------------------------------------|--------------------------------------------------------------|
| Circuit Breaker (Resilience4j)        | All inter-service API calls                  | Prevent cascading failures when dependent services are unavailable |
| Retry with Exponential Backoff        | Payment Service, License Service             | Retry transient failures while preventing retry storms       |
| Dead Letter Queue (DLQ)               | Watch History, Notification Kafka Consumers  | Store failed messages for later analysis and reprocessing    |
| Multi-AZ Deployment                   | MySQL, Cassandra, Redis                      | Ensure high availability and automatic failover across zones |
| CDN Failover                          | Video Delivery Network                       | Automatically route requests to the origin server when CDN cache misses or edge nodes fail |
| Graceful Degradation                  | Homepage, Recommendation Service             | Serve cached or trending content when the Recommendation Service is unavailable |
| Health Checks & Auto-Restart          | Kubernetes Pods                              | Automatically restart unhealthy containers using liveness and readiness probes |
| Auto Scaling                          | Streaming, Recommendation, Search Services   | Scale services automatically based on CPU, memory, or request load |
| Read Replicas                         | User Profile, Video Metadata                 | Distribute read traffic and reduce primary database load     |
| Distributed Tracing                   | All Microservices                            | Trace requests across services for faster debugging          |
| Centralized Logging                   | All Services                                 | Aggregate logs for monitoring, troubleshooting, and auditing |
| Rate Limiting                         | API Gateway                                  | Prevent abuse and protect backend services from excessive requests |
```

### Content Delivery Numbers (Back of Envelope)

```
1080p video bitrate:     ~4 Mbps
10M concurrent streams:  10,000,000 x 4 Mbps = 40 Tbps egress
CDN handles this:        distributed across 200+ global PoPs
Origin S3 load:          only cache misses hit origin (~1-2%)
```

---

## Interview Quick Reference
```
| **Question** | **Answer** |
|--------------|------------|
| What is Adaptive Bitrate (ABR) Streaming? | The client downloads a manifest containing multiple video quality variants and dynamically switches between them based on real-time network bandwidth. |
| Why use HLS over DASH? | HLS offers broader device support (especially on Apple devices), while DASH is an open standard. Many streaming platforms support both. |
| How do you prevent unauthorized streaming? | Generate signed CDN URLs with user identity, expiry time, and HMAC signature. Use DRM to encrypt video segments and prevent unauthorized playback. |
| Why use Cassandra for watch history? | Cassandra is optimized for high write throughput and time-series data, making it ideal for millions of watch progress updates. |
| How do you enforce concurrent stream limits? | Use Redis `INCR` when a stream starts and `DECR` when it ends. Compare the active stream count against the user's subscription limit atomically. |
| How is a video processed after upload? | Upload the raw video to object storage, trigger a transcoding pipeline, generate HLS segments at multiple resolutions using FFmpeg, and store the output back in object storage. |
| Why use a CDN instead of serving from the origin? | Most video requests are served from CDN edge locations to reduce latency and bandwidth costs, while the origin server acts as a fallback. |
| How do you handle live streaming at scale? | Ingest the stream using RTMP, perform real-time transcoding, generate Low-Latency HLS (LL-HLS) segments, and distribute them through CDN edge locations. |
| Which database is used for the content catalog? | MongoDB is used because movies, TV shows, and episodes have flexible and evolving schemas. |
| How are recommendations generated? | Generate recommendations using offline collaborative filtering (e.g., Spark ALS) combined with real-time Kafka events, then cache the results in Redis. |
| How do you implement geo-restrictions? | Store allowed regions in the content metadata and validate the user's location before issuing a signed CDN URL. |
| How do you optimize storage costs? | Apply lifecycle policies to move older and less frequently viewed content from Standard Storage to Infrequent Access and eventually to Archive storage. |

```