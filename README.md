# 财小账 (mini-bill)

> 面向家庭的财务管理系统，支持多家庭、多成员协同管理房租水电、物件费用、家庭储蓄、教育费用等财务数据。

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
   │  - 字典管理        │       │  - 物件管理/流水      │
   └──────────────────┘       │  - 家庭储蓄           │
                               │  - 维护费用           │
                               │  - 教育费用           │
                               │  - 文件上传           │
                               │  - 通用附件           │
                               │  - 首页仪表盘         │
                               └──────────────────────┘
```

- **网关服务** (`mini-bill-gateway`, 端口 8080)：请求路由入口，根据路径前缀分发：`/system/**` → 系统服务, `/bus/**` → 业务服务
- **系统服务** (`mini-bill-system`, 端口 8081)：认证鉴权、用户管理、角色菜单管理、字典管理等系统级基础能力
- **业务服务** (`mini-bill-bus`, 端口 8082)：家庭业务领域——家庭管理、住址管理、水电账单、物件/流水、家庭储蓄、维护费用、教育费用、文件上传、通用附件
- **公共模块** (`mini-bill-common`)：统一响应体、异常处理、常量定义、字典切面等共享代码
- **Feign 接口** (`mini-bill-api`)：跨服务调用的 Feign 客户端定义

### 数据库设计

采用**分库设计**：

| 数据库 | 用途 | 主要表 |
|--------|------|--------|
| `mini-bill-system` | 系统数据 | sys_user, sys_role, sys_menu, sys_dict_type, sys_dict_data |
| `mini-bill-bus` | 业务数据 | bus_family, bus_family_member, bus_address, bus_bill, bus_item, bus_item_cost, bus_family_saving, bus_saving_item, bus_saving_record, bus_maintenance, bus_education, bus_education_item, bus_attachment |

### 通用附件系统

附件采用统一设计，所有业务共享一张 `bus_attachment` 表：

| biz_type | 业务 |
|----------|------|
| `bill` | 房租水电账单 |
| `item` | 物件 |
| `item_cost` | 物品流水 |
| `maintenance` | 维护费用 |
| `education` | 教育费用 |
| `education_item` | 教育费用明细 |

附件数据在分页接口中通过 **一次性批量查询** 返回给前端，消除 N+1 查询问题。

## 模块功能详解

### 认证与权限

- 管理员默认账号：admin / admin123
- 普通用户通过邮箱注册，填写用户名，系统发送邮箱验证码完成注册
- 基于 JWT Token 的认证机制
- 菜单/按钮级别的权限控制（RBAC）

### 家庭管理

家庭是系统的核心维度，所有业务数据归属于家庭：

- 用户可创建家庭、邀请其他用户（通过邮箱）加入家庭
- 右上角切换家庭，展示的数据默认归属当前家庭
- 创建者默认为一家之主，可踢出成员
- 刷新页面后保持家庭数据不丢失

### 住址管理

一个家庭可以有多个住址（如租房地、老家），住址归属于当前家庭：

- 名称、省/市/区（级联选择）、门牌号、地址图片
- 默认房租、默认电费单价、默认水费单价、默认管理费
- 新增水电账单时自动填入默认值

### 房租水电管理

房租水电账单填写：

- 选择住址 + 账期（月份）
- 房租、水费（上月表底/本月表底/单价/金额，有表底+单价则自动计算）、电费、管理费
- 损耗 = (水费+电费) × 5%（自动计算）
- 抹零金额、备注
- 合计 = 房租 + 水费 + 电费 + 管理费 + 其他费用 + 抹零
- 支持多张图片附件上传

列表页特性：
- 分页查询（默认10条/页）、按住址/账期范围筛选
- 本页合计（表尾合计行）
- 附件缩略图预览，支持点击查看大图灯箱

### 物件管理

- 物件归属于住址，类型通过字典维护（家用电器、电子设备、交通工具等）
- 填写购买金额、购买日期、停用时间、残值
- 支持图片附件，列表展示住址名称
- 删除前检查是否存在费用记录

### 物品流水

- 选择住址 → 选择物件 → 填写日期/费用/里程（交通工具类型时显示里程）
- 支持图片附件
- 列表展示物件名称和住址名称
- 本页合计

### 维护费用

- 选择住址 → 选择维护类型（字典：设备维护/日常维护）→ 日期/费用/备注
- 支持图片附件
- 列表展示住址名称和维护类型中文
- 本页合计

### 教育费用

- 选择家庭成员 → 学期日期 → 学费/伙食费/住宿费
- 支持图片附件
- **明细费用管理**：在列表页点击明细按钮，可添加/编辑/删除明细项（班级活动、兴趣班、教辅资料等，支持字典配置）
- 每条明细含日期、类型、金额、备注、附件

### 储蓄管理

- 储蓄项管理：为家庭成员定制储蓄项（支付宝大号、支付宝小号、微信、银行卡、借出等）
- 家庭储蓄：选择日期 → 填写成员各储蓄项金额 → 一键创建并保存
- 按日期倒序展示，成员动态列显示各人小计
- 明细数据在分页时批量返回，消除 N+1 查询

### 首页仪表盘

**摘要统计卡片（四张）：**
- 🟢 家庭储蓄：最新一个月总额 + 距上次更新天数
- 🟠 家庭维护费用：房租水电+维护费用历史总计 + 今年合计 + 年均（总计÷年份数）
- 🔵 家庭物件：物件总数 + 日均成本最贵和最省的物件
- 🔴 今年总支出：房租水电 + 维护 + 物品流水的年度合计

**图表：**
- 家庭储蓄趋势折线图（按日期展示总金额变化）
- 房租水电同比折线图（按月对比各年份的总金额）
- 物件日均成本横向柱状图（总投入÷持有天数，含持有天数展开）

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

```bash
# 执行 schema 脚本
psql -h 192.168.0.121 -p 5432 -U wmsuser -d mini-bill-system -f backend/mini-bill-system/src/main/resources/schema.sql
psql -h 192.168.0.121 -p 5432 -U wmsuser -d mini-bill-bus -f backend/mini-bill-bus/src/main/resources/schema.sql

# 执行迁移脚本（如果使用了旧版 schema）
psql -h 192.168.0.121 -p 5432 -U wmsuser -d mini-bill-bus -f backend/mini-bill-bus/src/main/resources/migration_unify_attachment.sql
psql -h 192.168.0.121 -p 5432 -U wmsuser -d mini-bill-bus -f backend/mini-bill-bus/src/main/resources/migration_add_maintenance.sql
psql -h 192.168.0.121 -p 5432 -U wmsuser -d mini-bill-bus -f backend/mini-bill-bus/src/main/resources/migration_add_education.sql
```

迁移脚本位于 `backend/mini-bill-bus/src/main/resources/`：
- `migration_unify_attachment.sql` — 附件表统一
- `migration_drop_cost_family_address.sql` — 移除物品流水冗余字段
- `migration_add_residual_value.sql` — 物件增加残值字段
- `migration_add_maintenance.sql` — 维护费用表
- `migration_add_education.sql` — 教育费用表

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
