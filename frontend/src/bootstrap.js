import { createApp } from 'vue'
import '@/styles.css'
import '@/assets/css/reset.css'
import '@/assets/css/index.scss'
import '@/assets/css/fonts/iconfont.css'
import App from '@/App.vue'
import { createPinia } from 'pinia'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import '@/assets/css/style.scss'
import '@/assets/css/workbench.scss'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import btn from '@/directives/btn'
import dragDirective from '@/directives/drag.js'
import { vResize } from '@/directives/vResize.js'

const pinia = createPinia()
const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
	app.component(key, component)
}

app.directive('drag', dragDirective)
app.directive('resize', vResize)
app.use(pinia)
app.use(router)
app.use(ElementPlus, { zIndex: 3000 }).use(btn)
app.mount('#app')
