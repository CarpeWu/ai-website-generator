<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <div class="footer-image">
        <img :src="footerImage" alt="Footer decoration" class="footer-img">
      </div>
      <p class="copyright">
        <a
          href="https://github.com/CarpeWu"
          target="_blank"
          rel="noopener noreferrer"
          class="author-link"
        >
designed by Carpe Wu
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
  background: #ffffff;
  text-align: center;
  padding: 32px 20px;
  margin-top: 80px;
  border-top: 1px solid #f0f0f0;
}

.footer-image {
  margin-bottom: 15px;
}

.footer-img {
  width: 100px;
  height: auto;
  opacity: 0.7;

  transition: all 0.2s ease;
}

.footer-img:hover {
  opacity: 0.9;
  transform: scale(1.02);

}

.copyright {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}
</style>
