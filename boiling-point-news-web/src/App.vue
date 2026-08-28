<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import { useHotStore } from '@/stores/hot'

const hotStore = useHotStore()

onMounted(() => hotStore.initialize())
onBeforeUnmount(() => hotStore.disconnectSse())
</script>

<template>
  <AppLayout />
</template>
