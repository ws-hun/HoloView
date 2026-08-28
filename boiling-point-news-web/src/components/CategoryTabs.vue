<script setup lang="ts">
import type { CategoryMeta, HotCategory } from '@/types/hot'

defineProps<{ categories: CategoryMeta[]; active: HotCategory }>()
const emit = defineEmits<{ select: [value: HotCategory] }>()
</script>

<template>
  <div class="tabs">
    <button v-for="category in categories" :key="category.code" class="tab-button" :class="{ active: active === category.code }" type="button" @click="emit('select', category.code)">{{ category.name }}</button>
  </div>
</template>
