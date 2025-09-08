<script setup>
import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import { Layout, Menu, MenuItem, Dropdown } from 'ant-design-vue';
import { DatabaseOutlined, SettingOutlined, MessageOutlined, HistoryOutlined, MenuUnfoldOutlined, MenuFoldOutlined, FileOutlined } from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import UserMenu from './components/UserMenu.vue';

const route = useRoute();
const router = useRouter();
const store = useStore();

// 当前选中的菜单项
const selectedKeys = ref(['1']);

// 侧边栏收起状态
const collapsed = ref(false);

// 需要全屏显示的页面
const fullPageRoutes = ['/', '/login', '/register', '/email-login'];

// 菜单数据
const menuItems = [
  { key: 'database', icon: DatabaseOutlined, label: '项目列表', path: '/database-list' },
  { key: 'settings', icon: SettingOutlined, label: '个人设置', path: '/personal-settings' },
  { key: 'chatbot', icon: MessageOutlined, label: '聊天机器人', path: '/chatbot' },
  { key: 'fileInfo', icon: FileOutlined, label: '文件信息显示', path: '/file-info' },
  { key: 'qaBot', icon: MessageOutlined, label: '问答机器人', path: '/qa-bot' },
  { key: 'records', icon: HistoryOutlined, label: '操作记录', path: '/operation-records' }
];

// 计算属性 - 是否登录已移除
// 计算属性 - 用户信息已移除
// 处理退出登录方法已移除

// 注：如需退出登录功能，请在其他位置实现
// 处理菜单选择
const handleMenuSelect = (e) => {
  const key = e.key;
  const selectedItem = menuItems.find(item => item.key === key);
  if (selectedItem) {
    router.push(selectedItem.path);
    selectedKeys.value = [key];
  }
};
</script>

<template>
  <div v-if="fullPageRoutes.includes(route.path)">
    <!-- 全屏页面布局 - 登录和注册页面 -->
    <router-view />
  </div>
  <a-layout v-else id="components-layout-demo-custom-trigger" style="min-height: 100vh;">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible>
      <div class="logo">{{ collapsed ? 'DBM' : 'DB Master' }}</div>
      <a-menu v-model:selectedKeys="selectedKeys" theme="dark" mode="inline" @select="handleMenuSelect">
        <a-menu-item v-for="item in menuItems" :key="item.key">
          <component :is="item.icon" />
          <span>{{ item.label }}</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header style="background: #fff; padding: 0 36px; height: 64px; display: flex; align-items: center; justify-content: space-between;">
        <div style="display: flex; align-items: center;">
          <menu-unfold-outlined
            v-if="collapsed"
            class="trigger"
            @click="() => (collapsed = !collapsed)"
          />
          <menu-fold-outlined v-else class="trigger" @click="() => (collapsed = !collapsed)" />
        </div>
        <UserMenu />
      </a-layout-header>
      <a-layout-content
        :style="{ margin: '24px 16px', padding: '24px', background: '#fff', minHeight: '280px' }"
      >
        <router-view :key="$route.fullPath" />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<style>
#components-layout-demo-custom-trigger .trigger {
  font-size: 18px;
  padding: 0 16px 0 0;
  cursor: pointer;
  transition: color 0.3s;
  display: flex;
  align-items: center;
  height: 100%;
}

#components-layout-demo-custom-trigger .trigger:hover {
  color: #1890ff;
}

#components-layout-demo-custom-trigger .logo {
  height: 50px;
  background: linear-gradient(135deg, #2c3e50 0%, #3498db 100%);
  margin: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 18px;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

#components-layout-demo-custom-trigger .logo:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
}

.site-layout .site-layout-background {
  background: #fff;
}

/* 用户昵称按钮样式 */
.user-nickname-btn {
  display: flex;
  align-items: center;
  border: none;
  background: none;
  padding: 12px 16px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
}

.user-nickname-btn:hover {
  background-color: #f0f2f5;
}

/* 侧边栏菜单样式调整 */
.ant-menu-inline {
  padding: 8px 0;
}

.ant-menu-item {
  margin: 4px 0;
  border-radius: 4px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .ant-layout-content {
    margin: 8px;
    padding: 16px;
  }
}
</style>
