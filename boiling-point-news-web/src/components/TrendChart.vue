<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts } from 'echarts/core'
import type { TrendPoint } from '@/types/hot'
import { formatHotValue } from '@/utils/format'

const props = withDefaults(defineProps<{ points: TrendPoint[]; height?: number }>(), { height: 240 })
const chartEl = ref<HTMLElement | null>(null)
let chart: ECharts | null = null

echarts.use([LineChart, GridComponent, TooltipComponent, CanvasRenderer])

function render() {
  if (!chartEl.value) return
  chart?.dispose()
  chart = echarts.init(chartEl.value)
  chart.setOption({
    animation: true,
    grid: { left: 5, right: 12, top: 10, bottom: 25, containLabel: true },
    tooltip: { trigger: 'axis', backgroundColor: '#151719', borderWidth: 0, textStyle: { color: '#fff', fontSize: 12 }, formatter: (params: unknown) => {
      const point = Array.isArray(params) ? params[0] as { dataIndex: number } : { dataIndex: 0 }
      const data = props.points[point.dataIndex]
      return `${formatHotValue(data.hotValue)}<br/>排名 #${data.rank}`
    } },
    xAxis: { type: 'category', boundaryGap: false, data: props.points.map((point) => new Date(point.recordedAt).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })), axisLine: { lineStyle: { color: '#dfe3e6' } }, axisTick: { show: false }, axisLabel: { color: '#9aa0a6', fontSize: 10 } },
    yAxis: { type: 'value', splitNumber: 3, axisLabel: { color: '#9aa0a6', fontSize: 10, formatter: (value: number) => formatHotValue(value) }, splitLine: { lineStyle: { color: '#edf0f1' } }, axisLine: { show: false }, axisTick: { show: false } },
    series: [{ type: 'line', smooth: .35, symbol: 'circle', symbolSize: 5, showSymbol: false, data: props.points.map((point) => point.hotValue), lineStyle: { width: 3, color: '#e72b31' }, itemStyle: { color: '#e72b31', borderColor: '#fff', borderWidth: 2 }, areaStyle: { color: 'rgba(231,43,49,.08)' } }],
  })
}

function resize() { chart?.resize() }
onMounted(async () => { await nextTick(); render(); window.addEventListener('resize', resize) })
watch(() => props.points, render, { deep: true })
onBeforeUnmount(() => { window.removeEventListener('resize', resize); chart?.dispose() })
</script>

<template><div ref="chartEl" class="trend-chart" :style="{ height: `${height}px` }" /></template>
