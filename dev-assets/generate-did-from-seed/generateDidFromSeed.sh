#!/bin/bash

echo "Start the Docker container" 
docker-compose up -d
echo "Please open a new terminal and run the follwoing command:" 
echo ""
echo "   docker logs acapy_container -f "
echo ""
### Sleep for 180 seconds ###
sleep 180

echo "Docker container is stopping"
docker-compose down -v
echo "Docker container stopped and has been removed"
 