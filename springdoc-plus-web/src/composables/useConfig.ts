import { reactive } from 'vue'
import type { AuthHeader, LocalUiConfig, MergedConfig, ServerUiConfig } from '@/types'

const LS_KEY = 'springdoc-plus.ui'

export function getLocalConfig(): LocalUiConfig {
  try { return JSON.parse(localStorage.getItem(LS_KEY) || '{}') }
  catch { return {} }
}

export function setLocalConfig(cfg: LocalUiConfig) {
  localStorage.setItem(LS_KEY, JSON.stringify(cfg))
}

export function clearLocalConfig() {
  localStorage.removeItem(LS_KEY)
}

export function mergeConfig(server: ServerUiConfig, local: LocalUiConfig): MergedConfig {
  return { ...server, ...local }
}

export function useConfig(serverCfg: ServerUiConfig) {
  let currentServerConfig = { ...serverCfg }
  const state = reactive<MergedConfig>(mergeConfig(serverCfg, getLocalConfig()))

  function replaceState(nextState: MergedConfig) {
    for (const key of Object.keys(state) as Array<keyof MergedConfig>) {
      delete state[key]
    }
    Object.assign(state, nextState)
  }

  function syncState(local: LocalUiConfig = getLocalConfig()) {
    replaceState(mergeConfig(currentServerConfig, local))
  }

  function updateServer(nextServerConfig: ServerUiConfig) {
    currentServerConfig = { ...nextServerConfig }
    syncState()
  }

  function applyLocal(local: LocalUiConfig) {
    // 持久化时需要处理 authHeaders
    if (local.authPersist) {
      setLocalConfig(local)
    } else {
      // 不持久化时，不保存 authValue
      const localToSave = { ...local }
      if (localToSave.authHeaders) {
        localToSave.authHeaders = localToSave.authHeaders.map((h: AuthHeader) => ({
          name: h.name,
          defaultPrefix: h.defaultPrefix,
          value: undefined,
        }))
      }
      localToSave.oauth2ClientSecret = undefined
      localToSave.oauth2Password = undefined
      setLocalConfig(localToSave)
    }
    syncState()
  }

  function clear() {
    clearLocalConfig()
    syncState({})
  }

  return { state, applyLocal, clear, updateServer }
}
