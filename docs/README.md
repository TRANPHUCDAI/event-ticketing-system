# Event Ticketing System - SOA Architecture

**Hệ thống đặt vé sự kiện với kiến trúc Service-Oriented Architecture (SOA)**

## 🎯 Tổng quan dự án

Một nền tảng đặt vé sự kiện đầy đủ với 5 microservices:

- ✅ Quản lý sự kiện + cấp phát ghế (Event Booking Service)
- ✅ Giữ ghế theo thời gian (Seat Hold - 5 phút)
- ✅ Xử lý thanh toán (Payment Processing)
- ✅ Tạo vé với mã QR (Ticket Generation + QR Code)
- ✅ Check-in quét QR (QR Code Check-in)
- ✅ Gửi thông báo + Báo cáo (Notification & Analytics)
- ✅ Redis Distributed Lock cho concurrency
- ✅ Kafka Event-Driven Architecture

---

## 🏗️ Kiến trúc hệ thống

### Services (5 Microservices - Consolidated)

```
┌──────────────────────┐
│   API Gateway        │
│   (Port 8000)        │
└──────────┬───────────┘
           │
    ┌──────┴──────────────────────────────┬─────────────────┐
    │                                      │                 │
┌───▼──────────────┐  ┌─────────▼────┐  ┌──▼────────┐  ┌────▼───────────┐
│  Event Booking   │  │   Payment    │  │ Ticketing │  │ Notification & │
│   Service        │  │   Service    │  │ Service   │  │  Analytics     │
│   (8001)         │  │   (8003)     │  │ (8004)    │  │   (8005)       │
│ Event + Seat Mgmt│  │ (Merged)     │  │(Merged)   │  │ (Email+Report) │
└──────────────────┘  └──────────────┘  └───────────┘  └────────────────┘
```

### Infrastructure (Docker)

- **4 PostgreSQL Databases** (Event, Payment, Ticketing, Notification)
- **1 Redis Instance** (Seat Lock)
- **1 Kafka Cluster** (Event Streaming)
- **1 pgAdmin** (Database GUI)

---

## 🔧 Công nghệ Stack

| Component          | Technology        |
| ------------------ | ----------------- |
| Framework          | Spring Boot 3.3.6 |
| Language           | Java 17 LTS       |
| API Communication  | REST + gRPC       |
| Database           | PostgreSQL 15     |
| Caching/Locking    | Redis 7 Alpine    |
| Message Queue      | Kafka 7.5         |
| QR Code Generation | ZXing             |
| Build Tool         | Maven 3.8+        |
| Code Generation    | Lombok 1.18.30    |

---

## 📋 Cấu trúc thư mục (5 Microservices - Consolidated)

```
event-ticketing-system/
├── docs/                            # Documentation files
│   ├── README.md                   # This file
│   ├── RUNNING_GUIDE.md            # Complete setup guide
│   ├── ARCHITECTURE.md             # System architecture
│   ├── FILE_STRUCTURE.md           # Detailed file structure
│   ├── CONSOLIDATED_STRUCTURE.md   # 5-service consolidation
│   ├── API_EXAMPLES.md             # API examples
│   ├── PROJECT_SUMMARY.md          # Project overview
│   └── INDEX.md                    # Project index
├── infra/                           # Infrastructure
│   └── docker-compose.yml          # Docker services (Postgres, Redis, Kafka, pgAdmin)
├── scripts/                         # Automation scripts
│   ├── setup.sh                    # Start infrastructure
│   ├── cleanup.sh                  # Clean up containers
│   └── start_all_services.sh       # Start all microservices
├── pom.xml                          # Parent Maven (consolidated)
├── common-library/                  # Shared DTOs & utilities
├── grpc-proto/                      # gRPC protobuf definitions
├── api-gateway/                     # API Gateway (Port 8000)
├── event-booking-service/           # Event + Seat (Consolidated) (Port 8001)
├── payment-service/                 # Payment Processing (Port 8003)
├── ticketing-service/               # Ticket + QR Code (Port 8004)
└── notification-analytics-service/  # Notification + Reporting (Consolidated) (Port 8005)
```

**🔄 Service Consolidation:**

- ✅ Event Service + Seat Allocation Service → `event-booking-service`
- ✅ Notification Service + Reporting Service → `notification-analytics-service`

---

## 🚀 Quick Start

### 1️⃣ Prerequisites

```bash
✅ Java 17 or higher
✅ Maven 3.8+
✅ Docker & Docker Compose
✅ Git
```

### 2️⃣ Start Infrastructure (Docker)

```bash
# Navigate to project root
cd event-ticketing-system

# Start infrastructure (using new scripts directory)
./scripts/setup.sh

# Or manually:
cd infra
docker-compose up -d
cd ..

# Verify containers are running
cd infra
docker-compose ps
cd ..

# Access pgAdmin
# URL: http://localhost:5050
# Email: admin@example.com
# Password: admin
```

**Ports sau khi start:**

- PostgreSQL Event DB: `5432`
- PostgreSQL Payment DB: `5433`
- PostgreSQL Ticketing DB: `5434`
- PostgreSQL Notification DB: `5435`
- Redis Cache: `6379`
- Kafka: `9092`
- Zookeeper: `2181`
- pgAdmin: `5050`

### 3️⃣ Build & Start Services

```bash
# Build all services
mvn clean install -DskipTests

# Or build specific service
cd event-booking-service
mvn spring-boot:run

# In another terminal - Payment Service
cd payment-service
mvn spring-boot:run

# And so on for other services...

# Or use the provided script to start all services
./scripts/start_all_services.sh
```

**Services sẽ start trên:**

- API Gateway: `http://localhost:8000`
- Event Booking Service: `http://localhost:8001` (Event + Seat Management)
- Payment Service: `http://localhost:8003`
- Ticketing Service: `http://localhost:8004`
- Notification & Analytics: `http://localhost:8005` (Email + Reporting)

---

## 📝 API Endpoints

### Event Service

```bash
# Create event
POST /api/events
{
  "name": "Concert 2024",
  "venueName": "City Arena",
  "description": "Live Music Event",
  "totalSeats": 1000
}

# Get all events
GET /api/events

# Get event by ID
GET /api/events/{eventId}
```

### Seat Allocation Service

```bash
# Hold seat (5 minutes)
POST /api/seats/hold?eventId=xxx&seatId=A1&userId=user123

# Get seat status
GET /api/seats/status?eventId=xxx&seatId=A1

# Release seat
POST /api/seats/release?eventId=xxx&seatId=A1&userId=user123

# Confirm seat (after payment)
POST /api/seats/confirm?eventId=xxx&seatId=A1&userId=user123
```

### Payment Service

```bash
# Create payment
POST /api/payments?userId=user123&eventId=xxx&amount=100&paymentMethod=CREDIT_CARD

# Confirm payment
POST /api/payments/{paymentId}/confirm?transactionId=txn_123

# Get payment status
GET /api/payments/{paymentId}
```

### Ticketing Service

```bash
# Create ticket
POST /api/tickets?eventId=xxx&seatId=A1&userId=user123&paymentId=pay_123

# Get ticket
GET /api/tickets/{ticketId}

# Get user tickets
GET /api/tickets/user/{userId}

# Check-in (scan QR)
POST /api/tickets/{ticketId}/checkin

# Get event tickets
GET /api/tickets/event/{eventId}
```

### Notification & Analytics Service (Consolidated)

```bash
# Send notification (Email)
POST /api/notifications/send?userId=user123&subject=Test&body=Hello

# Get analytics
GET /api/reports/analytics

# Get revenue reports
GET /api/reports/revenue

# Get event statistics
GET /api/reports/statistics/{eventId}
```

---

## 🔄 Luồng hoạt động (User Flow)

### 1. User chọn ghế → Hệ thống giữ ghế

```
User Request
    ↓
Seat Allocation Service
    ↓
┌─ Redis Lock: SET seat_lock:eventId:seatId = userId (NX, PX 300000)
├─ If success → Save to PostgreSQL with status HELD
└─ If fail → Throw SeatAlreadyHeldException
    ↓
Response: "Seat held for 5 minutes"
```

### 2. User thanh toán

```
Payment Request
    ↓
Payment Service
    ↓
├─ Create transaction with status PENDING
├─ Mock payment gateway validation
└─ Update status to CONFIRMED
    ↓
Publish Kafka Event: payment-confirmed
```

### 3. Payment confirmed → Tạo ticket

```
Kafka Event: payment-confirmed
    ↓
Ticketing Service (Listener)
    ↓
├─ Generate QR Code (ZXing library)
├─ Create Ticket record
├─ Generate QR Image (PNG)
└─ Update Seat status to SOLD
    ↓
Publish Kafka Event: ticket-created
```

### 4. Ticket created → Gửi email

```
Kafka Event: ticket-created
    ↓
Notification Service (Listener)
    ↓
├─ Parse event data
├─ Compose email
└─ Send to user
```

---

## 🔐 Concurrency Control - Redis Lock

### Redis NX Command

```java
// Đây là lệnh Redis được sử dụng:
SET seat_lock:eventId:seatId userId NX PX 300000

// NX: Only set if key does not exist
// PX: Expire in 300000ms (5 minutes)
// Automatically releases after 5 minutes
```

### Thread-safe Seat Hold

```java
Boolean locked = redisTemplate.opsForValue()
    .setIfAbsent(lockKey, userId, Duration.ofMinutes(5));

if (Boolean.FALSE.equals(locked)) {
    throw new SeatAlreadyHeldException(); // Already held
}

// If success, save to database
```

---

## 📊 Database Schema

### Event Service

**events table**

```sql
id (UUID)
name (String)
venueName (String)
startTime (LocalDateTime)
endTime (LocalDateTime)
description (Text)
totalSeats (int)
availableSeats (int)
soldSeats (int)
```

**seats table**

```sql
id (UUID)
eventId (FK)
row (String: A, B, C...)
col (int: 1, 2, 3...)
status (AVAILABLE, BLOCKED, SOLD)
heldBy (userId)
heldUntil (timestamp)
```

### Seat Allocation Service

**seat_reservations table**

```sql
id (UUID)
eventId (FK)
seatId (FK)
userId (FK)
status (HELD, CONFIRMED, RELEASED)
heldAt (LocalDateTime)
expiresAt (LocalDateTime)
confirmedAt (LocalDateTime)
```

### Payment Service

**transactions table**

```sql
id (UUID)
userId (FK)
eventId (FK)
amount (double)
status (PENDING, CONFIRMED, FAILED)
paymentMethod (String)
transactionId (From Gateway)
createdAt, updatedAt
```

### Ticketing Service

**tickets table**

```sql
id (UUID)
eventId (FK)
seatId (FK)
userId (FK)
paymentId (FK)
qrCode (String: TICKET:xxx:yyy:zzz:abc:timestamp)
qrCodeImage (BLOB: PNG image)
status (ACTIVE, USED, CANCELLED)
checkedInAt (LocalDateTime)
```

---

## 🧪 Testing

### Unit Tests

```bash
# Navigate to project root
cd event-ticketing-system

# Run all tests
mvn test

# Run specific service tests
mvn test -pl event-booking-service

# Run with coverage
mvn clean test jacoco:report
```

### Load Testing - Concurrency

Để test 100 users holding seats đồng thời trong Event Booking Service:

```bash
# Using Apache JMeter hoặc Gatling
# Test scenario:
# 1. 100 concurrent users
# 2. Hold same 5 seats simultaneously
# 3. Verify only 5 succeed, 95 fail (thanks to Redis lock)
```

---

## 🛠️ Configuration

### Redis Lock TTL

File: `event-booking-service/src/main/java/.../SeatAllocationService.java`

```java
private static final long HOLD_DURATION_MINUTES = 5;
```

Change to adjust hold time.

### Kafka Topics

Topics được tự động tạo:

- `payment-confirmed` - Khi thanh toán thành công
- `payment-failed` - Khi thanh toán thất bại
- `ticket-created` - Khi tạo vé thành công
- `notification-sent` - Khi gửi thông báo

---

## 📈 Monitoring & Logging

Tất cả services log to console. Có thể config Log Aggregation:

```yaml
# Thêm vào application.yml
logging:
  level:
    com.eventticket: DEBUG
    org.springframework: INFO
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

---

## 🚨 Common Issues

### ❌ PostgreSQL Connection Refused

```bash
# Verify containers running
cd infra
docker-compose ps

# Check logs
docker-compose logs postgres-event-db

# Restart containers
docker-compose restart
```

### ❌ Redis Connection Error

```bash
# Check Redis
docker exec redis-cache redis-cli ping
# Should return: PONG

# Or
redis-cli -h localhost -p 6379 ping
```

### ❌ Kafka Connection Error

```bash
# Check Kafka
docker exec kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# Or telnet
telnet localhost 9092
```

### ❌ Port Already in Use

```bash
# Find process using port (e.g., 8001)
lsof -i :8001

# Kill process
kill -9 <PID>

# Or change port in service application.yml
# server:
#   port: 8011
```

### ❌ Clean Up

```bash
# Stop all services and clean up
./scripts/cleanup.sh

# Or manually
cd infra
docker-compose down -v
cd ..
```

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Redis NX Command](https://redis.io/commands/set/)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [ZXing QR Code Library](https://github.com/zxing/zxing)

---

## 👥 Contributing

```bash
git clone <repo-url>
git checkout -b feature/your-feature
# Make changes
git commit -am "Add feature"
git push origin feature/your-feature
```

---

## 📄 License

MIT License - See LICENSE file

---

## ✨ Next Steps

- [ ] Add UI (React/Angular)
- [ ] Add gRPC implementation
- [ ] Add more comprehensive tests
- [ ] Add Docker Swarm/Kubernetes deployment
- [ ] Add Elasticsearch for logging
- [ ] Add Prometheus monitoring
- [ ] Add API rate limiting
- [ ] Add authentication/authorization

---

**Created:** December 2024  
**Version:** 1.0.0  
**Maintainer:** Event Ticketing Team
