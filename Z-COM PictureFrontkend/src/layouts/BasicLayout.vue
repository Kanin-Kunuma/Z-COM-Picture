<template>
  <a-config-provider :theme="antdTheme">

    <div class="theme-float-btn">
      <a-button @click="toggleTheme" shape="circle" size="large" style="padding: 0; width: 40px; height: 40px;">
        <img
          :src="isDark ? BulbLight : BulbDark"
          alt="theme"
          style="width: 24px; height: 24px;"
          :class="{ 'bulb-bright': isDark }"
        />
      </a-button>
    </div>

    <div id="basicLayout">
      <a-layout style="min-height: 100vh">
        <a-layout-header class="header">
          <GlobalHeader />
        </a-layout-header>
        <a-layout>
          <GlobalSider class="sider" />
          <a-layout-content class="content">
            <router-view />
          </a-layout-content>
        </a-layout>
        <a-layout-footer class="footer">
          <a href="https://www.codefather.cn" target="_blank"> 人机玛巴 </a>
        </a-layout-footer>
      </a-layout>
    </div>
  </a-config-provider>
</template>

<script lang="ts" setup>
import GlobalHeader from '@/components/icons/GlobalHeader.vue'
import GlobalSider from '@/components/icons/GlobalSider.vue'
import { ref } from 'vue'
import { theme } from 'ant-design-vue'
import BulbDark from '@/components/assets/Bulb-Dark.svg'
import BulbLight from '@/components/assets/Bulb-Light.svg'

const { defaultAlgorithm, darkAlgorithm } = theme

const antdTheme = ref({
  algorithm: defaultAlgorithm
})

const isDark = ref(false)
const toggleTheme = () => {
  isDark.value = !isDark.value
  antdTheme.value.algorithm = isDark.value ? darkAlgorithm : defaultAlgorithm
}
</script>

<style scoped>
:root {
  --my-bg: #fff;
  --my-text: #222;
}
html.dark {
  --my-bg: #18181c;
  --my-text: #fff;
}
#basicLayout .header,
#basicLayout .content,
#basicLayout .footer {
  background: var(--my-bg) !important;
  color: var(--my-text) !important;
}
#basicLayout .footer {
  padding: 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}
#basicLayout .content {
  margin-bottom: 28px;
  padding: 20px;
}
#basicLayout .header {
  padding-inline: 20px;
  margin-bottom: 16px;
  color: unset;
}
.bulb-bright {
  filter: drop-shadow(0 0 12px #ffffffff) brightness(3) contrast(1.2);
  transition: filter 0.3s;
}
.theme-float-btn {
  position: fixed;
  left: 24px;
  bottom: 24px;
  z-index: 9999;
}
</style>
