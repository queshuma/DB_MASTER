<script setup>
import { ref, onMounted } from 'vue';
import { FileOutlined, FolderOpenOutlined, ReloadOutlined, DownloadOutlined, InboxOutlined } from '@ant-design/icons-vue';
import { Card, Table, Button, Space, message, Upload, Tag, Modal } from 'ant-design-vue';
import link from '../link/Link';

// 文件列表数据
const fileList = ref([]);
// 加载状态
const loading = ref(false);
// 搜索关键词
const searchKeyword = ref('');
// 显示上传弹窗
const showUploadModal = ref(false);
// 选中的文件
const selectedFile = ref(null);
// 文件内容预览
const fileContentPreview = ref('');
const showFileContentModal = ref(false);

// 模拟文件数据
const mockFileData = [
  {
    id: '1',
    name: '项目需求文档.docx',
    size: '2.5MB',
    type: 'document',
    path: '/docs/requirements/',
    lastModified: '2023-11-15 14:30:00',
    status: 'active',
    tags: ['需求', '项目规划']
  },
  {
    id: '2',
    name: '数据库设计方案.pdf',
    size: '4.8MB',
    type: 'document',
    path: '/docs/design/',
    lastModified: '2023-11-10 09:15:00',
    status: 'active',
    tags: ['设计', '数据库']
  },
  {
    id: '3',
    name: '技术架构图.png',
    size: '1.2MB',
    type: 'image',
    path: '/assets/images/',
    lastModified: '2023-11-05 16:45:00',
    status: 'active',
    tags: ['架构', '图片']
  },
  {
    id: '4',
    name: '接口说明文档.md',
    size: '0.8MB',
    type: 'document',
    path: '/docs/api/',
    lastModified: '2023-11-01 11:20:00',
    status: 'active',
    tags: ['API', '文档']
  },
  {
    id: '5',
    name: '用户手册.docx',
    size: '3.2MB',
    type: 'document',
    path: '/docs/manual/',
    lastModified: '2023-10-25 10:00:00',
    status: 'active',
    tags: ['用户指南', '手册']
  }
];

// 获取文件列表
const fetchFileList = async () => {
  loading.value = true;
  try {
    // 模拟API请求延迟
    await new Promise(resolve => setTimeout(resolve, 500));
    // 在实际项目中，这里应该调用后端API获取文件列表
    // const response = await link('/api/files/list', 'get');
    // fileList.value = response.data;
    
    // 目前使用模拟数据
    fileList.value = mockFileData;
  } catch (error) {
    console.error('获取文件列表失败:', error);
    message.error('获取文件列表失败，请稍后重试');
    // 出错时使用模拟数据
    fileList.value = mockFileData;
  } finally {
    loading.value = false;
  }
};

// 搜索文件
const searchFiles = () => {
  if (!searchKeyword.value.trim()) {
    fetchFileList();
    return;
  }
  
  const keyword = searchKeyword.value.toLowerCase().trim();
  fileList.value = mockFileData.filter(file => 
    file.name.toLowerCase().includes(keyword) ||
    file.path.toLowerCase().includes(keyword) ||
    file.tags.some(tag => tag.toLowerCase().includes(keyword))
  );
};

// 预览文件内容
const previewFile = (file) => {
  selectedFile.value = file;
  // 模拟文件内容
  fileContentPreview.value = `文件名: ${file.name}\n文件大小: ${file.size}\n文件路径: ${file.path}\n最后修改时间: ${file.lastModified}\n文件类型: ${file.type}\n状态: ${file.status === 'active' ? '正常' : '禁用'}\n标签: ${file.tags.join(', ')}`;
  showFileContentModal.value = true;
};

// 下载文件
const downloadFile = (file) => {
  message.success(`开始下载文件: ${file.name}`);
  // 实际项目中应该调用后端API进行文件下载
  // link('/api/files/download', 'post', { fileId: file.id });
};

// 删除文件
const deleteFile = (file) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除文件 "${file.name}" 吗？`,
    onOk: async () => {
      try {
        // 模拟API请求延迟
        await new Promise(resolve => setTimeout(resolve, 300));
        // 在实际项目中，这里应该调用后端API删除文件
        // await link('/api/files/delete', 'post', { fileId: file.id });
        
        // 从本地列表中移除
        fileList.value = fileList.value.filter(f => f.id !== file.id);
        message.success('文件删除成功');
      } catch (error) {
        console.error('删除文件失败:', error);
        message.error('删除文件失败，请稍后重试');
      }
    }
  });
};

// 上传文件
const uploadFile = async (file) => {
  try {
    // 模拟API请求延迟
    await new Promise(resolve => setTimeout(resolve, 1000));
    // 在实际项目中，这里应该调用后端API上传文件
    // await link('/api/files/upload', 'post', file);
    
    message.success('文件上传成功');
    showUploadModal.value = false;
    fetchFileList(); // 重新加载文件列表
  } catch (error) {
    console.error('上传文件失败:', error);
    message.error('上传文件失败，请稍后重试');
  }
};

// 表格列配置
const columns = [
  {
    title: '文件名',
    dataIndex: 'name',
    key: 'name',
    ellipsis: true,
    slots: {
      customRender: 'name'
    }
  },
  {
    title: '大小',
    dataIndex: 'size',
    key: 'size',
    width: 100
  },
  {
    title: '路径',
    dataIndex: 'path',
    key: 'path',
    ellipsis: true
  },
  {
    title: '最后修改',
    dataIndex: 'lastModified',
    key: 'lastModified',
    width: 160
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80,
    slots: {
      customRender: 'status'
    }
  },
  {
    title: '标签',
    dataIndex: 'tags',
    key: 'tags',
    slots: {
      customRender: 'tags'
    }
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
    slots: {
      customRender: 'action'
    }
  }
];

// 上传配置
const uploadProps = {
  name: 'file',
  multiple: false,
  action: '/api/files/upload', // 实际项目中应使用真实的上传接口
  headers: {
    authorization: 'Bearer token',
  },
  onChange(info) {
    if (info.file.status !== 'uploading') {
      console.log(info.file, info.fileList);
    }
    if (info.file.status === 'done') {
      message.success(`${info.file.name} 文件上传成功`);
      showUploadModal.value = false;
      fetchFileList();
    } else if (info.file.status === 'error') {
      message.error(`${info.file.name} 文件上传失败`);
    }
  },
};

// 组件挂载时获取文件列表
onMounted(() => {
  fetchFileList();
});
</script>

<template>
  <div class="file-info-display">
    <Card title="文件信息显示" class="card-container">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="search-container">
          <a-input
            v-model:value="searchKeyword"
            placeholder="搜索文件名、路径或标签..."
            style="width: 300px; margin-right: 16px;"
            allow-clear
            @change="searchFiles"
          />
          <Button type="default" @click="fetchFileList">
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新
          </Button>
        </div>
        <Button type="primary" @click="showUploadModal = true">
          上传文件
        </Button>
      </div>

      <!-- 文件列表 -->
      <Table
        :columns="columns"
        :data-source="fileList"
        row-key="id"
        :loading="loading"
        :scroll="{ x: 'max-content' }"
        pagination
        class="file-table"
      >
        <template #name="{ text, record }">
          <Space>
            <component :is="record.type === 'image' ? FolderOpenOutlined : FileOutlined" />
            <span>{{ text }}</span>
          </Space>
        </template>
        <template #status="{ text }">
          <Tag :color="text === 'active' ? 'green' : 'red'">
            {{ text === 'active' ? '正常' : '禁用' }}
          </Tag>
        </template>
        <template #tags="{ text }">
          <Space wrap>
            <Tag v-for="(tag, index) in text" :key="index" color="blue">
              {{ tag }}
            </Tag>
          </Space>
        </template>
        <template #action="{ record }">
          <Space size="middle">
            <Button size="small" @click="() => previewFile(record)">预览</Button>
            <Button size="small" type="primary" @click="() => downloadFile(record)">
              <template #icon>
                <DownloadOutlined />
              </template>
              下载
            </Button>
            <Button size="small" danger @click="() => deleteFile(record)">删除</Button>
          </Space>
        </template>
      </Table>

      <!-- 文件内容预览弹窗 -->
      <Modal
        v-model:visible="showFileContentModal"
        title="文件内容预览"
        :width="600"
        footer=""
      >
        <div v-if="selectedFile" class="file-preview-content">
          <div class="preview-header">
            <h4>{{ selectedFile.name }}</h4>
            <p>{{ selectedFile.path }}</p>
          </div>
          <div class="preview-body">
            <pre>{{ fileContentPreview }}</pre>
          </div>
        </div>
      </Modal>

      <!-- 上传文件弹窗 -->
      <Modal
        v-model:visible="showUploadModal"
        title="上传文件"
        width="520"
        :footer="null"
      >
        <Upload.Dragger v-bind="uploadProps">
          <p class="ant-upload-drag-icon">
            <template>
              <InboxOutlined />
            </template>
          </p>
          <p class="ant-upload-text">点击或拖拽文件到此处上传</p>
          <p class="ant-upload-hint">
            支持单个文件上传，文件大小不超过10MB
          </p>
        </Upload.Dragger>
      </Modal>
    </Card>
  </div>
</template>

<style scoped>
.file-info-display {
  height: 100%;
  width: 100%;
  overflow: auto;
}

.card-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 0;
}

.search-container {
  display: flex;
  align-items: center;
}

.file-table {
  flex: 1;
  overflow: auto;
}

.file-preview-content {
  max-height: 400px;
  overflow-y: auto;
}

.preview-header {
  border-bottom: 1px solid #e8e8e8;
  padding-bottom: 12px;
  margin-bottom: 16px;
}

.preview-header h4 {
  margin: 0 0 4px 0;
}

.preview-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.preview-body pre {
  background-color: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .search-container {
    width: 100%;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .search-container .ant-input {
    width: 100% !important;
    margin-right: 0 !important;
  }
}
</style>