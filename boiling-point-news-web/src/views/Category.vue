<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ArrowRight, Tags } from 'lucide-vue-next'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import CategoryTabs from '@/components/CategoryTabs.vue'
import HotList from '@/components/HotList.vue'
import { useHotStore } from '@/stores/hot'
import type { HotCategory } from '@/types/hot'

const hotStore = useHotStore()
const route = useRoute()
const router = useRouter()
const active = ref<HotCategory>('GENERAL')
const items = computed(() => active.value === 'GENERAL' ? hotStore.latestHotItems : hotStore.latestHotItems.filter((item) => item.category === active.value))
const activeName = computed(() => hotStore.categories.find((item) => item.code === active.value)?.name || '综合')

function syncRoute() {
  const code = String(route.params.code || 'GENERAL').toUpperCase() as HotCategory
  active.value = hotStore.categories.some((category) => category.code === code) ? code : 'GENERAL'
}
function selectCategory(value: HotCategory) {
  active.value = value
  router.push({ name: 'category', params: { code: value.toLowerCase() } })
}
watch(() => route.params.code, syncRoute)
onMounted(() => { hotStore.initialize(); syncRoute() })
</script>

<template>
  <div class="page-shell">
    <div class="page-kicker">按主题浏览</div>
    <h1 class="page-title">分类</h1>
    <p class="page-intro">按主题筛选正在升温的讨论。综合分类保留全网最热的内容。</p>
    <div class="category-nav"><CategoryTabs :categories="hotStore.categories" :active="active" @select="selectCategory" /></div>
    <div class="category-content">
      <section>
        <div class="section-head"><div><h2 class="section-title">{{ activeName }}热点</h2><span class="section-note">共 {{ items.length }} 条 · 由热度排序</span></div><Tags :size="19" color="#9aa0a6" /></div>
        <HotList :items="items" />
      </section>
      <aside class="side-stack board-side">
        <section class="side-block"><div class="side-block-title"><h3>分类说明</h3><span>浏览提示</span></div><p class="page-intro" style="font-size:13px;margin:0;line-height:1.8">同一条新闻可能出现在多个平台。进入详情，可以查看来源和热度变化。</p></section>
        <section class="side-block"><div class="side-block-title"><h3>热门分类</h3><span>当前排行</span></div><RouterLink v-for="category in hotStore.categories.slice(1, 4)" :key="category.code" :to="`/category/${category.code.toLowerCase()}`" class="notification-row"><div class="notification-row-title">{{ category.name }}</div><div class="notification-row-meta"><span>{{ hotStore.latestHotItems.filter((item) => item.category === category.code).length }} 条热点</span><ArrowRight :size="13" /></div></RouterLink></section>
      </aside>
    </div>
  </div>
</template>
