<template>
  <header class="header">
    
    <router-link to="/archive" class="logo">
      <div class="logo-icon">
        <span>0011</span>
        <span>1000</span>
        <span>101</span>
      </div>
      <span class="logo-text">PolyPaste</span>
    </router-link>

    <div class="actions">
      <button class="btn-paste" @click="$router.push('/')">
        <span class="plus">+</span> new paste
      </button>
      
      <div class="search-bar">
        <input 
          type="text" 
          placeholder="Search..." 
          v-model="searchQuery"
          @keyup.enter="handleSearch"
        />
        <span class="search-icon" @click="handleSearch">🔍</span>
      </div>
    </div>

    <div class="user-section" ref="profileRef">
      
      <div v-if="auth.isAuthenticated" class="profile" @click="toggleDropdown">
        <div class="profile-info">
          <span class="username">{{ auth.user.username }}</span>
          <span class="provider-info">via {{ auth.user.provider || 'polypaste' }}</span>
        </div>
        <div class="avatar">{{ auth.user.avatar || '👤' }}</div>
        <span class="arrow">▼</span>

        <div class="dropdown-menu" v-if="isDropdownOpen">
          <ul>
            <li @click.stop="navigate('/my-pastes')">My Pastes</li>
            <li @click.stop="navigate('/settings')">Settings</li>
            <li class="logout" @click.stop="handleLogout">Log Out</li>
          </ul>
        </div>
      </div>

      <button v-else class="btn-login-header" @click="$router.push('/login')">
        Войти
      </button>
      
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth' // Подключили хранилище друга

const router = useRouter()
const auth = useAuthStore()

// --- ЛОГИКА ПОИСКА ---
const searchQuery = ref('')

const handleSearch = () => {
  if (searchQuery.value.trim() === '') return
  // Оставил твой alert для наглядности (можно заменить на логику друга с console.log)
  alert(`Поиск по запросу "${searchQuery.value}" появится в следующей версии!`)
  searchQuery.value = ''
}

// --- ЛОГИКА МЕНЮ ПРОФИЛЯ ---
const isDropdownOpen = ref(false)
const profileRef = ref(null)

const toggleDropdown = () => {
  isDropdownOpen.value = !isDropdownOpen.value
}

// Функции навигации и выхода от друга
const navigate = (path) => {
  isDropdownOpen.value = false
  router.push(path)
}

const handleLogout = () => {
  auth.logout()
  isDropdownOpen.value = false
  router.push('/login')
}

// Твоя функция закрытия меню по клику вне его области
const handleClickOutside = (event) => {
  if (isDropdownOpen.value && profileRef.value && !profileRef.value.contains(event.target)) {
    isDropdownOpen.value = false
  }
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>

<style scoped>
/* Твоя зафиксированная шапка */
.header {
  position: sticky;
  top: 0;
  z-index: 1000; 
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #1b5e20; 
  color: white;
  padding: 0 20px;
  height: 60px;
  font-family: Arial, sans-serif;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

/* --- ЛОГОТИП (Твои исправления стилей ссылок) --- */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none; 
  color: white; 
}

.logo:visited {
  color: white;
}

.logo-icon {
  background-color: #fbc02d;
  color: #000;
  font-size: 8px;
  font-family: monospace;
  font-weight: bold;
  padding: 4px;
  border-radius: 2px;
  display: flex;
  flex-direction: column;
  line-height: 1;
  transform: rotate(-10deg);
}

.logo-text {
  font-size: 22px;
  font-weight: bold;
  letter-spacing: 2px;
}

/* --- ЦЕНТРАЛЬНАЯ ЧАСТЬ --- */
.actions {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-left: auto;
  margin-right: 30px;
}

.btn-paste {
  background-color: #66bb6a;
  color: white;
  border: none;
  padding: 6px 14px;
  border-radius: 3px;
  font-size: 15px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: background 0.2s;
}

.btn-paste:hover {
  background-color: #81c784;
}

.search-bar {
  position: relative;
  display: flex;
  align-items: center;
}

/* Твоя ширина поиска 250px */
.search-bar input {
  background-color: #0d3b13; 
  border: none;
  padding: 8px 30px 8px 15px;
  border-radius: 4px;
  color: white;
  outline: none;
  width: 180px; /* <--- Измени эту цифру (было 250px) */
  font-size: 14px;
}

.search-bar input::placeholder {
  color: #a5d6a7;
}

.search-icon {
  position: absolute;
  right: 10px;
  font-size: 14px;
  opacity: 0.7;
  cursor: pointer;
}

/* --- ПРОФИЛЬ --- */
.user-section { position: relative; }

.profile {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.profile-info {
  display: flex;
  flex-direction: column;
  text-align: right;
}

.username {
  font-size: 14px;
  font-weight: bold;
}

.provider-info {
  font-size: 10px;
  color: #a5d6a7;
}

.avatar {
  width: 32px;
  height: 32px;
  background-color: #ffcc80;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.arrow {
  font-size: 10px;
  opacity: 0.8;
  padding: 5px;
}

/* --- КНОПКА ВОЙТИ (от друга) --- */
.btn-login-header {
  background: transparent;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.5);
  padding: 6px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.2s;
}

.btn-login-header:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

/* --- ВЫПАДАЮЩЕЕ МЕНЮ --- */
.dropdown-menu {
  position: absolute;
  top: 50px; 
  right: 0;  
  background-color: white;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  min-width: 150px;
  z-index: 1001; 
}

.dropdown-menu ul {
  list-style: none;
  margin: 0;
  padding: 0;
}

.dropdown-menu li {
  border-bottom: 1px solid #eee;
  padding: 12px 15px;
  color: #333;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.dropdown-menu li:last-child {
  border-bottom: none;
}

.dropdown-menu li:hover {
  background-color: #f5f5f5; 
}

.dropdown-menu .logout {
  color: #d32f2f; 
}
</style>