<script setup lang="ts">
import { ref, computed } from 'vue'
import type { OperationItem } from '@/types/openapi'
import type { SchemaObject } from '@/types/openapi'
import MethodBadge from './MethodBadge.vue'
import SchemaView from './SchemaView.vue'
import { useSimulateRequest } from '@/composables/useSimulateRequest'

const props = defineProps<{
  item: OperationItem
  schemas?: Record<string, SchemaObject>
}>()

const opId = computed(() => `op-${props.item.method}-${props.item.path}`)

// 模拟请求
const simulate = useSimulateRequest(props.item)

// 自定义请求头
const customHeaders = ref<Array<{ name: string; value: string }>>([])

function addCustomHeader() {
  customHeaders.value.push({ name: '', value: '' })
}

function removeCustomHeader(index: number) {
  customHeaders.value.splice(index, 1)
}

// 本地引用，用于模板访问
const simulateParams = computed(() => simulate.params)
const simulateRequestBody = computed(() => simulate.requestBody)
const simulateResult = computed(() => simulate.result)

const statusColors: Record<string, string> = {
  '2': 'bg-green-100 text-green-800',
  '3': 'bg-blue-100 text-blue-800',
  '4': 'bg-amber-100 text-amber-800',
  '5': 'bg-red-100 text-red-800',
}

function statusColor(code: string): string {
  return statusColors[code[0]] ?? 'bg-gray-100 text-gray-700'
}

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

// 请求体 Schema 解析
interface BodyProp {
  name: string
  type: string
  required: boolean
  description?: string
  value: string
  isPrimitive: boolean
}

// 获取请求体 schema
const requestBodySchema = computed(() => {
  const rb = op.value.requestBody
  if (!rb?.content) return null
  const mediaType = Object.keys(rb.content)[0]
  const media = rb.content[mediaType]
  return media?.schema ?? null
})

// 解析 $ref
function resolveRef(ref: string): SchemaObject | null {
  if (!ref.startsWith('#/components/schemas/')) return null
  const name = ref.replace('#/components/schemas/', '')
  return props.schemas?.[name] ?? null
}

// 获取类型标签
function getTypeLabel(schema: SchemaObject | undefined): string {
  if (!schema) return 'any'
  if (schema.$ref) {
    const resolved = resolveRef(schema.$ref)
    if (resolved) return getTypeLabel(resolved)
    return schema.$ref.split('/').pop() ?? 'object'
  }
  if (schema.type === 'array') {
    const itemLabel = getTypeLabel(schema.items)
    return `${itemLabel}[]`
  }
  return schema.format ? `${schema.type}(${schema.format})` : schema.type ?? 'any'
}

// 判断是否为原始类型
function isPrimitiveType(schema: SchemaObject | undefined): boolean {
  if (!schema) return false
  if (schema.$ref) {
    const resolved = resolveRef(schema.$ref)
    return isPrimitiveType(resolved ?? undefined)
  }
  const t = schema.type
  return t === 'string' || t === 'number' || t === 'integer' || t === 'boolean'
}

// 请求体参数列表
const requestBodyProps = computed<BodyProp[]>(() => {
  const schema = requestBodySchema.value
  if (!schema) return []

  // 解引用
  let resolved = schema
  if (schema.$ref) {
    resolved = resolveRef(schema.$ref) ?? schema
  }

  if (!resolved.properties) return []

  const bodyParams = simulate.bodyParams.value

  return Object.entries(resolved.properties).map(([name, propSchema]) => {
    const p = propSchema as SchemaObject
    return {
      name,
      type: getTypeLabel(p),
      required: resolved.required?.includes(name) ?? false,
      description: p.description,
      value: bodyParams[name] ?? '',
      isPrimitive: isPrimitiveType(p),
    }
  })
})

// 更新请求体参数
function updateBodyProp(name: string, value: string) {
  simulate.updateBodyParam(name, value)
}
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

    <!-- Detail (always visible) -->
    <div class="border-t border-white/60 bg-white/80 px-4 py-4">

      <!-- Description -->
      <p v-if="op.description" class="mb-4 text-[13px] text-[var(--c-muted)]">{{ op.description }}</p>
      <p v-else-if="op.summary" class="mb-4 text-[13px] text-[var(--c-muted)]">{{ op.summary }}</p>

      <!-- Simulate request panel -->
      <div class="mb-4 rounded-lg border border-[var(--c-border)] bg-white">
        <div class="flex items-center gap-2 border-b border-[var(--c-border)] p-3">
          <button
            class="rounded-lg bg-[var(--c-primary)] px-4 py-2 text-[13px] font-medium text-white transition-colors hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="simulate.loading.value"
            @click="simulate.sendRequest(customHeaders)"
          >
            {{ simulate.loading.value ? '发送中...' : '发送请求' }}
          </button>
          <button
            class="rounded-lg border border-[var(--c-border)] bg-white px-3 py-2 text-[13px] text-[var(--c-text)] transition-colors hover:bg-gray-50"
            @click="simulate.reset()"
          >
            重置
          </button>
        </div>

        <!-- Request params editor -->
        <div v-if="simulateParams.value.length" class="border-b border-[var(--c-border)] p-3">
          <h4 class="mb-2 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]">请求参数</h4>
          <div class="space-y-2">
            <div v-for="param in simulateParams.value" :key="param.name" class="flex items-center gap-2">
              <span class="w-24 shrink-0 font-mono text-[12px]">{{ param.name }}</span>
              <span
                class="w-12 shrink-0 rounded px-1.5 py-0.5 text-[10px] font-medium text-center"
                :class="{
                  'bg-blue-100 text-blue-700': param.in === 'path',
                  'bg-green-100 text-green-700': param.in === 'query',
                  'bg-purple-100 text-purple-700': param.in === 'header',
                  'bg-amber-100 text-amber-700': param.in === 'cookie',
                }"
              >
                {{ param.in }}
              </span>
              <input
                :value="param.value"
                @input="param.value = ($event.target as HTMLInputElement).value"
                class="flex-1 rounded border border-[var(--c-border)] px-2 py-1 text-[12px] outline-none focus:border-[var(--c-primary)]"
                :placeholder="param.type || 'Value'"
              />
              <span v-if="param.required" class="shrink-0 text-[10px] text-red-500">必填</span>
            </div>
          </div>
        </div>

        <!-- Custom headers -->
        <div class="border-b border-[var(--c-border)] p-3">
          <div class="mb-2 flex items-center justify-between">
            <h4 class="text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]">自定义请求头</h4>
            <button
              class="rounded border border-[var(--c-border)] px-2 py-0.5 text-[11px] text-[var(--c-text)] hover:bg-gray-50"
              @click="addCustomHeader"
            >
              + 添加
            </button>
          </div>
          <div v-if="customHeaders.length" class="space-y-2">
            <div v-for="(header, index) in customHeaders" :key="index" class="flex items-center gap-2">
              <input
                v-model="header.name"
                class="w-32 shrink-0 rounded border border-[var(--c-border)] px-2 py-1 text-[12px] outline-none focus:border-[var(--c-primary)]"
                placeholder="Header Name"
              />
              <input
                v-model="header.value"
                class="flex-1 rounded border border-[var(--c-border)] px-2 py-1 text-[12px] outline-none focus:border-[var(--c-primary)]"
                placeholder="Header Value"
              />
              <button
                class="shrink-0 rounded border border-red-200 px-2 py-1 text-[11px] text-red-600 hover:bg-red-50"
                @click="removeCustomHeader(index)"
              >
                删除
              </button>
            </div>
          </div>
          <p v-else class="text-[11px] text-[var(--c-muted)]">点击"添加"按钮添加自定义请求头</p>
        </div>

        <!-- Request body params -->
        <div v-if="requestBodySchema" class="border-b border-[var(--c-border)] p-3">
          <h4 class="mb-2 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]">
            请求体参数
            <span v-if="op.requestBody?.required" class="ml-1 text-red-500 normal-case">（必填）</span>
          </h4>
          <div class="space-y-2">
            <div v-for="prop in requestBodyProps" :key="prop.name" class="flex items-start gap-2">
              <span class="w-32 shrink-0 font-mono text-[12px] pt-1">{{ prop.name }}</span>
              <span class="w-20 shrink-0 rounded bg-gray-100 px-1.5 py-0.5 text-[10px] font-medium text-center text-[var(--c-muted)] mt-1">
                {{ prop.type }}
              </span>
              <input
                v-if="prop.isPrimitive"
                :value="prop.value"
                @input="updateBodyProp(prop.name, ($event.target as HTMLInputElement).value)"
                class="flex-1 rounded border border-[var(--c-border)] px-2 py-1 text-[12px] outline-none focus:border-[var(--c-primary)]"
                :placeholder="prop.description || prop.type"
              />
              <textarea
                v-else
                :value="prop.value"
                @input="updateBodyProp(prop.name, ($event.target as HTMLTextAreaElement).value)"
                rows="2"
                class="flex-1 rounded border border-[var(--c-border)] px-2 py-1 text-[12px] font-mono outline-none focus:border-[var(--c-primary)]"
                :placeholder="prop.description || 'JSON'"
              />
              <span v-if="prop.required" class="shrink-0 text-[10px] text-red-500 pt-1">必填</span>
            </div>
          </div>
        </div>

        <!-- Response -->
        <div v-if="simulateResult.value || simulate.error.value" class="p-3">
          <h4 class="mb-2 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]">
            响应结果
            <span v-if="simulateResult.value" class="ml-2 font-normal normal-case">
              <span
                class="rounded px-2 py-0.5 font-mono text-[11px] font-bold"
                :class="{
                  'bg-green-100 text-green-800': simulateResult.value.status < 300,
                  'bg-blue-100 text-blue-800': simulateResult.value.status >= 300 && simulateResult.value.status < 400,
                  'bg-amber-100 text-amber-800': simulateResult.value.status >= 400 && simulateResult.value.status < 500,
                  'bg-red-100 text-red-800': simulateResult.value.status >= 500,
                }"
              >
                {{ simulateResult.value.status }} {{ simulateResult.value.statusText }}
              </span>
              <span class="ml-2 text-[var(--c-muted)]">{{ simulateResult.value.duration }}ms</span>
            </span>
          </h4>

          <!-- Error -->
          <div v-if="simulate.error.value" class="rounded-lg border border-red-200 bg-red-50 p-3 text-[12px] text-red-700">
            {{ simulate.error.value }}
          </div>

          <!-- Response headers -->
          <div v-if="simulateResult.value?.headers" class="mb-2 rounded-lg border border-[var(--c-border)] bg-gray-50 p-2">
            <p class="mb-1 text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]">响应头</p>
            <div v-for="(value, key) in simulateResult.value.headers" :key="key" class="flex text-[11px]">
              <span class="w-32 shrink-0 font-mono text-[var(--c-muted)]">{{ key }}:</span>
              <span class="flex-1 truncate font-mono">{{ value }}</span>
            </div>
          </div>

          <!-- Response body -->
          <div v-if="simulateResult.value?.data !== undefined" class="rounded-lg border border-[var(--c-border)] bg-gray-50 p-2">
            <p class="mb-1 text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]">响应体</p>
            <pre class="max-h-[400px] overflow-auto text-[11px]">{{ JSON.stringify(simulateResult.value.data, null, 2) }}</pre>
          </div>
        </div>
      </div>

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
              <tr
                v-for="p in parameters"
                :key="`${p.in}-${p.name}`"
                class="border-b border-[var(--c-border)] last:border-0"
              >
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

      <!-- Request body -->
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
            <div
              v-for="[code, resp] in responses"
              :key="code"
              class="overflow-hidden rounded-lg border border-[var(--c-border)] bg-white"
            >
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
