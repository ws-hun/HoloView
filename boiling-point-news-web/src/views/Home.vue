<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ArrowRight, Clock3, Sparkles } from 'lucide-vue-next'
import { RouterLink, useRoute } from 'vue-router'
import { hotApi } from '@/api/hot'
import HotList from '@/components/HotList.vue'
import { useHotStore } from '@/stores/hot'
import { formatClock, formatHotValue } from '@/utils/format'
import type { HotItem } from '@/types/hot'

const hotStore = useHotStore()
const route = useRoute()
const currentTime = ref(new Date().toISOString())
const searchResults = ref<HotItem[]>([])
const searchLoading = ref(false)
let clockTimer: number | null = null
let searchRequestId = 0

const query = computed(() => typeof route.query.q === 'string' ? route.query.q.trim() : '')
const currentDate = computed(() => new Intl.DateTimeFormat('zh-CN', {
  month: '2-digit',
  day: '2-digit',
}).format(new Date(currentTime.value)).replace('/', '月') + '日')
const filteredItems = computed(() => {
  if (!query.value) return hotStore.latestHotItems
  return searchResults.value
})
const hero = computed<HotItem | undefined>(() => filteredItems.value[0])
const platformPulse = computed(() => hotStore.platforms.map((platform) => {
  const items = hotStore.latestHotItems.filter((item) => item.source === platform.code)
  const score = items.reduce((total, item) => total + item.hotValue, 0)
  const ratio = score > 0 ? Math.max(8, Math.min(100, score / Math.max(1, hotStore.totalHeat) * 100)) : 0
  const colors: Record<string, string> = {
    WEIBO: '#e72b31', ZHIHU: '#1772f6', BAIDU: '#3b5bdb', DOUYIN: '#17191c',
    TOUTIAO: '#f04438', BILIBILI: '#00aeec', JUEJIN: '#1e80ff', THE_PAPER: '#1f6fb2',
    ITHOME: '#1677ff', KR36: '#222222', JIN10: '#1769aa', HACKER_NEWS: '#ff6600',
    WALLSTREET_CN: '#c49a32', NOWCODER: '#00a98f', TIEBA: '#4e6ef2', HUPU: '#c51f25',
    DOUBAN_MOVIE: '#2e963d', GITHUB_TRENDING: '#24292f', STEAM: '#1b2838',
    SMZDM: '#e62828', DONGQIUDI: '#16a05d', OTHER: '#7b8186',
  }
  return { ...platform, count: items.length, score, ratio, color: colors[platform.code] || platform.color }
}))

async function loadSearchResults() {
  const keyword = query.value
  const request = ++searchRequestId
  if (!keyword) {
    searchResults.value = []
    searchLoading.value = false
    return
  }
  searchLoading.value = true
  try {
    const result = await hotApi.search(keyword, 50)
    if (request === searchRequestId && query.value === keyword) searchResults.value = result
  } catch {
    if (request === searchRequestId && query.value === keyword) searchResults.value = []
  } finally {
    if (request === searchRequestId) searchLoading.value = false
  }
}

watch(query, () => { void loadSearchResults() }, { immediate: true })

onMounted(() => {
  clockTimer = window.setInterval(() => { currentTime.value = new Date().toISOString() }, 1000)
})
onBeforeUnmount(() => { if (clockTimer !== null) window.clearInterval(clockTimer) })
</script>

<template>
  <div class="page-shell">
    <section class="home-intro">
      <div>
        <div class="page-kicker">今日焦点 · {{ currentDate }}</div>
        <h1 class="page-title">今天，大家<br class="hidden md:block" />都在看什么</h1>
        <p class="page-intro">聚合多个公开新闻与社区榜单，按热度统一排序。先看发生了什么，再决定要不要深入。</p>
      </div>
      <div class="live-clock">
        <span class="live-label"><i class="live-dot" />{{ hotStore.dataMode === 'mock' ? '演示数据 · 模拟实时' : '数据流实时连接' }}</span>
        <div class="clock-time"><Clock3 :size="20" style="vertical-align: -3px; margin-right: 5px;" />{{ formatClock(currentTime) }}</div>
        <div class="clock-date">最后更新 {{ formatClock(hotStore.lastUpdateTime) }}</div>
      </div>
    </section>

    <div v-if="hotStore.loading" class="loading-line" aria-label="正在加载" />

    <template v-if="hero">
      <section class="hero-story" aria-labelledby="hero-title">
        <div class="hero-story-copy">
          <div>
            <div class="hero-story-label"><span>第 1 位</span>当前全网最高热度</div>
            <h2 id="hero-title" class="hero-story-title">{{ hero.title }}</h2>
            <p class="hero-story-description">{{ hero.description }}</p>
          </div>
          <div class="hero-story-meta">
            <span>{{ hero.sourceName }} · {{ hero.categoryName }}</span>
            <span class="trend-badge trend-up">↑ {{ Math.abs(hero.rankChange) || 1 }} 位</span>
            <RouterLink :to="`/hot/${hero.id}`" class="source-link">查看详情 <ArrowRight :size="14" /></RouterLink>
          </div>
        </div>
        <div class="hero-story-image"><img :src="hero.cover" :alt="hero.title" /></div>
      </section>

      <section class="hot-layout">
        <div>
          <div class="section-head">
            <div><h2 class="section-title">{{ query ? `“${query}”的搜索结果` : '今日热榜' }}</h2><span class="section-note">{{ query ? (searchLoading ? '正在搜索全量热点' : `${filteredItems.length} 条匹配结果`) : '按全网热度排序 · 每分钟刷新' }}</span></div>
            <RouterLink to="/boards" class="source-link">完整热榜 <ArrowRight :size="14" /></RouterLink>
          </div>
          <HotList :items="filteredItems" :limit="10" sequential-rank />
        </div>

        <aside class="side-stack">
          <section class="side-block">
            <div class="side-block-title"><h3>热度总览</h3><span>实时数据</span></div>
            <div class="stat-grid">
              <div class="stat-cell"><div class="stat-cell-label">全网热点</div><div class="stat-cell-value">{{ hotStore.latestHotItems.length }}</div></div>
              <div class="stat-cell"><div class="stat-cell-label">总热度</div><div class="stat-cell-value red">{{ formatHotValue(hotStore.totalHeat) }}</div></div>
              <div class="stat-cell"><div class="stat-cell-label">上涨中</div><div class="stat-cell-value green">{{ hotStore.risingCount }}</div></div>
              <div class="stat-cell"><div class="stat-cell-label">新上榜</div><div class="stat-cell-value">{{ hotStore.newCount }}</div></div>
            </div>
          </section>

          <section class="side-block">
            <div class="side-block-title"><h3>平台分布</h3><span>按来源</span></div>
            <div v-for="platform in platformPulse" :key="platform.code" class="pulse-row">
              <span class="pulse-name">{{ platform.shortName }}</span><span class="pulse-track"><i class="pulse-fill" :style="{ width: `${platform.ratio}%`, background: platform.color }" /></span><span class="pulse-score">{{ platform.count }} 条</span>
            </div>
          </section>

          <section class="side-block">
            <div class="side-block-title"><h3>刚刚发生</h3><span><Sparkles :size="12" style="vertical-align:-2px" /> 更新</span></div>
            <div v-if="hotStore.notifications.length">
              <div v-for="notice in hotStore.notifications.slice(0, 4)" :key="notice.id" class="notification-row"><div class="notification-row-title">{{ notice.title }}</div><div class="notification-row-meta"><span>{{ notice.message }}</span><span>刚刚</span></div></div>
            </div>
            <div v-else class="section-note">实时热点变化将在这里出现。</div>
          </section>
        </aside>
      </section>
    </template>
    <div v-else-if="searchLoading" class="empty-state"><strong>正在搜索全量热点</strong>正在从已采集的公开信息源中匹配内容。</div>
    <div v-else class="empty-state"><strong>没有找到相关热点</strong>换个关键词试试看。</div>

  </div>
</template>
