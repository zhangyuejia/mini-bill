# ============================
# Stage 1: 构建后端 (Maven)
# ============================
FROM maven:3.9-eclipse-temurin-17 AS backend-builder
WORKDIR /build

# 先复制 pom 文件，利用 Docker 缓存层加速
COPY backend/pom.xml ./
COPY backend/mini-bill-common/pom.xml ./mini-bill-common/
COPY backend/mini-bill-api/pom.xml ./mini-bill-api/
COPY backend/mini-bill-system/pom.xml ./mini-bill-system/
COPY backend/mini-bill-bus/pom.xml ./mini-bill-bus/
COPY backend/mini-bill-gateway/pom.xml ./mini-bill-gateway/

# 下载依赖（利用缓存，代码未变则不重复下载）
RUN mvn dependency:go-offline -B -q || true

# 复制源码并构建
COPY backend/ ./
RUN mvn clean package -DskipTests -B -q -pl mini-bill-gateway,mini-bill-system,mini-bill-bus -am

# ============================
# Stage 2: 构建前端 (Node)
# ============================
FROM node:22-alpine AS frontend-builder
WORKDIR /build

# Alpine 需要这些包来编译 sass 等原生模块
RUN apk add --no-cache python3 make g++

COPY frontend/package.json frontend/package-lock.json ./
# 使用 npm ci（lockfile 精确锁定版本，构建更可靠）
RUN npm ci

COPY frontend/ ./
RUN npm run build

# ============================
# Stage 3: 最终运行镜像
# ============================
FROM eclipse-temurin:17-jre

# 安装 nginx + curl（healthcheck 需要）
RUN apt-get update \
    && apt-get install -y --no-install-recommends nginx curl \
    && rm -f /etc/nginx/sites-enabled/default \
    && rm -rf /var/lib/apt/lists/*

# 创建工作目录
WORKDIR /app
RUN mkdir -p /app/logs /app/upload

# 复制后端 JAR 包
COPY --from=backend-builder /build/mini-bill-gateway/target/*.jar /app/backend/mini-bill-gateway.jar
COPY --from=backend-builder /build/mini-bill-system/target/*.jar /app/backend/mini-bill-system.jar
COPY --from=backend-builder /build/mini-bill-bus/target/*.jar /app/backend/mini-bill-bus.jar

# 复制前端构建产物
COPY --from=frontend-builder /build/dist /app/frontend/dist

# 复制 nginx 配置与启动脚本（conf.d 路径兼容所有 Debian/Ubuntu 版本）
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY docker/startup.sh /app/startup.sh
# 转换换行符为 LF 并设置可执行权限
RUN sed -i 's/\r$//' /app/startup.sh && chmod +x /app/startup.sh

EXPOSE 80 8080 8081 8082

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:80/ || exit 1

CMD ["/app/startup.sh"]
