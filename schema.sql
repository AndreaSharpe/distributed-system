-- 数据库初始化脚本 schema.sql

CREATE DATABASE IF NOT EXISTS seckill_db DEFAULT CHARACTER SET utf8mb4;
USE seckill_db;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    email VARCHAR(128),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商品表
CREATE TABLE IF NOT EXISTS product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 库存表
CREATE TABLE IF NOT EXISTS stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(id)
);

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no BIGINT NOT NULL COMMENT '雪花算法等业务唯一单号',
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,
    amount INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (product_id) REFERENCES product(id),
    FOREIGN KEY (stock_id) REFERENCES stock(id)
);

