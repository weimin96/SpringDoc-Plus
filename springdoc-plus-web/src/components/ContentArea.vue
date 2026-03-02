<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import TagSection from './swagger/TagSection.vue'
import SchemaView from './swagger/SchemaView.vue'
import { useOpenApi } from '@/composables/useOpenApi'
import type { MergedConfig } from '@/types'

const props = defineProps<{
  specUrl: string | null
  config: MergedConfig
  selectedOperation?: { method: string; path: string; summary?: string } | null
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

const filterKeyword = ref('')

const filteredGroups = computed(() => {
  const kw = filterKeyword.value.trim().toLowerCase()
  if (!kw) return tagGroups.value
  return tagGroups.value
    .map((g) => ({
      ...g,
      operations: g.operations.filter(
        (op) =>
          op.path.toLowerCase().includes(kw) ||
          op.operation.summary?.toLowerCase().includes(kw) ||
          op.operation.operationId?.toLowerCase().includes(kw),
      ),
    }))
    .filter((g) => g.operations.length > 0)
})

const schemas = computed(() => spec.value?.components?.schemas)
const totalOps = computed(() => tagGroups.value.reduce((n, g) => n + g.operations.length, 0))
const showSchemas = ref(false)

// 监听选中的接口，实现自动展开和滚动
watch(
  () => props.selectedOperation,
  (op) => {
    if (op) {
      // 通知 OperationPanel 展开并滚动
      window.dispatchEvent(new CustomEvent('scroll-to-operation', { detail: op }))
      emit('operationClicked')
    }
  },
)
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

        <!-- Info header -->
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

        <!-- Filter bar -->
        <div class="mb-4 flex items-center gap-2 border-b border-[var(--c-border)] pb-4">
          <div class="relative flex-1">
            <svg
              class="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[var(--c-muted)]"
              viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
            >
              <circle cx="11" cy="11" r="8" />
              <path d="m21 21-4.35-4.35" />
            </svg>
            <input
              v-model="filterKeyword"
              class="w-full rounded-lg border border-[var(--c-border)] bg-white py-2 pl-9 pr-3 text-[13px] outline-none transition-[border-color,box-shadow] placeholder:text-[#b0b7c3] focus:border-[var(--c-primary)] focus:shadow-[0_0_0_3px_rgb(37_99_235_/_0.1)]"
              placeholder="过滤接口路径 / 描述 / operationId…"
            />
          </div>
          <span v-if="filterKeyword" class="shrink-0 text-[12px] text-[var(--c-muted)]">
            {{ filteredGroups.reduce((n, g) => n + g.operations.length, 0) }} / {{ totalOps }}
          </span>
        </div>

        <!-- Tag sections -->
        <div v-if="filteredGroups.length">
          <TagSection
            v-for="group in filteredGroups"
            :key="group.name"
            :group="group"
            :schemas="schemas"
          />
        </div>
        <div v-else class="py-12 text-center text-[13px] text-[var(--c-muted)]">
          没有匹配的接口
        </div>

        <!-- Schemas section -->
        <div v-if="schemas && Object.keys(schemas).length" class="mt-6">
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
