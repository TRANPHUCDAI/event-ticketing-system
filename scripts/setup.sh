#!/bin/bash

# Setup script for Event Ticketing System
# This script initializes the entire project

set -e

echo "======================================"
echo "Event Ticketing System - Setup Script"
echo "======================================"
echo ""

# Check prerequisites
echo "✓ Checking prerequisites..."

if ! command -v docker &> /dev/null; then
    echo "✗ Docker is not installed"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "✗ Maven is not installed"
    exit 1
fi

if ! command -v java &> /dev/null; then
    echo "✗ Java is not installed"
    exit 1
fi

echo "✓ All prerequisites met"
echo ""

# Start Docker containers
echo "→ Starting Docker containers..."
cd infra
docker-compose up -d
cd ..

echo "✓ Waiting for databases to be ready..."
sleep 10

# Build all services
echo "→ Building Maven project..."
mvn clean install -DskipTests

echo ""
echo "======================================"
echo "Services are ready to start (5 Services):"
echo "======================================"
echo ""
echo "Start each service in separate terminal:"
echo ""
echo "1. API Gateway (8000):"
echo "   cd api-gateway && mvn spring-boot:run"
echo ""
echo "2. Event Booking Service (8001):"
echo "   cd event-booking-service && mvn spring-boot:run"
echo ""
echo "3. Payment Service (8003):"
echo "   cd payment-service && mvn spring-boot:run"
echo ""
echo "4. Ticketing Service (8004):"
echo "   cd ticketing-service && mvn spring-boot:run"
echo ""
echo "5. Notification & Analytics Service (8005):"
echo "   cd notification-analytics-service && mvn spring-boot:run"
echo ""
echo "======================================"
echo "Access Points:"
echo "======================================"
echo "API Gateway: http://localhost:8000"
echo "pgAdmin: http://localhost:5050"
echo "======================================"
