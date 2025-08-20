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
      localStorage.setItem('userInfo', JSON.stringify(userInfo));
      console.log(userInfo)
    },
    clearUserInfo(state) {
      state.userInfo = null;
      localStorage.removeItem('userInfo');
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