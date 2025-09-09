/**
 * 应用程序入口文件
 * 初始化Vue应用并配置全局插件
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// 引入Ant Design Vue组件库
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

// 引入权限控制
import '@/access'

// 创建Vue应用实例
const app = createApp(App)

// 配置状态管理
app.use(createPinia())
// 配置路由
app.use(router)
// 配置UI组件库
app.use(Antd)

// 挂载应用到DOM
app.mount('#app')
