<template>
  <div class="edit-view">
    <div class="page-header">
      <h2>Редактирование пасты</h2>
    </div>

    <div v-if="isLoading" class="loading">Загрузка данных пасты...</div>

    <div v-else-if="pasteData" class="editor-layout">
      <div class="left-col">
        <PasteArea v-model="updatedContent" title="Редактирование кода" />
      </div>
      
      <div class="right-col">
        <PasteSettings v-model="updatedSettings" />
        
        <button 
          class="btn-save" 
          @click="saveChanges" 
          :disabled="isSaving"
        >
          {{ isSaving ? 'Сохранение...' : 'Изменить пасту' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

import PasteArea from '@/components/PasteArea.vue'
import PasteSettings from '@/components/PasteSettings.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const isLoading = ref(true)
const isSaving = ref(false)
const pasteData = ref(null)

const updatedContent = ref('')
const updatedSettings = ref({})

const apiBase = computed(() => import.meta.env.VITE_API_URL || window.location.origin)

// 1. БОЕВАЯ ФУНКЦИЯ ПОЛУЧЕНИЯ ДАННЫХ
const fetchPasteForEdit = async () => {
  isLoading.value = true
  const id = route.params.id
  
  try {
    // Стучимся на бэкенд за метаданными и текстом из MinIO
    const response = await fetch(`${apiBase.value}/api/pastes/${id}`, {
      headers: { 
        'Authorization': `Bearer ${auth.token || localStorage.getItem('jwt')}` 
      }
    })
    
    if (!response.ok) {
      if (response.status === 403) {
        alert('У вас нет прав на редактирование этой пасты!')
      } else {
        alert('Паста не найдена')
      }
      router.push('/my-pastes')
      return
    }
    
    const result = await response.json()
    pasteData.value = result
    
    // Распределяем данные по реактивным переменным
    updatedContent.value = result.content
    
    // Передаем настройки в дочерний компонент PasteSettings
    updatedSettings.value = {
      title: result.title,
      syntax: result.syntax,
      visibility: result.visibility || 'PUBLIC'
    }
    
  } catch (error) {
    console.error('Ошибка при загрузке пасты:', error)
    alert('Не удалось загрузить данные для редактирования')
    router.push('/my-pastes')
  } finally {
    isLoading.value = false
  }
}

// 2. БОЕВАЯ ФУНКЦИЯ ОТПРАВКИ PUT-ЗАПРОСА
const saveChanges = async () => {
  if (!updatedContent.value.trim()) {
    alert('Текст пасты не может быть пустым!')
    return
  }

  isSaving.value = true
  const id = route.params.id

  // Формируем чистый payload строго под структуру нашего PasteUpdateRequest в Spring Boot
  const payload = {
    title: updatedSettings.value.title || 'Без названия',
    content: updatedContent.value,
    syntax: updatedSettings.value.syntax || 'None',
    visibility: updatedSettings.value.visibility || 'PUBLIC'
  }

  try {
    const response = await fetch(`${apiBase.value}/api/pastes/${id}`, {
      method: 'PUT', // Наш эндпоинт @PutMapping("/{shortUrl}")
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${auth.token || localStorage.getItem('jwt')}`
      },
      body: JSON.stringify(payload)
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.error || 'Ошибка при сохранении')
    }
    
    // Возвращаем пользователя на страницу просмотра измененной пасты
    router.push(`/paste/${id}`)
    
  } catch (error) {
    console.error('Ошибка обновления пасты:', error)
    alert(error.message || 'Не удалось сохранить изменения')
  } finally {
    isSaving.value = false
  }
}

onMounted(fetchPasteForEdit)
</script>

<style scoped>
.edit-view { padding: 20px; }
.page-header { margin-bottom: 20px; color: #1b5e20; border-bottom: 2px solid #eee; }
.loading { padding: 50px; text-align: center; color: #666; }
.editor-layout { display: flex; gap: 20px; }
.left-col { flex: 7; }
.right-col { flex: 3; display: flex; flex-direction: column; gap: 20px; }
.btn-save { background-color: #43a047; color: white; border: none; padding: 12px; border-radius: 4px; font-weight: bold; cursor: pointer; font-size: 16px; transition: background-color 0.2s; }
.btn-save:hover:not(:disabled) { background-color: #2e7d32; }
.btn-save:disabled { opacity: 0.7; cursor: not-allowed; }
</style>