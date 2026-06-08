# Notification System Design

## 1. Clarify Requirements

### Questions to Ask
- Push notifications?
- Email notifications?
- SMS notifications?
- In-app notifications?
- Real-time or delayed?
- Expected scale? (1M users? 100M users?)

### Assumptions
**Supported channels:**
- Email
- SMS
- Push Notifications
- In-App Notifications

**Scale:** Millions of notifications/day

---

## 2. High-Level Architecture

```
User Action
     |
     v
Order Service
     |
     v
Notification API
     |
     v
   Kafka
  /   |   \
 v    v    v
Email  SMS  Push
Worker Worker Worker
 |      |     |
 v      v     v
Email  SMS   FCM/
API    API   APNS
```

---

## 3. Flow

```
Order Placed
     |
     v
Order Service
     |
 Publish Event
     |
     v
 Kafka Topic
     |
Notification Consumers
     |
Send Email / SMS / Push
```

> **Key principle:** Order Service doesn't wait for notification delivery.

---

## 4. Why Kafka?

### Without Kafka
```
Order Service
     |
     +--> Email
     +--> SMS
     +--> Push
```
❌ If Email Service is down → Order API becomes slow.

### With Kafka
```
Order Service
     |
 Publish Event
     |
 Return Success
```
✅ Fast and decoupled.

---

## 5. Database Design

### Notification Table
```
notification
------------
id
user_id
type
channel
message
status
created_at
```

### User Preference Table
```
user_preferences
-----------------
user_id
email_enabled
sms_enabled
push_enabled
```

---

## 6. Notification Types

| Channel   | Use Cases                              |
|-----------|----------------------------------------|
| Email     | Order Confirmation, Password Reset, Monthly Reports |
| SMS       | OTP, Payment Success                   |
| Push      | Sale Alert, New Message                |
| In-App    | Bell Icon Notifications                |

---

## 7. Retry Mechanism

What if Email Service fails?

```
Email Worker
     |
     X  (failure)
     |
Retry Queue
     |
  Retry 1
  Retry 2
  Retry 3
     |
Dead Letter Queue (DLQ)
```

**Tools used:**
- Kafka Retry Topic
- Dead Letter Queue (DLQ)

---

## 8. User Preferences

Before sending any notification:

```
Notification Worker
        |
Check User Preferences
        |
  Email Enabled?
        |
       Yes
        |
   Send Email
```

---

## 9. Rate Limiting

Prevent notification spam.

**Example limits:**
- Max 5 SMS/hour
- Max 20 Emails/day

**Implementation:**
- Redis counters
- Sliding Window Algorithm

---

## 10. Real-Time Notifications

For In-App Notifications, use one of:

```
Backend
   |
WebSocket
   |
Browser
```

or

```
Backend
   |
  SSE (Server-Sent Events)
   |
Browser
```

---

## 11. Scaling

```
         Kafka
           |
  ----------------------
  |         |          |
Email-1  Email-2   Email-3

  ----------------------
  |         |          |
Push-1   Push-2    Push-3
```

> Consumers can scale **horizontally**.

---

## 12. Failure Handling

Track notification status for auditing:

| Status     | Description                  |
|------------|------------------------------|
| `PENDING`  | Queued, not yet sent         |
| `SENT`     | Successfully delivered       |
| `FAILED`   | Delivery failed              |
| `RETRYING` | In retry queue               |

---

## Interview Diagram

```
     Order Service
           |
           v
  Notification Service
           |
           v
         Kafka
  -----------------------
  |          |          |
  v          v          v
Email MS   SMS MS   Push MS
  |          |          |
  v          v          v
Email API  Twilio   FCM/APNS
```

---

## 2-Minute Interview Answer

> "I would design the notification system using an **event-driven architecture**. Business services publish notification events to **Kafka**. Dedicated consumers handle Email, SMS, Push, and In-App notifications independently. User preferences are stored in a database and checked before delivery. Failed notifications are retried using **retry topics and DLQs**. **Redis** can be used for rate limiting, and **WebSockets** can provide real-time in-app notifications. This architecture is scalable, fault-tolerant, and decoupled."
