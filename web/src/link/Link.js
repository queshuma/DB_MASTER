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

// 封装流式请求函数 - 用于处理Server-Sent Events (SSE) 或流式响应
// 注意：withCredentials参数必须为true才能在跨域请求中发送cookie
const linkStream = (url, method, data = {}, params = {}, headers = {}, withCredentials = true) => {
    console.log('linkStream: 开始构建请求');
    console.log('linkStream: 原始URL:', url);
    console.log('linkStream: 请求方法:', method);
    console.log('linkStream: 请求数据:', data);
    console.log('linkStream: 查询参数:', params);
    
    // 构建完整的URL，包含基础URL和查询参数
    let fullUrl = url;
    // 如果URL不是以http开头，则添加基础URL
    if (!fullUrl.startsWith('http')) {
        fullUrl = 'http://localhost:8000' + fullUrl;
        console.log('linkStream: 添加基础URL后:', fullUrl);
    }
    
    if (params && Object.keys(params).length > 0) {
        const queryParams = new URLSearchParams();
        for (const [key, value] of Object.entries(params)) {
            queryParams.append(key, value);
        }
        fullUrl += (fullUrl.includes('?') ? '&' : '?') + queryParams.toString();
        console.log('linkStream: 添加查询参数后:', fullUrl);
    }
    
    // 准备请求头
    const requestHeaders = { ...headers };
    // 确保Accept头设置为text/event-stream
    if (!requestHeaders['accept'] && !requestHeaders['Accept']) {
        requestHeaders['accept'] = 'text/event-stream';
    }
    
    console.log('linkStream: 最终请求头:', requestHeaders);
    console.log('linkStream: withCredentials:', withCredentials);
    // 检查当前域名下的cookie情况
    console.log('linkStream: 当前域名cookie:', document.cookie);
    console.log('linkStream: 当前域名cookie数量:', document.cookie.split(';').length);
    
    // 不自动添加token
    
    // 创建AbortController用于取消请求
    const abortController = new AbortController();
    const { signal } = abortController;
    
    // 发送流式请求 - 使用XMLHttpRequest替代fetch以确保cookie正确传递
    const requestPromise = new Promise((resolve, reject) => {
        console.log('linkStream: 准备发送XMLHttpRequest请求');
        
        // 创建XMLHttpRequest对象
        const xhr = new XMLHttpRequest();
        xhr.withCredentials = withCredentials;
        
        // 配置请求
        xhr.open(method, fullUrl);
        
        // 设置请求头
        for (const [key, value] of Object.entries(requestHeaders)) {
            xhr.setRequestHeader(key, value);
        }
        
        // 设置responseType为流式处理
        xhr.responseType = 'text';
        
        // 用于存储已处理的响应文本位置
        let lastResponseLength = 0;
        
        // 确认withCredentials参数是否正确设置
        console.log('linkStream: XMLHttpRequest withCredentials值:', xhr.withCredentials);
        console.log('linkStream: XMLHttpRequest请求URL:', fullUrl);
        console.log('linkStream: XMLHttpRequest请求方法:', method);
        console.log('linkStream: XMLHttpRequest请求头:', requestHeaders);
        
        // 监听进度事件以实现流式处理
        xhr.onprogress = function(event) {
            if (event.lengthComputable) {
                console.log('linkStream: 已加载:', event.loaded, '总大小:', event.total);
            }
        };
        
        // 监听状态变化
        xhr.onreadystatechange = function() {
            if (xhr.readyState === XMLHttpRequest.HEADERS_RECEIVED) {
                console.log('linkStream: 收到响应头:', xhr.status, xhr.statusText);
                console.log('linkStream: Set-Cookie:', xhr.getResponseHeader('Set-Cookie'));
            }
            
            if (xhr.readyState === XMLHttpRequest.DONE) {
                cleanup(); // 确保完成时清理资源
                
                if (xhr.status >= 200 && xhr.status < 300) {
                    // 模拟fetch的Response对象结构，以便调用者可以像使用fetch一样处理响应
                    const mockResponse = {
                        ok: true,
                        status: xhr.status,
                        statusText: xhr.statusText,
                        headers: {
                            get: function(name) {
                                return xhr.getResponseHeader(name);
                            },
                            has: function(name) {
                                return xhr.getResponseHeader(name) !== null;
                            },
                            keys: function() {
                                // 简化实现，返回所有响应头
                                const headers = {};
                                const allHeaders = xhr.getAllResponseHeaders().split('\n');
                                for (const header of allHeaders) {
                                    if (header) {
                                        const [name, value] = header.split(': ');
                                        headers[name] = value;
                                    }
                                }
                                return Object.keys(headers);
                            }
                        },
                        body: {
                            getReader: function() {
                                // 实现真正的流式读取 - 正确处理断点格式
                                let buffer = ''; // 数据缓冲区
                                let isComplete = false; // 标记是否收到完成信号
                                
                                // 解析文本流数据，处理[data:]格式和[complete]标记
                                const parseStreamData = (text) => {
                                    // 分割成事件流格式的行
                                    const lines = text.split(/\r?\n/);
                                    const parsedData = [];
                                    
                                    for (let line of lines) {
                                        line = line.trim();
                                        if (line === '') continue;
                                         
                                        // 处理data:前缀的数据
                                        if (line.startsWith('data:')) {
                                            const data = line.substring(5).trim();
                                            parsedData.push(data);
                                        } 
                                        // 检查是否包含完成标记
                                        else if (line.includes('[complete]')) {
                                            isComplete = true;
                                            // 提取完成标记前的数据
                                            const data = line.replace('[complete]', '').trim();
                                            if (data) {
                                                parsedData.push(data);
                                            }
                                        } else {
                                            // 直接添加其他数据
                                            parsedData.push(line);
                                        }
                                    }
                                     
                                    return parsedData.join('');
                                };
                                
                                return {
                                    read: function() {
                                        const reader = this; // 保存reader实例的引用
                                        return new Promise((resolve) => {
                                            // 检查是否有新的数据可用
                                            if (lastResponseLength >= xhr.responseText.length) {
                                                // 如果请求已经完成，处理剩余缓冲区数据
                                                if (xhr.readyState === XMLHttpRequest.DONE) {
                                                    if (buffer.length > 0) {
                                                        // 将缓冲区数据发送出去
                                                        const encoder = new TextEncoder();
                                                        const chunk = encoder.encode(buffer);
                                                        buffer = '';
                                                        console.log('linkStream: 发送完成前的缓冲区数据，大小:', chunk.length);
                                                        resolve({ done: false, value: chunk });
                                                    } else if (isComplete) {
                                                        // 发送完成标记
                                                        const encoder = new TextEncoder();
                                                        const chunk = encoder.encode('[complete]');
                                                        isComplete = false;
                                                        console.log('linkStream: 发送完成标记');
                                                        resolve({ done: false, value: chunk });
                                                    } else {
                                                        resolve({ done: true, value: null });
                                                    }
                                                } else {
                                                    // 否则设置一个小的延迟，继续等待新数据
                                                    setTimeout(() => {
                                                        resolve(reader.read()); // 使用保存的引用
                                                    }, 50);
                                                }
                                            } else {
                                                // 提取新的文本块
                                                const newText = xhr.responseText.substring(lastResponseLength);
                                                lastResponseLength = xhr.responseText.length;
                                                
                                                // 解析流式数据格式
                                                const parsedText = parseStreamData(newText);
                                                
                                                if (parsedText) {
                                                    // 将解析后的数据转换为Uint8Array
                                                    const encoder = new TextEncoder();
                                                    const chunk = encoder.encode(parsedText);
                                                    
                                                    console.log('linkStream: 发送解析后的数据块，大小:', chunk.length);
                                                    console.log('linkStream: 数据块内容:', parsedText);
                                                    resolve({ done: false, value: chunk });
                                                } else {
                                                    // 继续等待新数据
                                                    setTimeout(() => {
                                                        resolve(reader.read());
                                                    }, 50);
                                                }
                                            }
                                        });
                                    }
                                };
                            }
                        }
                    };
                    
                    resolve({
                        response: mockResponse,
                        abortController
                    });
                } else {
                    reject(new Error(`请求失败: ${xhr.status} ${xhr.statusText}`));
                }
            }
        };
        
        // 错误处理
        xhr.onerror = function() {
            cleanup();
            reject(new Error('网络错误'));
        };
        
        // 监听abort事件
        xhr.onabort = function() {
            cleanup();
            console.log('linkStream: 请求已中止');
        };
        
        // 设置abortController的signal
        const abortListener = function() {
            console.log('linkStream: 请求被取消');
            xhr.abort();
        };
        abortController.signal.addEventListener('abort', abortListener);
        
        // 在请求完成或出错时移除事件监听器，防止内存泄漏
        const cleanup = function() {
            abortController.signal.removeEventListener('abort', abortListener);
        };
        
        // 发送请求（如果是非GET请求，需要发送body）
        if (method.toUpperCase() !== 'GET' && data && Object.keys(data).length > 0) {
            const body = JSON.stringify(data);
            console.log('linkStream: 发送请求体:', body);
            xhr.send(body);
        } else {
            xhr.send();
        }
    });
    
    return requestPromise;
}

// 导出link和linkStream函数
export { link, linkStream };
export default link
