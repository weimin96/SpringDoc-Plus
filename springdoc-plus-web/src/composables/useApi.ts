import type { ApiGroup, ServerUiConfig } from '@/types'
import { assertOk, describeUnknownError } from '@/utils/apiError'

/** 网关模式：加载文档组列表 */
export async function fetchGroups(): Promise<ApiGroup[]> {
  try {
    const res = await fetch('/springdoc-plus-gateway/openapi/groups')
    await assertOk(res, '文档组接口')
    const data = await res.json() as { groups?: ApiGroup[] }
    return data.groups || []
  } catch (error) {
    throw new Error(describeUnknownError(error, '文档组接口'))
  }
}

/** 网关模式：加载服务端 UI 配置（可选接口，失败时返回空对象） */
export async function fetchServerUiConfig(): Promise<ServerUiConfig> {
  try {
    const res = await fetch('/springdoc-plus-gateway/ui-config', { signal: AbortSignal.timeout(3000) })
    await assertOk(res, 'UI 配置接口')
    if (res.ok) return await res.json() as ServerUiConfig
  } catch (error) {
    console.warn(describeUnknownError(error, 'UI 配置接口'))
  }
  return {}
}
