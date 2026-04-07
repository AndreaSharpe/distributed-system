<script setup lang="ts">
import type { Product } from '@/api/types'

defineProps<{
  rows: Product[]
  loading?: boolean
}>()
</script>

<template>
  <el-table :data="rows" v-loading="loading" style="width: 100%">
    <el-table-column prop="id" label="ID" width="90" />
    <el-table-column prop="name" label="名称" min-width="180" />
    <el-table-column prop="price" label="价格" width="120" />
    <el-table-column prop="stock" label="stock字段" width="120" />
    <el-table-column prop="createdAt" label="创建时间" min-width="160" />
    <el-table-column label="操作" width="120">
      <template #default="{ row }">
        <el-button link type="primary" :to="`/products/${row.id}`">详情</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

