/**
 * OpenAPI代码生成配置
 * 根据后端Swagger文档自动生成前端API接口和TypeScript类型定义
 */
export default {
  // 指定请求库的导入路径
  requestLibPath: "import request from '@/request'",
  // 后端OpenAPI文档地址
  schemaPath: 'http://localhost:8123/api/v3/api-docs',
  // 生成代码的输出目录
  serversPath: './src',
}
