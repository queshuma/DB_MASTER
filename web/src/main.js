import { createApp } from 'vue';
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import App from './App.vue';
import router from './router';
import './style.css';
import auth from './plugins/auth';
import store from './store';

// 初始化时从localStorage加载用户信息
const savedUserInfo = localStorage.getItem('userInfo');
if (savedUserInfo) {
  try {
    const userInfo = JSON.parse(savedUserInfo);
    store.commit('setUserInfo', userInfo);
  } catch (error) {
    console.error('解析本地用户信息失败:', error);
    localStorage.removeItem('userInfo');
  }
}
import { DatabaseOutlined, UserOutlined, MessageOutlined, HistoryOutlined } from '@ant-design/icons-vue';
import link from './link/Link.js';

// 登录拦截和用户信息初始化
router.beforeEach((to, from, next) => {
  // 不需要登录的页面
  const publicPages = ['/register','/login', '/email-login'];
  const authRequired = !publicPages.includes(to.path);
  const loggedIn = localStorage.getItem('token');
  const hasUserInfo = store.getters.isLoggedIn;

  // 如果需要登录但未登录，则跳转到登录页面
  if (authRequired && !loggedIn) {
    next('/login');
  } else if (authRequired && loggedIn && !hasUserInfo) {
    // 如果已登录但没有用户信息，则获取用户信息
    link('/user/getUserInfo', 'GET', {}, {}, {}, true, 'json') 
      .then(response => {
        if (response.resultCode === '200') {
          store.commit('setUserInfo', response.result);
          next();
        } else {
          // 获取用户信息失败，清除token并跳转到登录页
          localStorage.removeItem('token');
          next('/login');
        }
      }) 
      .catch(error => {
        console.error('获取用户信息失败:', error);
        localStorage.removeItem('token');
        next('/login');
      });
  } else {
    // 确保在路由跳转时触发组件的重新渲染
    if (to.path !== from.path) {
      next();
    } else {
      // 如果是相同路径的跳转，强制刷新页面
      window.location.reload();
    }
  }
});

const app = createApp(App);
app.config.globalProperties.$auth = auth;
// 全局注册图标组件
app.component('DatabaseOutlined', DatabaseOutlined);
app.component('UserOutlined', UserOutlined);
app.component('MessageOutlined', MessageOutlined);
app.component('HistoryOutlined', HistoryOutlined);
app.use(router).use(Antd).use(store).mount('#app');
