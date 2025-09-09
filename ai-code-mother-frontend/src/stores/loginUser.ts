/**
 * 用户登录状态管理
 * 使用Pinia管理全局登录用户信息
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUser } from '@/api/userController.ts'

/**
 * 登录用户信息状态管理
 */
export const useLoginUserStore = defineStore('loginUser', () => {
  // 登录用户信息，默认为未登录状态
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  /**
   * 从后端获取登录用户信息
   */
  async function fetchLoginUser() {
    const res = await getLoginUser()
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    }
  }

  /**
   * 更新登录用户信息
   * @param newLoginUser 新的用户信息
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, fetchLoginUser, setLoginUser }
})
