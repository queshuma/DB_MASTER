<script setup>
import { onMounted, ref, computed, nextTick } from 'vue';

import { useRoute } from 'vue-router';
import { message } from 'ant-design-vue';
import link from '../link/Link';

const route = useRoute();
const fileName = ref('');
const sourceText = ref(''); // 左边框的文本
const tokenizedItems = ref([]); // 右边框的分词项
const markedHtml = ref(''); // 带标记的HTML内容
const sourceTextBox = ref(null); // 左侧文本框的引用

// 组件挂载时获取传递的参数
onMounted(async () => {
  try {
    console.log('开始初始化手动分词页面...');
    
    // 获取路由参数中的文件内容
    const fileContent = route.query.fileContent;
    console.log('路由参数中的fileContent:', fileContent ? '存在' : '不存在');
    
    // 如果有文件内容，则使用传递过来的内容
    if (fileContent) {
      try {
        // 解码并设置文件内容到源文本框
        const decodedContent = decodeURIComponent(fileContent);
        console.log('解码后的文件内容长度:', decodedContent.length);
        
        // 先更新响应式数据
        sourceText.value = decodedContent;
        markedHtml.value = decodedContent;
        console.log('响应式数据已更新');
        
        // 使用nextTick确保DOM更新后，将内容同步到contenteditable元素
        await nextTick();
        
        // 添加额外的检查确保sourceTextBox已正确绑定
        if (!sourceTextBox.value) {
          console.error('源文本框元素未找到，请检查ref绑定');
          // 尝试使用querySelector作为备选方案
          const textbox = document.querySelector('.text-box[contenteditable="true"]');
          if (textbox) {
            console.log('通过querySelector找到源文本框');
            // 清空并设置内容
            textbox.textContent = '';
            textbox.textContent = decodedContent;
            textbox.innerHTML = decodedContent;
            console.log('文件内容已通过querySelector设置到源文本框');
          }
        } else {
          // 先清空现有内容
          sourceTextBox.value.textContent = '';
          // 设置内容
          sourceTextBox.value.textContent = decodedContent;
          // 同时更新innerHTML以确保v-html绑定正确
          sourceTextBox.value.innerHTML = decodedContent;
          console.log('文件内容已成功加载到源文本框');
        }
        
        // 强制触发一次更新
        await nextTick();
        // 再次确认内容已设置
        if (sourceTextBox.value) {
          console.log(`内容确认: textContent长度=${sourceTextBox.value.textContent.length}`);
        }
      } catch (error) {
        console.error('解码文件内容失败:', error);
        message.error('文件内容解析失败');
        // 使用默认文本
        setDefaultContent();
      }
    } else {
      // 然后检查是否有文件名参数
      const fileNameParam = route.query.fileName;
      console.log('路由参数中的fileName:', fileNameParam);
      if (fileNameParam) {
        fileName.value = fileNameParam;
        // 调用接口获取文件内容
        console.log(`准备调用fetchFileContent获取文件"${fileNameParam}"的内容`);
        await fetchFileContent(fileNameParam);
      } else {
        console.log('没有文件内容和文件名参数，使用默认文本');
        // 如果都没有，则使用默认文本
        setDefaultContent();
      }
    }
  } catch (error) {
    console.error('组件挂载时出错:', error);
    message.error('页面初始化失败');
    setDefaultContent();
  }
});

// 设置默认文本内容
const setDefaultContent = () => {
  try {
    const defaultText = '这是一段示例文本，用于展示手动分词功能。请选择文本并点击右侧的>>按钮将其添加到分词结果中。';
    
    // 先更新响应式数据
    sourceText.value = defaultText;
    markedHtml.value = defaultText;
    
    // 使用nextTick确保DOM更新后，将默认内容同步到contenteditable元素
    nextTick(() => {
      if (sourceTextBox.value) {
        // 先清空现有内容
        sourceTextBox.value.textContent = '';
        // 设置内容
        sourceTextBox.value.textContent = defaultText;
        // 同时更新innerHTML以确保v-html绑定正确
        sourceTextBox.value.innerHTML = defaultText;
        console.log('默认文本已设置到源文本框');
      }
    });
  } catch (error) {
    console.error('设置默认文本失败:', error);
  }
};

// 获取文件内容的函数
const fetchFileContent = async (fileName) => {
  try {
    console.log(`尝试获取文件"${fileName}"的内容`);
    
    // 调用实际的文件内容获取接口，使用GET请求并通过params传递fileName
    const response = await link('/file/getRagFileContent', 'get', {}, { fileName: fileName }, {}, true);
    
    // 获取文件内容
    const fileContent = response || '';
    console.log(`获取到的文件内容:`, response);
    
    // 先更新响应式数据
    sourceText.value = fileContent;
    markedHtml.value = fileContent;
    console.log(`响应式数据已更新`);
    
    // 使用nextTick确保DOM更新后，将内容同步到contenteditable元素
    await nextTick();
    
    // 添加额外的检查确保sourceTextBox已正确绑定
    if (!sourceTextBox.value) {
      console.error('源文本框元素未找到，请检查ref绑定');
      // 尝试使用querySelector作为备选方案
      const textbox = document.querySelector('.text-box[contenteditable="true"]');
      if (textbox) {
        console.log('通过querySelector找到源文本框');
        // 清空并设置内容
        textbox.textContent = '';
        textbox.textContent = fileContent;
        textbox.innerHTML = fileContent;
        console.log(`文件"${fileName}"内容已通过querySelector设置到源文本框`);
      }
      return;
    }
    
    // 先清空现有内容
    sourceTextBox.value.textContent = '';
    // 设置内容
    sourceTextBox.value.textContent = fileContent;
    // 同时更新innerHTML以确保v-html绑定正确
    sourceTextBox.value.innerHTML = fileContent;
    console.log(`文件"${fileName}"内容已成功加载到源文本框`);
    
    // 强制触发一次更新
    await nextTick();
    // 再次确认内容已设置
    if (sourceTextBox.value) {
      console.log(`内容确认: textContent长度=${sourceTextBox.value.textContent.length}`);
    }
  } catch (error) {
    console.error('获取文件内容失败:', error);
    message.error('获取文件内容失败，请稍后重试');
    // 使用默认文本
    setDefaultContent();
  }
};

// fetchFileContent函数已经在前面定义，这里移除重复定义

// 处理转移选中的文本
const handleTransferText = () => {
  // 获取选中的文本和范围
  const selection = window.getSelection();
  const selectedText = selection.toString().trim();
  
  if (selectedText) {
    // 生成随机背景色
    const bgColor = getRandomColor();
    
    // 添加到分词项列表
    tokenizedItems.value.push({
      text: selectedText,
      color: bgColor
    });
    
    // 为选中的文本添加边框标记
    try {
      if (selection.rangeCount > 0) {
        const range = selection.getRangeAt(0);
        const span = document.createElement('span');
        span.className = 'tokenized-span';
        span.style.border = '2px solid #0078d7';
        span.style.padding = '1px 2px';
        span.style.borderRadius = '2px';
        span.style.backgroundColor = 'rgba(0, 120, 215, 0.1)';
        span.textContent = selectedText;
        
        // 替换选中的内容
        range.deleteContents();
        range.insertNode(span);
        
        // 更新markedHtml
        if (sourceTextBox.value) {
          markedHtml.value = sourceTextBox.value.innerHTML;
        }
        
        // 清除选中状态
        selection.removeAllRanges();
      }
    } catch (error) {
      console.error('标记选中内容失败:', error);
    }
  } else {
    message.warning('请先选择要转移的文本');
  }
};

// 处理左侧文本框内容变化
const handleTextChange = () => {
  if (sourceTextBox.value) {
    // 获取纯文本内容
    const plainText = sourceTextBox.value.textContent;
    sourceText.value = plainText;
    // 保存HTML内容（包括标记）
    markedHtml.value = sourceTextBox.value.innerHTML;
  }
};

// 生成随机颜色
const getRandomColor = () => {
  const colors = [
    '#ffcccc', // 浅红
    '#ccffcc', // 浅绿
    '#ccccff', // 浅蓝
    '#ffffcc', // 浅黄
    '#ccffff', // 浅青
    '#ffccff', // 浅紫
    '#ffcc99', // 浅橙
    '#ccff99', // 浅薄荷绿
    '#99ccff', // 浅天蓝
    '#ff99cc'  // 浅粉
  ];
  return colors[Math.floor(Math.random() * colors.length)];
};

// 清空所有分词项
const clearAllTokens = () => {
  tokenizedItems.value = [];
  
  // 清除左侧文本框中的所有标记
  if (sourceTextBox.value && sourceText.value) {
    // 重置为纯文本内容
    sourceTextBox.value.textContent = sourceText.value;
    markedHtml.value = sourceText.value;
  }
};

// 删除单个分词项
const removeToken = (index) => {
  // 获取要删除的分词文本
  const tokenToRemove = tokenizedItems.value[index].text;
  
  // 从分词项列表中移除
  tokenizedItems.value.splice(index, 1);
  
  // 清除左侧文本框中对应的选中标记
  if (sourceTextBox.value && sourceText.value) {
    try {
      // 查找所有已标记的元素
      const markedElements = sourceTextBox.value.querySelectorAll('.tokenized-span');
      
      // 遍历查找包含相同文本的元素
      markedElements.forEach(element => {
        if (element.textContent === tokenToRemove) {
          // 创建一个新的文本节点替换标记元素
          const textNode = document.createTextNode(element.textContent);
          element.parentNode.insertBefore(textNode, element);
          element.parentNode.removeChild(element);
        }
      });
      
      // 更新markedHtml
      markedHtml.value = sourceTextBox.value.innerHTML;
    } catch (error) {
      console.error('清除标记失败:', error);
    }
  }
};

// 自动分词功能
const handleAutoTokenize = async () => {
  try {
    // 获取源文本内容
    const text = sourceText.value.trim();
    if (!text) {
      message.warning('请先输入或加载文本内容');
      return;
    }
    
    // 清空现有的分词结果
    tokenizedItems.value = [];
    
    // 使用link方法发送POST请求到自动分词API
    const response = await link('/file/autoParticiple', 'post', {
      context: text
    });
    
    // 检查响应是否包含blocks数组
    if (response && Array.isArray(response)) {
      const blocks = response;
      console.log('自动分词返回数据:', blocks);

      // 预定义的颜色数组
      const colors = ['#e6f7ff', '#f6ffed', '#fff7e6', '#f9f0ff', '#fff1f0'];
      
      // 将返回的Block列表转换为分词结果格式
      blocks.forEach((block, index) => {
        // 循环使用预定义颜色
        const colorIndex = index % colors.length;
        
        tokenizedItems.value.push({
          text: block.content || '',
          color: colors[colorIndex]
        });
      });
      
      // 为源文本中的每个分词内容添加边框样式
      updateSourceTextWithHighlightedTokens(blocks, colors);
      
      message.success('自动分词完成');
    } else {
      throw new Error('返回数据格式不正确');
    }
  } catch (error) {
    console.error('自动分词失败:', error);
    message.error('自动分词失败，请稍后重试');
  }
};

// 更新源文本，为分词内容添加边框样式
const updateSourceTextWithHighlightedTokens = (blocks, colors) => {
  // 取消自动分词对源文本的影响，不再修改markedHtml
  // 这样自动分词功能仍然会正常工作，但不会在源文本中显示分词结果的边框
  // 保持函数存在以确保其他代码调用不会出错
};

// 发布分词内容
const publishTokenizedContent = async () => {
  if (tokenizedItems.value.length === 0) {
    message.warning('请先进行分词操作');
    return;
  }
  
  try {
    // 构建请求体
    const requestBody = {
      id: Date.now().toString(), // 使用时间戳作为临时ID
      blocks: tokenizedItems.value.map((item, index) => ({
        id: `${Date.now()}_${index}`, // 为每个block生成唯一ID
        sortNum: index + 1, // 排序编号从1开始
        content: item.text
      }))
    };
    
    // 发送POST请求
    const response = await link('/file/participleContent', 'post', requestBody);
    
    if (response.status === 200) {
      message.success('发布成功');
    } else {
      message.error('发布失败，请稍后重试');
    }
  } catch (error) {
    console.error('发布请求失败:', error);
    message.error('发布请求失败，请检查网络连接或稍后重试');
  }
};
</script>

<template>
  <div class="manual-tokenize-container">
    <div class="tokenize-content">
      <!-- 左边的文本框 -->
      <div class="text-container left-container">
        <div class="container-header">
          <h3>源文本({{ fileName || '无文件' }})</h3>
        </div>
        <div 
          class="text-box" 
          ref="sourceTextBox" 
          contenteditable="true" 
          @input="handleTextChange"
          v-html="markedHtml"
        ></div>
      </div>
      
      <!-- 中间的转移按钮 -->
      <div class="transfer-controls">
        <Button type="primary" @click="handleTransferText">
          &gt;&gt;
        </Button>
        <Button @click="handleAutoTokenize">
          自动分词
        </Button>
        <Button type="primary" class="publish-btn" @click="publishTokenizedContent">
          发布
        </Button>
      </div>
      
      <!-- 右边的文本框 -->
      <div class="text-container right-container">
        <div class="container-header">
          <h3>分词结果</h3>
          <Button size="small" @click="clearAllTokens">清空</Button>
        </div>
        <div class="tokenized-box">
          <div 
            v-for="(item, index) in tokenizedItems" 
            :key="index" 
            class="token-item" 
            :style="{ backgroundColor: item.color }"
          >
            <span class="token-index">{{ index + 1 }}.</span> 
            <span class="token-text">{{ item.text }}</span>
            <button 
              class="token-remove-btn" 
              @click.stop="removeToken(index)"
              title="删除此分词项"
            >
              ×
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.manual-tokenize-container {
  padding: 20px;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.file-info {
  margin: 20px 0;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.manual-tokenize-container h2 {
  margin-top: 10px;
  margin-bottom: 15px;
  font-weight: 500;
  font-size: 20px;
}

.file-name {
  font-size: 16px;
  font-weight: normal;
  color: #666;
  margin-left: 10px;
}

.tokenize-content {
  flex: 1;
  display: flex;
  gap: 20px;
  align-items: stretch;
  min-height: 600px;
  height: 70vh;
  overflow: hidden;
}

.text-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  max-height: 100%;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

.container-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid #d9d9d9;
  background-color: #fafafa;
}

.container-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.text-box {
  flex: 1;
  padding: 16px;
  overflow-y: scroll;
  white-space: pre-wrap;
  word-wrap: break-word;
  background-color: white;
  outline: none; /* 去除聚焦时的默认边框 */
  max-height: 600px;
  height: 100%;
  /* 优化滚动条样式 */
  scrollbar-width: thin;
  scrollbar-color: #d9d9d9 #f5f5f5;
}

/* 分词标记的样式 */
.text-box span {
  transition: all 0.3s;
}

/* Webkit浏览器滚动条样式 */
.text-box::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.text-box::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 4px;
}

.text-box::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 4px;
}

.text-box::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}

.tokenized-span {
  transition: all 0.3s;
}

.tokenized-span:hover {
  background-color: rgba(0, 120, 215, 0.2) !important;
  cursor: pointer;
}

.text-box::selection {
  background-color: #0078d7;
  color: white;
}

.transfer-controls {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.publish-btn {
  background-color: #52c41a !important;
  border-color: #52c41a !important;
}

.publish-btn:hover {
  background-color: #73d13d !important;
  border-color: #73d13d !important;
}

.tokenized-box {
  flex: 1;
  padding: 16px;
  overflow-y: scroll;
  background-color: white;
  max-height: 600px;
  height: 100%;
  /* 优化滚动条样式 */
  scrollbar-width: thin;
  scrollbar-color: #d9d9d9 #f5f5f5;
}

/* Webkit浏览器滚动条样式 */
.tokenized-box::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.tokenized-box::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 4px;
}

.tokenized-box::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 4px;
}

.tokenized-box::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}

.token-item {
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  user-select: none;
  transition: all 0.3s;
  display: block;
  margin: 6px 0;
  width: fit-content;
}

.token-index {
  font-weight: bold;
  margin-right: 6px;
  color: #333;
  opacity: 0.8;
}

.token-text {
  flex: 1;
}

.token-remove-btn {
  background: none;
  border: none;
  color: #666;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  padding: 0 6px;
  margin-left: 8px;
  opacity: 0.7;
  transition: opacity 0.3s;
  display: inline-block;
}

.token-remove-btn:hover {
  opacity: 1;
  color: #d93025;
}

.token-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 30px;
}

.token-item:hover {
  opacity: 0.8;
  transform: translateY(-2px);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .tokenize-content {
    flex-direction: column;
    height: auto;
    min-height: 500px;
  }
  
  .text-container {
    width: 100%;
    max-height: 300px;
    height: 300px;
  }
  
  .text-box,
  .tokenized-box {
    max-height: 220px;
    overflow-y: scroll !important;
  }
  
  .transfer-controls {
    flex-direction: row;
  }
}
</style>