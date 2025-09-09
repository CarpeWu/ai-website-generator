<template>
  <div class="app-card" :class="{ 'app-card--featured': featured }">
    <div class="app-preview">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
      <div v-else class="app-placeholder">
        <span>🤖</span>
      </div>
      <div class="app-overlay">
        <a-space>
          <a-button type="primary" @click="handleViewChat">查看对话</a-button>
          <a-button v-if="app.deployKey" type="default" @click="handleViewWork">查看作品</a-button>
        </a-space>
      </div>
    </div>
    <div class="app-info">
      <div class="app-info-left">
        <a-avatar :src="app.user?.userAvatar" :size="40">
          {{ app.user?.userName?.charAt(0) || 'U' }}
        </a-avatar>
      </div>
      <div class="app-info-right">
        <h3 class="app-title">{{ app.appName || '未命名应用' }}</h3>
        <p class="app-author">
          {{ app.user?.userName || (featured ? '官方' : '未知用户') }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  app: API.AppVO
  featured?: boolean
}

interface Emits {
  (e: 'view-chat', appId: string | number | undefined): void
  (e: 'view-work', app: API.AppVO): void
}

const props = withDefaults(defineProps<Props>(), {
  featured: false,
})

const emit = defineEmits<Emits>()

const handleViewChat = () => {
  emit('view-chat', props.app.id)
}

const handleViewWork = () => {
  emit('view-work', props.app)
}
</script>

<style scoped>
.app-card {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #3b82f6;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
}

.app-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(59, 130, 246, 0.02);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.app-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(59, 130, 246, 0.2);
  border-color: #1e40af;
}

.app-card:hover::before {
  opacity: 1;
}

.app-card--featured {
  border: 2px solid #1e40af;
  background: #ffffff;
}

.app-preview {
  height: 220px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.app-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.app-card:hover .app-preview img {
  transform: scale(1.08);
}

.app-placeholder {
  font-size: 56px;
  color: #64748b;
  opacity: 0.6;
}

.app-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(8px);
}

.app-card:hover .app-overlay {
  opacity: 1;
}

.app-overlay .ant-btn {
  border-radius: 8px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: none;
  backdrop-filter: blur(10px);
}

.app-overlay .ant-btn-primary {
  background: rgba(255, 255, 255, 0.95);
  color: #1a1a1a;
}

.app-overlay .ant-btn-default {
  background: rgba(255, 255, 255, 0.8);
  color: #374151;
}

.app-info {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  background: #ffffff;
  position: relative;
}

.app-info-left {
  flex-shrink: 0;
}

.app-info-left .ant-avatar {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 2px solid #ffffff;
}

.app-info-right {
  flex: 1;
  min-width: 0;
}

.app-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 6px;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  line-height: 1.3;
}

.app-author {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
}

/* 精选卡片特殊样式 */
.app-card--featured .app-title {
  color: #475569;
  font-weight: 700;
}

.app-card--featured .app-author {
  color: #64748b;
  font-weight: 600;
}
</style>
