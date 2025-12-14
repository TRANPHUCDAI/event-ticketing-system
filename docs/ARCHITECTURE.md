# Event Ticketing System - Architecture & Design Document

## 📐 High-Level Architecture Diagram (5 Consolidated Services)

```
┌──────────────────────────────────────────────────────────────────┐
│                         USER/FRONTEND                             │
└────────────────┬─────────────────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────────────────┐
│                    API GATEWAY (Spring Cloud Gateway)             │
│                         Port 8000                                 │
│  - Route requests to appropriate microservices                   │
│  - JWT Token validation                                          │
│  - Rate limiting                                                 │
└──────────┬──────────────┬──────────────┬──────────────────────────┘
           │              │              │                │
      ┌────▼────────┐ ┌───▼────┐    ┌───▼──────┐    ┌────▼──────────┐
      │Event Booking│ │ Payment │    │Ticketing │    │ Notification  │
      │ Service     │ │ Service │    │ Service  │    │& Analytics    │
      │ (8001)      │ │ (8003)  │    │ (8004)   │    │ (8005)        │
      │             │ │         │    │          │    │ (Consolidated)│
      │ Event Mgmt  │ └─────────┘    └────┬─────┘    │               │
      │ + Seat Hold │                      │         │ Email notify  │
      │ (Merged)    │                      │         │ + Reporting   │
      └─────┬───────┘                      │         └────┬──────────┘
            │                              │              │
            │                    ┌─────────▼──────────────┼───┐
            │                    │ Kafka Event Streaming  │   │
            │                    │ - payment-confirmed    │   │
            │                    │ - ticket-created       │   │
            │                    │ - payment-failed       │   │
            │                    │ - notification-sent    │   │
            │                    └────────────────────────────┘
            │
      ┌─────▼──────────────────────────────────────────┐
      │  Communication Protocols                        │
      │  - REST API (Primary)                          │
      │  - gRPC (for inter-service calls)             │
      │  - Kafka (event streaming)                    │
      └──────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                     DATA PERSISTENCE LAYER                        │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────┐  ┌──────────────────┐  ┌────────────────┐│
│  │ Event Booking DB │  │   Payment DB     │  │Ticketing DB    ││
│  │ (Port 5432)      │  │ (Port 5433)      │  │(Port 5434)     ││
│  │ - Events         │  │ - Transactions   │  │ - Tickets      ││
│  │ - Seats          │  │ - Payments       │  │ - QR Codes     ││
│  │ - Reservations   │  │                  │  │                ││
│  └──────────────────┘  └──────────────────┘  └────────────────┘│
│                                                                  │
│  ┌──────────────────────┐  ┌──────────────────────────────────┐ │
│  │ Notification DB      │  │   Redis (Distributed Cache)      │ │
│  │ (Port 5435)          │  │   (Port 6379)                    │ │
│  │ - Notifications      │  │   - Seat Hold Locks (TTL: 5min) │ │
│  │ - Analytics          │  │   - Session Cache               │ │
│  │ - Reports            │  │   - Rate Limiting               │ │
│  └──────────────────────┘  └──────────────────────────────────┘ │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                    MESSAGE QUEUE (Kafka 7.5)                     │
│  - Event Streaming & Async Processing                            │
│  - Topics: payment-confirmed, ticket-created, payment-failed    │
│  - Supports Event-Driven Architecture                            │
└──────────────────────────────────────────────────────────────────┘
```

---

## 🔄 User Journey Sequence Diagram

### Flow 1: Seat Holding (in Event Booking Service)

```
┌────────┐         ┌────────────┐         ┌────────────────┐
│ Client │         │   API GW   │         │Event Booking   │
└───┬────┘         └─────┬──────┘         │ Service        │
    │                    │                 └─────┬──────────┘
    │ Hold seat A1       │                       │
    ├───────────────────>│                       │
    │                    │ Hold Seat Request     │
    │                    ├──────────────────────>│
    │                    │                       │
    │                    │                    ┌─▼──────────────┐
    │                    │                    │ Redis Command: │
    │                    │                    │ SET key=val NX │
    │                    │                    │ PX 300000 (5m) │
    │                    │                    └─┬──────────────┘
    │                    │                       │
    │                    │                    ┌─▼──────────────────┐
    │                    │                    │ Save to DB with:   │
    │                    │                    │ status = HELD      │
    │                    │                    │ expiresAt = now+5m │
    │                    │<──────────────────┤ id = reservation_id│
    │                    │ Hold Success       └─────────────────┘
    │<───────────────────┤
    │ {held_until: ...}  │
    │                    │
    │ (5 minutes later)  │
    │                    │ [TTL expires]
    │                    │ Auto-release seat
```

### Flow 2: Payment & Ticket Creation

```
┌────────┐    ┌────────────┐    ┌─────────┐    ┌──────────┐    ┌──────────┐
│ Client │    │ API GW     │    │ Payment │    │  Kafka   │    │Ticketing │
└───┬────┘    └─────┬──────┘    └────┬────┘    └────┬─────┘    └────┬─────┘
    │               │                │              │               │
    │ Create Payment│                │              │               │
    ├──────────────>│                │              │               │
    │               │ Create Payment │              │               │
    │               ├───────────────>│              │               │
    │               │                │              │               │
    │               │            ┌───▼──────┐       │               │
    │               │            │ Generate │       │               │
    │               │            │ txn_id   │       │               │
    │               │            │ status:  │       │               │
    │               │            │ PENDING  │       │               │
    │               │            └───┬──────┘       │               │
    │               │<───────────────┤              │               │
    │<──────────────┤ paymentId      │              │               │
    │ {paymentId}   │                │              │               │
    │               │                │              │               │
    │ Confirm Payment               │              │               │
    ├──────────────>│                │              │               │
    │               │ Confirm Payment│              │               │
    │               ├───────────────>│              │               │
    │               │                │              │               │
    │               │            ┌───▼──────┐       │               │
    │               │            │ Validate │       │               │
    │               │            │ Gateway  │       │               │
    │               │            │ status:  │       │               │
    │               │            │ CONFIRMED│       │               │
    │               │            └───┬──────┘       │               │
    │               │                │              │               │
    │               │                │─ Publish Payment-Confirmed ──>│
    │               │<───────────────┤              │               │
    │<──────────────┤ Success        │              │               │
    │               │                │              │     Listen    │
    │               │                │              │<──────────────┤
    │               │                │              │ Payment Event │
    │               │                │              │               │
    │               │                │              │     ┌─────────▼─┐
    │               │                │              │     │ Create    │
    │               │                │              │     │ Ticket    │
    │               │                │              │     │ Gen QR    │
    │               │                │              │     │ Gen Image │
    │               │                │              │     └─────────┬─┘
    │               │                │              │ Publish Ticket-Created
    │               │                │              │<─────────────>│
    │
    │ Poll ticket status (or get via SSE)
    │
    │ Ticket received with QR code
```

---

## 📊 Database Schema Details

### Event Service Database

```sql
-- events table
CREATE TABLE events (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    venue_name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    sold_seats INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- seats table
CREATE TABLE seats (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES events(id),
    row VARCHAR(10) NOT NULL,
    col INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, BLOCKED, SOLD
    held_by VARCHAR(255),
    held_until BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(event_id, row, col)
);

CREATE INDEX idx_seats_event_id ON seats(event_id);
CREATE INDEX idx_seats_status ON seats(status);
```

### Seat Allocation Service Database

```sql
-- seat_reservations table
CREATE TABLE seat_reservations (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    seat_id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'HELD', -- HELD, CONFIRMED, RELEASED
    held_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reservation_event_seat ON seat_reservations(event_id, seat_id);
CREATE INDEX idx_reservation_status ON seat_reservations(status);
CREATE INDEX idx_reservation_expires_at ON seat_reservations(expires_at);
```

### Payment Service Database

```sql
-- transactions table
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    event_id UUID NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, CONFIRMED, FAILED, CANCELLED
    payment_method VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transaction_user_id ON transactions(user_id);
CREATE INDEX idx_transaction_status ON transactions(status);
```

### Ticketing Service Database

```sql
-- tickets table
CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    seat_id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    payment_id UUID NOT NULL,
    qr_code VARCHAR(255) NOT NULL UNIQUE,
    qr_code_image BYTEA,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, USED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    checked_in_at TIMESTAMP
);

CREATE INDEX idx_ticket_user_id ON tickets(user_id);
CREATE INDEX idx_ticket_event_id ON tickets(event_id);
CREATE INDEX idx_ticket_qr_code ON tickets(qr_code);
CREATE INDEX idx_ticket_status ON tickets(status);
```

---

## 🔐 Concurrency Control Strategy

### Redis Lock Implementation

```
1. User Request: Hold Seat A1 for Event 1
   │
   ├─ Redis: SET seat_lock:event1:A1 = "user123" NX PX 300000
   │        (Only set if key doesn't exist, expire in 5 minutes)
   │
   ├─ If RETURN = OK:
   │    ├─ Successfully acquired lock
   │    └─ Save SeatReservation to PostgreSQL
   │
   └─ If RETURN = NIL:
        └─ Lock already exists
           └─ Throw SeatAlreadyHeldException

2. After 5 minutes:
   ├─ Redis TTL expires
   ├─ Key automatically deleted
   ├─ Seat becomes AVAILABLE again
   └─ Background job: cleanupExpiredReservations()
```

### Thread-Safe Flow

```java
// Atomic operation in Redis
Boolean locked = redisTemplate.opsForValue().setIfAbsent(
    "seat_lock:event1:A1",
    "user123",
    Duration.ofMinutes(5)
);

// If locked = true:  Lock acquired, save to DB
// If locked = false: Lock exists, another user already has it
```

### Preventing Double-Booking

1. **Redis NX Lock** - Only one user can acquire lock
2. **Database Constraint** - Unique index on (event_id, seat_id, status='SOLD')
3. **Confirmation Flow**:
   - User holds seat (Redis)
   - Payment is confirmed
   - Seat status changed to SOLD in DB
   - No other user can hold or confirm

---

## 🗂️ Service Communication

### 1. REST API (HTTP)

**Event Service** ↔ **Event Service**

- Simple CRUD operations
- Synchronous calls

**API Gateway** ↔ **All Services**

- Route requests
- Request/Response handling

### 2. gRPC (Async, Planned)

```protobuf
service SeatAllocationService {
  rpc HoldSeat(HoldSeatRequest) returns (HoldSeatResponse);
  rpc ReleaseSeat(ReleaseSeatRequest) returns (ReleaseSeatResponse);
  rpc ConfirmSeat(ConfirmSeatRequest) returns (ConfirmSeatResponse);
}
```

### 3. Kafka Event Streaming

**Topics:**

- `payment-confirmed`: Published by Payment Service
  - Consumed by: Ticketing Service, Notification Service
- `ticket-created`: Published by Ticketing Service
  - Consumed by: Notification Service, Reporting Service
- `payment-failed`: Published by Payment Service
  - Consumed by: Notification Service

---

## 🧪 Testing Strategy

### Unit Tests

- Service layer logic
- Entity validations
- Repository queries

### Integration Tests

- Redis lock mechanism
- Kafka event publishing
- End-to-end flows

### Load Tests

- 100+ concurrent seat holds
- Verify only winners get seats
- Redis performance under load

---

## 📈 Performance Considerations

### Redis Lock Performance

- O(1) complexity for SET operation
- Sub-millisecond latency
- Automatic TTL cleanup

### Database Indexing

```sql
-- Critical indexes for performance
CREATE INDEX idx_seats_event_status ON seats(event_id, status);
CREATE INDEX idx_reservation_expires ON seat_reservations(expires_at);
CREATE INDEX idx_tickets_user ON tickets(user_id);
```

### Kafka Throughput

- Topics can handle 1000+ msgs/sec
- Partitioning for scalability
- Consumer groups for fault tolerance

---

## 🚀 Deployment

### Local Development

```bash
docker-compose up -d
mvn clean install
Each service: mvn spring-boot:run
```

### Docker Deployment

```dockerfile
FROM openjdk:17-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Kubernetes (Future)

- StatefulSet for PostgreSQL
- ConfigMap for Redis
- Kafka on Kubernetes
- Service mesh (Istio)

---

## 🔒 Security Considerations

### JWT Authentication

- API Gateway validates tokens
- User context passed to services

### Data Protection

- Password hashing (BCrypt)
- HTTPS/TLS for all communication
- Sensitive data encryption at rest

### Rate Limiting

- API Gateway rate limiter
- Per-user request throttling

---

## 📚 References

- [Spring Boot Microservices](https://spring.io/microservices)
- [Redis Documentation](https://redis.io)
- [Kafka Architecture](https://kafka.apache.org)
- [ZXing QR Codes](https://github.com/zxing/zxing)
- [PostgreSQL JSON](https://www.postgresql.org/docs/current/datatype-json.html)
