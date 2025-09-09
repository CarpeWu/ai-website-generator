/**
 * ESLint配置文件
 * 为Vue 3 + TypeScript项目配置代码检查规则
 */
import { globalIgnores } from 'eslint/config'
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'
import pluginVue from 'eslint-plugin-vue'
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting'

// 如需在.vue文件中支持除ts外的其他语言，可取消以下注释：
// import { configureVueProject } from '@vue/eslint-config-typescript'
// configureVueProject({ scriptLangs: ['ts', 'tsx'] })
// 更多信息：https://github.com/vuejs/eslint-config-typescript/#advanced-setup

export default defineConfigWithVueTs(
  // 指定要检查的文件类型
  {
    name: 'app/files-to-lint',
    files: ['**/*.{ts,mts,tsx,vue}'],
  },

  // 全局忽略的目录
  globalIgnores(['**/dist/**', '**/dist-ssr/**', '**/coverage/**']),

  // Vue基础规则配置
  pluginVue.configs['flat/essential'],
  // TypeScript推荐规则配置
  vueTsConfigs.recommended,
  // 跳过格式化相关规则（交给Prettier处理）
  skipFormatting,
)
