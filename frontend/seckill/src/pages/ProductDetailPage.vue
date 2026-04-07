<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getProduct } from '@/api/product'
import { seckill } from '@/api/order'
import type { Product } from '@/api/types'

const route = useRoute()
const id = computed(() => Number(route.params.id))

const loading = ref(false)
const product = ref<Product | null>(null)

const userId = ref(1)
const amount = ref(1)
const lastOrderNo = ref<number | null>(null)

async function refresh() {
  loading.value = true
  try {
    product.value = await getProduct(id.value)
  } finally {
    loading.value = false
  }
}

async function doSeckill() {
  const resp = await seckill({ userId: userId.value, productId: id.value, amount: amount.value })
  if ('orderNo' in resp) {
    lastOrderNo.value = resp.orderNo
  }
}

onMounted(refresh)
</script>

<template>
  <div>
    <el-page-header content="商品详情" title="详情" @back="$router.back()" />

    <el-card class="mt" v-loading="loading">
      <template #header>
        <div class="hdr">
          <span>商品信息（ID={{ id }}）</span>
          <el-button size="small" @click="refresh">刷新</el-button>
        </div>
      </template>

      <template v-if="product">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="名称">{{ product.name }}</el-descriptions-item>
          <el-descriptions-item label="价格">{{ product.price }}</el-descriptions-item>
          <el-descriptions-item label="stock字段">{{ product.stock }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ product.createdAt || '-' }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ product.description || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template v-else>
        <el-alert title="该商品不存在（可以先去“商品”页面创建）" type="warning" show-icon />
      </template>
    </el-card>

    <el-card class="mt">
      <template #header>秒杀下单</template>
      <el-form inline>
        <el-form-item label="userId">
          <el-input-number v-model="userId" :min="1" />
        </el-form-item>
        <el-form-item label="amount">
          <el-input-number v-model="amount" :min="1" />
        </el-form-item>
        <el-form-item>
          <el-button type="danger" @click="doSeckill">秒杀</el-button>
        </el-form-item>
        <el-form-item v-if="lastOrderNo">
          <el-tag type="success">orderNo: {{ lastOrderNo }}</el-tag>
          <el-button link type="primary" :to="`/orders?orderNo=${lastOrderNo}`">去查询</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        class="hint"
        title="提示：秒杀依赖 stock 表库存，请先在“库存”页为该商品初始化库存。"
        type="info"
        show-icon
      />
    </el-card>
  </div>
</template>

<style scoped>
.mt {
  margin-top: 16px;
}
.hdr {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.hint {
  margin-top: 10px;
}
</style>

