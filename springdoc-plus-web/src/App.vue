<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import AppTopbar from '@/components/AppTopbar.vue'
import AppSidebar from '@/components/AppSidebar.vue'
import ContentArea from '@/components/ContentArea.vue'
import SettingsModal from '@/components/SettingsModal.vue'

import { useConfig } from '@/composables/useConfig'
import { useOpenApi } from '@/composables/useOpenApi'
import { fetchGroups, fetchServerUiConfig } from '@/composables/useApi'

import type { ApiGroup, LocalUiConfig, ServerUiConfig } from '@/types'

const groups = ref<ApiGroup[]>([])
const activeGroup = ref<ApiGroup | null>(null)
const sidebarLoading = ref(true)
const sidebarCollapsed = ref(false)
const showSettings = ref(false)
const serverConfig = ref<ServerUiConfig>({})
const groupsError = ref<string | null>(null)

type ViewMode = 'overview' | 'operation'

interface RouteState {
  groupUrl: string | null
  viewMode: ViewMode
  method?: string
  path?: string
}

const viewMode = ref<ViewMode>('overview')
const selectedOperation = ref<{ method: string; path: string; summary?: string } | null>(null)
const pendingRouteState = ref<RouteState | null>(null)

const configStore = useConfig({})
const { spec, loading: specLoading, error: specError, load: loadSpec, tagGroups } = useOpenApi(configStore.state)

const activeOperationKey = computed(() =>
  selectedOperation.value ? `${selectedOperation.value.method}:${selectedOperation.value.path}` : null,
)
const contentError = computed(() => groupsError.value ?? specError.value)

function normalizeOperation(method?: string | null, path?: string | null) {
  if (!method || !path) {
    return null
  }
  return {
    method: method.toLowerCase(),
    path,
  }
}

function parseRouteState(): RouteState {
  const url = new URL(window.location.href)
  const groupUrl = url.searchParams.get('group')
  const operation = normalizeOperation(url.searchParams.get('method'), url.searchParams.get('path'))

  return {
    groupUrl,
    viewMode: operation ? 'operation' : 'overview',
    method: operation?.method,
    path: operation?.path,
  }
}

function syncRouteState(replace = false) {
  const url = new URL(window.location.href)

  if (activeGroup.value?.url) {
    url.searchParams.set('group', activeGroup.value.url)
  } else {
    url.searchParams.delete('group')
  }

  if (viewMode.value === 'operation' && selectedOperation.value) {
    url.searchParams.set('view', 'operation')
    url.searchParams.set('method', selectedOperation.value.method)
    url.searchParams.set('path', selectedOperation.value.path)
  } else {
    url.searchParams.delete('view')
    url.searchParams.delete('method')
    url.searchParams.delete('path')
  }

  const nextUrl = `${url.pathname}${url.search}${url.hash}`
  if (replace) {
    window.history.replaceState(null, '', nextUrl)
  } else {
    window.history.pushState(null, '', nextUrl)
  }
}

function selectGroup(group: ApiGroup, replace = false) {
  activeGroup.value = group
  selectedOperation.value = null
  viewMode.value = 'overview'
  syncRouteState(replace)
}

function onSelectOperation(op: { method: string; path: string; summary?: string }, replace = false) {
  selectedOperation.value = op
  viewMode.value = 'operation'
  syncRouteState(replace)
}

function onReturnToOverview(replace = false) {
  selectedOperation.value = null
  viewMode.value = 'overview'
  syncRouteState(replace)
}

function applyRouteState(route: RouteState, replace = false) {
  if (!groups.value.length) {
    pendingRouteState.value = route
    return
  }

  const targetGroup = groups.value.find((group) => group.url === route.groupUrl) ?? groups.value[0] ?? null
  if (!targetGroup) {
    return
  }

  if (!activeGroup.value || activeGroup.value.url !== targetGroup.url) {
    activeGroup.value = targetGroup
  }

  const operation = normalizeOperation(route.method, route.path)
  if (!operation) {
    selectedOperation.value = null
    viewMode.value = 'overview'
    pendingRouteState.value = null
    syncRouteState(replace)
    return
  }

  const found = tagGroups.value
    .flatMap((group) => group.operations)
    .find((item) => item.method === operation.method && item.path === operation.path)

  if (!found) {
    pendingRouteState.value = route
    return
  }

  selectedOperation.value = {
    method: found.method,
    path: found.path,
    summary: found.operation.summary,
  }
  viewMode.value = 'operation'
  pendingRouteState.value = null
  syncRouteState(replace)
}

function onApply(local: LocalUiConfig) {
  configStore.applyLocal(local)
  showSettings.value = false
  const current = activeGroup.value
  if (current) {
    loadSpec(current.url)
  }
}

watch(activeGroup, (group) => {
  if (group) {
    loadSpec(group.url)
  }
})

watch(specError, (message) => {
  const group = activeGroup.value
  if (!group) return
  group.status = message ? 'offline' : 'online'
  group.statusMessage = message ?? ''
})

watch(spec, (value) => {
  const group = activeGroup.value
  if (!group || !value) return
  group.status = 'online'
  group.statusMessage = ''
})

watch(tagGroups, () => {
  if (pendingRouteState.value) {
    applyRouteState(pendingRouteState.value, true)
  }
})

const handlePopstate = () => {
  applyRouteState(parseRouteState(), true)
}

onMounted(async () => {
  pendingRouteState.value = parseRouteState()
  window.addEventListener('popstate', handlePopstate)
  groupsError.value = null

  try {
    const [g, srv] = await Promise.all([
      fetchGroups(),
      fetchServerUiConfig(),
    ])

    groups.value = g.map(group => ({ ...group, status: 'unknown' }))
    serverConfig.value = srv

    configStore.updateServer(srv)

    if (!configStore.state.authPersist) {
      configStore.state.authValue = ''
    }

    if (g.length) {
      if (pendingRouteState.value) {
        applyRouteState(pendingRouteState.value, true)
      } else {
        selectGroup(g[0], true)
      }
    }
  } catch (e) {
    groups.value = []
    activeGroup.value = null
    groupsError.value = e instanceof Error ? e.message : String(e)
  } finally {
    sidebarLoading.value = false
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('popstate', handlePopstate)
})
</script>

<template>
  <AppTopbar
    :active-group="activeGroup"
    :sidebar-collapsed="sidebarCollapsed"
    :spec="spec"
    :spec-url="activeGroup?.url ?? null"
    @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed"
    @open-settings="showSettings = true"
  />

  <div class="flex flex-1 overflow-hidden">
    <AppSidebar
      :groups="groups"
      :active-group-url="activeGroup?.url ?? null"
      :active-operation-key="activeOperationKey"
      :tag-groups="tagGroups"
      :collapsed="sidebarCollapsed"
      :loading="sidebarLoading"
      @select="selectGroup"
      @select-operation="onSelectOperation"
    />

    <ContentArea
      :spec-url="activeGroup?.url ?? null"
      :spec="spec"
      :loading="specLoading"
      :error="contentError"
      :tag-groups="tagGroups"
      :context-path="activeGroup?.contextPath"
      :config="configStore.state"
      :selected-operation="selectedOperation"
      :view-mode="viewMode"
      @operation-clicked="onReturnToOverview"
      @select-operation="onSelectOperation"
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
