import { reactive } from 'vue'
import type { LocalUiConfig, MergedConfig, ServerUiConfig } from '@/types'

const LS_KEY = 'springdoc-plus.ui'

export function getLocalConfig(): LocalUiConfig {
  try { return JSON.parse(localStorage.getItem(LS_KEY) || '{}') }
  catch { return {} }
}

export function setLocalConfig(cfg: Omit<LocalUiConfig, 'authValue'> & { authValue?: string }) {
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
    if (local.authPersist) {
      setLocalConfig(local)
    } else {
      const { authValue: _v, ...rest } = local
      setLocalConfig(rest)
    }
    Object.assign(state, mergeConfig(serverCfg, getLocalConfig()))
  }

  function clear() {
    clearLocalConfig()
    Object.assign(state, mergeConfig(serverCfg, {}))
  }

  return { state, applyLocal, clear }
}
