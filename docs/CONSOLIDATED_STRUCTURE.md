# Event Ticketing System - Cấu Trúc Tóm Gọn (5 Services)

## 📦 Các Microservices Chính

### 1. **API Gateway** (Port 8000)

- Cổng vào duy nhất cho tất cả các request
- Định tuyến đến các service tương ứng
- Xử lý authentication/authorization

### 2. **Event Booking Service** (Port 8001)

- **Hợp nhất từ:** Event Service + Seat Allocation Service
- Chức năng:
  - Quản lý sự kiện (Event)
  - Quản lý ghế (Seat)
  - Cấp phát ghế với Redis locking (5-minute TTL)
  - Xử lý hủy bỏ giữ chỗ tự động
- Database: PostgreSQL
- Cache: Redis

### 3. **Payment Service** (Port 8003)

- Xử lý thanh toán
- Mock payment gateway
- Phát hành Kafka events (payment-confirmed, payment-failed)

### 4. **Ticketing Service** (Port 8004)

- Tạo vé (Ticket)
- Sinh QR Code (ZXing library)
- Lắng nghe Kafka events từ Payment Service
- Check-in endpoint

### 5. **Notification & Analytics Service** (Port 8005)

- **Hợp nhất từ:** Notification Service + Reporting Service
- Chức năng:
  - Lắng nghe Kafka events
  - Gửi email (mock)
  - Tạo báo cáo và analytics
  - WebClient configuration

## 🗂️ Cấu Trúc Thư Mục

```
event-ticketing-system/
├── api-gateway/                      # Gateway
├── common-library/                   # Shared DTOs & Utilities
├── grpc-proto/                       # gRPC Definitions
├── event-booking-service/            # Event + Seat Management
│   ├── src/main/java/
│   │   └── com/eventticket/eventbooking/
│   │       ├── controller/           # REST Controllers
│   │       ├── service/              # Business Logic
│   │       ├── entity/               # JPA Entities
│   │       ├── repository/           # Data Access
│   │       ├── config/               # Configuration
│   │       └── seat/                 # Seat Allocation Logic
│   └── pom.xml
├── payment-service/                  # Payment Processing
├── ticketing-service/                # Ticket & QR Code
└── notification-analytics-service/   # Notifications + Analytics
    ├── src/main/java/
    │   └── com/eventticket/notificationanalytics/
    │       ├── controller/
    │       ├── service/
    │       ├── config/
    │       ├── notification/         # Email Service
    │       └── reporting/            # Analytics & Reports
    └── pom.xml
```

## 🔧 Docker Compose Services

- **postgres-event-db** (5432) - Event Booking Service DB
- **postgres-payment-db** (5433) - Payment Service DB
- **postgres-ticketing-db** (5434) - Ticketing Service DB
- **postgres-notification-db** (5435) - Notification & Analytics DB
- **redis-cache** (6379) - Seat Locking & Caching
- **kafka** (9092) - Event Streaming
- **zookeeper** (2181) - Kafka Coordination
- **pgadmin** (5050) - PostgreSQL Management UI

## 📊 Thay Đổi Chính

| Trước                                        | Sau                                  |
| -------------------------------------------- | ------------------------------------ |
| 6 Services                                   | 5 Services                           |
| Event Service + Seat Allocation Service (2)  | Event Booking Service (1)            |
| Notification Service + Reporting Service (2) | Notification & Analytics Service (1) |
| 6 PostgreSQL Databases                       | 4 PostgreSQL Databases               |

## 🚀 Lợi Ích

✅ Giảm số lượng service cần quản lý
✅ Giảm overhead database (4 thay vì 6)
✅ Logic Event + Seat được tập trung
✅ Notification + Analytics tích hợp dễ dàng
✅ Cấu trúc dễ hiểu và maintain hơn
✅ Vẫn giữ được tính modular và scalable
