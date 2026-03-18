# Distributed 秒杀系统项目结构

本项目采用 Maven 多模块结构，包含以下服务：
- user-service（用户服务）
- product-service（商品服务）
- stock-service（库存服务）
- order-service（订单服务）

## 目录结构

Distributed/
├── user-service/
├── product-service/
├── stock-service/
├── order-service/
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

## 快速启动

1. 配置好 application.yml
2. 各服务分别运行 Spring Boot 主类
3. 可用 Postman/curl 测试接口

---
如需详细开发步骤或代码模板，请参考《系统设计与环境准备文档.md》。
