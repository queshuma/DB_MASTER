import { createRouter, createWebHistory } from 'vue-router';
import Chatbot from '../views/Chatbot.vue';
import DatabaseList from '../views/DatabaseList.vue';
import PersonalSettings from '../views/PersonalSettings.vue';
import OperationRecords from '../views/OperationRecords.vue';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  { path: '/chatbot', name: 'Chatbot', component: Chatbot },
  { path: '/database-list', name: 'DatabaseList', component: DatabaseList },
  { path: '/personal-settings', name: 'PersonalSettings', component: PersonalSettings },
  { path: '/operation-records', name: 'OperationRecords', component: OperationRecords }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;