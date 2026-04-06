import { ref, type Ref, watch } from 'vue'
import type { OperationItem, SchemaObject } from '@/types/openapi'
import { generateJsonSchemaExample } from '@/utils/schema'
import { readStorage, writeStorage, removeStorage } from '@/utils/storage'

export interface RequestParam {
  name: string
  in: 'path' | 'query' | 'header' | 'cookie'
  value: string
  type?: string
  required?: boolean
  description?: string
  example?: string
}

export interface SimulateResult {
  status: number
  statusText: string
  headers: Record<string, string>
  data: unknown
  duration: number
}

export interface CustomHeader {
  name: string
  value: string
}

export interface RequestBodyOption {
  sourceType: string
  effectiveType: string
  schema: SchemaObject | null
  example?: unknown
}

interface OperationSnapshot {
  params: RequestParam[]
  requestBody: string
  contentType: string
}

/** 在内存缓存中存的同一份 snapshot，避免频繁 JSON 序列化 */
const MEMORY_CACHE = new Map<string, OperationSnapshot>()

/** localStorage key 前缀 — 供 gcStorage 清理 */
export const SIMULATE_REQUEST_STORAGE_PREFIX = 'springdoc-plus:simulate:request:'

function getRequestBodyContent(item: OperationItem) {
  return item.operation.requestBody?.content ?? {}
}

function normalizeContentType(mediaType: string, schema: SchemaObject | null): string {
  if (mediaType !== '*/*') return mediaType
  if (schema?.type === 'string') return 'text/plain'
  if (schema?.format === 'binary') return 'application/octet-stream'
  return 'application/json'
}

function storageKey(key: string): string {
  return `${SIMULATE_REQUEST_STORAGE_PREFIX}${key}`
}

export function getRequestBodyOptions(item: OperationItem): RequestBodyOption[] {
  const seen = new Set<string>()

  return Object.entries(getRequestBodyContent(item)).flatMap(([sourceType, media]) => {
    const schema = media?.schema ?? null
    const effectiveType = normalizeContentType(sourceType, schema)

    if (seen.has(effectiveType)) return []
    seen.add(effectiveType)

    return [{ sourceType, effectiveType, schema, example: media?.example }]
  })
}

export function resolveRequestBodyOption(
  item: OperationItem,
  contentType: string,
): RequestBodyOption | null {
  return getRequestBodyOptions(item).find(option => option.effectiveType === contentType) ?? null
}

function getPreferredContentType(item: OperationItem): string {
  const options = getRequestBodyOptions(item)
  if (!options.length) return 'application/json'
  const jsonOption = options.find(option => option.effectiveType.includes('json'))
  return jsonOption?.effectiveType ?? options[0].effectiveType
}

function stringifyExample(example: unknown): string {
  return typeof example === 'string' ? example : JSON.stringify(example, null, 2)
}

function buildInitialRequestBody(
  item: OperationItem,
  mediaType: string,
  schemas?: Record<string, SchemaObject>,
): string {
  const option = resolveRequestBodyOption(item, mediaType)
  const schema = option?.schema ?? null

  if (option?.example !== undefined) return stringifyExample(option.example)
  if (mediaType.includes('json')) return generateJsonSchemaExample(schema, schemas)
  if (schema?.example !== undefined) return stringifyExample(schema.example)
  if (schema?.default !== undefined) return stringifyExample(schema.default)
  if (mediaType === 'text/plain' || mediaType.startsWith('text/')) return ''
  if (mediaType === 'application/xml' || mediaType === 'text/xml') {
    return '<?xml version="1.0" encoding="UTF-8"?>\n<root>\n</root>'
  }
  return ''
}

export function useSimulateRequest(
  itemRef: Ref<OperationItem>,
  contextPathRef?: Ref<string | undefined>,
  schemasRef?: Ref<Record<string, SchemaObject> | undefined>,
) {
  const loading = ref(false)
  const error = ref<string | null>(null)
  const result = ref<SimulateResult | null>(null)
  const params = ref<RequestParam[]>([])
  const requestBody = ref('')
  const contentType = ref('application/json')
  const availableContentTypes = ref<string[]>([])

  function itemKey(item: OperationItem) {
    const contextPath = contextPathRef?.value ?? ''
    return `${contextPath}::${item.method}-${item.path}`
  }

  function buildDefaultParams(item: OperationItem): RequestParam[] {
    return (item.operation.parameters ?? []).map((param) => {
      const example =
        param.example !== undefined
          ? String(param.example)
          : param.schema?.example !== undefined
            ? String(param.schema.example)
            : ''

      return {
        name: param.name,
        in: param.in as RequestParam['in'],
        value: example,
        type: param.schema?.type,
        required: param.required ?? false,
        description: param.description,
        example: example || undefined,
      }
    })
  }

  function saveSnapshot(key: string) {
    const snapshot: OperationSnapshot = {
      params: params.value.map(p => ({ ...p })),
      requestBody: requestBody.value,
      contentType: contentType.value,
    }
    MEMORY_CACHE.set(key, snapshot)
    writeStorage(storageKey(key), snapshot)
  }

  function restoreOrInit(item: OperationItem) {
    const key = itemKey(item)
    const cached = MEMORY_CACHE.get(key) ?? readStorage<OperationSnapshot>(storageKey(key))
    const supportedTypes = getRequestBodyOptions(item).map(option => option.effectiveType)

    availableContentTypes.value = supportedTypes

    if (cached) {
      params.value = cached.params.map(p => ({ ...p }))
      contentType.value = supportedTypes.includes(cached.contentType)
        ? cached.contentType
        : supportedTypes.length
          ? getPreferredContentType(item)
          : 'application/json'
      requestBody.value = cached.requestBody
      MEMORY_CACHE.set(key, {
        params: params.value.map(p => ({ ...p })),
        requestBody: requestBody.value,
        contentType: contentType.value,
      })
      return
    }

    params.value = buildDefaultParams(item)
    contentType.value = supportedTypes.length ? getPreferredContentType(item) : 'application/json'
    requestBody.value = supportedTypes.length
      ? buildInitialRequestBody(item, contentType.value, schemasRef?.value)
      : ''
  }

  function buildUrl(): string {
    const item = itemRef.value
    const base = window.location.origin
    const contextPath = contextPathRef?.value
    let path = item.path

    if (contextPath) {
      const normalizedPath = contextPath.startsWith('/') ? contextPath : `/${contextPath}`
      path = `${normalizedPath.replace(/\/$/, '')}${path}`
    }

    for (const param of params.value.filter(p => p.in === 'path')) {
      path = path.replace(`{${param.name}}`, encodeURIComponent(param.value))
    }

    const query = params.value
      .filter(p => p.in === 'query' && p.value)
      .map(p => `${encodeURIComponent(p.name)}=${encodeURIComponent(p.value)}`)
      .join('&')

    if (query) {
      path += path.includes('?') ? `&${query}` : `?${query}`
    }

    return `${base}${path}`
  }

  function buildHeaders(customHeaders?: CustomHeader[], formData?: FormData): Record<string, string> {
    const headers: Record<string, string> = {}

    for (const param of params.value.filter(p => p.in === 'header' && p.value)) {
      headers[param.name] = param.value
    }

    for (const header of customHeaders ?? []) {
      if (header.name && header.value) {
        headers[header.name] = header.value
      }
    }

    if (!formData && availableContentTypes.value.length && contentType.value) {
      headers['Content-Type'] =
        contentType.value === 'text/plain'
          ? 'text/plain;charset=UTF-8'
          : contentType.value
    }

    return headers
  }

  function setContentType(nextContentType: string) {
    if (
      !availableContentTypes.value.includes(nextContentType) ||
      nextContentType === contentType.value
    ) return

    contentType.value = nextContentType
    requestBody.value = buildInitialRequestBody(itemRef.value, nextContentType, schemasRef?.value)
  }

  async function sendRequest(customHeaders?: CustomHeader[], formData?: FormData) {
    const method = itemRef.value.method.toUpperCase()
    const url = buildUrl()
    const headers = buildHeaders(customHeaders, formData)
    const startTime = Date.now()

    loading.value = true
    error.value = null
    result.value = null

    try {
      const options: RequestInit = { method, headers }

      if (!['GET', 'HEAD'].includes(method)) {
        if (formData) {
          options.body = formData
        } else if (availableContentTypes.value.length && requestBody.value !== '') {
          options.body = requestBody.value
        }
      }

      const response = await fetch(url, options)
      const responseType = response.headers.get('content-type') ?? ''
      const responseHeaders = Object.fromEntries(response.headers.entries())

      let data: unknown
      if (responseType.includes('application/json')) {
        data = await response.json()
      } else if (responseType.includes('text/')) {
        data = await response.text()
      } else {
        data = await response.blob()
      }

      result.value = {
        status: response.status,
        statusText: response.statusText,
        headers: responseHeaders,
        data,
        duration: Date.now() - startTime,
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : String(err)
    } finally {
      loading.value = false
    }
  }

  function resetRequestBody() {
    if (!availableContentTypes.value.length) {
      requestBody.value = ''
      return
    }
    requestBody.value = buildInitialRequestBody(itemRef.value, contentType.value, schemasRef?.value)
  }

  function reset() {
    const key = itemKey(itemRef.value)
    MEMORY_CACHE.delete(key)
    removeStorage(storageKey(key))
    result.value = null
    error.value = null
    restoreOrInit(itemRef.value)
  }

  watch(
    itemRef,
    (nextItem) => {
      result.value = null
      error.value = null
      restoreOrInit(nextItem)
    },
    { deep: false, immediate: true },
  )

  watch([params, requestBody, contentType], () => saveSnapshot(itemKey(itemRef.value)), { deep: true })

  return {
    loading,
    error,
    result,
    params,
    requestBody,
    contentType,
    availableContentTypes,
    sendRequest,
    setContentType,
    resetRequestBody,
    reset,
    itemKey,
  }
}
