@echo off
setlocal

echo Start configuration gateway.

:: Địa chỉ IP hoặc tên miền của API Gateway
set API_GATEWAY_IP=localhost

:: Thay đổi tên dịch vụ và route nếu cần thiết
set SERVICE_NAME=api-gateway
set ROUTE_PATH=/zimji

:: Xóa dịch vụ cũ nếu có
curl -X DELETE http://localhost:8001/services/%SERVICE_NAME%

:: Xóa routes cũ nếu có
curl -X DELETE http://localhost:8001/routes/%ROUTE_PATH%

:: Cấu hình Kong - Thêm Service
echo --- Adding service to Kong...
curl -X POST http://localhost:8001/services ^
  --data "name=%SERVICE_NAME%" ^
  --data "url=http://%API_GATEWAY_IP%:1997"
timeout /t 5

:: Cấu hình Kong - Thêm Route
echo --- Adding route to Kong...
curl -X POST http://localhost:8001/services/%SERVICE_NAME%/routes ^
  --data "paths[]=%ROUTE_PATH%" ^
  --data "methods[]=GET" ^
  --data "methods[]=POST" ^
  --data "methods[]=DELETE" ^
  --data "methods[]=PUT"
timeout /t 5

echo Configuration gateway complete.
endlocal
pause
