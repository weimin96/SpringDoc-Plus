import type { MergedConfig } from '../types'

declare global {
  interface Window {
    SwaggerUIBundle: any
    SwaggerUIStandalonePreset: any
    ui: any
  }
}

function getXOrder(obj: any): number {
  try {
    if (obj == null) return 0
    if (typeof obj.get === 'function') {
      const v = obj.get('x-order')
      return typeof v === 'number' ? v : parseInt(v || '0', 10)
    }
    const v = obj['x-order'] ?? obj.xOrder ?? obj.x_order
    return typeof v === 'number' ? v : parseInt(v || '0', 10)
  } catch {
    return 0
  }
}

function buildSorters(cfg: MergedConfig) {
  const tagsSorterFn = (a: any, b: any) => {
    const oa = getXOrder(a), ob = getXOrder(b)
    if (oa !== ob) return oa - ob
    const sa = String(a?.name ?? a)
    const sb = String(b?.name ?? b)
    return sa.localeCompare(sb)
  }

  const operationsSorterFn = (a: any, b: any) => {
    const opA = a?.get ? a.get('operation') : a?.operation ?? a
    const opB = b?.get ? b.get('operation') : b?.operation ?? b
    const oa = getXOrder(opA), ob = getXOrder(opB)
    return oa - ob
  }

  return {
    tagsSorter: cfg.tagsSorter === 'order' ? tagsSorterFn : 'alpha',
    operationsSorter: cfg.operationsSorter === 'order' ? operationsSorterFn : 'alpha',
  }
}

function buildRequestInterceptor(cfg: MergedConfig) {
  return (req: any) => {
    if (!cfg.authEnabled) return req
    const headerName = (cfg.authHeaderName || 'Authorization').trim()
    if (!headerName) return req
    let val = (cfg.authValue || '').trim()
    if (!val) return req
    const prefix = (cfg.authDefaultPrefix || '').trim()
    if (prefix && !val.startsWith(prefix + ' ')) val = prefix + ' ' + val
    req.headers[headerName] = val
    return req
  }
}

export function initSwagger(domId: string, url: string, cfg: MergedConfig) {
  const container = document.getElementById(domId)
  if (!container) return

  if (window.ui) {
    try { window.ui = null } catch { /* ignore */ }
  }
  container.innerHTML = ''

  const sorters = buildSorters(cfg)
  window.ui = window.SwaggerUIBundle({
    url,
    dom_id: `#${domId}`,
    deepLinking: true,
    presets: [window.SwaggerUIBundle.presets.apis, window.SwaggerUIStandalonePreset],
    layout: 'BaseLayout',
    requestInterceptor: buildRequestInterceptor(cfg),
    tagsSorter: sorters.tagsSorter,
    operationsSorter: sorters.operationsSorter,
    docExpansion: 'none',
    defaultModelsExpandDepth: 0,
    filter: true,
    tryItOutEnabled: false,
  })
}
