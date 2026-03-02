<script setup lang="ts">
import type { ApiGroup, Mode } from '@/types'

defineProps<{
  mode: Mode
  activeGroup: ApiGroup | null
  sidebarCollapsed: boolean
}>()

const emit = defineEmits<{
  toggleSidebar: []
  openSettings: []
}>()
</script>

<template>
  <header
    class="flex h-[52px] flex-shrink-0 items-center gap-2.5 border-b border-[var(--c-border)] bg-[var(--c-surface)] px-4"
    style="z-index:50"
  >
    <!-- Left -->
    <div class="flex min-w-0 flex-1 items-center gap-2.5">
      <!-- Hamburger -->
      <button
        class="flex h-8 w-8 flex-shrink-0 cursor-pointer items-center justify-center rounded-lg border-none bg-transparent text-[var(--c-muted)] transition-colors hover:bg-[var(--c-border)] hover:text-[var(--c-text)]"
        :title="sidebarCollapsed ? '展开侧边栏' : '收起侧边栏'"
        @click="emit('toggleSidebar')"
      >
        <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <path d="M3 6h18M3 12h18M3 18h18" />
        </svg>
      </button>

      <!-- Brand -->
      <div class="flex flex-shrink-0 items-center gap-2">
        <div class="flex h-7 w-7 items-center justify-center rounded-lg bg-[var(--c-primary)] text-white">
          <svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
            <line x1="16" y1="13" x2="8" y2="13" />
            <line x1="16" y1="17" x2="8" y2="17" />
            <polyline points="10 9 9 9 8 9" />
          </svg>
        </div>
        <span class="text-sm font-bold tracking-tight text-[var(--c-text)]">
          SpringDoc <em class="not-italic text-[var(--c-primary)]">Plus</em>
        </span>
      </div>

      <!-- Divider -->
      <div class="h-5 w-px flex-shrink-0 bg-[var(--c-border)]" />

      <!-- Mode badge -->
      <span
        class="flex-shrink-0 rounded-full px-1.5 py-0.5 text-[10px] font-bold uppercase tracking-wider"
        :class="mode === 'gateway'
          ? 'bg-blue-100 text-blue-800'
          : 'bg-green-100 text-green-800'"
      >
        {{ mode === 'gateway' ? '网关' : '子服务' }}
      </span>

      <!-- Current group -->
      <div v-if="activeGroup" class="flex min-w-0 items-center gap-1.5">
        <span class="h-1.5 w-1.5 flex-shrink-0 rounded-full bg-[var(--c-success)]" style="animation:pulse-ring 2.4s ease-in-out infinite" />
        <span class="truncate text-[13px] text-[var(--c-muted)]">{{ activeGroup.name }}</span>
      </div>
    </div>

    <!-- Right -->
    <div class="flex flex-shrink-0 items-center gap-2">
      <!-- Open JSON -->
      <a
        v-if="activeGroup"
        :href="activeGroup.url"
        target="_blank"
        class="inline-flex cursor-pointer items-center gap-1.5 rounded-lg border border-[var(--c-border)] bg-transparent px-3 py-[5px] text-xs text-[var(--c-muted)] no-underline transition-all hover:bg-[var(--c-bg)] hover:text-[var(--c-text)]"
      >
        <svg class="h-3 w-3 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
          <polyline points="15 3 21 3 21 9" />
          <line x1="10" y1="14" x2="21" y2="3" />
        </svg>
        OpenAPI JSON
      </a>

      <!-- Settings -->
      <button
        class="inline-flex cursor-pointer items-center gap-1.5 rounded-lg border border-[var(--c-primary)] bg-[var(--c-primary-light)] px-3 py-[5px] text-xs font-medium text-[var(--c-primary)] transition-all hover:bg-[var(--c-primary)] hover:text-white"
        @click="emit('openSettings')"
      >
        <svg class="h-3 w-3 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="3" />
          <path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14M12 2v2M12 20v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M2 12h2M20 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
        </svg>
        配置
      </button>
    </div>
  </header>
</template>
