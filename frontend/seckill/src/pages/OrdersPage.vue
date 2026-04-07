<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderByNo, listOrdersByUser } from '@/api/order'
import type { Order } from '@/api/types'

const route = useRoute()
const orderNo = ref<number | null>(null)
const userId = ref<number>(1)

const loading = ref(false)
const order = ref<Order | null>(null)
const userOrders = ref<Order[]>([])

onMounted(() => {
  const q = route.query.orderNo
  if (typeof q === 'string' && q.trim()) {
    const n = Number(q)
    if (!Number.isNaN(n)) orderNo.value = n
  }
})

async function queryByNo() {
  if (!orderNo.value) return
  loading.value = true
  try {
    order.value = await getOrderByNo(orderNo.value)
  } finally {
    loading.value = false
  }
}

async function queryByUser() {
  loading.value = true
  try {
    userOrders.value = await listOrdersByUser(userId.value)
  } finally {
    loading.value = false
  }
}

const statusTag = computed(() => (order.value?.status === 'paid' ? 'success' : order.value?.status ? 'warning' : 'info'))
</script>

<template>
  <div>
    <el-page-header content="按订单号或用户查询订单" title="订单" />

    <el-card class="mt">
      <template #header>按订单号查询</template>
      <el-form inline @submit.prevent>
        <el-form-item label="orderNo">
          <el-input-number v-model="orderNo" :min="1" :controls="false" style="width: 280px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="queryByNo">查询</el-button>
        </el-form-item>
        <el-form-item v-if="order">
          <el-tag :type="statusTag">status: {{ order.status }}</el-tag>
        </el-form-item>
      </el-form>

      <div v-if="order" class="mt2">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="orderNo">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="id">{{ order.id ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="userId">{{ order.userId }}</el-descriptions-item>
          <el-descriptions-item label="productId">{{ order.productId }}</el-descriptions-item>
          <el-descriptions-item label="stockId">{{ order.stockId }}</el-descriptions-item>
          <el-descriptions-item label="amount">{{ order.amount }}</el-descriptions-item>
          <el-descriptions-item label="createdAt" :span="2">{{ order.createdAt ?? '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-empty v-else description="暂无结果（可从商品详情页秒杀后复制 orderNo）" />
    </el-card>

    <el-card class="mt">
      <template #header>按用户查询</template>
      <el-form inline @submit.prevent>
        <el-form-item label="userId">
          <el-input-number v-model="userId" :min="1" />
        </el-form-item>
        <el-form-item>
          <el-button :loading="loading" @click="queryByUser">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table class="mt2" :data="userOrders" v-loading="loading">
        <el-table-column prop="orderNo" label="orderNo" min-width="160" />
        <el-table-column prop="status" label="status" width="120" />
        <el-table-column prop="productId" label="productId" width="120" />
        <el-table-column prop="amount" label="amount" width="100" />
        <el-table-column prop="createdAt" label="createdAt" min-width="160" />
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.mt {
  margin-top: 16px;
}
.mt2 {
  margin-top: 12px;
}
</style>

