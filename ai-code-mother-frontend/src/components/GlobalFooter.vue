<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <div class="footer-image">
        <img :src="footerImage" alt="Footer decoration" class="footer-img">
      </div>
      <p class="copyright">
        <a
          href="https://www.codefather.cn"
          target="_blank"
          rel="noopener noreferrer"
          class="author-link"
        >
          编程导航原创项目 by 程序员鱼皮
        </a>
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
import footerImage from '@/assets/footer-image.png'
</script>

<style scoped>
.footer {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  text-align: center;
  padding: 20px;
  margin-top: 40px;
  border-top: 1px solid rgba(102, 126, 234, 0.1);
}

.footer-image {
  margin-bottom: 15px;
}

.footer-img {
  width: 120px;
  height: auto;
  opacity: 0.8;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
  transition: all 0.3s ease;
}

.footer-img:hover {
  opacity: 1;
  transform: scale(1.05);
  filter: drop-shadow(0 6px 12px rgba(0, 0, 0, 0.15));
}

.copyright {
  margin: 0;
  color: #666;
  font-size: 14px;
}
</style>
