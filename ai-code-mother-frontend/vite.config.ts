import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  // 插件配置
  plugins: [
    vue(), // Vue 3 支持插件
    vueDevTools(), // Vue 开发者工具插件，提供组件调试等功能
  ],
  // 路径解析配置
  resolve: {
    alias: {
      // 设置 '@' 为 src 目录的别名，可以用 @/components 代替 ../src/components
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  // 开发服务器配置
  server: {
    // 代理配置 - 解决开发环境跨域问题
    proxy: {
      // 拦截所有以 /api 开头的请求
      '/api': {
        target: 'http://localhost:8123', // 将请求转发到后端API服务器
        changeOrigin: true, // 修改请求头的Host为目标地址，避免后端Host校验失败
        secure: false, // 如果target是https且证书无效时设为false，跳过SSL验证

        // 工作原理：
        // 浏览器请求: localhost:5173/api/users
        // 实际转发: localhost:8123/api/users
        // 浏览器以为请求的是同源地址，解决跨域问题
      },
    },
  },
})
