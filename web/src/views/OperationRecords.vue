<template>
  <div class="operation-records-container">
    <h2>操作记录</h2>
    
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
      </a-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { link } from '../link/Link';

// 表格列配置
const columns = [
  {    title: 'ID',    dataIndex: 'id',    key: 'id',    width: 200,  },
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
        page: paginationConfig.current,
        size: paginationConfig.pageSize,
        // 可以根据需要添加查询条件
      },
      {}, // params参数为空
      {'Content-Type': 'application/json'}
    );

    // 处理返回数据，根据接口实际返回格式
    if (response) {
      // 优先使用records字段获取数据列表
      if (response.records && Array.isArray(response.records)) {
        operationRecords.value = response.records || [];
        // 如果total为0但records有数据，使用records的长度作为total
        paginationConfig.total = response.total > 0 ? response.total : response.records.length;
      } 
      // 兼容旧格式，保持向后兼容
      else if (response.list && Array.isArray(response.list)) {
        operationRecords.value = response.list || [];
        paginationConfig.total = response.total || 0;
      }
      // 处理返回格式可能不一致的情况
      else if (Array.isArray(response)) {
        operationRecords.value = response;
        paginationConfig.total = response.length;
      }
      // 默认情况
      else {
        operationRecords.value = [];
        paginationConfig.total = 0;
      }
    } else {
      operationRecords.value = [];
      paginationConfig.total = 0;
    }
  } catch (error) {
    console.error('获取操作记录失败:', error);
    // 接口调用失败时清空数据
    operationRecords.value = [];
    paginationConfig.total = 0;
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