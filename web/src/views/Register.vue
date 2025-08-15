<script setup>
import { reactive, ref } from 'vue';
import { UserOutlined, PhoneOutlined, LockOutlined, MailOutlined } from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import link from '../link/Link.js';

const router = useRouter();
const formState = reactive({
  username: '',
  telephone: '',
  password: '',
  confirmPassword: '',
  email: ''
});


// 注册功能
const handleSubmit = async () => {
  if (!formState.username || formState.username.trim() === '') {
    message.error('请输入用户名');
    return;
  }

  if (!formState.telephone || !/^1[3-9]\d{9}$/.test(formState.telephone)) {
    message.error('请输入有效的手机号码');
    return;
  }

  if (!formState.password || formState.password.length < 6) {
    message.error('密码长度不能少于6位');
    return;
  }

  if (formState.password !== formState.confirmPassword) {
    message.error('两次输入的密码不一致');
    return;
  }

  if (!formState.email || !/^\S+@\S+\.\S+$/.test(formState.email)) {
    message.error('请输入有效的邮箱地址');
    return;
  }

  try {

    // 使用Link工具发送注册请求
    await link('/user/register', 'POST', {
      username: formState.username,
      password: formState.password,
      telephone: formState.telephone,
      email: formState.email
    });
    message.success('注册成功，请登录');
    router.push('/login');
  } catch (error) {
    message.error('注册失败: ' + (error.message || '未知错误'));
  } finally {

  }
};

const handleSubmitFailed = (errors) => {
  console.error('表单验证失败:', errors);
};
</script>

<template>
  <a-layout class="layout-container">
    <a-row :gutter="[24, 16]" align="middle" justify="center" class="min-h-screen">
        <a-col :xs="24" :sm="20" :md="16" :lg="12" :xl="10">
          <a-card 
            :headStyle="{ fontSize: '24px', borderBottom: 'none', padding: '20px 0' }"
            :bodyStyle="{ padding: '20px 20px 20px' }"
            class="shadow-lg">
          <a-form
            :model="formState"
            layout="vertical"
            :validate-trigger="['blur', 'change']"
            @finish="handleSubmit"
            @finishFailed="handleSubmitFailed"
          >
      <h2 class="form-title">账号注册</h2>
      <a-form-item
  label="用户名"
  name="username"
  :rules="[{ required: true, message: '请输入用户名' }]"
>
  <a-input
    v-model:value="formState.username"
    placeholder="请输入4-16位字母数字组合"
    allow-clear
    autocomplete="username"
    :maxLength="16"
  >
    <template #prefix>
      <user-outlined type="user" />
    </template>
  </a-input>
</a-form-item>
      <a-form-item
        label="手机号码"
        name="telephone"
        :rules="[{ required: true, message: '请输入手机号码' }]"
      >
        <a-input
          v-model:value="formState.telephone"
          placeholder="请输入手机号码"
          allow-clear
          autocomplete="tel"
        >
          <template #prefix>
            <phone-outlined type="phone" />
          </template>
        </a-input>
      </a-form-item>
      <a-form-item
        label="邮箱"
        name="email"
        :rules="[{ required: true, message: '请输入邮箱' }]"
      >
        <a-input
          v-model:value="formState.email"
          placeholder="请输入邮箱"
          allow-clear
          autocomplete="email"
        >
          <template #prefix>
            <MailOutlined type="mail" />
          </template>
        </a-input>
      </a-form-item>
      <a-form-item
        label="设置密码"
        name="password"
        :rules="[{ required: true, message: '请设置密码' }]"
      >
        <a-input-password
          v-model:value="formState.password"
          placeholder="请设置6-20位密码"
          allow-clear
          autocomplete="new-password"
        >
          <template #prefix>
            <lock-outlined type="lock" />
          </template>
        </a-input-password>
      </a-form-item>
      <a-form-item
        label="确认密码"
        name="confirmPassword"
        :rules="[{ required: true, message: '请再次输入密码' }]"
      >
        <a-input-password
          v-model:value="formState.confirmPassword"
          placeholder="请再次输入密码"
          allow-clear
          autocomplete="new-password"
        >
          <template #prefix>
            <lock-outlined type="lock" />
          </template>
        </a-input-password>
      </a-form-item>
      <a-form-item>
        <a-button
          type="primary"
          html-type="submit"
          block
          size="large"
        >
          注册
        </a-button>
      </a-form-item>
            <div class="text-center mt-4">
              <router-link to="/login">
                <a-button type="link">已有账号？立即登录</a-button>
              </router-link>
            </div>
                    </a-form>
          </a-card>
      </a-col>
      </a-row>
    </a-layout>
</template>

<style scoped>
.layout-container {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;

  .ant-card {
    border-radius: 16px;
    box-shadow: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
    transition: transform 0.2s ease-in-out;
    max-width: 25%; /* 设置为页面宽度的1/4 */
    min-width: 400px; /* 确保小屏幕上有足够宽度 */
    margin: 0 auto; /* 水平居中 */
    height: auto;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.ant-card:hover {
    transform: translateY(-2px);
}

.ant-form {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.ant-form-item-label > label {
    @apply text-gray-600 font-medium;
    text-align: left;
}

.ant-input-affix-wrapper {
    @apply rounded-lg border-gray-200 hover:border-blue-400;
    width: 100%; /* 恢复原始宽度 */
    margin: 0 auto; /* 水平居中 */
}

.ant-btn-primary {
    @apply bg-gradient-to-r from-blue-500 to-blue-600 shadow-md hover:shadow-lg transition-all;
    width: 100% !important; /* 与输入框宽度一致 */
    margin: 0 auto !important; /* 水平居中 */
    display: block !important; /* 确保margin: auto生效 */
}

  .ant-form-item {
  margin-bottom: 1rem;
  width: 100%;
  max-width: 90%; /* 确保不超过卡片宽度 */
}

  .ant-btn {
    &-block {
      height: 40px;
      font-size: 1rem;
    }
  }
}

.register-form {
  max-width: 400px;
  width: 100%;
  padding: 40px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin: 0 auto; /* 水平居中 */
}

.form-title {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

/* 移除自定义form-item样式，使用Ant Design的表单样式 */
/* .form-item {
  margin-bottom: 24px;
} */

.form-label {
    display: block;
    margin-bottom: 8px;
    color: #666;
    font-size: 14px;
    width: 100%; /* 与输入框宽度一致 */
    margin: 0 auto 8px; /* 水平居中 */
}

.form-input {
    width: 100%; /* 恢复原始宽度 */
    padding: 12px;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    font-size: 14px;
    transition: border-color 0.3s;
    margin: 0 auto; /* 水平居中 */
    display: block; /* 确保margin: auto生效 */
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

.register-btn {
    width: 100%; /* 与输入框宽度一致 */
  padding: 12px;
  background-color: #1890ff;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  margin: 0 auto; /* 水平居中 */
  display: block; /* 确保margin: auto生效 */
}

.register-btn:hover:not(:disabled) {
  background-color: #40a9ff;
}

.register-btn:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}

.login-link {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.login-link a {
  color: #1890ff;
  cursor: pointer;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>