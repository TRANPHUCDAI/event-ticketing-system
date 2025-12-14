#!/bin/bash

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Starting Event Ticketing System (without Docker)${NC}"
echo ""

# Function to start service
start_service() {
    local service_name=$1
    local port=$2
    local jar_file=$3
    
    echo -e "${YELLOW}📦 Starting $service_name (port $port)...${NC}"
    
    cd "/Users/phonguni/workspace/project/SOA/GR8/event-ticketing-system/$service_name"
    
    nohup java -jar "target/$jar_file" \
        --server.port=$port \
        > "../../logs/$service_name.log" 2>&1 &
    
    local pid=$!
    echo -e "${GREEN}✓ $service_name started (PID: $pid)${NC}"
}

# Create logs directory
mkdir -p /Users/phonguni/workspace/project/SOA/GR8/event-ticketing-system/logs

# Build all services first
echo -e "${YELLOW}🔨 Building all services...${NC}"
cd /Users/phonguni/workspace/project/SOA/GR8/event-ticketing-system
mvn clean install -DskipTests -q

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Build failed!${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Build successful!${NC}"
echo ""

# Start services
sleep 2
start_service "api-gateway" "8000" "api-gateway-1.0.0.jar"
sleep 2
start_service "event-booking-service" "8001" "event-booking-service-1.0.0.jar"
sleep 2
start_service "payment-service" "8003" "payment-service-1.0.0.jar"
sleep 2
start_service "ticketing-service" "8004" "ticketing-service-1.0.0.jar"
sleep 2
start_service "notification-analytics-service" "8005" "notification-analytics-service-1.0.0.jar"

echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║          ALL SERVICES STARTED SUCCESSFULLY! 🎉              ║${NC}"
echo -e "${GREEN}╠════════════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║  API Gateway:         http://localhost:8000                 ║${NC}"
echo -e "${GREEN}║  Event Booking:       http://localhost:8001                 ║${NC}"
echo -e "${GREEN}║  Payment Service:     http://localhost:8003                 ║${NC}"
echo -e "${GREEN}║  Ticketing Service:   http://localhost:8004                 ║${NC}"
echo -e "${GREEN}║  Notification:        http://localhost:8005                 ║${NC}"
echo -e "${GREEN}║                                                              ║${NC}"
echo -e "${GREEN}║  Frontend:            http://localhost:3000                 ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo "📝 Logs location: /Users/phonguni/workspace/project/SOA/GR8/event-ticketing-system/logs/"
echo ""
echo "To view logs: tail -f logs/<service>.log"
echo "To stop services: killall java"
