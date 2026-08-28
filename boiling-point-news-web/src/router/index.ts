import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: () => import('@/views/Home.vue') },
    { path: '/boards', name: 'boards', component: () => import('@/views/HotBoard.vue') },
    { path: '/category/:code?', name: 'category', component: () => import('@/views/Category.vue') },
    { path: '/trends', name: 'trends', component: () => import('@/views/Trend.vue') },
    { path: '/hot/:id', name: 'hot-detail', component: () => import('@/views/HotDetail.vue') },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
  scrollBehavior: () => ({ top: 0 }),
})

export default router
