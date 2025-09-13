<template>
  <div v-if="visible" class="dialog-overlay" @click.self="handleClose">
    <div class="dialog-container">
      <div class="dialog-header">
        <h3>智能机器人服务</h3>
        <button class="dialog-close" @click="handleClose">×</button>
      </div>
      <div class="dialog-body">
        <!-- 消息显示区域 -->
        <div ref="messageContainer" class="message-container">
          <div v-for="(msg, index) in messages" :key="index" :class="['message-item', msg.type]">
            <div :class="['message-bubble', msg.type === 'user' ? 'user-message' : 'bot-message']">
              <span :class="['sender-avatar', msg.type === 'user' ? 'user-avatar' : 'bot-avatar']">
                {{ msg.type === 'user' ? '我' : '系统' }}
              </span>
              <div class="message-content">
                <div v-html="md.render(msg.content)"></div>
                <!-- <span v-if="msg.isStreaming" class="streaming-indicator">...</span> -->
              </div>
            </div>
          </div>
        </div>
        
        <!-- 快速操作按钮区域 -->
        <div class="quick-actions">
          <div class="action-buttons">
            <button class="quick-btn" @click="handleQuickAction('queryDetail')">查询详情</button>
            <button class="quick-btn" @click="handleQuickAction('dbConfig')">数据库配置</button>
            <button class="quick-btn" @click="handleQuickAction('modifyConfig')">修改配置</button>
            <button class="quick-btn" @click="handleQuickAction('backup')">备份</button>
            <button class="quick-btn" @click="handleQuickAction('backupRecords')">备份记录</button>
            <button class="quick-btn" @click="handleQuickAction('tableCompare')">数据表对比</button>
            <button class="quick-btn" @click="handleQuickAction('fieldCompare')">数据字段对比</button>
          </div>
        </div>
        
        <!-- 输入区域 -->
        <div class="input-container">
          <textarea 
            v-model="inputValue" 
            @keydown.enter.prevent="handleEnterKey"
            @keydown.ctrl.enter="sendMessage"
            placeholder="请输入你的问题... (Enter换行，Ctrl+Enter发送)" 
            class="message-input"
            rows="3" 
            maxlength="500"
          ></textarea>
          <button @click="sendMessage" class="send-button">发送</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue';
import link, { streamingRequest } from '../link/Link.js'; // 引入API调用工具
import MarkdownIt from 'markdown-it';
import quickActionConfig from './quickActionConfig.json'; // 引入快速操作配置文件

// 创建Markdown实例并配置以更好地处理空行
const md = new MarkdownIt({
  breaks: true,  // 将回车转换为<br>
  linkify: true,
  typographer: true
});

// 定义组件的 props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  initialMessage: {
    type: String,
    default: ''
  },
  defaultInputValue: {
    type: String,
    default: ''
  }
});

// 快速操作按钮点击处理函数
const handleQuickAction = (actionType) => {
  // 根据按钮类型获取相应的问题描述
  const actionDescription = quickActionConfig[actionType];
  
  if (actionDescription) {
    // 构建问题字符串
    const question = `请${actionDescription}`;
    
    // 设置输入值并发送消息
    inputValue.value = question;
    sendMessage();
  }
};

// 定义emit
const emit = defineEmits(['update:visible', 'close']);

// 消息列表
const messages = ref([
  {
    type: 'bot',
    content: '你好！我是智能机器人助手，有什么可以帮助你的吗？',
    timestamp: new Date().toLocaleTimeString()
  }
]);

// 消息容器引用
const messageContainer = ref(null);

// 流式响应控制器
let abortController = null;

// 自动滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
    }
  });
};

// 输入框内容
const inputValue = ref('');

// 处理Enter键事件
const handleEnterKey = (e) => {
  // 如果按下Ctrl键，则发送消息
  if (e.ctrlKey) {
    sendMessage();
  }
  // 否则默认行为就是换行
};

// 发送消息
const sendMessage = async () => {
  if (!inputValue.value.trim()) {
    return;
  }
  
  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: inputValue.value,
    timestamp: new Date().toLocaleTimeString()
  });
  
  // 清空输入框
  inputValue.value = '';
  
  // 滚动到底部
  scrollToBottom();
  
  // 模拟机器人回复
  simulateStreamingResponse();
};

// 流式传输响应（从后端获取实际数据）
const simulateStreamingResponse = () => {
  // 取消之前的请求
  if (abortController) {
    abortController.close();
  }
  
  // 添加一个空的机器人消息作为流式响应的容器
  const botMessageIndex = messages.value.push({
    type: 'bot',
    content: '',
    isStreaming: true,
    timestamp: new Date().toLocaleTimeString()
  }) - 1;
  
  // 显示"正在查询..."的提示
  messages.value[botMessageIndex].content = '正在查询...';
  
  // 滚动到底部
  scrollToBottom();
  
  // 获取最后一条用户消息
  const lastUserMessage = messages.value
    .filter(msg => msg.type === 'user')
    .pop()?.content || '';
  
  // 尝试从用户消息中提取项目ID
  const projectIdMatch = lastUserMessage.match(/查询项目id为(\S+)项目详情/);
  const backupRecordsMatch = lastUserMessage.match(/查询项目id为(\S+)的备份记录/);
  
  let requestUrl = '';
  let requestParams = {};
  
  if (projectIdMatch && projectIdMatch[1]) {
    const projectId = projectIdMatch[1];
    requestUrl = '/api/database/ai/generateStreamAsString';
    requestParams = {
      message: `查询项目id为${projectId}项目详情`
    };
  } else if (backupRecordsMatch && backupRecordsMatch[1]) {
    const projectId = backupRecordsMatch[1];
    requestUrl = '/api/database/ai/generateStreamAsString';
    requestParams = {
      message: `查询项目id为${projectId}的备份记录`
    };
  } else {
    requestUrl = '/api/database/ai/generateStreamAsString';
    requestParams = {
      message: lastUserMessage
    };
  }
    
  // 使用streamingRequest发起请求
  abortController = streamingRequest(requestUrl, requestParams)
    .onMessage(data => {
      if (data === '[complete]') {
        messages.value[botMessageIndex].isStreaming = false;
        scrollToBottom();
        return;
      }
      // 清空正在查询的提示
      if (messages.value[botMessageIndex].content === '正在查询...') {
        messages.value[botMessageIndex].content = '';
      }
      messages.value[botMessageIndex].content += data;
      scrollToBottom();
    })
    .onComplete(() => {
      abortController = null;
    })
    .onError(error => {
      console.error('流式传输错误:', error);
      messages.value[botMessageIndex].content = error.message?.includes('aborted') ? '消息已取消' : '连接异常，请重试';
      messages.value[botMessageIndex].isStreaming = false;
      scrollToBottom();
      abortController = null;
    });
};

// 流式展示默认响应 - 备用逻辑，当前主要使用streamingRequest方式
const streamDefaultResponse = (messageIndex) => {
  const defaultContent = '感谢你的提问！这是一个智能机器人服务原型。\n\n你可以尝试发送类似"查询项目id为[项目ID]项目详情"或"查询项目id为[项目ID]的备份记录"的指令来获取相关信息。';
  
  let currentIndex = 0;
  
  const defaultInterval = setInterval(() => {
    if (currentIndex < defaultContent.length) {
      messages.value[messageIndex].content += defaultContent[currentIndex];
      currentIndex++;
    } else {
      clearInterval(defaultInterval);
    }
  }, 20);
};

// 流式展示项目列表
const showProjectList = (messageIndex, projects) => {
  let projectIndex = 0;
  let charIndex = 0;
  let currentProjectContent = '';
  
  const listInterval = setInterval(() => {
    // 如果当前项目内容还没开始构造，先构造下一个项目的内容
    if (currentProjectContent === '' && projectIndex < projects.length) {
      const project = projects[projectIndex];
      currentProjectContent = `项目${projectIndex + 1}：\n` +
                             `- ID: ${project.id}\n` +
                             `- 名称: ${project.name}\n` +
                             `- 类型: ${project.type}\n` +
                             `- 描述: ${project.description}\n\n`;
      projectIndex++;
    }
    
    // 逐个字符显示当前项目内容
    if (charIndex < currentProjectContent.length) {
      messages.value[messageIndex].content += currentProjectContent[charIndex];
      charIndex++;
    } else {
      // 当前项目显示完毕，重置字符索引
      charIndex = 0;
      currentProjectContent = '';
      
      // 如果所有项目都显示完毕，结束流式响应
      if (projectIndex >= projects.length) {
        clearInterval(listInterval);
        messages.value[messageIndex].content += '\n查询完成。';
      }
    }
  }, 20);
};

// 监听visible变化，当对话框打开时设置默认输入值并处理初始消息
watch(() => [props.visible, props.initialMessage, props.defaultInputValue], ([visible, initialMessage, defaultInputValue]) => {
  if (visible) {
    // 设置默认输入值
    if (defaultInputValue.trim()) {
      inputValue.value = defaultInputValue;
    }
    
    // 如果有初始消息则自动发送
    if (initialMessage.trim()) {
      // 添加初始消息
      messages.value.push({
        type: 'user',
        content: initialMessage,
        timestamp: new Date().toLocaleTimeString()
      });
      
      // 执行流式响应
      simulateStreamingResponse();
    }
  }
}, { immediate: true });

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
  right: 20px; /* 与悬浮球右侧对齐 */
  bottom: calc(10vh / 3 + 70px); /* 悬浮球底部位置 + 按钮高度 + 间距10px = 悬浮球上方 */
  width: 600px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  border: 1px solid #ccc;
}

.dialog-header {
  width: 100%;
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
  background-color: #fff;
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
  background-color: #f5f5f5;
  position: relative;
  min-height: 0;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  max-width: 100%;
  align-items: flex-start;
}

.message-item.user {
  justify-content: flex-end;
}

.message-item.bot {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 18px;
  display: flex;
  align-items: flex-start;
  position: relative;
}

.user-message {
  background-color: #e6f7ff;
  margin-left: auto;
  border-bottom-right-radius: 5px;
}

.bot-message {
  background-color: #fff;
  border: 1px solid #e8e8e8;
  border-bottom-left-radius: 5px;
}

.sender-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #fff;
  flex-shrink: 0;
  position: absolute;
  top: -12px;
}

.user-avatar {
  background-color: #1890ff;
  right: -12px;
}

.bot-avatar {
  background-color: #8c8c8c;
  left: -12px;
}

.user-avatar {
  background-color: #1890ff;
}

.bot-avatar {
  background-color: #8c8c8c;
}

.message-content {
  white-space: pre-wrap;
  word-break: break-word;
  text-align: left;
  flex: 1;
}

/* Markdown样式支持 */
.message-content :deep(pre) {
  white-space: pre-wrap;
  background: #f8f8f8;
  padding: 10px;
  border-radius: 4px;
}

.message-content :deep(code) {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.message-content :deep(p) {
  margin: 8px 0;  /* 增加段落间距以改善空行显示 */
  line-height: 1.0;  /* 增加行高以提高可读性 */
}

.streaming-indicator {
  animation: dot-flash 1.4s infinite both;
}

@keyframes dot-flash {
  0%, 20%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}

.quick-actions {
    padding: 12px 16px;
    background-color: #fafafa;
    border-top: 1px solid #e8e8e8;
    border-bottom: 1px solid #e8e8e8;
  }
  
  .action-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .quick-btn {
    padding: 6px 12px;
    background-color: #fff;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    cursor: pointer;
    font-size: 12px;
    min-width: 70px;
    text-align: center;
  }
  
  .quick-btn:hover {
    border-color: #1890ff;
    color: #1890ff;
  }
  
  .input-container {
    display: flex;
    padding: 16px;
    background-color: #fff;
    gap: 8px;
  }

.message-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  line-height: 1.5;
  min-height: 75px; /* 大约3行的高度 */
  max-height: 125px; /* 大约5行的高度 */
  overflow-y: auto;
  font-family: inherit;
  resize: vertical;
}

/* 为textarea添加特殊样式 */
.message-input:is(textarea) {
  resize: vertical;
  max-height: 125px;
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