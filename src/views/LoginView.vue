<template>
  <div class="login-wrapper">
    <div class="login-card">

      <div class="login-header">
        <div class="mini-logo">
          <span>0011</span>
          <span>1000</span>
          <span>101</span>
        </div>

        <h1>Вход в PolyPaste</h1>

        <p class="subtitle">
          Используйте социальные сети для быстрого доступа
        </p>
      </div>

      <div class="social-actions">

        <button
          class="btn-social btn-vk"
          @click="handleSocialLogin('vk')"
          :disabled="isConnecting"
        >
          <span class="icon">VK</span>

          <span v-if="loadingProvider !== 'vk'">
            Войти через ВКонтакте
          </span>

          <span v-else>
            Подключение...
          </span>
        </button>

        <button
          class="btn-social btn-yandex"
          @click="handleSocialLogin('yandex')"
          :disabled="isConnecting"
        >
          <span class="icon">Y</span>

          <span v-if="loadingProvider !== 'yandex'">
            Войти через Яндекс
          </span>

          <span v-else>
            Подключение...
          </span>
        </button>

      </div>

      <div class="login-footer">
        <p>
          Авторизуясь, вы соглашаетесь с правилами сервиса
        </p>

        <p v-if="apiBase">
          API: <code>{{ apiBase }}</code>
        </p>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const isConnecting = ref(false)
const loadingProvider = ref(null)

const apiBase = computed(
  () => import.meta.env.VITE_API_URL || window.location.origin
)

/**
 * Обработка возврата после OAuth
 */
onMounted(() => {
  const token = route.query.token
  const error = route.query.error

  // Извлекаем и декодируем данные из URL
  // decodeURIComponent нужен, чтобы превратить %D0%9B... обратно в "Леонид"
  // и восстановить спецсимволы в ссылке на аватарку
  const username = route.query.username ? decodeURIComponent(route.query.username) : 'User'
  const avatar = route.query.avatar ? decodeURIComponent(route.query.avatar) : null
  const provider = route.query.provider || 'social'

  if (error) {
    console.error('OAuth error:', error)
    alert('Ошибка при входе через соцсеть')
    return
  }

  if (token) {
    // 1. Сохраняем JWT в браузер
    localStorage.setItem('jwt', token)

    // 2. Записываем ВСЕ данные в AuthStore
    auth.login({
      username: username,
      avatar: avatar,   // Вот она, твоя аватарка!
      provider: provider
    })

    // 3. Улетаем на главную, очищая query-параметры из строки браузера
    router.replace('/')
  }
})

/**
 * Старт процесса OAuth
 */
const handleSocialLogin = (provider) => {
  isConnecting.value = true
  loadingProvider.value = provider

  // Редирект на бэкенд для начала авторизации
  window.location.href = `/auth/${provider}/login`
}
</script>

<style scoped>
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding-top: 50px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  text-align: center;
}

.mini-logo {
  display: inline-flex;
  flex-direction: column;
  background-color: #fbc02d;
  color: black;
  font-size: 8px;
  font-family: monospace;
  padding: 6px;
  border-radius: 3px;
  margin-bottom: 15px;
  transform: rotate(-5deg);
  font-weight: bold;
}

h1 {
  color: #1b5e20;
  font-size: 24px;
  margin: 0 0 10px 0;
}

.subtitle {
  color: #666;
  font-size: 14px;
  line-height: 1.4;
}

.social-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 25px;
}

.btn-social {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 14px;
  border: none;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
  color: white;
  transition: filter 0.2s;
}

.btn-social:hover:not(:disabled) {
  filter: brightness(1.1);
}

.btn-social:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-vk { background-color: #0077FF; }
.btn-yandex { background-color: #f33; }

.icon {
  background: rgba(255, 255, 255, 0.2);
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  font-size: 12px;
}

.login-footer {
  margin-top: 35px;
  font-size: 12px;
  color: #999;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

code {
  background: #eee;
  padding: 2px 4px;
  border-radius: 3px;
  color: #333;
}
</style>