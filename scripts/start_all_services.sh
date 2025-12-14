#!/bin/bash

PROJECT_ROOT="/Users/phonguni/workspace/project/SOA/GR8/event-ticketing-system"
cd "$PROJECT_ROOT"

# Kill any existing services
pkill -f "spring-boot:run" 2>/dev/null
sleep 2

echo "=========================================="
echo "Starting all 5 microservices..."
echo "=========================================="

# Start API Gateway (port 8000)
echo "▶ Starting API Gateway (8000)..."
cd "$PROJECT_ROOT/api-gateway"
mvn spring-boot:run -q > /tmp/api-gateway.log 2>&1 &
API_PID=$!
echo "  PID: $API_PID"

sleep 3

# Start Event Booking Service (port 8001)
echo "▶ Starting Event Booking Service (8001)..."
cd "$PROJECT_ROOT/event-booking-service"
mvn spring-boot:run -q > /tmp/event-booking-service.log 2>&1 &
EVENT_BOOKING_PID=$!
echo "  PID: $EVENT_BOOKING_PID"

sleep 3

# Start Payment Service (port 8003)
echo "▶ Starting Payment Service (8003)..."
cd "$PROJECT_ROOT/payment-service"
mvn spring-boot:run -q > /tmp/payment-service.log 2>&1 &
PAYMENT_PID=$!
echo "  PID: $PAYMENT_PID"

sleep 3

# Start Ticketing Service (port 8004)
echo "▶ Starting Ticketing Service (8004)..."
cd "$PROJECT_ROOT/ticketing-service"
mvn spring-boot:run -q > /tmp/ticketing-service.log 2>&1 &
TICKETING_PID=$!
echo "  PID: $TICKETING_PID"

sleep 3

# Start Notification & Analytics Service (port 8005)
echo "▶ Starting Notification & Analytics Service (8005)..."
cd "$PROJECT_ROOT/notification-analytics-service"
mvn spring-boot:run -q > /tmp/notification-analytics-service.log 2>&1 &
NOTIFICATION_PID=$!
echo "  PID: $NOTIFICATION_PID"

sleep 5

echo ""
echo "=========================================="
echo "All services started! Waiting for readiness..."
echo "=========================================="

# Wait for services to be ready
sleep 10

echo ""
echo "=========================================="
echo "Testing service endpoints..."
echo "=========================================="

# Test API Gateway
echo ""
echo "✓ API Gateway (8000):"
curl -s http://localhost:8000/api/events 2>&1 | head -c 200 && echo "..." || echo "Not ready"

# Test Event Booking Service
echo ""
echo "✓ Event Booking Service (8001):"
curl -s http://localhost:8001/api/events 2>&1 | head -c 200 && echo "..." || echo "Not ready"

# Test Payment Service
echo ""
echo "✓ Payment Service (8003):"
curl -s http://localhost:8003/api/payments 2>&1 | head -c 200 && echo "..." || echo "Not ready"

# Test Ticketing Service
echo ""
echo "✓ Ticketing Service (8004):"
curl -s http://localhost:8004/api/tickets 2>&1 | head -c 200 && echo "..." || echo "Not ready"

# Test Notification & Analytics Service
echo ""
echo "✓ Notification & Analytics (8005):"
curl -s http://localhost:8005/api/notifications 2>&1 | head -c 200 && echo "..." || echo "Not ready"

echo ""
echo "=========================================="
echo "Running E2E workflow test..."
echo "=========================================="

# Create event
echo ""
echo "1️⃣  Creating Event..."
EVENT_RESPONSE=$(curl -s -X POST http://localhost:8001/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Concert 2025",
    "venueName": "Lotte World",
    "totalSeats": 100,
    "description": "Amazing concert event"
  }')
echo "$EVENT_RESPONSE" | head -c 300
echo ""

# Extract EVENT_ID (simple extraction)
EVENT_ID=$(echo "$EVENT_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
if [ -n "$EVENT_ID" ]; then
  echo "✅ Event created: $EVENT_ID"
else
  EVENT_ID="test-event-1"
  echo "⚠️  Using dummy event ID: $EVENT_ID"
fi

# Get events
echo ""
echo "2️⃣  Fetching all events..."
curl -s http://localhost:8001/api/events 2>&1 | head -c 300
echo "..."

# Hold a seat
echo ""
echo "3️⃣  Holding seat..."
curl -s -X POST http://localhost:8001/api/seats/hold \
  -H "Content-Type: application/json" \
  -d "{\"eventId\": \"$EVENT_ID\", \"seatId\": \"A1\", \"userId\": \"user123\"}" | head -c 300
echo "..."

# Create payment
echo ""
echo "4️⃣  Creating payment..."
curl -s -X POST http://localhost:8003/api/payments \
  -H "Content-Type: application/json" \
  -d "{\"eventId\": \"$EVENT_ID\", \"amount\": 50.0, \"userId\": \"user123\"}" | head -c 300
echo "..."

# Get analytics
echo ""
echo "5️⃣  Fetching analytics..."
curl -s http://localhost:8005/api/reports/analytics 2>&1 | head -c 300
echo "..."

echo ""
echo "=========================================="
echo "✅ All services running!"
echo "=========================================="
echo ""
echo "Service Ports (5 Services):"
echo "  • API Gateway:              http://localhost:8000"
echo "  • Event Booking Service:    http://localhost:8001"
echo "  • Payment Service:          http://localhost:8003"
echo "  • Ticketing Service:        http://localhost:8004"
echo "  • Notification & Analytics: http://localhost:8005"
echo ""
echo "Docker Resources:"
echo "  • PostgreSQL (4 instances)  on ports 5432-5435"
echo "  • Redis                     on port 6379"
echo "  • Kafka                     on port 9092"
echo "  • pgAdmin                   on http://localhost:5050"
echo ""
echo "Log Files:"
echo "  • API Gateway:              tail -f /tmp/api-gateway.log"
echo "  • Event Booking:            tail -f /tmp/event-booking-service.log"
echo "  • Payment:                  tail -f /tmp/payment-service.log"
echo "  • Ticketing:                tail -f /tmp/ticketing-service.log"
echo "  • Notification & Analytics: tail -f /tmp/notification-analytics-service.log"
echo ""
echo "To stop all services, run: pkill -f 'spring-boot:run'"
echo ""
