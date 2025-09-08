<script setup>
import { ref, h, onMounted, watch } from 'vue';
import { Table, Button, message, Input, Pagination, Modal } from 'ant-design-vue';
import { PlusOutlined, SearchOutlined, InfoCircleOutlined, LinkOutlined, HistoryOutlined } from '@ant-design/icons-vue';
import { useRouter, useRoute } from 'vue-router';
import link from '../link/Link.js';
import FloatingButton from '../components/FloatingButton.vue';

const router = useRouter();
const route = useRoute();

// 项目列表数据
const projectData = ref([]);
// 搜索关键词
const searchKeyword = ref('');
// 分页参数
const currentPage = ref(1);
const pageSize = ref(10);
const totalCount = ref(0);

// 获取项目列表数据
const getProjectList = async () => {
  try {
    const response = await link('/project/getList', 'post', {
      page: currentPage.value,
      size: pageSize.value,
      name: searchKeyword.value
    });
    projectData.value = response.records;
    totalCount.value = response.total;
  } catch (error) {
    message.error('获取项目列表失败: ' + error.message);
    console.error('Failed to fetch project list:', error);
  }
};

// 初始加载数据
onMounted(() => {
  getProjectList();
});

// 监听路由变化，重新加载数据
watch(() => route.fullPath, () => {
  getProjectList();
});

// 处理分页变化
const handlePageChange = (page, newPageSize) => {
  currentPage.value = page;
  pageSize.value = newPageSize;
  getProjectList();
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
  getProjectList();
};

// 按下Enter键触发搜索
const handleKeyPress = (e) => {
  if (e.key === 'Enter') {
    handleSearch();
  }
};// 表格列配置
const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '项目名称', dataIndex: 'projectName', key: 'projectName', width: 150 },
  { title: '项目类型', dataIndex: 'projectType', key: 'projectType', width: 120 },
  { title: '项目描述', dataIndex: 'projectDescription', key: 'projectDescription', width: 200 },
  { 
    title: '创建时间', 
    dataIndex: 'createTime', 
    key: 'createTime', 
    width: 180, 
    customRender: ({ text }) => {
      return text ? new Date(text).toLocaleString() : '';
    }
  },
  { 
    title: '更新时间', 
    dataIndex: 'updateTime', 
    key: 'updateTime', 
    width: 180, 
    customRender: ({ text }) => {
      return text ? new Date(text).toLocaleString() : '';
    }
  },
  { title: '备份次数', dataIndex: 'backCount', key: 'backCount', width: 100 },
  { title: '版本号', dataIndex: 'version', key: 'version', width: 100 },
  { 
    title: '操作', 
    key: 'action', 
    width: 140, 
    fixed: 'right', 
    customRender: ({ record }) => {
      return h('div', { 
        style: {
          display: 'flex',
          gap: '8px'
        }
      }, [
        h(Button, {
          type: 'primary',
          size: 'small',
          onClick: () => handlePing(record),
          icon: h(LinkOutlined)
        }, 'Ping'),
        h(Button, {
          type: 'primary',
          size: 'small',
          onClick: () => handleDetail(record),
          icon: h(InfoCircleOutlined)
        }, '详情'),
        h(Button, {
          type: 'default',
          size: 'small',
          onClick: () => handleBackup(record),
          icon: h(HistoryOutlined)
        }, '备份'),
        h(Button, {
          type: 'default',
          size: 'small',
          onClick: () => handleBackupRecords(record),
          icon: h(HistoryOutlined)
        }, '备份记录')
      ]);
    }
  }
];

// 处理添加事件
const handleAdd = () => {
  // 打开对话框并设置默认输入值为'abcd'
  if (floatingButtonRef.value) {
    floatingButtonRef.value.openDialogWithMessage('', '项目名称：\n项目描述： \n项目类型：');
  }
};

// 为FloatingButton组件创建引用
const floatingButtonRef = ref(null);

// 处理Ping事件
const handlePing = async (record) => {
  try {
    // 发起GET请求到/project/connectCheck接口
    const response = await link('/project/connectCheck', 'get', {
      projectId: record.id
    });
    
    // 根据返回结果显示不同的消息
    if (response === true) {
      message.success('连接成功');
    } else {
      message.error('连接失败');
    }
  } catch (error) {
    message.error('操作失败: ' + error.message);
    console.error('Ping operation failed:', error);
  }
};

// 处理详情事件
const handleDetail = async (record) => {
  try {
    // 打开悬浮球对话框请求，设置空的默认输入值
    if (floatingButtonRef.value) {
      floatingButtonRef.value.openDialogWithMessage('查询项目id为' + record.id + '项目详情', '');
    }
  } catch (error) {
    message.error('操作失败: ' + error.message);
    console.error('Operation failed:', error);
  }
};

// 处理备份事件
const handleBackup = async (record) => {
  try {
    // 打开悬浮球对话框请求，发起流式请求
    if (floatingButtonRef.value) {
      floatingButtonRef.value.openDialogWithMessage('备份' + record.projectName + '项目数据库结构');
    }
  } catch (error) {
    message.error('操作失败: ' + error.message);
    console.error('Backup operation failed:', error);
  }
};

// 处理备份记录事件
const handleBackupRecords = async (record) => {
  try {
    // 打开悬浮球对话框请求，设置空的默认输入值
    if (floatingButtonRef.value) {
      floatingButtonRef.value.openDialogWithMessage('查询项目id为' + record.id + '的备份记录', '');
    }
  } catch (error) {
    message.error('操作失败: ' + error.message);
    console.error('Backup records operation failed:', error);
  }
};
</script>

<template>
  <div class="database-list-container">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <h2>项目列表</h2>
        <div style="display: flex; gap: 10px;">
          <Input
            placeholder="输入项目名称搜索"
            v-model:value="searchKeyword"
            :prefix="h(SearchOutlined)"
            style="width: 200px;"
            @keypress="handleKeyPress"
          />
          <Button type="primary" @click="handleSearch">搜索</Button>
          <Button type="primary" @click="handleAdd">
            <template #icon>
              <PlusOutlined />
            </template>
            添加项目
          </Button>
        </div>
    </div>
    
    <Table
      :columns="columns"
      :data-source="projectData"
      bordered
      rowKey="id"
      pagination="false"
      style="width: 100%"
    />
    <div style="display: flex; justify-content: flex-end; marginTop: 16px;">
      <Pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="totalCount"
        @change="handlePageChange"
        @showSizeChange="handlePageChange"
        showSizeChanger
        :pageSizeOptions="['5', '10', '20', '50']"
      />
    </div>
  </div>
  
  <!-- 右侧悬浮球按钮 -->
  <FloatingButton @onClick="handleAdd" ref="floatingButtonRef" />
</template>

<style scoped>
.database-list-container {
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
}
</style>