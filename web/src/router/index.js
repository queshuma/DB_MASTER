import { createRouter, createWebHistory } from 'vue-router';
import Chatbot from '../views/Chatbot.vue';
import DatabaseList from '../views/DatabaseList.vue';
import PersonalSettings from '../views/PersonalSettings.vue';
import OperationRecords from '../views/OperationRecords.vue';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import EmailLogin from '../views/EmailLogin.vue';
import FileInfoDisplay from '../views/FileInfoDisplay.vue';
import QABot from '../views/QABot.vue';

import HomeView from '@/views/HomeView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  { path: '/email-login', name: 'EmailLogin', component: EmailLogin },
  { path: '/chatbot', name: 'Chatbot', component: Chatbot },
  { path: '/database-list', name: 'DatabaseList', component: DatabaseList },
  { path: '/personal-settings', name: 'PersonalSettings', component: PersonalSettings },
  { path: '/operation-records', name: 'OperationRecords', component: OperationRecords },
  { path: '/file-info', name: 'FileInfoDisplay', component: FileInfoDisplay },
  { path: '/qa-bot', name: 'QABot', component: QABot }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;