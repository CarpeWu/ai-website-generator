<template>
  <div class="user-profile-page">
    <a-card title="个人信息" class="profile-card">
      <a-row :gutter="24">
        <!-- 左侧：头像区域 -->
        <a-col :span="8">
          <div class="avatar-section">
            <a-avatar :size="120" :src="userInfo.userAvatar || defaultAvatar" class="user-avatar" />
            <div class="avatar-actions">
              <a-upload
                :show-upload-list="false"
                :before-upload="beforeUpload"
                :custom-request="handleAvatarUpload"
                accept="image/*"
              >
                <a-button type="primary" :loading="uploading">
                  <UploadOutlined />
                  更换头像
                </a-button>
              </a-upload>
            </div>
          </div>
        </a-col>
        
        <!-- 右侧：用户信息 -->
        <a-col :span="16">
          <a-form
            :model="userInfo"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 18 }"
            class="user-form"
          >
            <a-form-item label="用户ID">
              <a-input :value="userInfo.id" disabled />
            </a-form-item>
            
            <a-form-item label="用户账号">
              <a-input :value="userInfo.userAccount" disabled />
            </a-form-item>
            
            <a-form-item label="用户名" name="userName">
              <a-input
                v-model:value="userInfo.userName"
                placeholder="请输入用户名"
                :disabled="!isEditing"
              />
            </a-form-item>
            
            <a-form-item label="用户角色">
              <a-tag :color="userInfo.userRole === 'admin' ? 'red' : 'blue'">
                {{ userInfo.userRole === 'admin' ? '管理员' : '普通用户' }}
              </a-tag>
            </a-form-item>
            
            <a-form-item label="注册时间">
              <span>{{ formatTime(userInfo.createTime) }}</span>
            </a-form-item>
            
            <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
              <a-space>
                <a-button v-if="!isEditing" type="primary" @click="startEdit">
                  <EditOutlined />
                  编辑信息
                </a-button>
                <template v-else>
                  <a-button type="primary" :loading="saving" @click="saveUserInfo">
                    <SaveOutlined />
                    保存
                  </a-button>
                  <a-button @click="cancelEdit">
                    取消
                  </a-button>
                </template>
              </a-space>
            </a-form-item>
          </a-form>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { UploadOutlined, EditOutlined, SaveOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { updateUserProfile } from '@/api/userController'
import { formatTime } from '@/utils/time'
import defaultAvatar from '@/assets/images/default-avatar.jpg'
import axios from 'axios'

interface UploadRequestOption {
  file: File
  onSuccess?: (response: { data: string }) => void
  onError?: (error: Error) => void
}

const loginUserStore = useLoginUserStore()

// 用户信息
const userInfo = reactive({
  id: '',
  userAccount: '',
  userName: '',
  userAvatar: '',
  userRole: '',
  createTime: ''
})

// 原始用户信息（用于取消编辑时恢复）
const originalUserInfo = reactive({
  userName: '',
  userAvatar: ''
})

// 状态管理
const isEditing = ref(false)
const saving = ref(false)
const uploading = ref(false)

// 初始化用户信息
const initUserInfo = () => {
  const loginUser = loginUserStore.loginUser
  if (loginUser) {
    Object.assign(userInfo, loginUser)
    originalUserInfo.userName = loginUser.userName || ''
    originalUserInfo.userAvatar = loginUser.userAvatar || ''
  }
}

// 开始编辑
const startEdit = () => {
  isEditing.value = true
  // 保存原始信息
  originalUserInfo.userName = userInfo.userName
  originalUserInfo.userAvatar = userInfo.userAvatar
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  // 恢复原始信息
  userInfo.userName = originalUserInfo.userName
  userInfo.userAvatar = originalUserInfo.userAvatar
}

// 保存用户信息
const saveUserInfo = async () => {
  try {
    saving.value = true
    const updateData = {
      userName: userInfo.userName
    }
    
    const res = await updateUserProfile(updateData)
    if (res.data.code === 0) {
      message.success('保存成功')
      isEditing.value = false
      // 更新登录用户信息
      await loginUserStore.fetchLoginUser()
    } else {
      message.error('保存失败：' + res.data.message)
    }
  } catch {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 头像上传前的检查
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }
  
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('图片大小不能超过 5MB！')
    return false
  }
  
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png']
  if (!allowedTypes.includes(file.type)) {
    message.error('只支持 JPG、JPEG、PNG 格式的图片！')
    return false
  }
  
  return true
}

// 处理头像上传
const handleAvatarUpload = async (options: UploadRequestOption) => {
  const { file, onSuccess, onError } = options
  
  try {
    uploading.value = true
    
    const formData = new FormData()
    formData.append('file', file as File)
    
    const response = await axios.post('/api/user/upload/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      withCredentials: true
    })
    
    if (response.data.code === 0) {
      const avatarUrl = response.data.data
      userInfo.userAvatar = avatarUrl
      message.success('头像上传成功')
      
      // 更新登录用户信息
      await loginUserStore.fetchLoginUser()
      
      onSuccess?.(response.data)
    } else {
      message.error('头像上传失败：' + response.data.message)
      onError?.(new Error(response.data.message))
    }
  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } }; message?: string }
    console.error('头像上传失败:', error)
    message.error('头像上传失败：' + (err.response?.data?.message || err.message || '未知错误'))
    onError?.(error instanceof Error ? error : new Error('上传失败'))
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  initUserInfo()
})
</script>

<style scoped>
.user-profile-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.avatar-section {
  text-align: center;
  padding: 20px;
}

.user-avatar {
  margin-bottom: 16px;
  border: 3px solid #f0f0f0;
}

.avatar-actions {
  margin-top: 16px;
}

.user-form {
  padding: 20px 0;
}

.user-form .ant-form-item {
  margin-bottom: 24px;
}

.user-form .ant-input[disabled] {
  color: rgba(0, 0, 0, 0.65);
  background-color: #f5f5f5;
}
</style>