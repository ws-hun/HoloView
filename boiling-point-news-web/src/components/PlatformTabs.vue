<script setup lang="ts">
import { Globe2 } from 'lucide-vue-next'
import PlatformIcon from '@/components/PlatformIcon.vue'
import type { HotSource, PlatformMeta } from '@/types/hot'

defineProps<{ platforms: PlatformMeta[]; active: HotSource | 'ALL' }>()
const emit = defineEmits<{ select: [value: HotSource | 'ALL'] }>()
</script>

<template>
  <div class="tabs">
    <button class="tab-button" :class="{ active: active === 'ALL' }" type="button" @click="emit('select', 'ALL')">
      <Globe2 class="platform-icon" :size="16" :stroke-width="2.2" aria-hidden="true" />全网
    </button>
    <button v-for="platform in platforms" :key="platform.code" class="tab-button" :class="{ active: active === platform.code }" type="button" @click="emit('select', platform.code)">
      <PlatformIcon :source="platform.code" />{{ platform.name }}
    </button>
  </div>
</template>
