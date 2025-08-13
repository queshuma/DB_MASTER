// 请求拦截器配置
import axios from 'axios';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';

// 创建axios实例
const instance = axios.create({
  baseURL: '/api',
  timeout: 5000
});

// 请求拦截器
instance.interceptors.request.use(
  config => {
    // 从本地存储获取token
    const token = localStorage.getItem('token');
    // 如果token存在，添加到请求头
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
instance.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      // 清除本地存储的token
      localStorage.removeItem('token');
      // 跳转到登录页面
      const router = useRouter();
      router.push('/login');
      message.error('登录已过期，请重新登录');
    }
    return Promise.reject(error);
  }
);

// 导出请求方法
export default {
  get: (url, params) => instance.get(url, { params }),
  post: (url, data) => instance.post(url, data),
  put: (url, data) => instance.put(url, data),
  delete: (url) => instance.delete(url)
};