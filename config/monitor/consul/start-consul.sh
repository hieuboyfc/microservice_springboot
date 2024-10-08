#!/bin/sh

# Tạo thư mục logs nếu nó không tồn tại
mkdir -p /consul/logs

# Khởi động Consul
consul agent -config-file=/consul/consul-config.json

# Lưu PID của Consul
CONSUL_PID=$!

# Chờ một khoảng thời gian để Consul chạy dịch vụ (10 giây)
echo "Chờ 10 giây để Consul chạy dịch vụ..."
sleep 10

# Chờ Consul kết thúc (giữ cho container hoạt động)
wait $CONSUL_PID

chmod +x start-consul.sh