import type { ApiGroup, ServerUiConfig } from '@/types'

/** 网关模式：加载文档组列表 */
export async function fetchGroups(): Promise<ApiGroup[]> {
  const res = await fetch('/springdoc-plus-gateway/openapi/groups')
  if (!res.ok) throw new Error(`加载文档组失败 (HTTP ${res.status})`)
  const data = await res.json() as { groups?: ApiGroup[] }
  return data.groups || []
}

/** 网关模式：加载服务端 UI 配置（可选接口，失败时返回空对象） */
export async function fetchServerUiConfig(): Promise<ServerUiConfig> {
  try {
    const res = await fetch('/springdoc-plus-gateway/ui-config', { signal: AbortSignal.timeout(3000) })
    if (res.ok) return await res.json() as ServerUiConfig
  } catch { /* ignore */ }
  return {}
}
