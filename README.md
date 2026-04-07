# Distributed 秒杀系统项目结构

本项目采用 Maven 多模块结构，包含以下服务：
- user-service（用户服务）
- product-service（商品服务）
- stock-service（库存服务）
- order-service（订单服务）
- payment-service（支付服务）

## 目录结构

Distributed/
├── user-service/
├── product-service/
├── stock-service/
├── order-service/
├── payment-service/
├── docs/
├── README.md

## 环境搭建

1. JDK 8+，Maven 3.6+
2. MySQL 5.7+，Redis 5+
3. 各服务 application.yml 配置数据库、端口
4. 执行 docs/schema.sql 初始化数据库

## 各服务说明

- user-service：用户注册、登录、鉴权
- product-service：商品管理、查询
- stock-service：库存管理、扣减
- order-service：订单创建、查询
- payment-service：支付单创建、回调（模拟），并通过可靠消息驱动订单状态更新

## 快速启动

1. 配置好 application.yml
2. 各服务分别运行 Spring Boot 主类
3. 可用 Postman/curl 测试接口

## Docker 一键启动

1. 在项目根目录执行：
   - `docker compose up -d --build --scale product-service=2`
2. 访问：
   - `http://localhost/`（Nginx 静态资源）
   - `http://localhost/api/products/1`（Nginx 转发到 product-service）
   - `http://localhost/api/products/search?keyword=xxx`（Elasticsearch 搜索）

## 作业：分布式事务（一致性）实现说明

本作业要求在“秒杀下单系统”里完成两条一致性：
- 下单 + 库存扣减一致性（订单服务与库存服务为独立微服务、各自数据库）
- 订单支付 + 订单状态更新一致性

本项目采用“工程化可落地”的组合方案：

- **库存侧：TCC（Try/Confirm/Cancel）**
  - Try：`stock-service` 按 `orderNo` 预留库存（Redis 原子预扣 + `stock_reservation` 记录，幂等）
  - Confirm：支付成功后确认扣减数据库库存（幂等）
  - Cancel：支付失败/超时等取消预留并回滚 Redis（幂等）
  - 超时回收：定时扫描 `stock_reservation` 的 TRY 超时记录并自动释放

- **消息侧：可靠消息 Outbox + 消费幂等 Inbox**
  - 生产端（order/payment）：业务写入与 Outbox 写入同库同事务，异步投递 Kafka
  - 投递失败自动重试（指数退避），并可标记 FAILED 以便人工/脚本介入
  - 消费端（order）：Inbox 以 `eventId` 去重，保证至少一次投递下的幂等处理

### 关键接口（用于验收）

- **秒杀下单（写入 Outbox，异步投递 Kafka）**
  - `POST /api/orders/seckill`
  - Body: `{ "userId": 1, "productId": 1, "amount": 1 }`
  - 返回：`orderNo`（accepted）

- **库存 TCC**
  - `POST /api/stocks/tcc/try` Body: `{ "orderNo": 123, "productId": 1, "amount": 1, "ttlSeconds": 120 }`
  - `POST /api/stocks/tcc/confirm` Body: `{ "orderNo": 123 }`
  - `POST /api/stocks/tcc/cancel` Body: `{ "orderNo": 123 }`

- **支付（模拟）**
  - `POST /api/payments/create` Body: `{ "orderNo": 123, "userId": 1, "amount": 99.99, "channel": "mock" }`
  - `POST /api/payments/callback` Body: `{ "paymentNo": 456, "success": true }`
  - 回调会写 Outbox 并发布 `PaymentSucceeded/PaymentFailed` 事件，`order-service` 消费后更新订单状态并触发库存 Confirm/Cancel

### 验收步骤（推荐按顺序）

1. `docker compose up -d --build`
2. 准备商品与库存（先创建 product，再创建 stock；或用你已有的接口初始化）
3. 调用 `/api/orders/seckill` 生成 `orderNo`
4. 等待 1-2 秒（OutboxPublisher 投递 + Kafka 消费）
5. 调用 `/api/orders/by-no/{orderNo}` 查看订单状态应为 `reserved`（成功预留）
6. 调用 `/api/payments/create` 生成 `paymentNo`
7. 调用 `/api/payments/callback`：
   - `success=true`：订单状态变为 `paid`，并 Confirm 扣减数据库库存
   - `success=false`：订单状态变为 `cancelled`，并 Cancel 回滚预留库存


