import type { ApiGroup, Mode, ServerUiConfig } from '@/types'

/** 自动检测进入模式
 *  - 网关模式：URL 中含 /springdoc-plus-gateway/ 等标志，或后端有 groups 接口
 *  - 子服务模式：直接访问某个服务，使用 /v3/api-docs
 */
export async function detectMode(): Promise<Mode> {
  // 优先检查 query 参数强制指定：?mode=gateway 或 ?mode=service
  const qs = new URLSearchParams(location.search)
  const forced = qs.get('mode')
  if (forced === 'gateway' || forced === 'service') return forced

  // 尝试请求网关接口，成功则为网关模式
  try {
    const res = await fetch('/springdoc-plus-gateway/openapi/groups', { signal: AbortSignal.timeout(3000) })
    if (res.ok) {
      const data = await res.json() as { groups?: ApiGroup[] }
      if (Array.isArray(data.groups)) return 'gateway'
    }
  } catch { /* fall through */ }

  return 'service'
}

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

/** 子服务模式：构造单一文档组
 *  优先顺序：
 *  1. query 参数 ?specUrl=xxx
 *  2. /v3/api-docs（SpringDoc 默认）
 *  3. /v2/api-docs（旧版 Springfox 兼容）
 */
export async function resolveServiceGroup(): Promise<ApiGroup[]> {
  const qs = new URLSearchParams(location.search)
  const specUrl = qs.get('specUrl')
  if (specUrl) {
    return [{ name: qs.get('groupName') || '默认文档', url: specUrl }]
  }

  // 尝试 /v3/api-docs
  const candidates = [
    { url: '/v3/api-docs', name: 'API 文档' },
    { url: '/v3/api-docs.yaml', name: 'API 文档 (YAML)' },
    { url: '/v2/api-docs', name: 'API 文档 (v2)' },
  ]

  for (const c of candidates) {
    try {
      const res = await fetch(c.url, { method: 'HEAD', signal: AbortSignal.timeout(2000) })
      if (res.ok) return [c]
    } catch { /* next */ }
  }

  // 兜底：直接返回 /v3/api-docs 让 swagger-ui 去尝试
  return [{ name: 'API 文档', url: '/v3/api-docs' }]
}
