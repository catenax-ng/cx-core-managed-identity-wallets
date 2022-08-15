#!/bin/bash

echo "Start the Docker container" 
docker-compose up -d
sleep 20
docker logs acapy_container -f
echo "Docker container is stopping"
docker-compose down -v
echo "Docker container stopped and has been removed"
