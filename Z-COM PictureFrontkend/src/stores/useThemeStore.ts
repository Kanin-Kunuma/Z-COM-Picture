import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  // 主题状态
  const isDarkMode = ref(localStorage.getItem('theme') === 'dark')

  // 切换主题
  const toggleTheme = () => {
    isDarkMode.value = !isDarkMode.value
    localStorage.setItem('theme', isDarkMode.value ? 'dark' : 'light')
    applyTheme()
  }

  // 应用主题到页面
  const applyTheme = () => {
    const html = document.documentElement
    const body = document.body

    if (isDarkMode.value) {
      html.classList.add('dark')
      html.setAttribute('data-theme', 'dark')
      body.classList.add('dark')
      body.style.backgroundColor = '#1f1f1f'
      body.style.color = '#ffffff'
    } else {
      html.classList.remove('dark')
      html.setAttribute('data-theme', 'light')
      body.classList.remove('dark')
      body.style.backgroundColor = '#ffffff'
      body.style.color = '#000000'
    }
  }

  // 初始化主题
  const initTheme = () => {
    applyTheme()
  }

  // 监听主题变化
  watch(isDarkMode, () => {
    applyTheme()
  }, { immediate: true })

  return {
    isDarkMode,
    toggleTheme,
    applyTheme,
    initTheme
  }
})
