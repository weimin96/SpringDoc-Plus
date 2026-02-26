export interface ApiGroup {
  name: string
  url: string
}

export interface ServerUiConfig {
  tagsSorter?: 'alpha' | 'order'
  operationsSorter?: 'alpha' | 'order'
  gatewayBasicEnabled?: boolean
  authEnabled?: boolean
  authHeaderName?: string
  authDefaultPrefix?: string
  authPersist?: boolean
}

export interface LocalUiConfig {
  tagsSorter?: 'alpha' | 'order'
  operationsSorter?: 'alpha' | 'order'
  authEnabled?: boolean
  authHeaderName?: string
  authDefaultPrefix?: string
  authValue?: string
  authPersist?: boolean
  theme?: 'light' | 'dark'
  sidebarCollapsed?: boolean
}

export interface MergedConfig extends ServerUiConfig, LocalUiConfig {}
