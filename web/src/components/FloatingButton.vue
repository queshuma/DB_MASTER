<template>
  <div 
    class="floating-button"
    @click="handleClick"
  >
    R
  </div>
  
  <!-- 智能机器人对话框组件 -->
  <SmartBotDialog
    :visible="dialogVisible"
    :initial-message="initialMessage"
    @update:visible="handleUpdateVisible"
    @close="handleDialogClose"
  />
</template>

<script setup>
import { ref, defineExpose } from 'vue';
import SmartBotDialog from './SmartBotDialog.vue';

// 控制对话框显示状态
const dialogVisible = ref(false);

// 外部传入的初始消息
const initialMessage = ref('');

// 内部点击处理
const handleClick = () => {
  dialogVisible.value = true;
};

// 处理对话框关闭
const handleDialogClose = () => {
  dialogVisible.value = false;
};

// 处理visible属性更新
const handleUpdateVisible = (newValue) => {
  dialogVisible.value = newValue;
};

// 从外部打开对话框并设置初始消息
const openDialogWithMessage = (message) => {
  initialMessage.value = message;
  dialogVisible.value = true;
};

// 暴露方法给父组件
defineExpose({
  openDialogWithMessage
});
</script>

<style scoped>
.floating-button {
  position: fixed;
  right: 30px;
  bottom: calc(100vh / 3); /* 位于页面的2/3位置（从顶部算起） */
  width: 60px;
  height: 60px;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1a73e8;
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  font-weight: bold;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  transition: all 0.3s ease;
  z-index: 1000;
}

.floating-button:hover {
  transform: scale(1.1);
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.floating-button:active {
  transform: scale(0.95);
}
</style>