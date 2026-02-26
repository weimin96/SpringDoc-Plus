import { reactive } from 'vue'
import type { LocalUiConfig, MergedConfig, ServerUiConfig } from '../types'

const LS_KEY = 'springdoc-plus.ui'

export function getLocalConfig(): LocalUiConfig {
  try {
    return JSON.parse(localStorage.getItem(LS_KEY) || '{}')
  } catch {
    return {}
  }
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

  function apply(local: LocalUiConfig) {
    if (local.authPersist) {
      setLocalConfig(local)
    } else {
      const { authValue: _, ...rest } = local
      setLocalConfig(rest)
    }
    const fresh = mergeConfig(serverCfg, getLocalConfig())
    Object.assign(state, fresh)
  }

  function clear() {
    clearLocalConfig()
    Object.assign(state, mergeConfig(serverCfg, {}))
  }

  return { state, apply, clear }
}
