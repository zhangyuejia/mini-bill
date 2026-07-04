<#
.SYNOPSIS
    财小账 (Mini-Bill) 增量部署脚本（快速模式）
.DESCRIPTION
    在已有 Docker 镜像的基础上，仅本地构建 JAR 和前端产物，
    通过 docker cp + commit 原地替换文件，跳过镜像的 Maven/NPM 阶段。
    首次部署请先运行 deploy.ps1 生成基础镜像。

.PARAMETER SkipBackend
    跳过 Maven 后端构建（仅更新前端时使用）

.PARAMETER SkipFrontend
    跳过 NPM 前端构建（仅更新后端时使用）

.EXAMPLE
    .\deploy-fast.ps1
    全量增量构建（后端 + 前端）

.EXAMPLE
    .\deploy-fast.ps1 -SkipFrontend
    只更新后端 JAR，不重新构建前端
#>

param(
    [switch] $SkipBackend,
    [switch] $SkipFrontend
)

$ErrorActionPreference = "Continue"
$Host.UI.RawUI.WindowTitle = "财小账 快速部署"

# 全局计时
$script:ScriptStart = Get-Date

# ============================================
# 配置
# ============================================
$IMAGE_NAME    = "mini-bill"
$IMAGE_TAG     = "latest"
$CONTAINER     = "mini-bill"
$LOG_DIR       = "D:\Program\mini-bill\logs"
$UPLOAD_DIR    = "D:\Program\mini-bill\upload"
$COMPOSE_FILE  = Join-Path $PSScriptRoot "docker-compose.yml"

# ============================================
# 颜色输出函数
# ============================================
function Write-Step  { Write-Host "`n>>> " -NoNewline -ForegroundColor Cyan; Write-Host $args }
function Write-OK    { Write-Host "  [OK] " -NoNewline -ForegroundColor Green; Write-Host $args }
function Write-Warn  { Write-Host "  [WARN] " -NoNewline -ForegroundColor Yellow; Write-Host $args }
function Write-Error { Write-Host "  [ERROR] " -NoNewline -ForegroundColor Red; Write-Host $args }
function Write-Time  { Write-Host "  [$((Get-Date).ToString('HH:mm:ss'))] " -NoNewline -ForegroundColor Gray; Write-Host $args }

# ============================================
# 1. 环境检查
# ============================================
Write-Step "1/7 检查运行环境..."

if (-not (Get-Command "docker" -ErrorAction SilentlyContinue)) {
    Write-Error "未检测到 Docker，请先安装 Docker Desktop for Windows"
    exit 1
}
Write-OK "Docker $((docker --version) -replace 'Docker version ','')"

docker info 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Error "Docker 引擎未运行，请启动 Docker Desktop"
    exit 1
}
Write-OK "Docker 引擎运行中"

# 检查基础镜像是否存在
$baseImage = docker images -q "${IMAGE_NAME}:${IMAGE_TAG}" 2>$null
if (-not $baseImage) {
    Write-Error "未找到基础镜像 ${IMAGE_NAME}:${IMAGE_TAG}"
    Write-Host "  请先运行 .\deploy.ps1 进行首次完整构建" -ForegroundColor Yellow
    exit 1
}
Write-OK "基础镜像存在: ${IMAGE_NAME}:${IMAGE_TAG}"

# ============================================
# 2. 后端构建 (Maven)
# ============================================
if (-not $SkipBackend) {
    Write-Step "2/7 构建后端 JAR..."

    $mvnCmd = $null
    if (Get-Command "mvn" -ErrorAction SilentlyContinue) {
        $mvnCmd = "mvn"
    } elseif (Get-Command "mvnw" -ErrorAction SilentlyContinue) {
        $mvnCmd = "mvnw"
    } else {
        Write-Error "未找到 Maven，请安装 Maven 或使用 ./mvnw"
        exit 1
    }

    $mvnStart = Get-Date
    Push-Location "$PSScriptRoot/backend"

    Write-Time "开始 Maven 编译 (跳过测试)..."
    & $mvnCmd clean package -DskipTests -B -q -pl mini-bill-gateway,mini-bill-system,mini-bill-bus -am
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Maven 构建失败！退出码: $LASTEXITCODE"
        Pop-Location
        exit 1
    }

    $mvnElapsed = [math]::Round(((Get-Date) - $mvnStart).TotalSeconds, 1)
    Write-OK "Maven 构建完成 (耗时 ${mvnElapsed}s)"

    # 定位各模块 JAR 文件
    function Find-Jar($modulePath) {
        $jars = Get-ChildItem "$modulePath/target/*.jar" -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -notmatch 'sources\.jar$' -and $_.Name -notmatch 'tests\.jar$' -and $_.Name -notmatch 'javadoc\.jar$' } |
            Sort-Object { $_.Name.Length }   # 优先选择名字最短的（不含 classifier）
        return $jars | Select-Object -First 1
    }

    $GATEWAY_JAR = Find-Jar "mini-bill-gateway"
    $SYSTEM_JAR  = Find-Jar "mini-bill-system"
    $BUS_JAR     = Find-Jar "mini-bill-bus"

    if (-not $GATEWAY_JAR -or -not $SYSTEM_JAR -or -not $BUS_JAR) {
        Write-Error "未能定位部分 JAR 文件，请检查 Maven 输出"
        Write-Host "  Gateway: $($GATEWAY_JAR.FullName)"
        Write-Host "  System:  $($SYSTEM_JAR.FullName)"
        Write-Host "  Bus:     $($BUS_JAR.FullName)"
        Pop-Location
        exit 1
    }

    Write-OK "定位 JAR: Gateway = $($GATEWAY_JAR.Name)"
    Write-OK "定位 JAR: System  = $($SYSTEM_JAR.Name)"
    Write-OK "定位 JAR: Bus     = $($BUS_JAR.Name)"

    Pop-Location
} else {
    Write-Step "2/7 跳过 (SkipBackend)"
}

# ============================================
# 3. 前端构建 (NPM)
# ============================================
if (-not $SkipFrontend) {
    Write-Step "3/7 构建前端..."

    $npmStart = Get-Date
    Push-Location "$PSScriptRoot/frontend"

    # 检查 node_modules，首次或依赖变更时需要 install
    if (-not (Test-Path "node_modules")) {
        Write-Warn "node_modules 不存在，先执行 npm install..."
        npm install
        if ($LASTEXITCODE -ne 0) {
            Write-Error "npm install 失败！"
            Pop-Location
            exit 1
        }
    }

    Write-Time "开始 npm run build..."
    npm run build
    if ($LASTEXITCODE -ne 0) {
        Write-Error "前端构建失败！退出码: $LASTEXITCODE"
        Pop-Location
        exit 1
    }

    $npmElapsed = [math]::Round(((Get-Date) - $npmStart).TotalSeconds, 1)
    Write-OK "前端构建完成 (耗时 ${npmElapsed}s)"

    Pop-Location
} else {
    Write-Step "3/7 跳过 (SkipFrontend)"
}

# ============================================
# 4. 创建目录
# ============================================
Write-Step "4/7 确保数据目录..."

foreach ($dir in @($LOG_DIR, $UPLOAD_DIR)) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
        Write-OK "创建目录: $dir"
    } else {
        Write-OK "目录已存在: $dir"
    }
}

# ============================================
# 5. 增量镜像更新（核心流程）
# ============================================
Write-Step "5/7 增量更新镜像..."

Push-Location $PSScriptRoot

# 生成临时容器名
$tempContainer = "mini-bill-update-$(Get-Date -Format 'HHmmss')"

Write-Time "基于现有镜像创建临时容器: $tempContainer"
docker create --name $tempContainer "${IMAGE_NAME}:${IMAGE_TAG}" 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Error "创建临时容器失败！"
    Pop-Location
    exit 1
}

# 复制后端 JAR
if (-not $SkipBackend) {
    Write-Time "替换后端 JAR..."
    docker cp $GATEWAY_JAR.FullName "${tempContainer}:/app/backend/mini-bill-gateway.jar"
    if ($LASTEXITCODE -ne 0) { Write-Error "Gateway JAR 复制失败！" }
    docker cp $SYSTEM_JAR.FullName "${tempContainer}:/app/backend/mini-bill-system.jar"
    if ($LASTEXITCODE -ne 0) { Write-Error "System  JAR 复制失败！" }
    docker cp $BUS_JAR.FullName "${tempContainer}:/app/backend/mini-bill-bus.jar"
    if ($LASTEXITCODE -ne 0) { Write-Error "Bus     JAR 复制失败！" }
}

# 复制前端产物
if (-not $SkipFrontend) {
    Write-Time "替换前端产物..."
    # docker cp 直接覆盖目标目录，Vite build 已清理 dist，无需额外处理
    docker cp "frontend/dist/." "${tempContainer}:/app/frontend/dist"
    if ($LASTEXITCODE -ne 0) { Write-Error "前端产物复制失败！" }
}

# Commits the temporary container as the new image
Write-Time "提交为新的镜像: ${IMAGE_NAME}:${IMAGE_TAG}"
docker commit $tempContainer "${IMAGE_NAME}:${IMAGE_TAG}"
if ($LASTEXITCODE -ne 0) {
    Write-Error "镜像提交失败！"
    docker rm $tempContainer 2>$null | Out-Null
    Pop-Location
    exit 1
}

# 清理临时容器
docker rm $tempContainer 2>$null | Out-Null
Write-OK "增量镜像构建完成"

Pop-Location

# ============================================
# 6. 替换容器
# ============================================
Write-Step "6/7 替换运行容器..."

# 停止旧容器
$running = docker ps -q -f "name=$CONTAINER" 2>$null
if ($running) {
    docker stop $CONTAINER 2>$null | Out-Null
    Write-OK "已停止容器: $CONTAINER"
} else {
    Write-OK "无运行中的容器"
}

# 删除旧容器
$exists = docker ps -aq -f "name=$CONTAINER" 2>$null
if ($exists) {
    docker rm $CONTAINER 2>$null | Out-Null
    Write-OK "已删除旧容器: $CONTAINER"
}

# 启动新容器
docker compose -f $COMPOSE_FILE up -d
if ($LASTEXITCODE -ne 0) {
    Write-Error "容器启动失败！退出码: $LASTEXITCODE"
    exit 1
}
Write-OK "容器启动成功"

# ============================================
# 7. 验证部署
# ============================================
Write-Step "7/7 验证部署..."

Write-Host "  等待服务就绪..." -ForegroundColor Gray
Start-Sleep -Seconds 10

$containerRunning = docker ps -q -f "name=$CONTAINER" 2>$null
if (-not $containerRunning) {
    Write-Error "容器未运行，检查日志: docker logs $CONTAINER"
    exit 1
}

$logLines = docker logs --tail 30 $CONTAINER 2>&1
$allStarted = $logLines | Select-String "所有服务启动完成"
if ($allStarted) {
    Write-OK "所有服务已就绪"
} else {
    Write-Warn "服务可能还在启动中，查看日志: docker logs -f $CONTAINER"
}

# ============================================
# 输出访问信息
# ============================================
$totalElapsed = [math]::Round(((Get-Date) - $script:ScriptStart).TotalSeconds, 1)
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  财小账 快速部署完成！" -ForegroundColor Green
Write-Host "  (总耗时约 ${totalElapsed}s)" -ForegroundColor Gray
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
Write-Host "    快速部署:    .\deploy-fast.ps1"
Write-Host "    完整构建:    .\deploy.ps1"
Write-Host ""
