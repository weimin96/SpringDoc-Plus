<script setup lang="ts">
import type { HttpMethod } from '@/types/openapi'

defineProps<{ method: HttpMethod }>()

const colorMap: Record<HttpMethod, string> = {
  get:     'bg-blue-600',
  post:    'bg-green-700',
  put:     'bg-amber-600',
  delete:  'bg-red-600',
  patch:   'bg-purple-600',
  head:    'bg-yellow-600',
  options: 'bg-zinc-500',
  trace:   'bg-slate-500',
}

const bgMap: Record<HttpMethod, string> = {
  get:     'bg-blue-50 border-blue-200',
  post:    'bg-green-50 border-green-200',
  put:     'bg-amber-50 border-amber-200',
  delete:  'bg-red-50 border-red-200',
  patch:   'bg-purple-50 border-purple-200',
  head:    'bg-yellow-50 border-yellow-200',
  options: 'bg-zinc-50 border-zinc-200',
  trace:   'bg-slate-50 border-slate-200',
}
</script>

<template>
  <div class="flex items-center gap-2">
    <span
      class="min-w-[65px] rounded-md px-2 py-[3px] text-center font-mono text-[11px] font-bold uppercase tracking-wide text-white"
      :class="colorMap[method]"
    >{{ method }}</span>
    <slot :bg="bgMap[method]" />
  </div>
</template>
