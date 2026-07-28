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
          <tr v-for="paste in pastes" :key="paste.shortId">
            <td class="paste-title" @click="viewPaste(paste.shortId)">
              {{ paste.title || paste.shortId || 'Без названия' }}
            </td>
            <td>{{ formatDate(paste.createdAt) }}</td>
            <td>{{ paste.expiresAt || 'Бессрочно' }}</td>
            <td class="actions">
              <button class="btn-icon" title="Редактировать" @click="editPaste(paste.shortId)">✏️</button>
              <button class="btn-icon delete" title="Удалить" @click="confirmDelete(paste.shortId)">🗑️</button>
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

const apiBase = computed(() => import.meta.env.VITE_API_URL || 'http://localhost:80')

// --- ПОЛУЧЕНИЕ СПИСКА ПАСТ ---
const fetchPastes = async () => {
  loading.value = true
  const token = localStorage.getItem('jwt')

  try {
    const response = await fetch(`${apiBase.value}/api/pastes/myPastes`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    })

    if (response.status === 401 || response.status === 403) {
      router.push('/login')
      return
    }

    if (!response.ok) throw new Error(`Ошибка: ${response.status}`)
    
    pastes.value = await response.json()
  } catch (error) {
    console.error('Ошибка загрузки:', error)
    pastes.value = []
  } finally {
    loading.value = false
  }
}

// --- УДАЛЕНИЕ ПАСТЫ ---
const confirmDelete = async (shortId) => {
  if (!confirm('Вы уверены, что хотите удалить эту пасту?')) return
  
  const token = localStorage.getItem('jwt')
  try {
    const response = await fetch(`${apiBase.value}/api/pastes/${shortId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    if (!response.ok) throw new Error('Ошибка удаления')

    // Фильтруем массив по полю shortId
    pastes.value = pastes.value.filter(p => p.shortId !== shortId)
    alert('Паста успешно удалена')
  } catch (error) {
    console.error(error)
    alert('Не удалось удалить пасту')
  }
}

// Функции перехода (id здесь — это фактически полученный shortId)
const viewPaste = (id) => router.push(`/paste/${id}`)
const editPaste = (id) => router.push(`/edit/${id}`)

const formatDate = (dateString) => {
  if (!dateString) return 'Неизвестно'
  return new Date(dateString).toLocaleDateString('ru-RU', { 
    year: 'numeric', month: 'short', day: 'numeric' 
  })
}

onMounted(() => {
  if (!localStorage.getItem('jwt')) {
    router.push('/login')
  } else {
    fetchPastes()
  }
})
</script>

<style scoped>
.pastes-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; border-bottom: 2px solid #eee; padding-bottom: 15px; }
h2 { color: #1b5e20; margin: 0; }
.btn-create { background-color: #43a047; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; font-weight: bold; }
.pastes-table { width: 100%; border-collapse: collapse; }
.pastes-table th { text-align: left; padding: 12px; background-color: #f8f9fa; border-bottom: 1px solid #ddd; font-size: 14px; }
.pastes-table td { padding: 12px; border-bottom: 1px solid #eee; font-size: 14px; }
.paste-title { color: #1b5e20; font-weight: bold; cursor: pointer; }
.actions { display: flex; gap: 10px; }
.btn-icon { background: none; border: none; cursor: pointer; font-size: 16px; opacity: 0.7; }
.btn-icon:hover { opacity: 1; }
.btn-icon.delete { color: #c62828; }
</style>