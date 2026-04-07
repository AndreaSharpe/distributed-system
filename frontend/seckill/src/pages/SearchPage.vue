<script setup lang="ts">
import { ref } from 'vue'
import { searchProducts } from '@/api/product'
import type { Product } from '@/api/types'
import ProductTable from '@/components/ProductTable.vue'

const keyword = ref('')
const loading = ref(false)
const rows = ref<Product[]>([])

async function onSearch() {
  loading.value = true
  try {
    rows.value = await searchProducts(keyword.value.trim())
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <el-page-header content="Elasticsearch 搜索" title="搜索" />
    <el-card class="mt">
      <el-form inline @submit.prevent>
        <el-form-item label="keyword">
          <el-input v-model="keyword" placeholder="例如：手机" style="width: 260px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">搜索</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="mt">
      <template #header>结果</template>
      <ProductTable :rows="rows" :loading="loading" />
    </el-card>
  </div>
</template>

<style scoped>
.mt {
  margin-top: 16px;
}
</style>

