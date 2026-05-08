<template>
  <div class="app-container">
    <AppHeader />
    
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import AppHeader from './components/AppHeader.vue'
</script>

<style>
/* Глобальные стили оставляем здесь */
body {
  margin: 0;
  padding: 0;
  background-color: #f5f5f5;
  font-family: Arial, sans-serif;
}

#app {
  max-width: 100%;
}

/* Общий белый контейнер-полотно для всех страниц */
.main-content {
  max-width: 1100px;
  margin: 20px auto;
  background-color: white;
  padding: 20px;
  box-shadow: 0 0 10px rgba(0,0,0,0.05);
  min-height: 800px;
}
</style>