/* ═══════════════════════════════════════════
   Types
═══════════════════════════════════════════ */

/** 进入模式 */
export type Mode = 'gateway' | 'service'

/** 文档组（网关模式） */
export interface ApiGroup {
  name: string
  url: string
  /** 用于模拟请求的 basePath（网关模式时服务可能需要前缀） */
  contextPath?: string
}

/** 服务端返回的 UI 配置（可选字段） */
export interface ServerUiConfig {
  tagsSorter?:       'alpha' | 'order'
  operationsSorter?: 'alpha' | 'order'
  gatewayBasicEnabled?: boolean
  authEnabled?:      boolean
  authHeaderName?:   string
  authDefaultPrefix?: string
  authPersist?:      boolean
}

/** 用户本地持久化的配置 */
export interface LocalUiConfig {
  tagsSorter?:        'alpha' | 'order'
  operationsSorter?:  'alpha' | 'order'
  authEnabled?:       boolean
  authHeaderName?:    string
  authDefaultPrefix?: string
  authValue?:         string
  authPersist?:       boolean
}

export interface MergedConfig extends ServerUiConfig, LocalUiConfig {}
