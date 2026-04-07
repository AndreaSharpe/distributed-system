<script setup lang="ts"></script>

<template>
  <div>
    <el-page-header content="验收入口" title="首页" />

    <el-alert
      class="mt"
      title="提示：所有请求都走 Nginx（http://localhost）。创建商品请用 /api/products/（末尾 /）。"
      type="info"
      show-icon
    />

    <el-card class="mt">
      <template #header>建议的演示顺序</template>
      <ol class="steps">
        <li>商品页创建商品（POST /api/products/）</li>
        <li>库存页为该商品初始化库存（POST /api/stocks）</li>
        <li>商品详情页查看详情（GET /api/products/{id}，Redis 缓存）</li>
        <li>搜索页用关键词搜索（GET /api/products/search）</li>
        <li>商品详情页发起秒杀（POST /api/orders/seckill）</li>
        <li>订单页按订单号查询状态（GET /api/orders/by-no/{orderNo}）</li>
      </ol>
    </el-card>
  </div>
</template>

<style scoped>
.mt {
  margin-top: 16px;
}
.steps {
  margin: 0;
  padding-left: 18px;
  line-height: 1.9;
}
</style>

