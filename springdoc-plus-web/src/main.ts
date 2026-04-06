import { createApp } from 'vue'
import App from './App.vue'
import './styles/index.css'
import { gcStorage } from '@/utils/storage'
import { SIMULATE_REQUEST_STORAGE_PREFIX } from '@/composables/useSimulateRequest'

// 应用启动时执行一次 GC，清理过期或旧版本的 localStorage 缓存
gcStorage([
  SIMULATE_REQUEST_STORAGE_PREFIX,
  'springdoc-plus:simulate:ui:',
])

createApp(App).mount('#app')
