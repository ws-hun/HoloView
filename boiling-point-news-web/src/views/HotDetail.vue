<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ArrowLeft, ArrowUp, ExternalLink, Radio } from 'lucide-vue-next'
import { RouterLink, useRoute } from 'vue-router'
import HotList from '@/components/HotList.vue'
import TrendChart from '@/components/TrendChart.vue'
import { buildTrend } from '@/api/mockData'
import { useHotStore } from '@/stores/hot'
import { hotApi } from '@/api/hot'
import { formatHotValue, formatTime } from '@/utils/format'
import type { HotItem, TrendPoint } from '@/types/hot'

const hotStore = useHotStore()
const route = useRoute()
const item = ref<HotItem | null>(null)
const trend = ref<TrendPoint[]>([])
const related = computed(() => item.value ? hotStore.latestHotItems.filter((candidate) => candidate.id !== item.value?.id && candidate.category === item.value?.category).slice(0, 4) : [])

async function load() {
  await hotStore.initialize()
  const id = Number(route.params.id)
  item.value = hotStore.latestHotItems.find((candidate) => candidate.id === id) || await hotApi.detail(id)
  if (item.value) trend.value = await hotApi.trend(item.value.id)
}
watch(() => route.params.id, load)
onMounted(load)
</script>

<template>
  <div class="page-shell">
    <RouterLink to="/" class="detail-back"><ArrowLeft :size="15" /> 返回热点首页</RouterLink>
    <template v-if="item">
      <section class="detail-header">
        <div>
          <div class="page-kicker">热点详情 · {{ item.categoryName }}</div>
          <h1 class="detail-title">{{ item.title }}</h1>
          <p class="detail-description">{{ item.description }}</p>
          <div class="detail-meta-row"><span class="source-label"><i class="source-dot" />{{ item.sourceName }}</span><span>发布于 {{ formatTime(item.publishedAt) }}</span><span>更新于 {{ formatTime(item.updatedAt) }}</span><a class="source-link" :href="item.sourceUrl" target="_blank" rel="noreferrer">打开原始来源 <ExternalLink :size="13" /></a></div>
          <div class="detail-metrics"><div class="detail-metric"><div class="detail-metric-label">当前排名</div><div class="detail-metric-value">#{{ item.rank }}</div></div><div class="detail-metric"><div class="detail-metric-label">当前热度</div><div class="detail-metric-value">{{ item.hotValueText || formatHotValue(item.hotValue) }}</div></div><div class="detail-metric"><div class="detail-metric-label">排名变化</div><div class="detail-metric-value" :class="item.rankChange >= 0 ? 'trend-up' : 'trend-down'"><ArrowUp v-if="item.rankChange >= 0" :size="18" style="vertical-align:-2px" />{{ Math.abs(item.rankChange) || '—' }}</div></div></div>
        </div>
        <img class="detail-cover" :src="item.cover" :alt="item.title" />
      </section>
      <section class="detail-content">
        <div class="detail-chart"><div class="section-head"><div><h2 class="section-title">热度趋势</h2><span class="section-note">记录最近 24 小时的热度变化</span></div><Radio :size="18" color="#14946a" /></div><div class="chart-wrap"><TrendChart :points="trend.length ? trend : buildTrend(item)" :height="300" /></div></div>
        <aside class="side-stack board-side"><section class="side-block"><div class="side-block-title"><h3>实时状态</h3><span>{{ hotStore.sseConnected ? '已连接' : '连接中' }}</span></div><div class="notification-row"><div class="notification-row-title">{{ hotStore.sseConnected ? '正在监听热度变化' : '等待数据流连接' }}</div><div class="notification-row-meta"><span>来源：{{ item.sourceName }}</span><span class="trend-badge trend-up">{{ item.trend === 'NEW' ? '新上榜' : '实时' }}</span></div></div></section><section class="side-block"><div class="side-block-title"><h3>相关热点</h3><span>同一分类</span></div><HotList :items="related" :limit="4" :compact="true" /></section></aside>
      </section>
    </template>
    <div v-else class="empty-state"><strong>热点不存在</strong>这条内容可能已经离开当前榜单。</div>
  </div>
</template>
