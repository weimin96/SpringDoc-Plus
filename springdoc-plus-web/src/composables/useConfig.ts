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
  const state = reactive<MergedConfig>(mergeConfig(serverCfg, getLocalConfig()))

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
      setLocalConfig(localToSave)
    }
    Object.assign(state, mergeConfig(serverCfg, getLocalConfig()))
  }

  function clear() {
    clearLocalConfig()
    Object.assign(state, mergeConfig(serverCfg, {}))
  }

  return { state, applyLocal, clear }
}
