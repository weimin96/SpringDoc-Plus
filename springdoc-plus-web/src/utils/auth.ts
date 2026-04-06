/**
 * auth.ts — 统一的鉴权 Header 构建工具
 *
 */
import type { AuthHeader } from '@/types'

/**
 * 将单个 AuthHeader 配置项规范化为最终的 Header 值。
 * - 自动拼接 defaultPrefix（如 "Bearer "）
 * - 若 value 已有前缀则不重复添加
 * - value 为空时返回 null，表示跳过该 Header
 */
export function resolveAuthHeaderValue(header: AuthHeader): string | null {
  const name = (header.name ?? '').trim()
  if (!name) return null

  let value = (header.value ?? '').trim()
  if (!value) return null

  const prefix = (header.defaultPrefix ?? '').trim()
  if (prefix && !value.startsWith(`${prefix} `)) {
    value = `${prefix} ${value}`
  }

  return value
}

/**
 * 将 AuthHeader[] 转换为 fetch 可直接使用的 Record<string, string>。
 * 同名 Header 以后者覆盖前者（与 Map 插入顺序一致）。
 */
export function buildAuthHeaders(
  authHeaders: AuthHeader[] | undefined | null,
): Record<string, string> {
  const result: Record<string, string> = {}
  for (const h of authHeaders ?? []) {
    const value = resolveAuthHeaderValue(h)
    if (value !== null) {
      result[h.name.trim()] = value
    }
  }
  return result
}

/**
 * 兼容旧版本单 Header 配置（authHeaderName + authValue + authDefaultPrefix）。
 * 若新版 authHeaders 已配置，则直接用 buildAuthHeaders，此函数作为回退。
 */
export function buildLegacyAuthHeader(
  headerName: string | undefined,
  value: string | undefined,
  defaultPrefix: string | undefined,
): Record<string, string> {
  const name = (headerName ?? 'Authorization').trim()
  const header: AuthHeader = { name, value, defaultPrefix }
  const resolved = resolveAuthHeaderValue(header)
  if (!resolved) return {}
  return { [name]: resolved }
}
