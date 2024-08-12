#!/bin/bash
set -e

# Tạo Schema thứ nhất
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "CREATE SCHEMA IF NOT EXISTS auth_service;"

# Tạo Schema thứ hai
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "CREATE SCHEMA IF NOT EXISTS movie_service;"
