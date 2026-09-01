<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ArrowRight, BarChart3 } from 'lucide-vue-next'
import { hotApi } from '@/api/hot'
import HotList from '@/components/HotList.vue'
import HotListSkeleton from '@/components/HotListSkeleton.vue'
import PlatformTabs from '@/components/PlatformTabs.vue'
import { useHotStore } from '@/stores/hot'
import { formatClock } from '@/utils/format'
import type { HotItem, HotSource } from '@/types/hot'

const hotStore = useHotStore()
const activeSource = ref<HotSource | 'ALL'>('ALL')
const sourceItems = ref<HotItem[]>([])
const sourceLoading = ref(false)
let requestId = 0
const items = computed(() => activeSource.value === 'ALL' ? hotStore.latestHotItems : sourceItems.value)
const activeName = computed(() => activeSource.value === 'ALL' ? '全网热榜' : hotStore.platforms.find((item) => item.code === activeSource.value)?.name || '平台热榜')

async function selectSource(source: HotSource | 'ALL') {
  activeSource.value = source
  const currentRequest = ++requestId
  if (source === 'ALL') {
    sourceItems.value = []
    sourceLoading.value = false
    return
  }
  sourceItems.value = []
  sourceLoading.value = true
  try {
    const result = await hotApi.list({ source, limit: 50 })
    if (currentRequest === requestId && activeSource.value === source) sourceItems.value = result
  } catch {
    if (currentRequest === requestId && activeSource.value === source) sourceItems.value = []
  } finally {
    if (currentRequest === requestId) sourceLoading.value = false
  }
}

watch(() => hotStore.lastUpdateTime, () => {
  if (activeSource.value !== 'ALL' && !sourceLoading.value) void selectSource(activeSource.value)
})
onMounted(() => hotStore.initialize())
</script>

<template>
  <div class="page-shell">
    <div class="page-kicker">平台热榜</div>
    <h1 class="page-title">热榜</h1>
    <p class="page-intro">切换来源，查看不同平台正在上升的讨论。榜单每分钟更新一次。</p>
    <div class="board-toolbar">
      <PlatformTabs :platforms="hotStore.platforms" :active="activeSource" @select="selectSource" />
      <span class="board-updated">更新于 {{ formatClock(hotStore.lastUpdateTime) }}</span>
    </div>
    <div class="category-content">
      <section class="board-list" :aria-busy="sourceLoading">
        <div class="section-head"><div><h2 class="section-title">{{ activeName }}</h2><span class="section-note">{{ sourceLoading ? '正在加载' : `${items.length} 个正在发生的讨论` }}</span></div><BarChart3 :size="19" color="#9aa0a6" /></div>
        <div class="board-list-content">
          <HotListSkeleton v-if="sourceLoading" />
          <HotList v-else :items="items" :limit="50" />
        </div>
      </section>
      <aside class="side-stack board-side">
        <section class="side-block"><div class="side-block-title"><h3>编辑提示</h3><span>阅读说明</span></div><p class="page-intro" style="font-size:13px;margin:0;line-height:1.8">排名反映平台热度，不代表事实结论。点击标题可以查看热度曲线和原始来源。</p></section>
        <section class="side-block"><div class="side-block-title"><h3>标记说明</h3><span><ArrowRight :size="12" style="vertical-align:-2px" /></span></div><div class="notification-row"><div class="notification-row-title">上涨</div><div class="notification-row-meta"><span>较上次采集排名上升</span><span style="color:#14946a">↑</span></div></div><div class="notification-row"><div class="notification-row-title">新上榜</div><div class="notification-row-meta"><span>首次进入当前榜单</span><span style="color:#2868d7">✦</span></div></div></section>
      </aside>
    </div>
  </div>
</template>
