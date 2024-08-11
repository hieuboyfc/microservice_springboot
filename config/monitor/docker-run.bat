@echo off
echo --- Starting Docker Compose...
@echo off

:: Khởi động Consul
echo Starting Consul...
docker-compose up -d consul
echo Waiting for Consul to start...
timeout /t 15 /nobreak > NUL

:: Khởi động Loki
echo Starting Loki...
docker-compose up -d loki
echo Waiting for Loki to start...
timeout /t 15 /nobreak > NUL

:: Khởi động Promtail
echo Starting Promtail...
docker-compose up -d promtail
echo Waiting for Promtail to start...
timeout /t 15 /nobreak > NUL

:: Khởi động Grafana
echo Starting Grafana...
docker-compose up -d grafana
echo Waiting for Grafana to start...
timeout /t 15 /nobreak > NUL

echo All Services have been started!

echo --- Waiting for Consul to start...
timeout /t 15 /nobreak > NUL

:: Dang ky Dich vu Loki
echo Registering Loki service...
docker exec -i consul consul services register /consul/loki-service.json
timeout /t 10 /nobreak > NUL

:: Dang ky Dich vu Loki
echo Registering Promtail service...
docker exec -i consul consul services register /consul/promtail-service.json
timeout /t 10 /nobreak > NUL

echo All Services have been registered!
