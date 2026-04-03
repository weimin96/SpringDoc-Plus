<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import AppTopbar from '@/components/AppTopbar.vue'
import AppSidebar from '@/components/AppSidebar.vue'
import ContentArea from '@/components/ContentArea.vue'
import SettingsModal from '@/components/SettingsModal.vue'

import { useConfig } from '@/composables/useConfig'
import { useOpenApi } from '@/composables/useOpenApi'
import { fetchGroups, fetchServerUiConfig } from '@/composables/useApi'

import type { ApiGroup, LocalUiConfig, ServerUiConfig } from '@/types'

// ── State ──────────────────────────────────────────
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

// OpenAPI spec 管理（复用 useOpenApi composable）
const { spec, loading: specLoading, error: specError, load: loadSpec, tagGroups } = useOpenApi(configStore.state)

// ── Actions ────────────────────────────────────────

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

// 选中的分组变化时，加载对应的 OpenAPI spec
watch(activeGroup, (group) => {
  if (group) {
    loadSpec(group.url)
  }
})

// ── Init ───────────────────────────────────────────
onMounted(async () => {
  try {
    const [g, srv] = await Promise.all([
      fetchGroups(),
      fetchServerUiConfig(),
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
      :active-url="activeGroup?.url ?? null"
      :tag-groups="tagGroups"
      :collapsed="sidebarCollapsed"
      :loading="sidebarLoading"
      @select="selectGroup"
      @select-operation="onSelectOperation"
    />

    <ContentArea
      :spec-url="activeGroup?.url ?? null"
      :context-path="activeGroup?.contextPath"
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
