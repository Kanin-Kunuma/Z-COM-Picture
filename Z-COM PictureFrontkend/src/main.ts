
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import '@/access.ts'
import VueCropper from 'vue-cropper';
import 'vue-cropper/dist/index.css'
import { createApp } from 'vue'
import naive from 'naive-ui'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './theme.css'
import './styles/index.css';

console.log('Routes:', router.getRoutes());
const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)
app.use(VueCropper)
app.use(naive)
app.use(ElementPlus);

app.mount('#app')
