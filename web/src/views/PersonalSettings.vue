<template>
  <div class="personal-settings-container">
    <h2>个人设置</h2>
    <a-card class="profile-card" :bordered="false">
      <a-row :gutter="[24, 16]" class="profile-row">
        <a-col :span="6" class="avatar-col">
          <div class="avatar-container">
            <a-avatar
              :size="128"
              :src="userInfo.avatar || defaultAvatar"
              class="avatar"
            />
            <div class="upload-btn">
              <a-button type="primary" @click="handleUploadClick" size="small">上传头像</a-button>
              <input
                type="file"
                ref="fileInput"
                accept="image/*"
                style="display: none"
                @change="handleFileChange"
              />
            </div>
          </div>
        </a-col>
        <a-col :span="18" class="form-col">
          <a-form
            :model="formData"
            class="profile-form"
            layout="vertical"
          >
            <a-row :gutter="[16, 16]">
              <a-col :span="12">
                <a-form-item label="用户名">
                  <a-input v-model:value="formData.username" placeholder="请输入用户名" />
                </a-form-item>
              </a-col>

              <a-col :span="12">
                <a-form-item label="手机号码">
                  <a-input v-model:value="formData.phone" placeholder="请输入手机号码" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="邮箱">
                  <a-input v-model:value="formData.email" placeholder="请输入邮箱" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
      </a-row>
    </a-card>

    <div class="save-button-container">
      <a-button type="primary" @click="handleSave">保存设置</a-button>
      <a-button style="margin-left: 12px;" @click="handleCancel">取消</a-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useStore } from 'vuex';
import { message } from 'ant-design-vue';
import link from '../link/Link.js';

const store = useStore();
const fileInput = ref(null);
const defaultAvatar = 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png';

// 用户信息
const userInfo = ref({
  username: '',
  telephone: '',
  email: '',
  avatar: ''
});

// 表单数据
const formData = ref({
  username: '',
  nickname: '',
  phone: '',
  email: ''
});

// 获取用户信息
const getUserInfo = async () => {
    const response = await link('/user/getUserInfo', 'GET');
      userInfo.value = response;
      console.log(response)

      // 初始化表单数据
      formData.value = {
        username: userInfo.value.username,
        phone: userInfo.value.telephone || '',
        email: userInfo.value.email || ''
      }
};

// 获取头像信息
const getAvatarImage = async () => {
  try {
    // 设置responseType为'blob'以接收二进制图像数据
    const response = await link('/user/getAvatarImage', 'GET', {}, {}, {}, true, 'blob');
    if (response) {
      // 将Blob对象转换为data URL
      const reader = new FileReader();
      reader.onloadend = () => {
        userInfo.value.avatar = reader.result;
      };
      reader.readAsDataURL(response);
    }
  } catch (error) {
    console.error('获取头像失败:', error);
    // 使用默认头像
    userInfo.value.avatar = defaultAvatar;
  }
};

// 上传头像
const handleUploadClick = () => {
  fileInput.value.click();
};

// 处理文件变化
const handleFileChange = async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  // 创建本地预览URL
  const previewUrl = URL.createObjectURL(file);
  // 立即更新头像显示（本地预览）
  userInfo.value.avatar = previewUrl;

  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await link('/file/uploadAvatar', 'POST', formData, {}, {
      'Content-Type': 'multipart/form-data'
    });

    console.log('上传头像响应:', response);
    // 检查响应格式，适应不同的服务器返回结构
    let avatarUrl = null;
    if (response && response.data) {
      avatarUrl = response.data;
    } else if (response && typeof response === 'string') {
      // 如果响应是字符串，尝试将其作为URL
      avatarUrl = response;
    }

    if (avatarUrl) {
      // 上传成功，保持使用本地预览URL
      // 不释放本地预览URL资源，以保持头像显示
      // 更新Vuex中的用户头像信息，但不改变当前显示的头像
      if (store.state.userInfo) {
        store.commit('setUserInfo', {
          ...store.state.userInfo,
          avatar: avatarUrl
        });
      }
      message.success('头像上传成功');
    } else {
      console.error('上传头像响应数据无效:', response);
      message.error('上传头像失败，响应数据无效');
      // 上传失败，恢复原来的头像
      getAvatarImage();
      // 释放本地预览URL资源
      URL.revokeObjectURL(previewUrl);
    }
  } catch (error) {
    console.error('上传头像失败:', error);
    message.error('上传头像失败，请重试');
    // 上传失败，恢复原来的头像
    getAvatarImage();
    // 释放本地预览URL资源
    URL.revokeObjectURL(previewUrl);
  }

  // 重置文件输入
  e.target.value = '';
};

// 处理保存设置
const handleSave = async () => {
  // 修改个人信息
  try {
    const response = await link('/user/modifyUserInfo', 'POST', formData.value);

    // 更新用户信息
    Object.assign(userInfo.value, formData.value);
    message.success('个人信息更新成功');
  } catch (error) {
    console.error('更新个人信息失败:', error);
    message.error('更新个人信息失败，请重试');
  }
};

// 取消修改
const handleCancel = () => {
  // 重置表单数据
  formData.value = {
      username: userInfo.value.username,
      phone: userInfo.value.phone || '',
      email: userInfo.value.email || ''
    };

  message.info('已取消修改');
};

// 初始加载
onMounted(() => {
  getUserInfo();
  getAvatarImage();
});
</script>

<style scoped>
.personal-settings-container {
  padding: 20px;
  background-color: #f5f5f5;
}

.profile-card,
.profile-row {
  align-items: flex-start;
  padding: 10px 0;
}

.avatar-col {
  display: flex;
  justify-content: center;
  padding-right: 20px;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar {
  margin-bottom: 16px;
}

.upload-btn {
  margin-top: 10px;
}

.form-col {
  padding-left: 20px;
  border-left: 1px solid #e8e8e8;
}

.profile-form .ant-row,
.ant-input {
  width: 100%;
}

.save-button-container {
  margin-top: 24px;
  text-align: right;
}

</style>