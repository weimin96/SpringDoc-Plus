import { ref, type Ref, watch } from 'vue'
import type { OperationItem, SchemaObject } from '@/types/openapi'
import { generateJsonSchemaExample } from '@/utils/schema'

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

/** 每个接口的缓存快照 */
interface OperationSnapshot {
  params: RequestParam[]
  requestBody: string
  contentType: string
  bodyParams: Record<string, string>
}

/**
 * 模拟请求 Composable
 * - schemasRef: 用于正确解析 $ref 生成 JSON 示例
 * - 切换接口时自动保存/恢复用户编辑的请求体和参数
 */
export function useSimulateRequest(
  itemRef: Ref<OperationItem>,
  contextPathRef?: Ref<string | undefined>,
  schemasRef?: Ref<Record<string, SchemaObject> | undefined>
) {
  const loading = ref(false)
  const error = ref<string | null>(null)
  const result = ref<SimulateResult | null>(null)
  const params = ref<RequestParam[]>([])
  const requestBody = ref<string>('')
  const contentType = ref<string>('application/json')
  const bodyParams = ref<Record<string, string>>({})

  /** 按 "method-path" 缓存用户编辑的状态 */
  const snapshotCache = new Map<string, OperationSnapshot>()

  function itemKey(item: OperationItem) {
    return `${item.method}-${item.path}`
  }

  /** 将当前界面状态保存到缓存 */
  function saveSnapshot(key: string) {
    snapshotCache.set(key, {
      params: params.value.map(p => ({ ...p })),
      requestBody: requestBody.value,
      contentType: contentType.value,
      bodyParams: { ...bodyParams.value },
    })
  }

  /** 从缓存恢复状态，若无缓存则初始化默认值 */
  function restoreOrInit(item: OperationItem) {
    const key = itemKey(item)
    const cached = snapshotCache.get(key)

    if (cached) {
      // 恢复用户上次编辑的内容
      params.value = cached.params.map(p => ({ ...p }))
      requestBody.value = cached.requestBody
      contentType.value = cached.contentType
      bodyParams.value = { ...cached.bodyParams }
      return
    }

    // 无缓存 → 初始化默认值
    const parameters = item.operation.parameters ?? []
    params.value = parameters.map(p => {
      const example = p.example !== undefined
        ? String(p.example)
        : p.schema?.example !== undefined
          ? String(p.schema.example)
          : undefined
      return {
        name: p.name,
        in: p.in as 'path' | 'query' | 'header' | 'cookie',
        value: example ?? '',
        type: p.schema?.type,
        required: p.required ?? false,
        description: p.description,
        example,
      }
    })

    bodyParams.value = {}

    const rb = item.operation.requestBody
    if (rb?.content) {
      const mediaType = Object.keys(rb.content)[0]
      contentType.value = mediaType
      if (mediaType === 'application/json') {
        const schema = rb.content[mediaType]?.schema ?? null
        requestBody.value = generateJsonSchemaExample(schema, schemasRef?.value)
      } else {
        requestBody.value = ''
      }
    } else {
      contentType.value = 'application/json'
      requestBody.value = ''
    }
  }

  function buildUrl(): string {
    const item = itemRef.value
    const contextPath = contextPathRef?.value
    const base = window.location.origin
    let path = item.path

    if (contextPath) {
      const normalizedContextPath = contextPath.startsWith('/') ? contextPath : '/' + contextPath
      path = normalizedContextPath.replace(/\/$/, '') + path
    }

    params.value.filter(p => p.in === 'path').forEach(p => {
      path = path.replace(`{${p.name}}`, encodeURIComponent(p.value))
    })

    const queryParams = params.value.filter(p => p.in === 'query' && p.value)
    if (queryParams.length) {
      const qs = queryParams.map(p => `${encodeURIComponent(p.name)}=${encodeURIComponent(p.value)}`).join('&')
      path += (path.includes('?') ? '&' : '?') + qs
    }

    return base + path
  }

  function buildHeaders(customHeaders?: CustomHeader[], formData?: FormData): Record<string, string> {
    const item = itemRef.value
    const headers: Record<string, string> = {}

    params.value.filter(p => p.in === 'header').forEach(p => {
      if (p.value) headers[p.name] = p.value
    })

    if (customHeaders) {
      customHeaders.forEach(h => {
        if (h.name && h.value) headers[h.name] = h.value
      })
    }

    if (!formData && item.operation.requestBody) {
      headers['Content-Type'] = contentType.value
    }

    return headers
  }

  function buildBodyFromParams(): string {
    const item = itemRef.value
    const schema = item.operation.requestBody?.content?.[contentType.value]?.schema
    if (!schema || !schema.properties) return requestBody.value

    const obj: Record<string, any> = {}
    for (const [key, propSchema] of Object.entries(schema.properties)) {
      const p = propSchema as any
      const val = bodyParams.value[key]
      if (val !== undefined && val !== '') {
        if (p.type === 'number' || p.type === 'integer') {
          obj[key] = Number(val)
        } else if (p.type === 'boolean') {
          obj[key] = val === 'true'
        } else if (p.type === 'object' || p.type === 'array') {
          try { obj[key] = JSON.parse(val) } catch { obj[key] = val }
        } else {
          obj[key] = val
        }
      }
    }
    return JSON.stringify(obj)
  }

  async function sendRequest(customHeaders?: CustomHeader[], formData?: FormData) {
    const item = itemRef.value
    loading.value = true
    error.value = null
    result.value = null

    const startTime = Date.now()
    const url = buildUrl()
    const headers = buildHeaders(customHeaders, formData)
    const method = item.method.toUpperCase()

    try {
      const options: RequestInit = { method, headers }

      if (!['GET', 'HEAD'].includes(method) && item.operation.requestBody) {
        if (formData) {
          options.body = formData
        } else {
          const bodyContent = Object.keys(bodyParams.value).length > 0
            ? buildBodyFromParams()
            : requestBody.value
          options.body = bodyContent
        }
      }

      const res = await fetch(url, options)
      const duration = Date.now() - startTime
      const resHeaders = Object.fromEntries(res.headers.entries())

      let data: unknown
      const resType = res.headers.get('content-type') || ''
      if (resType.includes('application/json')) {
        data = await res.json()
      } else if (resType.includes('text/')) {
        data = await res.text()
      } else {
        data = await res.blob()
      }

      result.value = { status: res.status, statusText: res.statusText, headers: resHeaders, data, duration }
    } catch (e) {
      error.value = e instanceof Error ? e.message : String(e)
    } finally {
      loading.value = false
    }
  }

  function updateParamValue(name: string, value: string) {
    const param = params.value.find(p => p.name === name)
    if (param) param.value = value
  }

  function updateBodyParam(name: string, value: string) {
    bodyParams.value[name] = value
  }

  /** 仅重置请求体为默认示例（不清参数，不清缓存） */
  function resetRequestBody() {
    const item = itemRef.value
    const rb = item.operation.requestBody
    if (rb?.content) {
      const mediaType = Object.keys(rb.content)[0]
      contentType.value = mediaType
      if (mediaType === 'application/json') {
        const schema = rb.content[mediaType]?.schema ?? null
        requestBody.value = generateJsonSchemaExample(schema, schemasRef?.value)
      } else {
        requestBody.value = ''
      }
    }
    // 同步更新缓存
    saveSnapshot(itemKey(item))
  }

  /** 完全重置当前接口（清空缓存中该接口的记录，重新初始化） */
  function reset() {
    const key = itemKey(itemRef.value)
    snapshotCache.delete(key)   // 删除缓存，强制重新初始化
    result.value = null
    error.value = null
    bodyParams.value = {}
    restoreOrInit(itemRef.value)
  }

  // 监听接口切换：先保存旧接口状态，再恢复/初始化新接口状态
  watch(itemRef, (newVal, oldVal) => {
    const newKey = newVal ? itemKey(newVal) : ''
    const oldKey = oldVal ? itemKey(oldVal) : ''
    if (newKey === oldKey) return

    // 保存旧接口当前编辑内容到缓存
    if (oldKey) saveSnapshot(oldKey)

    // 清除响应结果（每个接口的响应独立）
    result.value = null
    error.value = null

    // 恢复新接口的缓存，或初始化默认值
    restoreOrInit(newVal)
  }, { deep: false })

  restoreOrInit(itemRef.value)

  return {
    loading,
    error,
    result,
    params,
    requestBody,
    contentType,
    bodyParams,
    sendRequest,
    updateParamValue,
    updateBodyParam,
    resetRequestBody,
    reset,
    saveSnapshot,
    itemKey,
  }
}