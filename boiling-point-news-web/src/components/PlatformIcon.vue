<script setup lang="ts">
import { computed } from 'vue'
import { ChartNoAxesCombined, CircleEllipsis } from 'lucide-vue-next'
import { siBaidu, siBilibili, siJuejin, siSinaweibo, siTiktok, siZhihu } from 'simple-icons'
import type { HotSource } from '@/types/hot'

const props = defineProps<{ source: HotSource }>()

const brandIcons: Partial<Record<HotSource, { path: string; hex: string }>> = {
  WEIBO: siSinaweibo,
  ZHIHU: siZhihu,
  BAIDU: siBaidu,
  DOUYIN: siTiktok,
  BILIBILI: siBilibili,
  JUEJIN: siJuejin,
}

const brandIcon = computed(() => brandIcons[props.source])
const iconColor = computed(() => {
  if (brandIcon.value) return `#${brandIcon.value.hex}`
  if (props.source === 'TOUTIAO') return '#ff373c'
  if (props.source === 'THE_PAPER') return '#1f6fb2'
  if (props.source === 'ITHOME') return '#1677ff'
  if (props.source === 'KR36') return '#222'
  if (props.source === 'JIN10') return '#1769aa'
  if (props.source === 'HACKER_NEWS') return '#ff6600'
  if (props.source === 'WALLSTREET_CN') return '#c49a32'
  return '#7c858b'
})
</script>

<template>
  <svg
    v-if="brandIcon"
    class="platform-icon"
    :style="{ '--platform-icon-color': iconColor }"
    viewBox="0 0 24 24"
    aria-hidden="true"
  >
    <path fill="currentColor" :d="brandIcon.path" />
  </svg>
  <!-- Jinritoutiao glyph from IconPark (Apache-2.0). -->
  <svg
    v-else-if="source === 'TOUTIAO'"
    class="platform-icon"
    :style="{ '--platform-icon-color': iconColor }"
    viewBox="0 0 48 48"
    aria-hidden="true"
  >
    <path d="M36.883 44h-25.77C7.192 44 4 40.808 4 36.883v-25.77C4 7.192 7.192 4 11.113 4h25.77c3.92 0 7.113 3.192 7.113 7.113v25.77C44 40.808 40.808 44 36.883 44M11.113 4.303a6.82 6.82 0 0 0-6.81 6.81v25.77a6.82 6.82 0 0 0 6.814 6.814h25.77a6.82 6.82 0 0 0 6.815-6.814v-25.77a6.82 6.82 0 0 0-6.815-6.815H11.113z" />
    <path fill-rule="evenodd" d="m4 36.887l40-2.092V11.113L4 13.205zm20.963-18.333s.933.677 1.035.745c.667.459 1.548 1.065 2.917 1.694c-1.26.386-2.709.768-4.382 1.136v2.314c3.191-.686 5.692-1.463 7.667-2.251c1.95.564 4.472 1.079 7.781 1.446v-2.314a52 52 0 0 1-4.474-.702a17.5 17.5 0 0 0 3.614-2.682v-.354h.004v-2.169h-.004l-.525.028l-8.974.47l-2.324-.963zm3.547-.412l7.475-.392c-.82.6-1.978 1.305-3.715 2.04c-1.73-.553-2.902-1.137-3.76-1.648m-11.798-2.094l2.459-.128c-.062 4.621-.148 7.388-.82 9.557l5.095-.267v2.17l-6.107.318c-.976 1.569-2.493 3.046-4.867 5.14l-3.874.205q.215-.183.416-.358l.074-.064c2.388-2.07 4.03-3.494 5.142-4.76l-6.228.324V26.02l7.718-.405c.86-1.97.928-4.717.992-9.567m-1.81 1.419v2.42L9.43 19.39v-2.417zM9.43 21.064v2.42l5.472.499v-2.42zm21.552 5.922l-6.449.338v-2.17l6.45-.337v-1.235l2.518-.128v1.23l6.48-.339v2.17l-6.48.34v4.888l-2.519.132zM21.06 28.59l-2.97.153l2.386 3.682l2.97-.153zm13.587-.712l2.966-.154l2.386 3.682l-2.966.154zm-4.75.247l-2.97.158l-2.386 3.929l2.97-.154z" clip-rule="evenodd" />
  </svg>
  <CircleEllipsis
    v-else-if="source !== 'WALLSTREET_CN'"
    class="platform-icon"
    :style="{ '--platform-icon-color': iconColor }"
    :size="16"
    :stroke-width="2.2"
    aria-hidden="true"
  />
  <ChartNoAxesCombined
    v-else
    class="platform-icon"
    :style="{ '--platform-icon-color': iconColor }"
    :size="16"
    :stroke-width="2.2"
    aria-hidden="true"
  />
</template>
