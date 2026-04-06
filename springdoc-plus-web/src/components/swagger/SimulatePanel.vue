<script setup lang="ts">
import { computed, ref, toRef, watch } from 'vue'
import type { OperationItem, SchemaObject } from '@/types/openapi'
import type { AuthHeader } from '@/types'
import {
  getRequestBodyOptions,
  resolveRequestBodyOption,
  useSimulateRequest,
} from '@/composables/useSimulateRequest'
import SimulateResponsePanel from './SimulateResponsePanel.vue'
import {
  getParamLocationClass,
  isBinaryField,
  isFormContentType,
  isJsonContentType,
  resolveSchemaRef,
  CONTENT_TYPE,
} from '@/utils/schema'
import { readStorage, writeStorage, removeStorage } from '@/utils/storage'

const props = defineProps<{
  item: OperationItem
  schemas?: Record<string, SchemaObject>
  contextPath?: string
  authHeaders?: AuthHeader[]
}>()

type ActiveTab = 'params' | 'headers' | 'body' | 'response'

interface UiSnapshot {
  customHeaders: Array<{ name: string; value: string }>
  formFieldValues: Record<string, string>
  activeTab: ActiveTab
  collapsed: boolean
}

interface SchemaFieldMeta {
  name: string
  type: string
  isArray: boolean
  isBinary: boolean
  required: boolean
  description: string
  example: string
  enum?: string[]
}

const UI_STORAGE_PREFIX = 'springdoc-plus:simulate:ui:'
const uiCache = new Map<string, UiSnapshot>()

const simulate = useSimulateRequest(
  toRef(() => props.item),
  toRef(() => props.contextPath),
  toRef(() => props.schemas)
)

const customHeaders = ref<Array<{ name: string; value: string }>>([])
const formFieldValues = ref<Record<string, string>>({})
const formFileValues = ref<Record<string, File[]>>({})
const activeTab = ref<ActiveTab>('params')
const collapsed = ref(false)

const operationKey = computed(() => simulate.itemKey(props.item))
const availableContentTypes = computed(() => getRequestBodyOptions(props.item).map(option => option.effectiveType))
const requestBodyOption = computed(() => resolveRequestBodyOption(props.item, simulate.contentType.value))
const requestBodySchema = computed(() => requestBodyOption.value?.schema ?? null)
const hasRequestBodyEditor = computed(() => availableContentTypes.value.length > 0)
const isJson = computed(() => isJsonContentType(simulate.contentType.value))
const isForm = computed(() => isFormContentType(simulate.contentType.value))

const schemaFields = computed<SchemaFieldMeta[]>(() => {
  const resolved = resolveSchemaRef(requestBodySchema.value, props.schemas)
  if (!resolved?.properties) return []
  const required = resolved.required ?? []
  return Object.entries(resolved.properties).map(([name, fieldSchema]) => {
    const field = resolveSchemaRef(fieldSchema as SchemaObject, props.schemas)
    const isArray = field?.type === 'array'
    return {
      name,
      type: isArray ? 'array' : (field?.type ?? 'string'),
      isArray,
      isBinary: isBinaryField(field),
      required: required.includes(name),
      description: field?.description ?? '',
      example: String(field?.example ?? field?.default ?? ''),
      enum: field?.enum?.map(value => String(value)),
    }
  })
})

const schemaHint = computed(() => {
  const resolved = resolveSchemaRef(requestBodySchema.value, props.schemas)
  if (resolved?.additionalProperties) return '这是一个动态对象请求体，可以直接在下方编辑 JSON。'
  if (isJson.value && !schemaFields.value.length) return '当前没有固定字段约束，可以直接编辑原始请求体。'
  return ''
})

const tabs = computed(() => {
  const items: Array<{ key: ActiveTab; label: string; icon: string }> = [
    { key: 'params', label: '参数', icon: 'M4 6h16M4 12h16M4 18h7' },
    { key: 'headers', label: '请求头', icon: 'M20 7H4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z' },
  ]
  if (hasRequestBodyEditor.value) {
    items.push({ key: 'body', label: '请求体', icon: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z' })
  }
  items.push({ key: 'response', label: '响应', icon: 'M22 12 18 12 15 21 9 3 6 12 2 12' })
  return items
})

const mergedHeaders = computed(() => {
  const headerMap = new Map<string, string>()
  for (const header of props.authHeaders ?? []) {
    if (!header.name) continue
    const name = header.name.trim()
    let value = (header.value ?? '').trim()
    if (!value) continue
    const prefix = (header.defaultPrefix ?? '').trim()
    if (prefix && !value.startsWith(`${prefix} `)) value = `${prefix} ${value}`
    headerMap.set(name.toLowerCase(), value)
  }
  for (const header of customHeaders.value) {
    if (!header.name || !header.value) continue
    headerMap.set(header.name.trim().toLowerCase(), header.value.trim())
  }
  return Array.from(headerMap.entries()).map(([name, value]) => ({ name, value }))
})

function uiStorageKey(key: string): string { return `${UI_STORAGE_PREFIX}${key}` }
function getDefaultTab(): ActiveTab { return hasRequestBodyEditor.value ? 'body' : 'params' }

function restoreUiSnapshot(key: string) {
  const snapshot = uiCache.get(key) ?? readStorage<UiSnapshot>(uiStorageKey(key))
  formFileValues.value = {}
  if (!snapshot) {
    customHeaders.value = []; formFieldValues.value = {}; activeTab.value = getDefaultTab(); collapsed.value = false; return
  }
  customHeaders.value = snapshot.customHeaders.map(h => ({ ...h }))
  formFieldValues.value = { ...snapshot.formFieldValues }
  activeTab.value = tabs.value.some(t => t.key === snapshot.activeTab) ? snapshot.activeTab : getDefaultTab()
  collapsed.value = snapshot.collapsed
  uiCache.set(key, { customHeaders: customHeaders.value.map(h => ({ ...h })), formFieldValues: { ...formFieldValues.value }, activeTab: activeTab.value, collapsed: collapsed.value })
}

function saveUiSnapshot() {
  const snapshot: UiSnapshot = { customHeaders: customHeaders.value.map(h => ({ ...h })), formFieldValues: { ...formFieldValues.value }, activeTab: activeTab.value, collapsed: collapsed.value }
  uiCache.set(operationKey.value, snapshot); writeStorage(uiStorageKey(operationKey.value), snapshot)
}

function addCustomHeader() { customHeaders.value.push({ name: '', value: '' }) }
function removeCustomHeader(index: number) { customHeaders.value.splice(index, 1) }
function handleContentTypeChange(nextType: string) { simulate.setContentType(nextType); formFieldValues.value = {}; formFileValues.value = {} }
function resetCurrentRequestBody() { simulate.resetRequestBody(); formFieldValues.value = {}; formFileValues.value = {} }
function handleReset() {
  uiCache.delete(operationKey.value); removeStorage(uiStorageKey(operationKey.value))
  customHeaders.value = []; formFieldValues.value = {}; formFileValues.value = {}
  activeTab.value = getDefaultTab(); collapsed.value = false; simulate.reset()
}
function buildFormData(): FormData {
  const formData = new FormData()
  for (const field of schemaFields.value) {
    if (field.isBinary) { const files = formFileValues.value[field.name]; if (!files?.length) continue; if (field.isArray) { for (const file of files) formData.append(field.name, file) } else formData.append(field.name, files[0]); continue }
    const value = formFieldValues.value[field.name]; if (value !== undefined && value !== '') formData.append(field.name, value)
  }
  return formData
}
function handleFileChange(fieldName: string, event: Event) {
  const input = event.target as HTMLInputElement; formFileValues.value[fieldName] = input.files ? Array.from(input.files) : []
}
function buildFormUrlEncodedBody(): string {
  const sp = new URLSearchParams()
  for (const [name, value] of Object.entries(formFieldValues.value)) { if (value !== undefined && value !== '') sp.append(name, value) }
  return sp.toString()
}
function handleSendRequest() {
  if (isForm.value) {
    const hasBinaryField = schemaFields.value.some(f => f.isBinary)
    if (simulate.contentType.value.includes(CONTENT_TYPE.MULTIPART_FORM_DATA) || hasBinaryField) { simulate.sendRequest(mergedHeaders.value, buildFormData()); activeTab.value = 'response'; return }
    simulate.requestBody.value = buildFormUrlEncodedBody()
  }
  simulate.sendRequest(mergedHeaders.value); activeTab.value = 'response'
}

watch(operationKey, (key) => { restoreUiSnapshot(key) }, { immediate: true })
watch([customHeaders, formFieldValues, activeTab, collapsed], saveUiSnapshot, { deep: true })
watch(hasRequestBodyEditor, (enabled) => { if (!enabled && activeTab.value === 'body') activeTab.value = 'params' })

defineExpose({ simulate, customHeaders, formFieldValues, formFileValues, handleSendRequest, handleReset })
</script>

<template>
  <div class="flex h-full flex-col">
    <!-- Panel Header -->
    <div class="flex items-center gap-2.5 border-b border-[var(--c-border)] bg-[var(--c-bg)] px-4 py-3">
      <div class="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-[var(--c-primary)] text-white shadow-sm">
        <svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
          <polygon points="5 3 19 12 5 21 5 3" />
        </svg>
      </div>
      <span class="text-[13px] font-semibold text-[var(--c-text)]">调试请求</span>

      <div class="ml-auto flex items-center gap-1.5">
        <!-- Reset -->
        <button
          class="inline-flex h-7 w-7 items-center justify-center rounded-lg border border-[var(--c-border)] bg-white text-[var(--c-muted)] transition-colors hover:bg-[var(--c-bg)] hover:text-[var(--c-text)]"
          title="重置调试面板"
          @click="handleReset"
        >
          <svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M3 12a9 9 0 1 0 3-6.7" /><path d="M3 3v6h6" />
          </svg>
        </button>
        <!-- Collapse -->
        <button
          class="inline-flex h-7 w-7 items-center justify-center rounded-lg border border-[var(--c-border)] bg-white text-[var(--c-muted)] transition-colors hover:bg-[var(--c-bg)] hover:text-[var(--c-text)]"
          :title="collapsed ? '展开' : '收起'"
          @click="collapsed = !collapsed"
        >
          <svg class="h-3.5 w-3.5 transition-transform" :class="collapsed ? 'rotate-180' : 'rotate-0'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="m6 9 6 6 6-6" />
          </svg>
        </button>
      </div>
    </div>

    <div v-if="!collapsed" class="flex flex-1 flex-col gap-0">
      <!-- Tab Nav -->
      <div class="flex border-b border-[var(--c-border)] bg-[var(--c-bg)]">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="relative flex items-center gap-1.5 px-3.5 py-2.5 text-[12px] font-medium transition-colors"
          :class="activeTab === tab.key ? 'text-[var(--c-primary)]' : 'text-[var(--c-muted)] hover:text-[var(--c-text)]'"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
          <span
            v-if="tab.key === 'response' && simulate.result.value"
            class="h-1.5 w-1.5 rounded-full"
            :class="simulate.result.value.status < 400 ? 'bg-emerald-500' : 'bg-rose-500'"
          />
          <!-- 激活下划线：用独立元素替代 after: 伪元素，避免 Tailwind v4 动态 class 解析限制 -->
          <span
            v-if="activeTab === tab.key"
            class="absolute bottom-0 left-0 right-0 h-[2px] bg-[var(--c-primary)]"
          />
        </button>
      </div>

      <!-- Tab Content -->
      <div class="flex-1 px-4 py-3">

        <!-- Params Tab -->
        <div v-show="activeTab === 'params'">
          <div v-if="simulate.params.value.length" class="space-y-2">
            <div
              v-for="param in simulate.params.value"
              :key="`${param.in}-${param.name}`"
              class="rounded-xl border border-[var(--c-border)] bg-white p-3"
            >
              <div class="mb-2 flex flex-wrap items-center gap-1.5">
                <span class="font-mono text-[12px] font-semibold text-[var(--c-text)]">{{ param.name }}</span>
                <span class="rounded-md px-1.5 py-0.5 text-[10px] font-semibold" :class="getParamLocationClass(param.in)">{{ param.in }}</span>
                <span v-if="param.required" class="rounded-full bg-rose-50 px-1.5 py-0.5 text-[10px] font-bold text-rose-600">必填</span>
                <span v-if="param.type" class="ml-auto rounded bg-[var(--c-bg)] px-1.5 py-0.5 font-mono text-[10px] text-[var(--c-muted)]">{{ param.type }}</span>
              </div>
              <p v-if="param.description" class="mb-1.5 text-[11px] text-[var(--c-muted)]">{{ param.description }}</p>
              <input
                v-model="param.value"
                class="h-8 w-full rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-3 text-[12px] text-[var(--c-text)] outline-none transition-colors focus:border-[var(--c-primary)] focus:bg-white"
                :placeholder="param.example || param.type || '请输入参数值'"
              />
            </div>
          </div>
          <p v-else class="rounded-xl border border-dashed border-[var(--c-border)] px-3 py-6 text-center text-[12px] text-[var(--c-muted)]">
            当前接口没有请求参数
          </p>
        </div>

        <!-- Headers Tab -->
        <div v-show="activeTab === 'headers'">
          <div class="mb-3 flex items-center justify-between">
            <span class="text-[12px] font-semibold text-[var(--c-text)]">附加请求头</span>
            <button
              class="rounded-lg border border-[var(--c-border)] bg-white px-2.5 py-1 text-[11px] font-medium text-[var(--c-text)] transition-colors hover:bg-[var(--c-bg)]"
              @click="addCustomHeader"
            >
              + 添加
            </button>
          </div>
          <div v-if="customHeaders.length" class="space-y-2">
            <div
              v-for="(header, index) in customHeaders"
              :key="index"
              class="flex gap-2"
            >
              <input
                v-model="header.name"
                class="h-8 w-[140px] shrink-0 rounded-lg border border-[var(--c-border)] bg-white px-2.5 text-[12px] text-[var(--c-text)] outline-none focus:border-[var(--c-primary)]"
                placeholder="Header 名"
              />
              <input
                v-model="header.value"
                class="h-8 min-w-0 flex-1 rounded-lg border border-[var(--c-border)] bg-white px-2.5 text-[12px] text-[var(--c-text)] outline-none focus:border-[var(--c-primary)]"
                placeholder="值"
              />
              <button
                class="h-8 rounded-lg border border-rose-200 bg-white px-2.5 text-[12px] text-rose-500 transition-colors hover:bg-rose-50"
                @click="removeCustomHeader(index)"
              >✕</button>
            </div>
          </div>
          <p v-else class="rounded-xl border border-dashed border-[var(--c-border)] px-3 py-6 text-center text-[12px] text-[var(--c-muted)]">
            没有额外的请求头
          </p>
        </div>

        <!-- Body Tab -->
        <div v-show="activeTab === 'body' && hasRequestBodyEditor">
          <div class="mb-3 flex flex-wrap items-center justify-between gap-2">
            <span class="text-[12px] font-semibold text-[var(--c-text)]">请求体</span>
            <div class="flex items-center gap-2">
              <select
                v-if="availableContentTypes.length > 1"
                :value="simulate.contentType.value"
                class="h-7 rounded-lg border border-[var(--c-border)] bg-white px-2 text-[11px] text-[var(--c-text)] outline-none focus:border-[var(--c-primary)]"
                @change="handleContentTypeChange(($event.target as HTMLSelectElement).value)"
              >
                <option v-for="type in availableContentTypes" :key="type" :value="type">{{ type }}</option>
              </select>
              <span
                v-else
                class="rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-2 py-1 font-mono text-[10px] text-[var(--c-muted)]"
              >
                {{ simulate.contentType.value }}
              </span>
              <button
                class="h-7 rounded-lg border border-[var(--c-border)] bg-white px-2.5 text-[11px] text-[var(--c-text)] transition-colors hover:bg-[var(--c-bg)]"
                @click="resetCurrentRequestBody"
              >重置</button>
            </div>
          </div>

          <p v-if="schemaHint" class="mb-3 rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-2 text-[11px] leading-5 text-[var(--c-muted)]">
            {{ schemaHint }}
          </p>
          <p v-if="isForm && schemaFields.some(f => f.isBinary)" class="mb-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-[11px] leading-5 text-amber-700">
            已选文件在当前页面内保留；刷新后浏览器会清空文件选择。
          </p>

          <!-- Form fields -->
          <div v-if="isForm && schemaFields.length" class="space-y-2">
            <div
              v-for="field in schemaFields"
              :key="field.name"
              class="rounded-xl border border-[var(--c-border)] bg-white p-3"
            >
              <div class="mb-2 flex flex-wrap items-center gap-1.5">
                <span class="font-mono text-[12px] font-semibold text-[var(--c-text)]">{{ field.name }}</span>
                <span class="rounded bg-[var(--c-bg)] px-1.5 py-0.5 font-mono text-[10px] text-[var(--c-muted)]">{{ field.isBinary ? (field.isArray ? 'file[]' : 'file') : field.type }}</span>
                <span v-if="field.required" class="rounded-full bg-rose-50 px-1.5 py-0.5 text-[10px] font-bold text-rose-600">必填</span>
              </div>
              <p v-if="field.description" class="mb-1.5 text-[11px] text-[var(--c-muted)]">{{ field.description }}</p>

              <input v-if="field.isBinary" type="file" :multiple="field.isArray" class="block w-full rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-1.5 text-[12px] file:mr-2 file:rounded-full file:border-0 file:bg-[var(--c-primary-light)] file:px-2.5 file:py-1 file:text-[11px] file:font-medium file:text-[var(--c-primary)]" @change="handleFileChange(field.name, $event)" />
              <div v-if="field.isBinary && formFileValues[field.name]?.length" class="mt-1 rounded-lg bg-[var(--c-bg)] px-3 py-1.5 text-[11px] text-[var(--c-muted)]">{{ formFileValues[field.name].map(f => f.name).join(' , ') }}</div>
              <select v-else-if="field.enum?.length" v-model="formFieldValues[field.name]" class="w-full rounded-lg border border-[var(--c-border)] bg-white px-3 py-1.5 text-[12px] text-[var(--c-text)] outline-none focus:border-[var(--c-primary)]">
                <option value="">请选择</option>
                <option v-for="option in field.enum" :key="option" :value="option">{{ option }}</option>
              </select>
              <select v-else-if="field.type === 'boolean'" v-model="formFieldValues[field.name]" class="w-full rounded-lg border border-[var(--c-border)] bg-white px-3 py-1.5 text-[12px] text-[var(--c-text)] outline-none focus:border-[var(--c-primary)]">
                <option value="">请选择</option>
                <option value="true">true</option>
                <option value="false">false</option>
              </select>
              <input v-else v-model="formFieldValues[field.name]" class="w-full rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-1.5 text-[12px] text-[var(--c-text)] outline-none transition-colors focus:border-[var(--c-primary)] focus:bg-white" :placeholder="field.example || field.type || '请输入字段值'" />
              <p v-if="field.example" class="mt-1 text-[10px] text-[var(--c-muted)]">示例：{{ field.example }}</p>
            </div>
          </div>

          <!-- JSON textarea with schema hints -->
          <div v-else class="space-y-3">
            <div v-if="schemaFields.length" class="grid grid-cols-2 gap-1.5">
              <div v-for="field in schemaFields" :key="field.name" class="rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-2.5 py-2">
                <div class="flex flex-wrap items-center gap-1">
                  <span class="font-mono text-[11px] font-semibold text-[var(--c-text)]">{{ field.name }}</span>
                  <span class="rounded bg-white px-1 py-0.5 font-mono text-[9px] text-[var(--c-muted)]">{{ field.type }}</span>
                  <span v-if="field.required" class="rounded-full bg-rose-50 px-1 py-0.5 text-[9px] font-bold text-rose-600">*</span>
                </div>
                <p v-if="field.description" class="mt-0.5 line-clamp-1 text-[10px] text-[var(--c-muted)]">{{ field.description }}</p>
              </div>
            </div>
            <textarea
              v-model="simulate.requestBody.value"
              :rows="isJson ? 10 : 6"
              class="min-h-[120px] w-full resize-y rounded-xl border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-3 font-mono text-[12px] leading-6 text-[var(--c-text)] outline-none transition-colors focus:border-[var(--c-primary)] focus:bg-white"
              spellcheck="false"
              :placeholder="isJson ? '{\n  \n}' : '请输入请求体内容'"
            />
          </div>
        </div>

        <!-- Response Tab -->
        <div v-show="activeTab === 'response'">
          <SimulateResponsePanel
            :result="simulate.result.value"
            :error="simulate.error.value"
          />
          <p v-if="!simulate.result.value && !simulate.error.value" class="rounded-xl border border-dashed border-[var(--c-border)] px-3 py-6 text-center text-[12px] text-[var(--c-muted)]">
            点击「发送请求」后响应结果会显示在这里
          </p>
        </div>
      </div>

      <!-- Send Button — always visible at bottom -->
      <div class="border-t border-[var(--c-border)] bg-[var(--c-bg)] px-4 py-3">
        <button
          class="flex w-full items-center justify-center gap-2 rounded-xl bg-[var(--c-primary)] py-2.5 text-[13px] font-semibold text-white shadow-sm transition-all hover:bg-[var(--c-primary-hover)] hover:shadow-md disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="simulate.loading.value"
          @click="handleSendRequest"
        >
          <svg v-if="!simulate.loading.value" class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
            <polygon points="5 3 19 12 5 21 5 3" />
          </svg>
          <svg v-else class="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M21 12a9 9 0 1 1-6.219-8.56" />
          </svg>
          {{ simulate.loading.value ? '发送中...' : '发送请求' }}
        </button>
      </div>
    </div>
  </div>
</template>
