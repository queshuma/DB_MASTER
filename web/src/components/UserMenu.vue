<script setup>
import { useRouter } from 'vue-router';
import { Avatar } from 'ant-design-vue';
import { computed, ref, onMounted, onUnmounted } from 'vue';
import { useStore } from 'vuex';

// 控制下拉菜单的打开状态
const openDropdown = ref(false);

// 路由和状态管理
const router = useRouter();
const store = useStore();

// 计算属性
const isLoggedIn = computed(() => store.getters.isLoggedIn);
const userInfo = computed(() => store.getters.userInfo);

// 调试信息
onMounted(() => {
  console.log('UserMenu component mounted');
  console.log('isLoggedIn:', isLoggedIn.value);
  console.log('userInfo:', userInfo.value);
});

// 切换下拉菜单的打开状态
const toggleDropdown = () => {
  openDropdown.value = !openDropdown.value;
  console.log('Dropdown toggled:', openDropdown.value);
};

// 未登录状态会被全局拦截跳转到登录页面，无需在此处理


// 退出登录函数
const handleLogout = () => {
  // 清除token和用户信息
  localStorage.removeItem('token');
  store.commit('clearUserInfo');
  // 重定向到登录页
  router.push('/login');
  console.log('User logged out');
};

// 点击页面其他地方关闭下拉菜单
const closeDropdownOnClickOutside = (event) => {
  const dropdown = document.querySelector('.user-dropdown-menu');
  const userInfoContainer = document.querySelector('.user-info-container');
  if (dropdown && !dropdown.contains(event.target) && !userInfoContainer.contains(event.target)) {
    openDropdown.value = false;
  }
};

// 监听点击事件
onMounted(() => {
  document.addEventListener('click', closeDropdownOnClickOutside);
});

// 清理事件监听
onUnmounted(() => {
  document.removeEventListener('click', closeDropdownOnClickOutside);
});

</script>

<template>
  <div class="header-user-container">
    <div class="user-info-container" @click.stop="toggleDropdown">
      <Avatar size="small" class="user-avatar">
        {{ isLoggedIn ? (userInfo.username?.charAt(0).toUpperCase() || '用') : '未' }}
      </Avatar>
      <span class="username">{{ isLoggedIn ? (userInfo.username || '用户') : '未登录' }}</span>
    </div>
    <div v-if="openDropdown && isLoggedIn" class="user-dropdown-menu">
      <div class="dropdown-item" @click.stop="handleLogout">退出登录</div>
    </div>
    <!-- 未登录状态会被全局拦截跳转到登录页面，无需在此显示 -->

  </div>
</template>

<style scoped>
.header-user-container {
  display: flex;
  align-items: center;
  padding: 0 28px 0 20px;
  height: 100%;
  position: relative;
}

.user-info-container {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
  height: 60%;
  box-sizing: border-box;
  background-color: rgba(133, 131, 131, 0.126);
}

.user-info-container:hover {
  background-color: #f0f7ff;
}

.user-info-container:active {
  background-color: #e6f4ff;
}

.user-avatar {
  background-color: #1890ff;
  color: white;
}

.username {
  font-size: 15px;
  color: #1890ff;
  font-weight: 500;
  padding: 0 8px;
}

.user-dropdown-menu {
  position: absolute;
  top: 100%;
  right: 24px;
  margin-top: 4px;
  padding: 4px 0;
  background-color: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  z-index: 1050;
  min-width: 140px;
}

.dropdown-item {
  padding: 6px 12px;
  cursor: pointer;
  transition: all 0.3s;
  box-sizing: border-box;
  width: 100%;
  color: #1890ff;
  text-align: center;
  border-radius: 4px;
  border: none;
  margin: 2px 0;
  height: 36px;
  line-height: 24px;
  font-size: 14px;
}

.dropdown-item:hover {
  background-color: #f0f7ff;
}

/* 未登录状态会被全局拦截，相关样式已移除 */

</style>