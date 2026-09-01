<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ArrowRight, Tags } from 'lucide-vue-next'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { hotApi } from '@/api/hot'
import CategoryTabs from '@/components/CategoryTabs.vue'
import HotList from '@/components/HotList.vue'
import { useHotStore } from '@/stores/hot'
import { logger } from '@/utils/logger'
import type { HotCategory, HotItem } from '@/types/hot'

const hotStore = useHotStore()
const route = useRoute()
const router = useRouter()
const active = ref<HotCategory>('GENERAL')
const items = ref<HotItem[]>([])
const loading = ref(false)
let requestId = 0
const activeName = computed(() => hotStore.categories.find((item) => item.code === active.value)?.name || '综合')
const sourceCount = computed(() => new Set(items.value.map((item) => item.source)).size)

function syncRoute() {
  const code = String(route.params.code || 'GENERAL').toUpperCase() as HotCategory
  active.value = hotStore.categories.some((category) => category.code === code) ? code : 'GENERAL'
}
function selectCategory(value: HotCategory) {
  if (active.value !== value) void router.push({ name: 'category', params: { code: value.toLowerCase() } })
}

async function loadCategory(category: HotCategory) {
  const currentRequest = ++requestId
  loading.value = true
  logger.debug('Category ranking requested', { category, limit: 50 })
  try {
    const result = await hotApi.categoryHot(category, 50)
    if (currentRequest !== requestId || active.value !== category) return
    items.value = result
    logger.info('Category ranking loaded', {
      category,
      itemCount: result.length,
      sourceCount: new Set(result.map((item) => item.source)).size,
    })
  } catch (error) {
    if (currentRequest !== requestId || active.value !== category) return
    items.value = []
    logger.error('Failed to load category ranking', { category, message: String(error) })
  } finally {
    if (currentRequest === requestId) loading.value = false
  }
}

watch([() => route.params.code, () => hotStore.categories.length], () => {
  syncRoute()
  void loadCategory(active.value)
}, { immediate: true })
watch(() => hotStore.lastUpdateTime, () => void loadCategory(active.value))
onMounted(() => void hotStore.initialize())
</script>

<template>
  <div class="page-shell">
    <div class="page-kicker">按主题浏览</div>
    <h1 class="page-title">分类</h1>
    <p class="page-intro">聚合全部来源，按社会、科技、财经等主题查看跨平台榜单。</p>
    <div class="category-nav"><CategoryTabs :categories="hotStore.categories" :active="active" @select="selectCategory" /></div>
    <div class="category-content">
      <section>
        <div class="section-head"><div><h2 class="section-title">{{ activeName }}热榜</h2><span class="section-note">{{ items.length }} 条 · 来自 {{ sourceCount }} 个平台 · 原榜位次优先</span></div><Tags :size="19" color="#9aa0a6" /></div>
        <div v-if="loading" class="loading-line" aria-label="正在加载分类热榜" />
        <HotList v-else :items="items" :limit="50" sequential-rank />
      </section>
      <aside class="side-stack board-side">
        <section class="side-block"><div class="side-block-title"><h3>排序口径</h3><span>跨平台</span></div><p class="page-intro" style="font-size:13px;margin:0;line-height:1.8">优先参考内容在来源平台的原始名次，同位次再参考公开热度和更新时间。</p></section>
        <section class="side-block"><div class="side-block-title"><h3>常用分类</h3><span>快速切换</span></div><RouterLink v-for="category in hotStore.categories.slice(1, 4)" :key="category.code" :to="`/category/${category.code.toLowerCase()}`" class="notification-row"><div class="notification-row-title">{{ category.name }}</div><div class="notification-row-meta"><span>查看完整榜单</span><ArrowRight :size="13" /></div></RouterLink></section>
      </aside>
    </div>
  </div>
</template>
