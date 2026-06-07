# 财小账 (mini-bill)

> 面向家庭的财务管理系统，支持多家庭、多成员协同管理房租水电、物件费用、家庭储蓄等财务数据。

## 项目架构

### 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Element Plus + Pinia + ECharts |
| 后端 | Spring Boot 3 + Spring Cloud Gateway + MyBatis Plus |
| 注册中心 | Nacos (192.168.0.121:8848, namespace: mini-bill) |
| 数据库 | PostgreSQL (192.168.0.121:5432, 分库设计) |
| 缓存 | Redis (192.168.0.121:6379) |
| 构建工具 | Maven (后端) / Vite (前端) |

### 微服务架构

项目采用 Spring Cloud 微服务架构，分为三个核心服务：

```
┌───────────────┐     ┌──────────────────┐
│  前端 (Vue 3) │────▶│  网关 (Gateway)  │
└───────────────┘     └──────┬───────────┘
                             │
              ┌──────────────┴──────────────┐
              ▼                             ▼
   ┌──────────────────┐       ┌──────────────────────┐
   │  系统服务 (System) │       │   业务服务 (Bus)      │
   │  /system/**       │       │   /bus/**            │
   │                   │       │                      │
   │  - 认证鉴权        │       │  - 家庭管理           │
   │  - 用户管理        │       │  - 住址管理           │
   │  - 角色/菜单管理    │       │  - 房租水电账单       │
   │  - 字典管理        │       │  - 物件管理/费用      │
   └──────────────────┘       │  - 家庭储蓄           │
                               │  - 文件上传           │
                               └──────────────────────┘
```

- **网关服务** (`mini-bill-gateway`, 端口 8080)：请求路由入口，根据路径前缀分发：`/system/**` → 系统服务, `/bus/**` → 业务服务
- **系统服务** (`mini-bill-system`, 端口 8081)：认证鉴权、用户管理、角色菜单管理、字典管理等系统级基础能力
- **业务服务** (`mini-bill-bus`, 端口 8082)：家庭业务领域——家庭管理、住址管理、水电账单、物件/费用、家庭储蓄、文件上传
- **公共模块** (`mini-bill-common`)：统一响应体、异常处理、常量定义等共享代码
- **Feign 接口** (`mini-bill-api`)：跨服务调用的 Feign 客户端定义

### 数据库设计

采用**分库设计**：

| 数据库 | 用途 | 主要表 |
|--------|------|--------|
| `mini-bill-system` | 系统数据 | sys_user, sys_role, sys_menu, sys_dict_type, sys_dict_data |
| `mini-bill-bus` | 业务数据 | bus_family, bus_address, bus_bill, bus_bill_attachment, bus_item, bus_item_cost, bus_family_saving, bus_saving_item, bus_saving_record |

### 模块功能详解

#### 认证与权限

- 管理员默认账号：admin / admin123
- 普通用户通过邮箱注册，填写用户名，系统发送邮箱验证码完成注册
- 基于 JWT Token 的认证机制
- 菜单/按钮级别的权限控制（RBAC）

#### 家庭管理

家庭是系统的核心维度，所有业务数据归属于家庭：

- 用户可创建家庭、邀请其他用户（通过邮箱）加入家庭
- 右上角切换家庭，展示的数据默认归属当前家庭
- 创建者默认为一家之主，可踢出成员

#### 住址管理

一个家庭可以有多个住址（如租房地、老家），住址归属于当前家庭：

- 名称、省/市/区（级联选择）、门牌号、地址图片
- 默认房租、默认电费单价、默认水费单价、默认管理费
- 新增水电账单时自动填入默认值

#### 房租水电管理

房租水电账单填写：

- 选择住址 + 账期（月份）
- 房租、水费（上月表底/本月表底/单价/金额，有表底+单价则自动计算）、电费、管理费
- 损耗 = (水费+电费) × 5%（自动计算）
- 抹零金额、备注
- 合计 = 房租 + 水费 + 电费 + 管理费 + 其他费用 + 抹零
- 支持多张图片附件上传

列表页特性：
- 分页查询、按住址/账期范围筛选
- 本页小计（表尾合计行）
- 历史数据一键迁移（从备注提取管理费、损耗，重算抹零）

#### 物件管理

- 物件归属于住址，类型通过字典维护（家用电器、电子设备、交通工具等）
- 填写购买金额、购买日期，支持图片附件

#### 物件费用管理

- 选择住址 → 选择物件 → 填写日期/费用/里程（交通工具类型时显示里程）
- 支持图片附件

#### 储蓄管理

- 储蓄项管理：为家庭成员定制储蓄项（支付宝大号、支付宝小号、微信、银行卡、借出等）
- 家庭储蓄：选择日期 → 填写成员各储蓄项金额 → 一键创建并保存
- 按日期倒序展示，成员动态列显示各人小计

#### 首页仪表盘

- 家庭储蓄趋势折线图（按日期展示总金额变化）
- 房租水电同比折线图（按月对比各年份的总金额）

## 环境信息

| 资源 | 地址/账号 |
|------|-----------|
| Nacos | 192.168.0.121:8848, namespace: mini-bill, 账号: nacos / nacos |
| PostgreSQL | 192.168.0.121:5432, 账号: wmsuser / 123456 |
| Redis | 192.168.0.121:6379, 密码: 123456 |
| SMTP | 1099822863@qq.com (授权码: vogprcuszxmcfhad) |

## 本地开发

### 后端启动

```bash
cd backend

# 编译
mvn clean install -DskipTests

# 启动网关（端口 8080）
mvn spring-boot:run -pl mini-bill-gateway

# 启动系统服务（端口 8081）
mvn spring-boot:run -pl mini-bill-system

# 启动业务服务（端口 8082）
mvn spring-boot:run -pl mini-bill-bus
```

### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 开发模式（端口 3001）
npm run dev

# 构建
npm run build
```

### 数据库初始化

`schema.sql` 位于各模块的 `src/main/resources/` 目录下：

- `mini-bill-bus/src/main/resources/schema.sql`
- `mini-bill-system/src/main/resources/schema.sql`

## 上线部署

### 前置条件

1. PostgreSQL 数据库服务正常运行
2. Nacos 注册中心正常运行
3. Redis 服务正常运行

### 部署步骤

```bash
# 1. 执行数据库初始化脚本
psql -h 192.168.0.121 -p 5432 -U wmsuser -d mini-bill-system -f backend/mini-bill-system/src/main/resources/schema.sql
psql -h 192.168.0.121 -p 5432 -U wmsuser -d mini-bill-bus -f backend/mini-bill-bus/src/main/resources/schema.sql

# 2. 构建后端
cd backend && mvn clean package -DskipTests

# 3. 启动各服务
cd mini-bill-gateway && nohup java -jar target/mini-bill-gateway.jar > /dev/null 2>&1 &
cd mini-bill-system && nohup java -jar target/mini-bill-system.jar > /dev/null 2>&1 &
cd mini-bill-bus && nohup java -jar target/mini-bill-bus.jar > /dev/null 2>&1 &

# 4. 构建前端
cd frontend && npm install && npm run build

# 5. Nginx 配置代理到前端 dist 目录
```

### 日志说明

各服务日志存储在项目根目录下 `/logs` 目录：

- `/logs/mini-bill-system.log` — 当天日志
- `/logs/mini-bill-system/` — 非当天日志（按日期压缩归档）
- 其他服务同理

### 文件上传配置

上传文件路径在配置文件中通过以下属性配置：

```yaml
minibill:
  upload:
    path: /data/mini-bill/upload
```
