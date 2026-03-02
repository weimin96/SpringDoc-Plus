<script setup lang="ts">
import { ref, computed } from 'vue'
import type { ApiGroup } from '@/types'
import type { TagGroup } from '@/types/openapi'

const props = defineProps<{
  groups: ApiGroup[]
  activeUrl: string | null
  tagGroups: TagGroup[]
  collapsed: boolean
  loading: boolean
}>()

const emit = defineEmits<{
  select: [group: ApiGroup]
  selectOperation: [operation: { method: string; path: string; summary?: string }]
}>()

const keyword = ref('')
const expandedTags = ref<Set<string>>(new Set())

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return props.groups
  return props.groups.filter((g) => g.name.toLowerCase().includes(kw))
})

const filteredTagGroups = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return props.tagGroups
  return props.tagGroups
    .map((g) => ({
      ...g,
      operations: g.operations.filter(
        (op) =>
          op.path.toLowerCase().includes(kw) ||
          op.operation.summary?.toLowerCase().includes(kw) ||
          op.operation.operationId?.toLowerCase().includes(kw),
      ),
    }))
    .filter((g) => g.operations.length > 0)
})

function toggleTag(tagName: string) {
  if (expandedTags.value.has(tagName)) {
    expandedTags.value.delete(tagName)
  } else {
    expandedTags.value.add(tagName)
  }
}

function isTagExpanded(tagName: string): boolean {
  return expandedTags.value.has(tagName)
}
</script>

<template>
  <aside
    class="flex flex-shrink-0 flex-col overflow-hidden border-r border-[var(--c-border)] bg-[var(--c-surface)] transition-[width,opacity] duration-250"
    :class="collapsed ? 'w-0 opacity-0 pointer-events-none' : 'w-64'"
    style="transition-timing-function:cubic-bezier(.4,0,.2,1)"
  >
    <!-- Search -->
    <div class="relative flex-shrink-0 px-3 pb-1.5 pt-3">
      <svg
        class="pointer-events-none absolute left-[22px] top-1/2 h-3 w-3 -translate-y-1/2 text-[var(--c-muted)]"
        viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
      >
        <circle cx="11" cy="11" r="8" />
        <path d="m21 21-4.35-4.35" />
      </svg>
      <input
        v-model="keyword"
        class="w-full rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] py-1.5 pl-[30px] pr-2.5 text-xs text-[var(--c-text)] outline-none transition-[border-color,box-shadow] placeholder:text-[#b0b7c3] focus:border-[var(--c-primary)] focus:bg-white focus:shadow-[0_0_0_3px_rgb(37_99_235_/_0.1)]"
        placeholder="搜索文档组…"
        autocomplete="off"
      />
    </div>

    <!-- Nav -->
    <nav class="flex-1 overflow-y-auto px-2 py-1">
      <!-- 文档组列表 -->
      <p class="px-2 py-2 text-[10px] font-bold uppercase tracking-widest text-[#9ca3af]">文档组</p>

      <!-- Skeleton -->
      <template v-if="loading">
        <div
          v-for="i in 4"
          :key="i"
          class="mx-1.5 my-1 h-[30px] rounded-lg"
          :style="`width:${55 + i * 9}%; background: linear-gradient(90deg,#f0f2f5 25%,#e8eaed 50%,#f0f2f5 75%); background-size:200% 100%; animation:shimmer 1.4s infinite`"
        />
      </template>

      <!-- List -->
      <template v-else>
        <button
          v-for="g in filtered"
          :key="g.url"
          class="flex w-full cursor-pointer items-center gap-2 overflow-hidden rounded-lg border-none px-2.5 py-[7px] text-left text-[13px] text-[var(--c-text)] transition-[background,color] hover:bg-[var(--c-bg)]"
          :class="activeUrl === g.url ? 'bg-[var(--c-primary-light)] font-medium !text-[var(--c-primary)]' : ''"
          :title="g.name"
          @click="emit('select', g)"
        >
          <span
            class="h-[5px] w-[5px] min-w-[5px] flex-shrink-0 rounded-full bg-current opacity-30"
            :class="activeUrl === g.url ? '!opacity-100' : ''"
          />
          <span class="truncate">{{ g.name }}</span>
        </button>
        <p v-if="filtered.length === 0" class="py-6 text-center text-xs text-[var(--c-muted)]">
          无匹配文档组
        </p>
      </template>

      <!-- 分隔线 -->
      <div v-if="activeUrl && filteredTagGroups.length" class="my-3 border-t border-[var(--c-border)]" />

      <!-- 接口分组列表 -->
      <template v-if="activeUrl && filteredTagGroups.length">
        <p class="px-2 py-2 text-[10px] font-bold uppercase tracking-widest text-[#9ca3af]">接口分组</p>

        <template v-for="tagGroup in filteredTagGroups" :key="tagGroup.name">
          <button
            class="flex w-full cursor-pointer items-center gap-2 rounded-lg border-none bg-transparent px-2.5 py-2 text-left transition-colors hover:bg-[var(--c-bg)]"
            @click="toggleTag(tagGroup.name)"
          >
            <svg
              class="h-3.5 w-3.5 flex-shrink-0 text-[var(--c-muted)] transition-transform duration-200"
              :class="isTagExpanded(tagGroup.name) ? 'rotate-0' : '-rotate-90'"
              viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
            >
              <path d="m6 9 6 6 6-6" />
            </svg>
            <span class="flex-1 truncate text-[12px] font-medium text-[var(--c-text)]">
              {{ tagGroup.name }}
            </span>
            <span class="rounded-full bg-gray-100 px-1.5 py-px text-[10px] text-[var(--c-muted)]">
              {{ tagGroup.operations.length }}
            </span>
          </button>

          <div v-if="isTagExpanded(tagGroup.name)" class="ml-5 mt-1">
            <div
              v-for="op in tagGroup.operations"
              :key="`${op.method}-${op.path}`"
              class="mb-0.5 flex cursor-pointer items-center gap-2 rounded-md px-2 py-1.5 text-[11px] text-[var(--c-muted)] hover:bg-[var(--c-bg)] hover:text-[var(--c-text)]"
              :class="activeUrl === op.path ? 'bg-[var(--c-primary-light)] !text-[var(--c-primary)]' : ''"
              @click.stop="emit('selectOperation', { method: op.method, path: op.path, summary: op.operation.summary })"
            >
              <span
                class="shrink-0 px-1 py-px font-mono font-bold text-[9px]"
                :class="{
                  'text-[#3b82f6]': op.method === 'get',
                  'text-[#10b981]': op.method === 'post',
                  'text-[#f59e0b]': op.method === 'put',
                  'text-[#ef4444]': op.method === 'delete',
                  'text-[#8b5cf6]': op.method === 'patch',
                  'text-[#eab308]': op.method === 'head',
                  'text-[#71717a]': op.method === 'options',
                  'text-[#64748b]': op.method === 'trace',
                }"
              >
                {{ op.method.toUpperCase() }}
              </span>
              <span class="truncate font-mono">{{ op.path }}</span>
            </div>
          </div>
        </template>
      </template>
    </nav>

    <!-- Footer -->
    <footer class="flex flex-shrink-0 items-center gap-1.5 border-t border-[var(--c-border)] px-3.5 py-2.5 text-[11px] text-[var(--c-muted)]">
      <span>入口保持</span>
      <code class="rounded border border-[var(--c-border)] bg-[var(--c-bg)] px-1.5 py-px font-mono text-[10.5px] text-[var(--c-text)]">/doc.html</code>
    </footer>
  </aside>
</template>
