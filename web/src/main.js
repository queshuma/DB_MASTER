import { createApp } from 'vue';
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import App from './App.vue';
import router from './router';
import './style.css';
import auth from './plugins/auth';

// 已取消登录拦截
// router.beforeEach((to, from, next) => {
//   // 不需要登录的页面
//   const publicPages = ['/login', '/register'];
//   const authRequired = !publicPages.includes(to.path);
//   const loggedIn = localStorage.getItem('token');

//   // 如果需要登录但未登录，则跳转到登录页面
//   if (authRequired && !loggedIn) {
//     next('/login');
//   } else {
//     next();
//   }
// });

const app = createApp(App);
app.config.globalProperties.$auth = auth;
app.use(router).use(Antd).mount('#app');
