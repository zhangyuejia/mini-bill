<#
.SYNOPSIS
    财小账 (Mini-Bill) Docker 一键部署脚本
.DESCRIPTION
    自动构建前后端 → 打包 Docker 镜像 → 替换容器部署 → 设置自启动
    日志映射到 D:\Program\mini-bill\logs
    对外端口 = 内部端口 + 1000，避免与本地调试冲突
#>

$ErrorActionPreference = "Continue"
$Host.UI.RawUI.WindowTitle = "财小账 Docker 部署"

# ============================================
# 配置
# ============================================
$IMAGE_NAME   = "mini-bill"
$IMAGE_TAG    = "latest"
$CONTAINER    = "mini-bill"
$LOG_DIR      = "D:\Program\mini-bill\logs"
$UPLOAD_DIR   = "D:\Program\mini-bill\upload"
$COMPOSE_FILE = Join-Path $PSScriptRoot "docker-compose.yml"

# ============================================
# 颜色输出函数
# ============================================
function Write-Step  { Write-Host "`n>>> " -NoNewline -ForegroundColor Cyan; Write-Host $args }
function Write-OK    { Write-Host "  [OK] " -NoNewline -ForegroundColor Green; Write-Host $args }
function Write-Warn  { Write-Host "  [WARN] " -NoNewline -ForegroundColor Yellow; Write-Host $args }
function Write-Error { Write-Host "  [ERROR] " -NoNewline -ForegroundColor Red; Write-Host $args }

# ============================================
# 1. 环境检查
# ============================================
Write-Step "1/6 检查运行环境..."

if (-not (Get-Command "docker" -ErrorAction SilentlyContinue)) {
    Write-Error "未检测到 Docker，请先安装 Docker Desktop for Windows"
    exit 1
}
Write-OK "Docker $((docker --version) -replace 'Docker version ','')"

if (-not (Get-Command "docker-compose" -ErrorAction SilentlyContinue) -and
    -not (docker compose version 2>$null)) {
    Write-Error "未检测到 docker compose，请更新 Docker Desktop"
    exit 1
}
Write-OK "Docker Compose 可用"

# 检查 Docker 是否在运行
docker info 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Error "Docker 引擎未运行，请启动 Docker Desktop"
    exit 1
}
Write-OK "Docker 引擎运行中"

# 检查 Maven
$mvnCmd = $null
if (Get-Command "mvn" -ErrorAction SilentlyContinue) {
    $mvnCmd = "mvn"
    Write-OK "Maven 已安装"
} elseif (Get-Command "mvnw" -ErrorAction SilentlyContinue) {
    $mvnCmd = "mvnw"
    Write-OK "使用 Maven Wrapper"
} else {
    Write-Warn "未检测到 Maven，将使用 Docker 多阶段构建（首次构建较慢）"
}

# ============================================
# 2. 创建目录
# ============================================
Write-Step "2/6 创建日志与数据目录..."

foreach ($dir in @($LOG_DIR, $UPLOAD_DIR)) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
        Write-OK "创建目录: $dir"
    } else {
        Write-OK "目录已存在: $dir"
    }
}

# ============================================
# 3. 停止并删除旧容器
# ============================================
Write-Step "3/6 停止旧容器..."

$running = docker ps -q -f "name=$CONTAINER" 2>$null
if ($running) {
    docker stop $CONTAINER 2>$null | Out-Null
    Write-OK "已停止容器: $CONTAINER"
} else {
    Write-OK "无运行中的容器"
}

$exists = docker ps -aq -f "name=$CONTAINER" 2>$null
if ($exists) {
    docker rm $CONTAINER 2>$null | Out-Null
    Write-OK "已删除旧容器: $CONTAINER"
}

# ============================================
# 4. 构建镜像
# ============================================
Write-Step "4/6 构建 Docker 镜像..."

Push-Location $PSScriptRoot

docker compose -f $COMPOSE_FILE build --no-cache
if ($LASTEXITCODE -ne 0) {
    Write-Error "镜像构建失败！退出码: $LASTEXITCODE"
    Pop-Location
    exit 1
}
Write-OK "镜像构建成功: ${IMAGE_NAME}:${IMAGE_TAG}"

# ============================================
# 5. 启动容器
# ============================================
Write-Step "5/6 启动容器..."

docker compose -f $COMPOSE_FILE up -d
if ($LASTEXITCODE -ne 0) {
    Write-Error "容器启动失败！退出码: $LASTEXITCODE"
    Pop-Location
    exit 1
}
Write-OK "容器启动成功"

# ============================================
# 6. 验证部署
# ============================================
Write-Step "6/6 验证部署..."

# 等待服务启动
Write-Host "  等待服务就绪..." -ForegroundColor Gray
Start-Sleep -Seconds 10

$containerRunning = docker ps -q -f "name=$CONTAINER" 2>$null
if (-not $containerRunning) {
    Write-Error "容器未运行，检查日志: docker logs $CONTAINER"
    Pop-Location
    exit 1
}

# 检查容器内进程
$logLines = docker logs --tail 30 $CONTAINER 2>&1
$allStarted = $logLines | Select-String "所有服务启动完成"
if ($allStarted) {
    Write-OK "所有服务已就绪"
} else {
    Write-Warn "服务可能还在启动中，查看日志: docker logs -f $CONTAINER"
}

Pop-Location

# ============================================
# 输出访问信息
# ============================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  财小账 Docker 部署完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  访问地址:" -ForegroundColor Yellow
Write-Host "    前端页面:    http://localhost:1080"
Write-Host "    Gateway API: http://localhost:9080"
Write-Host "    System API:  http://localhost:9081"
Write-Host "    Bus API:     http://localhost:9082"
Write-Host ""
Write-Host "  管理命令:" -ForegroundColor Yellow
Write-Host "    查看日志:    docker logs -f $CONTAINER"
Write-Host "    宿主机日志:  $LOG_DIR"
Write-Host "    停止服务:    docker compose -f `"$COMPOSE_FILE`" down"
Write-Host "    重新部署:    .\deploy.ps1"
Write-Host ""
Write-Host "  自启动状态: " -NoNewline -ForegroundColor Yellow
$restartPolicy = docker inspect -f '{{.HostConfig.RestartPolicy.Name}}' $CONTAINER 2>$null
Write-Host $restartPolicy -ForegroundColor White
Write-Host ""
Write-Host "  端口映射 (对外 = 内部 + 1000):" -ForegroundColor Yellow
Write-Host "    1080 → 80   (Nginx 前端+API)"
Write-Host "    9080 → 8080 (Gateway)"
Write-Host "    9081 → 8081 (System)"
Write-Host "    9082 → 8082 (Bus)"
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
