import axios from 'axios';

/**
 * 线上环境
 */
// const baseURL = '/api';
/**
 * 本地环境
 */
const baseURL = 'http://localhost:8000';


// 创建axios实例
const service = axios.create({
    baseURL: baseURL, // 设置基础URL
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
        // 对于blob类型的响应，直接返回完整的response对象
        if (response.request.responseType === 'blob') {
            return response;
        }
        
        if(response.data && response.data.code === 401) {
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
const streamingRequest = (url, params = {}, options = { withCredentials: true }) => {
  const eventSource = new EventSource(baseURL + `${url}?${new URLSearchParams(params).toString()}`, options);
  
  const controller = {
    onMessage: (callback) => {
      eventSource.onmessage = (e) => {
        if (e.data === '[complete]') {
          eventSource.close();
          controller.onComplete?.(true);
          return;
        }
        callback(e.data);
      };
      return controller;
    },
    onComplete: (callback) => {
      controller._completeCallback = callback;
      return controller;
    },
    onError: (callback) => {
      eventSource.onerror = callback;
      return controller;
    },
    close: () => eventSource.close()
  };
  return controller;
};
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
};
// 封装流式请求函数 - 用于处理Server-Sent Events (SSE) 或流式响应
// 注意：withCredentials参数必须为true才能在跨域请求中发送cookie

// 统一导出请求方法
export { link, streamingRequest };
export default link
