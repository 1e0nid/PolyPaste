<template>
  <div class="view-layout">
    <div class="content-left">
      
      <div v-if="isLoading" class="loading-state">
        Загрузка пасты...
      </div>

      <div v-else-if="isError" class="error-state">
        <h2>404 - Paste Not Found</h2>
        <p>К сожалению, такая паста не существует или была удалена.</p>
        <button class="btn-home" @click="$router.push('/')">На главную</button>
      </div>

      <div v-else-if="pasteData">
        <div class="paste-header">
          <div class="header-main">
            <div class="author-avatar">👤</div>
            <div class="title-meta">
              <h1>{{ pasteData.title || 'Untitled' }}</h1>
              <div class="meta-info">
                <span class="author-name">{{ pasteData.author || 'Guest' }}</span>
                <span class="separator">|</span>
                <span class="date">{{ formatDate(pasteData.createdAt || pasteData.date) }}</span>
              </div>
            </div>
          </div>
          
          <div v-if="!pasteData.isPasswordProtected" class="action-bar">
            <button class="btn-action" @click="copyCode">copy</button>
            <button class="btn-action" @click="downloadFile">download</button>
          </div>
        </div>

        <div v-if="pasteData.isPasswordProtected" class="lock-container">
          <div class="lock-card">
            <div class="lock-icon">🔒</div>
            <h3>Эта паста защищена паролем</h3>
            <p>Введите пароль для получения доступа к содержимому.</p>
            
            <div class="password-form">
              <input 
                v-model="inputPassword" 
                type="password" 
                placeholder="Введите пароль..." 
                @keyup.enter="unlockPaste"
                :class="{ 'input-error': passwordError }"
              />
              <button class="btn-unlock" @click="unlockPaste">Открыть</button>
            </div>
            
            <div v-if="passwordError" class="password-error-msg">
              {{ passwordError }}
            </div>
          </div>
        </div>

        <div v-else class="code-container">
          <div class="code-header">
            <span class="lang-label">{{ pasteData.language || 'Plain Text' }}</span>
            <span class="size-label">{{ pasteData.size || '0 B' }}</span>
          </div>
          
          <div class="code-body">
            <div class="line-numbers" aria-hidden="true">
              <span v-for="n in lineCount" :key="n">{{ n }}</span>
            </div>
            
            <pre><code 
              ref="codeBlock" 
              :class="codeClass"
            >{{ pasteData.content }}</code></pre>
          </div>
        </div>
      </div>
    </div>

    <aside class="content-right">
      <RightSidebar />
    </aside>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import RightSidebar from '../components/RightSidebar.vue'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css' 

const route = useRoute()
const codeBlock = ref(null)

const isLoading = ref(false)
const isError = ref(false)
const pasteData = ref(null)

// Поля для работы с паролем
const inputPassword = ref('')
const passwordError = ref('')

const apiBase = computed(() => import.meta.env.VITE_API_URL || 'http://localhost:80')

// Приводим язык к нижнему регистру для Highlight.js
const codeClass = computed(() => {
  if (!pasteData.value || !pasteData.value.language || pasteData.value.language === 'None') {
    return 'language-plaintext'
  }
  return `language-${pasteData.value.language.toLowerCase()}`
})

// Считаем количество строк
const lineCount = computed(() => {
  if (!pasteData.value || !pasteData.value.content) return 0
  return pasteData.value.content.split('\n').length
})

// ФУНКЦИЯ ПОДСВЕТКИ
const applyHighlight = async () => {
  await nextTick() 
  if (codeBlock.value && pasteData.value && !pasteData.value.isPasswordProtected) {
    delete codeBlock.value.dataset.highlighted
    hljs.highlightElement(codeBlock.value)
  }
}

// ОБЫЧНАЯ ЗАГРУЗКА ПАСТЫ
const loadRealPaste = async () => {
  isLoading.value = true
  isError.value = false
  inputPassword.value = ''
  passwordError.value = ''
  
  const id = route.params.id
  const token = localStorage.getItem('jwt') 

  try {
    const response = await fetch(`${apiBase.value}/api/pastes/${id}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      }
    })
    
    if (!response.ok) throw new Error('Not Found')

    const data = await response.json()
    pasteData.value = data
    
    // Подсветка сработает только если бэкенд отдал открытую пасту
    applyHighlight()

  } catch (error) {
    console.error('Ошибка загрузки:', error)
    isError.value = true
  } finally {
    isLoading.value = false
  }
}

// 🔥 ФУНКЦИЯ ОТПРАВКИ ПАРОЛЯ НА БЭКЕНД
const unlockPaste = async () => {
  if (!inputPassword.value.trim()) {
    passwordError.value = 'Пожалуйста, введите пароль'
    return
  }
  passwordError.value = ''
  
  const id = route.params.id
  const token = localStorage.getItem('jwt')

  try {
    const response = await fetch(`${apiBase.value}/api/pastes/${id}/unlock`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      },
      body: JSON.stringify({ password: inputPassword.value })
    })

    if (!response.ok) {
      if (response.status === 403 || response.status === 401) {
        passwordError.value = 'Неверный пароль! Попробуйте еще раз.'
      } else {
        passwordError.value = 'Ошибка проверки пароля сервером'
      }
      return
    }

    // Бэкенд проверил хэш и прислал полноценную пасту с текстом из MinIO
    const data = await response.json()
    pasteData.value = data
    
    // Очищаем форму и запускаем подсветку синтаксиса
    inputPassword.value = ''
    applyHighlight()

  } catch (error) {
    console.error('Ошибка разблокировки:', error)
    passwordError.value = 'Не удалось связаться с сервером'
  }
}

onMounted(loadRealPaste)

watch(() => route.params.id, loadRealPaste)

watch(pasteData, () => {
  applyHighlight()
}, { deep: true })

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('ru-RU', { 
    day: 'numeric', month: 'long', year: 'numeric' 
  })
}

const copyCode = async () => {
  if (!pasteData.value || pasteData.value.isPasswordProtected) return
  await navigator.clipboard.writeText(pasteData.value.content)
  alert('Скопировано!')
}

const downloadFile = () => {
  if (!pasteData.value || pasteData.value.isPasswordProtected) return
  const blob = new Blob([pasteData.value.content], { type: 'text/plain' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${pasteData.value.title || 'paste'}.txt`
  a.click()
  window.URL.revokeObjectURL(url)
}
</script>

<style scoped>
/* Общий макет */
.view-layout { display: flex; gap: 30px; padding: 20px; max-width: 1200px; margin: 0 auto; }
.content-left { flex: 7; min-width: 0; }
.content-right { flex: 3; }

/* Шапка пасты */
.paste-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.header-main { display: flex; gap: 15px; align-items: center; }
.author-avatar { width: 45px; height: 45px; background: #fbc02d; border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.title-meta h1 { font-size: 22px; margin: 0 0 5px 0; color: #333; }
.meta-info { font-size: 13px; color: #666; }
.separator { margin: 0 8px; color: #ccc; }

/* Кнопки действий */
.action-bar { display: flex; gap: 8px; }
.btn-action { 
  padding: 4px 10px; 
  background: #f5f5f5; 
  border: 1px solid #ccc; 
  border-radius: 3px; 
  cursor: pointer; 
  font-size: 12px; 
  color: #333;
}
.btn-action:hover { background: #e0e0e0; }

/* 🔒 СТИЛИ ДЛЯ КАРТОЧКИ С ЗАМОЧКОМ */
.lock-container {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
.lock-card {
  background: #ffffff;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  padding: 30px;
  text-align: center;
  max-width: 420px;
  width: 100%;
}
.lock-icon {
  font-size: 46px;
  margin-bottom: 15px;
}
.lock-card h3 {
  margin: 0 0 10px 0;
  color: #1b5e20;
  font-size: 18px;
  font-weight: bold;
}
.lock-card p {
  margin: 0 0 20px 0;
  color: #666;
  font-size: 14px;
}
.password-form {
  display: flex;
  gap: 10px;
}
.password-form input {
  flex: 1;
  padding: 9px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}
.password-form input:focus {
  border-color: #1b5e20;
}
.password-form input.input-error {
  border-color: #d32f2f;
  background-color: #fff8f8;
}
.btn-unlock {
  background: #1b5e20;
  color: white;
  border: none;
  padding: 9px 18px;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}
.btn-unlock:hover {
  background: #144316;
}
.password-error-msg {
  color: #d32f2f;
  font-size: 13px;
  margin-top: 12px;
  text-align: left;
  font-weight: bold;
}

/* Контейнер кода */
.code-container { 
  border: 1px solid #ddd; 
  border-radius: 4px; 
  overflow: hidden; 
  background: #fff;
}
.code-header { 
  background: #f8f8f8; 
  padding: 8px 15px; 
  display: flex; 
  justify-content: space-between; 
  font-size: 12px; 
  border-bottom: 1px solid #ddd;
  color: #777;
}

/* Стилизация тела кода и номеров */
.code-body { 
  display: flex; 
  align-items: stretch;
}

.line-numbers { 
  padding: 10px 0; 
  background: #f5f5f5; 
  text-align: right; 
  color: #999; 
  border-right: 1px solid #ddd; 
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  line-height: 1.5; 
  user-select: none; 
  min-width: 45px;
}

.line-numbers span { 
  display: block; 
  padding: 0 10px; 
}

pre { 
  margin: 0; 
  padding: 10px; 
  flex-grow: 1; 
  overflow-x: auto; 
  background: #fff;
}

code { 
  font-family: 'Courier New', Courier, monospace; 
  font-size: 13px;
  line-height: 1.5; 
  display: block;
  white-space: pre; 
}

.hljs { background: transparent !important; padding: 0 !important; }

/* Состояния */
.loading-state, .error-state { text-align: center; padding: 100px 0; color: #666; }
.btn-home { margin-top: 20px; padding: 10px 20px; cursor: pointer; }
</style>