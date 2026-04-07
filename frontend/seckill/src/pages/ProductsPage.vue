<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listProducts } from '@/api/product'
import type { Product } from '@/api/types'
import ProductForm from '@/components/ProductForm.vue'
import ProductTable from '@/components/ProductTable.vue'

const loading = ref(false)
const rows = ref<Product[]>([])

async function refresh() {
  loading.value = true
  try {
    rows.value = await listProducts()
  } finally {
    loading.value = false
  }
}

onMounted(refresh)
</script>

<template>
  <div>
    <el-page-header content="商品管理与查询" title="商品" />

    <el-card class="mt">
      <template #header>创建商品</template>
      <ProductForm @created="refresh" />
    </el-card>

    <el-card class="mt">
      <template #header>
        <div class="hdr">
          <span>商品列表</span>
          <el-button size="small" @click="refresh">刷新</el-button>
        </div>
      </template>
      <ProductTable :rows="rows" :loading="loading" />
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
</style>

