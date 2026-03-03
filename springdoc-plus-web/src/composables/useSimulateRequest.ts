import { ref, type Ref, watch } from 'vue'
import type { OperationItem } from '@/types/openapi'

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

/**
 * 模拟请求 Composable
 * 支持填写参数值、发送 HTTP 请求、显示响应结果
 * @param itemRef 操作项（响应式引用）
 * @param contextPathRef 网关模式下的路径前缀（响应式引用）
 */
export function useSimulateRequest(
  itemRef: Ref<OperationItem>,
  contextPathRef?: Ref<string | undefined>
) {
  const loading = ref(false)
  const error = ref<string | null>(null)
  const result = ref<SimulateResult | null>(null)
  const params = ref<RequestParam[]>([])
  const requestBody = ref<string>('')
  const contentType = ref<string>('application/json')
  const bodyParams = ref<Record<string, string>>({})

  // 初始化参数列表
  function initParams() {
    const item = itemRef.value
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
        const schema = rb.content[mediaType]?.schema
        requestBody.value = generateJsonExample(schema)
      }
    }
  }

  // 生成 JSON 示例
  function generateJsonExample(schema: any): string {
    if (!schema) return '{}'

    if (schema.example !== undefined) {
      return JSON.stringify(schema.example, null, 2)
    }

    if (schema.properties) {
      const obj: Record<string, any> = {}
      for (const [key, prop] of Object.entries(schema.properties)) {
        const p = prop as any
        if (p.type === 'string') obj[key] = p.example || ''
        else if (p.type === 'number' || p.type === 'integer') obj[key] = p.example || 0
        else if (p.type === 'boolean') obj[key] = p.example || false
        else if (p.type === 'array') obj[key] = []
        else if (p.type === 'object') obj[key] = generateJsonExample(p)
        else obj[key] = null
      }
      return JSON.stringify(obj, null, 2)
    }

    return '{}'
  }

  // 构建完整 URL
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

  // 构建请求头
  // 注意：传入 formData 时不设置 Content-Type，由浏览器自动添加 boundary
  function buildHeaders(customHeaders?: CustomHeader[], formData?: FormData): Record<string, string> {
    const item = itemRef.value
    const headers: Record<string, string> = {}

    params.value.filter(p => p.in === 'header').forEach(p => {
      if (p.value) headers[p.name] = p.value
    })

    if (customHeaders) {
      customHeaders.forEach(h => {
        if (h.name && h.value) {
          headers[h.name] = h.value
        }
      })
    }

    // 有 FormData 时不手动设置 Content-Type，让浏览器自动处理（含 boundary）
    if (!formData && item.operation.requestBody) {
      headers['Content-Type'] = contentType.value
    }

    return headers
  }

  // 根据请求体参数构建请求体 JSON
  function buildBodyFromParams(): string {
    const item = itemRef.value
    const schema = item.operation.requestBody?.content?.[contentType.value]?.schema
    if (!schema || !schema.properties) {
      return requestBody.value
    }

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
          try {
            obj[key] = JSON.parse(val)
          } catch {
            obj[key] = val
          }
        } else {
          obj[key] = val
        }
      }
    }

    return JSON.stringify(obj)
  }

  /**
   * 发送请求
   * @param customHeaders 自定义请求头
   * @param formData 文件/表单数据（multipart/form-data 场景）
   */
  async function sendRequest(customHeaders?: CustomHeader[], formData?: FormData) {
    const item = itemRef.value
    loading.value = true
    error.value = null
    result.value = null

    const startTime = Date.now()
    const url = buildUrl()
    // 有 FormData 时不设置 Content-Type header
    const headers = buildHeaders(customHeaders, formData)
    const method = item.method.toUpperCase()

    try {
      const options: RequestInit = {
        method,
        headers,
      }

      if (!['GET', 'HEAD'].includes(method) && item.operation.requestBody) {
        if (formData) {
          // 直接使用 FormData 作为请求体
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

      const resHeaders: Record<string, string> = {}
      res.headers.forEach((value, key) => {
        resHeaders[key] = value
      })

      let data: unknown
      const resType = res.headers.get('content-type') || ''
      if (resType.includes('application/json')) {
        data = await res.json()
      } else if (resType.includes('text/')) {
        data = await res.text()
      } else {
        data = await res.blob()
      }

      result.value = {
        status: res.status,
        statusText: res.statusText,
        headers: resHeaders,
        data,
        duration,
      }
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

  function resetRequestBody() {
    const item = itemRef.value
    const rb = item.operation.requestBody
    if (rb?.content) {
      const mediaType = Object.keys(rb.content)[0]
      contentType.value = mediaType
      if (mediaType === 'application/json') {
        const schema = rb.content[mediaType]?.schema
        requestBody.value = generateJsonExample(schema)
      }
    }
  }

  function reset() {
    result.value = null
    error.value = null
    bodyParams.value = {}
    initParams()
  }

  watch(itemRef, () => {
    reset()
  }, { deep: false })

  initParams()

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
  }
}