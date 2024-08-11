@echo off
echo Starting Docker Compose...
docker-compose up -d

echo Waiting for Grafana to start...
timeout /t 30

echo Grafana setup complete.
