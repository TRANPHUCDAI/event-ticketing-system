# 📂 Cấu Trúc Dự Án Event Ticketing System

## 🗂️ Thư Mục Chính

```
event-ticketing-system/
├── 📁 docs/                          # Documentation
│   ├── README.md                     # Hướng dẫn chính
│   ├── RUNNING_GUIDE.md              # Cách chạy dự án
│   ├── ARCHITECTURE.md               # Kiến trúc hệ thống
│   ├── API_EXAMPLES.md               # Ví dụ API
│   ├── CONSOLIDATED_STRUCTURE.md     # Cấu trúc đã tóm gọn
│   └── FILE_STRUCTURE.md             # Chi tiết cấu trúc file
│
├── 📁 infra/                         # Infrastructure (Docker, Config)
│   └── docker-compose.yml            # Docker Compose configuration
│
├── 📁 scripts/                       # Automation Scripts
│   ├── setup.sh                      # Tự động setup dự án
│   └── cleanup.sh                    # Dọn dẹp containers
│
├── 📁 api-gateway/                   # API Gateway Service (Port 8000)
│   ├── src/
│   ├── target/
│   └── pom.xml
│
├── 📁 common-library/                # Shared Code & DTOs
│   ├── src/
│   ├── target/
│   └── pom.xml
│
├── 📁 grpc-proto/                    # gRPC Protocol Definitions
│   ├── src/
│   ├── target/
│   └── pom.xml
│
├── 📁 event-booking-service/         # Event Booking Service (Port 8001)
│   │                                 # (Event + Seat Management merged)
│   ├── src/main/java/com/eventticket/eventbooking/
│   │   ├── controller/               # REST Controllers
│   │   ├── service/                  # Business Logic
│   │   ├── entity/                   # JPA Entities (Event, Seat)
│   │   ├── repository/               # Data Access
│   │   ├── config/                   # Configuration
│   │   └── seat/                     # Seat Allocation Logic
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── target/
│   └── pom.xml
│
├── 📁 payment-service/               # Payment Service (Port 8003)
│   ├── src/main/java/com/eventticket/payment/
│   │   ├── controller/               # REST Controllers
│   │   ├── service/                  # Payment Processing Logic
│   │   ├── entity/                   # PaymentTransaction Entity
│   │   ├── repository/               # Data Access
│   │   └── config/                   # Kafka Configuration
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── target/
│   └── pom.xml
│
├── 📁 ticketing-service/             # Ticketing Service (Port 8004)
│   ├── src/main/java/com/eventticket/ticketing/
│   │   ├── controller/               # REST Controllers
│   │   ├── service/                  # Ticket & QR Code Logic
│   │   ├── entity/                   # Ticket Entity
│   │   ├── repository/               # Data Access
│   │   └── config/                   # Kafka Configuration
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── target/
│   └── pom.xml
│
├── 📁 notification-analytics-service/  # Notification & Analytics (Port 8005)
│   │                                   # (Notification + Reporting merged)
│   ├── src/main/java/com/eventticket/notificationanalytics/
│   │   ├── controller/                 # REST Controllers
│   │   ├── service/                    # Business Logic
│   │   ├── config/                     # Kafka & Mail Configuration
│   │   ├── notification/               # Email Notification Logic
│   │   │   └── service/
│   │   └── reporting/                  # Analytics & Reports
│   │       ├── controller/
│   │       ├── service/
│   │       ├── config/
│   │       └── dto/
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── target/
│   └── pom.xml
│
├── 📁 .github/                       # GitHub Configuration
│   └── workflows/                    # CI/CD Workflows (if any)
│
├── pom.xml                           # Parent POM (Maven)
└── .gitignore                        # Git Ignore

```

## 🚀 Quick Start

### 1. Setup (Tự động)

```bash
cd scripts
./setup.sh
```

### 2. Run Docker Infrastructure

```bash
cd infra
docker-compose up -d
cd ..
```

### 3. Run Services (Mở 5 terminal riêng)

**Terminal 1: Event Booking Service**

```bash
cd event-booking-service && mvn spring-boot:run
```

**Terminal 2: Payment Service**

```bash
cd payment-service && mvn spring-boot:run
```

**Terminal 3: Ticketing Service**

```bash
cd ticketing-service && mvn spring-boot:run
```

**Terminal 4: Notification & Analytics Service**

```bash
cd notification-analytics-service && mvn spring-boot:run
```

**Terminal 5: API Gateway**

```bash
cd api-gateway && mvn spring-boot:run
```

### 4. Cleanup

```bash
cd scripts
./cleanup.sh
```

## 📊 Services Overview

| Service                      | Port | Chức Năng             |
| ---------------------------- | ---- | --------------------- |
| **API Gateway**              | 8000 | Định tuyến requests   |
| **Event Booking Service**    | 8001 | Quản lý sự kiện + ghế |
| **Payment Service**          | 8003 | Xử lý thanh toán      |
| **Ticketing Service**        | 8004 | Quản lý vé + QR Code  |
| **Notification & Analytics** | 8005 | Email + Báo cáo       |

## 🗄️ Databases

| Service                  | Database                 | Port |
| ------------------------ | ------------------------ | ---- |
| Event Booking            | postgres-event-db        | 5432 |
| Payment                  | postgres-payment-db      | 5433 |
| Ticketing                | postgres-ticketing-db    | 5434 |
| Notification & Analytics | postgres-notification-db | 5435 |

## 📚 Documentation Files

- **README.md** - Tổng quan dự án
- **RUNNING_GUIDE.md** - Hướng dẫn chi tiết cách chạy
- **ARCHITECTURE.md** - Kiến trúc và thiết kế
- **API_EXAMPLES.md** - Ví dụ API calls
- **CONSOLIDATED_STRUCTURE.md** - Thay đổi từ 8 services → 5 services

## 🔧 Tools & Technologies

- **Build Tool**: Maven
- **Java Version**: 17 LTS
- **Spring Boot**: 3.3.6
- **Database**: PostgreSQL 15
- **Cache/Lock**: Redis 7
- **Message Queue**: Kafka
- **IaC**: Docker Compose
- **Annotation**: Lombok

## 📝 Notes

- Tất cả markdown docs nằm trong thư mục `docs/`
- Docker config nằm trong thư mục `infra/`
- Scripts tự động nằm trong thư mục `scripts/`
- Các services được organize theo chức năng rõ ràng
- Mỗi service là một module Maven độc lập

---

**Cập nhật lần cuối:** December 11, 2025
