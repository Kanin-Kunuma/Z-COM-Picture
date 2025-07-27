<template>
  <n-config-provider :theme="darkTheme">
    <div id="app">
      <BasicLayout />
    </div>
  </n-config-provider>
</template>



<script>

import { defineComponent } from 'vue'
import BasicLayout from '@/layouts/BasicLayout.vue'
import { darkTheme } from 'naive-ui'

export default defineComponent({
  components: { BasicLayout },
  setup() {
    return {
      darkTheme,
    }
  },
})
</script>
