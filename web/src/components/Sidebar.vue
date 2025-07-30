<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import { Menu } from 'ant-design-vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 定义props接收选中状态
const props = defineProps({ selectedKeys: { type: Array, required: true } });

// 定义事件用于通知父组件选中状态变化
const emit = defineEmits(['update:selectedKeys']);

// 导航菜单数据
const menuItems = [
  { key: 'database', label: '数据库列表' },
  { key: 'settings', label: '个人设置' },
  { key: 'chatbot', label: '聊天机器人' },
  { key: 'records', label: '操作记录' }
];

// 处理菜单选择变化
const handleMenuSelect = ({ key }) => {
  // 直接路由跳转
  const pathMap = {
    database: '/database-list',
    settings: '/personal-settings',
    chatbot: '/chatbot',
    records: '/operation-records'
  };
  router.push(pathMap[key]);
  emit('update:selectedKeys', [key]);
};
</script>

<template>
  <div style="height: 100%; display: flex; flex-direction: column;">
    <div style="height: 64px; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid #f0f0f0;">
      <h1 style="margin: 0; font-size: 18px;">系统导航</h1>
    </div>
    <Menu
      mode="inline"
      :items="menuItems"
      :selectedKeys="selectedKeys"
      @select="handleMenuSelect"
      style="height: calc(100% - 64px); border-right: 0;">
    </Menu>
  </div>
</template>