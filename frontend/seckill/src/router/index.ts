import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../pages/HomePage.vue')
    },
    {
      path: '/products',
      name: 'products',
      component: () => import('../pages/ProductsPage.vue')
    },
    {
      path: '/products/:id(\\d+)',
      name: 'productDetail',
      component: () => import('../pages/ProductDetailPage.vue')
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('../pages/SearchPage.vue')
    },
    {
      path: '/stocks',
      name: 'stocks',
      component: () => import('../pages/StocksPage.vue')
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('../pages/OrdersPage.vue')
    }
  ]
})

export default router
