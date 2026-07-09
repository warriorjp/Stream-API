# 🗄️ Distributed Caching System — System Design

---

## Table of Contents

1. [Requirements](#1-requirements)
2. [High-Level Architecture](#2-high-level-architecture)
3. [Request Flow](#3-request-flow)
4. [Cache Distribution — Consistent Hashing](#4-cache-distribution--consistent-hashing)
5. [Cache Replication](#5-cache-replication)
6. [High Availability](#6-high-availability)
7. [Cache Eviction Strategies](#7-cache-eviction-strategies)
8. [Common Problems & Solutions](#8-common-problems--solutions)
9. [Redis-Based Production Architecture](#9-redis-based-production-architecture)
10. [2-Minute Interview Answer](#10-2-minute-interview-answer)

---

## 1. Requirements

### ✅ Functional
- Store frequently accessed data to reduce database load
- Fast reads and writes
- Cache expiration via **TTL** (Time To Live)
- Cache invalidation on data updates

### ⚙️ Non-Functional

| Property          | Target                     |
|-------------------|----------------------------|
| Latency           | < 10 ms                    |
| Availability      | High (99.99%)              |
| Scalability       | Horizontal scaling         |
| Fault Tolerance   | No single point of failure |

---

## 2. High-Level Architecture

```
                        Client
                           │
                           ▼
                      Application
                           │
                           ▼
                  Distributed Cache
                 /         │         \
                ▼          ▼          ▼
            Cache-1     Cache-2     Cache-3
                 \         │         /
                  \        │        /
                           ▼
                        Database
```

- The application **always checks the cache first** (Cache-Aside pattern)
- Cache nodes share the load — no single node holds all data
- Database is the **source of truth** — only queried on cache miss

## L1 Cache :

In memory cache inside the application

Extremely fast

## L2 Cache :

Distributed cache shared across multiple application instance.

Faster then querying database

## L3 Cache :
Hardware cache inside the CPU.

Stores frequently accessed memory data. 


---

## 3. Request Flow

### ✅ Cache Hit — Fast Path

```
Client
  │
  ▼
Application
  │
  ▼
Cache ──► Key Found
              │
              ▼
         Return Data
         (milliseconds)
```

### ❌ Cache Miss — Slow Path

```
Client
  │
  ▼
Application
  │
  ▼
Cache ──► Key Not Found
              │
              ▼
           Database
              │
              ▼
        Store in Cache
              │
              ▼
         Return Data
```

> The **Cache-Aside** (Lazy Loading) pattern: the application manages cache population.
> Alternatively, **Write-Through** writes to cache and DB simultaneously on every write.

---

## 4. Cache Distribution — Consistent Hashing

### The Problem
With `n` cache nodes, a naive `key % n` hash means that **adding or removing a node reshuffles almost every key** — causing a thundering herd on the database.

### The Solution — Consistent Hashing

```
              Cache-1
                 │
                 │
  Cache-3 ──────┼────── Cache-2
                 │
                 │
                Key
```

Keys and cache nodes are both mapped onto a **virtual ring**. Each key is assigned to the nearest node clockwise on the ring.

**Benefits:**

| Benefit | Details |


| Minimal key movement | Only `K/n` keys are remapped when a node is added or removed |

| Easy horizontal scaling | Add nodes to the ring without reshuffling everything |

| Better load balancing | Virtual nodes (vnodes) distribute load evenly |


```
Ring:  [Cache-1] ── [Cache-2] ── [Cache-3] ── (wraps back)

Key "user:123" hashes to a point on the ring
→ assigned to the next node clockwise (e.g., Cache-2)

If Cache-2 is removed:
→ only Cache-2's keys move to Cache-3
→ Cache-1's keys are untouched ✅
```

---

## 5. Cache Replication

### The Problem — Single Node is Risky

```
Cache-1
   │
 Fails
   │
Data Lost ❌
```

### The Solution — Primary + Replicas

```
         Primary (Cache-1)
               │
       ┌───────┴───────┐
       ▼               ▼
   Replica-1       Replica-2
```

- **Primary** handles all writes
- **Replicas** handle reads (read scaling) and serve as failover
- If the primary fails, a replica is **automatically promoted** to primary

> Redis Sentinel and Redis Cluster both support automatic failover with replica promotion.

---

## 6. High Availability

### Redis Cluster — Automatic Failover

```
              Redis Cluster
     ┌─────────────┬─────────────┐
     ▼             ▼             ▼
   Node-1        Node-2        Node-3
  (Master)      (Master)      (Master)
     │             │             │
     ▼             ▼             ▼
  Replica-1    Replica-2    Replica-3
```

**On node failure:**
```
Node-1 fails
    │
    ▼
Replica-1 promoted to Master
    │
    ▼
System continues serving requests ✅
New replica spun up in background
```

**Redis Cluster key points:**
- Data is sharded across masters using **hash slots** (16,384 total)
- Each master owns a range of slots
- Minimum recommended setup: **3 masters + 3 replicas**

---

## 7. Cache Eviction Strategies

Memory is finite — when the cache is full, old entries must be removed.

### LRU — Least Recently Used *(Most Common)*

```
Cache State:  [A] [B] [C] [D]  ← D is oldest unused
New key E arrives, cache full
    │
    ▼
Evict D (least recently used)
Cache State:  [A] [B] [C] [E]
```

> Best when recent data is most likely to be accessed again (typical web traffic).

---

### LFU — Least Frequently Used

```
Cache State:
  A → accessed 50 times
  B → accessed  3 times  ← least frequently used
  C → accessed 20 times

New key D arrives, cache full
    │
    ▼
Evict B (least frequently accessed)
```

> Best when some data is consistently hot (e.g., homepage, top products).

---

### TTL — Time To Live

```
SET user:123  "John"  TTL = 5 mins
        │
        ▼
After 5 minutes:
Key automatically expires and is removed ✅
```

> Always set a TTL on cache entries to prevent stale data accumulating indefinitely.

**Redis eviction policy configuration:**
```
# In redis.conf
maxmemory 2gb

maxmemory-policy allkeys-lru   # or allkeys-lfu, volatile-lru, etc.
```

| Policy | Behavior |


| `allkeys-lru` | Evict any key by LRU (recommended general use) |

| `allkeys-lfu` | Evict any key by LFU |

| `volatile-lru` | Only evict keys with a TTL set |

| `noeviction` | Return error when memory full (use with caution) |

---

## 8. Common Problems & Solutions

### 🔴 Cache Stampede (Thundering Herd)

**Problem:**

```

Popular key expires

       │
       ▼
1000 concurrent requests arrive

       │
       ▼
All hit the database simultaneously ❌
Database overloaded
```

**Solution — Distributed Lock:**

```
Popular key expires

       │
       ▼
1000 requests arrive

       │
       ▼
Only Request-1 acquires lock → rebuilds cache
Requests 2–1000 wait or serve stale data

       │
       ▼
Cache rebuilt ✅  Lock released
All subsequent requests served from cache
```

Implementation with Redis:

```java
String lockKey = "lock:user:123";
boolean locked = redis.setnx(lockKey, "1", 10, TimeUnit.SECONDS); // Atomic lock

if (locked) {
    try {
        String data = database.fetch("user:123");
        redis.set("user:123", data, 5, TimeUnit.MINUTES);
    } finally {
        redis.del(lockKey); // Always release lock
    }
} else {
    // Return stale data or wait briefly and retry
    return redis.get("user:123");
}
```

---

### 🔴 Cache Penetration

**Problem:** Requests for keys that **don't exist** in cache or DB (e.g., invalid IDs) — every request hits the DB.

**Solution — Bloom Filter:**

```
Request for user:999 (doesn't exist)
       │
       ▼
Bloom Filter check ──► Definitely not in DB
       │                      │
       ▼                      ▼
  If "maybe exists"     Return null immediately
  check cache/DB        (no DB hit) ✅
```

```java
BloomFilter<String> bloomFilter = BloomFilter.create(
    Funnels.stringFunnel(Charset.defaultCharset()),
    1_000_000,  // Expected insertions
    0.01        // 1% false positive rate
);

// On startup — populate with all valid keys
bloomFilter.put("user:123");

// On request
if (!bloomFilter.mightContain("user:999")) {
    return null; // Definitely doesn't exist — skip DB
}
```

---

### 🔴 Cache Avalanche

**Problem:** Many keys expire at the **same time** → massive simultaneous DB load.

**Solution:** Add random jitter to TTL values:

```java
// ❌ All keys expire at the same time
redis.set(key, value, 60, TimeUnit.MINUTES);

// ✅ Spread expiration across a window
int jitter = ThreadLocalRandom.current().nextInt(0, 300); // 0–5 min jitter
redis.set(key, value, 60 * 60 + jitter, TimeUnit.SECONDS);
```

---

## 9. Redis-Based Production Architecture

```
                      Load Balancer
                            │
          ┌─────────────────┼─────────────────┐
          ▼                 ▼                 ▼
        App-1             App-2             App-3
          │                 │                 │
          └─────────────────┼─────────────────┘
                            │
                            ▼
                      Redis Cluster
          ┌─────────────────┼─────────────────┐
          ▼                 ▼                 ▼
       Master-1          Master-2          Master-3
      (Slots 0–5460) (Slots 5461–10922) (Slots 10923–16383)
          │                 │                 │
          ▼                 ▼                 ▼
       Replica-1         Replica-2         Replica-3
                            │
                            ▼
                        PostgreSQL
                      (Source of Truth)
```

**Component responsibilities:**

| Component | Role |

| Load Balancer | Distributes traffic across app instances |

| App-1/2/3 | Check cache first; fall back to DB on miss |

| Redis Masters | Handle writes; own hash slot ranges |

| Redis Replicas | Handle reads; auto-promote on master failure |

| PostgreSQL | Persistent source of truth |


**Cache-Aside pattern (application code):**

```java
public User getUser(String userId) {
    // 1. Check cache
    String cached = redis.get("user:" + userId);
    if (cached != null) {
        return deserialize(cached); // Cache hit ✅
    }

    // 2. Cache miss — fetch from DB
    User user = database.findById(userId);

    // 3. Populate cache with TTL
    redis.set("user:" + userId, serialize(user), 30, TimeUnit.MINUTES);

    return user;
}

public void updateUser(String userId, User user) {
    database.save(user);             // Update DB first
    redis.del("user:" + userId);     // Invalidate cache (delete > update)
}
```

---

## 10. 2-Minute Interview Answer

> *"I would design a distributed cache using **Redis Cluster**. Applications follow the **Cache-Aside pattern** — always checking the cache before querying the database. Data is distributed across cache nodes using **Consistent Hashing**, which minimizes key redistribution when nodes are added or removed.*
>
> *For **high availability**, each cache master has replicas with automatic failover — if a master goes down, its replica is promoted automatically. Cache entries use **TTL** for expiration and **LRU eviction** when memory is full.*
>
> *To handle **cache stampedes** (thundering herd when a popular key expires), I use distributed locking so only one request rebuilds the cache. To prevent **cache penetration** (requests for non-existent keys hitting the DB), I use a **Bloom Filter** at the edge. And to avoid **cache avalanche** (mass simultaneous expiration), I add random TTL jitter.*
>
> *The system scales horizontally by adding Redis nodes — Consistent Hashing ensures minimal key movement. Write operations update the database first and then invalidate the cache entry to prevent stale reads."*

---

### Key Numbers to Remember

| Metric | Value |

| Target read/write latency | < 10 ms |

| Redis typical latency | < 1 ms |

| Redis Cluster hash slots | 16,384 |

| Minimum cluster setup | 3 masters + 3 replicas |

| Bloom filter false positive | ~1% (tunable) |

