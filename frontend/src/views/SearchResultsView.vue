<template>
  <div class="search-layout">
    <div class="search-header">
      <h2>Поиск по: <span class="highlight">"{{ searchQuery }}"</span></h2>
      
      <button 
        v-if="!loading && results.length > 0" 
        @click="toggleSortOrder" 
        class="btn-sort"
      >
        Календарь: {{ sortOrder === 'desc' ? 'Сначала новые' : 'Сначала старые' }}
      </button>
    </div>

    <div v-if="loading" class="loading-state">Ищем пасты...</div>

    <div v-else-if="results.length === 0" class="empty-state">
      <p>К сожалению, по вашему запросу ничего не найдено.</p>
    </div>

    <table v-else class="search-table">
      <thead>
        <tr>
          <th class="col-name">Название</th>
          <th class="col-author">Автор</th>
          <th class="col-posted">Дата публикации</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="paste in sortedResults" :key="paste.id || paste.shortId">
          <td class="col-name">
            <span class="paste-title" @click="viewPaste(paste.id || paste.shortId)">
              {{ paste.title || 'Без названия' }}
            </span>
          </td>
          <td class="col-author">{{ paste.author || 'Гость' }}</td>
          <td class="col-posted">{{ formatDate(paste.date || paste.createdAt || paste.created_at || paste.timestamp) }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const searchQuery = ref('')
const loading = ref(true)
const results = ref([])

// Направление сортировки: 'desc' (от новых к старым) или 'asc' (от старых к новым)
const sortOrder = ref('desc')

const apiBase = computed(() => import.meta.env.VITE_API_URL || 'http://localhost:80')

const sortedResults = computed(() => {
  return [...results.value].sort((a, b) => {
    const dateA = new Date(a.date || a.createdAt || a.created_at || a.timestamp)
    const dateB = new Date(b.date || b.createdAt || b.created_at || b.timestamp)
    
    if (sortOrder.value === 'desc') {
      return dateB - dateA
    } else {
      return dateA - dateB
    }
  })
})
const toggleSortOrder = () => {
  sortOrder.value = sortOrder.value === 'desc' ? 'asc' : 'desc'
}

// --- БОЕВАЯ ЛОГИКА ПОИСКА ЧЕРЕЗ OPENSEARCH ---
const performSearch = async () => {
  searchQuery.value = route.query.q || ''
  
  if (!searchQuery.value.trim()) {
    results.value = []
    loading.value = false
    return
  }

  loading.value = true

  try {
    const response = await fetch(`${apiBase.value}/api/pastes/search?q=${encodeURIComponent(searchQuery.value)}`)
    
    if (!response.ok) {
      throw new Error('Ошибка при ответе сервера')
    }
    
    results.value = await response.json()
  } catch (error) {
    console.error('Сбой поиска через бэкенд:', error)
    results.value = []
  } finally {
    loading.value = false
  }
}

const viewPaste = (id) => {
  router.push(`/paste/${id}`)
}

// СОВРЕМЕННЫЙ ФОРМАТ ДАТЫ И ВРЕМЕНИ
const formatDate = (dateString) => {
  if (!dateString) return '—'
  
  const date = new Date(dateString)
  if (isNaN(date.getTime())) return '—'

  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')

  return `${day}.${month}.${year} в ${hours}:${minutes}`
}

onMounted(performSearch)
watch(() => route.query.q, performSearch)
</script>

<style scoped>
/* Убираем жесткие рамки, возвращаем естественные отступы */
.search-layout { 
  width: 100%;
  padding: 20px; 
}

.search-header { 
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px; 
  padding-bottom: 15px; 
  border-bottom: 1px solid #eee; 
}

.search-header h2 { 
  color: #333; 
  margin: 0; 
  font-size: 24px;
}

.highlight { 
  color: #1b5e20; 
  font-style: italic; 
}

/* КНОПКА СОРТИРОВКИ (с легким отступом влево от края сетки) */
.btn-sort {
  background-color: #fff;
  border: 1px solid #1b5e20;
  color: #1b5e20;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: bold;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  margin-right: 15px; /* Отодвигаем кнопку от правого края, чтобы она стояла вровень с таблицей */
}

.btn-sort:hover {
  background-color: #e8f5e9;
  border-color: #a5d6a7;
}

/* ПЛОСКАЯ ТАБЛИЦА НА ВСЮ ШИРИНУ КОНТЕЙНЕРА */
.search-table { 
  width: 100%; 
  border-collapse: collapse; 
  background-color: transparent; /* Никаких лишних белых коробок и теней */
}

.search-table thead tr { 
  background-color: #f8f9fa; 
}

.search-table th { 
  text-align: left; 
  padding: 12px 15px; 
  color: #333; 
  font-weight: bold; 
  font-size: 14px; 
  border-bottom: 1px solid #dee2e6; 
}

.search-table thead tr th:first-child {
  border-top-left-radius: 4px;
}
.search-table thead tr th:last-child {
  border-top-right-radius: 4px;
}

.search-table td { 
  padding: 12px 15px; 
  border-bottom: 1px solid #eee; 
  font-size: 14px; 
  vertical-align: middle; 
}

/* Четкое распределение колонок один в один как в архиве */
.col-name { width: 50%; }
.col-author { width: 25%; color: #1b5e20; font-weight: bold; }
.col-posted { 
  width: 25%; 
  color: #666; 
  font-variant-numeric: tabular-nums; 
}

.paste-title { 
  color: #1b5e20; 
  font-weight: bold; 
  cursor: pointer; 
}

.paste-title:hover { 
  text-decoration: underline; 
}

.loading-state, .empty-state { 
  text-align: center; 
  padding: 40px; 
  color: #666; 
  font-size: 16px; 
}
</style>