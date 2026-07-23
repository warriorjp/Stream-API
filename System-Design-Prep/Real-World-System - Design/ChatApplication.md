# 💬 WhatsApp — System Design (HLD + LLD)

---

## Table of Contents

**High Level Design (HLD)**
1. [Requirements](#1-requirements)
2. [High-Level Architecture](#2-high-level-architecture)
3. [Core Components](#3-core-components)
4. [Message Flow](#4-message-flow)
5. [Group Messaging](#5-group-messaging)
6. [Media Sharing](#6-media-sharing)
7. [Online Presence & Last Seen](#7-online-presence--last-seen)
8. [Notifications](#8-notifications)
9. [Scalability & High Availability](#9-scalability--high-availability)

**Low Level Design (LLD)**
10. [Entities & Relationships](#10-entities--relationships)
11. [Database Schema](#11-database-schema)
12. [Message Delivery States](#12-message-delivery-states)
13. [End-to-End Encryption](#13-end-to-end-encryption)
14. [API Design](#14-api-design)
15. [2-Minute Interview Answer](#15-2-minute-interview-answer)

---

# Part 1 — High Level Design (HLD)

---

## 1. Requirements

### Functional
- One-to-one messaging
- Group messaging (up to 1024 members)
- Message delivery receipts (Sent, Delivered, Read)
- Media sharing (images, video, documents, audio)
- Online presence and Last Seen
- Push notifications when offline
- End-to-end encryption
- Message history sync across devices

### Non-Functional

| Property | Target |
|---|---|
| Scale | 2 billion users, 100 billion messages per day |
| Latency | Message delivery under 100ms |
| Availability | 99.99% uptime |
| Consistency | Eventual — message order preserved per chat |
| Durability | Messages persisted until delivered |
| Security | End-to-end encrypted, no plaintext on server |

---

## 2. High-Level Architecture

```
                   Client (Mobile / Web)
                          |
                          v
                    Load Balancer
                          |
          ----------------+----------------
          |               |               |
          v               v               v
    WebSocket         API Gateway     Media Service
    Server                |            (S3 / CDN)
    (Chat)                |
          ----------------+----------------
          |               |               |
          v               v               v
   Message Service   User Service    Group Service
          |
          v
    Kafka (Message Queue)
          |
    ------+-------
    |             |
    v             v
Message Store   Notification
(Cassandra)     Service
                (FCM / APNs)
```

---

## 3. Core Components

| Component | Responsibility |
|---|---|
| WebSocket Server | Persistent real-time connection per client. Routes incoming and outgoing messages |
| API Gateway | Handles REST calls — login, media upload, group management. Auth + rate limiting |
| Message Service | Processes all messages. Writes to Cassandra. Publishes to Kafka |
| User Service | User profiles, contacts, online/offline presence, last seen |
| Group Service | Group creation, member management, admin controls, fan-out |
| Media Service | Handles uploads to S3, generates CDN URLs and thumbnails |
| Notification Service | Sends FCM (Android) / APNs (iOS) push when user is offline |
| Kafka | Decouples message send from delivery. Buffers traffic spikes |
| Cassandra | Stores all messages. High write throughput. Partitioned by chat_id |
| Redis | Stores online presence with TTL. Routes messages across WS servers via Pub/Sub |

---

## 4. Message Flow

### Receiver Online

```
Sender types message
        |
        v
WebSocket Server (Sender side)
        |
        v
Message Service
  - Assign message_id
  - Set status = SENT
  - Write to Cassandra
        |
        v
Kafka Topic: messages
        |
        v
WebSocket Server (Receiver side)
        |
        v
Message delivered to Receiver instantly
  - Receiver device ACKs → status = DELIVERED
  - Receiver opens chat  → status = READ
        |
        v
Receipt sent back to Sender via WebSocket
```

### Receiver Offline

```
Message Service writes to Cassandra
        |
        v
Kafka publishes to notification topic
        |
        v
Notification Service
  - Receiver is offline
  - Push notification sent via FCM / APNs
        |
        v
Receiver comes online
  - WebSocket reconnects
  - Pulls pending messages from Cassandra
  - Status updated SENT → DELIVERED → READ
```

---

## 5. Group Messaging

### Fan-Out Strategy

```
Sender sends to Group (500 members)
        |
        v
Group Service fetches all member IDs
        |
        v
Message stored ONCE against group_id in Cassandra
        |
        v
Kafka publishes one delivery task per member
        |
        v
Per member:
  Online  → deliver via WebSocket
  Offline → push notification via FCM / APNs
```

### Why Store Once Not Per User

| Approach | Storage Cost |
|---|---|
| Per-user copy | 500 members x 100B messages = 50 trillion rows |
| Single group storage | 100B messages total — one row per message |

Storing once against group_id and fanning out delivery is the only approach that scales.

---

## 6. Media Sharing

```
User selects image
        |
        v
Client compresses image + generates thumbnail locally
        |
        v
Client requests pre-signed S3 URL from Media Service
        |
        v
Client uploads directly to S3 (bypasses app servers)
        |
        v
CDN caches media at edge nodes globally
        |
        v
Client sends message containing CDN media URL only
        |
        v
Receiver downloads media from nearest CDN edge node
```

**Why CDN:** Media is served from the edge server closest to the receiver. Reduces latency from seconds to milliseconds and removes load from core servers.

---

## 7. Online Presence & Last Seen

```
User opens app
  → WebSocket connects
  → Redis key set: presence:{user_id} = ONLINE  TTL = 60s
  → Client sends heartbeat ping every 30s to refresh TTL

User closes app (clean)
  → WebSocket disconnects
  → Redis key updated: presence:{user_id} = <timestamp>
  → last_seen written to DB

User loses connection (crash / no signal)
  → No clean disconnect
  → Redis TTL expires after 60s
  → System treats as offline automatically

Friend checks Last Seen
  → Read Redis (if ONLINE)
  → Else read last_seen from DB
  → Response: "Online" or "Last seen today at 4:30 PM"
```

---

## 8. Notifications

```
Offline user receives a message
        |
        v
Notification Service triggered via Kafka
        |
      ------
      |    |
      v    v
   FCM    APNs
Android   iOS
      |    |
      v    v
Push notification arrives on device
"New message from Jay"
        |
        v
User taps → App opens → WebSocket reconnects
→ Pending messages fetched from Cassandra
```

**Important:** Push notification payload contains NO message content — only a sender ID and badge count. Actual message is fetched after reconnect. This is required for end-to-end encryption compliance.

---

## 9. Scalability & High Availability

```
                  Load Balancer
                       |
        ---------------+---------------
        |              |              |
        v              v              v
  WS-Server-1    WS-Server-2    WS-Server-3
  (100K conns)   (100K conns)   (100K conns)
        |              |              |
        ---------------+---------------
                       |
                  Kafka Cluster
                       |
        ---------------+---------------
        |              |              |
        v              v              v
  Cassandra-1    Cassandra-2    Cassandra-3
  (chat A–H)     (chat I–P)     (chat Q–Z)
```

### Message Routing Across WebSocket Servers

```
User A → connected to WS-Server-1
User B → connected to WS-Server-3

A sends message to B:
WS-Server-1 publishes to Redis channel: inbox:user_B
WS-Server-3 subscribed to inbox:user_B
WS-Server-3 pushes message to User B ✅
```

### Scaling Decisions

| Concern | Solution |
|---|---|
| 2B users on WebSocket | Each WS server holds 100K connections — scale horizontally |
| Message routing across WS servers | Redis Pub/Sub — publish to user inbox channel |
| High write throughput | Cassandra — optimised for writes, partitioned by chat_id |
| Media bandwidth | S3 + CDN — offloads media serving from app servers |
| Traffic spikes | Kafka buffers messages — delivery service processes at its own pace |
| WS server crash | Client auto-reconnects — pulls missed messages from Cassandra |
| Cassandra hot partitions | Partition by chat_id not user_id — distributes evenly |

---

# Part 2 — Low Level Design (LLD)

---

## 10. Entities & Relationships

### Core Entities

| Entity | Description |
|---|---|
| User | A registered WhatsApp account identified by phone number |
| Chat | A conversation — either Direct (2 users) or Group |
| Message | A single message sent within a Chat |
| Group | Metadata for a group chat — name, icon, admin |
| GroupMember | A User's membership in a Group with their role |
| Media | A file (image, video, audio, document) attached to a message |
| MessageReceipt | Tracks Delivered / Read status per recipient per message |
| UserContact | A User's saved contacts list |
| Device | A registered device for a User (for push notifications) |

---

### Entity Relationships

```
User ──────────────< UserContact >────────────── User
  |                                                |
  |──────────────< ChatMember >──────────────── Chat
                                                   |
                                              ─────+─────
                                              |         |
                                           Direct     Group
                                                         |
                                                      GroupMember
                                                      (User + role)

Chat ──────────────< Message >──────────────────────────
                        |
                        |──────── Media (optional)
                        |
                        └──────── MessageReceipt (per recipient)

User ─────────────< Device >
(FCM token / APNs token per device)
```

---

### Relationship Summary

| Relationship | Type | Details |
|---|---|---|
| User to Chat | Many-to-Many | Through ChatMember table |
| Chat to Message | One-to-Many | One chat has many messages |
| Message to Media | One-to-One | Optional — only media messages |
| Message to MessageReceipt | One-to-Many | One receipt per recipient |
| User to Group | Many-to-Many | Through GroupMember table |
| User to Device | One-to-Many | One user can have multiple devices |
| User to UserContact | Many-to-Many | Saved phone contacts |

---

## 11. Database Schema

### users

| Column | Type | Notes |
|---|---|---|
| user_id | UUID | Primary Key |
| phone | VARCHAR(15) | Unique, used for lookup |
| name | VARCHAR(100) | Display name |
| profile_pic_url | TEXT | CDN URL |
| about | VARCHAR(139) | Status message |
| last_seen | TIMESTAMP | Updated on disconnect |
| created_at | TIMESTAMP | Account creation time |

---

### chats

| Column | Type | Notes |
|---|---|---|
| chat_id | UUID | Primary Key |
| chat_type | ENUM | DIRECT or GROUP |
| created_at | TIMESTAMP | |

---

### chat_members

| Column | Type | Notes |
|---|---|---|
| chat_id | UUID | FK → chats |
| user_id | UUID | FK → users |
| joined_at | TIMESTAMP | |
| PRIMARY KEY | (chat_id, user_id) | Composite |

---

### groups

| Column | Type | Notes |
|---|---|---|
| group_id | UUID | Primary Key |
| chat_id | UUID | FK → chats (one-to-one) |
| name | VARCHAR(100) | Group display name |
| description | TEXT | |
| icon_url | TEXT | CDN URL |
| created_by | UUID | FK → users |
| created_at | TIMESTAMP | |

---

### group_members

| Column | Type | Notes |
|---|---|---|
| group_id | UUID | FK → groups |
| user_id | UUID | FK → users |
| role | ENUM | MEMBER or ADMIN |
| joined_at | TIMESTAMP | |
| PRIMARY KEY | (group_id, user_id) | Composite |

---

### messages (Cassandra)

| Column | Type | Notes |
|---|---|---|
| chat_id | UUID | Partition key — all messages of a chat together |
| message_id | TIMEUUID | Clustering key — time-ordered UUID |
| sender_id | UUID | Who sent it |
| content | TEXT | Encrypted ciphertext only |
| media_id | UUID | FK → media (nullable) |
| message_type | ENUM | TEXT, IMAGE, VIDEO, AUDIO, DOCUMENT |
| status | ENUM | SENT, DELIVERED, READ |
| created_at | TIMESTAMP | |
| PRIMARY KEY | (chat_id, message_id) | Partition by chat, cluster by time |

> Cassandra chosen for messages — optimised for high write throughput and sequential time-ordered reads per chat.

---

### message_receipts

| Column | Type | Notes |
|---|---|---|
| message_id | UUID | FK → messages |
| user_id | UUID | FK → users (recipient) |
| status | ENUM | DELIVERED or READ |
| updated_at | TIMESTAMP | |
| PRIMARY KEY | (message_id, user_id) | Composite |

> Used for group messages — tracks delivery per member. Single grey tick = SENT, double grey = all DELIVERED, double blue = all READ.

---

### media

| Column | Type | Notes |
|---|---|---|
| media_id | UUID | Primary Key |
| uploader_id | UUID | FK → users |
| media_type | ENUM | IMAGE, VIDEO, AUDIO, DOCUMENT |
| original_url | TEXT | S3 URL (private) |
| cdn_url | TEXT | CDN URL (served to clients) |
| thumbnail_url | TEXT | CDN URL for preview |
| size_bytes | BIGINT | File size |
| created_at | TIMESTAMP | |

---

### devices

| Column | Type | Notes |
|---|---|---|
| device_id | UUID | Primary Key |
| user_id | UUID | FK → users |
| platform | ENUM | ANDROID or IOS |
| push_token | TEXT | FCM token or APNs token |
| last_active | TIMESTAMP | Last heartbeat received |

---

### user_contacts

| Column | Type | Notes |
|---|---|---|
| owner_id | UUID | FK → users (who saved the contact) |
| contact_id | UUID | FK → users (the saved contact) |
| nickname | VARCHAR(100) | Custom name given by owner |
| saved_at | TIMESTAMP | |
| PRIMARY KEY | (owner_id, contact_id) | Composite |

---

## 12. Message Delivery States

```
Client sends message
        |
        v
   [ SENDING ]        -- on device only, not reached server yet

Server receives
        |
        v
    [ SENT ]          -- ✓  single grey tick

Delivered to device
        |
        v
  [ DELIVERED ]       -- ✓✓ double grey tick

Recipient opens chat
        |
        v
    [ READ ]          -- ✓✓ double blue tick
```

### State Transition Rules

| From | To | Trigger |
|---|---|---|
| SENDING | SENT | Server ACK received |
| SENDING | FAILED | Network error / timeout |
| SENT | DELIVERED | Recipient device receives message |
| DELIVERED | READ | Recipient opens the chat |

### Group State Rules

| Tick | Condition |
|---|---|
| Single grey tick | Message reached server |
| Double grey tick | At least ONE member received it |
| Double blue tick | ALL members have read it |

---

## 13. End-to-End Encryption

WhatsApp uses the **Signal Protocol** (X3DH key agreement + Double Ratchet).

```
One-time setup:
  Each device generates a public/private key pair
  Public key uploaded to WhatsApp server
  Private key never leaves the device

Sending a message:
  Sender fetches receiver's public key from server
        |
        v
  Message encrypted with receiver's public key
        |
        v
  Encrypted ciphertext sent to server
  (Server cannot read it — no private key)
        |
        v
  Server stores and forwards ciphertext
        |
        v
  Receiver decrypts with their own private key ✅

Result:
  Server stores only ciphertext
  WhatsApp has zero access to message content
  Even if DB is breached — data is unreadable
```

### Key Properties

| Property | Detail |
|---|---|
| Algorithm | X3DH (Extended Triple Diffie-Hellman) key agreement |
| Message encryption | AES-256-GCM |
| Forward secrecy | Double Ratchet — new key per message, old keys deleted |
| Key storage | Private key on device only, never transmitted |
| Push notification | Contains no message content — only sender ID |

---

## 14. API Design

### Send Message

```
POST /api/v1/messages

Headers:
  Authorization: Bearer <jwt_token>
  Idempotency-Key: <uuid>

Body:
  chat_id      → UUID of the conversation
  content      → encrypted ciphertext
  type         → TEXT / IMAGE / VIDEO / AUDIO / DOCUMENT
  media_id     → UUID (nullable, for media messages)

Response 201:
  message_id   → server assigned UUID
  status       → SENT
  created_at   → timestamp
```

---

### Get Chat History

```
GET /api/v1/chats/{chat_id}/messages

Query params:
  limit        → number of messages (default 50)
  before       → message_id for pagination (cursor-based)

Response 200:
  messages[]   → list of messages, newest first
  has_more     → boolean for next page
```

---

### Upload Media

```
POST /api/v1/media/upload

Body: multipart/form-data
  file         → binary file content
  type         → IMAGE / VIDEO / AUDIO / DOCUMENT

Response 200:
  media_id     → UUID
  cdn_url      → URL to embed in message
  thumbnail_url→ preview URL
```

---

### Get User Presence

```
GET /api/v1/users/{user_id}/presence

Response 200:
  status       → ONLINE or OFFLINE
  last_seen    → timestamp (if OFFLINE)
```

---

### WebSocket Events

| Event | Direction | Payload |
|---|---|---|
| NEW_MESSAGE | Server → Client | message_id, chat_id, sender_id, content, type |
| MESSAGE_DELIVERED | Server → Client | message_id, recipient_id |
| MESSAGE_READ | Server → Client | message_id, recipient_id |
| PRESENCE_UPDATE | Server → Client | user_id, status |
| HEARTBEAT | Client → Server | user_id (every 30s) |
| TYPING | Client → Server | chat_id, user_id |
| TYPING_INDICATOR | Server → Client | chat_id, user_id |

---

## 15. 2-Minute Interview Answer

> *"WhatsApp is a real-time messaging system built around three core components — WebSocket servers for persistent connections, Kafka for async delivery, and Cassandra for message storage.*
>
> *When a user sends a message, it hits a WebSocket server which passes it to the Message Service. The message is written to Cassandra partitioned by chat_id for fast sequential reads, then published to Kafka. If the receiver is online on a different WebSocket server, Redis Pub/Sub routes it across. If offline, a push notification is sent via FCM or APNs — with no message content in the payload for E2E encryption compliance.*
>
> *For group messages I store the message once against the group chat_id — not per member — and fan out delivery tasks per member through Kafka. This avoids storing trillions of duplicate rows.*
>
> *Media is uploaded directly to S3 via pre-signed URLs and served through CDN. The message carries only the CDN URL. Online presence uses Redis keys with a 60-second TTL refreshed by heartbeat pings.*
>
> *Security uses the Signal Protocol — private keys never leave the device, the server stores only ciphertext and cannot read any message content."*

---

## Quick Reference

```
Real-time delivery      → WebSocket
Cross-server routing    → Redis Pub/Sub
Async processing        → Kafka
Message storage         → Cassandra (partition by chat_id)
Presence tracking       → Redis TTL + heartbeat
Media storage           → S3 + CDN
Push notifications      → FCM (Android) / APNs (iOS)
Encryption              → Signal Protocol (X3DH + AES-256-GCM)
Group fan-out           → Store once, deliver per member via Kafka
Delivery receipts       → Single grey → Double grey → Double blue
```

---

*Last updated: 2025*
