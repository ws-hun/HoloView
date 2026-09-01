<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ChevronLeft, ChevronRight, Globe2 } from 'lucide-vue-next'
import PlatformIcon from '@/components/PlatformIcon.vue'
import type { HotSource, PlatformMeta } from '@/types/hot'

const props = defineProps<{ platforms: PlatformMeta[]; active: HotSource | 'ALL' }>()
const emit = defineEmits<{ select: [value: HotSource | 'ALL'] }>()

const tabsRef = ref<HTMLElement | null>(null)
const canScrollLeft = ref(false)
const canScrollRight = ref(false)
let resizeObserver: ResizeObserver | null = null
let dragPointerId: number | null = null
let dragStartX = 0
let dragStartScrollLeft = 0
let didDrag = false

function updateScrollState() {
  const tabs = tabsRef.value
  if (!tabs) return
  canScrollLeft.value = tabs.scrollLeft > 1
  canScrollRight.value = tabs.scrollLeft < tabs.scrollWidth - tabs.clientWidth - 1
}

function scrollByPage(direction: -1 | 1) {
  const tabs = tabsRef.value
  if (!tabs) return
  tabs.scrollBy({ left: direction * Math.max(220, tabs.clientWidth * 0.72), behavior: 'smooth' })
}

function handleWheel(event: WheelEvent) {
  const tabs = tabsRef.value
  if (!tabs) return
  const delta = Math.abs(event.deltaX) > Math.abs(event.deltaY) ? event.deltaX : event.deltaY
  if (!delta) return
  const canMove = delta < 0 ? canScrollLeft.value : canScrollRight.value
  if (!canMove) return
  event.preventDefault()
  tabs.scrollLeft += delta
}

function handlePointerDown(event: PointerEvent) {
  const tabs = tabsRef.value
  if (!tabs || event.pointerType !== 'mouse' || event.button !== 0) return
  dragPointerId = event.pointerId
  dragStartX = event.clientX
  dragStartScrollLeft = tabs.scrollLeft
  didDrag = false
  tabs.setPointerCapture(event.pointerId)
}

function handlePointerMove(event: PointerEvent) {
  const tabs = tabsRef.value
  if (!tabs || dragPointerId !== event.pointerId) return
  const distance = event.clientX - dragStartX
  if (Math.abs(distance) > 4) {
    didDrag = true
    tabs.classList.add('dragging')
  }
  if (didDrag) tabs.scrollLeft = dragStartScrollLeft - distance
}

function finishPointerDrag(event: PointerEvent) {
  const tabs = tabsRef.value
  if (!tabs || dragPointerId !== event.pointerId) return
  dragPointerId = null
  tabs.classList.remove('dragging')
  if (tabs.hasPointerCapture(event.pointerId)) tabs.releasePointerCapture(event.pointerId)
  window.setTimeout(() => { didDrag = false }, 0)
}

function selectPlatform(value: HotSource | 'ALL') {
  if (didDrag) return
  emit('select', value)
}

async function revealActivePlatform(behavior: ScrollBehavior = 'smooth') {
  await nextTick()
  const tabs = tabsRef.value
  if (!tabs) return
  const activeButton = Array.from(tabs.querySelectorAll<HTMLElement>('[data-source]'))
    .find((button) => button.dataset.source === props.active)
  activeButton?.scrollIntoView({ behavior, block: 'nearest', inline: 'center' })
  window.setTimeout(updateScrollState, behavior === 'smooth' ? 350 : 0)
}

watch(() => props.active, () => { void revealActivePlatform() })
watch(() => props.platforms.length, () => { void revealActivePlatform('auto') })

onMounted(() => {
  resizeObserver = new ResizeObserver(updateScrollState)
  if (tabsRef.value) resizeObserver.observe(tabsRef.value)
  void revealActivePlatform('auto')
})

onBeforeUnmount(() => resizeObserver?.disconnect())
</script>

<template>
  <div class="platform-tabs-scroller">
    <button class="tabs-scroll-button" type="button" aria-label="向左查看更多平台" :disabled="!canScrollLeft" @click="scrollByPage(-1)">
      <ChevronLeft :size="17" aria-hidden="true" />
    </button>
    <div
      ref="tabsRef"
      class="tabs platform-tabs"
      @scroll="updateScrollState"
      @wheel="handleWheel"
      @pointerdown="handlePointerDown"
      @pointermove="handlePointerMove"
      @pointerup="finishPointerDrag"
      @pointercancel="finishPointerDrag"
    >
      <button data-source="ALL" class="tab-button" :class="{ active: active === 'ALL' }" type="button" @click="selectPlatform('ALL')">
        <Globe2 class="platform-icon" :size="16" :stroke-width="2.2" aria-hidden="true" />全网
      </button>
      <button v-for="platform in platforms" :key="platform.code" :data-source="platform.code" class="tab-button" :class="{ active: active === platform.code }" type="button" @click="selectPlatform(platform.code)">
        <PlatformIcon :source="platform.code" />{{ platform.name }}
      </button>
    </div>
    <button class="tabs-scroll-button" type="button" aria-label="向右查看更多平台" :disabled="!canScrollRight" @click="scrollByPage(1)">
      <ChevronRight :size="17" aria-hidden="true" />
    </button>
  </div>
</template>
