# 💳 Designing a Payment System — System Design

---

## Table of Contents

1. [Requirements](#1-requirements)
2. [High-Level Architecture](#2-high-level-architecture)
3. [Request Flow](#3-request-flow)
4. [Core Components](#4-core-components)
5. [Database Design](#5-database-design)
6. [Idempotency — Prevent Duplicate Payments](#6-idempotency--prevent-duplicate-payments)
7. [Handling Payment States](#7-handling-payment-states)
8. [Failure Handling & Retry Strategy](#8-failure-handling--retry-strategy)
9. [Security](#9-security)
10. [Scalability & High Availability](#10-scalability--high-availability)
11. [2-Minute Interview Answer](#11-2-minute-interview-answer)

---

## 1. Requirements

### ✅ Functional
- User can initiate a payment
- Support multiple payment methods (Credit Card, UPI, Net Banking, Wallet)
- Payment success / failure / pending states
- Refund support
- Payment history per user
- Notify user on payment status change

### ⚙️ Non-Functional

Property ------- Target             
                         
Availability ----- 99.99% (payments must never go down)      

Latency     ----- &lt; 500ms for payment initiation           |

Consistency ----- Strong — money must never be double-charged |

Durability -----Every payment event must be persisted       |

Security    ----- PCI-DSS compliant, no raw card data stored  |

## 2. High-Level Architecture

```
         Client (Mobile / Web)
                  │
                  ▼
            API Gateway
          (Auth, Rate Limit)
                  │
                  ▼
         Payment Service
                  │
        ┌─────────┴──────────┐
        ▼                    ▼
  Payment Gateway      Ledger Service
 (Stripe / Razorpay)   (Double-Entry)
        │                    │
        ▼                    ▼
  Bank / Card Network    Database
  (Visa / Mastercard)
        │
        ▼
  Notification Service
  (Email / SMS / Push)
```

---

## 3. Request Flow

### Successful Payment

```
User clicks "Pay ₹500"
        │
        ▼
API Gateway (validate token)
        │
        ▼
Payment Service
        │
        ▼
Generate idempotency key
        │
        ▼
Create payment record → Status: PENDING
        │
        ▼
Call Payment Gateway (Stripe / Razorpay)
        │
        ▼
Gateway contacts Bank / Card Network
        │
        ▼
Bank approves transaction
        │
        ▼
Payment Service receives webhook
        │
        ▼
Update payment record → Status: SUCCESS
        │
        ▼
Ledger updated (debit user, credit merchant)
        │
        ▼
Notification sent to user ✅
```

### Failed Payment

```
Bank declines transaction
        │
        ▼
Payment Service receives webhook
        │
        ▼
Update payment record → Status: FAILED
        │
        ▼
Retry logic triggered (if applicable)
        │
        ▼
Notify user of failure
```

---

## 4. Core Components

### 4.1 API Gateway
- Authenticates requests (JWT / OAuth2)
- Rate limiting (prevent payment spam)
- Routes to Payment Service

### 4.2 Payment Service
- Core orchestrator — manages the full payment lifecycle
- Generates and validates idempotency keys
- Calls external Payment Gateway
- Listens to gateway webhooks for status updates
- Publishes events to message queue (Kafka)

### 4.3 Payment Gateway
- External provider: Stripe, Razorpay, PayU, Braintree
- Handles actual card/UPI/bank communication
- Returns success/failure via webhooks (async) or response (sync)

### 4.4 Ledger Service
- Maintains **double-entry bookkeeping**
- Every payment = debit from one account + credit to another
- Immutable — records are never updated, only appended
- Source of truth for all financial calculations

### 4.5 Notification Service
- Listens to Kafka payment events
- Sends Email / SMS / Push notification on status change
- Async — does not block the payment flow

### 4.6 Message Queue (Kafka)
- Decouples Payment Service from downstream services
- Events: `payment.initiated`, `payment.success`, `payment.failed`, `payment.refunded`
- Guarantees at-least-once delivery

---

## 5. Database Design

### payments table

```sql
CREATE TABLE payments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(64) UNIQUE NOT NULL,  -- prevents duplicates
    user_id         UUID NOT NULL,
    merchant_id     UUID NOT NULL,
    amount          DECIMAL(12, 2) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'INR',
    status          VARCHAR(20) NOT NULL,          -- PENDING, SUCCESS, FAILED, REFUNDED
    payment_method  VARCHAR(20) NOT NULL,          -- CARD, UPI, NETBANKING, WALLET
    gateway_txn_id  VARCHAR(100),                  -- ID from Stripe / Razorpay
    failure_reason  TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### ledger_entries table (Double-Entry)

```sql
CREATE TABLE ledger_entries (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id  UUID NOT NULL REFERENCES payments(id),
    account_id  UUID NOT NULL,
    entry_type  VARCHAR(6) NOT NULL,   -- DEBIT or CREDIT
    amount      DECIMAL(12, 2) NOT NULL,
    currency    VARCHAR(3) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
-- Every payment creates exactly 2 rows: one DEBIT + one CREDIT
```

### payment_events table (Audit Trail)

```sql
CREATE TABLE payment_events (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id  UUID NOT NULL REFERENCES payments(id),
    event_type  VARCHAR(50) NOT NULL,  -- INITIATED, GATEWAY_CALLED, WEBHOOK_RECEIVED, etc.
    payload     JSONB,                 -- full event data
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
-- Append-only — never update or delete
```

---

## 6. Idempotency — Prevent Duplicate Payments

### The Problem

```
User clicks "Pay" → Request sent
Network timeout → User clicks "Pay" again

Without idempotency:
→ Two payments created ❌
→ User charged twice ❌
```

### The Solution — Idempotency Key

Client generates a unique key per payment attempt and sends it in the header:

```
POST /payments
Headers:
  Idempotency-Key: a3f8c2d1-9b4e-4f7a-8c3d-2e1f9a4b5c6d

Body:
  { "amount": 500, "currency": "INR", "method": "UPI" }
```

Server logic:

```java
public PaymentResponse initiatePayment(PaymentRequest request, String idempotencyKey) {

    // Check if this key was already processed
    Optional<Payment> existing = paymentRepo.findByIdempotencyKey(idempotencyKey);

    if (existing.isPresent()) {
        return toResponse(existing.get()); // Return same result, no duplicate ✅
    }

    // First time — process the payment
    Payment payment = Payment.builder()
        .idempotencyKey(idempotencyKey)
        .userId(request.getUserId())
        .amount(request.getAmount())
        .status(PaymentStatus.PENDING)
        .build();

    paymentRepo.save(payment);
    gatewayService.charge(payment);
    return toResponse(payment);
}
```

---

## 7. Handling Payment States

```
                  ┌─────────┐
                  │ PENDING │
                  └────┬────┘
                       │
           ┌───────────┼───────────┐
           ▼           ▼           ▼
       SUCCESS       FAILED     EXPIRED
           │                       │
           ▼                       ▼
       REFUNDED               (retry flow)
           │
           ▼
    REFUND_PENDING
           │
           ▼
    REFUND_SUCCESS
```

### State Transition Rules

| From | To | Trigger |


| PENDING | SUCCESS | Bank approves via webhook |

| PENDING | FAILED | Bank declines via webhook |

| PENDING | EXPIRED | No response within timeout window |

| SUCCESS | REFUNDED | Refund initiated by user/merchant |

| REFUNDED | REFUND_PENDING | Refund request sent to gateway |

| REFUND_PENDING | REFUND_SUCCESS | Gateway confirms refund |

> States should only move **forward** — never back. Enforce this in the service layer.

```java
public void updatePaymentStatus(UUID paymentId, PaymentStatus newStatus) {
    Payment payment = paymentRepo.findById(paymentId).orElseThrow();

    if (!payment.getStatus().canTransitionTo(newStatus)) {
        throw new InvalidStateTransitionException(
            "Cannot move from " + payment.getStatus() + " to " + newStatus);
    }

    payment.setStatus(newStatus);
    paymentRepo.save(payment);
    eventPublisher.publish(new PaymentStatusChangedEvent(payment));
}
```

---

## 8. Failure Handling & Retry Strategy

### Problem — Network Failures with Payment Gateways

```
Payment Service calls Stripe
        │
   Network drops
        │
        ▼
Did Stripe charge the user or not? Unknown ❌
```

### Solution — Idempotent Retry with Exponential Backoff

```java
public GatewayResponse callGatewayWithRetry(Payment payment) {
    int maxRetries = 3;
    long delayMs = 500; // start with 500ms

    for (int attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            return gateway.charge(payment.getIdempotencyKey(), payment.getAmount());
        } catch (TransientException e) {
            if (attempt == maxRetries) throw e;

            Thread.sleep(delayMs);
            delayMs *= 2; // exponential backoff: 500 → 1000 → 2000ms
        }
    }
}
```

### Webhook Handling — At-Least-Once Delivery

```
Stripe fires webhook → Payment Service processes → Stripe expects 200 OK
                                │
                     Network drops before 200 sent
                                │
                     Stripe retries webhook ──► Same payment processed twice? ❌

Fix: Check idempotency key before processing webhook
```

```java
public void handleWebhook(WebhookEvent event) {
    // Skip if already processed
    if (webhookRepo.existsByGatewayEventId(event.getId())) {
        return; // Idempotent ✅
    }

    webhookRepo.save(new WebhookRecord(event.getId()));
    paymentService.updateStatus(event.getPaymentId(), event.getStatus());
}
```

---

## 9. Security

### Never Store Raw Card Data

```
❌ Do NOT store: card number, CVV, expiry date
✅ Store: gateway token (e.g., Stripe's tok_xxxxx)

PCI-DSS compliance = let the gateway handle raw card data
```

### Encrypt Sensitive Fields

```java
@Convert(converter = EncryptedStringConverter.class)
private String maskedCardNumber; // Store only last 4 digits, encrypted
```

### Validate Payment Amount Server-Side

```java
// ❌ Never trust amount from client
// A user could modify request to pay ₹1 instead of ₹500

// ✅ Always fetch amount from your own DB / order service
Order order = orderRepo.findById(request.getOrderId());
payment.setAmount(order.getTotalAmount()); // Authoritative amount
```

### Secure Webhook Endpoints

```java
// Verify Stripe webhook signature to reject forged webhooks
public void handleWebhook(HttpServletRequest request, String payload) {
    String sigHeader = request.getHeader("Stripe-Signature");
    Event event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
    // proceed only if no exception thrown
}
```

---

## 10. Scalability & High Availability

```
                    Load Balancer
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
    PaymentSvc-1   PaymentSvc-2   PaymentSvc-3
          │              │              │
          └──────────────┼──────────────┘
                         │
                         ▼
                   Kafka Cluster
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
    LedgerSvc      NotificationSvc   AuditSvc
                         │
                         ▼
               PostgreSQL (Primary)
                         │
                    ┌────┴────┐
                    ▼         ▼
                Read       Read
               Replica-1  Replica-2
```

### Key Decisions

| Concern | Solution |
|---|---|

| DB write bottleneck | Write to primary only; reads from replicas |

| Payment Service crash mid-payment | Kafka ensures event is replayed on restart |

| Gateway timeout | Idempotent retry with exponential backoff |

| High read traffic (payment history) | Redis cache for recent transactions |

| DB scalability | Shard by `user_id` when table exceeds 100M rows |

| Audit & compliance | Append-only `payment_events` table, never delete |

---

## 11. 2-Minute Interview Answer

> *"I would design the payment system around three core principles: **idempotency**, **strong consistency**, and **event-driven architecture**.*
>
> *The client sends a payment request with a unique idempotency key to prevent duplicate charges on retries. The Payment Service creates a PENDING record, calls the Payment Gateway (e.g., Razorpay or Stripe), and waits for a webhook callback to confirm success or failure.*
>
> *For money movement, I use a Ledger Service with double-entry bookkeeping — every payment generates an immutable debit and credit entry. Payment state transitions are strictly enforced: PENDING → SUCCESS or FAILED — never backwards.*
>
> *Downstream services like Notification and Audit are decoupled via Kafka, so a notification failure never affects the payment itself. For security, raw card data is never stored — only gateway tokens — and webhook endpoints verify signatures to prevent forgery.*
>
> *The system scales horizontally with multiple Payment Service instances behind a load balancer, read replicas for history queries, and Redis caching for recent transactions."*

---

## Quick Reference

```
Duplicate payment      → Idempotency key (unique per attempt)
Double charge on retry → Idempotent retry + exponential backoff
Fake webhook           → Verify gateway signature
Raw card data          → Never store, use gateway token (PCI-DSS)
State management       → Strict forward-only state machine
Money consistency      → Double-entry ledger (append-only)
Service decoupling     → Kafka for events (Notification, Audit, Ledger)
High availability      → Multiple instances + DB replicas + Kafka
```

---

*Last updated: 2025*
