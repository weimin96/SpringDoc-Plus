<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import type { TagGroup } from '@/types/openapi'
import type { SchemaObject } from '@/types/openapi'
import OperationPanel from './OperationPanel.vue'

const props = defineProps<{
  group: TagGroup
  schemas?: Record<string, SchemaObject>
}>()

const open = ref(true)

// 监听全局事件，展开并滚动到指定接口
function handleScrollToOp(event: Event) {
  const detail = (event as CustomEvent).detail
  if (!detail) return

  // 检查是否属于当前 tag
  const found = props.group.operations.find((op) =>
    op.method === detail.method && op.path === detail.path
  )

  if (found) {
    // 等待 DOM 更新后滚动
    setTimeout(() => {
      const el = document.getElementById(`op-${detail.method}-${detail.path}`)
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' })
        // 高亮闪烁效果
        el.classList.add('ring-2', 'ring-blue-400')
        setTimeout(() => el.classList.remove('ring-2', 'ring-blue-400'), 1500)
      }
    }, 100)
  }
}

onMounted(() => {
  window.addEventListener('scroll-to-operation', handleScrollToOp)
})

onUnmounted(() => {
  window.removeEventListener('scroll-to-operation', handleScrollToOp)
})
</script>

<template>
  <div class="mb-4">
    <!-- Tag header -->
    <button
      class="flex w-full cursor-pointer items-center gap-2 rounded-[10px] border-none bg-transparent px-1 py-2.5 text-left transition-colors hover:bg-[var(--c-primary-light)]"
      @click="open = !open"
    >
      <svg
        class="h-4 w-4 flex-shrink-0 text-[var(--c-muted)] transition-transform duration-200"
        :class="open ? 'rotate-0' : '-rotate-90'"
        viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
      >
        <path d="m6 9 6 6 6-6" />
      </svg>
      <span class="flex-1 text-[13.5px] font-semibold text-[var(--c-text)]">{{ group.name }}</span>
      <span class="rounded-full bg-gray-100 px-2 py-px text-[11px] text-[var(--c-muted)]">
        {{ group.operations.length }}
      </span>
    </button>
    <p v-if="group.description" class="mb-2 ml-6 text-[12px] text-[var(--c-muted)]">{{ group.description }}</p>

    <!-- Operations -->
    <div v-if="open" class="mt-1 ml-1" style="animation:slide-down .15s ease">
      <OperationPanel
        v-for="item in group.operations"
        :key="`${item.method}-${item.path}`"
        :item="item"
        :schemas="schemas"
      />
    </div>
  </div>
</template>
