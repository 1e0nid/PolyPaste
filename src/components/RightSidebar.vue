<template>
  <aside class="sidebar">
    
    <div class="sidebar-section">
      <h3 class="sidebar-title">My Pastes</h3>
      
      <p v-if="myPastes.length === 0" class="empty-state">
        Nothing here yet...
      </p>
      
      <ul v-else class="pastes-list">
        <li v-for="paste in myPastes" :key="paste.id" class="paste-item">
          <div class="paste-info">
            <router-link :to="'/paste/' + paste.id" class="paste-link">
              {{ paste.title }}
            </router-link>
            <span class="paste-meta">{{ paste.time }} | {{ paste.size }}</span>
          </div>
        </li>
      </ul>
    </div>

    <div class="sidebar-section">
      <h3 class="sidebar-title">Public Pastes</h3>
      
      <ul class="pastes-list">
        <li v-for="paste in publicPastes" :key="paste.id" class="paste-item">
          <div class="paste-info">
            <router-link :to="'/paste/' + paste.id" class="paste-link">
              {{ paste.title }}
            </router-link>
            <span class="paste-meta">{{ paste.time }} | {{ paste.size }}</span>
          </div>
        </li>
      </ul>
    </div>

  </aside>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// Изначально массивы пустые
const myPastes = ref([])
const publicPastes = ref([])

// Функция сработает автоматически при загрузке компонента
onMounted(async () => {
  try {
    // 1. Запрашиваем публичные пасты из нашего локального файла
    const publicResponse = await fetch('/public-pastes.json')
    if (publicResponse.ok) {
      publicPastes.value = await publicResponse.json()
    }

    // 2. Запрашиваем "мои" пасты из второго локального файла
    const myResponse = await fetch('/my-pastes.json') // Заголовки пока можно убрать, локальному файлу они не нужны
    if (myResponse.ok) {
      myPastes.value = await myResponse.json()
    }
    
  } catch (error) {
    console.error('Ошибка при загрузке паст с сервера:', error)
  }
})
</script>

<style scoped>
.sidebar { font-family: Arial, sans-serif; width: 100%; }
.sidebar-section { margin-bottom: 30px; }
.sidebar-title { font-size: 15px; color: #333; margin-bottom: 10px; border-bottom: 1px dotted #ccc; padding-bottom: 5px; font-weight: bold; }
.empty-state { color: #999; font-size: 13px; margin: 0; padding: 10px 0; }
.pastes-list { list-style: none; padding: 0; margin: 0; }
.paste-item { display: flex; align-items: flex-start; gap: 8px; padding: 8px 0; border-bottom: 1px dotted #eee; }
.paste-item:last-child { border-bottom: none; }
.paste-info { display: flex; flex-direction: column; }
.paste-link { color: #d32f2f; text-decoration: none; font-size: 13px; font-weight: bold; }
.paste-link:hover { text-decoration: underline; }
.paste-meta { font-size: 11px; color: #999; margin-top: 3px; }
</style>