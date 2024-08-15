@echo off
echo --- Starting Docker Compose...
@echo off

:: Khởi tạo Network Postgres
echo --- Create Network Postgres...
docker network create --driver bridge postgres-network
echo Waiting for Network Postgres to create...
timeout /t 2 /nobreak > NUL

:: Dừng Postgres SQL
echo Stopping PostgresSQL...
docker-compose down postgres
timeout /t 2 /nobreak > NUL

:: Khởi động Postgres SQL
echo Starting PostgresSQL...
docker-compose up -d postgres
echo Waiting for PostgresSQL to start...
timeout /t 10 /nobreak > NUL

echo All Services have been started!
