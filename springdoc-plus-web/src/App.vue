<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import AppTopbar from '@/components/AppTopbar.vue'
import AppSidebar from '@/components/AppSidebar.vue'
import ContentArea from '@/components/ContentArea.vue'
import SettingsModal from '@/components/SettingsModal.vue'

import { useConfig } from '@/composables/useConfig'
import { detectMode, fetchGroups, fetchServerUiConfig, resolveServiceGroup } from '@/composables/useApi'

import type { ApiGroup, LocalUiConfig, Mode, ServerUiConfig } from '@/types'
import type { OpenApiSpec, HttpMethod } from '@/types/openapi'

// ── State ──────────────────────────────────────────
const mode             = ref<Mode>('gateway')
const groups           = ref<ApiGroup[]>([])
const activeGroup      = ref<ApiGroup | null>(null)
const sidebarLoading   = ref(true)
const sidebarCollapsed = ref(false)
const showSettings     = ref(false)
const serverConfig     = ref<ServerUiConfig>({})

// 视图模式：overview（概览）或 operation（接口详情）
type ViewMode = 'overview' | 'operation'
const viewMode = ref<ViewMode>('overview')

// 当前选中的接口（用于侧边栏点击定位）
const selectedOperation = ref<{ method: string; path: string; summary?: string } | null>(null)

// config store
const configStore = useConfig({})

// Spec and loading state
const spec = ref<OpenApiSpec | null>(null)
const specLoading = ref(false)
const specError = ref<string | null>(null)

const HTTP_METHODS: HttpMethod[] = ['get', 'post', 'put', 'delete', 'patch', 'head', 'options', 'trace']

// Tag groups computed from spec
const tagGroups = computed(() => {
  if (!spec.value?.paths) return []

  const s = spec.value
  const groups = new Map<string, { name: string; description?: string; order: number; operations: any[] }>()

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
        const opItem = {
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
  const sorter = configStore.state.operationsSorter
  groups.forEach((g) => {
    g.operations.sort((a: any, b: any) => {
      if (sorter === 'order') return a.order - b.order
      return `${a.method}${a.path}`.localeCompare(`${b.method}${b.path}`)
    })
  })

  // sort groups
  const arr = [...groups.values()]
  if (configStore.state.tagsSorter === 'order') {
    arr.sort((a, b) => a.order - b.order)
  } else {
    arr.sort((a, b) => a.name.localeCompare(b.name))
  }

  return arr
})

// ── Actions ────────────────────────────────────────
async function loadSpec(url: string) {
  specLoading.value = true
  specError.value = null
  try {
    const headers: Record<string, string> = {}
    // inject auth header if configured
    if (configStore.state.authEnabled) {
      const name = (configStore.state.authHeaderName || 'Authorization').trim()
      let val = (configStore.state.authValue || '').trim()
      if (name && val) {
        const prefix = (configStore.state.authDefaultPrefix || '').trim()
        if (prefix && !val.startsWith(prefix + ' ')) val = `${prefix} ${val}`
        headers[name] = val
      }
    }
    const res = await fetch(url, { headers })
    if (!res.ok) throw new Error(`HTTP ${res.status} — 无法加载文档 spec`)
    const contentType = res.headers.get('content-type') || ''
    if (contentType.includes('yaml') || url.endsWith('.yaml') || url.endsWith('.yml')) {
      throw new Error('暂不支持 YAML 格式，请使用 JSON spec 地址')
    }
    spec.value = await res.json() as OpenApiSpec
  } catch (e) {
    specError.value = e instanceof Error ? e.message : String(e)
  } finally {
    specLoading.value = false
  }
}

function selectGroup(group: ApiGroup) {
  activeGroup.value = group
  selectedOperation.value = null
  viewMode.value = 'overview'
}

function onSelectOperation(op: { method: string; path: string; summary?: string }) {
  selectedOperation.value = op
  viewMode.value = 'operation'
}

function onReturnToOverview() {
  selectedOperation.value = null
  viewMode.value = 'overview'
}

function onApply(local: LocalUiConfig) {
  configStore.applyLocal(local)
  showSettings.value = false
  // 重新加载当前选中的 spec
  const current = activeGroup.value
  if (current) {
    loadSpec(current.url)
  }
}

// 当选中的文档组变化时，加载对应的 OpenAPI spec
watch(activeGroup, (group) => {
  if (group) {
    loadSpec(group.url)
  }
})

// ── Init ───────────────────────────────────────────
onMounted(async () => {
  try {
    mode.value = await detectMode()

    const [g, srv] = await Promise.all([
      mode.value === 'gateway' ? fetchGroups() : resolveServiceGroup(),
      mode.value === 'gateway' ? fetchServerUiConfig() : Promise.resolve({} as ServerUiConfig),
    ])

    groups.value = g
    serverConfig.value = srv

    // 更新 config store
    Object.assign(configStore.state, useConfig(srv).state)

    if (!configStore.state.authPersist) {
      configStore.state.authValue = ''
    }

    if (g.length) selectGroup(g[0])
  } catch (_e) {
    // errors are handled inside ContentArea via specUrl=null
  } finally {
    sidebarLoading.value = false
  }
})
</script>

<template>
  <AppTopbar
    :mode="mode"
    :active-group="activeGroup"
    :sidebar-collapsed="sidebarCollapsed"
    @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed"
    @open-settings="showSettings = true"
  />

  <div class="flex flex-1 overflow-hidden">
    <AppSidebar
      :groups="groups"
      :active-url="activeGroup?.url ?? null"
      :tag-groups="tagGroups"
      :collapsed="sidebarCollapsed"
      :loading="sidebarLoading"
      @select="selectGroup"
      @select-operation="onSelectOperation"
    />

    <ContentArea
      :spec-url="activeGroup?.url ?? null"
      :config="configStore.state"
      :selected-operation="selectedOperation"
      :view-mode="viewMode"
      @operation-clicked="onReturnToOverview"
    />
  </div>

  <SettingsModal
    :visible="showSettings"
    :config="configStore.state"
    :server-config="serverConfig"
    @close="showSettings = false"
    @apply="onApply"
    @clear="configStore.clear()"
  />
</template>
