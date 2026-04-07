<script setup lang="ts">
import { reactive, ref } from 'vue'
import { createProduct } from '@/api/product'
import type { Product } from '@/api/types'

const emit = defineEmits<{
  (e: 'created', product: Product): void
}>()

const loading = ref(false)
const form = reactive({
  name: '',
  price: 9.99,
  stock: 100,
  description: ''
})

async function onSubmit() {
  loading.value = true
  try {
    const p = await createProduct({
      name: form.name,
      price: Number(form.price),
      stock: Number(form.stock),
      description: form.description || undefined
    })
    emit('created', p)
    form.name = ''
    form.description = ''
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-form label-width="90px" @submit.prevent>
    <el-form-item label="名称">
      <el-input v-model="form.name" placeholder="例如：测试商品（中文可用）" />
    </el-form-item>
    <el-form-item label="价格">
      <el-input-number v-model="form.price" :min="0" :step="0.1" />
    </el-form-item>
    <el-form-item label="stock字段">
      <el-input-number v-model="form.stock" :min="0" />
      <div class="hint">该字段是 product 表字段；秒杀/扣减实际以 stock 表为准</div>
    </el-form-item>
    <el-form-item label="描述">
      <el-input v-model="form.description" type="textarea" :rows="3" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" :loading="loading" @click="onSubmit">创建商品</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
.hint {
  margin-left: 10px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>

