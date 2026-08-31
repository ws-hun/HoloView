<script setup lang="ts">
import { ArrowDown, ArrowUp, ExternalLink, Minus, Sparkles } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
import type { HotItem } from '@/types/hot'
import { formatHotValue, formatTime } from '@/utils/format'

const props = defineProps<{ item: HotItem; compact?: boolean }>()

const sourceColors: Record<string, string> = { WEIBO: '#e72b31', ZHIHU: '#1772f6', BAIDU: '#3b5bdb', DOUYIN: '#17191c', TOUTIAO: '#f04438', BILIBILI: '#00aeec', JUEJIN: '#1e80ff', THE_PAPER: '#1f6fb2', ITHOME: '#1677ff', KR36: '#222', OTHER: '#7b8186' }
function trendText() {
  if (props.item.trend === 'NEW') return '新上榜'
  if (props.item.trend === 'STABLE') return '持平'
  if (props.item.rankChange === 0) return '热度'
  return `${Math.abs(props.item.rankChange)}`
}

function displayTime() {
  const value = formatTime(props.item.updatedAt)
  return props.compact ? value.slice(-5) : value
}
</script>

<template>
  <article class="hot-list-item" :class="{ compact }">
    <span class="rank-number" :class="{ 'top-rank': item.rank <= 3 }">{{ String(item.rank).padStart(2, '0') }}</span>
    <div>
      <RouterLink :to="`/hot/${item.id}`" class="hot-item-title">{{ item.title }}</RouterLink>
      <p v-if="!compact" class="hot-item-description">{{ item.description }}</p>
      <div class="hot-item-meta">
        <RouterLink :to="`/hot/${item.id}`" class="source-label hot-item-source-link" :style="{ '--source-color': sourceColors[item.source] }"><i class="source-dot" />{{ item.sourceName }}</RouterLink>
        <span>{{ displayTime() }}</span>
        <span class="trend-badge" :class="`trend-${item.trend.toLowerCase()}`">
          <ArrowUp v-if="item.trend === 'UP'" :size="12" /><ArrowDown v-else-if="item.trend === 'DOWN'" :size="12" /><Sparkles v-else-if="item.trend === 'NEW'" :size="11" /><Minus v-else :size="11" />
          {{ trendText() }}
        </span>
      </div>
    </div>
    <div class="hot-item-score">
      <span class="hot-score-value">{{ item.hotValueText || formatHotValue(item.hotValue) }}</span>
      <span class="hot-score-label">热度指数</span>
      <div class="heat-track"><div class="heat-fill" :style="{ width: `${Math.max(10, Math.round(item.hotValue / 985600))}%` }" /></div>
    </div>
    <a v-if="compact" class="compact-external" :href="item.sourceUrl" target="_blank" rel="noreferrer" aria-label="打开来源"><ExternalLink :size="13" /></a>
  </article>
</template>

<style scoped>
.compact-external { display: none; }
.hot-list-item.compact { grid-template-columns: 34px minmax(0, 1fr) 94px; }
@media (max-width: 680px) { .hot-list-item.compact { grid-template-columns: 34px minmax(0, 1fr) 78px; } }
</style>
