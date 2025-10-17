<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue';
import MarkdownIt from 'markdown-it';
import { streamingRequest } from '../link/Link';
import link from '../link/Link';
import { MessageOutlined, BookOutlined } from '@ant-design/icons-vue';
import { Tabs } from 'ant-design-vue';

// 创建Markdown实例并配置以更好地处理空行
const md = new MarkdownIt({
  breaks: true,  // 将回车转换为<br>
  linkify: true,
  typographer: true
});

const defaultSystemPrompt = `
我是一个知识库AI助手！我可以帮你：
✅知识库管理：创建和查询知识库
✅知识问答：回答关于知识库内容的问题
✅知识检索：检索和查找相关知识
✅知识推荐：根据需求推荐相关知识
✅知识更新：更新和维护知识库内容
专门处理知识库相关的管理和分析工作！(๑•̀ㅂ•́)و✧
`;

// 消息列表数据
const messages = ref([
  { id: 1, content: defaultSystemPrompt, sender: 'bot', isUser: false }
]);
// 用户输入内容
const inputText = ref('');
// 发送状态
const isSending = ref(false);
// 消息容器引用
const messageContainer = ref(null);
// 流式响应控制器
let abortController = null;

// 知识库列表数据（模拟数据）
const knowledgeBases = ref([
  { id: 1, name: '项目文档库', description: '项目相关的文档和说明' },
  { id: 2, name: '技术知识库', description: '技术相关的知识和解决方案' },
  { id: 3, name: '用户手册', description: '系统使用指南和常见问题' }
]);

// 当前选中的知识库
const selectedKnowledgeBase = ref(null);

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
    console.log('发送流式请求到: /api/knowledge/ai/generateStreamAsString');
    
    // 构建请求参数，包含知识库ID（如果有）
    const requestParams = `?message=${encodeURIComponent(userInput)}${selectedKnowledgeBase.value ? `&knowledgeBaseId=${selectedKnowledgeBase.value.id}` : ''}`;
    
    const botMessage = messages.value.find(m => m.id === botMsgId);
    streamingRequest(`/api/knowledge/ai/generateStreamAsString${requestParams}`)
      .onMessage(data => {
        if (data === '[complete]') {
          botMessage.isStreaming = false;
          eventSource.close();
          return;
        }
        botMessage.content += data;
        scrollToBottom();
      })
      .onComplete(() => {
        isSending.value = false;
        abortController = null;
      })
      .onError(error => {
        console.error('流式传输错误:', error);
        botMessage.content = error.message.includes('aborted') ? '消息已取消' : '连接异常，请重试';
        botMessage.isStreaming = false;
        isSending.value = false;
        abortController = null;
      });

    abortController = {
      abort: () => {
        streamingRequest.abort();
        console.log('流式请求已中止');
      },
      signal: { aborted: false }
    };
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

// 切换知识库
const selectKnowledgeBase = (kb) => {
  selectedKnowledgeBase.value = kb;
  // 清空消息记录，重新初始化机器人消息
  messages.value = [
    {
      id: 1,
      content: `你好！我是${kb.name}的专属助手。\n${kb.description}\n请告诉我你想了解什么？`,
      sender: 'bot',
      isUser: false
    }
  ];
};

onMounted(scrollToBottom);

onUnmounted(() => {
  if (abortController) {
    abortController.abort();
  }
});
</script>

<template>
  <div class="knowledge-bot-container">
    <div class="knowledge-bot-header">
      <h2><BookOutlined /> 知识库机器人</h2>
    </div>
    
    <div class="knowledge-bot-content">
      <!-- 左侧知识库列表 -->
      <div class="knowledge-base-sidebar">
        <div class="sidebar-header">
          <h3>知识库列表</h3>
        </div>
        <div class="knowledge-base-list">
          <div 
            v-for="kb in knowledgeBases" 
            :key="kb.id"
            :class="['knowledge-base-item', { active: selectedKnowledgeBase?.id === kb.id }]"
            @click="selectKnowledgeBase(kb)"
          >
            <h4>{{ kb.name }}</h4>
            <p>{{ kb.description }}</p>
          </div>
        </div>
      </div>
      
      <!-- 右侧聊天区域 -->
      <div class="chat-area">
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
                </div>
              </div>
            </a-list-item>
          </a-list>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input-area">
          <a-input
            v-model:value="inputText"
            placeholder="请输入问题..."
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
    </div>
  </div>
</template>

<style scoped>
.knowledge-bot-container {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #ccc;
}

.knowledge-bot-header {
  width: 100%;
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  gap: 8px;
}

.knowledge-bot-content {
  flex: 1;
  display: flex;
  min-height: 0;
}

.knowledge-base-sidebar {
  width: 280px;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  background-color: #fafafa;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
  background-color: #fff;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
}

.knowledge-base-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.knowledge-base-item {
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fff;
  border: 1px solid transparent;
}

.knowledge-base-item:hover {
  background-color: #f0f0f0;
}

.knowledge-base-item.active {
  background-color: #e6f7ff;
  border-color: #91d5ff;
}

.knowledge-base-item h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 500;
}

.knowledge-base-item p {
  margin: 0;
  font-size: 12px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
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
  background-color: #fff;
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
  margin: 8px 0;
  line-height: 1.6;
}

.message-input {
  flex: 1;
}

.cancel-button {
  margin-left: 8px;
}
</style>