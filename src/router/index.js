import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth' // Импорт от друга для проверки входа

// --- ИМПОРТ ВСЕХ СТРАНИЦ ---
// Твои страницы:
import HomeView from '../views/HomeView.vue'
import PasteView from '../views/PasteView.vue'
import ArchiveView from '../views/ArchiveView.vue'
// Страницы друга:
import LoginView from '../views/LoginView.vue'
import MyPastesView from '../views/MyPastesView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView, // Главная страница (создание пасты)
      meta: { requiresAuth: true }
    },
    {
      path: '/archive',
      name: 'archive',
      component: ArchiveView // Твоя страница архива (публичная)
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView // Страница логина от друга (публичная)
    },
    {
      path: '/paste/:id',
      name: 'paste-view',
      component: PasteView
      // Я убрал requiresAuth, чтобы любой человек мог посмотреть публичную пасту по ссылке
    },
    {
      path: '/my-pastes',
      name: 'my-pastes',
      component: MyPastesView,
      meta: { requiresAuth: true } // А вот этот раздел доступен ТОЛЬКО после входа
    }
  ]
})

// --- ЕДИНЫЙ НАВИГАЦИОННЫЙ ГАРД (Логика друга) ---
// Этот код запускается перед каждым переходом по ссылкам
router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  
  // Если страница требует входа (requiresAuth === true) и пользователь НЕ вошел
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    next('/login') // Выгоняем его на страницу логина
  } else {
    next() // Иначе - пускаем на страницу без проблем
  }
})

export default router