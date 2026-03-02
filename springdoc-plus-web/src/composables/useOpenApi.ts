import { ref, computed } from 'vue'
import type { OpenApiSpec, TagGroup, OperationItem, HttpMethod } from '@/types/openapi'
import type { MergedConfig } from '@/types'

const HTTP_METHODS: HttpMethod[] = ['get', 'post', 'put', 'delete', 'patch', 'head', 'options', 'trace']

export function useOpenApi(cfg: MergedConfig) {
  const spec = ref<OpenApiSpec | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function load(url: string) {
    loading.value = true
    error.value = null
    spec.value = null
    try {
      const headers: Record<string, string> = {}
      // inject auth header if configured
      if (cfg.authEnabled) {
        const name = (cfg.authHeaderName || 'Authorization').trim()
        let val = (cfg.authValue || '').trim()
        if (name && val) {
          const prefix = (cfg.authDefaultPrefix || '').trim()
          if (prefix && !val.startsWith(prefix + ' ')) val = `${prefix} ${val}`
          headers[name] = val
        }
      }
      const res = await fetch(url, { headers })
      if (!res.ok) throw new Error(`HTTP ${res.status} — 无法加载文档 spec`)
      const contentType = res.headers.get('content-type') || ''
      let data: OpenApiSpec
      if (contentType.includes('yaml') || url.endsWith('.yaml') || url.endsWith('.yml')) {
        // minimal YAML support: just try JSON fallback
        throw new Error('暂不支持 YAML 格式，请使用 JSON spec 地址')
      } else {
        data = await res.json() as OpenApiSpec
      }
      spec.value = data
    } catch (e) {
      error.value = e instanceof Error ? e.message : String(e)
    } finally {
      loading.value = false
    }
  }

  const tagGroups = computed<TagGroup[]>(() => {
    if (!spec.value?.paths) return []

    const s = spec.value
    const groups = new Map<string, TagGroup>()

    // pre-populate from spec.tags to preserve order / description
    const specTags = s.tags ?? []
    specTags.forEach((t, i) => {
      groups.set(t.name, {
        name: t.name,
        description: t.description,
        order: t['x-order'] ?? i,
        operations: [],
      })
    })

    // walk paths
    Object.entries(s.paths ?? {}).forEach(([path, item]) => {
      HTTP_METHODS.forEach((method) => {
        const op = item?.[method]
        if (!op) return
        const tags = op.tags?.length ? op.tags : ['默认']
        tags.forEach((tag) => {
          if (!groups.has(tag)) {
            groups.set(tag, { name: tag, order: groups.size, operations: [] })
          }
          const opItem: OperationItem = {
            method,
            path,
            operation: op,
            order: op['x-order'] ?? groups.get(tag)!.operations.length,
          }
          groups.get(tag)!.operations.push(opItem)
        })
      })
    })

    // sort operations within each group
    const sorter = cfg.operationsSorter
    groups.forEach((g) => {
      g.operations.sort((a, b) => {
        if (sorter === 'order') return a.order - b.order
        return `${a.method}${a.path}`.localeCompare(`${b.method}${b.path}`)
      })
    })

    // sort groups
    const arr = [...groups.values()]
    if (cfg.tagsSorter === 'order') {
      arr.sort((a, b) => a.order - b.order)
    } else {
      arr.sort((a, b) => a.name.localeCompare(b.name))
    }

    return arr
  })

  return { spec, loading, error, load, tagGroups }
}
