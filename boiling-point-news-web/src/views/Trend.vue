<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { ArrowDown, ArrowUp, Sparkles } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
import TrendChart from '@/components/TrendChart.vue'
import { buildTrend } from '@/api/mockData'
import { useHotStore } from '@/stores/hot'
import { formatHotValue } from '@/utils/format'

const hotStore = useHotStore()
const leading = computed(() => hotStore.latestHotItems[0])
const rising = computed(() => hotStore.latestHotItems.filter((item) => item.trend === 'UP').sort((a, b) => b.rankChange - a.rankChange).slice(0, 5))
const newItems = computed(() => hotStore.latestHotItems.filter((item) => item.trend === 'NEW').slice(0, 5))
const falling = computed(() => hotStore.latestHotItems.filter((item) => item.trend === 'DOWN').sort((a, b) => a.rankChange - b.rankChange).slice(0, 5))
onMounted(() => hotStore.initialize())
</script>

<template>
  <div class="page-shell">
    <div class="page-kicker">24小时变化</div>
    <h1 class="page-title">趋势</h1>
    <p class="page-intro">看热度如何上升、哪些话题刚刚出现，以及哪些讨论正在退潮。</p>

    <div v-if="leading" class="trend-overview">
      <section class="trend-main"><div class="trend-main-head"><div><div class="section-note">当前热度曲线 · 近24小时</div><h2 class="trend-main-title">{{ leading.title }}</h2></div><div class="trend-main-score"><strong>{{ leading.hotValueText }}</strong><span><ArrowUp :size="13" style="vertical-align:-2px" /> {{ leading.rankChange || 1 }} 位</span></div></div><div class="chart-wrap"><TrendChart :points="buildTrend(leading)" :height="240" /></div></section>
      <div class="trend-summary"><div class="trend-summary-cell"><div class="trend-summary-label">上涨最快</div><div class="trend-summary-value green">{{ rising[0] ? `+${rising[0].rankChange}` : '—' }}</div><div class="section-note">排名变化</div></div><div class="trend-summary-cell"><div class="trend-summary-label">新上榜</div><div class="trend-summary-value">{{ newItems.length }}</div><div class="section-note">条新讨论</div></div><div class="trend-summary-cell"><div class="trend-summary-label">退潮中</div><div class="trend-summary-value red">{{ falling.length }}</div><div class="section-note">条热度下降</div></div><div class="trend-summary-cell"><div class="trend-summary-label">持续热门</div><div class="trend-summary-value">{{ hotStore.latestHotItems.filter((item) => item.trend === 'STABLE').length }}</div><div class="section-note">条保持稳定</div></div></div>
    </div>

    <div class="trend-columns">
      <section class="trend-column"><h3><ArrowUp :size="17" color="#14946a" style="vertical-align:-3px" /> 上涨最快</h3><p>排名变化最明显的热点</p><div v-for="(item, index) in rising" :key="item.id" class="trend-mini-row"><span class="trend-mini-rank">0{{ index + 1 }}</span><RouterLink :to="`/hot/${item.id}`" class="trend-mini-title">{{ item.title }}</RouterLink><span class="trend-mini-value">+{{ item.rankChange }}</span></div></section>
      <section class="trend-column"><h3><Sparkles :size="17" color="#2868d7" style="vertical-align:-3px" /> 刚刚出现</h3><p>首次进入当前热榜的讨论</p><div v-for="(item, index) in newItems" :key="item.id" class="trend-mini-row"><span class="trend-mini-rank">0{{ index + 1 }}</span><RouterLink :to="`/hot/${item.id}`" class="trend-mini-title">{{ item.title }}</RouterLink><span class="trend-mini-value">{{ formatHotValue(item.hotValue) }}</span></div></section>
      <section class="trend-column"><h3><ArrowDown :size="17" color="#e72b31" style="vertical-align:-3px" /> 正在退潮</h3><p>热度和排名同时回落的热点</p><div v-for="(item, index) in falling" :key="item.id" class="trend-mini-row"><span class="trend-mini-rank">0{{ index + 1 }}</span><RouterLink :to="`/hot/${item.id}`" class="trend-mini-title">{{ item.title }}</RouterLink><span class="trend-mini-value">{{ item.rankChange }}</span></div></section>
    </div>
    <div v-if="!leading" class="empty-state"><strong>趋势数据准备中</strong>连接数据流后会显示热度曲线。</div>
  </div>
</template>
