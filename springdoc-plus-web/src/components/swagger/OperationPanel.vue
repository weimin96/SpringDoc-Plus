<script setup lang="ts">
import { computed, ref } from 'vue'
import type { OperationItem, SchemaObject } from '@/types/openapi'
import type { AuthHeader } from '@/types'
import MethodBadge from './MethodBadge.vue'
import SchemaView from './SchemaView.vue'
import SimulatePanel from './SimulatePanel.vue'
import { getSchemaPrimaryType } from '@/utils/schema'

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

const methodHeaderColors: Record<string, string> = {
  get: 'border-blue-200 bg-gradient-to-r from-blue-50/80 to-sky-50/40',
  post: 'border-green-200 bg-gradient-to-r from-green-50/80 to-emerald-50/40',
  put: 'border-amber-200 bg-gradient-to-r from-amber-50/80 to-yellow-50/40',
  delete: 'border-red-200 bg-gradient-to-r from-red-50/80 to-rose-50/40',
  patch: 'border-violet-200 bg-gradient-to-r from-violet-50/80 to-purple-50/40',
  head: 'border-yellow-200 bg-gradient-to-r from-yellow-50/80 to-amber-50/40',
  options: 'border-zinc-200 bg-gradient-to-r from-zinc-50/80 to-slate-50/40',
  trace: 'border-slate-200 bg-gradient-to-r from-slate-50/80 to-gray-50/40',
}

const headerColor = computed(
  () => methodHeaderColors[props.item.method] ?? 'border-[var(--c-border)] bg-white',
)

const statusColors: Record<string, string> = {
  '2': 'bg-emerald-100 text-emerald-800 border-emerald-200',
  '3': 'bg-sky-100 text-sky-800 border-sky-200',
  '4': 'bg-amber-100 text-amber-800 border-amber-200',
  '5': 'bg-rose-100 text-rose-800 border-rose-200',
}

function statusColor(code: string): string {
  return statusColors[code[0]] ?? 'bg-gray-100 text-gray-700 border-gray-200'
}

function schemaTypeText(schema?: SchemaObject): string {
  return getSchemaPrimaryType(schema) ?? '?'
}

const responseDocsCollapsed = ref(false)
const simulatePanelRef = ref<InstanceType<typeof SimulatePanel> | null>(null)
</script>

<template>
  <div
    :id="opId"
    class="mb-4 overflow-hidden rounded-2xl border border-[var(--c-border)] bg-white shadow-[0_2px_12px_rgb(0_0_0/6%)]"
  >
    <!-- Header -->
    <div class="flex flex-wrap items-center gap-3 border-b px-5 py-3.5" :class="headerColor">
      <MethodBadge :method="item.method" />
      <code class="flex-1 truncate font-mono text-[13px] font-semibold text-[var(--c-text)]">
        {{ item.path }}
      </code>
      <span
        v-if="op.deprecated"
        class="shrink-0 rounded-md border border-amber-300 bg-amber-100 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wide text-amber-700"
      >
        Deprecated
      </span>
      <span
        v-if="op.summary"
        class="hidden max-w-[260px] shrink-0 truncate rounded-full bg-white/70 px-3 py-1 text-[12px] text-[var(--c-muted)] md:block"
      >
        {{ op.summary }}
      </span>
    </div>

    <!-- Two-column layout -->
    <div class="flex min-h-0 flex-col xl:flex-row">
      <!-- Left: API Documentation -->
      <div class="min-w-0 flex-1 divide-y divide-[var(--c-border)]">
        <!-- Description -->
        <div v-if="op.description || op.summary" class="px-5 py-4">
          <p class="text-[13px] leading-relaxed text-[var(--c-muted)]">
            {{ op.description || op.summary }}
          </p>
          <div v-if="op.tags?.length" class="mt-3 flex flex-wrap gap-1.5">
            <span
              v-for="tag in op.tags"
              :key="tag"
              class="rounded-full border border-[var(--c-border)] bg-[var(--c-bg)] px-2.5 py-0.5 text-[11px] font-medium text-[var(--c-muted)]"
            >
              {{ tag }}
            </span>
          </div>
        </div>

        <!-- Parameters -->
        <div v-if="parameters.length" class="px-5 py-4">
          <h4
            class="mb-3 flex items-center gap-2 text-[11px] font-bold uppercase tracking-widest text-[var(--c-muted)]"
          >
            <svg
              class="h-3.5 w-3.5"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2.5"
              stroke-linecap="round"
            >
              <path d="M4 6h16M4 12h16M4 18h7" />
            </svg>
            请求参数
          </h4>
          <div class="overflow-hidden rounded-xl border border-[var(--c-border)]">
            <table class="w-full border-collapse text-[12px]">
              <thead>
                <tr class="border-b border-[var(--c-border)] bg-[var(--c-bg)]">
                  <th
                    class="px-3 py-2.5 text-left text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]"
                  >
                    名称
                  </th>
                  <th
                    class="px-3 py-2.5 text-left text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]"
                  >
                    位置
                  </th>
                  <th
                    class="px-3 py-2.5 text-left text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]"
                  >
                    类型
                  </th>
                  <th
                    class="px-3 py-2.5 text-left text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]"
                  >
                    必填
                  </th>
                  <th
                    class="px-3 py-2.5 text-left text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]"
                  >
                    说明
                  </th>
                </tr>
              </thead>
              <tbody class="divide-y divide-[var(--c-border)]">
                <tr
                  v-for="p in parameters"
                  :key="`${p.in}-${p.name}`"
                  class="bg-white transition-colors hover:bg-[var(--c-bg)]"
                >
                  <td class="px-3 py-2.5 font-mono text-[12px] font-semibold text-[var(--c-text)]">
                    {{ p.name }}
                  </td>
                  <td class="px-3 py-2.5">
                    <span
                      class="rounded-md border px-2 py-0.5 text-[10px] font-semibold"
                      :class="{
                        'border-blue-200 bg-blue-50 text-blue-700': p.in === 'path',
                        'border-green-200 bg-green-50 text-green-700': p.in === 'query',
                        'border-purple-200 bg-purple-50 text-purple-700': p.in === 'header',
                        'border-amber-200 bg-amber-50 text-amber-700': p.in === 'cookie',
                      }"
                    >
                      {{ p.in }}
                    </span>
                  </td>
                  <td class="px-3 py-2.5 font-mono text-[11px] text-[var(--c-muted)]">
                    {{ schemaTypeText(p.schema) }}
                  </td>
                  <td class="px-3 py-2.5">
                    <span
                      v-if="p.required"
                      class="rounded-full bg-rose-50 px-2 py-0.5 text-[10px] font-bold text-rose-600"
                    >
                      必填
                    </span>
                    <span v-else class="text-[11px] text-[var(--c-border)]">—</span>
                  </td>
                  <td class="px-3 py-2.5 text-[11px] text-[var(--c-muted)]">
                    {{ p.description ?? '—' }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Request Body Schema -->
        <div v-if="requestBodyContent" class="px-5 py-4">
          <h4
            class="mb-3 flex items-center gap-2 text-[11px] font-bold uppercase tracking-widest text-[var(--c-muted)]"
          >
            <svg
              class="h-3.5 w-3.5"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2.5"
              stroke-linecap="round"
            >
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
            </svg>
            请求体 Schema
            <span
              v-if="op.requestBody?.required"
              class="ml-0.5 rounded-full bg-rose-50 px-2 py-0.5 text-[9px] font-bold normal-case text-rose-600"
            >
              必填
            </span>
          </h4>
          <div class="space-y-3">
            <div v-for="(media, contentType) in requestBodyContent" :key="contentType">
              <span
                class="mb-2 inline-block rounded-md border border-[var(--c-border)] bg-[var(--c-bg)] px-2 py-0.5 font-mono text-[10px] text-[var(--c-muted)]"
              >
                {{ contentType }}
              </span>
              <div class="rounded-xl border border-[var(--c-border)] bg-gray-50/80 p-3">
                <SchemaView v-if="media.schema" :schema="media.schema" :schemas="schemas" />
              </div>
            </div>
          </div>
        </div>

        <!-- Responses -->
        <div v-if="responses.length" class="px-5 py-4">
          <div class="mb-3 flex items-center gap-3">
            <h4
              class="flex items-center gap-2 text-[11px] font-bold uppercase tracking-widest text-[var(--c-muted)]"
            >
              <svg
                class="h-3.5 w-3.5"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2.5"
                stroke-linecap="round"
              >
                <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
              </svg>
              响应文档
            </h4>
            <button
              class="ml-auto inline-flex h-7 w-7 items-center justify-center rounded-full border border-[var(--c-border)] bg-white text-[var(--c-muted)] transition-colors hover:bg-[var(--c-bg)] hover:text-[var(--c-text)]"
              :title="responseDocsCollapsed ? '展开响应文档' : '收起响应文档'"
              @click="responseDocsCollapsed = !responseDocsCollapsed"
            >
              <svg
                class="h-3.5 w-3.5 transition-transform"
                :class="responseDocsCollapsed ? '-rotate-90' : 'rotate-0'"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2.5"
              >
                <path d="m6 9 6 6 6-6" />
              </svg>
            </button>
          </div>

          <div v-if="!responseDocsCollapsed" class="grid gap-2.5">
            <div
              v-for="[code, resp] in responses"
              :key="code"
              class="overflow-hidden rounded-xl border border-[var(--c-border)] bg-white"
            >
              <div
                class="flex flex-wrap items-center gap-2 border-b border-[var(--c-border)] bg-[var(--c-bg)] px-4 py-2.5"
              >
                <span
                  class="rounded-full border px-2.5 py-0.5 font-mono text-[11px] font-bold"
                  :class="statusColor(code)"
                >
                  {{ code }}
                </span>
                <span class="text-[13px] font-medium text-[var(--c-text)]">
                  {{ resp.description ?? 'No description' }}
                </span>
              </div>
              <div v-if="resp.content" class="space-y-3 px-4 py-3">
                <div
                  v-for="(media, ct) in resp.content"
                  :key="ct"
                  class="rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] p-3"
                >
                  <span
                    class="mb-2 inline-flex rounded-full border border-[var(--c-border)] bg-white px-2.5 py-0.5 font-mono text-[10px] text-[var(--c-muted)]"
                  >
                    {{ ct }}
                  </span>
                  <div class="rounded-lg border border-[var(--c-border)] bg-white p-3">
                    <SchemaView v-if="media.schema" :schema="media.schema" :schemas="schemas" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right: Simulate Panel (sticky) -->
      <div
        class="shrink-0 border-t border-[var(--c-border)] bg-[var(--c-bg)/30] xl:w-[440px] xl:border-l xl:border-t-0"
      >
        <div class="xl:sticky xl:top-0 xl:max-h-[calc(100vh-120px)] xl:overflow-y-auto">
          <SimulatePanel
            ref="simulatePanelRef"
            :item="item"
            :schemas="schemas"
            :context-path="contextPath"
            :auth-headers="authHeaders"
          />
        </div>
      </div>
    </div>
  </div>
</template>
