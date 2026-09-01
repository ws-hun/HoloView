<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { Flame, Menu, Search, X } from 'lucide-vue-next'
import SseStatus from '@/components/SseStatus.vue'

const route = useRoute()
const router = useRouter()
const searchValue = ref('')
const menuOpen = ref(false)
const dataSourceLabel = import.meta.env.VITE_DATA_MODE === 'live'
  ? '当前聚合多个公开信息源'
  : '当前使用本地 Mock 热点流'
const navItems = [
  { name: '首页', to: '/', icon: '◉' },
  { name: '热榜', to: '/boards', icon: '↗' },
  { name: '分类', to: '/category', icon: '◌' },
  { name: '趋势', to: '/trends', icon: '⌁' },
]

const isDetail = computed(() => route.name === 'hot-detail')

function submitSearch() {
  const keyword = searchValue.value.trim()
  menuOpen.value = false
  if (keyword) router.push({ name: 'home', query: { q: keyword } })
  else router.push({ name: 'home' })
}
</script>

<template>
  <div class="app-frame">
    <header class="site-header">
      <div class="header-inner">
        <RouterLink to="/" class="brand" aria-label="回到沸点速报首页">
          <span class="brand-mark"><Flame :size="19" :stroke-width="2.4" /></span>
          <span><strong class="brand-name">沸点速报</strong><small class="brand-sub">今日热点实时更新</small></span>
        </RouterLink>

        <nav class="main-nav" aria-label="主导航">
          <RouterLink v-for="item in navItems" :key="item.to" :to="item.to" class="nav-link">
            <span aria-hidden="true">{{ item.icon }}</span>{{ item.name }}
          </RouterLink>
        </nav>

        <div class="header-tools">
          <form class="search-box" role="search" @submit.prevent="submitSearch">
            <button class="search-submit" type="submit" aria-label="提交搜索"><Search :size="16" /></button>
            <input v-model="searchValue" aria-label="搜索热点" placeholder="搜索热点" />
          </form>
          <SseStatus />
          <button class="mobile-menu-button" type="button" :aria-expanded="menuOpen" aria-label="打开导航" @click="menuOpen = !menuOpen">
            <X v-if="menuOpen" :size="18" /><Menu v-else :size="18" />
          </button>
        </div>
      </div>
      <nav v-if="menuOpen" class="header-mobile-nav" aria-label="移动端导航">
        <RouterLink v-for="item in navItems" :key="item.to" :to="item.to" class="nav-link" @click="menuOpen = false">
          {{ item.name }}
        </RouterLink>
      </nav>
    </header>

    <main><RouterView /></main>

    <footer v-if="!isDetail" class="site-footer">
      <div class="footer-inner">
        <p><strong>沸点速报</strong> · 把全网正在发生的事，放在一起看。</p>
        <p>数据每分钟刷新 · {{ dataSourceLabel }}</p>
      </div>
    </footer>
  </div>
</template>
