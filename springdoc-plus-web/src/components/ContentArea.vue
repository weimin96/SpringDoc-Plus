<script setup lang="ts">
import { computed, ref } from 'vue'
import SchemaView from './swagger/SchemaView.vue'
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

const schemas = computed(() => props.spec?.components?.schemas)
const totalOps = computed(() => props.tagGroups.reduce((count, group) => count + group.operations.length, 0))
const totalTags = computed(() => props.tagGroups.length)
const totalSchemas = computed(() => Object.keys(schemas.value ?? {}).length)
const totalServers = computed(() => props.spec?.servers?.length ?? 0)
const securitySchemes = computed(() => props.spec?.components?.securitySchemes ?? {})
const showSchemas = ref(false)

const operationsPreview = computed(() =>
  props.tagGroups
    .flatMap((group) =>
      group.operations.slice(0, 2).map((operation) => ({
        tagName: group.name,
        method: operation.method,
        path: operation.path,
        summary: operation.operation.summary || operation.path,
        description: operation.operation.description,
        deprecated: operation.operation.deprecated,
      })),
    )
    .slice(0, 8),
)

const selectedOpData = computed(() => {
  if (!props.selectedOperation || !props.tagGroups.length) {
    return null
  }

  for (const group of props.tagGroups) {
    const found = group.operations.find(
      (op) => op.method === props.selectedOperation?.method && op.path === props.selectedOperation?.path,
    )
    if (found) {
      return found
    }
  }

  return null
})

function retry() {
  window.location.reload()
}
</script>

<template>
  <main class="flex flex-1 flex-col overflow-hidden bg-[var(--c-bg)]">
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

    <div v-else-if="spec" class="flex-1 overflow-y-auto">
      <div class="mx-auto max-w-[1120px] px-6 pb-12">
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

        <template v-if="viewMode === 'overview'">
          <div class="mb-6 grid gap-3 md:grid-cols-2 xl:grid-cols-4">
            <section class="rounded-2xl border border-[var(--c-border)] bg-white p-4 shadow-[var(--shadow-sm)]">
              <p class="text-[11px] font-semibold uppercase tracking-[0.18em] text-[var(--c-muted)]">Operations</p>
              <p class="mt-3 text-3xl font-bold text-[var(--c-text)]">{{ totalOps }}</p>
              <p class="mt-2 text-[12px] text-[var(--c-muted)]">当前服务可浏览接口总数。</p>
            </section>
            <section class="rounded-2xl border border-[var(--c-border)] bg-white p-4 shadow-[var(--shadow-sm)]">
              <p class="text-[11px] font-semibold uppercase tracking-[0.18em] text-[var(--c-muted)]">Tags</p>
              <p class="mt-3 text-3xl font-bold text-[var(--c-text)]">{{ totalTags }}</p>
              <p class="mt-2 text-[12px] text-[var(--c-muted)]">按业务标签组织的接口分组。</p>
            </section>
            <section class="rounded-2xl border border-[var(--c-border)] bg-white p-4 shadow-[var(--shadow-sm)]">
              <p class="text-[11px] font-semibold uppercase tracking-[0.18em] text-[var(--c-muted)]">Schemas</p>
              <p class="mt-3 text-3xl font-bold text-[var(--c-text)]">{{ totalSchemas }}</p>
              <p class="mt-2 text-[12px] text-[var(--c-muted)]">请求和响应的数据模型定义。</p>
            </section>
            <section class="rounded-2xl border border-[var(--c-border)] bg-white p-4 shadow-[var(--shadow-sm)]">
              <p class="text-[11px] font-semibold uppercase tracking-[0.18em] text-[var(--c-muted)]">Servers</p>
              <p class="mt-3 text-3xl font-bold text-[var(--c-text)]">{{ totalServers }}</p>
              <p class="mt-2 text-[12px] text-[var(--c-muted)]">文档声明的服务入口数量。</p>
            </section>
          </div>

          <div class="mb-6 grid gap-4 xl:grid-cols-[1.3fr_0.7fr]">
            <section class="rounded-2xl border border-[var(--c-border)] bg-white p-5 shadow-[var(--shadow-sm)]">
              <div class="flex items-center justify-between gap-3">
                <div>
                  <h2 class="text-[15px] font-semibold text-[var(--c-text)]">快速浏览接口</h2>
                  <p class="mt-1 text-[12px] text-[var(--c-muted)]">从服务主页直接跳转到接口详情，分享链接会保留当前定位。</p>
                </div>
                <span class="rounded-full bg-[var(--c-primary-light)] px-2.5 py-1 text-[11px] font-medium text-[var(--c-primary)]">
                  Shareable
                </span>
              </div>

              <div class="mt-4 grid gap-3">
                <button
                  v-for="operation in operationsPreview"
                  :key="`${operation.method}-${operation.path}`"
                  class="cursor-pointer rounded-xl border border-[var(--c-border)] bg-[var(--c-bg)] px-4 py-3 text-left transition-all hover:border-[var(--c-primary)] hover:bg-[var(--c-primary-light)]"
                  @click="emit('selectOperation', { method: operation.method, path: operation.path, summary: operation.summary })"
                >
                  <div class="flex flex-wrap items-center gap-2">
                    <span
                      class="rounded-full px-2 py-0.5 text-[10px] font-bold uppercase"
                      :class="{
                        'bg-blue-100 text-blue-700': operation.method === 'get',
                        'bg-emerald-100 text-emerald-700': operation.method === 'post',
                        'bg-amber-100 text-amber-700': operation.method === 'put',
                        'bg-rose-100 text-rose-700': operation.method === 'delete',
                        'bg-violet-100 text-violet-700': operation.method === 'patch',
                        'bg-slate-200 text-slate-700': !['get', 'post', 'put', 'delete', 'patch'].includes(operation.method),
                      }"
                    >
                      {{ operation.method }}
                    </span>
                    <span class="rounded-full bg-white px-2 py-0.5 text-[10px] text-[var(--c-muted)]">{{ operation.tagName }}</span>
                    <span v-if="operation.deprecated" class="rounded-full bg-amber-100 px-2 py-0.5 text-[10px] text-amber-700">Deprecated</span>
                  </div>
                  <div class="mt-2 text-[13px] font-semibold text-[var(--c-text)]">{{ operation.summary }}</div>
                  <div class="mt-1 font-mono text-[11px] text-[var(--c-muted)]">{{ operation.path }}</div>
                  <p v-if="operation.description" class="mt-2 text-[12px] leading-5 text-[var(--c-muted)]">
                    {{ operation.description }}
                  </p>
                </button>

                <p v-if="!operationsPreview.length" class="rounded-xl bg-[var(--c-bg)] px-4 py-3 text-[12px] text-[var(--c-muted)]">
                  当前文档尚未解析到接口列表。
                </p>
              </div>
            </section>

            <section class="rounded-2xl border border-[var(--c-border)] bg-white p-5 shadow-[var(--shadow-sm)]">
              <h2 class="text-[15px] font-semibold text-[var(--c-text)]">服务信息</h2>
              <dl class="mt-4 grid gap-3 text-[12px]">
                <div class="rounded-xl bg-[var(--c-bg)] px-4 py-3">
                  <dt class="font-semibold text-[var(--c-text)]">版本</dt>
                  <dd class="mt-1 font-mono text-[var(--c-muted)]">{{ spec.info?.version || '未声明' }}</dd>
                </div>
                <div class="rounded-xl bg-[var(--c-bg)] px-4 py-3">
                  <dt class="font-semibold text-[var(--c-text)]">基础路径</dt>
                  <dd class="mt-1 font-mono text-[var(--c-muted)]">{{ contextPath || '/' }}</dd>
                </div>
                <div class="rounded-xl bg-[var(--c-bg)] px-4 py-3">
                  <dt class="font-semibold text-[var(--c-text)]">联系信息</dt>
                  <dd class="mt-1 text-[var(--c-muted)]">
                    {{ spec.info?.contact?.name || spec.info?.contact?.email || spec.info?.contact?.url || '未提供' }}
                  </dd>
                </div>
                <div class="rounded-xl bg-[var(--c-bg)] px-4 py-3">
                  <dt class="font-semibold text-[var(--c-text)]">许可证</dt>
                  <dd class="mt-1 text-[var(--c-muted)]">{{ spec.info?.license?.name || '未提供' }}</dd>
                </div>
              </dl>
            </section>
          </div>

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

          <div class="mb-4 grid gap-4 xl:grid-cols-[0.9fr_1.1fr]">
            <section class="rounded-2xl border border-[var(--c-border)] bg-white p-5 shadow-[var(--shadow-sm)]">
              <h2 class="text-[14px] font-semibold text-[var(--c-text)]">服务地址</h2>
              <div class="mt-4 grid gap-3">
                <div
                  v-for="srv in spec.servers ?? []"
                  :key="srv.url"
                  class="rounded-xl border border-[var(--c-border)] bg-[var(--c-bg)] px-4 py-3"
                >
                  <div class="font-mono text-[12px] text-[var(--c-primary)]">{{ srv.url }}</div>
                  <p v-if="srv.description" class="mt-1 text-[12px] text-[var(--c-muted)]">{{ srv.description }}</p>
                </div>
                <p v-if="!(spec.servers?.length)" class="rounded-xl bg-[var(--c-bg)] px-4 py-3 text-[12px] text-[var(--c-muted)]">
                  文档未声明 servers，通常表示沿用当前访问域名。
                </p>
              </div>
            </section>

            <section class="rounded-2xl border border-[var(--c-border)] bg-white p-5 shadow-[var(--shadow-sm)]">
              <h2 class="text-[14px] font-semibold text-[var(--c-text)]">认证与安全</h2>
              <div class="mt-4 grid gap-3">
                <div
                  v-for="(scheme, name) in securitySchemes"
                  :key="name"
                  class="rounded-xl border border-[var(--c-border)] bg-[var(--c-bg)] px-4 py-3"
                >
                  <div class="flex items-center gap-2">
                    <span class="rounded-full bg-white px-2 py-0.5 text-[10px] font-semibold text-[var(--c-text)]">{{ scheme.type }}</span>
                    <span class="text-[13px] font-medium text-[var(--c-text)]">{{ name }}</span>
                  </div>
                  <p class="mt-2 text-[12px] text-[var(--c-muted)]">
                    {{ scheme.description || `${scheme.scheme || scheme.in || '默认'} 认证方案` }}
                  </p>
                </div>
                <p v-if="!Object.keys(securitySchemes).length" class="rounded-xl bg-[var(--c-bg)] px-4 py-3 text-[12px] text-[var(--c-muted)]">
                  当前文档未声明 securitySchemes，可在设置中手动附加请求头调试接口。
                </p>
              </div>
            </section>
          </div>
        </template>

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
