<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue';
import MarkdownIt from 'markdown-it';
import { streamingRequest } from '../link/Link';
import link from '../link/Link';
import { MessageOutlined, SendOutlined, StopOutlined, RefreshOutlined } from '@ant-design/icons-vue';
import { Card, Input, Button, List, Avatar, Spin, Empty, Tooltip } from 'ant-design-vue';
import { message } from 'ant-design-vue';

// 创建Markdown实例并配置以更好地处理空行
const md = new MarkdownIt({
  breaks: true,  // 将回车转换为<br>
  linkify: true,
  typographer: true
});

// 默认系统提示词
const defaultSystemPrompt = `
我是一个智能问答机器人！我可以帮你：
✅回答问题：解答各种技术和业务问题
✅提供建议：基于专业知识给出建议
✅辅助决策：提供数据支持和分析
✅信息检索：帮助查找和整理信息
✅学习助手：解释复杂概念和原理
请随时向我提问，我会尽力为你提供帮助！(๑•̀ㅂ•́)و✧
`;

// 消息列表数据
const messages = ref([
  { id: 1, content: defaultSystemPrompt, sender: 'bot', isUser: false, timestamp: new Date().toLocaleTimeString() }
]);
// 用户输入内容
const inputText = ref('');
// 发送状态
const isSending = ref(false);
// 消息容器引用
const messageContainer = ref(null);
// 流式响应控制器
let abortController = null;
// 错误状态
const error = ref(null);
// 历史对话列表（模拟）
const conversationHistory = ref([
  { id: 1, title: '数据库优化问题', date: '今天 14:30' },
  { id: 2, title: '系统架构设计讨论', date: '昨天 09:15' },
  { id: 3, title: 'API接口调用方式', date: '2023-11-15' }
]);

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
  if (!inputText.value.trim()) {
    message.warning('请输入问题内容');
    return;
  }

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
  error.value = null;
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
    console.log('发送流式请求到: /api/qa/ai/generateStreamAsString');
    
    const botMessage = messages.value.find(m => m.id === botMsgId);
    streamingRequest(`/api/qa/ai/generateStreamAsString?message=${encodeURIComponent(userInput)}`)
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
  } catch (err) {
    if (err.name !== 'AbortError') {
      error.value = err;
      const botMessage = messages.value.find(m => m.id === botMsgId);
      if (botMessage) {
        botMessage.content = `网络请求失败: ${err.message}\n请确保后端服务已启动并正确配置代理`;
        botMessage.isStreaming = false;
      }
      console.error('流式传输错误:', err);
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
    message.info('消息已取消');
  }
};

// 清空对话
const clearConversation = () => {
  messages.value = [
    {
      id: 1,
      content: defaultSystemPrompt,
      sender: 'bot',
      isUser: false,
      timestamp: new Date().toLocaleTimeString()
    }
  ];
  scrollToBottom();
  message.success('对话已清空');
};

// 加载历史对话（模拟）
const loadConversation = (conversation) => {
  messages.value = [
    {
      id: 1,
      content: `正在加载对话：${conversation.title}`,
      sender: 'bot',
      isUser: false,
      timestamp: new Date().toLocaleTimeString()
    }
  ];
  // 模拟加载历史对话内容
  setTimeout(() => {
    messages.value = [
      {
        id: 1,
        content: `你正在查看历史对话：${conversation.title}\n这是一段模拟的对话内容，实际项目中应该从后端加载真实的对话历史。`,
        sender: 'bot',
        isUser: false,
        timestamp: conversation.date
      },
      {
        id: 2,
        content: '你好，我想了解一下这个项目的数据库设计。',
        sender: 'user',
        isUser: true,
        timestamp: conversation.date
      },
      {
        id: 3,
        content: '该项目采用了关系型数据库设计，主要包含用户表、产品表、订单表等核心表结构...',
        sender: 'bot',
        isUser: false,
        timestamp: conversation.date
      }
    ];
    scrollToBottom();
  }, 500);
};

// 创建新对话
const createNewConversation = () => {
  clearConversation();
};

onMounted(scrollToBottom);

onUnmounted(() => {
  if (abortController) {
    abortController.abort();
  }
});
</script>

<template>
  <div class="qa-bot-container">
    <Card title="智能问答机器人" class="card-container">
      <div class="qa-bot-content">
        <!-- 左侧历史对话列表 -->
        <div class="conversation-sidebar">
          <div class="sidebar-header">
            <h3>历史对话</h3>
            <Button 
              type="text" 
              icon={<RefreshOutlined />} 
              @click="createNewConversation"
              size="small"
              title="创建新对话"
            />
          </div>
          <div class="conversation-list">
            <div 
              v-for="conversation in conversationHistory" 
              :key="conversation.id"
              class="conversation-item"
              @click="loadConversation(conversation)"
            >
              <h4>{{ conversation.title }}</h4>
              <p>{{ conversation.date }}</p>
            </div>
          </div>
          <div class="sidebar-footer">
            <Button 
              type="primary" 
              block 
              size="small" 
              @click="createNewConversation"
            >
              新建对话
            </Button>
          </div>
        </div>
        
        <!-- 右侧对话区域 -->
        <div class="chat-area">
          <!-- 错误提示 -->
          <div v-if="error" class="error-message">
            <p>{{ error.message }}</p>
            <Button type="link" @click="error = null">关闭</Button>
          </div>
          
          <!-- 消息列表 -->
          <div class="chat-messages" ref="messageContainer">
            <List
              :data-source="messages"
              :renderItem="(msg) => (
                <List.Item class="message-item">
                  <List.Item.Meta
                    :avatar="
                      <Avatar :style="{ backgroundColor: msg.isUser ? '#1890ff' : '#8c8c8c' }">
                        {{ msg.isUser ? '我' : 'AI' }}
                      </Avatar>
                    "
                    :description="
                      <div class="message-content-wrapper">
                        <div 
                          :class="['message-content', msg.isUser ? 'user-message' : 'bot-message']"
                          v-html="md.render(msg.content)"
                        />
                        <div class="message-time">{{ msg.timestamp }}</div>
                      </div>
                    "
                  />
                </List.Item>
              )"
              class="message-list"
            >
              <template #empty>
                <Empty description="暂无消息" />
              </template>
            </List>
          </div>

          <!-- 输入区域 -->
          <div class="chat-input-area">
            <Input.TextArea
              v-model:value="inputText"
              placeholder="请输入你的问题..."
              :disabled="isSending"
              :rows="3"
              allowClear
              @keyup.enter.ctrl="sendMessage"
              class="message-input"
            />
            <div class="input-actions">
              <Button 
                type="default" 
                @click="clearConversation"
                :disabled="isSending"
                size="small"
                style="margin-right: 8px;"
              >
                清空
              </Button>
              <Button 
                v-if="isSending"
                danger 
                @click="cancelSend"
                icon={<StopOutlined />}
                size="small"
                style="margin-right: 8px;"
              >
                取消
              </Button>
              <Button 
                type="primary" 
                @click="sendMessage"
                :loading="isSending"
                :disabled="!inputText.trim() || isSending"
                icon={<SendOutlined />}
                size="small"
              >
                {{ isSending ? '发送中' : '发送' }}
              </Button>
            </div>
          </div>
        </div>
      </div>
    </Card>
  </div>
</template>

<style scoped>
.qa-bot-container {
  height: 100%;
  width: 100%;
  overflow: auto;
}

.card-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.qa-bot-content {
  flex: 1;
  display: flex;
  min-height: 0;
}

.conversation-sidebar {
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
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.conversation-item {
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fff;
  border: 1px solid transparent;
}

.conversation-item:hover {
  background-color: #f0f0f0;
  border-color: #d9d9d9;
}

.conversation-item h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-item p {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #e8e8e8;
  background-color: #fff;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  background-color: #fff;
}

.error-message {
  background-color: #fff1f0;
  color: #cf1322;
  padding: 12px 16px;
  border-bottom: 1px solid #ffccc7;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.error-message p {
  margin: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #fafafa;
  min-height: 0;
}

.message-list {
  width: 100%;
}

.message-item {
  margin-bottom: 16px;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.message-content {
  padding: 10px 14px;
  border-radius: 8px;
  max-width: 70%;
  white-space: pre-wrap;
  word-break: break-word;
  text-align: left;
}

.user-message {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
  align-self: flex-end;
}

.bot-message {
  background-color: #fff;
  border: 1px solid #d9d9d9;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  align-self: flex-start;
}

.chat-input-area {
  padding: 16px;
  border-top: 1px solid #e8e8e8;
  background-color: #fff;
}

.message-input {
  margin-bottom: 12px;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

/* 自定义滚动条 */
.chat-messages::-webkit-scrollbar,
.conversation-list::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track,
.conversation-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb,
.conversation-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover,
.conversation-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Markdown样式 */
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

/* 响应式调整 */
@media (max-width: 1024px) {
  .conversation-sidebar {
    width: 240px;
  }
}

@media (max-width: 768px) {
  .qa-bot-content {
    flex-direction: column;
  }
  
  .conversation-sidebar {
    width: 100%;
    height: 200px;
    border-right: none;
    border-bottom: 1px solid #e8e8e8;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .input-actions {
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>