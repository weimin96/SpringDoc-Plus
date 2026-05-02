<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { AuthHeader, LocalUiConfig, MergedConfig, ServerUiConfig } from '@/types'

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

// 表单数据结构
const form = reactive({
  tagsSorter: 'alpha' as 'alpha' | 'order',
  operationsSorter: 'alpha' as 'alpha' | 'order',
  authEnabled: false,
  // 兼容旧版本单 header 配置（保留但不推荐使用）
  authHeaderName: 'Authorization',
  authDefaultPrefix: '',
  authValue: '',
  // 新版本多 header 配置
  authHeaders: [] as AuthHeader[],
  authPersist: false,
  oauth2Enabled: false,
  oauth2TokenUrl: '',
  oauth2ClientId: '',
  oauth2ClientSecret: '',
  oauth2Scope: '',
  oauth2GrantType: 'client_credentials' as 'client_credentials' | 'password',
  oauth2Username: '',
  oauth2Password: '',
})

const oauth2Loading = ref(false)
const oauth2Error = ref('')

// 添加新 header
function addHeader() {
  form.authHeaders.push({ name: '', defaultPrefix: '', value: '' })
}

// 删除 header
function removeHeader(index: number) {
  form.authHeaders.splice(index, 1)
}

watch(
  () => props.visible,
  (v) => {
    if (!v) return
    const c = props.config
    form.tagsSorter = c.tagsSorter ?? 'alpha'
    form.operationsSorter = c.operationsSorter ?? 'alpha'
    form.authEnabled = c.authEnabled ?? false
    form.authPersist = c.authPersist ?? false
    form.oauth2Enabled = c.oauth2Enabled ?? false
    form.oauth2TokenUrl = c.oauth2TokenUrl ?? ''
    form.oauth2ClientId = c.oauth2ClientId ?? ''
    form.oauth2ClientSecret = c.oauth2ClientSecret ?? ''
    form.oauth2Scope = c.oauth2Scope ?? ''
    form.oauth2GrantType = c.oauth2GrantType ?? 'client_credentials'
    form.oauth2Username = c.oauth2Username ?? ''
    form.oauth2Password = c.oauth2Password ?? ''
    oauth2Error.value = ''

    // 优先使用新版本多 header 配置，回退到旧版本单 header
    if (c.authHeaders && c.authHeaders.length > 0) {
      form.authHeaders = c.authHeaders.map(h => ({ ...h }))
    } else if (c.authHeaderName) {
      // 旧版本配置转换为新格式
      form.authHeaderName = c.authHeaderName ?? 'Authorization'
      form.authDefaultPrefix = c.authDefaultPrefix ?? ''
      form.authValue = c.authValue ?? ''
      form.authHeaders = [{
        name: form.authHeaderName,
        defaultPrefix: form.authDefaultPrefix,
        value: form.authValue,
      }]
    } else {
      form.authHeaders = []
    }
  },
)

function ensureAuthorizationHeader(token: string) {
  const existing = form.authHeaders.find(header => header.name.toLowerCase() === 'authorization')
  if (existing) {
    existing.defaultPrefix = 'Bearer'
    existing.value = token
    return
  }
  form.authHeaders.unshift({ name: 'Authorization', defaultPrefix: 'Bearer', value: token })
}

async function fetchOAuth2Token() {
  oauth2Error.value = ''
  if (!form.oauth2TokenUrl || !form.oauth2ClientId) {
    oauth2Error.value = 'Token 地址和 Client ID 不能为空'
    return
  }
  oauth2Loading.value = true
  try {
    const body = new URLSearchParams()
    body.set('grant_type', form.oauth2GrantType)
    body.set('client_id', form.oauth2ClientId)
    if (form.oauth2ClientSecret) body.set('client_secret', form.oauth2ClientSecret)
    if (form.oauth2Scope) body.set('scope', form.oauth2Scope)
    if (form.oauth2GrantType === 'password') {
      body.set('username', form.oauth2Username)
      body.set('password', form.oauth2Password)
    }

    const response = await fetch(form.oauth2TokenUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body,
    })
    const payload = await response.json().catch(() => ({})) as { access_token?: string; error_description?: string; error?: string }
    if (!response.ok || !payload.access_token) {
      throw new Error(payload.error_description || payload.error || `Token 获取失败，HTTP ${response.status}`)
    }
    form.authEnabled = true
    ensureAuthorizationHeader(payload.access_token)
  } catch (error) {
    oauth2Error.value = error instanceof Error ? error.message : String(error)
  } finally {
    oauth2Loading.value = false
  }
}

// 点击应用时触发
function handleApply() {
  const cfg: LocalUiConfig = {
    tagsSorter: form.tagsSorter,
    operationsSorter: form.operationsSorter,
    authEnabled: form.authEnabled,
    authPersist: form.authPersist,
    authHeaders: form.authHeaders,
    oauth2Enabled: form.oauth2Enabled,
    oauth2TokenUrl: form.oauth2TokenUrl,
    oauth2ClientId: form.oauth2ClientId,
    oauth2ClientSecret: form.oauth2ClientSecret,
    oauth2Scope: form.oauth2Scope,
    oauth2GrantType: form.oauth2GrantType,
    oauth2Username: form.oauth2Username,
    oauth2Password: form.oauth2Password,
  }
  emit('apply', cfg)
}
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
        <form class="max-h-[calc(100vh-200px)] overflow-y-auto p-5" autocomplete="off" data-lpignore="true">

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

            <div v-if="form.authEnabled" class="space-y-3" style="animation:fade-in .15s ease">
              <div class="rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] p-3 text-[12px] leading-5 text-[var(--c-muted)]">
                这里配置的是请求 Header，不是站点登录密码。常见写法是 `Authorization + Bearer + token`，
                也可以填写自定义 Header 名称和值。
              </div>
              <!-- Header 列表 -->
              <div v-for="(header, index) in form.authHeaders" :key="index" class="rounded-lg border border-[var(--c-border)] bg-gray-50 p-3">
                <div class="mb-2 flex items-center justify-between">
                  <span class="text-[11px] font-medium text-[var(--c-muted)]">Header {{ index + 1 }}</span>
                  <button
                    v-if="form.authHeaders.length > 1"
                    type="button"
                    class="flex h-5 w-5 items-center justify-center rounded text-[var(--c-muted)] hover:bg-[var(--c-border)] hover:text-[var(--c-danger)]"
                    title="删除"
                    @click="removeHeader(index)"
                  >
                    <svg class="h-3 w-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                      <path d="M18 6 6 18M6 6l12 12" />
                    </svg>
                  </button>
                </div>
                <div class="grid grid-cols-[80px_1fr] items-center gap-x-3 gap-y-2">
                  <label class="text-[13px] text-[var(--c-text)]">名称</label>
                  <input v-model="header.name" class="w-full rounded-lg border border-[var(--c-border)] bg-white px-2.5 py-[7px] text-[13px] outline-none transition-[border-color,box-shadow] focus:border-[var(--c-primary)] focus:shadow-[0_0_0_3px_rgb(37_99_235_/_0.12)]" placeholder="Authorization" />

                  <label class="text-[13px] text-[var(--c-text)]">前缀</label>
                  <input v-model="header.defaultPrefix" class="w-full rounded-lg border border-[var(--c-border)] bg-white px-2.5 py-[7px] text-[13px] outline-none transition-[border-color,box-shadow] focus:border-[var(--c-primary)] focus:shadow-[0_0_0_3px_rgb(37_99_235_/_0.12)]" placeholder="Bearer / Basic / (可空)" />

                  <label class="text-[13px] text-[var(--c-text)]">值</label>
                  <input
                    v-model="header.value"
                    type="text"
                    class="w-full rounded-lg border border-[var(--c-border)] bg-white px-2.5 py-[7px] text-[13px] outline-none transition-[border-color,box-shadow] focus:border-[var(--c-primary)] focus:shadow-[0_0_0_3px_rgb(37_99_235_/_0.12)]"
                    placeholder="eyJhbGci…"
                    inputmode="text"
                    autocomplete="new-password"
                    autocapitalize="off"
                    autocorrect="off"
                    spellcheck="false"
                    data-lpignore="true"
                    data-1p-ignore="true"
                    data-form-type="other"
                  />
                </div>
              </div>

              <!-- 添加新 Header -->
              <button
                type="button"
                class="flex w-full items-center justify-center gap-1.5 rounded-lg border border-dashed border-[var(--c-border)] py-2 text-[13px] text-[var(--c-muted)] transition-colors hover:border-[var(--c-primary)] hover:text-[var(--c-primary)]"
                @click="addHeader"
              >
                <svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                  <path d="M12 5v14M5 12h14" />
                </svg>
                添加请求头
              </button>

              <!-- 持久化 -->
              <div class="flex items-center gap-2 pt-1">
                <label class="flex cursor-pointer items-center gap-1.5">
                  <input v-model="form.authPersist" type="checkbox" class="h-3.5 w-3.5 accent-[var(--c-primary)]" />
                  <span class="text-xs text-[var(--c-muted)]">保存到 localStorage（刷新后保留）</span>
                </label>
              </div>

              <div class="rounded-lg border border-[var(--c-border)] bg-white p-3">
                <div class="mb-3 flex items-center justify-between">
                  <span class="text-[12px] font-semibold text-[var(--c-text)]">OAuth2 / OpenID Connect</span>
                  <label class="flex cursor-pointer items-center gap-1.5 text-[12px] text-[var(--c-muted)]">
                    <input v-model="form.oauth2Enabled" type="checkbox" class="h-3.5 w-3.5 accent-[var(--c-primary)]" />
                    启用
                  </label>
                </div>
                <div v-if="form.oauth2Enabled" class="grid grid-cols-[92px_1fr] items-center gap-x-3 gap-y-2">
                  <label class="text-[13px] text-[var(--c-text)]">Token 地址</label>
                  <input v-model="form.oauth2TokenUrl" class="w-full rounded-lg border border-[var(--c-border)] bg-white px-2.5 py-[7px] text-[13px] outline-none focus:border-[var(--c-primary)]" placeholder="https://auth.example.com/oauth2/token" />

                  <label class="text-[13px] text-[var(--c-text)]">授权模式</label>
                  <select v-model="form.oauth2GrantType" class="w-full rounded-lg border border-[var(--c-border)] bg-white px-2.5 py-[7px] text-[13px] outline-none focus:border-[var(--c-primary)]">
                    <option value="client_credentials">client_credentials</option>
                    <option value="password">password</option>
                  </select>

                  <label class="text-[13px] text-[var(--c-text)]">Client ID</label>
                  <input v-model="form.oauth2ClientId" class="w-full rounded-lg border border-[var(--c-border)] bg-white px-2.5 py-[7px] text-[13px] outline-none focus:border-[var(--c-primary)]" />

                  <label class="text-[13px] text-[var(--c-text)]">Client Secret</label>
                  <input v-model="form.oauth2ClientSecret" type="password" class="w-full rounded-lg border border-[var(--c-border)] bg-white px-2.5 py-[7px] text-[13px] outline-none focus:border-[var(--c-primary)]" autocomplete="new-password" />

                  <label class="text-[13px] text-[var(--c-text)]">Scope</label>
                  <input v-model="form.oauth2Scope" class="w-full rounded-lg border border-[var(--c-border)] bg-white px-2.5 py-[7px] text-[13px] outline-none focus:border-[var(--c-primary)]" placeholder="read write" />

                  <template v-if="form.oauth2GrantType === 'password'">
                    <label class="text-[13px] text-[var(--c-text)]">用户名</label>
                    <input v-model="form.oauth2Username" class="w-full rounded-lg border border-[var(--c-border)] bg-white px-2.5 py-[7px] text-[13px] outline-none focus:border-[var(--c-primary)]" autocomplete="username" />

                    <label class="text-[13px] text-[var(--c-text)]">密码</label>
                    <input v-model="form.oauth2Password" type="password" class="w-full rounded-lg border border-[var(--c-border)] bg-white px-2.5 py-[7px] text-[13px] outline-none focus:border-[var(--c-primary)]" autocomplete="current-password" />
                  </template>

                  <div class="col-span-2">
                    <button
                      type="button"
                      :disabled="oauth2Loading"
                      class="w-full rounded-lg border border-[var(--c-border)] bg-gray-50 px-3 py-2 text-[13px] text-[var(--c-text)] hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-60"
                      @click="fetchOAuth2Token"
                    >
                      {{ oauth2Loading ? '获取中...' : '获取 Token 并写入 Authorization' }}
                    </button>
                    <p v-if="oauth2Error" class="mt-2 text-[12px] text-[var(--c-danger)]">{{ oauth2Error }}</p>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="serverConfig.gatewayBasicEnabled" class="mt-2.5 flex items-start gap-2 rounded-lg border border-[#fde68a] bg-[#fef3c7] p-2.5 text-xs text-[#92400e]">
              <svg class="mt-px h-3.5 w-3.5 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <circle cx="12" cy="12" r="10" /><path d="M12 8v4M12 16h.01" />
              </svg>
              网关 Basic 认证已开启，访问 UI 可能需要先完成浏览器认证。
            </div>
          </section>
        </form>

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
            @click="handleApply"
          >
            应用并刷新
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>
