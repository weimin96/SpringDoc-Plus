<script setup lang="ts">
/**
 * ContentArea.vue — 纯状态协调层
 *
 * 重构前：387 行，overview 渲染逻辑与 operation 路由混杂于此。
 * 重构后：此文件只做三件事：
 *   1. 处理 loading / error / 空状态
 *   2. 渲染页面顶部 API 信息栏（title、version、servers）
 *   3. 按 viewMode 分发到 <OverviewPanel> 或 <OperationPanel>
 *
 * overview 细节全部迁移至 OverviewPanel.vue，
 * operation 细节保留在 OperationPanel.vue。
 */
import { computed } from 'vue'
import OverviewPanel from './OverviewPanel.vue'
import OperationPanel from './swagger/OperationPanel.vue'
import type { MergedConfig } from '@/types'
import type { OpenApiSpec, TagGroup } from '@/types/openapi'

type ViewMode = 'overview' | 'operation'

const props = defineProps<{
  specUrl: string | null
  spec: OpenApiSpec | null
  loading: boolean
  error: string | null
  tagGroups: TagGroup[]
  config: MergedConfig
  contextPath?: string
  selectedOperation?: { method: string; path: string; summary?: string } | null
  viewMode?: ViewMode
}>()

const emit = defineEmits<{
  operationClicked: []
  selectOperation: [operation: { method: string; path: string; summary?: string }]
}>()

// ── Computed helpers ─────────────────────────────────────────────────────────
const schemas = computed(() => props.spec?.components?.schemas)
const totalOps = computed(() => props.tagGroups.reduce((n, g) => n + g.operations.length, 0))
const totalTags = computed(() => props.tagGroups.length)
const totalSchemas = computed(() => Object.keys(schemas.value ?? {}).length)
const totalServers = computed(() => props.spec?.servers?.length ?? 0)

/** Up to 8 operation cards shown in the quick-jump section of OverviewPanel */
const operationsPreview = computed(() =>
  props.tagGroups
    .flatMap(group =>
      group.operations.slice(0, 2).map(op => ({
        tagName: group.name,
        method: op.method,
        path: op.path,
        summary: op.operation.summary || op.path,
        description: op.operation.description,
        deprecated: op.operation.deprecated,
      })),
    )
    .slice(0, 8),
)

/** Resolve the full OperationItem for the selected operation */
const selectedOpData = computed(() => {
  if (!props.selectedOperation || !props.tagGroups.length) return null
  for (const group of props.tagGroups) {
    const found = group.operations.find(
      op =>
        op.method === props.selectedOperation?.method &&
        op.path === props.selectedOperation?.path,
    )
    if (found) return found
  }
  return null
})

function retry() {
  window.location.reload()
}
</script>

<template>
  <main class="flex flex-1 flex-col overflow-hidden bg-[var(--c-bg)]">

    <!-- Loading -->
    <div
      v-if="loading"
      class="flex h-[60vh] flex-col items-center justify-center gap-3.5 text-[var(--c-muted)]"
    >
      <div
        class="h-8 w-8 rounded-full border-[3px] border-[var(--c-border)] border-t-[var(--c-primary)]"
        style="animation: spin 0.8s linear infinite"
      />
      <h2 class="text-[15px] font-semibold text-[var(--c-text)]">正在加载文档</h2>
      <p class="text-[13px]">正在获取 API 文档数据</p>
    </div>

    <!-- Error -->
    <div
      v-else-if="error"
      class="flex h-[60vh] flex-col items-center justify-center gap-3.5 text-center text-[var(--c-muted)]"
    >
      <svg class="h-10 w-10" viewBox="0 0 24 24" fill="none" stroke="#f87171" stroke-width="1.5" stroke-linecap="round">
        <circle cx="12" cy="12" r="10" />
        <path d="M12 8v4M12 16h.01" />
      </svg>
      <h2 class="text-[15px] font-semibold text-[var(--c-text)]">加载失败</h2>
      <p class="max-w-[360px] text-[13px]">{{ error }}</p>
      <button
        class="mt-2 cursor-pointer rounded-lg border border-[var(--c-border)] bg-white px-4 py-2 text-[13px] text-[var(--c-text)] transition-colors hover:bg-[var(--c-bg)]"
        @click="retry"
      >
        重试
      </button>
    </div>

    <!-- No spec selected -->
    <div
      v-else-if="!specUrl"
      class="flex h-[60vh] flex-col items-center justify-center gap-3.5 text-center text-[var(--c-muted)]"
    >
      <svg class="h-10 w-10" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5" stroke-linecap="round">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
        <polyline points="14 2 14 8 20 8" />
      </svg>
      <h2 class="text-[15px] font-semibold text-[var(--c-text)]">请选择文档组</h2>
      <p class="max-w-[340px] text-[13px]">从左侧导航选择一个服务，即可开始浏览和分享文档链接。</p>
    </div>

    <!-- Main content -->
    <div v-else-if="spec" class="flex-1 overflow-y-auto">
      <div class="mx-auto max-w-[1120px] px-6 pb-12">

        <!-- API title bar -->
        <div class="py-5">
          <div class="flex flex-wrap items-start justify-between gap-4">
            <div>
              <h1 class="text-xl font-bold text-[var(--c-text)]">
                {{ spec.info?.title ?? 'API 文档' }}
              </h1>
              <div class="mt-1.5 flex flex-wrap items-center gap-2">
                <span class="rounded-full bg-blue-100 px-2 py-0.5 font-mono text-[11px] text-blue-700">
                  {{ spec.info?.version }}
                </span>
                <span
                  v-if="spec.openapi"
                  class="rounded-full bg-gray-100 px-2 py-0.5 font-mono text-[11px] text-gray-600"
                >
                  OpenAPI {{ spec.openapi }}
                </span>
                <span class="text-[12px] text-[var(--c-muted)]">{{ totalOps }} 个接口</span>
              </div>
              <p
                v-if="spec.info?.description"
                class="mt-2 max-w-[640px] text-[13px] leading-relaxed text-[var(--c-muted)]"
              >
                {{ spec.info.description }}
              </p>
            </div>

            <div v-if="spec.servers?.length" class="shrink-0 text-right">
              <p class="mb-1 text-[11px] font-semibold uppercase tracking-wider text-[var(--c-muted)]">
                服务地址
              </p>
              <div v-for="srv in spec.servers" :key="srv.url">
                <a
                  :href="srv.url"
                  target="_blank"
                  class="font-mono text-[12px] text-[var(--c-primary)] hover:underline"
                >{{ srv.url }}</a>
                <p v-if="srv.description" class="text-[11px] text-[var(--c-muted)]">
                  {{ srv.description }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- Overview -->
        <OverviewPanel
          v-if="viewMode === 'overview'"
          :spec="spec"
          :tag-groups="tagGroups"
          :schemas="schemas"
          :context-path="contextPath"
          :total-ops="totalOps"
          :total-tags="totalTags"
          :total-schemas="totalSchemas"
          :total-servers="totalServers"
          :operations-preview="operationsPreview"
          @select-operation="emit('selectOperation', $event)"
        />

        <!-- Operation detail -->
        <template v-else-if="viewMode === 'operation' && selectedOpData">
          <div class="mb-4 border-b border-[var(--c-border)] pb-3">
            <button
              class="flex cursor-pointer items-center gap-1.5 text-[13px] text-[var(--c-primary)] hover:underline"
              @click="emit('operationClicked')"
            >
              <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="m15 18-6-6 6-6" />
              </svg>
              返回概览
            </button>
          </div>

          <OperationPanel
            :key="`${selectedOpData.method}-${selectedOpData.path}`"
            :item="selectedOpData"
            :schemas="schemas"
            :context-path="contextPath"
            :auth-headers="config.authHeaders"
          />
        </template>

      </div>
    </div>
  </main>
</template>
