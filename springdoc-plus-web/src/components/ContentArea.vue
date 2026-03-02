<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import SchemaView from './swagger/SchemaView.vue'
import OperationPanel from './swagger/OperationPanel.vue'
import { useOpenApi } from '@/composables/useOpenApi'
import type { MergedConfig } from '@/types'

type ViewMode = 'overview' | 'operation'

const props = defineProps<{
  specUrl: string | null
  config: MergedConfig
  selectedOperation?: { method: string; path: string; summary?: string } | null
  viewMode?: ViewMode
}>()

const emit = defineEmits<{
  operationClicked: []
}>()

const { spec, loading, error, load, tagGroups } = useOpenApi(props.config)

watch(
  () => props.specUrl,
  (url) => { if (url) load(url) },
  { immediate: true },
)

const schemas = computed(() => spec.value?.components?.schemas)
const totalOps = computed(() => tagGroups.value.reduce((n, g) => n + g.operations.length, 0))
const showSchemas = ref(false)

// 当前选中的接口数据
const selectedOpData = computed(() => {
  if (!props.selectedOperation || !tagGroups.value.length) return null

  for (const group of tagGroups.value) {
    const found = group.operations.find(
      (op) => op.method === props.selectedOperation?.method && op.path === props.selectedOperation?.path
    )
    if (found) return found
  }
  return null
})
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
      <h2 class="text-[15px] font-semibold text-[var(--c-text)]">正在加载…</h2>
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
        @click="specUrl && load(specUrl)"
      >
        重试
      </button>
    </div>

    <!-- No selection -->
    <div
      v-else-if="!specUrl"
      class="flex h-[60vh] flex-col items-center justify-center gap-3.5 text-center text-[var(--c-muted)]"
    >
      <svg class="h-10 w-10" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5" stroke-linecap="round">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
        <polyline points="14 2 14 8 20 8" />
      </svg>
      <h2 class="text-[15px] font-semibold text-[var(--c-text)]">请选择文档组</h2>
      <p class="max-w-[340px] text-[13px]">从左侧侧边栏选择一个文档组开始浏览</p>
    </div>

    <!-- Content -->
    <div v-else-if="spec" class="flex-1 overflow-y-auto">
      <div class="mx-auto max-w-[1120px] px-6 pb-12">

        <!-- Info header (概览信息，始终显示) -->
        <div class="py-5">
          <div class="flex flex-wrap items-start justify-between gap-4">
            <div>
              <h1 class="text-xl font-bold text-[var(--c-text)]">
                {{ spec.info?.title ?? 'API 文档' }}
              </h1>
              <div class="mt-1.5 flex flex-wrap items-center gap-2">
                <span class="rounded-full bg-blue-100 px-2 py-0.5 font-mono text-[11px] text-blue-700">
                  v{{ spec.info?.version }}
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

            <!-- Servers -->
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

        <!-- Tag groups overview (概览模式) -->
        <template v-if="viewMode === 'overview'">
          <!-- Tag groups summary -->
          <div class="mb-4 border-b border-[var(--c-border)] pb-4">
            <h2 class="mb-3 text-[14px] font-semibold text-[var(--c-text)]">接口分组概览</h2>
            <div class="grid gap-2">
              <div
                v-for="group in tagGroups"
                :key="group.name"
                class="flex items-center justify-between rounded-lg border border-[var(--c-border)] bg-white px-4 py-3"
              >
                <div>
                  <span class="text-[13px] font-medium text-[var(--c-text)]">{{ group.name }}</span>
                  <p v-if="group.description" class="mt-0.5 text-[12px] text-[var(--c-muted)]">
                    {{ group.description }}
                  </p>
                </div>
                <span class="rounded-full bg-gray-100 px-2.5 py-1 text-[12px] font-medium text-[var(--c-muted)]">
                  {{ group.operations.length }} 个接口
                </span>
              </div>
            </div>
          </div>
        </template>

        <!-- Single operation detail (接口详情模式) -->
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
            :item="selectedOpData"
            :schemas="schemas"
          />
        </template>

        <!-- Schemas section (仅概览模式显示) -->
        <div v-if="viewMode === 'overview' && schemas && Object.keys(schemas).length" class="mt-6">
          <button
            class="flex w-full cursor-pointer items-center gap-2 rounded-t-[10px] border border-[var(--c-border)] bg-white px-4 py-3 text-left text-[13px] font-semibold text-[var(--c-text)] transition-colors hover:bg-[var(--c-primary-light)]"
            :class="showSchemas ? 'rounded-t-[10px]' : 'rounded-[10px]'"
            @click="showSchemas = !showSchemas"
          >
            <svg
              class="h-4 w-4 shrink-0 text-[var(--c-muted)] transition-transform duration-200"
              :class="showSchemas ? 'rotate-0' : '-rotate-90'"
              viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
            >
              <path d="m6 9 6 6 6-6" />
            </svg>
            <svg class="h-4 w-4 shrink-0 text-[var(--c-muted)]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
              <line x1="3" y1="9" x2="21" y2="9" />
              <line x1="9" y1="21" x2="9" y2="3" />
            </svg>
            数据模型 (Schemas)
            <span class="ml-auto rounded-full bg-gray-100 px-2 py-px text-[11px] text-[var(--c-muted)]">
              {{ Object.keys(schemas).length }}
            </span>
          </button>

          <div
            v-if="showSchemas"
            class="divide-y divide-[var(--c-border)] rounded-b-[10px] border border-t-0 border-[var(--c-border)] bg-white"
            style="animation: slide-down 0.15s ease"
          >
            <div v-for="(schema, name) in schemas" :key="name" class="p-4">
              <h4 class="mb-2 font-mono text-[13px] font-semibold text-[var(--c-primary)]">
                {{ name }}
              </h4>
              <div class="rounded-lg border border-[var(--c-border)] bg-gray-50 p-3">
                <SchemaView :schema="schema" :schemas="schemas" />
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>

  </main>
</template>
