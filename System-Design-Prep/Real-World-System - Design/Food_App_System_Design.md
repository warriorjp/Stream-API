# Food Delivery Platform - HLD / LLD (Zomato / Swiggy / Uber Eats Style)

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
| **Feature**              | **Description**                                         |
|--------------------------|---------------------------------------------------------|
| User Management          | Register, login, and address management                 |
| Restaurant Discovery     | Browse, search, and filter restaurants by location      |
| Menu Management          | Restaurant owners manage menus, pricing, and availability |
| Cart & Ordering          | Add items, place orders, and apply coupons              |
| Payment                  | UPI, credit/debit cards, wallets, and Cash on Delivery (COD) |
| Order Tracking           | Real-time delivery partner location tracking on a map   |
| Delivery Assignment      | Automatically assign the nearest available delivery partner |
| Ratings & Reviews        | Rate restaurants and delivery partners after delivery   |
| Notifications            | Order status updates via push notifications, SMS, and email |
| Surge Pricing            | Dynamic pricing based on demand and supply              |

```

### Non-Functional Requirements

```
| **Property**        | **Target**                                                      |
|---------------------|-----------------------------------------------------------------|
| Availability        | 99.99% uptime                                                   |
| Latency             | Restaurant search under 200 ms, order placement under 500 ms    |
| Location Updates    | Delivery partner location update every 5 seconds                |
| Scale               | 5M+ orders/day, 500K concurrent users during peak traffic       |
| Consistency         | Strong consistency for payments and orders; eventual consistency for reviews and ratings |
```
---

## High Level Design (HLD)

### System Architecture Overview

```
          Client (User App / Restaurant App / Delivery App)
                              |
                       API Gateway / BFF
                  (Auth, Rate Limiting, Routing)
                              |
   +--------+--------+--------+--------+--------+--------+
   |        |        |        |        |        |        |
 User   Restaurant  Order  Payment  Delivery  Notif-
Service  Service   Service  Service  Service  ication
   |        |        |        |        |        |
 MySQL   MongoDB   MySQL   Stripe   Redis +  Kafka +
 Redis   Redis     Redis   /UPI    PostGIS  SES/SMS
                   Kafka            WebSocket
                              |
                      Search Service
                      (Elasticsearch)
                              |
                      Location Service
                      (Redis GEO + PostGIS)
```

### End-to-End Order Flow

```
User searches restaurants (location based)
       |
Location Service -> returns restaurants within 5km radius
       |
User selects restaurant, adds items to cart
       |
User places order -> Order Service
  - validate cart + menu prices
  - check restaurant is open
  - create order in PENDING state
       |
Payment Service
  - process payment
  - on success -> publish ORDER_PAID to Kafka
       |
Kafka: order.paid
  -> Order Service    (status: CONFIRMED)
  -> Restaurant App   (WebSocket push: new order alert)
  -> Delivery Service (trigger assignment)
       |
Delivery Service
  - find nearest available delivery partner (Redis GEO)
  - assign partner -> notify via push + WebSocket
       |
Delivery partner picks up -> order PICKED_UP
Delivery partner delivers -> order DELIVERED
       |
Notification Service sends confirmation
Rating prompt sent to user (async, 30 min delay)
```

---

## Microservices Breakdown

### 1. User Service
- Register, login, OAuth (Google, Apple)
- Address management (multiple delivery addresses)
- Saved payment methods
- **DB:** MySQL + Redis (session cache)

### 2. Restaurant Service
- Onboarding, profile, menu management
- Operating hours, cuisine type, service radius
- Menu item availability toggle (real-time)
- **DB:** MongoDB (flexible menu schema) + Redis (active menu cache)

### 3. Order Service
- Create, update, cancel orders
- Order state machine
- Coupon and discount validation
- **DB:** MySQL (strong consistency) + Redis (order status cache)
- **Events:** Publishes to Kafka on every state change

### 4. Delivery Service
- Real-time delivery partner location tracking
- Auto-assignment of nearest available partner
- Partner availability management (online/offline toggle)
- ETA calculation
- **DB:** Redis GEO (live locations) + PostgreSQL with PostGIS (historical routes)
- **Protocol:** WebSocket for live location push to user app

### 5. Payment Service
- UPI, card, wallet, COD support
- Idempotency for retry-safe transactions
- Payout to restaurant and delivery partner
- **DB:** MySQL (payment records, immutable)

### 6. Search Service
- Restaurant search by name, cuisine, dish name
- Filters: rating, delivery time, price range, offers
- Autocomplete on dish and restaurant names
- **DB:** Elasticsearch (synced from Restaurant Service via Kafka)

### 7. Location Service
- Geo-based restaurant discovery
- Uses Redis GEO for fast radius queries
- Haversine distance calculation
- **DB:** Redis GEO + PostGIS (polygon-based zone mapping)

### 8. Notification Service
- Order status push notifications, SMS, email
- Consumes Kafka events
- **Stack:** FCM (Android), APNs (iOS), Twilio (SMS), SES (email)

### 9. Rating and Review Service
- Post-delivery ratings for restaurant and delivery partner
- Verified purchase check before allowing review
- **DB:** MongoDB + Elasticsearch (searchable reviews)

---

## Low Level Design (LLD)

### Order State Machine

```
CART_CHECKOUT
      |
  PENDING        (order created, awaiting payment)
      |
  CONFIRMED      (payment done, sent to restaurant)
      |
  PREPARING      (restaurant accepted and started cooking)
      |
  READY          (food ready for pickup)
      |
  PICKED_UP      (delivery partner collected order)
      |
  DELIVERED      (order delivered to user)
      |
[CANCELLED]      <- from PENDING or CONFIRMED only
[FAILED]         <- payment failure
```

### Delivery Partner Assignment (Nearest Rider)

```
order.paid event received
       |
Delivery Service queries Redis GEO:
  GEORADIUS delivery_partners:online
             {restaurant_lat} {restaurant_lng}
             3 km ASC COUNT 10

  Returns: [partner_1 (0.4km), partner_2 (0.9km), partner_3 (1.2km)]
       |
Try partner_1:
  - check active order count < 1 (not already on delivery)
  - send push notification + WebSocket alert
  - wait 30 seconds for acceptance
       |
If accepted -> assign, lock partner (Redis SET partner:{id}:status BUSY)
If rejected / timeout -> try partner_2, then partner_3
       |
If no partner found within 3km -> expand radius to 5km, retry
If still no partner -> order enters WAITING_FOR_PARTNER state
                    -> notify user with updated ETA
```

### Real-Time Location Tracking

```
Delivery App (every 5 seconds):
  POST /location
  { partnerId, lat, lng, timestamp }
       |
Location Service:
  GEOADD delivery_partners:online {lng} {lat} {partnerId}
  SET    partner:{partnerId}:location {lat,lng,ts} EX 30
       |
User App polls or receives via WebSocket:
  GET /order/{orderId}/track
  -> returns current lat/lng of assigned partner
  -> calculates ETA using road distance (Google Maps API)
       |
WebSocket push (server -> user app):
  every 5s: { lat, lng, eta_minutes, status }
```

### ETA Calculation

```
Factors:
  - Distance from restaurant to delivery address (road, not straight line)
  - Estimated preparation time (restaurant historical average)
  - Current traffic (Google Maps Distance Matrix API)
  - Delivery partner's current speed

Formula:
  ETA = prep_time_remaining + (road_distance / avg_speed) + buffer

Stored as:
  Redis key: order:{orderId}:eta
  Updated every 60 seconds as partner moves
```

### Surge Pricing Logic

```
Every 5 minutes, Surge Engine evaluates:
  demand  = pending orders in zone in last 10 min
  supply  = available delivery partners in zone

  surge_multiplier =
    if demand/supply > 3.0 -> 1.5x
    if demand/supply > 2.0 -> 1.3x
    if demand/supply > 1.5 -> 1.2x
    else                   -> 1.0x (no surge)

Stored in Redis: surge:{zoneId} = 1.3
TTL: 5 minutes (re-evaluated on expiry)

Applied at cart checkout:
  delivery_fee = base_fee * surge_multiplier
  User shown surge message: "High demand in your area"
```

### Coupon Validation (Idempotency + Abuse Prevention)

```
User applies coupon at checkout:
  1. Check coupon validity (expiry, min order value)
  2. Check coupon usage:
       INCR coupon:{code}:used
       if used_count > max_uses -> reject
  3. Check per-user limit:
       GET coupon:{code}:user:{userId}
       if exists -> "Already used"
       else -> SET coupon:{code}:user:{userId} 1 EX 2592000
  4. Apply discount, store in order record
  5. On order cancel -> DECR coupon:{code}:used
                     -> DEL coupon:{code}:user:{userId}
```

---

## Database Design

### Users Table (MySQL)

```sql
CREATE TABLE users (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone       VARCHAR(15) UNIQUE NOT NULL,
  email       VARCHAR(255),
  name        VARCHAR(100),
  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_addresses (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT NOT NULL,
  label       VARCHAR(50),          -- Home, Work, Other
  address     VARCHAR(500),
  lat         DECIMAL(10, 8),
  lng         DECIMAL(11, 8),
  is_default  BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Orders Table (MySQL)

```sql
CREATE TABLE orders (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id         BIGINT NOT NULL,
  restaurant_id   BIGINT NOT NULL,
  partner_id      BIGINT,
  status          ENUM('PENDING','CONFIRMED','PREPARING',
                       'READY','PICKED_UP','DELIVERED',
                       'CANCELLED','FAILED'),
  total_amount    DECIMAL(10,2),
  delivery_fee    DECIMAL(6,2),
  surge_multiplier DECIMAL(3,2) DEFAULT 1.0,
  address_id      BIGINT,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_restaurant (restaurant_id),
  INDEX idx_partner (partner_id),
  INDEX idx_status (status)
);

CREATE TABLE order_items (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id     BIGINT NOT NULL,
  item_id      BIGINT NOT NULL,
  item_name    VARCHAR(255),
  quantity     INT,
  unit_price   DECIMAL(8,2),
  FOREIGN KEY (order_id) REFERENCES orders(id)
);
```

### Restaurant Document (MongoDB)

```json
{
  "_id": "rest_123",
  "name": "Burger Barn",
  "cuisine": ["American", "Fast Food"],
  "address": "MG Road, Bangalore",
  "location": { "type": "Point", "coordinates": [77.5946, 12.9716] },
  "rating": 4.2,
  "delivery_time_min": 30,
  "min_order": 149,
  "is_open": true,
  "operating_hours": {
    "mon": { "open": "10:00", "close": "23:00" },
    "tue": { "open": "10:00", "close": "23:00" }
  },
  "menu": [
    {
      "category": "Burgers",
      "items": [
        {
          "id": "item_456",
          "name": "Classic Smash Burger",
          "price": 249,
          "description": "Double patty with special sauce",
          "is_veg": false,
          "is_available": true,
          "customizations": [
            { "name": "Extra Cheese", "price": 30 },
            { "name": "No Onion", "price": 0 }
          ]
        }
      ]
    }
  ]
}
```

### Delivery Partner (PostgreSQL + PostGIS)

```sql
CREATE TABLE delivery_partners (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  name        VARCHAR(100),
  phone       VARCHAR(15) UNIQUE NOT NULL,
  vehicle     ENUM('BIKE','CYCLE','SCOOTER'),
  status      ENUM('ONLINE','OFFLINE','BUSY'),
  rating      DECIMAL(3,2),
  created_at  TIMESTAMP
);

CREATE TABLE delivery_zones (
  id       BIGINT PRIMARY KEY,
  name     VARCHAR(100),
  boundary GEOMETRY(POLYGON, 4326)   -- PostGIS polygon
);
```

---

## Key System Design Decisions

### Caching Strategy
```
| **Data**                     | **Cache**  | **TTL** | **Reason**                                  |
|------------------------------|------------|---------|---------------------------------------------|
| Restaurant Menu              | Redis      | 5 min   | High read frequency, infrequent updates     |
| Restaurant List by Location  | Redis      | 2 min   | Geo-spatial queries are expensive           |
| Partner Live Location        | Redis GEO  | 30 sec  | Auto-expire when delivery partner goes offline |
| Order Status                 | Redis      | 10 min  | Frequent polling from customer applications |
| Surge Multiplier per Zone    | Redis      | 5 min   | Recalculated periodically based on demand   |
| Coupon Usage Count           | Redis      | 30 days | Prevent coupon abuse and duplicate usage    |
```


### Kafka Topics

```
| **Kafka Topic**   | **Producer**      | **Consumer(s)**                                    |
|-------------------|-------------------|----------------------------------------------------|
| order.created     | Order Service     | Notification Service (Order Confirmation SMS)      |
| order.paid        | Payment Service   | Order Service, Delivery Service, Restaurant App    |
| order.confirmed   | Restaurant App    | Notification Service (Customer Alert)              |
| order.ready       | Restaurant App    | Delivery Service (Pickup Notification)             |
| order.picked_up   | Delivery App      | Order Service, Notification Service                |
| order.delivered   | Delivery App      | Order Service, Notification Service, Rating Service |
| order.cancelled   | Order Service     | Payment Service (Refund), Inventory Service, Notification Service |
| partner.location  | Delivery App      | Location Service, ETA Engine                        |
```

### Geo-Indexing with Redis GEO

```
Add partner location:
  GEOADD delivery_partners:online 77.5946 12.9716 "partner_99"

Query nearest partners within 3km:
  GEORADIUS delivery_partners:online 77.5946 12.9716 3 km
            ASC COUNT 10 WITHCOORD WITHDIST

Remove partner when offline:
  ZREM delivery_partners:online "partner_99"
```

### Handling Restaurant Menu Updates (Cache Invalidation)

```
Restaurant owner updates item price
       |
Restaurant Service writes to MongoDB
       |
DEL menu:{restaurantId} from Redis (cache invalidation)
       |
Publish to Kafka: menu.updated
       |
Search Service consumer re-indexes restaurant in Elasticsearch
       |
Next request fetches fresh menu from MongoDB, re-caches in Redis
```

---

## Non-Functional Requirements

### Scalability

```
AWS ALB + Route53
        |
API Gateway (K8s, HPA on RPS)
        |
Microservices (Docker + Kubernetes)
        |
Data:
  MySQL:         Read replicas (orders, users)
  MongoDB:       Replica set + sharding (restaurants)
  Redis Cluster: Geo + cache + session
  PostgreSQL:    PostGIS for zone polygons
  Elasticsearch: 3+ node cluster for search
```

### Fault Tolerance

```
| **Reliability Strategy** | **Applied To**                          |
|--------------------------|-----------------------------------------|
| Circuit Breaker          | All inter-service HTTP calls            |
| Retry with Backoff       | Payment Service, Maps API calls         |
| Dead Letter Queue (DLQ)  | All Kafka consumers (prevent message loss) |
| Fallback ETA             | Use straight-line distance if Maps API is unavailable |
| Graceful Degradation     | Serve cached restaurant list if geo queries are slow |
| Multi-AZ Deployment      | MySQL, Redis, and Kafka clusters        |
```

### Back of Envelope

```
5M orders/day = ~58 orders/sec (avg), ~500/sec (peak dinner hours)
Partner location update every 5s:
  100K active partners x 1 update/5s = 20K writes/sec to Redis GEO
Restaurant search (geo radius):
  500K concurrent users x 1 search/10s = 50K reads/sec -> Redis cache
```

---

## Interview Quick Reference
```
| Question | Answer |
|---|---|
| How to find nearest delivery partner? | Redis GEORADIUS on online partners set; try in order of distance; fallback to wider radius |
| How to track delivery in real time? | Partner app posts location every 5s; stored in Redis GEO; user app gets updates via WebSocket |
| How to prevent order double placement? | Idempotency key per cart checkout stored in Redis; duplicate request returns cached response |
| Why MongoDB for restaurant menu? | Flexible nested schema (categories, items, customizations vary per restaurant); schema-less fits well |
| How to handle surge pricing? | Demand/supply ratio per zone evaluated every 5 min; multiplier stored in Redis; applied at checkout |
| How to handle restaurant going offline mid-order? | Order already CONFIRMED stays in system; cancellation window only before PREPARING state |
| How to validate coupons at scale? | Redis INCR for usage count; per-user key for abuse prevention; rollback on order cancel |
| DB choice for orders? | MySQL for ACID guarantees; strong consistency needed for financial records |
| How to sync restaurants to Elasticsearch? | Kafka event on menu/profile update; ES consumer re-indexes; eventual consistency acceptable |
| ETA accuracy approach? | Google Maps Distance Matrix for road distance + historical prep time + partner speed; update every 60s |
| How to handle peak dinner traffic? | Horizontal pod autoscaling; Redis cache for restaurant list; async order processing via Kafka |
| How to handle refunds on cancellation? | Kafka order.cancelled event triggers Payment Service refund; idempotent refund API to prevent double refund |
```