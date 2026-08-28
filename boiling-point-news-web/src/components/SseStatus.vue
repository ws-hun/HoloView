<script setup lang="ts">
import { Radio } from 'lucide-vue-next'
import { useHotStore } from '@/stores/hot'

const hotStore = useHotStore()
</script>

<template>
  <span class="sse-status" :class="{ connected: hotStore.sseConnected }" :title="hotStore.sseConnected ? '实时热点流已连接' : '正在连接实时热点流'">
    <Radio :size="15" />
    <span class="sse-status-label">{{ hotStore.sseConnected ? '实时' : '连接中' }}</span>
  </span>
</template>

<style scoped>
.sse-status { display: inline-flex; align-items: center; gap: 5px; color: #9aa0a6; font-size: 11px; font-weight: 700; white-space: nowrap; }
.sse-status.connected { color: #14946a; }
.sse-status svg { stroke-width: 2.2; }
@media (max-width: 680px) { .sse-status-label { display: none; } }
</style>
