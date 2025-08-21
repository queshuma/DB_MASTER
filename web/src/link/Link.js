import axios from 'axios';

// 创建axios实例
const service = axios.create({
    baseURL: 'http://localhost:8000', // 设置基础URL
    timeout: 5000, // 设置超时时间
    withCredentials: true // 默认携带cookie
});

// 请求拦截器
service.interceptors.request.use(
    config => {
        // 在发送请求之前做些什么，例如添加token
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        // 对请求错误做些什么
        console.error('请求错误:', error);
        return Promise.reject(error);
    }
);

// 响应拦截器
service.interceptors.response.use(
    response => {
        if(response.data.code === 401) {
            // 清除本地存储的token
            localStorage.removeItem('token');
            // 重定向到登录页面
            window.location.href = '/login';
        }
        // 对响应数据做点什么
        return response.data;
    },
    error => {
        // 对响应错误做点什么
        console.error('响应错误:', error);
        return Promise.reject(error);
    }
);

// 封装请求函数
const link = (url, method, data = {}, params = {}, headers = {}, withCredentials = true, responseType = 'json') => {
    return service.request({
        url,
        method,
        data,
        params,
        headers,
        withCredentials,
        responseType
    });
}

export default link
