<script setup lang="ts">
import { ref } from 'vue'
import { createStock, getStockByProductId } from '@/api/stock'
import type { Stock } from '@/api/types'

const productId = ref<number>(1)
const quantity = ref<number>(100)
const loading = ref(false)
const current = ref<Stock | null>(null)

async function query() {
  loading.value = true
  try {
    current.value = await getStockByProductId(productId.value)
  } finally {
    loading.value = false
  }
}

async function initStock() {
  loading.value = true
  try {
    await createStock({ productId: productId.value, quantity: quantity.value })
    await query()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <el-page-header content="为秒杀准备库存（写入 stock 表）" title="库存" />

    <el-card class="mt">
      <el-form inline @submit.prevent>
        <el-form-item label="productId">
          <el-input-number v-model="productId" :min="1" />
        </el-form-item>
        <el-form-item label="quantity">
          <el-input-number v-model="quantity" :min="0" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="initStock">初始化/追加库存</el-button>
          <el-button :loading="loading" @click="query">查询库存</el-button>
        </el-form-item>
      </el-form>

      <el-divider />

      <template v-if="current">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="stockId">{{ current.id }}</el-descriptions-item>
          <el-descriptions-item label="productId">{{ current.productId }}</el-descriptions-item>
          <el-descriptions-item label="quantity">{{ current.quantity }}</el-descriptions-item>
          <el-descriptions-item label="updatedAt">{{ current.updatedAt || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template v-else>
        <el-empty description="暂无库存记录（先初始化）" />
      </template>
    </el-card>
  </div>
</template>

<style scoped>
.mt {
  margin-top: 16px;
}
</style>

