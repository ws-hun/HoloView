import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { hotApi } from '@/api/hot'
import { formatHotValue } from '@/utils/format'
import { logger } from '@/utils/logger'
import type { CategoryMeta, HotItem, HotNotification, HotSource, PlatformMeta } from '@/types/hot'

export const useHotStore = defineStore('hot', () => {
  const latestHotItems = ref<HotItem[]>([])
  const platforms = ref<PlatformMeta[]>([])
  const categories = ref<CategoryMeta[]>([])
  const notifications = ref<HotNotification[]>([])
  const sseConnected = ref(false)
  const lastUpdateTime = ref(new Date().toISOString())
  const loading = ref(false)
  const dataMode = import.meta.env.VITE_DATA_MODE === 'live' ? 'live' : 'mock'
  let eventSource: EventSource | null = null
  let mockTimer: number | null = null

  const totalHeat = computed(() => latestHotItems.value.reduce((total, item) => total + item.hotValue, 0))
  const risingCount = computed(() => latestHotItems.value.filter((item) => item.trend === 'UP').length)
  const newCount = computed(() => latestHotItems.value.filter((item) => item.trend === 'NEW').length)

  async function initialize() {
    if (latestHotItems.value.length) return
    loading.value = true
    try {
      const [items, platformList, categoryList] = await Promise.all([
        hotApi.list(),
        hotApi.platforms(),
        hotApi.categories(),
      ])
      latestHotItems.value = items
      platforms.value = platformList
      categories.value = categoryList
      lastUpdateTime.value = new Date().toISOString()
      logger.info('Hot data initialized', { count: items.length, mode: dataMode })
      connectSse()
    } catch (error) {
      logger.error('Failed to initialize hot data', { message: String(error) })
    } finally {
      loading.value = false
    }
  }

  function connectSse() {
    disconnectSse()
    if (dataMode === 'mock') {
      sseConnected.value = true
      mockTimer = window.setInterval(simulateUpdate, 12_000)
      logger.info('Mock realtime stream connected')
      return
    }

    const url = import.meta.env.VITE_SSE_URL || '/api/sse/hot'
    eventSource = new EventSource(url)
    eventSource.onopen = () => {
      sseConnected.value = true
      logger.info('SSE connected', { url })
    }
    eventSource.addEventListener('hot-update', (event) => {
      try {
        const update = JSON.parse((event as MessageEvent<string>).data) as { persistedCount?: number; source?: string }
        void refreshLiveItems(update)
      } catch (error) {
        logger.error('Invalid SSE payload', { message: String(error) })
      }
    })
    eventSource.onerror = () => {
      sseConnected.value = false
      logger.warn('SSE disconnected; browser will retry', { url })
    }
  }

  async function refreshLiveItems(context: { persistedCount?: number; source?: string }) {
    try {
      const items = await hotApi.list()
      latestHotItems.value = items
      lastUpdateTime.value = new Date().toISOString()
      if (context.persistedCount) {
        notifications.value.unshift({
          id: `${context.source || 'all'}-${Date.now()}`,
          title: '热点榜单已更新',
          message: `${context.persistedCount} 条内容完成采集`,
          createdAt: lastUpdateTime.value,
        })
        notifications.value = notifications.value.slice(0, 6)
      }
      logger.info('Live hot data refreshed', context)
    } catch (error) {
      logger.error('Failed to refresh live hot data', { message: String(error), ...context })
    }
  }

  function disconnectSse() {
    eventSource?.close()
    eventSource = null
    if (mockTimer !== null) window.clearInterval(mockTimer)
    mockTimer = null
    sseConnected.value = false
  }

  function applyUpdate(update: Partial<HotItem> & { id: number }) {
    const index = latestHotItems.value.findIndex((item) => item.id === update.id)
    if (index < 0) return
    const current = latestHotItems.value[index]
    latestHotItems.value[index] = { ...current, ...update, updatedAt: new Date().toISOString() }
    latestHotItems.value.sort((a, b) => b.hotValue - a.hotValue)
    latestHotItems.value.forEach((item, itemIndex) => { item.rank = itemIndex + 1 })
    lastUpdateTime.value = new Date().toISOString()
  }

  function simulateUpdate() {
    if (!latestHotItems.value.length) return
    const index = Math.floor(Math.random() * Math.min(6, latestHotItems.value.length))
    const item = latestHotItems.value[index]
    const increment = Math.round(item.hotValue * (0.006 + Math.random() * 0.014))
    applyUpdate({ id: item.id, hotValue: item.hotValue + increment, hotValueText: formatHotValue(item.hotValue + increment), trend: 'UP' })
    notifications.value.unshift({
      id: `${item.id}-${Date.now()}`,
      title: item.title,
      message: `热度新增 ${formatHotValue(increment)}`,
      createdAt: new Date().toISOString(),
    })
    notifications.value = notifications.value.slice(0, 6)
    logger.info('Mock hot update', { hotId: item.id, increment })
  }

  function bySource(source?: HotSource) {
    return source ? latestHotItems.value.filter((item) => item.source === source) : latestHotItems.value
  }

  return {
    latestHotItems,
    platforms,
    categories,
    notifications,
    sseConnected,
    lastUpdateTime,
    loading,
    dataMode,
    totalHeat,
    risingCount,
    newCount,
    initialize,
    connectSse,
    disconnectSse,
    bySource,
  }
})
