<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { LocalUiConfig, MergedConfig, ServerUiConfig } from '../types'

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

const form = reactive<LocalUiConfig>({
  tagsSorter: 'alpha',
  operationsSorter: 'alpha',
  authEnabled: false,
  authHeaderName: 'Authorization',
  authDefaultPrefix: '',
  authValue: '',
  authPersist: false,
})

watch(() => props.visible, (v) => {
  if (v) {
    const c = props.config
    form.tagsSorter = c.tagsSorter ?? 'alpha'
    form.operationsSorter = c.operationsSorter ?? 'alpha'
    form.authEnabled = c.authEnabled ?? false
    form.authHeaderName = c.authHeaderName ?? 'Authorization'
    form.authDefaultPrefix = c.authDefaultPrefix ?? ''
    form.authValue = c.authValue ?? ''
    form.authPersist = c.authPersist ?? false
  }
})

function onApply() {
  emit('apply', { ...form })
}
</script>

<template>
  <Transition name="modal">
    <div
      v-if="visible"
      class="modal-backdrop"
      @click.self="emit('close')"
    >
      <div class="modal-box" role="dialog" aria-modal="true" aria-label="设置">
        <!-- Header -->
        <div class="modal-header">
          <div class="flex items-center gap-2">
            <svg class="w-4 h-4 text-blue-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14M12 2v2M12 20v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M2 12h2M20 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/>
            </svg>
            <span class="font-semibold text-sm">UI 配置</span>
          </div>
          <button class="icon-btn" @click="emit('close')">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6 6 18M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <!-- Body -->
        <div class="modal-body">
          <!-- Sorting -->
          <div class="section-title">排序规则</div>
          <div class="form-grid">
            <label class="form-label">Tags 排序</label>
            <div class="radio-group">
              <label class="radio-label">
                <input v-model="form.tagsSorter" type="radio" value="alpha" />
                <span>字母序</span>
              </label>
              <label class="radio-label">
                <input v-model="form.tagsSorter" type="radio" value="order" />
                <span>x-order</span>
              </label>
            </div>

            <label class="form-label">Operations 排序</label>
            <div class="radio-group">
              <label class="radio-label">
                <input v-model="form.operationsSorter" type="radio" value="alpha" />
                <span>字母序</span>
              </label>
              <label class="radio-label">
                <input v-model="form.operationsSorter" type="radio" value="order" />
                <span>x-order</span>
              </label>
            </div>
          </div>

          <div class="divider" />

          <!-- Auth -->
          <div class="section-title flex items-center justify-between">
            <span>请求鉴权</span>
            <label class="toggle">
              <input v-model="form.authEnabled" type="checkbox" class="sr-only" />
              <span class="toggle-track">
                <span class="toggle-thumb" />
              </span>
            </label>
          </div>

          <div v-if="form.authEnabled" class="form-grid">
            <label class="form-label">Header 名称</label>
            <input v-model="form.authHeaderName" class="form-input" placeholder="Authorization" />

            <label class="form-label">默认前缀</label>
            <input v-model="form.authDefaultPrefix" class="form-input" placeholder="Bearer / Basic / (可空)" />

            <label class="form-label">Token 值</label>
            <input v-model="form.authValue" class="form-input" type="password" placeholder="eyJhbGci..." />

            <label class="form-label">持久化</label>
            <label class="radio-label">
              <input v-model="form.authPersist" type="checkbox" />
              <span class="text-xs text-gray-500">保存到 localStorage</span>
            </label>
          </div>

          <div v-if="serverConfig.gatewayBasicEnabled" class="notice">
            <svg class="w-3.5 h-3.5 flex-shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/>
            </svg>
            网关 Basic 认证已开启，访问 UI 可能需要先完成认证。
          </div>
        </div>

        <!-- Footer -->
        <div class="modal-footer">
          <button class="btn-ghost danger" @click="emit('clear')">清空本地配置</button>
          <div style="flex:1"/>
          <button class="btn-ghost" @click="emit('close')">取消</button>
          <button class="btn-primary" @click="onApply">应用并刷新</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: 16px;
}

.modal-box {
  width: min(580px, 100%);
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.18), 0 0 0 1px rgba(0,0,0,.06);
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--c-border);
  background: #fafafa;
}

.modal-body {
  padding: 20px 20px 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

.modal-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  border-top: 1px solid var(--c-border);
  background: #fafafa;
}

.section-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--c-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  display: flex;
  align-items: center;
  gap: 6px;
}

.form-grid {
  display: grid;
  grid-template-columns: 110px 1fr;
  gap: 8px 12px;
  align-items: center;
}

.form-label {
  font-size: 13px;
  color: var(--c-text);
}

.form-input {
  border: 1px solid var(--c-border);
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 13px;
  outline: none;
  width: 100%;
  transition: border-color .15s, box-shadow .15s;
}

.form-input:focus {
  border-color: var(--c-primary);
  box-shadow: 0 0 0 3px rgba(37,99,235,.12);
}

.radio-group {
  display: flex;
  gap: 16px;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  cursor: pointer;
}

.radio-label input[type=radio],
.radio-label input[type=checkbox] {
  accent-color: var(--c-primary);
  width: 14px;
  height: 14px;
}

.divider {
  height: 1px;
  background: var(--c-border);
  margin: 4px 0;
}

.notice {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 12px;
  color: #92400e;
  background: #fef3c7;
  border: 1px solid #fde68a;
  border-radius: 8px;
  padding: 8px 10px;
}

/* Toggle */
.toggle { display: flex; align-items: center; cursor: pointer; }
.toggle-track {
  width: 36px; height: 20px;
  background: #e5e7eb;
  border-radius: 99px;
  position: relative;
  transition: background .2s;
}
.toggle input:checked ~ .toggle-track { background: var(--c-primary); }
.toggle-thumb {
  position: absolute;
  top: 2px; left: 2px;
  width: 16px; height: 16px;
  background: #fff;
  border-radius: 50%;
  transition: left .2s;
  box-shadow: 0 1px 4px rgba(0,0,0,.2);
}
.toggle input:checked ~ .toggle-track .toggle-thumb { left: 18px; }

/* Buttons */
.icon-btn {
  width: 28px; height: 28px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: var(--c-muted);
  cursor: pointer;
  transition: background .15s;
}
.icon-btn:hover { background: var(--c-border); color: var(--c-text); }

.btn-ghost {
  padding: 6px 14px;
  border-radius: 8px;
  border: 1px solid var(--c-border);
  background: transparent;
  font-size: 13px;
  cursor: pointer;
  transition: background .15s;
  color: var(--c-text);
}
.btn-ghost:hover { background: var(--c-bg); }
.btn-ghost.danger { color: var(--c-danger); border-color: #fecaca; }
.btn-ghost.danger:hover { background: #fef2f2; }

.btn-primary {
  padding: 6px 16px;
  border-radius: 8px;
  border: none;
  background: var(--c-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity .15s;
}
.btn-primary:hover { opacity: 0.9; }

/* Transitions */
.modal-enter-active, .modal-leave-active { transition: opacity .2s, transform .2s; }
.modal-enter-from, .modal-leave-to { opacity: 0; transform: scale(.96); }
</style>
