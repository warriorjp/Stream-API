# Core Concepts of System Design

1. **Latency:** How fast a system responds to a request. Lower latency means faster response.
2. **Throughput:** How many requests a system can handle at once. Higher throughput means more work in less time.
3. **Scale:** Ability of the system to handle growth in users and traffic.
4. **Consistency:** Ensures users see accurate and up-to-date data across the system.
5. **Availability:** System remains accessible and operational even during failures.


<div style="margin-left:3rem">
   <img src="./images/System_Design_Intro.png" width="400" />
</div>

# Scale from Zero to Millions of Users

## Introduction
Scaling a system to support millions of users is a complex, iterative journey requiring refinement and optimization. This chapter outlines how to begin with a single server setup and scale the architecture step by step to handle millions of users.

---

## Single Server Setup
Initially, all components (web app, database, cache) run on a single server. 

<div style="margin-left:3rem">
   <img src="./images/single-server.png" width="400" />
</div>

### Request Flow
1. Users access the application via domain names (e.g., `api.mysite.com`), resolved to IP addresses using DNS.
2. IP address of the web-server is returned to the browser or mobile app.
3. HTTP requests are sent to the web server, which returns HTML or JSON responses.

### Traffic Sources
1. **Web Applications:** Use server-side languages (e.g., Python, Java) for business logic and client-side languages (e.g., JavaScript, HTML) for presentation.
2. **Mobile Applications:** Communicate with the web server using HTTP and JSON for lightweight data exchange.

---


## Load Balancer

<div style="margin-left:3rem">
   <img src="./images/load-balancer.png" width="400" />
</div>

A **load balancer** distributes traffic among multiple servers. Benefits include:
1. Redundancy: If a server goes offline, traffic is rerouted.
   -  If server 1 goes offline, all the traffic will be routed to server 2.
2. Scalability: Easily add servers to handle traffic spikes.
   -  If the website traffic grows rapidly, subsequent servers can be added to handle the additional traffic.

## Caching
A **cache** stores frequently accessed data in memory to reduce database load. The cache tier is a temporary data store layer, much faster than the database. 

<div style="margin-left:3rem">
   <img src="./images/cache.png" width="500" />
</div>

### Caching considerations
1. **Use case**: Consider using cache when data is read frequently but modified infrequently.
2. **Expiration Policies:** Once cached data is expired, it is removed from the cache. When there is no expiration policy, cached
data will be stored in the memory permanently.
3. **Consistency:** This means keeping the data store and the cache in sync. Inconsistency
can happen because data-modifying operations on the data store and cache are not in a single transaction. 
4. **Mitigating failures**: A single cache server represents a potential single point of failure, multiple
cache servers across different data centers are recommended to avoid SPOF.
5. **Eviction Policies:**: Once the cache is full, items need to be evicted to free up memory. LRU is the most popular cache eviction policy.

---

## Content Delivery Network (CDN)
A **CDN** improves load times by caching static content (images, CSS, JavaScript) on geographically distributed servers.

<div style="margin-left:3rem">
   <img src="./images/cdn.png" width="400" />
</div>

### Workflow
1. User requests content from the nearest CDN server.
2. If unavailable, content is fetched from the origin server and cached.


### CDN considerations
1. **Cost:** CDNs are run by third-party providers which charge for data transfers in and out of the CDN.
2. **Cache Expiry:** The cache expiry time should neither be too long nor too short.
3. **CDN fallback:** If there is a temporary CDN outage, clients should be able to detect the problem
and request resources from the origin.
4. **Invalidating files:** If files are updated the cache should be invalidated to point to the updated files.

---

## TCP VS UDP

<div style="margin-left:3rem">
   <img src="./images/TCP-UDP.png" width="600" />
</div>

---

### Stateless
- Server does not store client session context between requests
- Every request carries everything needed to process it
- Easier to scale horizontally
- Easier to load balance

Example: most REST APIs

### Stateful
- Server keeps client-specific context across requests
- Later requests depend on what happened before
- Harder to scale and rebalance
- Needs sticky sessions or shared state

Example: WebSocket connections, game sessions

<div style="margin-left:3rem">
   <img src="./images/Stateless-Stateful.png" width="600" />
</div>

---

## SAGA

<div style="margin-left:3rem">
   <img src="./images/SAGA.png" width="600" />
</div>

 **Two Types of Saga Pattern :**

**1️⃣ Choreography Saga**

Services communicate through events directly.


**Example:**

 - Order Service → publishes event

 - Payment Service → listens & processes

 - Restaurant Service → listens & processes

Each service reacts independently.

 - ✅ Simple to implement

 - ✅ No central controller

 - ❌ Hard to debug in large systems

 - ❌ Complex event chains

Best for:
 ✔️ Small/medium microservices systems

**2️⃣ Orchestration Saga**

 A central Saga Orchestrator controls the workflow.

**Example:**

Saga Orchestrator

 - Trigger Payment Service

 - Trigger Restaurant Service

 - Trigger Delivery Service

If failure occurs:

Orchestrator decides rollback steps.

  - ✅ Centralized control

  - ✅ Easier monitoring & debugging

  - ✅ Better for enterprise systems

 ❌ Extra orchestration service required


Best for:

 - Large enterprise systems


---

###Optimistic locking### 
Assumes conflicts are rare. Both users read the data without acquiring any lock. Each record carries a version number. When a user attempts to write, the database checks: does the version in your update match the current version in the database? If another transaction already incremented the version from 1 to 2, your update still references version 1. The write is rejected.

##Pessimistic locking###
 Takes the opposite approach. It assumes conflicts are likely, so it blocks them before they happen. The first transaction locks the row, and every other transaction waits until that lock is released. No version checks needed.

If your system is read-heavy with occasional writes, optimistic locking is the best option. When concurrent writes occur frequently and the cost of a conflict is high, pessimistic locking is the safer choice.

<div style="margin-left:3rem">
   <img src="./images/Locking-Mechanism.png" width="600" />
</div>

---
##Caching##
Caching means storing frequently used data in a temporary fast storage so that next time we don’t need to fetch it again from the main database or service.

Instead of hitting DB every time:

App → Cache → Database

- If data is found in cache → return fast

- If not found → fetch from DB and store in cache

- This is called Cache Hit and Cache Miss

**1.Cache Aside (Lazy Loading) ← Most Common**

- Cache-aside, or lazy loading, is a strategy where the application loads data into the cache only on demand. 

- If data isn't in the cache, the app fetches it from the database, returns it, and stores it in the cache for next time. 

- This ensures only frequently used (hot) data gets cached. 

- The downside is the first request for any item will hit the database (slow), but subsequent requests are served quickly from the cache.

**2.Write-Through Caching**

- In a write-through strategy, whenever data is updated, it's written to the database and the cache at the same time. This keeps the cache up-to-date, so reads will always get fresh values from cache. 

- The benefit is consistency and no cache misses on recent writes, but the drawback is extra write overhead – each write does double work and might cache data that never gets read.

**1. Browser Cache **

Frontend side

Example:

- CSS
- JS
- Images

**2. CDN Cache**

Stores content near user location

Example:

- Amazon CloudFront

**3. Application Cache**

- Inside service

Example:

- Redis
- Ehcache
- Hazelcast

**4. Database Cache**

DB internal caching

Example:

- MongoDB query cache


<div style="margin-left:3rem">
   <img src="./images/Redis.png" width="800" />
</div>

---

##CAP Theorem (System Design)##

In a Distributed System, you can guarantee only 2 out of these 3 at the same time:

C → Consistency 

A → Availability

P → Partition Tolerance

You cannot perfectly achieve all 3 together.

** Example **

- You have 2 ATM machines connected to same bank server.

- If one ATM updates balance and network issue happens:

 **Now system must choose:**

- Show latest correct balance (Consistency)

OR

- Always give response even if old data (Availability)

 Since network failure exists (Partition), both perfect consistency + availability together is difficult.

