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
// 不再需要的预览相关变量已移除

// 获取文件列表
const fetchFileList = async () => {
  loading.value = true;
  try {
    // 调用后端API获取文件列表
    const response = await link('/file/getRagFileList', 'post', {
      pageDTO: {
        page: {
          size: 10,
          current: 1
        },
        size: 10
      }
    }, {}, {}, true);
    
    // 处理响应数据，将接口返回的字段映射到组件所需的格式
    if (response && response.records && Array.isArray(response.records)) {
      fileList.value = response.records.map(file => ({
        id: file.id,
        name: file.fileName,
        initialName: file.fileInitialName,
        size: file.fileSize || '未知大小',
        type: file.fileType || 'document',
        path: file.filePath || '',
        // 处理同步状态字段
        sync: file.sync || false,
        // 接口没有返回以下字段，使用默认值
        lastModified: new Date().toLocaleString('zh-CN'),
        status: 'active',
        tags: []
      }));
    } else {
      // 接口返回数据格式不正确时，清空文件列表
      fileList.value = [];
      console.warn('接口返回数据格式不正确');
    }
  } catch (error) {
    console.error('获取文件列表失败:', error);
    message.error('获取文件列表失败，请稍后重试');
    // 出错时清空文件列表
    fileList.value = [];
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
  // 对当前文件列表进行搜索
  const originalList = [...fileList.value];
  fileList.value = originalList.filter(record => 
    record.name.toLowerCase().includes(keyword) ||
    record.path.toLowerCase().includes(keyword) ||
    (record.tags && record.tags.some(tag => tag.toLowerCase().includes(keyword)))
  );
};

// 预览文件内容 - 直接在新页面打开
const previewFile = (record) => {
  if (record && record.path) {
    // 在新标签页中打开文件地址
    window.open(record.path, '_blank');
    message.success(`正在打开文件预览: ${record.name}`);
  } else {
    message.error('文件路径不存在，无法预览');
    console.warn('文件路径不存在:', record);
  }
};

// 下载文件
const downloadFile = async (record) => {
  try {
    loading.value = true;
    message.success(`开始下载文件: ${record.name}`);
    // 调用后端API进行文件下载，根据后端接口要求：POST请求，参数包装在ragFile对象中
    const response = await link('/file/downloadRagFile', 'post', {
      ragFile: {
        id: record.id,
        fileName: record.name,
        fileInitialName: record.initialName,
        fileType: record.type,
        fileSize: record.size,
        filePath: record.path,
        sync: record.sync
      }
    }, {}, {}, true, 'blob');
    
    // 确保获取到正确的blob数据
    const blob = response.data instanceof Blob ? response.data : new Blob([response.data], { type: response.headers?.['content-type'] || 'application/octet-stream' });

    // 文件名（优先使用后端返回的，也可以使用前端的）
    let fileName = record.initialName || 'downloaded.file';
    
    // 尝试从响应头中解析文件名
    const contentDisposition = response.headers?.['content-disposition'];
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename[^;=]*=(["']*(?:UTF-8''))?([^;"']*)/);
      if (filenameMatch && filenameMatch[2]) {
        fileName = decodeURIComponent(filenameMatch[2]);
      }
    }

    // 创建下载链接并触发下载
    const downloadLink = document.createElement('a');
    downloadLink.href = window.URL.createObjectURL(blob);
    downloadLink.download = fileName;
    document.body.appendChild(downloadLink);
    downloadLink.click();

    // 释放资源
    setTimeout(() => {
      window.URL.revokeObjectURL(downloadLink.href);
      document.body.removeChild(downloadLink);
    }, 100);

    message.success(`文件下载成功: ${fileName}`);
  } catch (error) {
    console.error('下载文件失败:', error);
    message.error('下载文件失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 删除文件
const deleteFile = (record) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除文件 "${record.name}" 吗？`,
    onOk: async () => {
      try {
        // 模拟API请求延迟
        await new Promise(resolve => setTimeout(resolve, 300));
        // 在实际项目中，这里应该调用后端API删除文件
        // await link('/api/files/delete', 'post', { fileId: record.id });
        
        // 从本地列表中移除
        fileList.value = fileList.value.filter(f => f.id !== record.id);
        message.success('文件删除成功');
      } catch (error) {
        console.error('删除文件失败:', error);
        message.error('删除文件失败，请稍后重试');
      }
    }
  });
};

// 同步文件
const syncFile = async (record) => {
  try {
    loading.value = true;
    // 调用后端API同步文件，使用post请求并提供file对象
    const response = await link('/file/syncRagFile', 'post', {
        id: record.id,
        fileName: record.name,
        fileInitialName: record.initialName,
        fileType: record.type,
        fileSize: record.size,
        filePath: record.path,
        sync: record.sync
    }, {}, {}, true);
    
    // 更新本地文件的同步状态
    const updatedFile = fileList.value.find(f => f.id === record.id);
    if (updatedFile) {
      updatedFile.sync = true;
    }
    message.success(`${record.name} 同步成功`);
  } catch (error) {
    console.error('同步文件失败:', error);
    message.error('同步文件失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 取消同步文件
const cancelSyncFile = async (record) => {
  Modal.confirm({
    title: '确认取消同步',
    content: `确定要取消文件 "${record.name}" 的同步吗？`,
    onOk: async () => {
      try {
        loading.value = true;
        // 模拟API请求延迟
        await new Promise(resolve => setTimeout(resolve, 300));
        // 在实际项目中，这里应该调用后端API取消同步
        // await link('/file/cancelSyncRagFile', 'post', { fileId: record.id });
        
        // 更新本地文件的同步状态
        const updatedFile = fileList.value.find(f => f.id === record.id);
        if (updatedFile) {
          updatedFile.sync = false;
        }
        message.success(`${record.name} 取消同步成功`);
      } catch (error) {
        console.error('取消同步文件失败:', error);
        message.error('取消同步文件失败，请稍后重试');
      } finally {
        loading.value = false;
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
    dataIndex: 'initialName',
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

// 使用封装的link方法进行文件上传
const uploadProps = {
  name: 'file',
  multiple: false,
  customRequest: async (options) => {
    const { file, onSuccess, onError } = options;
    const formData = new FormData();
    formData.append('file', file);
    
    try {
      // 使用封装的link方法上传文件
      const response = await link('/file/uploadRagFile', 'post', formData, {}, {}, true, 'text');
      
      // 上传成功处理
      onSuccess(response);
      message.success(`${file.name} 文件上传成功`);
      showUploadModal.value = false;
      fetchFileList();
    } catch (error) {
      // 上传失败处理
      onError(error);
      message.error(`${file.name} 文件上传失败`);
      console.error('文件上传失败:', error);
    }
  },
  onChange(info) {
    if (info.file.status !== 'uploading') {
      console.log(info.file, info.fileList);
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
    <Card title="RAG文件仓库" class="card-container">
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
        <template #status="{ record }">
          <Tag :color="record.sync ? 'blue' : 'red'">
            {{ record.sync ? '已同步' : '未同步' }}
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
            <!-- 根据同步状态显示不同的按钮 -->
            <Button v-if="!record.sync" size="small" type="default" @click="() => syncFile(record)">
              同步
            </Button>
            <Button v-else size="small" type="default" danger @click="() => cancelSyncFile(record)">
              取消同步
            </Button>
            <Button size="small" danger @click="() => deleteFile(record)">删除</Button>
          </Space>
        </template>
      </Table>

      <!-- 文件内容预览弹窗已移除，改为直接在新页面打开 -->

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

/* 响应式调整 */
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .search-container {
    width: 100%;
  }
  
  .file-table {
    overflow-x: auto;
  }
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