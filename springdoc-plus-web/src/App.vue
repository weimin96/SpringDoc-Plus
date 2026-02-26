<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import SettingsModal from './components/SettingsModal.vue'
import type { ApiGroup, ServerUiConfig, LocalUiConfig, MergedConfig } from './types'
import { mergeConfig, getLocalConfig, setLocalConfig, clearLocalConfig } from './composables/useConfig'
import { initSwagger } from './composables/useSwagger'

// ── State ─────────────────────────────────────────
const groups = ref<ApiGroup[]>([])
const activeGroup = ref<ApiGroup | null>(null)
const serverConfig = ref<ServerUiConfig>({})
const mergedConfig = ref<MergedConfig>({})
const loading = ref(true)
const error = ref<string | null>(null)
const showSettings = ref(false)
const searchKeyword = ref('')
const sidebarCollapsed = ref(false)

// ── Fetch helpers ─────────────────────────────────
async function loadGroups(): Promise<ApiGroup[]> {
  const res = await fetch('/springdoc-plus-gateway/openapi/groups')
  if (!res.ok) throw new Error(`加载文档组失败: ${res.status}`)
  const data = await res.json()
  return data.groups || []
}

async function loadServerUiConfig(): Promise<ServerUiConfig> {
  try {
    const res = await fetch('/springdoc-plus-gateway/ui-config')
    if (!res.ok) return {}
    return await res.json()
  } catch {
    return {}
  }
}

// ── Swagger render ────────────────────────────────
async function renderGroup(group: ApiGroup) {
  activeGroup.value = group
  await nextTick()
  initSwagger('swagger-root', group.url, mergedConfig.value)
}

// ── Settings modal ────────────────────────────────
function onApply(local: LocalUiConfig) {
  if (local.authPersist) {
    setLocalConfig(local)
  } else {
    const { authValue: _, ...rest } = local
    setLocalConfig(rest)
  }
  mergedConfig.value = mergeConfig(serverConfig.value, getLocalConfig())
  showSettings.value = false
  if (activeGroup.value) renderGroup(activeGroup.value)
}

function onClear() {
  clearLocalConfig()
  mergedConfig.value = mergeConfig(serverConfig.value, {})
  if (activeGroup.value) renderGroup(activeGroup.value)
}

// ── Filtered groups ───────────────────────────────
function filteredGroups() {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) return groups.value
  return groups.value.filter(g => g.name.toLowerCase().includes(kw))
}

// ── Init ─────────────────────────────────────────
onMounted(async () => {
  try {
    const [g, srv] = await Promise.all([loadGroups(), loadServerUiConfig()])
    groups.value = g
    serverConfig.value = srv
    const local = getLocalConfig()
    mergedConfig.value = mergeConfig(srv, local)
    if (!mergedConfig.value.authPersist) {
      mergedConfig.value.authValue = ''
    }
    if (g.length) {
      await renderGroup(g[0])
    }
  } catch (e: any) {
    error.value = e?.message ?? String(e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="app-shell">
    <!-- ── Top bar ──────────────────────────────────── -->
    <header class="topbar">
      <button class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed" :title="sidebarCollapsed ? '展开侧边栏' : '收起侧边栏'">
        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 6h18M3 12h18M3 18h18"/>
        </svg>
      </button>

      <div class="brand">
        <div class="brand-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="w-4 h-4">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
            <polyline points="10 9 9 9 8 9"/>
          </svg>
        </div>
        <span class="brand-name">SpringDoc <em>Plus</em></span>
      </div>

      <div class="topbar-divider" />

      <div v-if="activeGroup" class="current-group">
        <span class="tag-dot" />
        {{ activeGroup.name }}
      </div>

      <div style="flex:1" />

      <div class="topbar-actions">
        <a
          v-if="activeGroup"
          :href="activeGroup.url"
          target="_blank"
          class="action-btn"
          title="在新窗口打开 JSON"
        >
          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/>
            <polyline points="15 3 21 3 21 9"/>
            <line x1="10" y1="14" x2="21" y2="3"/>
          </svg>
          <span>OpenAPI JSON</span>
        </a>

        <button class="action-btn primary" @click="showSettings = true">
          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/>
          </svg>
          <span>配置</span>
        </button>
      </div>
    </header>

    <!-- ── Main layout ─────────────────────────────── -->
    <div class="layout" :class="{ 'sidebar-hidden': sidebarCollapsed }">

      <!-- Sidebar -->
      <aside class="sidebar">
        <div class="sidebar-search">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
          </svg>
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="搜索文档组..."
          />
        </div>

        <nav class="sidebar-nav">
          <div class="nav-section-title">文档组</div>

          <div v-if="loading" class="nav-skeleton">
            <div v-for="i in 4" :key="i" class="skeleton-item" :style="`width: ${60 + i * 8}%`" />
          </div>

          <div v-else-if="error" class="nav-error">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/>
            </svg>
            {{ error }}
          </div>

          <template v-else>
            <button
              v-for="g in filteredGroups()"
              :key="g.url"
              class="nav-item"
              :class="{ active: activeGroup?.url === g.url }"
              @click="renderGroup(g)"
            >
              <span class="nav-dot" />
              {{ g.name }}
            </button>
            <div v-if="filteredGroups().length === 0" class="nav-empty">
              无匹配文档组
            </div>
          </template>
        </nav>

        <div class="sidebar-footer">
          <div class="sidebar-footer-text">
            <span>入口保持</span>
            <code>/doc.html</code>
          </div>
        </div>
      </aside>

      <!-- Content -->
      <main class="content">
        <div v-if="loading" class="content-loading">
          <div class="spinner" />
          <p>正在加载文档…</p>
        </div>
        <div v-else-if="error" class="content-error">
          <svg class="w-8 h-8 text-red-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/>
          </svg>
          <h2>加载失败</h2>
          <p>{{ error }}</p>
        </div>
        <div v-else-if="!groups.length" class="content-error">
          <svg class="w-8 h-8 text-gray-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          </svg>
          <h2>暂无文档</h2>
          <p>当前没有可用的 API 文档组</p>
        </div>
        <div v-else id="swagger-root" class="swagger-wrapper" />
      </main>
    </div>
  </div>

  <!-- Settings Modal -->
  <SettingsModal
    :visible="showSettings"
    :config="mergedConfig"
    :serverConfig="serverConfig"
    @close="showSettings = false"
    @apply="onApply"
    @clear="onClear"
  />
</template>

<style scoped>
/* ── Shell ─────────────────────────────────────── */
.app-shell {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

/* ── Topbar ────────────────────────────────────── */
.topbar {
  height: var(--topbar-h);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid var(--c-border);
  background: var(--c-surface);
  flex-shrink: 0;
  position: relative;
  z-index: 10;
}

.sidebar-toggle {
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 8px; border: none;
  background: transparent;
  color: var(--c-muted);
  cursor: pointer;
  transition: background .15s;
  flex-shrink: 0;
}
.sidebar-toggle:hover { background: var(--c-border); color: var(--c-text); }

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.brand-icon {
  width: 28px; height: 28px;
  background: var(--c-primary);
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  color: #fff;
}
.brand-name {
  font-size: 14px;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: var(--c-text);
}
.brand-name em {
  font-style: normal;
  color: var(--c-primary);
}

.topbar-divider {
  width: 1px;
  height: 20px;
  background: var(--c-border);
  flex-shrink: 0;
}

.current-group {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--c-muted);
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.tag-dot {
  width: 6px; height: 6px;
  background: var(--c-success);
  border-radius: 50%;
  flex-shrink: 0;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  border-radius: 8px;
  border: 1px solid var(--c-border);
  background: transparent;
  font-size: 12px;
  color: var(--c-muted);
  cursor: pointer;
  text-decoration: none;
  transition: all .15s;
  white-space: nowrap;
}
.action-btn:hover { background: var(--c-bg); color: var(--c-text); }
.action-btn.primary {
  border-color: var(--c-primary);
  color: var(--c-primary);
  background: var(--c-primary-light);
}
.action-btn.primary:hover {
  background: var(--c-primary);
  color: #fff;
}

/* ── Layout ────────────────────────────────────── */
.layout {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* ── Sidebar ────────────────────────────────────── */
.sidebar {
  width: var(--sidebar-w);
  flex-shrink: 0;
  border-right: 1px solid var(--c-border);
  background: var(--c-surface);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width .25s ease, opacity .25s;
}

.layout.sidebar-hidden .sidebar {
  width: 0;
  opacity: 0;
  overflow: hidden;
}

.sidebar-search {
  padding: 12px 12px 8px;
  position: relative;
}
.search-icon {
  position: absolute;
  left: 22px; top: 50%;
  transform: translateY(-50%);
  width: 14px; height: 14px;
  color: var(--c-muted);
  pointer-events: none;
}
.search-input {
  width: 100%;
  padding: 6px 10px 6px 32px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  font-size: 12px;
  outline: none;
  background: var(--c-bg);
  color: var(--c-text);
  transition: border-color .15s, box-shadow .15s;
}
.search-input:focus {
  border-color: var(--c-primary);
  box-shadow: 0 0 0 3px rgba(37,99,235,.1);
  background: #fff;
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px 8px;
}

.nav-section-title {
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--c-muted);
  padding: 8px 8px 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 7px 10px;
  border-radius: 8px;
  border: none;
  background: transparent;
  font-size: 13px;
  color: var(--c-text);
  cursor: pointer;
  text-align: left;
  transition: background .12s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.nav-item:hover { background: var(--c-bg); }
.nav-item.active {
  background: var(--c-primary-light);
  color: var(--c-primary);
  font-weight: 500;
}
.nav-dot {
  width: 5px; height: 5px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.4;
  flex-shrink: 0;
}
.nav-item.active .nav-dot { opacity: 1; }

.nav-skeleton {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 4px 8px;
}
.skeleton-item {
  height: 30px;
  border-radius: 8px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.nav-error {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 10px;
  font-size: 12px;
  color: var(--c-danger);
  background: #fef2f2;
  border-radius: 8px;
  margin: 4px 4px;
}

.nav-empty {
  font-size: 12px;
  color: var(--c-muted);
  text-align: center;
  padding: 20px 10px;
}

.sidebar-footer {
  border-top: 1px solid var(--c-border);
  padding: 10px 14px;
  flex-shrink: 0;
}
.sidebar-footer-text {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--c-muted);
}
.sidebar-footer-text code {
  font-family: var(--font-mono);
  font-size: 11px;
  background: var(--c-bg);
  border: 1px solid var(--c-border);
  border-radius: 4px;
  padding: 1px 5px;
  color: var(--c-text);
}

/* ── Content ─────────────────────────────────── */
.content {
  flex: 1;
  overflow-y: auto;
  background: var(--c-bg);
}

.swagger-wrapper {
  padding: 0 24px 40px;
  max-width: 1100px;
  margin: 0 auto;
}

.content-loading, .content-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  height: 60%;
  color: var(--c-muted);
  text-align: center;
}
.content-error h2 {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
  margin: 0;
}
.content-error p {
  font-size: 13px;
  margin: 0;
  max-width: 360px;
}

.spinner {
  width: 32px; height: 32px;
  border: 3px solid var(--c-border);
  border-top-color: var(--c-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
