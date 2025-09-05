<template>
  <div v-if="visible" class="dialog-overlay" @click.self="handleClose">
    <div class="dialog-container">
      <div class="dialog-header">
        <h3>智能机器人服务</h3>
        <button class="dialog-close" @click="handleClose">×</button>
      </div>
      <div class="dialog-body">
        <!-- 消息显示区域 -->
        <div class="message-container">
          <div v-for="(msg, index) in messages" :key="index" :class="['message-item', msg.type]">
            <div class="message-avatar">{{ msg.type === 'bot' ? '🤖' : '👤' }}</div>
            <div class="message-bubble">{{ msg.content }}</div>
          </div>
        </div>
        <!-- 输入区域 -->
        <div class="input-container">
          <input 
            v-model="inputValue" 
            @keyup.enter="sendMessage"
            placeholder="请输入你的问题..." 
            class="message-input"
          >
          <button @click="sendMessage" class="send-button">发送</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

// 定义组件的 props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});

// 定义emit
const emit = defineEmits(['update:visible', 'close']);

// 消息列表
const messages = ref([
  {
    type: 'bot',
    content: '你好！我是智能机器人助手，有什么可以帮助你的吗？'
  }
]);

// 输入框内容
const inputValue = ref('');

// 发送消息
const sendMessage = () => {
  if (!inputValue.value.trim()) {
    return;
  }
  
  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: inputValue.value
  });
  
  // 清空输入框
  inputValue.value = '';
  
  // 模拟机器人回复
  setTimeout(() => {
    messages.value.push({
      type: 'bot',
      content: '感谢你的提问！这是一个智能机器人服务原型，目前正在开发中。'
    });
  }, 500);
};

// 处理对话框关闭
const handleClose = () => {
  emit('update:visible', false);
  emit('close');
};
</script>

<style scoped>
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1001;
}

.dialog-container {
  position: fixed;
  right: calc(30px + 60px + 10px + 400px); /* 悬浮按钮右侧距离 + 按钮宽度 + 间距 + 对话框宽度 */
  bottom: 30px; /* 与悬浮按钮底部对齐 */
  width: 400px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.dialog-header {
  padding: 16px 20px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.dialog-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #6c757d;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-body {
  height: 400px;
  display: flex;
  flex-direction: column;
}

.message-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #f9f9f9;
}

.message-item {
  display: flex;
  margin-bottom: 12px;
  align-items: flex-start;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  font-size: 24px;
  margin: 0 8px;
  flex-shrink: 0;
}

.message-bubble {
  max-width: 70%;
  padding: 8px 12px;
  border-radius: 8px;
  word-wrap: break-word;
}

.message-item.bot .message-bubble {
  background-color: #fff;
  border: 1px solid #d9d9d9;
}

.message-item.user .message-bubble {
  background-color: #1890ff;
  color: #fff;
}

.input-container {
  display: flex;
  padding: 12px 16px;
  background-color: #fff;
  border-top: 1px solid #f0f0f0;
  gap: 8px;
}

.message-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
}

.message-input:focus {
  border-color: #1890ff;
}

.send-button {
  padding: 8px 16px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.send-button:hover {
  background-color: #40a9ff;
}
</style>