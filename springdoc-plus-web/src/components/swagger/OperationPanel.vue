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

const cardBg: Record<string, string> = {
  get:     'border-blue-200 bg-blue-50',
  post:    'border-green-200 bg-green-50',
  put:     'border-amber-200 bg-amber-50',
  delete:  'border-red-200 bg-red-50',
  patch:   'border-purple-200 bg-purple-50',
  head:    'border-yellow-200 bg-yellow-50',
  options: 'border-zinc-200 bg-zinc-50',
  trace:   'border-slate-200 bg-slate-50',
}

const bg = computed(() => cardBg[props.item.method] ?? 'border-[var(--c-border)] bg-white')

const op = computed(() => props.item.operation)
const parameters = computed(() => op.value.parameters ?? [])
const requestBodyContent = computed(() => {
  const rb = op.value.requestBody
  if (!rb?.content) return null
  return rb.content
})
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

// SimulatePanel 引用
const simulatePanelRef = ref<InstanceType<typeof SimulatePanel> | null>(null)
</script>

<template>
  <div :id="opId" class="mb-1.5 overflow-hidden rounded-[10px] border" :class="bg">
    <!-- Summary row -->
    <div class="flex items-center gap-3 border-none bg-transparent px-3.5 py-2.5">
      <MethodBadge :method="item.method" />
      <code class="flex-1 truncate font-mono text-[12.5px] font-medium text-[var(--c-text)]">
        {{ item.path }}
      </code>
      <span v-if="op.deprecated" class="rounded border border-amber-300 bg-amber-100 px-1.5 py-px text-[10px] font-medium text-amber-700">
        Deprecated
      </span>
      <span v-if="op.summary" class="hidden max-w-[240px] truncate text-[12.5px] text-[var(--c-muted)] md:block">
        {{ op.summary }}
      </span>
    </div>

    <!-- Detail -->
    <div class="border-t border-white/60 bg-white/80 px-4 py-4">
      <!-- Description -->
      <p v-if="op.description" class="mb-4 text-[13px] text-[var(--c-muted)]">{{ op.description }}</p>
      <p v-else-if="op.summary" class="mb-4 text-[13px] text-[var(--c-muted)]">{{ op.summary }}</p>

      <!-- Simulate Request Panel -->
      <SimulatePanel
        ref="simulatePanelRef"
        :item="item"
        :schemas="schemas"
        :context-path="contextPath"
        :auth-headers="authHeaders"
      />

      <!-- Parameters -->
      <template v-if="parameters.length">
        <div class="mb-4">
          <h4 class="mb-2 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]">参数</h4>
          <table class="w-full border-collapse text-xs">
            <thead>
              <tr class="border-b-2 border-[var(--c-border)]">
                <th class="py-1.5 pr-3 text-left font-semibold text-[var(--c-muted)]">名称</th>
                <th class="py-1.5 pr-3 text-left font-semibold text-[var(--c-muted)]">位置</th>
                <th class="py-1.5 pr-3 text-left font-semibold text-[var(--c-muted)]">类型</th>
                <th class="py-1.5 pr-3 text-left font-semibold text-[var(--c-muted)]">必填</th>
                <th class="py-1.5 text-left font-semibold text-[var(--c-muted)]">说明</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in parameters" :key="`${p.in}-${p.name}`" class="border-b border-[var(--c-border)] last:border-0">
                <td class="py-1.5 pr-3 font-mono font-medium text-[var(--c-text)]">{{ p.name }}</td>
                <td class="py-1.5 pr-3">
                  <span class="rounded px-1.5 py-px text-[10px] font-medium"
                    :class="{
                      'bg-blue-100 text-blue-700': p.in === 'path',
                      'bg-green-100 text-green-700': p.in === 'query',
                      'bg-purple-100 text-purple-700': p.in === 'header',
                      'bg-amber-100 text-amber-700': p.in === 'cookie',
                    }">
                    {{ p.in }}
                  </span>
                </td>
                <td class="py-1.5 pr-3 font-mono text-[var(--c-muted)]">{{ p.schema?.type ?? '?' }}</td>
                <td class="py-1.5 pr-3">
                  <span v-if="p.required" class="text-red-500">✓</span>
                  <span v-else class="text-[var(--c-muted)]">-</span>
                </td>
                <td class="py-1.5 text-[var(--c-muted)]">{{ p.description ?? '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>

      <!-- Request Body (docs) -->
      <template v-if="requestBodyContent">
        <div class="mb-4">
          <h4 class="mb-2 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]">
            请求体
            <span v-if="op.requestBody?.required" class="ml-1 text-red-500 normal-case">（必填）</span>
          </h4>
          <div v-for="(media, contentType) in requestBodyContent" :key="contentType" class="mb-2">
            <span class="mb-1.5 inline-block rounded bg-gray-100 px-1.5 py-px font-mono text-[10px] text-[var(--c-muted)]">{{ contentType }}</span>
            <div class="rounded-lg border border-[var(--c-border)] bg-gray-50 p-3">
              <SchemaView v-if="media.schema" :schema="media.schema" :schemas="schemas" />
            </div>
          </div>
        </div>
      </template>

      <!-- Responses -->
      <template v-if="responses.length">
        <div>
          <h4 class="mb-2 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]">响应</h4>
          <div class="space-y-2">
            <div v-for="[code, resp] in responses" :key="code" class="overflow-hidden rounded-lg border border-[var(--c-border)] bg-white">
              <div class="flex items-center gap-2 px-3 py-2">
                <span class="rounded px-2 py-0.5 font-mono text-[11px] font-bold" :class="statusColor(code)">
                  {{ code }}
                </span>
                <span class="text-[13px] text-[var(--c-muted)]">{{ resp.description ?? '' }}</span>
              </div>
              <div v-if="resp.content" class="border-t border-[var(--c-border)] px-3 pb-3 pt-2">
                <div v-for="(media, ct) in resp.content" :key="ct">
                  <span class="mb-1 inline-block rounded bg-gray-100 px-1.5 py-px font-mono text-[10px] text-[var(--c-muted)]">{{ ct }}</span>
                  <div class="rounded-lg border border-[var(--c-border)] bg-gray-50 p-2.5">
                    <SchemaView v-if="media.schema" :schema="media.schema" :schemas="schemas" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>