import { ref, computed } from 'vue'
import yaml from 'js-yaml'
import type { OpenApiSpec, TagGroup, OperationItem, HttpMethod } from '@/types/openapi'
import type { MergedConfig } from '@/types'
import { buildAuthHeaders, buildLegacyAuthHeader } from '@/utils/auth'

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
      // ── Build auth headers (unified via utils/auth) ────────────────────
      let headers: Record<string, string> = {}
      if (cfg.authEnabled) {
        if (cfg.authHeaders && cfg.authHeaders.length > 0) {
          // 新版多 Header 配置
          headers = buildAuthHeaders(cfg.authHeaders)
        } else if (cfg.authHeaderName) {
          // 旧版单 Header 配置兼容
          headers = buildLegacyAuthHeader(cfg.authHeaderName, cfg.authValue, cfg.authDefaultPrefix)
        }
      }

      const res = await fetch(url, { headers })
      if (!res.ok) throw new Error(`HTTP ${res.status} — 无法加载文档 spec`)

      const contentType = res.headers.get('content-type') || ''
      const responseText = await res.text()
      let data: OpenApiSpec

      if (contentType.includes('yaml') || url.endsWith('.yaml') || url.endsWith('.yml')) {
        try {
          data = yaml.load(responseText) as OpenApiSpec
        } catch (e) {
          throw new Error(`YAML 格式解析失败: ${e instanceof Error ? e.message : String(e)}`)
        }
      } else {
        try {
          data = JSON.parse(responseText) as OpenApiSpec
        } catch (e) {
          throw new Error(`JSON 格式解析失败: ${e instanceof Error ? e.message : String(e)}`)
        }
      }

      if (!data || typeof data !== 'object') {
        throw new Error('无效的 OpenAPI 文档格式')
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
