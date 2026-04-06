<script setup lang="ts">
/**
 * OverviewPanel.vue
 *
 * 提取自 ContentArea.vue 的服务总览面板。
 * 职责：展示统计数据、接口预览列表、服务信息、Tags 分组、Schemas 折叠区块。
 * 原 ContentArea 387 行中约 250 行的 overview 模板已迁移至此，使 ContentArea
 * 收缩为纯路由/状态协调层，每个文件专注单一职责。
 */
import { ref } from 'vue'
import SchemaView from './swagger/SchemaView.vue'
import type { OpenApiSpec, TagGroup, SchemaObject } from '@/types/openapi'

const props = defineProps<{
  spec: OpenApiSpec
  tagGroups: TagGroup[]
  schemas?: Record<string, SchemaObject>
  contextPath?: string
  totalOps: number
  totalTags: number
  totalSchemas: number
  totalServers: number
  operationsPreview: Array<{
    tagName: string
    method: string
    path: string
    summary: string
    description?: string
    deprecated?: boolean
  }>
}>()

const emit = defineEmits<{
  selectOperation: [operation: { method: string; path: string; summary?: string }]
}>()

const showSchemas = ref(false)
const securitySchemes = props.spec.components?.securitySchemes ?? {}
</script>

<template>
  <!-- Stats row -->
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

  <!-- Quick jump + service info -->
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

  <!-- Servers + Security -->
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

  <!-- Schemas collapsible -->
  <div v-if="schemas && Object.keys(schemas).length" class="mt-6">
    <button
      class="flex w-full cursor-pointer items-center gap-2 border border-[var(--c-border)] bg-white px-4 py-3 text-left text-[13px] font-semibold text-[var(--c-text)] transition-colors hover:bg-[var(--c-primary-light)]"
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
</template>
