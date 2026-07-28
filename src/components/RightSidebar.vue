<template>
  <aside class="sidebar">
    
    <div v-if="auth.isAuthenticated" class="sidebar-section">
      <h3 class="sidebar-title">My Pastes</h3>
      
      <p v-if="myPastes.length === 0" class="empty-state">
        Nothing here yet...
      </p>
      
      <ul v-else class="pastes-list">
        <li v-for="paste in myPastes" :key="paste.shortId" class="paste-item">
          <div class="paste-info">
            <router-link :to="'/paste/' + paste.shortId" class="paste-link">
              {{ paste.title || 'Untitled Paste' }}
            </router-link>
            <span class="paste-meta">{{ formatDate(paste.createdAt) }}</span>
          </div>
        </li>
      </ul>
    </div>

    <div class="sidebar-section">
      <h3 class="sidebar-title">Public Pastes</h3>
      
      <ul v-if="publicPastes.length > 0" class="pastes-list">
        <li v-for="paste in publicPastes" :key="paste.shortId" class="paste-item">
          <div class="paste-info">
            <router-link :to="'/paste/' + paste.shortId" class="paste-link">
              {{ paste.title || 'Untitled' }}
            </router-link>
            <span class="paste-meta">
              {{ paste.author || 'Guest' }} | {{ formatDate(paste.createdAt) }}
            </span>
          </div>
        </li>
      </ul>
      <p v-else class="empty-text">No public pastes</p>
    </div>

  </aside>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const myPastes = ref([])
const publicPastes = ref([])

// Динамический URL бэкенда
const apiBase = computed(() => import.meta.env.VITE_API_URL || 'http://localhost:80')

const loadSidebarData = async () => {
  try {
    // 1. ЗАГРУЖАЕМ ПУБЛИЧНЫЕ ПАСТЫ
    const publicRes = await fetch(`${apiBase.value}/api/pastes/allPastes`)
    if (publicRes.ok) {
      const data = await publicRes.json()
      // Ограничиваем список (например, последние 7 штук)
      publicPastes.value = data.slice(0, 7)
    }

    // 2. ЗАГРУЖАЕМ "МОИ ПАСТЫ"
    if (auth.isAuthenticated) {
      const token = localStorage.getItem('jwt') // берем токен напрямую или из auth.token
      const myRes = await fetch(`${apiBase.value}/api/pastes/myPastes`, {
        headers: {
          'Authorization': `Bearer ${token}` 
        }
      })
      if (myRes.ok) {
        const data = await myRes.json()
        myPastes.value = data.slice(0, 5)
      }
    } else {
      myPastes.value = []
    }

  } catch (error) {
    console.error('Ошибка при загрузке сайдбара с сервера:', error)
  }
}

// Форматирование даты, чтобы не было длинной строки ISO
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })
}

// Запускаем при монтировании
onMounted(loadSidebarData)

// Если статус входа изменился (залогинился или вышел) — обновляем данные
watch(() => auth.isAuthenticated, () => {
  loadSidebarData()
})
</script>

<style scoped>
.sidebar { font-family: Arial, sans-serif; width: 100%; }
.sidebar-section { margin-bottom: 30px; }
.sidebar-title { 
  font-size: 14px; 
  color: #333; 
  margin: 0 0 10px 0; 
  border-bottom: 1px dotted #ccc; 
  padding-bottom: 5px; 
  font-weight: bold; 
}
.empty-state, .empty-text { color: #999; font-size: 12px; font-style: italic; margin: 0; padding: 5px 0; }
.pastes-list { list-style: none; padding: 0; margin: 0; }
.paste-item { 
  padding: 6px 0; 
  border-bottom: 1px dotted #eee; 
}
.paste-item:last-child { border-bottom: none; }
.paste-info { display: flex; flex-direction: column; }
.paste-link { 
  color: #c62828; /* Тот самый темно-красный */
  text-decoration: none; 
  font-size: 13px; 
  font-weight: bold; 
  line-height: 1.2;
}
.paste-link:hover { text-decoration: underline; }
.paste-meta { font-size: 11px; color: #888; margin-top: 2px; }
</style>