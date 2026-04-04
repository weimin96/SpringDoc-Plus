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
  if (resolved?.additionalProperties) {
    return '这是一个动态对象请求体，可以直接在下方编辑 JSON。'
  }
  if (isJson.value && !schemaFields.value.length) {
    return '当前没有固定字段约束，可以直接编辑原始请求体。'
  }
  return ''
})

const tabs = computed(() => {
  const items: Array<{ key: ActiveTab; label: string }> = [
    { key: 'params', label: '请求参数' },
    { key: 'headers', label: '附加请求头' },
  ]
  if (hasRequestBodyEditor.value) {
    items.push({ key: 'body', label: '请求体' })
  }
  items.push({ key: 'response', label: '响应' })
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
    if (prefix && !value.startsWith(`${prefix} `)) {
      value = `${prefix} ${value}`
    }

    headerMap.set(name.toLowerCase(), value)
  }

  for (const header of customHeaders.value) {
    if (!header.name || !header.value) continue
    headerMap.set(header.name.trim().toLowerCase(), header.value.trim())
  }

  return Array.from(headerMap.entries()).map(([name, value]) => ({ name, value }))
})

function uiStorageKey(key: string): string {
  return `${UI_STORAGE_PREFIX}${key}`
}

function readUiStorage(key: string): UiSnapshot | null {
  try {
    const raw = window.localStorage.getItem(uiStorageKey(key))
    if (!raw) return null
    return JSON.parse(raw) as UiSnapshot
  } catch {
    return null
  }
}

function writeUiStorage(key: string, snapshot: UiSnapshot) {
  try {
    window.localStorage.setItem(uiStorageKey(key), JSON.stringify(snapshot))
  } catch {
    // Ignore storage failures.
  }
}

function removeUiStorage(key: string) {
  try {
    window.localStorage.removeItem(uiStorageKey(key))
  } catch {
    // Ignore storage failures.
  }
}

function getDefaultTab(): ActiveTab {
  return hasRequestBodyEditor.value ? 'body' : 'params'
}

function restoreUiSnapshot(key: string) {
  const snapshot = uiCache.get(key) ?? readUiStorage(key)
  formFileValues.value = {}

  if (!snapshot) {
    customHeaders.value = []
    formFieldValues.value = {}
    activeTab.value = getDefaultTab()
    collapsed.value = false
    return
  }

  customHeaders.value = snapshot.customHeaders.map(header => ({ ...header }))
  formFieldValues.value = { ...snapshot.formFieldValues }
  activeTab.value = tabs.value.some(tab => tab.key === snapshot.activeTab)
    ? snapshot.activeTab
    : getDefaultTab()
  collapsed.value = snapshot.collapsed

  uiCache.set(key, {
    customHeaders: customHeaders.value.map(header => ({ ...header })),
    formFieldValues: { ...formFieldValues.value },
    activeTab: activeTab.value,
    collapsed: collapsed.value,
  })
}

function saveUiSnapshot() {
  const snapshot: UiSnapshot = {
    customHeaders: customHeaders.value.map(header => ({ ...header })),
    formFieldValues: { ...formFieldValues.value },
    activeTab: activeTab.value,
    collapsed: collapsed.value,
  }

  uiCache.set(operationKey.value, snapshot)
  writeUiStorage(operationKey.value, snapshot)
}

function addCustomHeader() {
  customHeaders.value.push({ name: '', value: '' })
}

function removeCustomHeader(index: number) {
  customHeaders.value.splice(index, 1)
}

function handleContentTypeChange(nextType: string) {
  simulate.setContentType(nextType)
  formFieldValues.value = {}
  formFileValues.value = {}
}

function resetCurrentRequestBody() {
  simulate.resetRequestBody()
  formFieldValues.value = {}
  formFileValues.value = {}
}

function handleReset() {
  uiCache.delete(operationKey.value)
  removeUiStorage(operationKey.value)
  customHeaders.value = []
  formFieldValues.value = {}
  formFileValues.value = {}
  activeTab.value = getDefaultTab()
  collapsed.value = false
  simulate.reset()
}

function buildFormData(): FormData {
  const formData = new FormData()

  for (const field of schemaFields.value) {
    if (field.isBinary) {
      const files = formFileValues.value[field.name]
      if (!files?.length) continue

      if (field.isArray) {
        for (const file of files) {
          formData.append(field.name, file)
        }
      } else {
        formData.append(field.name, files[0])
      }
      continue
    }

    const value = formFieldValues.value[field.name]
    if (value !== undefined && value !== '') {
      formData.append(field.name, value)
    }
  }

  return formData
}

function handleFileChange(fieldName: string, event: Event) {
  const input = event.target as HTMLInputElement
  formFileValues.value[fieldName] = input.files ? Array.from(input.files) : []
}

function buildFormUrlEncodedBody(): string {
  const searchParams = new URLSearchParams()
  for (const [name, value] of Object.entries(formFieldValues.value)) {
    if (value !== undefined && value !== '') {
      searchParams.append(name, value)
    }
  }
  return searchParams.toString()
}

function handleSendRequest() {
  if (isForm.value) {
    const hasBinaryField = schemaFields.value.some(field => field.isBinary)

    if (simulate.contentType.value.includes(CONTENT_TYPE.MULTIPART_FORM_DATA) || hasBinaryField) {
      simulate.sendRequest(mergedHeaders.value, buildFormData())
      activeTab.value = 'response'
      return
    }

    simulate.requestBody.value = buildFormUrlEncodedBody()
  }

  simulate.sendRequest(mergedHeaders.value)
  activeTab.value = 'response'
}

watch(operationKey, (key) => {
  restoreUiSnapshot(key)
}, { immediate: true })

watch([customHeaders, formFieldValues, activeTab, collapsed], saveUiSnapshot, { deep: true })

watch(hasRequestBodyEditor, (enabled) => {
  if (!enabled && activeTab.value === 'body') {
    activeTab.value = 'params'
  }
})

defineExpose({
  simulate,
  customHeaders,
  formFieldValues,
  formFileValues,
  handleSendRequest,
  handleReset,
})
</script>

<template>
  <div class="rounded-2xl border border-[var(--c-border)] bg-white">
    <div class="flex flex-wrap items-center justify-between gap-3 border-b border-[var(--c-border)] px-4 py-3">
      <div class="flex items-center gap-2.5">
        <span class="inline-flex h-8 w-8 items-center justify-center rounded-full bg-[var(--c-primary-light)] text-[var(--c-primary)]">
          <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M5 12h14" />
            <path d="m13 5 7 7-7 7" />
          </svg>
        </span>
        <div>
          <h3 class="text-[14px] font-semibold text-[var(--c-text)]">调试请求</h3>
        </div>
      </div>

      <div class="flex items-center gap-2">
        <button
          class="inline-flex h-9 w-9 items-center justify-center rounded-full border border-[var(--c-border)] bg-white text-[var(--c-muted)] transition-colors hover:bg-[var(--c-bg)] hover:text-[var(--c-text)]"
          :title="collapsed ? '展开调试请求' : '收起调试请求'"
          @click="collapsed = !collapsed"
        >
          <svg class="h-4 w-4 transition-transform" :class="collapsed ? '-rotate-90' : 'rotate-0'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="m6 9 6 6 6-6" />
          </svg>
        </button>
        <button
          class="inline-flex h-9 w-9 items-center justify-center rounded-full border border-[var(--c-border)] bg-white text-[var(--c-muted)] transition-colors hover:bg-[var(--c-bg)] hover:text-[var(--c-text)]"
          title="重置调试面板"
          @click="handleReset"
        >
          <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 12a9 9 0 1 0 3-6.7" />
            <path d="M3 3v6h6" />
          </svg>
        </button>
        <button
          class="rounded-full bg-[var(--c-primary)] px-4 py-2 text-[12px] font-semibold text-white transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="simulate.loading.value"
          @click="handleSendRequest"
        >
          {{ simulate.loading.value ? '发送中...' : '发送请求' }}
        </button>
      </div>
    </div>

    <div v-if="!collapsed" class="space-y-3 px-4 py-4">
      <div class="rounded-2xl border border-[var(--c-border)] bg-[var(--c-bg)] p-1">
        <div class="flex flex-wrap gap-1">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            class="rounded-xl px-3 py-2 text-[12px] font-medium transition-colors"
            :class="activeTab === tab.key ? 'bg-white text-[var(--c-text)] shadow-sm' : 'text-[var(--c-muted)] hover:text-[var(--c-text)]'"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>

      <div v-show="activeTab === 'params'" class="rounded-2xl border border-[var(--c-border)] bg-white p-3">
        <div class="mb-2">
          <h4 class="text-[12px] font-semibold text-[var(--c-text)]">请求参数</h4>
        </div>

        <div v-if="simulate.params.value.length" class="space-y-2">
          <div
            v-for="param in simulate.params.value"
            :key="`${param.in}-${param.name}`"
            class="grid items-center gap-2 rounded-xl border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-2 md:grid-cols-[minmax(0,240px)_minmax(0,1fr)]"
          >
            <div class="min-w-0 space-y-1">
              <div class="flex flex-wrap items-center gap-1.5">
                <span class="truncate font-mono text-[12px] font-semibold text-[var(--c-text)]">{{ param.name }}</span>
                <span class="rounded-full px-2 py-0.5 text-[10px] font-semibold" :class="getParamLocationClass(param.in)">
                  {{ param.in }}
                </span>
                <span v-if="param.required" class="rounded-full bg-rose-50 px-2 py-0.5 text-[10px] font-semibold text-rose-600">
                  必填
                </span>
                <span v-if="param.type" class="rounded-full bg-white px-2 py-0.5 text-[10px] font-medium text-[var(--c-muted)]">
                  {{ param.type }}
                </span>
              </div>
              <p v-if="param.description" class="truncate text-[11px] leading-4 text-[var(--c-muted)]">{{ param.description }}</p>
            </div>

            <input
              v-model="param.value"
              class="h-9 w-full rounded-xl border border-[var(--c-border)] bg-white px-3 text-[12px] text-[var(--c-text)] outline-none transition-colors focus:border-[var(--c-primary)]"
              :placeholder="param.example || param.type || '请输入参数值'"
            />
          </div>
        </div>
        <p v-else class="rounded-xl border border-dashed border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-4 text-[12px] text-[var(--c-muted)]">
          当前接口没有可编辑的请求参数。
        </p>
      </div>

      <div v-show="activeTab === 'headers'" class="rounded-2xl border border-[var(--c-border)] bg-white p-3">
        <div class="mb-3 flex items-center justify-between gap-3">
          <div>
            <h4 class="text-[12px] font-semibold text-[var(--c-text)]">附加请求头</h4>
          </div>
          <button
            class="rounded-full border border-[var(--c-border)] bg-white px-3 py-1.5 text-[12px] text-[var(--c-text)] transition-colors hover:bg-[var(--c-bg)]"
            @click="addCustomHeader"
          >
            添加
          </button>
        </div>

        <div v-if="customHeaders.length" class="space-y-2">
          <div
            v-for="(header, index) in customHeaders"
            :key="index"
            class="grid gap-2 rounded-xl border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-2.5 md:grid-cols-[160px_minmax(0,1fr)_72px]"
          >
            <input
              v-model="header.name"
              class="rounded-xl border border-[var(--c-border)] bg-white px-3 py-2 text-[12px] text-[var(--c-text)] outline-none focus:border-[var(--c-primary)]"
              placeholder="Header 名称"
            />
            <input
              v-model="header.value"
              class="rounded-xl border border-[var(--c-border)] bg-white px-3 py-2 text-[12px] text-[var(--c-text)] outline-none focus:border-[var(--c-primary)]"
              placeholder="Header 值"
            />
            <button
              class="rounded-xl border border-rose-200 bg-white px-3 py-2 text-[12px] text-rose-600 transition-colors hover:bg-rose-50"
              @click="removeCustomHeader(index)"
            >
              删除
            </button>
          </div>
        </div>
        <p v-else class="rounded-xl border border-dashed border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-4 text-[12px] text-[var(--c-muted)]">
          当前没有额外的 Header。
        </p>
      </div>

      <div v-show="activeTab === 'body' && hasRequestBodyEditor" class="rounded-2xl border border-[var(--c-border)] bg-white p-3">
        <div class="mb-3 flex flex-wrap items-center justify-between gap-3">
          <div>
            <h4 class="text-[12px] font-semibold text-[var(--c-text)]">请求体</h4>
          </div>

          <div class="flex items-center gap-2">
            <select
              v-if="availableContentTypes.length > 1"
              :value="simulate.contentType.value"
              class="rounded-full border border-[var(--c-border)] bg-white px-3 py-1.5 text-[12px] text-[var(--c-text)] outline-none focus:border-[var(--c-primary)]"
              @change="handleContentTypeChange(($event.target as HTMLSelectElement).value)"
            >
              <option v-for="type in availableContentTypes" :key="type" :value="type">{{ type }}</option>
            </select>
            <span
              v-else
              class="rounded-full border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-1.5 font-mono text-[11px] text-[var(--c-muted)]"
            >
              {{ simulate.contentType.value }}
            </span>
            <button
              class="rounded-full border border-[var(--c-border)] bg-white px-3 py-1.5 text-[12px] text-[var(--c-text)] transition-colors hover:bg-[var(--c-bg)]"
              @click="resetCurrentRequestBody"
            >
              重置
            </button>
          </div>
        </div>

        <p
          v-if="schemaHint"
          class="mb-3 rounded-xl border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-2.5 text-[11px] leading-5 text-[var(--c-muted)]"
        >
          {{ schemaHint }}
        </p>
        <p
          v-if="isForm && schemaFields.some(field => field.isBinary)"
          class="mb-3 rounded-xl border border-amber-200 bg-amber-50 px-3 py-2.5 text-[11px] leading-5 text-amber-700"
        >
          已选文件会在当前页面内保留；刷新页面后浏览器会清空文件选择。
        </p>

        <div v-if="isForm && schemaFields.length" class="space-y-2">
          <div
            v-for="field in schemaFields"
            :key="field.name"
            class="grid gap-2 rounded-xl border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-2.5 md:grid-cols-[160px_minmax(0,1fr)]"
          >
            <div>
              <div class="flex flex-wrap items-center gap-1.5">
                <span class="font-mono text-[12px] font-semibold text-[var(--c-text)]">{{ field.name }}</span>
                <span class="rounded-full bg-white px-2 py-0.5 text-[10px] font-semibold text-[var(--c-muted)]">
                  {{ field.isBinary ? (field.isArray ? 'file[]' : 'file') : field.type }}
                </span>
                <span v-if="field.required" class="rounded-full bg-rose-50 px-2 py-0.5 text-[10px] font-semibold text-rose-600">
                  必填
                </span>
              </div>
              <p v-if="field.description" class="mt-1 line-clamp-2 text-[11px] leading-5 text-[var(--c-muted)]">{{ field.description }}</p>
            </div>

            <div class="space-y-1">
              <input
                v-if="field.isBinary"
                type="file"
                :multiple="field.isArray"
                class="block w-full rounded-xl border border-[var(--c-border)] bg-white px-3 py-2 text-[12px] text-[var(--c-text)] file:mr-2 file:rounded-full file:border-0 file:bg-[var(--c-primary-light)] file:px-3 file:py-1.5 file:text-[12px] file:font-medium file:text-[var(--c-primary)]"
                @change="handleFileChange(field.name, $event)"
              />
              <div v-if="field.isBinary && formFileValues[field.name]?.length" class="rounded-xl bg-white px-3 py-2 text-[11px] text-[var(--c-muted)]">
                {{ formFileValues[field.name].map(file => file.name).join(' , ') }}
              </div>
              <select
                v-else-if="field.enum?.length"
                v-model="formFieldValues[field.name]"
                class="w-full rounded-xl border border-[var(--c-border)] bg-white px-3 py-2 text-[12px] text-[var(--c-text)] outline-none focus:border-[var(--c-primary)]"
              >
                <option value="">请选择</option>
                <option v-for="option in field.enum" :key="option" :value="option">{{ option }}</option>
              </select>
              <select
                v-else-if="field.type === 'boolean'"
                v-model="formFieldValues[field.name]"
                class="w-full rounded-xl border border-[var(--c-border)] bg-white px-3 py-2 text-[12px] text-[var(--c-text)] outline-none focus:border-[var(--c-primary)]"
              >
                <option value="">请选择</option>
                <option value="true">true</option>
                <option value="false">false</option>
              </select>
              <input
                v-else
                v-model="formFieldValues[field.name]"
                class="w-full rounded-xl border border-[var(--c-border)] bg-white px-3 py-2 text-[12px] text-[var(--c-text)] outline-none focus:border-[var(--c-primary)]"
                :placeholder="field.example || field.type || '请输入字段值'"
              />
              <p v-if="field.example" class="text-[11px] text-[var(--c-muted)]">示例：{{ field.example }}</p>
            </div>
          </div>
        </div>

        <div v-else class="space-y-3">
          <div v-if="schemaFields.length" class="grid gap-2 md:grid-cols-2">
            <div
              v-for="field in schemaFields"
              :key="field.name"
              class="rounded-xl border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-2.5"
            >
              <div class="flex flex-wrap items-center gap-1.5">
                <span class="font-mono text-[12px] font-semibold text-[var(--c-text)]">{{ field.name }}</span>
                <span class="rounded-full bg-white px-2 py-0.5 text-[10px] font-semibold text-[var(--c-muted)]">{{ field.type }}</span>
                <span v-if="field.required" class="rounded-full bg-rose-50 px-2 py-0.5 text-[10px] font-semibold text-rose-600">
                  必填
                </span>
              </div>
              <p v-if="field.description" class="mt-1 line-clamp-2 text-[11px] leading-5 text-[var(--c-muted)]">{{ field.description }}</p>
            </div>
          </div>

          <textarea
            v-model="simulate.requestBody.value"
            :rows="isJson ? 10 : 6"
            class="min-h-[144px] w-full resize-y rounded-xl border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-3 font-mono text-[12px] leading-6 text-[var(--c-text)] outline-none transition-colors focus:border-[var(--c-primary)] focus:bg-white"
            spellcheck="false"
            :placeholder="isJson ? '{\n  \n}' : '请输入请求体内容'"
          />
        </div>
      </div>

      <div v-show="activeTab === 'response'" class="rounded-2xl border border-[var(--c-border)] bg-white p-3">
        <SimulateResponsePanel
          :result="simulate.result.value"
          :error="simulate.error.value"
        />
      </div>
    </div>
  </div>
</template>
