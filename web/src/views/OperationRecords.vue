<template>
  <div class="operation-records-container">
    <h2>操作记录</h2>
    
    <!-- 模拟数据提示 -->
    <a-alert
      v-if="showMockDataAlert"
      message="当前显示的是模拟数据"
      description="实际环境中会从服务器获取真实的操作记录"
      type="info"
      showIcon
      style="margin-bottom: 16px;"
    />
    
    <!-- 操作记录表格 -->
    <div class="records-content">
      <a-table
        :columns="columns"
        :data-source="operationRecords"
        row-key="id"
        :loading="loading"
        :pagination="paginationConfig"
        @change="handleTableChange"
      >
        <!-- 如有需要可以在这里添加自定义列渲染 -->
      </a-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { link } from '../link/Link';

// 表格列配置
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 100,
  },
  {
    title: '操作记录',
    dataIndex: 'operation',
    key: 'operation',
    ellipsis: true,
  },
  {
    title: '用户名称',
    dataIndex: 'userName',
    key: 'userName',
    width: 120,
  },
  {
    title: '项目名称',
    dataIndex: 'projectName',
    key: 'projectName',
    width: 150,
  },
  {
    title: '创建时间',
    dataIndex: 'createDate',
    key: 'createDate',
    width: 180,
  },
];

// 操作记录数据
const operationRecords = ref([]);
// 加载状态
const loading = ref(false);
// 是否显示模拟数据提示
const showMockDataAlert = ref(false);

// 模拟数据，用于接口未就绪时的展示
const mockData = [
  {
    id: '1',
    operation: '创建新项目 "客户管理系统"',
    userName: '张三',
    projectName: '客户管理系统',
    createDate: '2023-10-15 10:23:45'
  },
  {
    id: '2',
    operation: '更新数据库配置信息',
    userName: '李四',
    projectName: '数据分析平台',
    createDate: '2023-10-15 09:12:30'
  },
  {
    id: '3',
    operation: '执行数据导入任务',
    userName: '王五',
    projectName: '客户管理系统',
    createDate: '2023-10-14 16:45:22'
  },
  {
    id: '4',
    operation: '删除过期日志记录',
    userName: '赵六',
    projectName: '日志管理系统',
    createDate: '2023-10-14 14:05:18'
  },
  {
    id: '5',
    operation: '修改用户权限设置',
    userName: '张三',
    projectName: '客户管理系统',
    createDate: '2023-10-13 11:30:40'
  }
];

// 分页配置
const paginationConfig = reactive({
  current: 1,
  pageSize: 10,
  pageSizeOptions: ['10', '20', '50', '100'],
  showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
  showSizeChanger: true,
  showQuickJumper: true,
  total: 0,
});

// 获取操作记录列表
const getOperationRecords = async () => {
  loading.value = true;
  try {
    // 根据Link.js中link函数的参数顺序：url, method, data, params, headers, withCredentials, responseType
    // POST请求中，分页参数应该放在data中
    const response = await link(
      '/api/operation/getList', // 不需要完整URL，Link.js中已配置baseURL
      'POST',
      {
        pageNum: paginationConfig.current,
        pageSize: paginationConfig.pageSize,
        // 可以根据需要添加查询条件
      },
      {}, // params参数为空
      {'Content-Type': 'application/json'}
    );

    // 处理返回数据，根据Link.js的响应拦截器，返回的直接是response.data
    if (response && response.list !== undefined) {
      operationRecords.value = response.list || [];
      paginationConfig.total = response.total || 0;
    } else {
      // 处理返回格式可能不一致的情况
      operationRecords.value = Array.isArray(response) ? response : [];
      paginationConfig.total = operationRecords.value.length;
    }
  } catch (error) {
    console.error('获取操作记录失败:', error);
    // 在接口未就绪时使用模拟数据
    console.log('使用模拟数据展示操作记录');
    // 模拟分页逻辑
    const start = (paginationConfig.current - 1) * paginationConfig.pageSize;
    const end = start + paginationConfig.pageSize;
    operationRecords.value = mockData.slice(start, end);
    paginationConfig.total = mockData.length;
    
    // 显示模拟数据提示
    showMockDataAlert.value = true;
  } finally {
    loading.value = false;
  }
};

// 处理表格分页变化
const handleTableChange = (pagination) => {
  paginationConfig.current = pagination.current;
  paginationConfig.pageSize = pagination.pageSize;
  getOperationRecords();
};

// 页面加载时获取数据
onMounted(() => {
  getOperationRecords();
});
</script>

<style scoped>
.operation-records-container {
  height: 100%;
  padding: 20px;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.operation-records-container h2 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
}

.records-content {
  flex: 1;
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
  overflow: auto;
}

/* 表格样式优化 */
:deep(.ant-table) {
  font-size: 14px;
}

:deep(.ant-table-thead > tr > th) {
  background-color: #fafafa;
  font-weight: 500;
}

:deep(.ant-pagination) {
  margin-top: 16px;
  text-align: right;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .operation-records-container {
    padding: 10px;
  }
  
  .records-content {
    padding: 10px;
  }
}
</style>