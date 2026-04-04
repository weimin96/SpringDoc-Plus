<script setup lang="ts">
import { computed, ref } from 'vue'
import type { SimulateResult } from '@/composables/useSimulateRequest'

const props = defineProps<{
  result: SimulateResult | null
  error: string | null
}>()

const previewHtml = ref(false)

const responseContentType = computed(() => props.result?.headers?.['content-type'] ?? '')
const isHtmlResponse = computed(() => {
  return typeof props.result?.data === 'string' && responseContentType.value.includes('text/html')
})

const formattedData = computed(() => {
  if (props.result?.data === undefined) return ''
  if (props.result.data instanceof Blob) {
    const mediaType = props.result.data.type || 'application/octet-stream'
    return `[binary response] ${mediaType}, ${props.result.data.size} bytes`
  }
  return typeof props.result.data === 'string'
    ? props.result.data
    : JSON.stringify(props.result.data, null, 2)
})

const statusClass = computed(() => {
  const status = props.result?.status ?? 0
  if (status < 300) return 'bg-emerald-50 text-emerald-700'
  if (status < 400) return 'bg-sky-50 text-sky-700'
  if (status < 500) return 'bg-amber-50 text-amber-700'
  return 'bg-rose-50 text-rose-700'
})
</script>

<template>
  <div v-if="result || error" class="space-y-4">
    <div class="flex flex-wrap items-center gap-2">
      <h4 class="text-[13px] font-semibold text-[var(--c-text)]">响应结果</h4>
      <span
        v-if="result"
        class="rounded-full px-2.5 py-1 font-mono text-[11px] font-semibold"
        :class="statusClass"
      >
        {{ result.status }} {{ result.statusText }}
      </span>
      <span v-if="result" class="text-[12px] text-[var(--c-muted)]">{{ result.duration }} ms</span>
    </div>

    <div
      v-if="error"
      class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-[12px] leading-5 text-rose-700"
    >
      {{ error }}
    </div>

    <section
      v-if="result?.headers && Object.keys(result.headers).length"
      class="rounded-2xl border border-[var(--c-border)] bg-white"
    >
      <header class="border-b border-[var(--c-border)] px-4 py-3">
        <h5 class="text-[12px] font-semibold text-[var(--c-text)]">响应头</h5>
      </header>
      <div class="divide-y divide-[var(--c-border)]">
        <div
          v-for="(value, key) in result.headers"
          :key="key"
          class="grid gap-1 px-4 py-3 md:grid-cols-[180px_1fr]"
        >
          <div class="font-mono text-[11px] text-[var(--c-muted)]">{{ key }}</div>
          <div class="break-all font-mono text-[12px] text-[var(--c-text)]">{{ value }}</div>
        </div>
      </div>
    </section>

    <section
      v-if="result?.data !== undefined"
      class="overflow-hidden rounded-2xl border border-[var(--c-border)] bg-white"
    >
      <header class="flex items-center justify-between gap-3 border-b border-[var(--c-border)] px-4 py-3">
        <h5 class="text-[12px] font-semibold text-[var(--c-text)]">响应体</h5>
        <button
          v-if="isHtmlResponse"
          class="rounded-full border border-[var(--c-border)] bg-white px-3 py-1.5 text-[11px] font-medium text-[var(--c-text)] transition-colors hover:bg-[var(--c-bg)]"
          @click="previewHtml = !previewHtml"
        >
          {{ previewHtml ? '查看源码' : '预览 HTML' }}
        </button>
      </header>

      <iframe
        v-if="isHtmlResponse && previewHtml"
        class="h-[420px] w-full bg-white"
        :srcdoc="String(result?.data ?? '')"
        sandbox=""
      />
      <pre v-else class="max-h-[420px] overflow-auto bg-[var(--c-bg)] px-4 py-4 text-[12px] leading-6 text-[var(--c-text)]">{{ formattedData }}</pre>
    </section>
  </div>
</template>
