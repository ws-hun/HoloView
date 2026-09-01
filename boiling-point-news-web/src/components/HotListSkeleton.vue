<script setup lang="ts">
withDefaults(defineProps<{ rows?: number }>(), { rows: 6 })
</script>

<template>
  <div class="hot-list board-loading-list" role="status" aria-label="正在加载热点">
    <div v-for="index in rows" :key="index" class="hot-list-item board-loading-row" aria-hidden="true">
      <span class="skeleton-block skeleton-rank" />
      <div><span class="skeleton-block skeleton-title" /><span class="skeleton-block skeleton-meta" /></div>
      <div><span class="skeleton-block skeleton-score" /><span class="skeleton-block skeleton-track" /></div>
    </div>
  </div>
</template>
