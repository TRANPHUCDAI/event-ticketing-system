#!/bin/bash

# Cleanup script - Remove all containers and volumes
# WARNING: This will delete all data!

echo "======================================"
echo "Event Ticketing System - Cleanup"
echo "======================================"
echo ""
echo "⚠️  WARNING: This will delete all containers and volumes!"
echo "⚠️  All data will be lost!"
echo ""

read -p "Are you sure? (yes/no): " confirm

if [ "$confirm" = "yes" ]; then
    echo ""
    echo "→ Stopping containers..."
    cd infra
    docker-compose down -v
    cd ..
    
    echo "✓ Cleanup completed"
else
    echo "✗ Cleanup cancelled"
fi
