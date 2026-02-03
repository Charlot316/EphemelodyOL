"use strict"

import axios from "axios"

// Full config:  https://github.com/axios/axios#request-config
// axios.defaults.baseURL = process.env.baseURL || process.env.apiUrl || ''
// axios.defaults.headers.common['Authorization'] = AUTH_TOKEN
// axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded'

// 根据当前访问的域名动态决定后端 API 地址
const getBaseURL = () => {
    const hostname = window.location.hostname;
    console.log('[Axios] hostname:', hostname);
    console.log('[Axios] origin:', window.location.origin);
    if (hostname === 'localhost' || hostname === '127.0.0.1') {
        // 本地开发环境，后端现在也有 /api 前缀
        const url = 'http://localhost:8090/api';
        console.log('[Axios] Using LOCAL baseURL:', url);
        return url;
    } else {
        // 通过 Cloudflare Tunnel 访问，使用当前域名 + /api 路径
        const url = window.location.origin + '/api';
        console.log('[Axios] Using REMOTE baseURL:', url);
        return url;
    }
};

console.log('[Axios] Initializing with baseURL:', getBaseURL());

let config = {
    //baseURL: "http://47.113.89.104:8090",
    baseURL: getBaseURL(),
    timeout: 60 * 1000, // Timeout
    withCredentials: true, // Check cross-site Access-Control
};

export const Axios = axios.create(config)

// 请求拦截器
Axios.interceptors.request.use(
    function(config) {
        // Do something before request is sent
        return config
    },
    function(error) {
        // Do something with request error
        return Promise.reject(error)
    }
);

// 响应拦截器
Axios.interceptors.response.use(
    function(response) {
        // Do something with response data
        return response
    },
    function(error) {
        // Do something with response error
        return Promise.reject(error)
    }
);

export default (app) => {
    // 挂载axios到Vue对象
    app.config.globalProperties.$http = Axios
}