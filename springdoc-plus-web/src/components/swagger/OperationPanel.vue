<script setup lang="ts">
import { ref, computed, toRef, watch } from 'vue'
import type { OperationItem } from '@/types/openapi'
import type { SchemaObject } from '@/types/openapi'
import MethodBadge from './MethodBadge.vue'
import SchemaView from './SchemaView.vue'
import { useSimulateRequest } from '@/composables/useSimulateRequest'
import {
  generateJsonSchemaExample,
  resolveSchemaRef,
  buildSchemaExample,
  isJsonContentType,
  isFormContentType,
  isBinaryField,
  CONTENT_TYPE,
} from '@/utils/schema'

const props = defineProps<{
  item: OperationItem
  schemas?: Record<string, SchemaObject>
  contextPath?: string
}>()

const opId = computed(() => `op-${props.item.method}-${props.item.path}`)

const simulate = useSimulateRequest(
  toRef(() => props.item),
  toRef(() => props.contextPath)
)

// 自定义请求头
const customHeaders = ref<Array<{ name: string; value: string }>>([])

// 发送请求面板整体折叠（默认收起）
const simulatePanelCollapsed = ref(true)

function addCustomHeader() {
  customHeaders.value.push({ name: '', value: '' })
}

function removeCustomHeader(index: number) {
  customHeaders.value.splice(index, 1)
}

const simulateParams = computed(() => simulate.params)
const simulateRequestBody = computed(() => simulate.requestBody)
const simulateResult = computed(() => simulate.result)

// 使用工具函数判断 Content-Type
const isJson = computed(() => isJsonContentType(simulate.contentType.value))
const isForm = computed(() => isFormContentType(simulate.contentType.value))

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

const requestBodySchema = computed(() => {
  const rb = op.value.requestBody
  if (!rb?.content) return null
  const mediaType = Object.keys(rb.content)[0]
  const media = rb.content[mediaType]
  return media?.schema ?? null
})

// 从 schema 中提取字段列表（用于 form 表单展示）
function getSchemaFields(schema: SchemaObject | null): Array<{
  name: string
  type: string
  format?: string
  isArray: boolean
  isBinary: boolean
  required: boolean
  description: string
  example: string
  enum?: string[]
}> {
  if (!schema) return []
  const resolvedSchema = resolveSchemaRef(schema, props.schemas)
  if (!resolvedSchema?.properties) return []
  const requiredFields = resolvedSchema.required ?? []
  return Object.entries(resolvedSchema.properties).map(([name, fieldSchema]) => {
    const resolved = resolveSchemaRef(fieldSchema as SchemaObject, props.schemas)
    // 判断是否为数组类型的文件字段（type: array, items.format: binary）
    const isArray = resolved?.type === 'array'
    const itemSchema = isArray ? resolveSchemaRef((resolved as any).items as SchemaObject, props.schemas) : null
    const isBinary = isBinaryField(resolved)
    const effectiveType = isArray ? 'array' : (resolved?.type ?? 'string')
    return {
      name,
      type: effectiveType,
      format: resolved?.format ?? (isArray ? itemSchema?.format : undefined),
      isArray,
      isBinary,
      required: requiredFields.includes(name),
      description: resolved?.description ?? '',
      example: String(resolved?.example ?? resolved?.default ?? ''),
      enum: resolved?.enum ? (resolved.enum as string[]) : undefined,
    }
  })
}

// form 字段的输入状态（用 name -> value 的 map）
const formFieldValues = ref<Record<string, string>>({})
// 文件字段（name -> FileList）
const formFileValues = ref<Record<string, FileList | null>>({})
// 防止 JSON 重复初始化
const jsonBodyInitialized = ref(false)

// 当 schema 字段列表可用时，初始化 form 字段值
const schemaFields = computed(() => getSchemaFields(requestBodySchema.value))

// 当 content-type 为 form 时，将 formFieldValues 序列化后同步到 requestBody，再发送
function handleSendRequest() {
  if (isForm.value) {
    const ct = simulate.contentType.value
    const hasFiles = schemaFields.value.some(f => f.isBinary)
    if (ct.includes(CONTENT_TYPE.MULTIPART_FORM_DATA) || hasFiles) {
      // 使用 FormData（含文件）
      const fd = new FormData()
      for (const field of schemaFields.value) {
        if (field.isBinary) {
          const files = formFileValues.value[field.name]
          if (files && files.length > 0) {
            if (field.isArray) {
              // multiple files -> append each with same key
              for (let i = 0; i < files.length; i++) {
                fd.append(field.name, files[i])
              }
            } else {
              fd.append(field.name, files[0])
            }
          }
        } else {
          const val = formFieldValues.value[field.name]
          if (val !== '' && val !== undefined) {
            fd.append(field.name, val)
          }
        }
      }
      simulate.sendRequest(customHeaders.value, fd)
    } else {
      // application/x-www-form-urlencoded
      const params = new URLSearchParams()
      for (const [key, val] of Object.entries(formFieldValues.value)) {
        if (val !== '' && val !== undefined) {
          params.append(key, val)
        }
      }
      simulate.requestBody.value = params.toString()
      simulate.sendRequest(customHeaders.value)
    }
  } else {
    simulate.sendRequest(customHeaders.value)
  }
}

// 重置时同时清空 form 字段值，并重新初始化 JSON 示例
function handleReset() {
  formFieldValues.value = {}
  formFileValues.value = {}
  jsonBodyInitialized.value = false
  simulate.reset()
  initJsonBody()
}

// 初始化 JSON 请求体示例（带防重复）
function initJsonBody() {
  if (jsonBodyInitialized.value) return
  if (isJson.value && requestBodySchema.value) {
    const example = generateJsonSchemaExample(requestBodySchema.value, props.schemas)
    if (!simulate.requestBody.value || simulate.requestBody.value === '{}' || simulate.requestBody.value === '') {
      simulate.requestBody.value = example
    }
    jsonBodyInitialized.value = true
  }
}

// panel 展开时初始化 JSON
watch(simulatePanelCollapsed, (collapsed) => {
  if (!collapsed) initJsonBody()
})

// schema 变化时也初始化（切换接口时），同时重置初始化标志
watch(requestBodySchema, () => {
  jsonBodyInitialized.value = false
  if (!simulatePanelCollapsed.value) initJsonBody()
})
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

      <!-- ===== Simulate Request Panel ===== -->
      <div class="mb-4 rounded-lg border border-[var(--c-border)] bg-white">

        <!-- Panel header with collapse toggle -->
        <div
          class="flex cursor-pointer items-center justify-between border-b border-[var(--c-border)] px-3 py-2.5 select-none hover:bg-gray-50/60 transition-colors"
          @click="simulatePanelCollapsed = !simulatePanelCollapsed"
        >
          <div class="flex items-center gap-2">
            <svg
              class="h-4 w-4 text-[var(--c-muted)] transition-transform duration-200"
              :class="simulatePanelCollapsed ? '-rotate-90' : ''"
              viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
            >
              <path d="m6 9 6 6 6-6" />
            </svg>
            <span class="text-[12px] font-semibold text-[var(--c-text)]">调试请求</span>
          </div>
          <div class="flex items-center gap-2" @click.stop>
            <button
              class="rounded-lg border border-[var(--c-border)] bg-white px-3 py-1.5 text-[12px] text-[var(--c-text)] transition-colors hover:bg-gray-50"
              @click="handleReset()"
            >
              重置
            </button>
            <button
              class="rounded-lg bg-[var(--c-primary)] px-4 py-1.5 text-[12px] font-medium text-white transition-colors hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
              :disabled="simulate.loading.value"
              @click="handleSendRequest()"
            >
              {{ simulate.loading.value ? '发送中...' : '发送请求' }}
            </button>
          </div>
        </div>

        <div v-if="!simulatePanelCollapsed">

          <!-- ===== Unified Request Config (always visible) ===== -->
          <div class="border-b border-[var(--c-border)]">
            <div class="px-3 pb-4 pt-3 space-y-4">

              <!-- Path / Query Params -->
              <div v-if="simulateParams.value.length">
                <p class="mb-2 text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]">路径 / 查询参数</p>
                <div class="space-y-2">
                  <div v-for="param in simulateParams.value" :key="param.name" class="flex items-center gap-2">
                    <span class="w-28 shrink-0 font-mono text-[12px] truncate" :title="param.name">{{ param.name }}</span>
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
                      :placeholder="param.example !== undefined ? String(param.example) : (param.type || 'Value')"
                    />
                    <span v-if="param.required" class="shrink-0 text-[10px] text-red-500">必填</span>
                  </div>
                </div>
              </div>

              <!-- Custom Headers -->
              <div>
                <div class="mb-2 flex items-center justify-between">
                  <p class="text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]">自定义请求头</p>
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
                <p v-else class="text-[11px] text-[var(--c-muted)]">暂无自定义请求头</p>
              </div>

              <!-- Request Body -->
              <div v-if="requestBodySchema">
                <div class="mb-2 flex items-center justify-between">
                  <p class="text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]">
                    请求体
                    <span class="ml-1 rounded bg-gray-100 px-1.5 py-px font-mono text-[10px] normal-case text-[var(--c-muted)]">
                      {{ simulate.contentType.value }}
                    </span>
                    <span v-if="op.requestBody?.required" class="ml-1 text-red-500 normal-case">（必填）</span>
                  </p>
                  <button
                    class="rounded border border-[var(--c-border)] px-2 py-0.5 text-[11px] text-[var(--c-text)] hover:bg-gray-50"
                    @click="simulate.resetRequestBody(); formFieldValues = {}"
                  >
                    重置
                  </button>
                </div>

                <!-- application/x-www-form-urlencoded or multipart/form-data: field list -->
                <div v-if="isForm">
                  <div class="rounded-lg border border-[var(--c-border)] bg-gray-50 overflow-hidden">
                    <table class="w-full border-collapse text-[12px]">
                      <thead>
                        <tr class="border-b border-[var(--c-border)] bg-gray-100">
                          <th class="px-3 py-2 text-left font-semibold text-[var(--c-muted)] text-[11px]">字段名</th>
                          <th class="px-3 py-2 text-left font-semibold text-[var(--c-muted)] text-[11px]">类型</th>
                          <th class="px-3 py-2 text-left font-semibold text-[var(--c-muted)] text-[11px]">必填</th>
                          <th class="px-3 py-2 text-left font-semibold text-[var(--c-muted)] text-[11px]">说明</th>
                          <th class="px-3 py-2 text-left font-semibold text-[var(--c-muted)] text-[11px]">值</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr
                          v-for="field in schemaFields"
                          :key="field.name"
                          class="border-b border-[var(--c-border)] last:border-0"
                        >
                          <td class="px-3 py-2 font-mono font-medium text-[var(--c-text)]">
                            {{ field.name }}
                          </td>
                          <td class="px-3 py-2 font-mono text-[var(--c-muted)] text-[11px]">
                            <span v-if="field.isBinary" class="rounded bg-purple-100 px-1.5 py-0.5 text-[10px] text-purple-700">
                              {{ field.isArray ? 'file[]' : 'file' }}
                            </span>
                            <span v-else>{{ field.type }}</span>
                          </td>
                          <td class="px-3 py-2">
                            <span v-if="field.required" class="text-red-500 text-[11px]">必填</span>
                            <span v-else class="text-[var(--c-muted)] text-[11px]">-</span>
                          </td>
                          <td class="px-3 py-2 text-[var(--c-muted)] max-w-[120px] truncate" :title="field.description">
                            {{ field.description || '-' }}
                          </td>
                          <td class="px-3 py-2">
                            <!-- binary / file upload -->
                            <input
                              v-if="field.isBinary"
                              type="file"
                              :multiple="field.isArray"
                              class="w-full text-[12px] text-[var(--c-text)] file:mr-2 file:rounded file:border file:border-[var(--c-border)] file:bg-gray-50 file:px-2 file:py-0.5 file:text-[11px] file:text-[var(--c-text)] hover:file:bg-gray-100"
                              @change="formFileValues[field.name] = ($event.target as HTMLInputElement).files"
                            />
                            <!-- enum select -->
                            <select
                              v-else-if="field.enum && field.enum.length"
                              v-model="formFieldValues[field.name]"
                              class="w-full rounded border border-[var(--c-border)] px-2 py-1 text-[12px] outline-none focus:border-[var(--c-primary)] bg-white"
                            >
                              <option value="">-- 请选择 --</option>
                              <option v-for="opt in field.enum" :key="opt" :value="opt">{{ opt }}</option>
                            </select>
                            <!-- boolean toggle -->
                            <select
                              v-else-if="field.type === 'boolean'"
                              v-model="formFieldValues[field.name]"
                              class="w-full rounded border border-[var(--c-border)] px-2 py-1 text-[12px] outline-none focus:border-[var(--c-primary)] bg-white"
                            >
                              <option value="">-- 请选择 --</option>
                              <option value="true">true</option>
                              <option value="false">false</option>
                            </select>
                            <!-- default text input -->
                            <input
                              v-else
                              v-model="formFieldValues[field.name]"
                              class="w-full rounded border border-[var(--c-border)] px-2 py-1 text-[12px] outline-none focus:border-[var(--c-primary)]"
                              :placeholder="field.example || field.type"
                            />
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>

                <!-- application/json: JSON editor with generated example -->
                <div v-else-if="isJson">
                  <!-- Schema fields overview -->
                  <div v-if="schemaFields.length" class="mb-2 rounded-lg border border-[var(--c-border)] bg-gray-50 overflow-hidden">
                    <table class="w-full border-collapse text-[12px]">
                      <thead>
                        <tr class="border-b border-[var(--c-border)] bg-gray-100">
                          <th class="px-3 py-2 text-left font-semibold text-[var(--c-muted)] text-[11px]">字段名</th>
                          <th class="px-3 py-2 text-left font-semibold text-[var(--c-muted)] text-[11px]">类型</th>
                          <th class="px-3 py-2 text-left font-semibold text-[var(--c-muted)] text-[11px]">必填</th>
                          <th class="px-3 py-2 text-left font-semibold text-[var(--c-muted)] text-[11px]">示例值</th>
                          <th class="px-3 py-2 text-left font-semibold text-[var(--c-muted)] text-[11px]">说明</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr
                          v-for="field in schemaFields"
                          :key="field.name"
                          class="border-b border-[var(--c-border)] last:border-0"
                        >
                          <td class="px-3 py-2 font-mono font-medium text-[var(--c-text)]">{{ field.name }}</td>
                          <td class="px-3 py-2 font-mono text-[var(--c-muted)] text-[11px]">{{ field.type }}</td>
                          <td class="px-3 py-2">
                            <span v-if="field.required" class="text-red-500 text-[11px]">必填</span>
                            <span v-else class="text-[var(--c-muted)] text-[11px]">-</span>
                          </td>
                          <td class="px-3 py-2 font-mono text-[11px] text-[var(--c-muted)]">
                            <span v-if="field.enum" class="text-[10px]">{{ field.enum.join(' | ') }}</span>
                            <span v-else>{{ field.example || '-' }}</span>
                          </td>
                          <td class="px-3 py-2 text-[var(--c-muted)] max-w-[160px] truncate" :title="field.description">
                            {{ field.description || '-' }}
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <!-- JSON editor -->
                  <textarea
                    v-model="simulate.requestBody.value"
                    rows="8"
                    class="w-full resize-y rounded border border-[var(--c-border)] bg-gray-50 p-2 font-mono text-[11px] outline-none focus:border-[var(--c-primary)]"
                    spellcheck="false"
                    placeholder="{}"
                  />
                  <p class="mt-1 text-[10px] text-[var(--c-muted)]">
                    💡 上方为示例 JSON，可直接修改后发送
                  </p>
                </div>

                <!-- Other content types -->
                <div v-else>
                  <input
                    v-model="simulate.requestBody.value"
                    class="w-full rounded border border-[var(--c-border)] px-2 py-1 text-[12px] outline-none focus:border-[var(--c-primary)]"
                    placeholder="Request body"
                  />
                </div>
              </div>

            </div>
          </div>

          <!-- ===== Response Result ===== -->
          <div v-if="simulateResult.value || simulate.error.value" class="border-t border-[var(--c-border)] px-3 py-3">
            <div class="mb-2 flex items-center gap-2">
              <p class="text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]">响应结果</p>
              <span v-if="simulateResult.value"
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
              <span v-if="simulateResult.value" class="text-[11px] text-[var(--c-muted)]">{{ simulateResult.value.duration }}ms</span>
            </div>

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
      </div>

      <!-- ===== Parameters (docs) ===== -->
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

      <!-- ===== Request Body (docs) ===== -->
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

      <!-- ===== Responses (docs) ===== -->
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