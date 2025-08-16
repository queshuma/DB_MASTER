<script setup>
import { ref, h } from 'vue';
import { Table, Button, message } from 'ant-design-vue';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 模拟数据库列表数据
const databaseData = ref([
  { id: 1, name: '测试数据库1', type: 'MySQL', host: 'localhost', port: 3306, status: '运行中' },
  { id: 2, name: '测试数据库2', type: 'PostgreSQL', host: '192.168.1.100', port: 5432, status: '运行中' },
  { id: 3, name: '测试数据库3', type: 'MongoDB', host: '192.168.1.101', port: 27017, status: '已停止' },
  { id: 4, name: '测试数据库4', type: 'Redis', host: '192.168.1.102', port: 6379, status: '运行中' },
  { id: 5, name: '测试数据库5', type: 'SQLite', host: 'localhost', port: null, status: '运行中' },
]);

// 表格列配置
const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '数据库名称', dataIndex: 'name', key: 'name', width: 150 },
  { title: '数据库类型', dataIndex: 'type', key: 'type', width: 120 },
  { title: '主机', dataIndex: 'host', key: 'host', width: 150 },
  { title: '端口', dataIndex: 'port', key: 'port', width: 100 },
  { 
    title: '状态', 
    dataIndex: 'status', 
    key: 'status', 
    width: 100, 
    customRender: ({ text }) => {
      return h('span', {
        style: {
          color: text === '运行中' ? 'green' : 'red',
          fontWeight: 'bold'
        }
      }, text);
    }
  },
  { 
    title: '操作', 
    key: 'action', 
    width: 150, 
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
          onClick: () => handleEdit(record),
          icon: h(EditOutlined)
        }, '编辑'),
        h(Button, {
          type: 'danger',
          size: 'small',
          onClick: () => handleDelete(record.id),
          icon: h(DeleteOutlined)
        }, '删除')
      ]);
    }
  },
];

// 处理编辑事件
const handleEdit = (record) => {
  message.info(`编辑数据库: ${record.name}`);
  // 这里可以添加路由跳转或弹窗编辑逻辑
};

// 处理删除事件
const handleDelete = (id) => {
  // 确认删除
  message.warning('确定要删除该数据库吗？').then(() => {
    databaseData.value = databaseData.value.filter(item => item.id !== id);
    message.success('删除成功');
  });
};

// 处理添加事件
const handleAdd = () => {
  message.info('添加新数据库');
  // 这里可以添加路由跳转或弹窗添加逻辑
};
</script>

<template>
  <div class="database-list-container">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2>数据库列表</h2>
      <Button type="primary" @click="handleAdd">
          <template #icon>
            <PlusOutlined />
          </template>
          添加数据库
        </Button>
    </div>
    
    <Table
      :columns="columns"
      :data-source="databaseData"
      bordered
      rowKey="id"
      :pagination="{ pageSize: 10 }"
      style="width: 100%"
    />
  </div>
</template>

<style scoped>
.database-list-container {
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
}
</style>