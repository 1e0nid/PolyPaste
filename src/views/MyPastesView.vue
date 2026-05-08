<template>
  <div class="pastes-container">
    <div class="page-header">
      <h2>Мои пасты</h2>
      <button class="btn-create" @click="$router.push('/')">+ Создать новую</button>
    </div>

    <div v-if="loading" class="loading-state">Загрузка данных...</div>

    <div v-else-if="pastes.length === 0" class="empty-state">
      <p>У вас еще нет созданных паст.</p>
    </div>

    <div v-else class="pastes-list">
      <table class="pastes-table">
        <thead>
          <tr>
            <th>Название</th>
            <th>Дата создания</th>
            <th>Срок хранения</th>
            <th>Действия</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="paste in pastes" :key="paste.id">
            <td class="paste-title" @click="viewPaste(paste.id)">
              {{ paste.title || 'Без названия' }}
            </td>
            <td>{{ formatDate(paste.created_at) }}</td>
            <td>{{ paste.expires_at || 'Бессрочно' }}</td>
            <td class="actions">
              <button class="btn-icon" title="Редактировать">✏️</button>
              <button class="btn-icon delete" title="Удалить">🗑️</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(true)
const pastes = ref([])

// Используем твой динамический домен для будущих запросов
const apiBase = computed(() => import.meta.env.VITE_API_URL || window.location.origin)

const fetchPastes = async () => {
  loading.value = true
  
  // ИМИТАЦИЯ: В будущем здесь будет fetch(`${apiBase.value}/api/my-pastes`)
  setTimeout(() => {
    pastes.value = [
      { id: 1, title: 'SQL запрос для Car Service', created_at: '2026-04-10T12:00:00', expires_at: '1 месяц' },
      { id: 2, title: 'Лабораторная работа №3 (C++)', created_at: '2026-05-01T09:30:00', expires_at: 'Бессрочно' },
      { id: 3, title: 'Черновик статьи по оптимизации', created_at: '2026-05-07T15:45:00', expires_at: '1 неделя' }
    ]
    loading.value = false
  }, 800)
}

const viewPaste = (id) => {
  router.push(`/view/${id}`)
}

const formatDate = (dateString) => {
  const options = { year: 'numeric', month: 'short', day: 'numeric' }
  return new Date(dateString).toLocaleDateString('ru-RU', options)
}

onMounted(() => {
  if (!auth.isAuthenticated) {
    router.push('/login')
  } else {
    fetchPastes()
  }
})
</script>

<style scoped>
.pastes-container {
  padding: 10px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  border-bottom: 2px solid #eee;
  padding-bottom: 15px;
}

h2 {
  color: #1b5e20;
  margin: 0;
}

.btn-create {
  background-color: #43a047;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 50px;
  color: #666;
}

.pastes-table {
  width: 100%;
  border-collapse: collapse;
}

.pastes-table th {
  text-align: left;
  padding: 12px;
  background-color: #f8f9fa;
  color: #333;
  font-size: 14px;
  border-bottom: 1px solid #ddd;
}

.pastes-table td {
  padding: 12px;
  border-bottom: 1px solid #eee;
  font-size: 14px;
}

.paste-title {
  color: #1b5e20;
  font-weight: bold;
  cursor: pointer;
}

.paste-title:hover {
  text-decoration: underline;
}

.actions {
  display: flex;
  gap: 10px;
}

.btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  border-radius: 4px;
}

.btn-icon:hover {
  background-color: #f0f0f0;
}

.btn-icon.delete:hover {
  background-color: #ffebee;
}
</style>