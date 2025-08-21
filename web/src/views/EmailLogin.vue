<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { message } from 'ant-design-vue';
import link from '../link/Link.js';

const router = useRouter();
const store = useStore();
const email = ref('');
const code = ref('');
const isButtonDisabled = ref(false);
const codeButtonText = ref('发送验证码');
const codeButtonDisabled = ref(false);
const countdown = ref(300);

// 验证邮箱格式
const validateEmail = (email) => {
  return /^\S+@\S+\.\S+$/.test(email);
};

// 发送验证码
const sendCode = async () => {
  if (!email.value || !validateEmail(email.value)) {
    message.error('请输入有效的邮箱地址');
    return;
  }

  codeButtonDisabled.value = true;
  codeButtonText.value = `重新发送(${countdown.value}s)`;

  try {
    // 调用发送验证码API
    await link('/email/sendEmail', 'GET', {}, {
      email: email.value
    });
    message.success('验证码发送成功');

    // 倒计时
    const timer = setInterval(() => {
      countdown.value--;
      codeButtonText.value = `重新发送(${countdown.value}s)`;

      if (countdown.value <= 0) {
        clearInterval(timer);
        codeButtonDisabled.value = false;
        codeButtonText.value = '发送验证码';
        countdown.value = 60;
      }
    }, 1000);
  } catch (error) {
    message.error('发送失败: ' + (error.message || '未知错误'));
    codeButtonDisabled.value = false;
    codeButtonText.value = '发送验证码';
  }
};

// 邮箱登录功能
const emailLogin = async () => {
  if (!email.value || !validateEmail(email.value)) {
    message.error('请输入有效的邮箱地址');
    return;
  }

  if (!code.value || code.value.length !== 6) {
    message.error('请输入6位验证码');
    return;
  }

  isButtonDisabled.value = true;

link("/user/loginByEmail", 'GET',{}, {      
      email: email.value,
      dynamicPassword: code.value
    }, {} )
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
      <h2 class="form-title">邮箱登录</h2>
      <div class="form-item">
        <label for="email" class="form-label">邮箱</label>
        <input
          type="email"
          id="email"
          v-model="email"
          placeholder="请输入邮箱"
          class="form-input"
        />
      </div>
      <div class="form-item">
        <label for="code" class="form-label">验证码</label>
        <div class="code-input-container">
          <input
            type="text"
            id="code"
            v-model="code"
            placeholder="请输入6位验证码"
            class="form-input code-input"
            maxlength="6"
          />
          <button
            class="send-code-btn"
            @click="sendCode"
            :disabled="codeButtonDisabled"
          >
            {{ codeButtonText }}
          </button>
        </div>
      </div>
      <button
        class="login-btn"
        @click="emailLogin"
        :disabled="isButtonDisabled"
      >
        登录
      </button>
      <div class="register-link">
        返回<router-link to="/login">账号密码登录</router-link>
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
  padding-left: 12px;
}

.send-code-btn {
  padding: 0 16px;
  background-color: #1890ff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  white-space: nowrap;
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