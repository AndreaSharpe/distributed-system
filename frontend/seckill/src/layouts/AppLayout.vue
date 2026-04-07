<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Menu as IconMenu } from '@element-plus/icons-vue'

const route = useRoute()
const active = computed(() => (route.path.startsWith('/products') ? '/products' : route.path))
</script>

<template>
  <el-container style="min-height: 100vh">
    <el-header height="56px" class="header">
      <div class="brand">
        <el-icon><IconMenu /></el-icon>
        <span>秒杀系统（课程验收前端）</span>
      </div>
      <div class="hint">统一走 Nginx：页面 `/`，接口 `/api/*`</div>
    </el-header>
    <el-container>
      <el-aside width="220px" class="aside">
        <el-menu :default-active="active" router>
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/products">商品</el-menu-item>
          <el-menu-item index="/search">搜索</el-menu-item>
          <el-menu-item index="/stocks">库存</el-menu-item>
          <el-menu-item index="/orders">订单</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="main">
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--el-border-color);
}
.brand {
  display: flex;
  gap: 8px;
  align-items: center;
  font-weight: 600;
}
.hint {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.aside {
  border-right: 1px solid var(--el-border-color);
}
.main {
  max-width: 1100px;
}
</style>

