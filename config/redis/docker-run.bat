@echo off
echo Starting Docker Compose...
docker-compose up -d

echo Waiting for Redis to start...
timeout /t 30

echo Creating Redis Cluster...
docker exec -it redis-6380 redis-cli --cluster create ^
  redis-6380:6380 ^
  redis-6381:6381 ^
  redis-6382:6382 ^
  redis-6383:6383 ^
  redis-6384:6384 ^
  redis-6385:6385 ^
  --cluster-replicas 1
timeout /t 3

echo Redis Cluster setup complete.
