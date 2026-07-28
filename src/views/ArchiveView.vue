<template>
  <div class="archive-layout">
    <div class="archive-header">
      <h2>Последние публичные пасты</h2>
      
      <button 
        v-if="!loading && recentPastes.length > 0" 
        @click="toggleSortOrder" 
        class="btn-sort"
      >
        Календарь: {{ sortOrder === 'desc' ? 'Сначала новые' : 'Сначала старые' }}
      </button>
    </div>

    <div v-if="loading" class="loading-state">
      Загрузка архива...
    </div>

    <table v-else-if="sortedPastes.length > 0" class="archive-table">
      <thead>
        <tr>
          <th class="col-name">Название</th>
          <th class="col-author">Автор</th>
          <th class="col-posted">Дата создания</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="paste in sortedPastes" :key="paste.shortId">
          <td class="col-name">
            <router-link :to="'/paste/' + paste.shortId" class="paste-link">
              {{ paste.title || paste.shortId || 'Untitled' }}
            </router-link>
          </td>
          <td class="col-author">{{ paste.author || 'Аноним' }}</td>
          <td class="col-posted">{{ formatDate(paste.createdAt) }}</td>
        </tr>
      </tbody>
    </table>

    <div v-if="!loading && recentPastes.length === 0" class="empty-state">
      Публичных паст пока не найдено.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'

const recentPastes = ref([])
const loading = ref(true)

// Направление сортировки: 'desc' (от новых к старым) или 'asc' (от старых к новым)
const sortOrder = ref('desc')

const apiBase = computed(() => import.meta.env.VITE_API_URL || 'http://localhost:80')

// 🔥 ЧИСТАЯ СОРТИРОВКА ПО ДАТЕ
const sortedPastes = computed(() => {
  // Копируем массив через спред [...], чтобы не мутировать оригинальный refs
  return [...recentPastes.value].sort((a, b) => {
    const dateA = new Date(a.createdAt)
    const dateB = new Date(b.createdAt)
    
    if (sortOrder.value === 'desc') {
      return dateB - dateA // Новые сверху
    } else {
      return dateA - dateB // Старые сверху
    }
  })
})

const toggleSortOrder = () => {
  sortOrder.value = sortOrder.value === 'desc' ? 'asc' : 'desc'
}

const fetchArchive = async () => {
  loading.value = true
  try {
    const response = await fetch(`${apiBase.value}/api/pastes/allPastes`)
    if (!response.ok) throw new Error('Ошибка загрузки архива')
    recentPastes.value = await response.json()
  } catch (error) {
    console.error('Ошибка при получении архива:', error)
    recentPastes.value = []
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '—'
  
  const date = new Date(dateString)
  // Проверка на валидность даты, чтобы избежать ошибок Invalid Date
  if (isNaN(date.getTime())) return '—'

  // Вытаскиваем элементы даты и дополняем нулями слева, если нужно
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')

  // Возвращаем стильный минималистичный вариант
  return `${day}.${month}.${year} в ${hours}:${minutes}`
}

onMounted(fetchArchive)
</script>

<style scoped>
.archive-layout {
  width: 100%;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.archive-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.archive-header h2 {
  color: #1b5e20;
  margin: 0;
  font-size: 24px;
  font-weight: bold;
}

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
  
  margin-right: 15px; 
}

.btn-sort:hover {
  background-color: #e8f5e9;
  border-color: #a5d6a7;
}

/* --- ТАБЛИЦА --- */
.archive-table {
  width: 100%;
  border-collapse: collapse;
  background-color: white;
}

.archive-table thead tr {
  background-color: #f8f9fa;
}

.archive-table th {
  text-align: left;
  padding: 12px 15px;
  color: #333;
  font-weight: bold;
  font-size: 14px;
  border-bottom: 1px solid #dee2e6;
}

.archive-table thead tr th:first-child {
  border-top-left-radius: 4px;
}
.archive-table thead tr th:last-child {
  border-top-right-radius: 4px;
}

.archive-table td {
  padding: 12px 15px;
  border-bottom: 1px solid #eee;
  font-size: 14px;
  vertical-align: middle;
}

.col-name { width: 50%; }
.col-author { width: 25%; color: #1b5e20; font-weight: bold; }
.col-posted { width: 25%; color: #666; }

.paste-link {
  color: #1b5e20;
  text-decoration: none;
  font-weight: bold;
}

.paste-link:hover {
  text-decoration: underline;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 40px;
  color: #666;
}
</style>