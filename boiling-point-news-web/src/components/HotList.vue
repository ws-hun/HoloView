<script setup lang="ts">
import { computed } from 'vue'
import HotListItem from '@/components/HotListItem.vue'
import type { HotItem } from '@/types/hot'

const props = withDefaults(defineProps<{ items: HotItem[]; limit?: number; compact?: boolean; sequentialRank?: boolean }>(), {
  compact: false,
  sequentialRank: false,
})
const visibleItems = computed(() => props.items.slice(0, props.limit || props.items.length))
const maxHeat = computed(() => Math.max(1, ...visibleItems.value.map((item) => item.hotValue)))
</script>

<template>
  <div v-if="items.length" class="hot-list">
    <HotListItem
      v-for="(item, index) in visibleItems"
      :key="item.id"
      :item="item"
      :compact="compact"
      :display-rank="sequentialRank ? index + 1 : undefined"
      :heat-percent="Math.max(8, Math.round(item.hotValue / maxHeat * 100))"
    />
  </div>
  <div v-else class="empty-state"><strong>暂时没有匹配的热点</strong>试试更换关键词或分类。</div>
</template>
