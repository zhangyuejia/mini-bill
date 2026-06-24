#!/bin/bash
set -e

echo "========================================"
echo "  财小账 (Mini-Bill) 容器启动中..."
echo "========================================"
echo ""

# JVM 通用参数
JVM_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
JVM_OPTS="$JVM_OPTS -Djava.security.egd=file:/dev/./urandom"
JVM_OPTS="$JVM_OPTS -Duser.timezone=Asia/Shanghai"

# -------------------------------------------
# 1. 启动 System 服务 (8081)
# -------------------------------------------
echo "[1/4] 启动 System 服务 (端口 8081)..."
java $JVM_OPTS \
    -jar /app/backend/mini-bill-system.jar \
    --logging.file.name=/app/logs/mini-bill-system.log \
    > /app/logs/system-stdout.log 2>&1 &
SYSTEM_PID=$!
echo "  System PID: $SYSTEM_PID"

# 等待 System 服务启动
sleep 8
if ! kill -0 $SYSTEM_PID 2>/dev/null; then
    echo "  [ERROR] System 服务启动失败！"
    cat /app/logs/system-stdout.log
    exit 1
fi
echo "  System 服务已启动"

# -------------------------------------------
# 2. 启动 Bus 服务 (8082)
# -------------------------------------------
echo "[2/4] 启动 Bus 服务 (端口 8082)..."
java $JVM_OPTS \
    -jar /app/backend/mini-bill-bus.jar \
    --logging.file.name=/app/logs/mini-bill-bus.log \
    > /app/logs/bus-stdout.log 2>&1 &
BUS_PID=$!
echo "  Bus PID: $BUS_PID"

sleep 8
if ! kill -0 $BUS_PID 2>/dev/null; then
    echo "  [ERROR] Bus 服务启动失败！"
    cat /app/logs/bus-stdout.log
    exit 1
fi
echo "  Bus 服务已启动"

# -------------------------------------------
# 3. 启动 Gateway 服务 (8080)
# -------------------------------------------
echo "[3/4] 启动 Gateway 服务 (端口 8080)..."
# 等待 Nacos 注册完成
sleep 5
java $JVM_OPTS \
    -jar /app/backend/mini-bill-gateway.jar \
    --logging.file.name=/app/logs/gateway.log \
    > /app/logs/gateway-stdout.log 2>&1 &
GATEWAY_PID=$!
echo "  Gateway PID: $GATEWAY_PID"

sleep 8
if ! kill -0 $GATEWAY_PID 2>/dev/null; then
    echo "  [ERROR] Gateway 服务启动失败！"
    cat /app/logs/gateway-stdout.log
    exit 1
fi
echo "  Gateway 服务已启动"

# -------------------------------------------
# 4. 启动 Nginx (前台运行，保持容器存活)
# -------------------------------------------
echo "[4/4] 启动 Nginx (端口 80)..."
echo ""
echo "========================================"
echo "  所有服务启动完成！"
echo "  - Nginx (前端):    0.0.0.0:80"
echo "  - Gateway:         0.0.0.0:8080"
echo "  - System Service:  0.0.0.0:8081"
echo "  - Bus Service:     0.0.0.0:8082"
echo "========================================"
echo ""

# Nginx 前台运行（容器主进程）
exec nginx -g "daemon off;"
