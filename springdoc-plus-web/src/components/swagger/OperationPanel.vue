<script setup lang="ts">
import { computed, ref } from 'vue'
import type { OperationItem, SchemaObject } from '@/types/openapi'
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
const op = computed(() => props.item.operation)
const parameters = computed(() => op.value.parameters ?? [])
const requestBodyContent = computed(() => op.value.requestBody?.content ?? null)
const responses = computed(() => Object.entries(op.value.responses ?? {}))

const methodColors: Record<string, { header: string }> = {
  get: { header: 'border-blue-200 bg-blue-50/60' },
  post: { header: 'border-green-200 bg-green-50/60' },
  put: { header: 'border-amber-200 bg-amber-50/60' },
  delete: { header: 'border-red-200 bg-red-50/60' },
  patch: { header: 'border-violet-200 bg-violet-50/60' },
  head: { header: 'border-yellow-200 bg-yellow-50/60' },
  options: { header: 'border-zinc-200 bg-zinc-50/60' },
  trace: { header: 'border-slate-200 bg-slate-50/60' },
}

const colors = computed(() => methodColors[props.item.method] ?? { header: 'border-[var(--c-border)] bg-white' })

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
const responseDocsCollapsed = ref(false)
</script>

<template>
  <div :id="opId" class="mb-2 overflow-hidden rounded-xl border border-[var(--c-border)] bg-white shadow-sm">
    <div class="flex items-center gap-3 border-b px-4 py-3" :class="colors.header">
      <MethodBadge :method="item.method" />
      <code class="flex-1 truncate font-mono text-[13px] font-semibold text-[var(--c-text)]">{{ item.path }}</code>
      <span
        v-if="op.deprecated"
        class="shrink-0 rounded-md border border-amber-300 bg-amber-100 px-2 py-0.5 text-[10px] font-semibold text-amber-700"
      >
        Deprecated
      </span>
      <span
        v-if="op.summary"
        class="hidden max-w-[260px] shrink-0 truncate text-[12.5px] text-[var(--c-muted)] md:block"
      >
        {{ op.summary }}
      </span>
    </div>

    <div class="divide-y divide-[var(--c-border)]">
      <div v-if="op.description || op.summary" class="px-5 py-4">
        <p class="text-[13px] leading-relaxed text-[var(--c-muted)]">
          {{ op.description || op.summary }}
        </p>
        <div v-if="op.tags?.length" class="mt-2 flex flex-wrap gap-1.5">
          <span
            v-for="tag in op.tags"
            :key="tag"
            class="rounded-full bg-gray-100 px-2.5 py-0.5 text-[11px] font-medium text-[var(--c-muted)]"
          >
            {{ tag }}
          </span>
        </div>
      </div>

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
                  <span
                    class="rounded-md px-2 py-0.5 text-[10px] font-semibold"
                    :class="{
                      'bg-blue-100 text-blue-700': p.in === 'path',
                      'bg-green-100 text-green-700': p.in === 'query',
                      'bg-purple-100 text-purple-700': p.in === 'header',
                      'bg-amber-100 text-amber-700': p.in === 'cookie',
                    }"
                  >
                    {{ p.in }}
                  </span>
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

      <div v-if="requestBodyContent" class="px-5 py-4">
        <h4 class="mb-3 text-[10px] font-bold uppercase tracking-widest text-[var(--c-muted)]">
          请求体 Schema
          <span v-if="op.requestBody?.required" class="ml-1 font-semibold normal-case text-red-500">(必填)</span>
        </h4>
        <div class="space-y-3">
          <div v-for="(media, contentType) in requestBodyContent" :key="contentType">
            <span class="mb-2 inline-block rounded-md bg-gray-100 px-2 py-0.5 font-mono text-[10px] text-[var(--c-muted)]">
              {{ contentType }}
            </span>
            <div class="rounded-xl border border-[var(--c-border)] bg-gray-50 p-3">
              <SchemaView v-if="media.schema" :schema="media.schema" :schemas="schemas" />
            </div>
          </div>
        </div>
      </div>

      <div class="px-5 py-4">
        <SimulatePanel
          ref="simulatePanelRef"
          :item="item"
          :schemas="schemas"
          :context-path="contextPath"
          :auth-headers="authHeaders"
        />
      </div>

      <div v-if="responses.length" class="px-5 py-4">
        <div class="mb-3 flex items-center justify-between gap-3">
          <h4 class="text-[10px] font-bold uppercase tracking-widest text-[var(--c-muted)]">响应</h4>
          <button
            class="inline-flex h-9 w-9 items-center justify-center rounded-full border border-[var(--c-border)] bg-white text-[var(--c-muted)] transition-colors hover:bg-[var(--c-bg)] hover:text-[var(--c-text)]"
            :title="responseDocsCollapsed ? '展开响应文档' : '收起响应文档'"
            @click="responseDocsCollapsed = !responseDocsCollapsed"
          >
            <svg class="h-4 w-4 transition-transform" :class="responseDocsCollapsed ? '-rotate-90' : 'rotate-0'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="m6 9 6 6 6-6" />
            </svg>
          </button>
        </div>

        <div v-if="!responseDocsCollapsed" class="grid gap-3">
          <div
            v-for="[code, resp] in responses"
            :key="code"
            class="overflow-hidden rounded-2xl border border-[var(--c-border)] bg-[linear-gradient(180deg,#ffffff_0%,#fbfcfe_100%)]"
          >
            <div class="flex flex-wrap items-center gap-2 border-b border-[var(--c-border)] px-4 py-3">
              <span class="rounded-full px-2.5 py-1 font-mono text-[11px] font-bold" :class="statusColor(code)">
                {{ code }}
              </span>
              <span class="text-[13px] font-medium text-[var(--c-text)]">{{ resp.description ?? 'No description' }}</span>
            </div>

            <div v-if="resp.content" class="space-y-3 px-4 py-3">
              <div
                v-for="(media, ct) in resp.content"
                :key="ct"
                class="rounded-xl border border-[var(--c-border)] bg-white p-3"
              >
                <span class="mb-2 inline-flex rounded-full bg-[var(--c-bg)] px-2.5 py-1 font-mono text-[10px] text-[var(--c-muted)]">
                  {{ ct }}
                </span>
                <div class="rounded-xl border border-[var(--c-border)] bg-[var(--c-bg)] p-3">
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
