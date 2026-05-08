import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', () => {
  // Инициализируем состояние из localStorage, если оно там есть
  const user = ref(JSON.parse(localStorage.getItem('user')) || null)
  
  const isAuthenticated = computed(() => !!user.value)

  function login(userData) {
    // В реальном проекте здесь был бы запрос к API (axios)
    user.value = userData
    localStorage.setItem('user', JSON.stringify(userData))
  }

  function logout() {
    user.value = null
    localStorage.removeItem('user')
  }

  return { user, isAuthenticated, login, logout }
})