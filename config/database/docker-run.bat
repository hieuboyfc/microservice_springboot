@echo off
echo --- Starting Docker Compose...
@echo off

:: Khởi động Consul
echo Starting PostgresSQL...
docker-compose up -d postgres
echo Waiting for PostgresSQL to start...
timeout /t 15 /nobreak > NUL

echo All Services have been started!
