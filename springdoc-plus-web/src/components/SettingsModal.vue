<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { LocalUiConfig, MergedConfig, ServerUiConfig } from '@/types'

const props = defineProps<{
  visible: boolean
  config: MergedConfig
  serverConfig: ServerUiConfig
}>()

const emit = defineEmits<{
  close: []
  apply: [cfg: LocalUiConfig]
  clear: []
}>()

const form = reactive<LocalUiConfig & { authEnabled: boolean }>({
  tagsSorter: 'alpha',
  operationsSorter: 'alpha',
  authEnabled: false,
  authHeaderName: 'Authorization',
  authDefaultPrefix: '',
  authValue: '',
  authPersist: false,
})

watch(
  () => props.visible,
  (v) => {
    if (!v) return
    const c = props.config
    form.tagsSorter = c.tagsSorter ?? 'alpha'
    form.operationsSorter = c.operationsSorter ?? 'alpha'
    form.authEnabled = c.authEnabled ?? false
    form.authHeaderName = c.authHeaderName ?? 'Authorization'
    form.authDefaultPrefix = c.authDefaultPrefix ?? ''
    form.authValue = c.authValue ?? ''
    form.authPersist = c.authPersist ?? false
  },
)
</script>

<template>
  <Transition
    enter-active-class="transition-[opacity,transform] duration-200"
    enter-from-class="opacity-0 scale-[0.96] translate-y-2"
    leave-active-class="transition-[opacity,transform] duration-150"
    leave-to-class="opacity-0 scale-[0.96] translate-y-2"
  >
    <div
      v-if="visible"
      class="fixed inset-0 z-[200] flex items-center justify-center bg-black/35 p-4 backdrop-blur-sm"
      @click.self="emit('close')"
    >
      <div
        class="w-[min(560px,100%)] overflow-hidden rounded-2xl bg-white"
        style="box-shadow:var(--shadow-modal)"
        role="dialog"
        aria-modal="true"
      >
        <!-- Header -->
        <div class="flex items-center justify-between border-b border-[var(--c-border)] bg-gray-50 px-4 py-3.5">
          <div class="flex items-center gap-2 text-[13px] font-semibold text-[var(--c-text)]">
            <svg class="h-[15px] w-[15px] text-[var(--c-primary)]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="3" />
              <path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14" />
            </svg>
            UI 配置
          </div>
          <button
            class="flex h-7 w-7 cursor-pointer items-center justify-center rounded-[7px] border-none bg-transparent text-[var(--c-muted)] transition-colors hover:bg-[var(--c-border)] hover:text-[var(--c-text)]"
            @click="emit('close')"
          >
            <svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <path d="M18 6 6 18M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- Body -->
        <div class="max-h-[calc(100vh-200px)] overflow-y-auto p-5">

          <!-- 排序规则 -->
          <section class="mb-4">
            <p class="mb-3 text-[10.5px] font-bold uppercase tracking-[.07em] text-[var(--c-muted)]">排序规则</p>
            <div class="grid grid-cols-[110px_1fr] items-center gap-x-3.5 gap-y-2.5">
              <label class="text-[13px] text-[var(--c-text)]">Tags 排序</label>
              <div class="flex gap-4">
                <label class="flex cursor-pointer items-center gap-1.5 text-[13px]">
                  <input v-model="form.tagsSorter" type="radio" value="alpha" class="h-3.5 w-3.5 accent-[var(--c-primary)]" />
                  <span>字母序</span>
                </label>
                <label class="flex cursor-pointer items-center gap-1.5 text-[13px]">
                  <input v-model="form.tagsSorter" type="radio" value="order" class="h-3.5 w-3.5 accent-[var(--c-primary)]" />
                  <span>x-order</span>
                </label>
              </div>

              <label class="text-[13px] text-[var(--c-text)]">Operations 排序</label>
              <div class="flex gap-4">
                <label class="flex cursor-pointer items-center gap-1.5 text-[13px]">
                  <input v-model="form.operationsSorter" type="radio" value="alpha" class="h-3.5 w-3.5 accent-[var(--c-primary)]" />
                  <span>字母序</span>
                </label>
                <label class="flex cursor-pointer items-center gap-1.5 text-[13px]">
                  <input v-model="form.operationsSorter" type="radio" value="order" class="h-3.5 w-3.5 accent-[var(--c-primary)]" />
                  <span>x-order</span>
                </label>
              </div>
            </div>
          </section>

          <div class="my-4 h-px bg-[var(--c-border)]" />

          <!-- 请求鉴权 -->
          <section>
            <div class="mb-3 flex items-center justify-between">
              <p class="text-[10.5px] font-bold uppercase tracking-[.07em] text-[var(--c-muted)]">请求鉴权</p>
              <!-- Toggle -->
              <label class="relative inline-flex cursor-pointer items-center">
                <input v-model="form.authEnabled" type="checkbox" class="sr-only" />
                <span
                  class="relative block h-5 w-9 rounded-full transition-colors duration-200"
                  :class="form.authEnabled ? 'bg-[var(--c-primary)]' : 'bg-gray-300'"
                >
                  <span
                    class="absolute top-0.5 h-4 w-4 rounded-full bg-white shadow transition-[left] duration-200"
                    :class="form.authEnabled ? 'left-[18px]' : 'left-0.5'"
                  />
                </span>
              </label>
            </div>

            <div v-if="form.authEnabled" class="grid grid-cols-[110px_1fr] items-center gap-x-3.5 gap-y-2.5" style="animation:fade-in .15s ease">
              <label class="text-[13px]">Header 名称</label>
              <input v-model="form.authHeaderName" class="w-full rounded-lg border border-[var(--c-border)] bg-white px-2.5 py-[7px] text-[13px] outline-none transition-[border-color,box-shadow] focus:border-[var(--c-primary)] focus:shadow-[0_0_0_3px_rgb(37_99_235_/_0.12)]" placeholder="Authorization" />

              <label class="text-[13px]">默认前缀</label>
              <input v-model="form.authDefaultPrefix" class="w-full rounded-lg border border-[var(--c-border)] bg-white px-2.5 py-[7px] text-[13px] outline-none transition-[border-color,box-shadow] focus:border-[var(--c-primary)] focus:shadow-[0_0_0_3px_rgb(37_99_235_/_0.12)]" placeholder="Bearer / Basic / (可空)" />

              <label class="text-[13px]">Token 值</label>
              <input v-model="form.authValue" type="password" class="w-full rounded-lg border border-[var(--c-border)] bg-white px-2.5 py-[7px] text-[13px] outline-none transition-[border-color,box-shadow] focus:border-[var(--c-primary)] focus:shadow-[0_0_0_3px_rgb(37_99_235_/_0.12)]" placeholder="eyJhbGci…" autocomplete="off" />

              <label class="text-[13px]">持久化</label>
              <label class="flex cursor-pointer items-center gap-1.5">
                <input v-model="form.authPersist" type="checkbox" class="h-3.5 w-3.5 accent-[var(--c-primary)]" />
                <span class="text-xs text-[var(--c-muted)]">保存到 localStorage（刷新后保留）</span>
              </label>
            </div>

            <div v-if="serverConfig.gatewayBasicEnabled" class="mt-2.5 flex items-start gap-2 rounded-lg border border-[#fde68a] bg-[#fef3c7] p-2.5 text-xs text-[#92400e]">
              <svg class="mt-px h-3.5 w-3.5 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <circle cx="12" cy="12" r="10" /><path d="M12 8v4M12 16h.01" />
              </svg>
              网关 Basic 认证已开启，访问 UI 可能需要先完成浏览器认证。
            </div>
          </section>
        </div>

        <!-- Footer -->
        <div class="flex items-center gap-2 border-t border-[var(--c-border)] bg-gray-50 px-4 py-3">
          <button
            class="cursor-pointer rounded-lg border border-[#fecaca] bg-transparent px-3.5 py-[7px] text-[13px] font-medium text-[var(--c-danger)] transition-colors hover:bg-[#fef2f2]"
            @click="emit('clear')"
          >
            清空本地配置
          </button>
          <div class="flex-1" />
          <button
            class="cursor-pointer rounded-lg border border-[var(--c-border)] bg-transparent px-3.5 py-[7px] text-[13px] font-medium text-[var(--c-text)] transition-colors hover:bg-[var(--c-bg)]"
            @click="emit('close')"
          >
            取消
          </button>
          <button
            class="cursor-pointer rounded-lg border-none bg-[var(--c-primary)] px-3.5 py-[7px] text-[13px] font-medium text-white transition-colors hover:bg-[var(--c-primary-hover)]"
            @click="emit('apply', { ...form })"
          >
            应用并刷新
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>
