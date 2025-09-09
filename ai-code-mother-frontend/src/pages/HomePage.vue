<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SendOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

// 优化提示词功能已移除

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功')
      // 跳转到对话页面，确保ID是字符串类型
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }

  try {
    const res = await listMyAppVoByPage({
      pageNum: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const res = await listGoodAppVoByPage({
      pageNum: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
  }
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

// 格式化时间函数已移除，不再需要显示创建时间

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()

  // 鼠标跟随光效
  const handleMouseMove = (e: MouseEvent) => {
    const { clientX, clientY } = e
    const { innerWidth, innerHeight } = window
    const x = (clientX / innerWidth) * 100
    const y = (clientY / innerHeight) * 100

    document.documentElement.style.setProperty('--mouse-x', `${x}%`)
    document.documentElement.style.setProperty('--mouse-y', `${y}%`)
  }

  document.addEventListener('mousemove', handleMouseMove)

  // 清理事件监听器
  return () => {
    document.removeEventListener('mousemove', handleMouseMove)
  }
})
</script>

<template>
  <div id="homePage">
    <div class="container">
      <!-- 简约英雄区域 -->
      <div class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">言语创造，即刻生成</h1>
          <p class="hero-subtitle">AI 赋能，一句话生成专业网站</p>
        </div>
      </div>

      <!-- 优化后的输入区域 -->
      <div class="input-section featured-input">
        <a-textarea
          v-model:value="userPrompt"
          placeholder="描述你想要的网站，例如：创建一个企业官网，包含产品展示、关于我们和联系方式页面..."
          :rows="5"
          :maxlength="1000"
          class="prompt-input"
        />
        <div class="input-actions">
          <a-button type="primary" size="large" @click="createApp" :loading="creating" class="create-btn">
            <template #icon>
              <SendOutlined />
            </template>
            立即生成
          </a-button>
        </div>
      </div>

      <!-- 快捷按钮 -->
      <div class="quick-actions">
        <a-button
          type="default"
          @click="
            setPrompt(
              '创建一个现代化的个人博客网站，包含文章列表、详情页、分类标签、搜索功能、评论系统和个人简介页面。采用简洁的设计风格，支持响应式布局，文章支持Markdown格式，首页展示最新文章和热门推荐。',
            )
          "
          >个人博客网站</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '设计一个专业的企业官网，包含公司介绍、产品服务展示、新闻资讯、联系我们等页面。采用商务风格的设计，包含轮播图、产品展示卡片、团队介绍、客户案例展示，支持多语言切换和在线客服功能。',
            )
          "
        >企业官网</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '构建一个功能完整的在线商城，包含商品展示、购物车、用户注册登录、订单管理、支付结算等功能。设计现代化的商品卡片布局，支持商品搜索筛选、用户评价、优惠券系统和会员积分功能。',
            )
          "
        >在线商城</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '制作一个精美的作品展示网站，适合设计师、摄影师、艺术家等创作者。包含作品画廊、项目详情页、个人简历、联系方式等模块。采用瀑布流或网格布局展示作品，支持图片放大预览和作品分类筛选。',
            )
          "
        >作品展示网站</a-button
        >
      </div>

      <!-- 我的作品 - 精致容器设计 -->
      <div class="works-container">
        <div class="container-inner">
          <div class="section-header">
            <h2 class="section-title">我的作品</h2>
            <p class="section-description">探索你创建的精彩应用，每一个都是创意的结晶</p>
          </div>
          <div class="works-grid">
            <AppCard
              v-for="app in myApps"
              :key="app.id"
              :app="app"
              @view-chat="viewChat"
              @view-work="viewWork"
            />
          </div>
          <div class="pagination-wrapper">
            <a-pagination
              v-model:current="myAppsPage.current"
              v-model:page-size="myAppsPage.pageSize"
              :total="myAppsPage.total"
              :show-size-changer="false"
              :show-total="(total: number) => `共 ${total} 个作品`"
              @change="loadMyApps"
            />
          </div>
        </div>
      </div>

      <!-- 精选推荐 - 精致容器设计 -->
      <div class="featured-container">
        <div class="container-inner">
          <div class="section-header">
            <h2 class="section-title">精选推荐</h2>
            <p class="section-description">发现社区中的优秀作品，获取创作灵感</p>
          </div>
          <div class="featured-grid">
            <AppCard
              v-for="app in featuredApps"
              :key="app.id"
              :app="app"
              :featured="true"
              @view-chat="viewChat"
              @view-work="viewWork"
            />
          </div>
          <div class="pagination-wrapper">
            <a-pagination
              v-model:current="featuredAppsPage.current"
              v-model:page-size="featuredAppsPage.pageSize"
              :total="featuredAppsPage.total"
              :show-size-changer="false"
              :show-total="(total: number) => `共 ${total} 个案例`"
              @change="loadFeaturedApps"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#homePage {
  width: 100%;
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background: #fafbfc;
  position: relative;
  overflow: hidden;
}

/* 精致的背景纹理 */
#homePage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 2px 2px, rgba(102, 126, 234, 0.04) 1px, transparent 0),
    radial-gradient(circle at 50px 50px, rgba(118, 75, 162, 0.02) 1px, transparent 0);
  background-size: 40px 40px, 80px 80px;
  pointer-events: none;
  animation: backgroundFloat 20s ease-in-out infinite;
}

@keyframes backgroundFloat {
  0%, 100% { 
    transform: translate(0, 0) rotate(0deg);
    opacity: 1;
  }
  50% { 
    transform: translate(10px, -10px) rotate(1deg);
    opacity: 0.8;
  }
}

/* 优雅的微光效果 */
#homePage::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(
      600px circle at var(--mouse-x, 50%) var(--mouse-y, 50%),
      rgba(102, 126, 234, 0.03) 0%,
      rgba(118, 75, 162, 0.02) 40%,
      transparent 70%
    );
  pointer-events: none;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0;
}

#homePage:hover::after {
  opacity: 1;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
  z-index: 2;
  width: 100%;
  box-sizing: border-box;
}

/* 移除居中光束效果 */

/* 重新设计的英雄区域 */
.hero-section {
  text-align: center;
  padding: 60px 0 40px;
  margin-bottom: 40px;
  color: #1e293b;
  position: relative;
  margin: 0 auto;
  max-width: 900px;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  padding: 0 20px;
}

.hero-title {
  font-size: 36px;
  font-weight: 600;
  margin: 0 0 20px;
  line-height: 1.1;
  color: #0f172a;
  letter-spacing: -0.03em;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif;
}



.hero-subtitle {
  font-size: 18px;
  margin: 0 0 40px;
  color: #475569;
  font-weight: 400;
  line-height: 1.5;
  opacity: 0.9;
}



/* 优化后的输入区域 */
.featured-input {
  margin: 0 auto 60px;
  max-width: 800px;
  position: relative;
  z-index: 10;
}

.featured-input .prompt-input {
  border-radius: 16px;
  border: 2px solid #e2e8f0;
  font-size: 18px;
  padding: 24px;
  background: #ffffff;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  min-height: 140px;
}

.featured-input .prompt-input:focus {
  border-color: #64748b;
  box-shadow: 0 12px 40px rgba(100, 116, 139, 0.15);
  transform: translateY(-2px);
}

.featured-input .prompt-input:hover {
  border-color: #d1d5db;
}

/* 优化按钮样式 */
.create-btn {
  border-radius: 10px;
  padding: 0 24px;
  height: 42px;
  font-weight: 600;
  font-size: 14px;
  background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(30, 64, 175, 0.3);
  transition: all 0.3s ease;
  color: white;
}

.create-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(30, 64, 175, 0.4);
  background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 100%);
}

.create-btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 4px rgba(30, 64, 175, 0.3);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .hero-section {
    padding: 40px 0 30px;
    margin-bottom: 30px;
  }
  
  .hero-title {
    font-size: 28px;
    margin-bottom: 16px;
  }
  
  .hero-subtitle {
    font-size: 16px;
    margin-bottom: 32px;
  }
  
  .hero-section::before {
    width: 180px;
    height: 180px;
    opacity: 0.1;
  }
  
  .create-btn {
    height: 38px;
    padding: 0 20px;
    font-size: 13px;
  }
}

/* 输入区域 */
.input-section {
  position: relative;
  margin: 0 auto 40px;
  max-width: 800px;
}

.prompt-input {
  border-radius: 12px;
  border: 2px solid #f0f0f0;
  font-size: 16px;
  padding: 20px 24px;
  background: #ffffff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.prompt-input:focus {
  border-color: #64748b;
  box-shadow: 0 4px 25px rgba(100, 116, 139, 0.15);
  transform: translateY(-2px);
}

.prompt-input:hover {
  border-color: #e0e0e0;
}

.input-actions {
  position: absolute;
  bottom: 16px;
  right: 16px;
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 快捷按钮 */
.quick-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 60px;
  flex-wrap: wrap;
}

.quick-actions .ant-btn {
  border-radius: 8px;
  padding: 12px 24px;
  height: auto;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  color: #374151;
  font-weight: 500;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.quick-actions .ant-btn:hover {
  background: #f9fafb;
  border-color: #d1d5db;
  color: #111827;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 精致容器设计 */
.works-container,
.featured-container {
  background: #ffffff;
  border-radius: 24px;
  margin: 60px 0;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  border: 1px solid #3b82f6;
  overflow: hidden;
  position: relative;
}

.works-container::before,
.featured-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #1e40af 0%, #3b82f6 50%, #1e40af 100%);
}

.container-inner {
  padding: 48px 40px;
}

.section-header {
  text-align: center;
  margin-bottom: 48px;
}

.section-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 12px;
  color: #1e293b;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.section-description {
  font-size: 18px;
  color: #6b7280;
  font-weight: 400;
  line-height: 1.5;
  margin: 0;
}

/* 我的作品网格 */
.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
  gap: 32px;
  margin-bottom: 40px;
}

/* 精选案例网格 */
.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
  gap: 32px;
  margin-bottom: 40px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .container {
    padding: 16px;
  }

  .hero-section {
    border-radius: 24px;
    margin: 0 16px 20px;
  }

  .hero-content {
    padding: 0 24px;
  }

  .works-grid,
  .featured-grid {
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    gap: 24px;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 60px 0 30px;
    border-radius: 20px;
    margin: 0 8px 20px;
  }

  .hero-content {
    padding: 0 20px;
  }

  .hero-title {
    font-size: 28px;
    margin: 0 0 12px;
  }

  .hero-subtitle {
    font-size: 15px;
    margin: 0 0 32px;
  }

  .works-grid,
  .featured-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .quick-actions {
    justify-content: center;
    gap: 12px;
  }

  .quick-actions .ant-btn {
    padding: 10px 16px;
    font-size: 13px;
  }

  .container-inner {
    padding: 32px 20px;
  }

  .works-container,
  .featured-container {
    margin: 32px 0;
    border-radius: 16px;
  }

  .section-title {
    font-size: 28px;
  }

  .section-description {
    font-size: 16px;
  }

  .featured-input {
    margin: 0 auto 40px;
  }

  .featured-input .prompt-input {
    padding: 16px;
    font-size: 15px;
  }

  .create-btn {
    height: 38px;
    padding: 0 20px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 12px;
  }

  .hero-section {
    padding: 40px 0 24px;
    border-radius: 16px;
    margin: 0 4px 16px;
  }

  .hero-content {
    padding: 0 16px;
  }

  .hero-title {
    font-size: 24px;
    line-height: 1.2;
    margin-bottom: 12px;
  }

  .hero-subtitle {
    font-size: 14px;
    margin: 0 0 24px;
  }

  .quick-actions {
    flex-direction: column;
    align-items: center;
    gap: 8px;
  }

  .quick-actions .ant-btn {
    width: 100%;
    max-width: 280px;
  }

  .container-inner {
    padding: 24px 16px;
  }

  .works-container,
  .featured-container {
    margin: 24px 0;
    border-radius: 12px;
  }

  .section-title {
    font-size: 24px;
  }

  .section-description {
    font-size: 15px;
  }

  .featured-input .prompt-input {
    padding: 14px;
    border-radius: 12px;
  }
}
</style>
