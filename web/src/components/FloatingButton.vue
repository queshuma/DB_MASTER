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
    @update:visible="handleUpdateVisible"
    @close="handleDialogClose"
  />
</template>

<script setup>
import { ref } from 'vue';
import SmartBotDialog from './SmartBotDialog.vue';

// 控制对话框显示状态
const dialogVisible = ref(false);

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
</script>

<style scoped>
.floating-button {
  position: fixed;
  right: 30px;
  bottom: 30px;
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