<template>
  <div class="view-layout">
    <div class="content-left">
      
      <div class="paste-header">
        <div class="header-main">
          <div class="author-avatar">👤</div>
          <div class="title-meta">
            <h1>{{ pasteData.title }}</h1>
            <div class="meta-info">
              <span class="author-name">{{ pasteData.author }}</span>
              <span class="separator">|</span>
              <span class="date">{{ pasteData.date }}</span>
            </div>
          </div>
        </div>
        
        <div class="action-bar">
          <button class="btn-action" @click="copyCode">copy</button>
          <button class="btn-action" @click="downloadFile">download</button>
        </div>
      </div>

      <div class="code-container">
        <div class="code-header">
          <span class="lang-label">{{ pasteData.language }}</span>
          <span class="size-label">{{ pasteData.size }}</span>
        </div>
        
        <div class="code-wrapper">
          <div class="line-numbers">
            <span v-for="n in lineCount" :key="n">{{ n }}</span>
          </div>
          <pre><code ref="codeBlock" :class="'language-' + pasteData.language">{{ pasteData.content }}</code></pre>
        </div>
      </div>
    </div>

    <aside class="content-right">
      <RightSidebar />
    </aside>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import RightSidebar from '../components/RightSidebar.vue'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css' // Тема подсветки (светлая)

const route = useRoute()
const codeBlock = ref(null)

// Имитируем получение данных по ID из ссылки (route.params.id)
const pasteData = ref({
  id: route.params.id,
  title: 'BEST EBOOKS 2026 PART 2',
  author: 'NickPickS',
  date: 'Mar 24th, 2024',
  views: '1,540',
  language: 'javascript',
  size: '4.17 KB',
  content: `// Пример кода для проверки подсветки
function polybinInit() {
  const siteName = "Polybin";
  console.log("Welcome to " + siteName);
  
  const features = ["Fast", "Secure", "Simple"];
  features.forEach(f => console.log("- " + f));======================================================================================
}

polybinInit();
`
})

// Считаем количество строк для нумерации
const lineCount = computed(() => pasteData.value.content.split('\n').length)

// --- ЛОГИКА КОПИРОВАНИЯ ---
const copyCode = async () => {
  try {
    // Используем встроенный API браузера для буфера обмена
    await navigator.clipboard.writeText(pasteData.value.content)
    alert('Code copied to clipboard!')
  } catch (err) {
    alert('Failed to copy text.')
    console.error(err)
  }
}

// --- ЛОГИКА СКАЧИВАНИЯ ---
const downloadFile = () => {
  // 1. Создаем виртуальный текстовый файл (Blob) из нашей пасты
  const blob = new Blob([pasteData.value.content], { type: 'text/plain' })
  
  // 2. Делаем временную ссылку на этот файл
  const url = window.URL.createObjectURL(blob)
  
  // 3. Создаем невидимый тег <a>
  const a = document.createElement('a')
  a.href = url
  
  // 4. Задаем имя файла (берем из названия пасты + .txt)
  // Если в названии есть пробелы, заменяем их на подчеркивания для красоты
  const fileName = pasteData.value.title.replace(/\s+/g, '_') + '.txt'
  a.download = fileName
  
  // 5. Имитируем клик по этой ссылке и тут же удаляем её
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  window.URL.revokeObjectURL(url)
}

onMounted(() => {
  // Запускаем подсветку синтаксиса после загрузки страницы
  if (codeBlock.value) {
    hljs.highlightElement(codeBlock.value)
  }
})
</script>

<style scoped>
.view-layout {
  display: flex;
  gap: 30px;
  width: 100%;
}

.content-left {
  flex: 7;
  min-width: 0; /* МАГИЯ ЗДЕСЬ: запрещаем колонке растягиваться бесконечно */
}

.content-right {
  flex: 3;
  border-left: 1px solid #eee;
  padding-left: 20px;
}

/* --- СТИЛИ ПЛАШКИ --- */
.paste-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-main {
  display: flex;
  gap: 15px;
}

.author-avatar {
  width: 45px;
  height: 45px;
  background-color: #ffcc80;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.title-meta h1 {
  font-size: 20px;
  margin: 0 0 5px 0;
  color: #333;
}

.meta-info {
  font-size: 12px;
  color: #777;
  display: flex;
  gap: 8px;
}

.author-name {
  color: #1b5e20;
  font-weight: bold;
}

.action-bar {
  display: flex;
  gap: 5px;
}

.btn-action {
  background-color: #f5f5f5;
  border: 1px solid #ccc;
  padding: 4px 8px;
  font-size: 11px;
  text-transform: uppercase;
  cursor: pointer;
  border-radius: 3px;
}

.btn-action:hover {
  background-color: #e0e0e0;
}

/* --- СТИЛИ ОКНА С КОДОМ --- */
.code-container {
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.code-header {
  background-color: #f8f8f8;
  padding: 8px 15px;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
}

.code-wrapper {
  display: flex;
  background-color: white;
}

.line-numbers {
  background-color: #f0f0f0;
  padding: 10px 5px;
  text-align: right;
  min-width: 30px;
  border-right: 1px solid #ddd;
  display: flex;
  flex-direction: column;
  /* Делаем шрифт и размер ТОЧНО ТАКИМИ ЖЕ, как у кода: */
  font-family: 'Courier New', Courier, monospace; 
  font-size: 14px; 
  color: #999;
  user-select: none;
}

/* ДОБАВЛЯЕМ ЭТО: Жестко фиксируем высоту каждой цифры */
.line-numbers span {
  display: block;
  line-height: 1.5;
}

pre {
  margin: 0;
  padding: 10px 15px; 
  flex-grow: 1;
  overflow-x: auto; /* Включает горизонтальный ползунок (скролл) */
  max-width: 100%;  /* Не дает тексту вырваться наружу */
}

code {
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  line-height: 1.5;
}

/* МАГИЯ ЗДЕСЬ: Жестко убиваем встроенные отступы от Highlight.js */
code.hljs {
  padding: 0 !important; 
  background: transparent !important;
}
</style>