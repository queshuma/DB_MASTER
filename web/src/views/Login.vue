<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { message } from 'ant-design-vue';
import link from '../link/Link.js';

const router = useRouter();
const store = useStore();
const username = ref('');
const password = ref('');
const isButtonDisabled = ref(false);

// 登录功能
const login = async () => {
  if (!username.value || username.value.trim() === '') {
    message.error('请输入用户名');
    return;
  }

  if (!password.value || password.value.length < 6) {
    message.error('密码长度不能少于6位');
    return;
  }

    // 使用Link工具发送登录请求

    link("/user/login", 'GET',{}, {      
      username: username.value,
      password: password.value}, {} )
    .then(response => {
      console.log(response.resultCode)
      if (response.resultCode === '200') {
          // 保存token到本地存储
          localStorage.setItem('token', response.token || 'dummy-token');
          // 存储用户信息到store
          store.commit('setUserInfo', response.result);
          isButtonDisabled.value = true;
          message.success('登录成功');
          console.log('登录成功，准备跳转');
          console.log('当前路由:', router.currentRoute.value);
          console.log('尝试跳转的路由:', '/database-list');
          
          // 尝试使用replace代替push
          setTimeout(() => {
            try {
              router.replace('/database-list');
              console.log('replace跳转已执行');
            } catch (err) {
              console.error('replace跳转失败:', err);
            }
          }, 500);
        } else {
        message.error(response.resultMsg);
      }
    })
};

</script>

<template>
  <div class="login-container">
    <div class="login-form">
      <h2 class="form-title">账号登录</h2>
      <div class="form-item">
        <label for="username" class="form-label">用户名</label>
        <input
          type="text"
          id="username"
          v-model="username"
          placeholder="请输入用户名"
          class="form-input"
        />
      </div>
      <div class="form-item">
        <label for="password" class="form-label">密码</label>
        <input
          type="password"
          id="password"
          v-model="password"
          placeholder="请输入密码"
          class="form-input"
        />
      </div>
      <button
        class="login-btn"
        @click="login"
        :disabled="isButtonDisabled"
      >
        登录
      </button>
      <div class="register-link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
      <div class="register-link">
        使用邮箱登录？<router-link to="/email-login">前往邮箱登录</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f5f5;
}

.login-form {
  width: 400px;
  padding: 40px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.form-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.form-item {
  margin-bottom: 24px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: #1890ff;
}

.code-input-container {
  display: flex;
  gap: 12px;
}

.code-input {
  flex: 1;
}

.send-code-btn {
  padding: 0 16px;
  background-color: #1890ff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.send-code-btn:hover:not(:disabled) {
  background-color: #40a9ff;
}

.send-code-btn:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}

.login-btn {
  width: 100%;
  padding: 12px;
  background-color: #1890ff;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-btn:hover:not(:disabled) {
  background-color: #40a9ff;
}

.login-btn:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}

.register-link {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.register-link a {
  color: #1890ff;
  cursor: pointer;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>