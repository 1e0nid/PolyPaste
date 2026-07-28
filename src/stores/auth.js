import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', () => {
  // 1. СОСТОЯНИЕ
  const user = ref(JSON.parse(localStorage.getItem('user')) || null)

  // 2. ГЕТТЕРЫ
  const isAuthenticated = computed(() => !!user.value)
  const apiBase = import.meta.env.VITE_API_URL || window.location.origin

  // 3. ДЕЙСТВИЯ
  function login(userData) {
    user.value = {
      username: userData.username,
      avatar: userData.avatar || '👤',
      provider: userData.provider || 'polypaste'
    }
    localStorage.setItem('user', JSON.stringify(user.value))
    console.log('Данные профиля обновлены. Токен управляется Cookies.')
  }

  /**
   * Полный выход из системы (и на клиенте, и на сервере)
   */
  async function logout() {
  // 1. Очищаем локальные данные профиля И САМ ТОКЕН
  user.value = null
  localStorage.removeItem('user')
  localStorage.removeItem('jwt') // 🔥 ВОТ ОНА, СПАСИТЕЛЬНАЯ СТРОЧКА!

  // 2. Так как у тебя чистый JWT в LocalStorage, запрос к бэкенду на /logout 
  // в принципе больше не обязан удалять куки, но мы можем оставить его, 
  // чтобы Spring Security просто очищал контекст потока.
  try {
    await fetch(`${apiBase}/api/auth/logout`, {
      method: 'POST',
      headers: {
        // Если бэкенду для логаута нужен токен, мы его передаем (пока еще не стерли из переменной)
        'Authorization': `Bearer ${localStorage.getItem('jwt')}`
      }
    })
    console.log('Сессия на сервере закрыта.')
  } catch (error) {
    console.error('Не удалось уведомить сервер о выходе:', error)
  }
}

  return { 
    user, 
    isAuthenticated, 
    login, 
    logout 
  }
})