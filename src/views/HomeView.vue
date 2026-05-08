<template>
  <div class="home-layout">
    <div class="content-left">
      <PasteArea v-model="pasteText" />
      
      <PasteSettings @submitPaste="sendDataToServer" />
    </div>

    <aside class="content-right">
      <RightSidebar />
    </aside>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import PasteArea from '../components/PasteArea.vue'
import PasteSettings from '../components/PasteSettings.vue'
import RightSidebar from '../components/RightSidebar.vue'

const router = useRouter()

// Переменная для хранения текста из PasteArea
const pasteText = ref('')

// Эта функция сработает, когда в PasteSettings нажмут кнопку
const sendDataToServer = async (settings) => {
  
  // 1. Проверяем, не пустое ли текстовое поле
  if (pasteText.value.trim() === '') {
    alert('Пожалуйста, введите код пасты!')
    return
  }

  // 2. Склеиваем текст и настройки в один красивый объект
  const newPasteData = {
    content: pasteText.value,
    ...settings // Берем все поля из объекта settings и разворачиваем здесь
  }

  // Показываем в консоли, что именно полетит на бэкенд
  console.log('Отправляем на бэкенд:', JSON.stringify(newPasteData, null, 2))

  // 3. ЗДЕСЬ БУДЕТ FETCH ЗАПРОС К БЭКЕНДУ (POST)
  // Временно имитируем успешную работу сервера:
  alert('Паста успешно создана! Перенаправляем на страницу просмотра...')
  
  // 4. Перекидываем пользователя на страницу просмотра (пока на фейковый ID)
  router.push('/paste/demo-123')
}
</script>

<style scoped>
.home-layout {
  display: flex;
  gap: 30px;
}
.content-left {
  flex: 7;
  min-width: 0;
}
.content-right {
  flex: 3;
  border-left: 1px solid #eee;
  padding-left: 20px;
}
</style>