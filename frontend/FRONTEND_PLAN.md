## 目标

用 `pnpm + Vite + Vue 3 + TypeScript` 实现一个用于课程验收的前端：

- 统一通过 Nginx 访问：页面走 `/` 静态资源；接口走 `/api/*`
- 覆盖核心演示链路：商品创建 -> 库存初始化 -> 商品详情/搜索 -> 秒杀下单（异步）-> 按订单号查询
- UI 简洁现代，便于录屏/截图验收

---

## 页面与路由

### 1) 首页 `/`
- 系统简介与验收指引
- 一键跳转：商品、库存、秒杀、订单查询

### 2) 商品列表 `/products`
- 调用：`GET /api/products`
- 展示：id、name、price、stock、createdAt
- 操作：
  - 跳转详情：`/products/:id`
  - 新建商品表单（可弹窗或同页折叠）

### 3) 商品详情 `/products/:id`
- 调用：`GET /api/products/{id}`
- 展示：name/price/stock/description/createdAt
- 操作：
  - 秒杀下单入口（选择 userId、amount）

### 4) 搜索 `/search`
- 调用：`GET /api/products/search?keyword=xxx`
- 展示：搜索结果列表（同商品列表卡片）

### 5) 库存管理 `/stocks`
- 创建/初始化库存：
  - `POST /api/stocks`（写入 DB stock 表）
  - 可提供“按 productId 查询库存”：`GET /api/stocks/product/{productId}`
- 说明：秒杀链路会先走 Redis 预扣库存，因此应先保证 DB 中 stock 表存在对应 productId 的库存数量

### 6) 订单查询 `/orders`
- 按订单号查询：
  - `GET /api/orders/by-no/{orderNo}`
- 按 userId 查询：
  - `GET /api/orders/user/{userId}`

---

## 需要对接的后端接口（已实现）

- 商品：
  - `GET /api/products`
  - `GET /api/products/{id}`
  - `POST /api/products/`（注意末尾 `/`，避免 301 导致 POST 变 GET）
  - `GET /api/products/search?keyword=...`
- 库存：
  - `POST /api/stocks`
  - `GET /api/stocks/product/{productId}`
- 秒杀：
  - `POST /api/orders/seckill`
  - `GET /api/orders/by-no/{orderNo}`

---

## 技术与工程结构

### 技术栈
- Vue 3 + TS
- Vue Router
- Pinia
- Axios（baseURL 为空，直接请求相对路径 `/api/...`）
- UI：推荐 Element Plus（组件齐全，开发快，验收观感好）

### 目录结构（frontend/）
- `src/main.ts`：入口
- `src/router/`：路由
- `src/stores/`：Pinia
- `src/api/`：后端 API 封装与类型
- `src/pages/`：页面级组件
- `src/components/`：复用组件（商品卡片、表单等）
- `src/layouts/`：全局布局（导航 + 内容区）

---

## 与 Nginx 的集成策略

### 开发期
- `pnpm dev`：前端本地开发服务器（建议通过 Vite proxy 转发 `/api` 到 `http://localhost`）

### 生产/验收期
- `pnpm build` 输出 `frontend/dist`
- 在 `docker-compose.yml` 的 `nginx` 服务中把 `./frontend/dist` 挂载到 `/usr/share/nginx/html`
  - 这样访问 `http://localhost/` 就是 Vue SPA

