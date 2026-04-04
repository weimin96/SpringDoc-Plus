<script setup lang="ts">
import { ref, computed, toRef, watch } from 'vue'
import type { OperationItem, SchemaObject } from '@/types/openapi'
import type { AuthHeader } from '@/types'
import { useSimulateRequest } from '@/composables/useSimulateRequest'
import {
  generateJsonSchemaExample,
  resolveSchemaRef,
  isJsonContentType,
  isFormContentType,
  isBinaryField,
  CONTENT_TYPE,
} from '@/utils/schema'

const props = defineProps<{
  item: OperationItem
  schemas?: Record<string, SchemaObject>
  contextPath?: string
  authHeaders?: AuthHeader[]
}>()

// pass schemasRef so $ref can be resolved when generating JSON examples
const simulate = useSimulateRequest(
  toRef(() => props.item),
  toRef(() => props.contextPath),
  toRef(() => props.schemas)
)

// ── Per-operation UI state cache ────────────────────────────────────────────
// Stores formFieldValues, formFileValues and customHeaders per "method-path" key
// so switching away and back restores what the user typed.
interface UiSnapshot {
  formFieldValues: Record<string, string>
  customHeaders: Array<{ name: string; value: string }>
}
const uiCache = new Map<string, UiSnapshot>()

function saveUiSnapshot(key: string) {
  uiCache.set(key, {
    formFieldValues: { ...formFieldValues.value },
    customHeaders: customHeaders.value.map(h => ({ ...h })),
  })
}

function restoreUiSnapshot(key: string) {
  const cached = uiCache.get(key)
  if (cached) {
    formFieldValues.value = { ...cached.formFieldValues }
    customHeaders.value = cached.customHeaders.map(h => ({ ...h }))
  } else {
    formFieldValues.value = {}
    customHeaders.value = []
  }
}
// ────────────────────────────────────────────────────────────────────────────

const customHeaders = ref<Array<{ name: string; value: string }>>([])
const simulatePanelCollapsed = ref(true)

function addCustomHeader() {
  customHeaders.value.push({ name: '', value: '' })
}
function removeCustomHeader(index: number) {
  customHeaders.value.splice(index, 1)
}

const mergedHeaders = computed(() => {
  const headerMap = new Map<string, string>()
  if (props.authHeaders) {
    props.authHeaders.forEach(h => {
      if (h.name) {
        const name = h.name.trim().toLowerCase()
        let val = (h.value || '').trim()
        if (val) {
          const prefix = (h.defaultPrefix || '').trim()
          if (prefix && !val.startsWith(prefix + ' ')) val = `${prefix} ${val}`
          headerMap.set(name, val)
        }
      }
    })
  }
  customHeaders.value.forEach(h => {
    if (h.name && h.value) headerMap.set(h.name.trim().toLowerCase(), h.value)
  })
  return Array.from(headerMap.entries()).map(([name, value]) => ({ name, value }))
})

const simulateParams = computed(() => simulate.params)
const simulateResult = computed(() => simulate.result)
const isJson = computed(() => isJsonContentType(simulate.contentType.value))
const isForm = computed(() => isFormContentType(simulate.contentType.value))

const requestBodySchema = computed(() => {
  const rb = props.item.operation.requestBody
  if (!rb?.content) return null
  const mediaType = Object.keys(rb.content)[0]
  return rb.content[mediaType]?.schema ?? null
})

function getSchemaFields(schema: SchemaObject | null) {
  if (!schema) return []
  const resolvedSchema = resolveSchemaRef(schema, props.schemas)
  if (!resolvedSchema?.properties) return []
  const requiredFields = resolvedSchema.required ?? []
  return Object.entries(resolvedSchema.properties).map(([name, fieldSchema]) => {
    const resolved = resolveSchemaRef(fieldSchema as SchemaObject, props.schemas)
    const isArray = resolved?.type === 'array'
    const isBinary = isBinaryField(resolved)
    const effectiveType = isArray ? 'array' : (resolved?.type ?? 'string')
    return {
      name,
      type: effectiveType,
      isArray,
      isBinary,
      required: requiredFields.includes(name),
      description: resolved?.description ?? '',
      example: String(resolved?.example ?? resolved?.default ?? ''),
      enum: resolved?.enum ? (resolved.enum as string[]) : undefined,
    }
  })
}

const formFieldValues = ref<Record<string, string>>({})
const formFileValues = ref<Record<string, FileList | null>>({})
const jsonBodyInitialized = ref(false)
const schemaFields = computed(() => getSchemaFields(requestBodySchema.value))

function handleSendRequest() {
  if (isForm.value) {
    const ct = simulate.contentType.value
    const hasFiles = schemaFields.value.some(f => f.isBinary)
    if (ct.includes(CONTENT_TYPE.MULTIPART_FORM_DATA) || hasFiles) {
      const fd = new FormData()
      for (const field of schemaFields.value) {
        if (field.isBinary) {
          const files = formFileValues.value[field.name]
          if (files && files.length > 0) {
            if (field.isArray) { for (let i = 0; i < files.length; i++) fd.append(field.name, files[i]) }
            else fd.append(field.name, files[0])
          }
        } else {
          const val = formFieldValues.value[field.name]
          if (val !== '' && val !== undefined) fd.append(field.name, val)
        }
      }
      simulate.sendRequest(mergedHeaders.value, fd)
    } else {
      const params = new URLSearchParams()
      for (const [key, val] of Object.entries(formFieldValues.value)) {
        if (val !== '' && val !== undefined) params.append(key, val)
      }
      simulate.requestBody.value = params.toString()
      simulate.sendRequest(mergedHeaders.value)
    }
  } else {
    simulate.sendRequest(mergedHeaders.value)
  }
}

function handleReset() {
  // Clear UI state for current item
  const key = simulate.itemKey(props.item)
  uiCache.delete(key)
  formFieldValues.value = {}
  formFileValues.value = {}
  customHeaders.value = []
  jsonBodyInitialized.value = false
  // reset() removes the composable's snapshot and re-inits defaults
  simulate.reset()
  // re-init JSON body since reset cleared it
  initJsonBody()
}

// Initialize JSON body from schema if not yet done for this item
function initJsonBody() {
  if (jsonBodyInitialized.value) return
  if (isJson.value && requestBodySchema.value) {
    // Only set if still empty (composable may have already restored a cached value)
    if (!simulate.requestBody.value) {
      simulate.requestBody.value = generateJsonSchemaExample(requestBodySchema.value, props.schemas)
    }
    jsonBodyInitialized.value = true
  }
}

watch(simulatePanelCollapsed, (collapsed) => {
  if (!collapsed) initJsonBody()
})

// When the user switches to a different operation:
// 1. Save current UI snapshot (formFields, customHeaders)
// 2. Composable's own watch handles params + requestBody save/restore
// 3. Restore UI snapshot for the new operation
watch(() => props.item, (newVal, oldVal) => {
  const newKey = newVal ? `${newVal.method}-${newVal.path}` : ''
  const oldKey = oldVal ? `${oldVal.method}-${oldVal.path}` : ''
  if (newKey === oldKey) return

  // Save outgoing UI state
  if (oldKey) saveUiSnapshot(oldKey)

  // Restore incoming UI state (or reset to defaults)
  restoreUiSnapshot(newKey)

  // File inputs can't be serialized — always reset
  formFileValues.value = {}

  // Reset JSON init flag so initJsonBody() can run for the new item
  // (but don't clear requestBody — composable already restored/initialized it)
  jsonBodyInitialized.value = !!simulate.requestBody.value

  if (!simulatePanelCollapsed.value) initJsonBody()
}, { deep: false })

// If the spec reloads and schema changes, re-generate only if body is empty
watch(requestBodySchema, () => {
  if (!simulate.requestBody.value) {
    jsonBodyInitialized.value = false
    if (!simulatePanelCollapsed.value) initJsonBody()
  }
})

defineExpose({
  simulate, simulateParams, simulateResult, isJson, isForm,
  requestBodySchema, schemaFields, simulatePanelCollapsed,
  customHeaders, handleSendRequest, handleReset,
  addCustomHeader, removeCustomHeader, formFieldValues, formFileValues,
})
</script>

<template>
  <div class="mb-5 overflow-hidden rounded-xl border border-[var(--c-border)] bg-white shadow-sm">
    <!-- Panel Header -->
    <div
      class="flex cursor-pointer items-center justify-between bg-[var(--c-bg)] px-4 py-3 select-none transition-colors hover:bg-gray-100/60"
      @click="simulatePanelCollapsed = !simulatePanelCollapsed"
    >
      <div class="flex items-center gap-2.5">
        <svg
          class="h-3.5 w-3.5 text-[var(--c-muted)] transition-transform duration-200"
          :class="simulatePanelCollapsed ? '-rotate-90' : ''"
          viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"
        >
          <path d="m6 9 6 6 6-6" />
        </svg>
        <svg class="h-3.5 w-3.5 text-[var(--c-primary)]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <polygon points="5 3 19 12 5 21 5 3" />
        </svg>
        <span class="text-[12.5px] font-semibold text-[var(--c-text)]">调试请求</span>
        <span v-if="simulate.loading.value" class="text-[11px] text-[var(--c-primary)]">请求中...</span>
      </div>
      <div class="flex items-center gap-2" @click.stop>
        <button
          class="rounded-lg border border-[var(--c-border)] bg-white px-3 py-1.5 text-[12px] text-[var(--c-text)] transition-colors hover:bg-gray-50"
          @click="handleReset()"
        >重置</button>
        <button
          class="rounded-lg bg-[var(--c-primary)] px-4 py-1.5 text-[12px] font-semibold text-white transition-all hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="simulate.loading.value"
          @click="handleSendRequest()"
        >{{ simulate.loading.value ? '发送中...' : '发送请求' }}</button>
      </div>
    </div>

    <div v-if="!simulatePanelCollapsed">
      <div class="divide-y divide-[var(--c-border)]">

        <!-- Path / Query Params -->
        <div v-if="simulateParams.value.length" class="px-4 py-4">
          <p class="mb-3 text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]">路径 / 查询参数</p>
          <div class="space-y-2">
            <div v-for="param in simulateParams.value" :key="param.name" class="flex items-center gap-2">
              <span class="w-32 shrink-0 truncate font-mono text-[12px] font-medium text-[var(--c-text)]" :title="param.name">{{ param.name }}</span>
              <span
                class="w-14 shrink-0 rounded-md px-1.5 py-0.5 text-center text-[10px] font-semibold"
                :class="{
                  'bg-blue-100 text-blue-700': param.in === 'path',
                  'bg-green-100 text-green-700': param.in === 'query',
                  'bg-purple-100 text-purple-700': param.in === 'header',
                  'bg-amber-100 text-amber-700': param.in === 'cookie',
                }"
              >{{ param.in }}</span>
              <input
                :value="param.value"
                @input="param.value = ($event.target as HTMLInputElement).value"
                class="flex-1 rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-1.5 text-[12px] outline-none transition-colors focus:border-[var(--c-primary)] focus:bg-white"
                :placeholder="param.example !== undefined ? String(param.example) : (param.type || 'Value')"
              />
              <span v-if="param.required" class="shrink-0 text-[10px] font-semibold text-red-500">必填</span>
            </div>
          </div>
        </div>

        <!-- Custom Headers -->
        <div class="px-4 py-4">
          <div class="mb-3 flex items-center justify-between">
            <p class="text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]">自定义请求头</p>
            <button
              class="rounded-md border border-[var(--c-border)] bg-white px-2.5 py-1 text-[11px] font-medium text-[var(--c-text)] transition-colors hover:bg-gray-50"
              @click="addCustomHeader"
            >+ 添加</button>
          </div>
          <div v-if="customHeaders.length" class="space-y-2">
            <div v-for="(header, index) in customHeaders" :key="index" class="flex items-center gap-2">
              <input v-model="header.name"
                class="w-36 shrink-0 rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-1.5 text-[12px] outline-none focus:border-[var(--c-primary)] focus:bg-white"
                placeholder="Header Name" />
              <input v-model="header.value"
                class="flex-1 rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-1.5 text-[12px] outline-none focus:border-[var(--c-primary)] focus:bg-white"
                placeholder="Header Value" />
              <button
                class="shrink-0 rounded-md border border-red-200 bg-red-50 px-2.5 py-1.5 text-[11px] font-medium text-red-600 transition-colors hover:bg-red-100"
                @click="removeCustomHeader(index)"
              >删除</button>
            </div>
          </div>
          <p v-else class="text-[11px] text-[var(--c-muted)]">暂无自定义请求头</p>
        </div>

        <!-- Request Body -->
        <div v-if="requestBodySchema" class="px-4 py-4">
          <div class="mb-3 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <p class="text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]">请求体</p>
              <span class="rounded-md bg-gray-100 px-2 py-0.5 font-mono text-[10px] text-[var(--c-muted)]">{{ simulate.contentType.value }}</span>
              <span v-if="item.operation.requestBody?.required" class="text-[10px] font-semibold text-red-500">必填</span>
            </div>
            <button
              class="rounded-md border border-[var(--c-border)] bg-white px-2.5 py-1 text-[11px] font-medium text-[var(--c-text)] transition-colors hover:bg-gray-50"
              @click="() => { simulate.resetRequestBody(); formFieldValues = {} }"
            >重置</button>
          </div>

          <!-- Form -->
          <div v-if="isForm">
            <div class="overflow-hidden rounded-xl border border-[var(--c-border)]">
              <table class="w-full border-collapse text-[12px]">
                <thead>
                  <tr class="border-b border-[var(--c-border)] bg-gray-50">
                    <th class="px-3 py-2 text-left text-[11px] font-semibold text-[var(--c-muted)]">字段名</th>
                    <th class="px-3 py-2 text-left text-[11px] font-semibold text-[var(--c-muted)]">类型</th>
                    <th class="px-3 py-2 text-left text-[11px] font-semibold text-[var(--c-muted)]">必填</th>
                    <th class="px-3 py-2 text-left text-[11px] font-semibold text-[var(--c-muted)]">说明</th>
                    <th class="px-3 py-2 text-left text-[11px] font-semibold text-[var(--c-muted)]">值</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-[var(--c-border)]">
                  <tr v-for="field in schemaFields" :key="field.name" class="bg-white">
                    <td class="px-3 py-2 font-mono text-[12px] font-medium text-[var(--c-text)]">{{ field.name }}</td>
                    <td class="px-3 py-2 font-mono text-[11px] text-[var(--c-muted)]">
                      <span v-if="field.isBinary" class="rounded-md bg-purple-100 px-1.5 py-0.5 text-[10px] font-semibold text-purple-700">
                        {{ field.isArray ? 'file[]' : 'file' }}
                      </span>
                      <span v-else>{{ field.type }}</span>
                    </td>
                    <td class="px-3 py-2">
                      <span v-if="field.required" class="text-[11px] font-semibold text-red-500">必填</span>
                      <span v-else class="text-[11px] text-[var(--c-muted)]">-</span>
                    </td>
                    <td class="max-w-[120px] truncate px-3 py-2 text-[11px] text-[var(--c-muted)]" :title="field.description">
                      {{ field.description || '-' }}
                    </td>
                    <td class="px-3 py-2">
                      <input v-if="field.isBinary" type="file" :multiple="field.isArray"
                        class="w-full text-[12px] text-[var(--c-text)] file:mr-2 file:rounded file:border file:border-[var(--c-border)] file:bg-gray-50 file:px-2 file:py-0.5 file:text-[11px]"
                        @change="formFileValues[field.name] = ($event.target as HTMLInputElement).files" />
                      <select v-else-if="field.enum && field.enum.length" v-model="formFieldValues[field.name]"
                        class="w-full rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-2 py-1.5 text-[12px] outline-none focus:border-[var(--c-primary)]">
                        <option value="">-- 请选择 --</option>
                        <option v-for="opt in field.enum" :key="opt" :value="opt">{{ opt }}</option>
                      </select>
                      <select v-else-if="field.type === 'boolean'" v-model="formFieldValues[field.name]"
                        class="w-full rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-2 py-1.5 text-[12px] outline-none focus:border-[var(--c-primary)]">
                        <option value="">-- 请选择 --</option>
                        <option value="true">true</option>
                        <option value="false">false</option>
                      </select>
                      <input v-else v-model="formFieldValues[field.name]"
                        class="w-full rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-2 py-1.5 text-[12px] outline-none focus:border-[var(--c-primary)]"
                        :placeholder="field.example || field.type" />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- JSON editor -->
          <div v-else-if="isJson">
            <div v-if="schemaFields.length" class="mb-3 overflow-hidden rounded-xl border border-[var(--c-border)]">
              <table class="w-full border-collapse text-[12px]">
                <thead>
                  <tr class="border-b border-[var(--c-border)] bg-gray-50">
                    <th class="px-3 py-2 text-left text-[11px] font-semibold text-[var(--c-muted)]">字段名</th>
                    <th class="px-3 py-2 text-left text-[11px] font-semibold text-[var(--c-muted)]">类型</th>
                    <th class="px-3 py-2 text-left text-[11px] font-semibold text-[var(--c-muted)]">必填</th>
                    <th class="px-3 py-2 text-left text-[11px] font-semibold text-[var(--c-muted)]">示例值</th>
                    <th class="px-3 py-2 text-left text-[11px] font-semibold text-[var(--c-muted)]">说明</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-[var(--c-border)]">
                  <tr v-for="field in schemaFields" :key="field.name" class="bg-white">
                    <td class="px-3 py-2 font-mono text-[12px] font-medium text-[var(--c-text)]">{{ field.name }}</td>
                    <td class="px-3 py-2 font-mono text-[11px] text-[var(--c-muted)]">{{ field.type }}</td>
                    <td class="px-3 py-2">
                      <span v-if="field.required" class="text-[11px] font-semibold text-red-500">必填</span>
                      <span v-else class="text-[11px] text-[var(--c-muted)]">-</span>
                    </td>
                    <td class="px-3 py-2 font-mono text-[11px] text-[var(--c-muted)]">
                      <span v-if="field.enum" class="text-[10px]">{{ field.enum.join(' | ') }}</span>
                      <span v-else>{{ field.example || '-' }}</span>
                    </td>
                    <td class="max-w-[160px] truncate px-3 py-2 text-[11px] text-[var(--c-muted)]" :title="field.description">
                      {{ field.description || '-' }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <textarea
              v-model="simulate.requestBody.value"
              rows="10"
              class="w-full resize-y rounded-xl border border-[var(--c-border)] bg-gray-50 p-3 font-mono text-[12px] leading-relaxed outline-none transition-colors focus:border-[var(--c-primary)] focus:bg-white"
              spellcheck="false"
              placeholder="{}"
            />
            <p class="mt-1.5 text-[10px] text-[var(--c-muted)]">💡 已根据 Schema 自动生成示例，可直接修改后发送</p>
          </div>

          <!-- Other content types -->
          <div v-else>
            <textarea
              v-model="simulate.requestBody.value"
              rows="5"
              class="w-full resize-y rounded-xl border border-[var(--c-border)] bg-gray-50 p-3 font-mono text-[12px] outline-none transition-colors focus:border-[var(--c-primary)] focus:bg-white"
              placeholder="Request body"
            />
          </div>
        </div>
      </div>

      <!-- Response Result -->
      <div v-if="simulateResult.value || simulate.error.value" class="border-t border-[var(--c-border)] bg-[var(--c-bg)] px-4 py-4">
        <div class="mb-3 flex items-center gap-2.5">
          <p class="text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]">响应结果</p>
          <span v-if="simulateResult.value"
            class="rounded-md px-2.5 py-0.5 font-mono text-[11px] font-bold"
            :class="{
              'bg-green-100 text-green-800': simulateResult.value.status < 300,
              'bg-blue-100 text-blue-800': simulateResult.value.status >= 300 && simulateResult.value.status < 400,
              'bg-amber-100 text-amber-800': simulateResult.value.status >= 400 && simulateResult.value.status < 500,
              'bg-red-100 text-red-800': simulateResult.value.status >= 500,
            }"
          >{{ simulateResult.value.status }} {{ simulateResult.value.statusText }}</span>
          <span v-if="simulateResult.value" class="text-[11px] text-[var(--c-muted)]">{{ simulateResult.value.duration }}ms</span>
        </div>

        <div v-if="simulate.error.value" class="rounded-xl border border-red-200 bg-red-50 p-3 text-[12px] text-red-700">
          {{ simulate.error.value }}
        </div>

        <div v-if="simulateResult.value?.headers" class="mb-3 overflow-hidden rounded-xl border border-[var(--c-border)] bg-white">
          <div class="border-b border-[var(--c-border)] bg-gray-50 px-3 py-2">
            <p class="text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]">响应头</p>
          </div>
          <div class="px-3 py-2">
            <div v-for="(value, key) in simulateResult.value.headers" :key="key" class="flex gap-3 py-0.5 text-[11px]">
              <span class="w-36 shrink-0 font-mono text-[var(--c-muted)]">{{ key }}</span>
              <span class="flex-1 truncate font-mono text-[var(--c-text)]">{{ value }}</span>
            </div>
          </div>
        </div>

        <div v-if="simulateResult.value?.data !== undefined" class="overflow-hidden rounded-xl border border-[var(--c-border)] bg-white">
          <div class="border-b border-[var(--c-border)] bg-gray-50 px-3 py-2">
            <p class="text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]">响应体</p>
          </div>
          <pre class="max-h-[400px] overflow-auto p-3 text-[12px] leading-relaxed">{{ typeof simulateResult.value.data === 'string' ? simulateResult.value.data : JSON.stringify(simulateResult.value.data, null, 2) }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>