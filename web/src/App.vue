<script setup>
import { ref, watch } from 'vue';
import { Layout } from 'ant-design-vue';
import { useRoute, useRouter } from 'vue-router';
import Sidebar from './components/Sidebar.vue';
import UserMenu from './components/UserMenu.vue';
const { Sider, Content, Header } = Layout;

const route = useRoute();
const router = useRouter();

// 当前选中的菜单项
const selectedKeys = ref(['chatbot']);

// 需要全屏显示的页面
const fullPageRoutes = ['/login', '/register'];

// 监听路由变化，更新选中的菜单项
watch(
  () => route.path,
  (newPath) => {
    const pathMap = {
      '/chatbot': 'chatbot',
      '/database-list': 'database',
      '/personal-settings': 'settings',
      '/operation-records': 'records'
    };
    // 仅在有对应映射时更新selectedKeys
    if (pathMap[newPath]) {
      selectedKeys.value = [pathMap[newPath]];
    } else {
      selectedKeys.value = [];
    }
  },
  { immediate: true }
);

// 监听菜单项变化，更新路由
watch(
  selectedKeys,
  (newKeys) => {
    const keyMap = {
      'chatbot': '/chatbot',
      'database': '/database-list',
      'settings': '/personal-settings',
      'records': '/operation-records'
    };
    const path = keyMap[newKeys[0]];
    if (path && route.path !== path && !fullPageRoutes.includes(route.path)) {
      router.push(path);
    }
  },
  { immediate: true }
);
</script>

<template>
  <div v-if="fullPageRoutes.includes(route.path)">
    <!-- 全屏页面布局 - 登录和注册页面 -->
    <router-view />
  </div>
  <Layout v-else style="min-height: 100vh; display: flex; flex-direction: row; width: 100%;">
    <!-- 左侧导航栏 - 使用拆分后的Sidebar组件 -->
    <Sider width="256" theme="light" style="box-shadow: 2px 0 6px rgba(0, 21, 41, 0.05);">
      <Sidebar v-model:selectedKeys="selectedKeys" />
    </Sider>

    <!-- 主内容区域 -->
    <Layout style="flex: 1; display: flex; flex-direction: column; width: 100%; padding: 0; overflow: hidden;">
      <Header style="padding: 0; background: #fff; border-bottom: 1px solid #e8e8e8;">
        <UserMenu />
      </Header>
      <Content style="margin: 24px; padding: 24px; background: #fff; min-height: 280px; border-radius: 4px; flex: 1; width: 100%; box-sizing: border-box;">
        <router-view />
      </Content>
    </Layout>
  </Layout>
</template>

<style scoped>
template {
  padding: 0%;
}
.chat-container {
  display: flex;
  flex-direction: column;
  height: 500px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.chat-header {
  background-color: #1677ff;
  color: white;
  padding: 16px;
  text-align: center;
}

.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background-color: #f5f5f5;
}

.message-list {
  max-width: 600px;
  margin: 0 auto;
}

.message-item {
  margin-bottom: 16px;
  justify-content: flex-start;
}

.message-bubble {
  display: flex;
  align-items: flex-start;
  max-width: 70%;
}

.sender-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  margin-right: 8px;
}

.user-avatar {
  background-color: #1677ff;
}

.bot-avatar {
  background-color: #8c8c8c;
}

.message-content {
  padding: 8px 16px;
  border-radius: 16px;
  word-break: break-word;
}

.user-message .message-content {
  background-color: #1677ff;
  color: white;
}

.bot-message .message-content {
  background-color: white;
  color: #333;
  border: 1px solid #e8e8e8;
}

.user-message {
  flex-direction: row-reverse;
}

.user-message .sender-avatar {
  margin-right: 0;
  margin-left: 8px;
}

.chat-input-area {
  display: flex;
  padding: 16px;
  background-color: white;
  border-top: 1px solid #e8e8e8;
}

.message-input {
  flex: 1;
  margin-right: 8px;
}
</style>
