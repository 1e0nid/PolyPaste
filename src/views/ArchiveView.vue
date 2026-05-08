<template>
  <div class="archive-layout">
    
    <div class="archive-header">
      <h2>Pastes Published in the Last 24 Hours</h2>
    </div>

    <table class="archive-table">
      <thead>
        <tr>
          <th class="col-name">NAME / TITLE</th>
          <th class="col-author">AUTHOR</th>
          <th class="col-posted">POSTED</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="paste in recentPastes" :key="paste.id">
          
          <td class="col-name">
            <router-link :to="'/paste/' + paste.id" class="paste-link">
              {{ paste.title }}
            </router-link>
          </td>
          
          <td class="col-author">{{ paste.author }}</td>
          
          <td class="col-posted">{{ paste.time }}</td>
        </tr>
      </tbody>
    </table>

  </div>
</template>

<script setup>
import { ref } from 'vue'
// Импорт сайдбара мы удалили, так как он здесь больше не нужен

// Обновили мок-данные: добавили поле author
const recentPastes = ref([
  { id: 'p1', title: 'Untitled', author: 'NickPickS', time: '12 min ago' },
  { id: 'p2', title: 'Python Web Scraper', author: 'PyDev', time: '45 min ago' },
  { id: 'p3', title: 'Rogue - Tank/Mutilate', author: 'WoW_Player', time: '2 hours ago' },
  { id: 'p4', title: 'Mage - Frost', author: 'WoW_Player', time: '2 hours ago' },
  { id: 'p5', title: 'webview2-workaround.sh', author: 'LinuxGuru', time: '3 hours ago' },
  { id: 'p6', title: 'Untitled', author: 'Guest', time: '4 hours ago' },
  { id: 'p7', title: 'BEST EBOOKS 2026 PART 6', author: 'NickPickS', time: '15 hours ago' },
  { id: 'p8', title: 'Python Scraper Fix', author: 'PyDev', time: '18 hours ago' },
  { id: 'p9', title: 'Untitled', author: 'Guest', time: '22 hours ago' },
  { id: 'p10', title: 'Config.json dump', author: 'Admin', time: '23 hours ago' }
])
</script>

<style scoped>
/* Контейнер на всю ширину страницы */
.archive-layout {
  width: 100%;
}

/* --- Стили Заголовка --- */
.archive-header h2 {
  font-size: 20px;
  color: #333;
  margin: 0 0 20px 0;
  font-weight: bold;
  border-bottom: 1px solid #ccc;
  padding-bottom: 10px;
}

/* --- Стили Таблицы --- */
.archive-table {
  width: 100%;
  border-collapse: collapse; 
  font-family: Arial, sans-serif;
}

.archive-table th {
  text-align: left;
  padding: 10px 5px;
  border-bottom: 2px solid #ddd;
  font-size: 12px;
  color: #333;
}

.archive-table td {
  padding: 12px 5px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

/* Подсветка строки при наведении */
.archive-table tbody tr:hover {
  background-color: #f9f9f9;
}

/* Размеры колонок: 50% / 25% / 25% */
.col-name {
  width: 50%; 
}

.col-author {
  width: 25%;
  color: #1b5e20; /* Сделал никнеймы немного зелеными для красоты */
  font-weight: bold;
  font-size: 13px;
}

.col-posted {
  width: 25%;
  color: #777;
  font-size: 13px;
}

.paste-link {
  color: #0066cc; 
  text-decoration: none;
  font-weight: bold;
}

.paste-link:hover {
  text-decoration: underline;
}
</style>