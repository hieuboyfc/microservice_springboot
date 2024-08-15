@echo off
echo --- Starting Docker Compose...
@echo off

:: Khởi tạo Network Kong
echo --- Create Network Kong...
docker network create --driver bridge kong-network
echo Waiting for Network Kong to create...
timeout /t 2 /nobreak > NUL

:: Dừng Postgres SQL
echo Stopping Kong Database PostgresSQL...
docker-compose down kong_database
timeout /t 2 /nobreak > NUL

:: Khởi động Kong Database Postgres SQL
echo Starting Kong Database PostgresSQL...
docker-compose up -d kong_database
echo Waiting for Kong Database PostgresSQL to start...
timeout /t 10 /nobreak > NUL

:: Khởi động Kong Migrations
echo Starting Kong Migrations...
docker run --rm --network=kong-network ^
 -e "KONG_DATABASE=postgres" ^
 -e "KONG_PG_HOST=kong_database" ^
 -e "KONG_PG_PASSWORD=vip.hieuboy" ^
 -e "KONG_PASSWORD=test" ^
 kong/kong-gateway:3.7.1.2 kong migrations bootstrap

echo Settings License...
set KONG_LICENSE_DATA={"license":{"payload":{"admin_seats":"1","customer":"Example Company, Inc","dataplanes":"1","license_creation_date":"2024-08-15","license_expiration_date":"2025-08-15","license_key":"00141000017ODj3AAG_a1V41000004wT0OEAU","product_subscription":"Konnect Enterprise","support_plan":"None"},"signature":"6985968131533a967fcc721244a979948b1066967f1e9cd65dbd8eeabe060fc32d894a2945f5e4a03c1cd2198c74e058ac63d28b045c2f1fcec95877bd790e1b","version":"1"}}
echo Waiting for Kong Migrations to start...
timeout /t 2 /nobreak > NUL

:: Dừng Kong Gateway
echo Stopping Kong Gateway...
docker-compose down kong_gateway
timeout /t 2 /nobreak > NUL

:: Khởi động Kong Gateway
echo Starting Kong Gateway...
docker-compose up -d kong_gateway
echo Waiting for Kong Gateway to start...
timeout /t 10 /nobreak > NUL

echo All Services have been started!
