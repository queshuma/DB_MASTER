<script setup>
import { useRouter } from 'vue-router';
import { Button, Space, Dropdown, Menu } from 'ant-design-vue';
import { ref, onMounted, computed } from 'vue';
import { useStore } from 'vuex';
import link from '../link/Link.js';

const router = useRouter();
const store = useStore();
const isLoggedIn = computed(() => store.getters.isLoggedIn);
const userInfo = computed(() => store.getters.userInfo);

// 退出登录
const logout = () => {
  localStorage.removeItem('token');
  store.commit('clearUserInfo');
  router.push('/login');
};

const goToLogin = () => {
  router.push('/login');
};

const goToRegister = () => {
  router.push('/register');
};

// 组件挂载时不需要检查登录状态，通过Vuex store实时获取

</script>

<template>
  <div class="auth-links-container">
    <Space v-if="isLoggedIn">
      <Dropdown trigger="hover">
        <template #overlay>
          <Menu>
            <Menu.Item key="1" @click="logout">退出登录</Menu.Item>
          </Menu>
        </template>
        <Button type="text">
          {{ userInfo.username || '用户' }}
        </Button>
      </Dropdown>
    </Space>
    <Space v-else>
      <Button type="default" @click="goToLogin">登录</Button>
      <Button type="primary" @click="goToRegister">注册</Button>
    </Space>
  </div>
</template>

<style scoped>
.auth-links-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
  background-color: #f5f5f5;
  border-bottom: 1px solid #e8e8e8;
}
</style>