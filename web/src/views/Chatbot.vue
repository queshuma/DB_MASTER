<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue';
import MarkdownIt from 'markdown-it';
import { linkStream } from '../link/Link';
import link from '../link/Link';

// 创建Markdown实例并配置以更好地处理空行
const md = new MarkdownIt({
  breaks: true,  // 将回车转换为<br>
  linkify: true,
  typographer: true
});

// 消息列表数据
const messages = ref([
  { id: 1, content: '你好！我是聊天机器人，有什么可以帮助你的吗？', sender: 'bot', isUser: false }
]);
// 用户输入内容
const inputText = ref('');
// 发送状态
const isSending = ref(false);
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

// 发送消息
const sendMessage = async () => {
  if (!inputText.value.trim()) return;

  // 添加用户消息
  const userMsgId = Date.now();
  messages.value.push({
    id: userMsgId,
    content: inputText.value,
    isUser: true,
    timestamp: new Date().toLocaleTimeString()
  });

  // 清空输入框
  const userInput = inputText.value;
  inputText.value = '';
  isSending.value = true;
  scrollToBottom();

  // 添加机器人响应占位符
  const botMsgId = Date.now() + 1;
  messages.value.push({
    id: botMsgId,
    content: '',
    isUser: false,
    isStreaming: true,
    timestamp: new Date().toLocaleTimeString()
  });

  try {
    console.log('发送流式请求到: /api/database/ai/generateStreamAsString');
    
    // 使用linkStream函数发送请求
    const { response, abortController: streamAbortController } = await linkStream(
      '/api/database/ai/generateStreamAsString', 
      'GET', 
      {}, 
      { message: userInput }, 
      { 'accept': 'text/event-stream' },
      true
    );
    
    // 保存abortController引用以便后续取消请求
    abortController = streamAbortController;
    
    // 记录cookie相关信息
    console.log('流式请求成功发送，withCredentials已启用');
    console.log('Response Set-Cookie:', response.headers.get('set-cookie'));

    if (!response.ok) {
      throw new Error(`请求失败: ${response.status} ${response.statusText}`);
    }

    // 流式处理响应 - 打字机效果实现
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    const botMessage = messages.value.find(m => m.id === botMsgId);
    let complete = false;
    let pendingText = '';  // 待处理的文本
    let isTyping = false;  // 是否正在打字中
    const TYPE_SPEED = 20;  // 打字速度(ms/字符)

    // 打字机函数 - 逐字显示文本
    const typeWriter = () => {
      if (pendingText.length === 0) {
        isTyping = false;
        return;
      }

      isTyping = true;
      const char = pendingText.charAt(0);
      pendingText = pendingText.substring(1);

      botMessage.content += char;
      scrollToBottom();

      // 调整不同字符的打字速度，提升真实感
      const delay = char === '\n' || char === '。' || char === '！' || char === '？' ? TYPE_SPEED * 3 : TYPE_SPEED;
      setTimeout(typeWriter, delay);
    };

    while (!complete && !abortController.signal.aborted) {
      const { done, value } = await reader.read();
      if (done) break;

      const chunk = decoder.decode(value, { stream: true });
      if (botMessage) {
        // 处理流式数据
        let messageContent = chunk.replace(/data:/g, '').trim();
        messageContent = messageContent.replace(/white-space:\s*['"]?pre-wrap['"]?;/gi, '').trim();
        
        // 移除可能重复的用户输入前缀
        if (messageContent.startsWith(userInput)) {
          messageContent = messageContent.slice(userInput.length).trim();
        }

        // 处理完成标记
        if (messageContent.includes('[complete]')) {
          complete = true;
          messageContent = messageContent.replace('[complete]', '').trim();
        }

        if (messageContent) {
          // 添加到待处理文本
          pendingText += messageContent;
          // 如果当前没有在打字，则开始打字
          if (!isTyping) {
            typeWriter();
          }
        }
      }
    }

    // 确保所有待处理文本都被显示
    const ensureAllTextDisplayed = () => {
      if (pendingText.length > 0) {
        botMessage.content += pendingText;
        pendingText = '';
        scrollToBottom();
      }
    };

    // 确保所有文本都已显示后再标记为完成
    if (pendingText.length > 0) {
      await new Promise(resolve => {
        const checkPending = () => {
          if (pendingText.length === 0) {
            resolve();
          } else {
            setTimeout(checkPending, 100);
          }
        };
        checkPending();
      });
    }

    if (botMessage) {
      botMessage.isStreaming = false;
    }
  } catch (error) {
    if (error.name !== 'AbortError') {
      const botMessage = messages.value.find(m => m.id === botMsgId);
      if (botMessage) {
        botMessage.content = `网络请求失败: ${error.message}\n请确保后端服务已启动并正确配置代理`;
        botMessage.isStreaming = false;
      }
      console.error('流式传输错误:', error);
    }
  } finally {
    isSending.value = false;
    abortController = null;
  }
};

// 取消发送
const cancelSend = () => {
  if (abortController) {
    abortController.abort();
    const botMessage = messages.value.find(m => m.isStreaming);
    if (botMessage) {
      botMessage.content = '消息已取消';
      botMessage.isStreaming = false;
    }
    isSending.value = false;
  }
};

onMounted(scrollToBottom);

onUnmounted(() => {
  if (abortController) {
    abortController.abort();
  }
});
</script>

<template>
  <div class="chat-container">
    <div class="chat-header"><h2>DB Master 数据处理大师</h2></div>
    
    <!-- 流式消息窗口 -->
    <div class="chat-messages" ref="messageContainer">
      <a-list class="message-list">
        <a-list-item v-for="msg in messages" :key="msg.id" class="message-item">
          <div :class="['message-bubble', msg.isUser ? 'user-message' : 'bot-message']">
            <span :class="['sender-avatar', msg.isUser ? 'user-avatar' : 'bot-avatar']">
              {{ msg.isUser ? '我' : '系统' }}
            </span>
            <div class="message-content">
              <div v-html="md.render(msg.content)"></div>
              <span v-if="msg.isStreaming" class="loading-dot">...</span>
            </div>
          </div>
        </a-list-item>
      </a-list>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-area">
      <a-input
        v-model:value="inputText"
        placeholder="请输入消息..."
        @keyup.enter="sendMessage"
        class="message-input"
        :disabled="isSending"
        :max-length="500"
      />
      <a-button 
        type="primary"
        @click="sendMessage"
        :loading="isSending"
        :disabled="!inputText.trim() || isSending"
        class="send-button"
      >
        {{ isSending ? '发送中' : '发送' }}
      </a-button>
      <a-button 
        v-if="isSending"
        @click="cancelSend"
        class="cancel-button"
      >
        取消
      </a-button>
    </div>
  </div>
</template>

<style scoped>
/* 合并重复样式定义 */
.chat-container {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #ccc;
}

.chat-header {
  width: 100%;
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #f5f5f5;
  position: relative;
  min-height: 0;
}

.chat-input-area {
  padding: 16px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  gap: 8px;
}

.message-list {
  width: 100%;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  max-width: 100%;
}

.user-message {
  justify-content: flex-end;
}

.bot-message {
  justify-content: flex-start;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 18px;
  max-width: 70%;
}

.user-message .message-bubble {
  background-color: #e6f7ff;
}

.bot-message .message-bubble {
  background-color: #fff;
  border: 1px solid #e8e8e8;
}

.sender-avatar {
  margin-right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #fff;
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
}

.message-content >>> pre {
  white-space: pre-wrap;
  background: #f8f8f8;
  padding: 10px;
  border-radius: 4px;
}

.message-content >>> code {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.message-content >>> p {
  margin: 8px 0;  /* 增加段落间距以改善空行显示 */
  line-height: 1.0;  /* 增加行高以提高可读性 */
}

.message-input {
  flex: 1;
}

.loading-dot::after {
  content: '';
  animation: dot-flash 1.4s infinite both;
}

@keyframes dot-flash {
  0%, 20%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}

.cancel-button {
  margin-left: 8px;
}
</style>

