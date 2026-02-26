import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [
    vue(),
    tailwindcss(),
  ],
  base: '/',  // dev 模式用 /，构建时再改回 /springdoc-plus-ui/
  server: {
    port: 3000,
    proxy: {
      // 代理后端 API 接口
      '/springdoc-plus-gateway': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/user-service': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/order-service': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // 代理 swagger-ui-dist webjars 静态资源
      '/webjars': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // 如果有其他 /v3/api-docs 等路径也一并代理
      '/v3': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    rollupOptions: {
      output: {
        manualChunks: {
          'swagger-ui': ['swagger-ui-dist'],
        }
      }
    }
  },
})