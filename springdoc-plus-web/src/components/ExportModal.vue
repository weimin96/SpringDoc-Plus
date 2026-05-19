<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { OpenApiSpec } from '@/types/openapi'
import PizZip from 'pizzip'
import Docxtemplater from 'docxtemplater'
import { saveAs } from 'file-saver'
import yaml from 'js-yaml'
import { buildDocData, getTagsMeta, groupApisByTag, summarizeSwagger, buildTagGroups } from '@/utils/openapi'
import {
  buildHtml,
  buildInsomniaCollection,
  buildMarkdown,
  buildPostmanCollection,
  type ExportFormat,
} from '@/utils/export'

const props = defineProps<{
  visible: boolean
  spec: OpenApiSpec | null
}>()

const emit = defineEmits<{
  close: []
}>()

// 模板文件
const templateFile = ref<File | null>(null)
const loading = ref(false)
const exportFormat = ref<ExportFormat>('docx')
const includeExamples = ref(true)
const includeSchemas = ref(true)
const maskSensitiveHeaders = ref(true)

// 编辑信息
const editableTitle = ref('')
const editableVersion = ref('')
const editableDescription = ref('')

// swagger 信息
const swaggerInfo = computed(() => props.spec ? summarizeSwagger(props.spec) : null)
const endpointCount = computed(() => swaggerInfo.value?.endpointCount ?? 0)

// 初始化编辑信息
watch(() => props.visible, (v) => {
  if (v && swaggerInfo.value) {
    editableTitle.value = swaggerInfo.value.title
    editableVersion.value = swaggerInfo.value.version
    editableDescription.value = swaggerInfo.value.description
  }
})

// 分组列表
const groupList = computed(() => {
  if (!props.spec) return []
  return buildTagGroups(props.spec)
})
const allApisFlat = computed(() => groupList.value.flatMap((g: any) => g.items))

// 选择状态
const selectedKeys = ref<Set<string>>(new Set())
function apiKey(api: any) {
  return `${api.method} ${api.path}`
}
function isSelected(api: any) {
  return selectedKeys.value.has(apiKey(api))
}
function toggleSelect(api: any) {
  const key = apiKey(api)
  if (selectedKeys.value.has(key)) {
    selectedKeys.value.delete(key)
  } else {
    selectedKeys.value.add(key)
  }
}

const allSelected = computed(() => allApisFlat.value.length > 0 && selectedKeys.value.size === allApisFlat.value.length)
const selectedCount = computed(() => selectedKeys.value.size)

function toggleSelectAll() {
  if (allSelected.value) {
    selectedKeys.value.clear()
  } else {
    const s = new Set<string>()
    allApisFlat.value.forEach((api: any) => s.add(apiKey(api)))
    selectedKeys.value = s
  }
}

function isGroupAllSelected(group: any) {
  return group.items.length > 0 && group.items.every((api: any) => selectedKeys.value.has(apiKey(api)))
}
function groupSelectedCount(group: any) {
  let c = 0
  group.items.forEach((api: any) => {
    if (selectedKeys.value.has(apiKey(api))) c++
  })
  return c
}
function toggleSelectGroup(group: any) {
  if (isGroupAllSelected(group)) {
    group.items.forEach((api: any) => selectedKeys.value.delete(apiKey(api)))
  } else {
    group.items.forEach((api: any) => selectedKeys.value.add(apiKey(api)))
  }
}

// 展开状态
const expandedKeys = ref<Set<string>>(new Set())
function isExpanded(api: any) {
  return expandedKeys.value.has(apiKey(api))
}
function toggleExpand(api: any) {
  const key = apiKey(api)
  if (expandedKeys.value.has(key)) {
    expandedKeys.value.delete(key)
  } else {
    expandedKeys.value.add(key)
  }
}

// 模板相关
const DEFAULT_TEMPLATE_PATH = '/docs/模板.docx'

function getTemplateUrl(): string {
  const base = import.meta.env.BASE_URL.replace(/\/$/, '')
  return `${base}/docs/模板.docx`
}

function onTemplateFileChange(e: Event) {
  templateFile.value = (e.target as HTMLInputElement).files?.[0] || null
}

async function loadDefaultTemplate() {
  try {
    const res = await fetch(getTemplateUrl())
    if (!res.ok) throw new Error('默认模板加载失败')
    const blob = await res.blob()
    templateFile.value = new File([blob], '模板.docx', {
      type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    })
  } catch (e) {
    console.warn('加载默认模板失败:', e)
  }
}

async function downloadTemplate() {
  try {
    const res = await fetch(getTemplateUrl())
    if (!res.ok) throw new Error('模板下载失败')
    const blob = await res.blob()
    saveAs(blob, '模板.docx')
  } catch (e) {
    alert('下载失败: ' + (e instanceof Error ? e.message : String(e)))
  }
}

// 导出
const canExport = computed(() => !!props.spec && (exportFormat.value !== 'docx' || !!templateFile.value))

function downloadTextFile(content: string, fileName: string, type: string) {
  saveAs(new Blob([content], { type }), fileName)
  emit('close')
}

function getSelectedApis(data: any) {
  if (selectedKeys.value.size === 0) return Array.isArray(data.apis) ? data.apis : []
  return (Array.isArray(data.apis) ? data.apis : []).filter((api: any) =>
    selectedKeys.value.has(`${api.method} ${api.path}`),
  )
}

async function exportDoc() {
  if (!canExport.value || !props.spec) return
  loading.value = true
  try {
    const data = buildDocData(props.spec)
    data.title = editableTitle.value || data.title
    data.version = editableVersion.value || data.version
    data.description = editableDescription.value || data.description
    data.apis = getSelectedApis(data)
    const tagsMeta = getTagsMeta(props.spec)
    data.groups = groupApisByTag(Array.isArray(data.apis) ? data.apis : [], tagsMeta)
    const exportOptions = {
      includeExamples: includeExamples.value,
      includeSchemas: includeSchemas.value,
      maskSensitiveHeaders: maskSensitiveHeaders.value,
    }

    const baseName = `${data.title || 'api'}_${data.version || ''}`
    if (exportFormat.value === 'markdown') {
      downloadTextFile(buildMarkdown(data, exportOptions), `${baseName}.md`, 'text/markdown;charset=utf-8')
      return
    }
    if (exportFormat.value === 'html') {
      downloadTextFile(buildHtml(data, exportOptions), `${baseName}.html`, 'text/html;charset=utf-8')
      return
    }
    if (exportFormat.value === 'json') {
      downloadTextFile(JSON.stringify(props.spec, null, 2), `${baseName}.json`, 'application/json;charset=utf-8')
      return
    }
    if (exportFormat.value === 'yaml') {
      downloadTextFile(yaml.dump(props.spec), `${baseName}.yaml`, 'application/yaml;charset=utf-8')
      return
    }
    if (exportFormat.value === 'postman') {
      downloadTextFile(JSON.stringify(buildPostmanCollection(data, exportOptions), null, 2), `${baseName}.postman_collection.json`, 'application/json;charset=utf-8')
      return
    }
    if (exportFormat.value === 'insomnia') {
      downloadTextFile(JSON.stringify(buildInsomniaCollection(data, exportOptions), null, 2), `${baseName}.insomnia.json`, 'application/json;charset=utf-8')
      return
    }

    const arrayBuffer = await templateFile.value!.arrayBuffer()
    const zip = new PizZip(arrayBuffer)
    const doc = new Docxtemplater(zip, {
      paragraphLoop: true,
      linebreaks: true,
    })

    doc.setData(data)
    doc.render()
    const out = doc.getZip().generate({ type: 'blob' })
    saveAs(out, `${baseName}.docx`)
    emit('close')
  } catch (err: any) {
    console.error('导出失败:', err)
    const msg = err?.properties?.errors?.[0]?.properties?.explanation || (err instanceof Error ? err.message : String(err))
    alert('导出失败: ' + msg)
  } finally {
    loading.value = false
  }
}

// 弹窗打开时加载默认模板
watch(() => props.visible, (v) => {
  if (v) {
    templateFile.value = null
    selectedKeys.value.clear()
    expandedKeys.value.clear()
    loadDefaultTemplate()
  }
})
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
        class="max-h-[90vh] w-[min(900px,100%)] overflow-hidden rounded-2xl bg-white"
        style="box-shadow:var(--shadow-modal)"
        role="dialog"
        aria-modal="true"
      >
        <!-- Header -->
        <div class="flex items-center justify-between border-b border-[var(--c-border)] bg-gray-50 px-4 py-3.5">
          <div class="flex items-center gap-2 text-[13px] font-semibold text-[var(--c-text)]">
            <svg class="h-[15px] w-[15px] text-[var(--c-primary)]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
            </svg>
            导出 API 文档
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
        <div class="flex h-[calc(90vh-120px)] overflow-hidden">
          <!-- Left Panel -->
          <div class="w-[320px] flex-shrink-0 overflow-y-auto border-r border-[var(--c-border)] p-4">
            <div class="mb-4 rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] p-3">
              <h3 class="mb-2 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]">使用说明</h3>
              <div class="space-y-2 text-[12px] leading-5 text-[var(--c-muted)]">
                <p>可编辑字段：标题、版本、描述。</p>
                <p>模板用法：下载默认模板后按需调整样式或占位内容，再上传你自己的 `.docx` 模板导出。</p>
                <p>右侧勾选哪些接口，最终 DOCX 就导出哪些接口；不勾选时默认导出全部。</p>
              </div>
            </div>

            <!-- 基本信息 -->
            <div v-if="swaggerInfo" class="mb-4 rounded-lg border border-[var(--c-border)] p-3">
              <h3 class="mb-3 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]">基本信息</h3>
              <div class="space-y-2">
                <div>
                  <label class="mb-1 block text-[12px] text-[var(--c-text)]">标题</label>
                  <input
                    v-model="editableTitle"
                    class="w-full rounded border border-[var(--c-border)] px-2 py-1.5 text-[12px] outline-none focus:border-[var(--c-primary)]"
                  />
                </div>
                <div>
                  <label class="mb-1 block text-[12px] text-[var(--c-text)]">版本</label>
                  <input
                    v-model="editableVersion"
                    class="w-full rounded border border-[var(--c-border)] px-2 py-1.5 text-[12px] outline-none focus:border-[var(--c-primary)]"
                  />
                </div>
                <div>
                  <label class="mb-1 block text-[12px] text-[var(--c-text)]">描述</label>
                  <textarea
                    v-model="editableDescription"
                    rows="2"
                    class="w-full rounded border border-[var(--c-border)] px-2 py-1.5 text-[12px] outline-none focus:border-[var(--c-primary)]"
                  />
                </div>
                <div class="text-[11px] text-[var(--c-muted)]">
                  接口数：{{ endpointCount }}
                </div>
              </div>
            </div>

            <div class="mb-4 rounded-lg border border-[var(--c-border)] p-3">
              <h3 class="mb-3 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]">导出格式</h3>
              <select
                v-model="exportFormat"
                class="w-full rounded border border-[var(--c-border)] px-2 py-1.5 text-[12px] outline-none focus:border-[var(--c-primary)]"
              >
                <option value="docx">DOCX 文档</option>
                <option value="markdown">Markdown 文档</option>
                <option value="html">HTML 文档</option>
                <option value="json">OpenAPI JSON 原始文件</option>
                <option value="yaml">OpenAPI YAML 原始文件</option>
                <option value="postman">Postman Collection</option>
                <option value="insomnia">Insomnia Collection</option>
              </select>
            </div>

            <div class="mb-4 rounded-lg border border-[var(--c-border)] p-3">
              <h3 class="mb-3 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]">导出选项</h3>
              <div class="space-y-2 text-[12px] text-[var(--c-text)]">
                <label class="flex cursor-pointer items-center gap-2">
                  <input v-model="includeExamples" type="checkbox" class="accent-[var(--c-primary)]" />
                  包含响应示例
                </label>
                <label class="flex cursor-pointer items-center gap-2">
                  <input v-model="includeSchemas" type="checkbox" class="accent-[var(--c-primary)]" />
                  包含 Schema 详情
                </label>
                <label class="flex cursor-pointer items-center gap-2">
                  <input v-model="maskSensitiveHeaders" type="checkbox" class="accent-[var(--c-primary)]" />
                  脱敏鉴权 Header
                </label>
              </div>
            </div>

            <!-- 模板选择 -->
            <div v-if="exportFormat === 'docx'" class="mb-4 rounded-lg border border-[var(--c-border)] p-3">
              <h3 class="mb-3 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]">DOCX 模板</h3>
              <p class="mb-2 text-[12px] leading-5 text-[var(--c-muted)]">
                不上传时会自动尝试加载默认模板。上传自定义模板后，可以复用你自己的封面、页眉、页脚和章节样式。
              </p>
              <input
                type="file"
                accept=".docx"
                class="mb-2 w-full text-[12px]"
                @change="onTemplateFileChange"
              />
              <button
                class="w-full rounded border border-[var(--c-border)] bg-gray-50 px-3 py-1.5 text-[12px] text-[var(--c-text)] hover:bg-gray-100"
                @click="downloadTemplate"
              >
                下载默认模板
              </button>
            </div>

            <!-- 导出按钮 -->
            <button
              :disabled="!canExport || loading"
              class="w-full rounded-lg bg-[var(--c-primary)] py-2 text-[13px] font-medium text-white hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
              @click="exportDoc"
            >
              {{ loading ? '导出中...' : '导出文件' }}
            </button>
          </div>

          <!-- Right Panel - 预览 -->
          <div class="flex-1 overflow-y-auto p-4">
            <div v-if="swaggerInfo">
              <div class="mb-3 flex items-center justify-between">
                <div class="text-[11px] text-[var(--c-muted)]">已选 {{ selectedCount }} 项</div>
                <label class="flex cursor-pointer items-center gap-1.5 text-[11px] text-[var(--c-text)]">
                  <input type="checkbox" :checked="allSelected" @change="toggleSelectAll" class="accent-[var(--c-primary)]" />
                  全选
                </label>
              </div>

              <div class="rounded-lg border border-[var(--c-border)]">
                <div
                  v-for="group in groupList"
                  :key="group.tag"
                  class="border-b border-[var(--c-border)] last:border-0"
                >
                  <!-- Group Header -->
                  <div class="flex items-center justify-between bg-gray-50 px-3 py-2">
                    <div>
                      <div class="text-[12px] font-medium text-[var(--c-text)]">{{ group.tag }}</div>
                      <div class="text-[10px] text-[var(--c-muted)]">{{ group.description }}</div>
                    </div>
                    <div class="flex items-center gap-2">
                      <label class="flex cursor-pointer items-center gap-1 text-[10px] text-[var(--c-muted)]">
                        <input
                          type="checkbox"
                          :checked="isGroupAllSelected(group)"
                          @change="toggleSelectGroup(group)"
                          class="accent-[var(--c-primary)]"
                        />
                        本组全选
                      </label>
                      <span class="text-[10px] text-[var(--c-muted)]">
                        {{ groupSelectedCount(group) }} / {{ group.items.length }}
                      </span>
                    </div>
                  </div>

                  <!-- API Items -->
                  <div
                    v-for="api in group.items"
                    :key="apiKey(api)"
                    class="border-t border-[var(--c-border)] px-3 py-2"
                  >
                    <div class="flex items-start gap-2">
                      <input
                        type="checkbox"
                        :checked="isSelected(api)"
                        @change="toggleSelect(api)"
                        class="mt-1 accent-[var(--c-primary)]"
                      />
                      <div class="flex-1">
                        <div class="flex items-center gap-2">
                          <span class="rounded bg-gray-100 px-1.5 py-0.5 text-[10px] font-medium text-[var(--c-muted)]">
                            {{ api.method }}
                          </span>
                          <span class="font-mono text-[11px] text-[var(--c-text)]">{{ api.path }}</span>
                        </div>
                        <div class="mt-1 flex items-start justify-between">
                          <span class="text-[11px] text-[var(--c-muted)]">{{ api.summary }}</span>
                          <button
                            class="text-[10px] text-[var(--c-primary)] hover:underline"
                            @click="toggleExpand(api)"
                          >
                            {{ isExpanded(api) ? '收起' : '展开' }}
                          </button>
                        </div>

                        <!-- Expanded Details -->
                        <div v-if="isExpanded(api)" class="mt-2 rounded bg-gray-50 p-2">
                          <div v-if="api.description" class="mb-2 text-[10px] text-[var(--c-muted)]">
                            {{ api.description }}
                          </div>

                          <!-- Parameters -->
                          <div v-if="api.parameters?.length" class="mb-2">
                            <div class="mb-1 text-[10px] font-medium text-[var(--c-muted)]">参数</div>
                            <table class="w-full text-[10px]">
                              <thead>
                                <tr class="text-left text-[var(--c-muted)]">
                                  <th class="py-1">名称</th>
                                  <th class="py-1">类型</th>
                                  <th class="py-1">必填</th>
                                  <th class="py-1">描述</th>
                                </tr>
                              </thead>
                              <tbody>
                                <tr v-for="p in api.parameters" :key="p.name" class="border-t border-[var(--c-border)]">
                                  <td class="py-1 font-mono">{{ p.name }}</td>
                                  <td class="py-1">{{ p.type }}</td>
                                  <td class="py-1">{{ p.required ? '是' : '否' }}</td>
                                  <td class="py-1 text-[var(--c-muted)]">{{ p.desc }}</td>
                                </tr>
                              </tbody>
                            </table>
                          </div>

                          <!-- Responses -->
                          <div v-if="api.responseContents?.length">
                            <div class="mb-1 text-[10px] font-medium text-[var(--c-muted)]">响应</div>
                            <div class="space-y-1">
                              <div v-for="r in api.responseContents" :key="r.code" class="text-[10px]">
                                <span class="font-mono">{{ r.code }}</span>
                                <span class="text-[var(--c-muted)]">{{ r.contentType ? ` (${r.contentType})` : '' }}</span>
                                <span class="text-[var(--c-muted)]"> - {{ r.schema || r.description }}</span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="text-center text-[12px] text-[var(--c-muted)]">
              加载接口数据后显示预览
            </div>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>
