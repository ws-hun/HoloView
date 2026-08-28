<script setup lang="ts">
import type { HotSource, PlatformMeta } from '@/types/hot'

defineProps<{ platforms: PlatformMeta[]; active: HotSource | 'ALL' }>()
const emit = defineEmits<{ select: [value: HotSource | 'ALL'] }>()
</script>

<template>
  <div class="tabs">
    <button class="tab-button" :class="{ active: active === 'ALL' }" type="button" @click="emit('select', 'ALL')">全网</button>
    <button v-for="platform in platforms" :key="platform.code" class="tab-button" :class="{ active: active === platform.code }" type="button" @click="emit('select', platform.code)">
      <i class="tab-platform-dot" :style="{ '--tab-color': platform.color }" />{{ platform.name }}
    </button>
  </div>
</template>
