# E-Commerce Platform - HLD / LLD (Amazon / Flipkart Style)

## Table of Contents
- [Requirements](#requirements)
- [High Level Design (HLD)](#high-level-design-hld)
- [Microservices Breakdown](#microservices-breakdown)
- [Low Level Design (LLD)](#low-level-design-lld)
- [Database Design](#database-design)
- [Key System Design Decisions](#key-system-design-decisions)
- [Non-Functional Requirements](#non-functional-requirements)
- [Interview Quick Reference](#interview-quick-reference)

---

## Requirements

### Functional Requirements

```
| **Feature**              | **Description**                              |
|--------------------------|----------------------------------------------|
| User Management          | Register, login, profile management          |
| Product Catalog          | Browse, search, filter products              |
| Cart                     | Add, update, remove items                    |
| Order Management         | Place, track, cancel orders                  |
| Payment                  | Multiple payment methods, refunds            |
| Inventory                | Real-time stock tracking                     |
| Notifications            | Email, SMS, push for order updates           |
| Reviews and Ratings      | Product reviews by verified buyers           |
| Recommendations          | Personalized product suggestions             |
```

### Non-Functional Requirements

```

| **Property**             | **Target**                                          |
|--------------------------|-----------------------------------------------------|
| Availability             | 99.99% uptime                                       |
| Latency                  | Product search < 200 ms, checkout < 500 ms          |
| Scalability              | 10M+ daily active users, 100K+ orders/day           |
| Consistency              | Strong consistency for payments and inventory       |
| Eventual Consistency     | Acceptable for catalog, reviews, and recommendations|

```

---

## High Level Design (HLD)

### System Architecture Overview

```
                        Client (Web / Mobile App)
                                  |
                          API Gateway / BFF
                    (Auth, Rate Limiting, Routing)
                                  |
          +----------+-----------+-----------+----------+
          |          |           |           |          |
      User       Product      Order      Payment   Notification
     Service     Service     Service     Service    Service
          |          |           |           |          |
       MySQL    Elasticsearch  MySQL      Stripe    Kafka
       Redis    + MongoDB      Redis     / Razorpay  + SES
                               Kafka               + SNS
                                  |
                           Inventory Service
                                  |
                              MySQL + Redis
```

### Request Flow - Place an Order

```
User clicks "Buy Now"
        |
API Gateway (validate JWT token)
        |
Order Service
  - validate cart items
  - check inventory (Inventory Service)
  - create order in PENDING state
        |
Payment Service
  - initiate payment gateway
  - on success -> publish ORDER_PAID event to Kafka
        |
Kafka Topics:
  order.paid -> Inventory Service (deduct stock)
  order.paid -> Notification Service (send confirmation)
  order.paid -> Fulfillment Service (trigger shipment)
        |
Order status updated to CONFIRMED
```

---

## Microservices Breakdown

### 1. User Service
- Register / Login / OAuth (Google, Facebook)
- JWT token issuance and refresh
- Profile management, address book
- **DB:** MySQL (user data) + Redis (session / token cache)

### 2. Product Catalog Service
- CRUD for products, categories, images
- Search, filter, sort
- **DB:** MongoDB (flexible product schema) + Elasticsearch (search)
- **Cache:** Redis for hot products (TTL 5 min)

### 3. Cart Service
- Add, update, remove items
- Price recalculation on cart fetch
- **DB:** Redis (cart stored as hash per user, TTL 7 days)
- No permanent DB - cart is transient

### 4. Order Service
- Create, update, cancel orders
- Order state machine management
- **DB:** MySQL (strong consistency for order records)
- **Cache:** Redis for order status lookups
- **Events:** Publishes to Kafka on state changes

### 5. Inventory Service
- Real-time stock management
- Reserve stock on order placement, deduct on payment
- **DB:** MySQL with row-level locking
- **Cache:** Redis for available stock count

### 6. Payment Service
- Integrates with Stripe / Razorpay / PayPal
- Handles payment initiation, webhooks, refunds
- Idempotency keys to prevent duplicate charges
- **DB:** MySQL (payment records, immutable)

### 7. Notification Service
- Consumes Kafka events
- Sends Email (SES), SMS (SNS / Twilio), Push notifications
- **DB:** MongoDB (notification logs)

### 8. Search Service
- Product search, autocomplete, filters
- Synced from Product Catalog via Kafka
- **DB:** Elasticsearch

### 9. Recommendation Service
- Collaborative filtering, purchase history based
- Runs ML models, exposes REST API
- **DB:** Redis (pre-computed recommendations per user)

---

## Low Level Design (LLD)

### Order State Machine

```
CART_CHECKOUT
      |
  PENDING (order created, awaiting payment)
      |
   PAID (payment confirmed)
      |
  CONFIRMED (inventory reserved)
      |
  SHIPPED (dispatched from warehouse)
      |
 DELIVERED (delivery confirmed)
      |
[CANCELLED] <-- can happen from PENDING or CONFIRMED
[RETURN_REQUESTED] <-- after DELIVERED
[REFUNDED] <-- after RETURN_REQUESTED
```

### Inventory - Prevent Overselling

Two-phase approach to prevent race conditions:

```
Phase 1 - Soft Reserve (Order placement):
  UPDATE inventory
  SET reserved = reserved + quantity
  WHERE product_id = ?
  AND (available - reserved) >= quantity

Phase 2 - Hard Deduct (Payment success):
  UPDATE inventory
  SET available = available - quantity,
      reserved = reserved - quantity
  WHERE product_id = ?

Phase 3 - Release Reserve (Payment failure / timeout):
  UPDATE inventory
  SET reserved = reserved - quantity
  WHERE product_id = ?
```

### Payment Idempotency

```
Client generates idempotency_key = UUID (per order)
        |
Payment Service checks Redis:
  - key exists + status = SUCCESS -> return cached response
  - key exists + status = PROCESSING -> return 202 wait
  - key not found -> process payment, store result in Redis (TTL 24h)
```

### Cart Design (Redis)

```
Key:   cart:{userId}
Type:  Redis Hash
Field: productId -> JSON { qty, price, name, sellerId }

HSET cart:user123 prod456 '{"qty":2,"price":999,"name":"iPhone"}'
EXPIRE cart:user123 604800   # 7 days TTL
```

### Product Search Flow (Elasticsearch)

```
User types "red nike shoes size 10"
        |
Search Service
  - bool query:
      must:   multi_match on name, description, tags
      filter: term on category="shoes", brand="nike"
      filter: term on size=10, color=red
      filter: range on price, in_stock=true
        |
  - Results ranked by BM25 + popularity boost
        |
  - Facets returned via aggregations (brand, size, price range)
```

---

## Database Design

### Users Table (MySQL)

```sql
CREATE TABLE users (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  email       VARCHAR(255) UNIQUE NOT NULL,
  phone       VARCHAR(20),
  password    VARCHAR(255),
  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE addresses (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT NOT NULL,
  line1       VARCHAR(255),
  city        VARCHAR(100),
  pincode     VARCHAR(10),
  is_default  BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Orders Table (MySQL)

```sql
CREATE TABLE orders (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id         BIGINT NOT NULL,
  status          ENUM('PENDING','PAID','CONFIRMED',
                       'SHIPPED','DELIVERED','CANCELLED','REFUNDED'),
  total_amount    DECIMAL(10,2),
  address_id      BIGINT,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_status (status)
);

CREATE TABLE order_items (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id    BIGINT NOT NULL,
  product_id  BIGINT NOT NULL,
  seller_id   BIGINT NOT NULL,
  quantity    INT,
  unit_price  DECIMAL(10,2),
  FOREIGN KEY (order_id) REFERENCES orders(id)
);
```

### Inventory Table (MySQL)

```sql
CREATE TABLE inventory (
  product_id  BIGINT PRIMARY KEY,
  available   INT NOT NULL DEFAULT 0,
  reserved    INT NOT NULL DEFAULT 0,
  updated_at  TIMESTAMP,
  CONSTRAINT chk_stock CHECK (available >= 0 AND reserved >= 0)
);
```

### Product Document (MongoDB)

```json
{
  "_id": "prod_456",
  "name": "Nike Air Max 270",
  "brand": "Nike",
  "category": "shoes",
  "description": "Lightweight running shoes...",
  "price": 8999,
  "images": ["url1", "url2"],
  "attributes": {
    "color": "red",
    "sizes": [7, 8, 9, 10, 11],
    "material": "mesh"
  },
  "ratings": { "average": 4.3, "count": 1820 },
  "seller_id": "seller_789",
  "created_at": "2024-01-15T10:00:00Z"
}
```

---

## Key System Design Decisions

### 1. Caching Strategy
```
| **Data**              | **Cache** | **TTL** | **Reason**                              |
|-----------------------|-----------|---------|-----------------------------------------|
| Product Details       | Redis     | 5 min   | High read frequency, low write frequency |
| Shopping Cart         | Redis     | 7 days  | Temporary user data requiring fast access |
| Inventory Count       | Redis     | 30 sec  | Near real-time stock availability        |
| User Session / JWT    | Redis     | 24 hrs  | Fast authentication and session lookup   |
| Search Results        | Redis     | 2 min   | Reduce expensive search queries          |
| Recommendations       | Redis     | 1 hr    | Cache pre-computed recommendation data   |
```

### 2. Kafka Topics
```

| **Kafka Topic**     | **Producer**      | **Consumer(s)**                      |
|---------------------|-------------------|--------------------------------------|
| order.created       | Order Service     | Inventory Service, Notification Service |
| order.paid          | Payment Service   | Order Service, Inventory Service, Fulfillment Service |
| order.shipped       | Fulfillment Service | Notification Service               |
| order.delivered     | Fulfillment Service | Notification Service, Review Service |
| order.cancelled     | Order Service     | Inventory Service (Release Stock), Notification Service |
| product.updated     | Product Service   | Search Service (Elasticsearch Sync) |

```

### 3. Handling Flash Sales (High Traffic)

```
Normal flow:                    Flash Sale flow:
                                  |
User -> API -> Inventory    User -> Queue (Redis List)
                                  |
                            Rate-limited dequeue
                                  |
                            Batch inventory deduction
                                  |
                            Async order confirmation
```

- Use Redis DECR with Lua script for atomic stock decrement
- Queue overflow requests, process at controlled rate
- Show "Only 5 left!" using approximate Redis count (not DB)

### 4. Database Sharding Strategy
```
| **Service**       | **Sharding Key** | **Sharding Strategy** | **Reason**                                      |
|-------------------|------------------|-----------------------|-------------------------------------------------|
| Order Service     | `user_id`        | Hash / Range          | Distributes user orders evenly across shards    |
| Product Service   | `category_id`    | Range                 | Groups products by category for efficient queries |
| Inventory Service | `product_id`     | Hash                  | Evenly distributes inventory records            |
| Payment Service   | `order_id`       | Hash                  | Ensures uniform distribution of payment records |
```


### 5. Search Sync - DB to Elasticsearch

```
Product Service (write to MongoDB)
        |
Debezium CDC connector
        |
Kafka topic: product.updated
        |
ES Consumer (reads Kafka, indexes to Elasticsearch)
        |
Elasticsearch index updated (eventual consistency ~1-2s)
```

---

## Non-Functional Requirements

### Scalability

```
Load Balancer (AWS ALB)
        |
API Gateway cluster (horizontal scale)
        |
Microservices (Docker + Kubernetes)
  - HPA (Horizontal Pod Autoscaler) based on CPU / RPS
        |
Databases
  - MySQL: Read replicas for heavy read services
  - MongoDB: Replica sets + sharding
  - Redis: Redis Cluster mode
  - Elasticsearch: Multi-node cluster
```

### Availability and Fault Tolerance

```
| **Reliability Strategy**              | **Applied To**                            | **Purpose**                                               |
|---------------------------------------|-------------------------------------------|-----------------------------------------------------------|
| Circuit Breaker (Resilience4j)        | All inter-service API calls               | Prevent cascading failures and improve fault tolerance    |
| Retry with Exponential Backoff        | Payment Service, Inventory Service        | Retry transient failures while avoiding system overload   |
| Dead Letter Queue (DLQ)               | All Kafka consumers                       | Store failed messages for later analysis and reprocessing |
| Database Read Replicas                | Product Service, Order Service            | Scale read operations and reduce database load            |
| Multi-AZ Deployment                   | All stateful services                     | Ensure high availability during zone failures             |
| Health Checks & Auto-Restart          | Kubernetes Liveness & Readiness Probes    | Automatically detect and recover unhealthy containers     |
```

### CDN and Static Assets

```
Product Images -> S3 (origin)
              -> CloudFront CDN (edge cached globally)
              -> Client browser (cache-control: 7 days)
```

---

## Interview Quick Reference

```
| Question                    | Answer
|-----------------------------|--------------------------------------------------------------------------------------------|

| How to prevent overselling? | Two-phase reserve: soft reserve at order, hard deduct at payment; use DB row-level locking |

| How to handle flash sales? | Redis DECR with Lua for atomic decrement; queue excess requests; async confirmation |

| Cart storage choice? | Redis Hash per user; fast, TTL-based auto-expiry, no DB needed |

| How to keep ES in sync? | CDC via Debezium + Kafka; eventual consistency ~1-2s acceptable for catalog |

| Payment idempotency? | Client-generated UUID idempotency key; check Redis before processing |

| Order state management? | State machine with explicit transitions; only Kafka events trigger state changes |

| How to scale product search? | Elasticsearch with BM25 + Redis cache for hot queries |

| DB for orders vs products? | Orders: MySQL (ACID, strong consistency); Products: MongoDB (flexible schema) |

| How to handle payment failure? | Release inventory reserve, update order to FAILED, notify user, allow retry |

| Monolith vs Microservices here? | Microservices: independent scaling of Search, Inventory, Payment; fault isolation |

| How to recommend products? | Collaborative filtering on purchase history; pre-compute in batch, serve from Redis |

| Handling returns and refunds? | Order state: RETURN_REQUESTED -> REFUNDED; Kafka event triggers payment reversal and inventory restore |

```
