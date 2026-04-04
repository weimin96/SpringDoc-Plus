<script setup lang="ts">
import { computed, ref } from 'vue'
import type { OperationItem } from '@/types/openapi'
import type { SchemaObject } from '@/types/openapi'
import type { AuthHeader } from '@/types'
import MethodBadge from './MethodBadge.vue'
import SchemaView from './SchemaView.vue'
import SimulatePanel from './SimulatePanel.vue'

const props = defineProps<{
  item: OperationItem
  schemas?: Record<string, SchemaObject>
  contextPath?: string
  authHeaders?: AuthHeader[]
}>()

const opId = computed(() => `op-${props.item.method}-${props.item.path}`)

const methodColors: Record<string, { header: string; badge: string }> = {
  get:     { header: 'border-blue-200 bg-blue-50/60',    badge: 'bg-blue-100 text-blue-800' },
  post:    { header: 'border-green-200 bg-green-50/60',  badge: 'bg-green-100 text-green-800' },
  put:     { header: 'border-amber-200 bg-amber-50/60',  badge: 'bg-amber-100 text-amber-800' },
  delete:  { header: 'border-red-200 bg-red-50/60',      badge: 'bg-red-100 text-red-800' },
  patch:   { header: 'border-violet-200 bg-violet-50/60',badge: 'bg-violet-100 text-violet-800' },
  head:    { header: 'border-yellow-200 bg-yellow-50/60',badge: 'bg-yellow-100 text-yellow-800' },
  options: { header: 'border-zinc-200 bg-zinc-50/60',    badge: 'bg-zinc-100 text-zinc-700' },
  trace:   { header: 'border-slate-200 bg-slate-50/60',  badge: 'bg-slate-100 text-slate-700' },
}

const colors = computed(() => methodColors[props.item.method] ?? { header: 'border-[var(--c-border)] bg-white', badge: 'bg-gray-100 text-gray-700' })

const op = computed(() => props.item.operation)
const parameters = computed(() => op.value.parameters ?? [])
const requestBodyContent = computed(() => op.value.requestBody?.content ?? null)
const responses = computed(() => Object.entries(op.value.responses ?? {}))

const statusColors: Record<string, string> = {
  '2': 'bg-green-100 text-green-800',
  '3': 'bg-blue-100 text-blue-800',
  '4': 'bg-amber-100 text-amber-800',
  '5': 'bg-red-100 text-red-800',
}
function statusColor(code: string): string {
  return statusColors[code[0]] ?? 'bg-gray-100 text-gray-700'
}

const simulatePanelRef = ref<InstanceType<typeof SimulatePanel> | null>(null)
</script>

<template>
  <div :id="opId" class="mb-2 overflow-hidden rounded-xl border border-[var(--c-border)] bg-white shadow-sm">

    <!-- ── Header bar ───────────────────────────────────── -->
    <div class="flex items-center gap-3 border-b px-4 py-3" :class="colors.header">
      <MethodBadge :method="item.method" />
      <code class="flex-1 truncate font-mono text-[13px] font-semibold text-[var(--c-text)]">{{ item.path }}</code>
      <span v-if="op.deprecated" class="shrink-0 rounded-md border border-amber-300 bg-amber-100 px-2 py-0.5 text-[10px] font-semibold text-amber-700">
        Deprecated
      </span>
      <span v-if="op.summary" class="hidden max-w-[260px] shrink-0 truncate text-[12.5px] text-[var(--c-muted)] md:block">
        {{ op.summary }}
      </span>
    </div>

    <!-- ── Body ──────────────────────────────────────────── -->
    <div class="divide-y divide-[var(--c-border)]">

      <!-- Description -->
      <div v-if="op.description || op.summary" class="px-5 py-4">
        <p class="text-[13px] leading-relaxed text-[var(--c-muted)]">
          {{ op.description || op.summary }}
        </p>
        <div v-if="op.tags?.length" class="mt-2 flex flex-wrap gap-1.5">
          <span
            v-for="tag in op.tags" :key="tag"
            class="rounded-full bg-gray-100 px-2.5 py-0.5 text-[11px] font-medium text-[var(--c-muted)]"
          >{{ tag }}</span>
        </div>
      </div>

      <!-- ── Parameters ── -->
      <div v-if="parameters.length" class="px-5 py-4">
        <h4 class="mb-3 text-[10px] font-bold uppercase tracking-widest text-[var(--c-muted)]">参数</h4>
        <div class="overflow-hidden rounded-xl border border-[var(--c-border)]">
          <table class="w-full border-collapse text-[12px]">
            <thead>
              <tr class="border-b border-[var(--c-border)] bg-gray-50">
                <th class="px-3 py-2 text-left text-[11px] font-semibold text-[var(--c-muted)]">名称</th>
                <th class="px-3 py-2 text-left text-[11px] font-semibold text-[var(--c-muted)]">位置</th>
                <th class="px-3 py-2 text-left text-[11px] font-semibold text-[var(--c-muted)]">类型</th>
                <th class="px-3 py-2 text-left text-[11px] font-semibold text-[var(--c-muted)]">必填</th>
                <th class="px-3 py-2 text-left text-[11px] font-semibold text-[var(--c-muted)]">说明</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-[var(--c-border)]">
              <tr v-for="p in parameters" :key="`${p.in}-${p.name}`" class="bg-white">
                <td class="px-3 py-2 font-mono text-[12px] font-semibold text-[var(--c-text)]">{{ p.name }}</td>
                <td class="px-3 py-2">
                  <span class="rounded-md px-2 py-0.5 text-[10px] font-semibold"
                    :class="{
                      'bg-blue-100 text-blue-700': p.in === 'path',
                      'bg-green-100 text-green-700': p.in === 'query',
                      'bg-purple-100 text-purple-700': p.in === 'header',
                      'bg-amber-100 text-amber-700': p.in === 'cookie',
                    }">{{ p.in }}</span>
                </td>
                <td class="px-3 py-2 font-mono text-[11px] text-[var(--c-muted)]">{{ p.schema?.type ?? '?' }}</td>
                <td class="px-3 py-2">
                  <span v-if="p.required" class="text-[11px] font-semibold text-red-500">必填</span>
                  <span v-else class="text-[11px] text-[var(--c-muted)]">-</span>
                </td>
                <td class="px-3 py-2 text-[11px] text-[var(--c-muted)]">{{ p.description ?? '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- ── Request Body (Schema docs) ── -->
      <div v-if="requestBodyContent" class="px-5 py-4">
        <h4 class="mb-3 text-[10px] font-bold uppercase tracking-widest text-[var(--c-muted)]">
          请求体 Schema
          <span v-if="op.requestBody?.required" class="ml-1 font-semibold normal-case text-red-500">（必填）</span>
        </h4>
        <div class="space-y-3">
          <div v-for="(media, contentType) in requestBodyContent" :key="contentType">
            <span class="mb-2 inline-block rounded-md bg-gray-100 px-2 py-0.5 font-mono text-[10px] text-[var(--c-muted)]">{{ contentType }}</span>
            <div class="rounded-xl border border-[var(--c-border)] bg-gray-50 p-3">
              <SchemaView v-if="media.schema" :schema="media.schema" :schemas="schemas" />
            </div>
          </div>
        </div>
      </div>

      <!-- ── Debug Panel ── -->
      <div class="px-5 py-4">
        <SimulatePanel
          ref="simulatePanelRef"
          :item="item"
          :schemas="schemas"
          :context-path="contextPath"
          :auth-headers="authHeaders"
        />
      </div>

      <!-- ── Responses ── -->
      <div v-if="responses.length" class="px-5 py-4">
        <h4 class="mb-3 text-[10px] font-bold uppercase tracking-widest text-[var(--c-muted)]">响应</h4>
        <div class="space-y-2">
          <div v-for="[code, resp] in responses" :key="code" class="overflow-hidden rounded-xl border border-[var(--c-border)] bg-white">
            <div class="flex items-center gap-2.5 px-3 py-2.5">
              <span class="rounded-md px-2.5 py-0.5 font-mono text-[11px] font-bold" :class="statusColor(code)">
                {{ code }}
              </span>
              <span class="text-[13px] text-[var(--c-muted)]">{{ resp.description ?? '' }}</span>
            </div>
            <div v-if="resp.content" class="border-t border-[var(--c-border)] px-3 pb-3 pt-3">
              <div v-for="(media, ct) in resp.content" :key="ct" class="space-y-1.5">
                <span class="inline-block rounded-md bg-gray-100 px-2 py-0.5 font-mono text-[10px] text-[var(--c-muted)]">{{ ct }}</span>
                <div class="rounded-xl border border-[var(--c-border)] bg-gray-50 p-3">
                  <SchemaView v-if="media.schema" :schema="media.schema" :schemas="schemas" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>