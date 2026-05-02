/* ═══════════════════════════════════════════
   Types
═══════════════════════════════════════════ */

/** 文档组（网关模式） */
export interface ApiGroup {
  name: string
  url: string
  /** 用于模拟请求的 basePath（网关模式时服务可能需要前缀） */
  contextPath?: string
  /** 文档组最近一次加载状态，用于在下游文档不可达时降级展示 */
  status?: 'unknown' | 'online' | 'offline'
  /** 文档组最近一次加载失败原因 */
  statusMessage?: string
}

/** 单个鉴权请求头配置 */
export interface AuthHeader {
  name: string
  defaultPrefix?: string
  value?: string
}

/** 服务端返回的 UI 配置（可选字段） */
export interface ServerUiConfig {
  tagsSorter?:       'alpha' | 'order'
  operationsSorter?: 'alpha' | 'order'
  gatewayBasicEnabled?: boolean
  authEnabled?:      boolean
  /** 兼容旧版本单 header 配置 */
  authHeaderName?:   string
  authDefaultPrefix?: string
  /** 多个鉴权请求头 */
  authHeaders?:      AuthHeader[]
  authPersist?:      boolean
  authStorage?:      'local' | 'session'
  oauth2Enabled?:    boolean
  oauth2TokenUrl?:   string
  oauth2ClientId?:   string
  oauth2Scope?:      string
  oauth2GrantType?:  'client_credentials' | 'password'
}

/** 用户本地持久化的配置 */
export interface LocalUiConfig {
  tagsSorter?:        'alpha' | 'order'
  operationsSorter?:  'alpha' | 'order'
  authEnabled?:       boolean
  /** 兼容旧版本单 header 配置 */
  authHeaderName?:    string
  authDefaultPrefix?: string
  authValue?:         string
  /** 多个鉴权请求头 */
  authHeaders?:       AuthHeader[]
  authPersist?:       boolean
  authStorage?:       'local' | 'session'
  oauth2Enabled?:     boolean
  oauth2TokenUrl?:    string
  oauth2ClientId?:    string
  oauth2ClientSecret?: string
  oauth2Scope?:       string
  oauth2GrantType?:   'client_credentials' | 'password'
  oauth2Username?:    string
  oauth2Password?:    string
}

export interface MergedConfig extends ServerUiConfig, LocalUiConfig {}
