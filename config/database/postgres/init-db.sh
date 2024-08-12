#!/bin/bash
set -e

# Tạo cơ sở dữ liệu thứ nhất
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "CREATE DATABASE IF NOT EXISTS auth_service;"

# Tạo cơ sở dữ liệu thứ hai
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "CREATE DATABASE IF NOT EXISTS movie_service;"
