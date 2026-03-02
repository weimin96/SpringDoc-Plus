import { ref } from 'vue'
import type { OperationItem } from '@/types/openapi'

export interface RequestParam {
  name: string
  in: 'path' | 'query' | 'header' | 'cookie'
  value: string
  type?: string
  required?: boolean
}

export interface SimulateResult {
  status: number
  statusText: string
  headers: Record<string, string>
  data: unknown
  duration: number
}

/**
 * 模拟请求 Composable
 * 支持填写参数值、发送 HTTP 请求、显示响应结果
 */
export function useSimulateRequest(item: OperationItem, baseUrl?: string) {
  const loading = ref(false)
  const error = ref<string | null>(null)
  const result = ref<SimulateResult | null>(null)
  const params = ref<RequestParam[]>([])
  const requestBody = ref<string>('')
  const contentType = ref<string>('application/json')

  // 初始化参数列表
  function initParams() {
    const parameters = item.operation.parameters ?? []
    params.value = parameters.map(p => ({
      name: p.name,
      in: p.in as 'path' | 'query' | 'header' | 'cookie',
      value: '',
      type: p.schema?.type,
      required: p.required ?? false,
    }))

    // 初始化请求体
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
    const base = baseUrl || window.location.origin
    let path = item.path

    // 替换路径参数
    params.value.filter(p => p.in === 'path').forEach(p => {
      path = path.replace(`{${p.name}}`, encodeURIComponent(p.value))
    })

    // 添加查询参数
    const queryParams = params.value.filter(p => p.in === 'query' && p.value)
    if (queryParams.length) {
      const qs = queryParams.map(p => `${encodeURIComponent(p.name)}=${encodeURIComponent(p.value)}`).join('&')
      path += (path.includes('?') ? '&' : '?') + qs
    }

    return base + path
  }

  // 构建请求头
  function buildHeaders(): Record<string, string> {
    const headers: Record<string, string> = {}

    // 添加 header 参数
    params.value.filter(p => p.in === 'header').forEach(p => {
      if (p.value) headers[p.name] = p.value
    })

    // 添加 Content-Type
    if (item.operation.requestBody) {
      headers['Content-Type'] = contentType.value
    }

    return headers
  }

  // 发送请求
  async function sendRequest() {
    loading.value = true
    error.value = null
    result.value = null

    const startTime = Date.now()
    const url = buildUrl()
    const headers = buildHeaders()
    const method = item.method.toUpperCase()

    try {
      const options: RequestInit = {
        method,
        headers,
      }

      // 添加请求体（GET/HEAD 不需要）
      if (!['GET', 'HEAD'].includes(method) && item.operation.requestBody) {
        options.body = requestBody.value
      }

      const res = await fetch(url, options)
      const duration = Date.now() - startTime

      // 获取响应头
      const resHeaders: Record<string, string> = {}
      res.headers.forEach((value, key) => {
        resHeaders[key] = value
      })

      // 解析响应体
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

  // 更新参数值
  function updateParamValue(name: string, value: string) {
    const param = params.value.find(p => p.name === name)
    if (param) {
      param.value = value
    }
  }

  // 重置
  function reset() {
    result.value = null
    error.value = null
    initParams()
  }

  // 初始化
  initParams()

  return {
    loading,
    error,
    result,
    params,
    requestBody,
    contentType,
    sendRequest,
    updateParamValue,
    reset,
  }
}
