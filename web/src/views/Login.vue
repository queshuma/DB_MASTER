<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import axios from 'axios';

const router = useRouter();
const phoneNumber = ref('');
const verificationCode = ref('');
const countdown = ref(0);
const isButtonDisabled = ref(false);

// 发送验证码
const sendVerificationCode = async () => {
  if (!phoneNumber.value || !/^1[3-9]\d{9}$/.test(phoneNumber.value)) {
    message.error('请输入有效的手机号码');
    return;
  }

  try {
    isButtonDisabled.value = true;
    // 实际项目中替换为真实的发送验证码API
    await axios.post('/api/send-sms', {
      phone: phoneNumber.value
    });
    message.success('验证码发送成功');
    startCountdown();
  } catch (error) {
    message.error('验证码发送失败: ' + error.message);
  } finally {
    isButtonDisabled.value = false;
  }
};

// 倒计时功能
const startCountdown = () => {
  countdown.value = 60;
  const timer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(timer);
    }
  }, 1000);
};

// 已移除，使用router-link代替;

// 登录功能
const login = async () => {
  if (!phoneNumber.value || !/^1[3-9]\d{9}$/.test(phoneNumber.value)) {
    message.error('请输入有效的手机号码');
    return;
  }

  if (!verificationCode.value || verificationCode.value.length !== 6) {
    message.error('请输入6位验证码');
    return;
  }

  try {
    isButtonDisabled.value = true;
    // 实际项目中替换为真实的登录API
    const response = await axios.post('/api/login', {
      phone: phoneNumber.value,
      code: verificationCode.value
    });

    // 保存token到本地存储
    localStorage.setItem('token', response.data.token);
    message.success('登录成功');
    router.push('/chatbot');
  } catch (error) {
    message.error('登录失败: ' + error.message);
  } finally {
    isButtonDisabled.value = false;
  }
};
</script>

<template>
  <div class="login-container">
    <div class="login-form">
      <h2 class="form-title">账号登录</h2>
      <div class="form-item">
        <label for="phone" class="form-label">手机号码</label>
        <input
          type="tel"
          id="phone"
          v-model="phoneNumber"
          placeholder="请输入手机号码"
          class="form-input"
          :disabled="countdown > 0"
        />
      </div>
      <div class="form-item">
        <label for="code" class="form-label">验证码</label>
        <div class="code-input-container">
          <input
            type="text"
            id="code"
            v-model="verificationCode"
            placeholder="请输入验证码"
            class="form-input code-input"
            maxlength="6"
          />
          <button
            class="send-code-btn"
            @click="sendVerificationCode"
            :disabled="countdown > 0 || isButtonDisabled"
          >
            {{ countdown > 0 ? `${countdown}秒后重新发送` : '获取验证码' }}
          </button>
        </div>
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