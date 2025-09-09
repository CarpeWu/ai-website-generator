/**
 * HTTP请求配置模块
 * 基于axios封装的HTTP客户端，包含请求/响应拦截器
 */
import axios from 'axios'
import { message } from 'ant-design-vue'
import { API_BASE_URL } from '@/config/env'

/**
 * 创建axios实例
 * 配置基础URL、超时时间和跨域凭证
 */
const myAxios = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
  withCredentials: true,
})

/**
 * 全局请求拦截器
 * 在请求发送前进行统一处理
 */
myAxios.interceptors.request.use(
  function (config) {
    // 请求发送前的处理逻辑
    return config
  },
  function (error) {
    // 请求错误处理
    return Promise.reject(error)
  },
)

/**
 * 全局响应拦截器
 * 统一处理响应数据和错误状态
 */
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    
    // 处理未登录状态（错误码40100）
    if (data.code === 40100) {
      // 不是获取用户信息的请求，并且用户目前不在登录页面，则跳转到登录页面
      if (
        !response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    // 响应错误处理（状态码不在2xx范围内）
    return Promise.reject(error)
  },
)

export default myAxios
