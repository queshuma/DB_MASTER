import { createStore } from 'vuex';

// 创建一个新的store实例
const store = createStore({
  state() {
    return {
      userInfo: null,
    };
  },
  mutations: {
    setUserInfo(state, userInfo) {
      state.userInfo = userInfo;
      console.log(userInfo)
    },
    clearUserInfo(state) {
      state.userInfo = null;
    },
  },
  actions: {
    // 可以在这里定义异步操作
  },
  getters: {
    isLoggedIn(state) {
      return !!state.userInfo;
    },
    userInfo(state) {
      return state.userInfo;
    },
  },
});

export default store;