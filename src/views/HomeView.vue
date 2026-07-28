<template>
  <div class="home-layout">
    <div class="content-left">
      <PasteArea v-model="pasteText" />
      <PasteSettings v-model="pasteSettings" />

      <div class="submit-wrapper">
        <input 
          v-model="pastePassword"
          type="password"
          placeholder="Установить пароль"
          class="input-password"
          :disabled="isSubmitting"
        />

        <button 
          type="button"
          class="btn-visibility"
          :class="{ 'is-unlisted': isUnlisted }"
          @click="toggleVisibility"
          :disabled="isSubmitting"
        >
          <span v-if="isUnlisted">Доступ по ссылке (Unlisted)</span>
          <span v-else>Публичная (Public)</span>
        </button>

        <button 
          class="btn-submit" 
          @click="sendDataToServer"
          :disabled="isSubmitting"
        >
          {{ isSubmitting ? 'Создание...' : 'Create New Paste' }}
        </button>
      </div>
    </div>

    <aside class="content-right">
      <RightSidebar />
    </aside>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

import PasteArea from '../components/PasteArea.vue'
import PasteSettings from '../components/PasteSettings.vue'
import RightSidebar from '../components/RightSidebar.vue'

const router = useRouter()
const isSubmitting = ref(false)
const apiBase = computed(() => import.meta.env.VITE_API_URL || window.location.origin)

const pasteText = ref('')
const pastePassword = ref('') // 🔥 Реактивная переменная для пароля
const isUnlisted = ref(false)

const toggleVisibility = () => {
  isUnlisted.value = !isUnlisted.value
}

const pasteSettings = ref({
  syntax: 'None',
  expiration: 'Never'
})

const calculateExpirationTime = (expirationText) => {
  if (expirationText === 'Never' || expirationText === 'Burn after read') {
    return null;
  }

  const now = new Date()
  if (expirationText === '10 Minutes') now.setMinutes(now.getMinutes() + 10)
  if (expirationText === '1 Hour') now.setHours(now.getHours() + 1)
  if (expirationText === '1 Day') now.setDate(now.getDate() + 1)

  const tzOffset = now.getTimezoneOffset() * 60000;
  const localIsoTime = new Date(now - tzOffset).toISOString().slice(0, 19);
  
  return localIsoTime;
}

const sendDataToServer = async () => {
  if (pasteText.value.trim() === '') {
    alert('Пожалуйста, введите код пасты!')
    return
  }

  isSubmitting.value = true

  // СОБИРАЕМ PAYLOAD ДЛЯ БЭКЕНДА
  const payload = {
    content: pasteText.value,
    syntax: pasteSettings.value.syntax,
    expirationTime: calculateExpirationTime(pasteSettings.value.expiration),
    burnAfterRead: pasteSettings.value.expiration === 'Burn after read',
    visibility: isUnlisted.value ? 'UNLISTED' : 'PUBLIC',
    password: pastePassword.value.trim() || null // 🔥 Добавили отправку пароля (если пустой — шлем null)
  }

  try {
    const token = localStorage.getItem('jwt')

    const response = await fetch(`${apiBase.value}/api/pastes/create`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      },
      body: JSON.stringify(payload)
    })

    if (!response.ok) throw new Error('Ошибка при создании пасты')

    const result = await response.json()
    
    // Очищаем пароль после успешного создания
    pastePassword.value = ''
    
    router.push(`/paste/${result.short_url}`)

  } catch (error) {
    console.error('Ошибка создания:', error)
    alert('Не удалось создать пасту. Проверьте соединение с сервером.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.home-layout { display: flex; gap: 30px; }
.content-left { flex: 7; min-width: 0; }
.content-right { flex: 3; border-left: 1px solid #eee; padding-left: 20px; }
.submit-wrapper { margin-top: 25px; display: flex; justify-content: flex-end; gap: 15px; align-items: center; }

/* 🔥 КРАСИВЫЙ СТИЛЬ ИНПУТА ДЛЯ ПАРОЛЯ */
.input-password {
  padding: 10px 15px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 3px;
  outline: none;
  width: 260px; /* Фиксированная комфортная ширина */
  transition: all 0.2s;
}
.input-password:focus {
  border-color: #1b5e20; /* Зеленый акцент проекта при фокусе */
  box-shadow: 0 0 4px rgba(27, 94, 32, 0.15);
}

/* Базовый стиль кнопки видимости */
.btn-visibility {
  background-color: #fff;
  border: 1px solid #ccc;
  padding: 10px 15px;
  font-size: 14px;
  color: #555;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-visibility:hover:not(:disabled) {
  background-color: #f5f5f5;
  border-color: #b5b5b5;
}

/* Зеленый стиль для Unlisted */
.btn-visibility.is-unlisted {
  background-color: #e8f5e9; 
  border-color: #a5d6a7;     
  color: #1b5e20;            
}
.btn-visibility.is-unlisted:hover:not(:disabled) {
  background-color: #c8e6c9; 
}

.btn-submit { background-color: #1b5e20; border: 1px solid #144316; padding: 10px 20px; font-size: 14px; font-weight: bold; color: #fff; border-radius: 3px; cursor: pointer; transition: background-color 0.2s; }
.btn-submit:hover:not(:disabled) { background-color: #144316; }
.btn-submit:disabled, .btn-visibility:disabled, .input-password:disabled { opacity: 0.7; cursor: not-allowed; }
</style>