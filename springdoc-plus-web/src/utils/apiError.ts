export class ApiRequestError extends Error {
  readonly status?: number

  constructor(message: string, status?: number) {
    super(message)
    this.name = 'ApiRequestError'
    this.status = status
  }
}

export function describeHttpError(status: number, target: string): string {
  if (status === 401) return `${target} 未授权，请检查 Basic Auth 或请求鉴权配置。`
  if (status === 403) return `${target} 被拒绝访问，请检查 Spring Security 放行路径或网关权限配置。`
  if (status >= 500) return `${target} 服务端异常，请检查下游服务日志或网关转发配置。`
  if (status === 404) return `${target} 不存在，请检查文档路径或分组 URL 配置。`
  return `${target} 请求失败，HTTP ${status}。`
}

export function describeUnknownError(error: unknown, target: string): string {
  if (error instanceof ApiRequestError) return error.message
  if (error instanceof TypeError) return `${target} 网络不可达，请检查服务是否启动、网关路由和跨域配置。`
  if (error instanceof Error) return error.message
  return String(error)
}

export async function assertOk(response: Response, target: string) {
  if (response.ok) return
  throw new ApiRequestError(describeHttpError(response.status, target), response.status)
}
