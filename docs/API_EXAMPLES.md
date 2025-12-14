# API Test Examples for Event Ticketing System

## 1. CREATE EVENT

```bash
curl -X POST http://localhost:8000/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rock Concert 2024",
    "venueName": "City Arena",
    "description": "Amazing rock music performance",
    "totalSeats": 1000
  }'
```

**Response:**

```json
{
  "success": true,
  "message": "Event created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Rock Concert 2024",
    "venueName": "City Arena",
    "totalSeats": 1000,
    "availableSeats": 1000,
    "soldSeats": 0
  }
}
```

---

## 2. GET ALL EVENTS

```bash
curl http://localhost:8000/api/events
```

---

## 3. HOLD SEAT (5 minutes)

```bash
curl -X POST "http://localhost:8000/api/seats/hold?eventId=550e8400-e29b-41d4-a716-446655440000&seatId=A1&userId=user123"
```

**Response:**

```json
{
  "success": true,
  "message": "Seat held successfully for 5 minutes",
  "data": {
    "id": "A1",
    "eventId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "BLOCKED",
    "heldBy": "user123",
    "heldUntil": 1702123456789
  }
}
```

---

## 4. GET SEAT STATUS

```bash
curl "http://localhost:8000/api/seats/status?eventId=550e8400-e29b-41d4-a716-446655440000&seatId=A1"
```

---

## 5. CREATE PAYMENT

```bash
curl -X POST "http://localhost:8000/api/payments?userId=user123&eventId=550e8400-e29b-41d4-a716-446655440000&amount=100&paymentMethod=CREDIT_CARD"
```

**Response:**

```json
{
  "success": true,
  "message": "Payment created successfully",
  "data": "pay_550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 6. CONFIRM PAYMENT

```bash
curl -X POST "http://localhost:8000/api/payments/pay_550e8400-e29b-41d4-a716-446655440000/confirm?transactionId=txn_12345"
```

---

## 7. CREATE TICKET

```bash
curl -X POST "http://localhost:8000/api/tickets?eventId=550e8400-e29b-41d4-a716-446655440000&seatId=A1&userId=user123&paymentId=pay_550e8400-e29b-41d4-a716-446655440000"
```

**Response:**

```json
{
  "success": true,
  "message": "Ticket created successfully",
  "data": {
    "id": "ticket_xxx",
    "eventId": "550e8400-e29b-41d4-a716-446655440000",
    "seatId": "A1",
    "userId": "user123",
    "qrCode": "TICKET:550e:440:A1:user:1702123456789",
    "status": "ACTIVE",
    "createdAt": 1702123456789
  }
}
```

---

## 8. GET TICKET

```bash
curl http://localhost:8000/api/tickets/ticket_xxx
```

---

## 9. CHECK-IN (Scan QR)

```bash
curl -X POST http://localhost:8000/api/tickets/ticket_xxx/checkin
```

**Response:**

```json
{
  "success": true,
  "message": "Check-in successful",
  "data": {
    "id": "ticket_xxx",
    "status": "USED",
    "checkedInAt": "2024-12-10T15:30:00"
  }
}
```

---

## 10. GET EVENT REPORT

```bash
curl http://localhost:8000/api/reports/events/550e8400-e29b-41d4-a716-446655440000
```

**Response:**

```json
{
  "success": true,
  "data": {
    "eventId": "550e8400-e29b-41d4-a716-446655440000",
    "eventName": "Rock Concert 2024",
    "totalSeats": 1000,
    "soldSeats": 150,
    "availableSeats": 850,
    "totalRevenue": 15000.0,
    "occupancyRate": 15.0
  }
}
```

---

## Postman Collection

Import này vào Postman để test dễ hơn:

```json
{
  "info": {
    "name": "Event Ticketing API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Create Event",
      "request": {
        "method": "POST",
        "url": "http://localhost:8000/api/events"
      }
    }
  ]
}
```

---

## Using cURL with variables

```bash
# Set variables
BASE_URL="http://localhost:8000"
EVENT_ID="550e8400-e29b-41d4-a716-446655440000"
USER_ID="user123"
SEAT_ID="A1"

# Hold seat
curl -X POST "$BASE_URL/api/seats/hold?eventId=$EVENT_ID&seatId=$SEAT_ID&userId=$USER_ID"

# Create payment
PAYMENT_ID=$(curl -X POST "$BASE_URL/api/payments?userId=$USER_ID&eventId=$EVENT_ID&amount=100&paymentMethod=CREDIT_CARD" | jq -r '.data')

# Confirm payment
curl -X POST "$BASE_URL/api/payments/$PAYMENT_ID/confirm?transactionId=txn_12345"

# Create ticket
TICKET_ID=$(curl -X POST "$BASE_URL/api/tickets?eventId=$EVENT_ID&seatId=$SEAT_ID&userId=$USER_ID&paymentId=$PAYMENT_ID" | jq -r '.data.id')

# Check-in
curl -X POST "$BASE_URL/api/tickets/$TICKET_ID/checkin"
```
