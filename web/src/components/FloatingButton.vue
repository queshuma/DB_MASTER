<template>
  <div 
    class="floating-button"
    @click="handleClick"
  >
    <div class="button-content">
      <div class="button-icon">R</div>
      <div class="button-text">机器人</div>
    </div>
  </div>
  
  <!-- 智能机器人对话框组件 -->
  <SmartBotDialog
    :visible="dialogVisible"
    :initial-message="initialMessage"
    :default-input-value="defaultInputValue"
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

// 默认输入值
const defaultInputValue = ref('');

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

// 从外部打开对话框并设置初始消息和默认输入值
const openDialogWithMessage = (message = '', defaultInput = '') => {
  initialMessage.value = message;
  defaultInputValue.value = defaultInput;
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
  right: 20px;
  bottom: calc(10vh / 3); /* 位于页面的2/3位置（从顶部算起） */
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  transition: all 0.3s ease;
  z-index: 1000;
  overflow: hidden;
}

.button-content {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  position: relative;
  transition: all 0.3s ease;
}

.button-icon {
  font-size: 24px;
  font-weight: bold;
  color: #1a73e8;
  transition: transform 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 60px;
  height: 60px;
}

.button-text {
  font-size: 14px;
  font-weight: bold;
  color: #1a73e8;
  opacity: 0;
  transform: translateX(20px);
  transition: all 0.3s ease;
  white-space: nowrap;
  margin-left: 0;
  width: 0;
  overflow: hidden;
}

.floating-button:hover {
  width: 120px;
  border-radius: 30px;
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.floating-button:hover .button-content {
  justify-content: flex-start;
  padding-left: 20px;
}

.floating-button:hover .button-icon {
  transform: translateX(0);
  min-width: auto;
  width: auto;
  height: auto;
}

.floating-button:hover .button-text {
  opacity: 1;
  transform: translateX(0);
  margin-left: 10px;
  width: auto;
}

.floating-button:active {
  transform: scale(0.95);
}
</style>