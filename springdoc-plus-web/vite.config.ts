import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import { resolve } from 'path'

// 本地调试时，Spring Boot 后端地址
const BACKEND = 'http://localhost:8080'

export default defineConfig({
  plugins: [vue(), tailwindcss()],

  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },

  server: {
    port: 3000,
    proxy: {
      '/springdoc-plus-gateway': { target: BACKEND, changeOrigin: true },
      '/user-service': { target: BACKEND, changeOrigin: true },
      '/v3': { target: BACKEND, changeOrigin: true },
      '/actuator': { target: BACKEND, changeOrigin: true },
    },
  },

  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    rollupOptions: {
      output: {
        manualChunks: {
          'vue-vendor': ['vue'],
        },
      },
    },
  },
})
