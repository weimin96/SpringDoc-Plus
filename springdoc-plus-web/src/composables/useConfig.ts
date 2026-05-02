import { reactive } from 'vue'
import type { AuthHeader, LocalUiConfig, MergedConfig, ServerUiConfig } from '@/types'

const LS_KEY = 'springdoc-plus.ui'
const SS_KEY = 'springdoc-plus.ui.session'

export function getLocalConfig(): LocalUiConfig {
  try {
    return {
      ...JSON.parse(localStorage.getItem(LS_KEY) || '{}'),
      ...JSON.parse(sessionStorage.getItem(SS_KEY) || '{}'),
    }
  }
  catch { return {} }
}

export function setLocalConfig(cfg: LocalUiConfig) {
  localStorage.setItem(LS_KEY, JSON.stringify(cfg))
}

export function clearLocalConfig() {
  localStorage.removeItem(LS_KEY)
  sessionStorage.removeItem(SS_KEY)
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
    if (local.authPersist && local.authStorage === 'local') {
      setLocalConfig(local)
      sessionStorage.removeItem(SS_KEY)
    } else if (local.authPersist && local.authStorage === 'session') {
      const localToSave = sanitizeSensitiveConfig(local)
      setLocalConfig(localToSave)
      sessionStorage.setItem(SS_KEY, JSON.stringify({
        authHeaders: local.authHeaders,
        authValue: local.authValue,
        oauth2ClientSecret: local.oauth2ClientSecret,
        oauth2Password: local.oauth2Password,
      }))
    } else {
      setLocalConfig(sanitizeSensitiveConfig(local))
      sessionStorage.removeItem(SS_KEY)
    }
    syncState()
  }

  function clear() {
    clearLocalConfig()
    syncState({})
  }

  return { state, applyLocal, clear, updateServer }
}

function sanitizeSensitiveConfig(local: LocalUiConfig) {
  const localToSave = { ...local }
  if (localToSave.authHeaders) {
    localToSave.authHeaders = localToSave.authHeaders.map((h: AuthHeader) => ({
      name: h.name,
      defaultPrefix: h.defaultPrefix,
      value: undefined,
    }))
  }
  localToSave.authValue = undefined
  localToSave.oauth2ClientSecret = undefined
  localToSave.oauth2Password = undefined
  return localToSave
}
