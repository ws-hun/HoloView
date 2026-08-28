<script setup lang="ts">
import HotListItem from '@/components/HotListItem.vue'
import type { HotItem } from '@/types/hot'

withDefaults(defineProps<{ items: HotItem[]; limit?: number; compact?: boolean }>(), { compact: false })
</script>

<template>
  <div v-if="items.length" class="hot-list">
    <HotListItem v-for="item in items.slice(0, limit || items.length)" :key="item.id" :item="item" :compact="compact" />
  </div>
  <div v-else class="empty-state"><strong>暂时没有匹配的热点</strong>试试更换关键词或分类。</div>
</template>
