<template>
  <div class="settings-container">
    <h3 class="settings-title">Optional Paste Settings</h3>

    <div class="form-row">
      <div class="form-label">Category:</div>
      <div class="form-control">
        <select v-model="formData.category">
          <option>None</option>
          <option>Code</option>
          <option>Text</option>
        </select>
      </div>
    </div>

    <div class="form-row">
      <div class="form-label">Tags:</div>
      <div class="form-control">
        <input type="text" v-model="formData.tags" />
      </div>
    </div>

    <div class="form-row">
      <div class="form-label">Syntax Highlighting:</div>
      <div class="form-control">
        <select v-model="formData.syntax">
          <option>None</option>
          <option>HTML</option>
          <option>CSS</option>
          <option>JavaScript</option>
          <option>Vue</option>
        </select>
      </div>
    </div>

    <div class="form-row">
      <div class="form-label">Paste Expiration:</div>
      <div class="form-control">
        <select v-model="formData.expiration">
          <option>Never</option>
          <option>Burn after read</option>
          <option>10 Minutes</option>
          <option>1 Hour</option>
          <option>1 Day</option>
        </select>
      </div>
    </div>

    <div class="form-row">
      <div class="form-label">Paste Exposure:</div>
      <div class="form-control">
        <select v-model="formData.exposure">
          <option>Public</option>
          <option>Access by link</option>
          <option>Unlisted</option>
          <option>Private</option>
        </select>
      </div>
    </div>

    <div class="form-row">
      <div class="form-label">Password:</div>
      <div class="form-control">
        <label class="checkbox-label">
          <input type="checkbox" v-model="formData.passwordEnabled" /> Enabled
        </label>
        <input 
          type="password" 
          class="password-input" 
          v-model="formData.password"
          :disabled="!formData.passwordEnabled" 
          :style="{ backgroundColor: formData.passwordEnabled ? 'white' : '#f9f9f9' }"
        />
      </div>
    </div>

    <div class="form-row">
      <div class="form-label">Paste Name / Title:</div>
      <div class="form-control">
        <input type="text" v-model="formData.title" />
      </div>
    </div>

    <div class="form-row submit-row">
      <div class="form-label"></div>
      <div class="form-control action-controls">
        <button class="btn-submit" @click="createPaste">Create New Paste</button>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'

// Создаем "канал связи" с родителем
const emit = defineEmits(['submitPaste'])

const formData = ref({
  category: 'None',
  tags: '',
  syntax: 'None',
  expiration: 'Never',
  exposure: 'Public',
  passwordEnabled: false,
  password: '',
  title: ''
})

const createPaste = () => {
  // Вместо alert мы "выстреливаем" событием наверх и передаем настройки
  emit('submitPaste', formData.value)
}
</script>

<style scoped>
/* Твои стили из предыдущего сообщения остаются без изменений (чтобы не дублировать, я их сократил, оставь свои) */
.settings-container { margin-top: 30px; font-family: Arial, sans-serif; }
.settings-title { font-size: 16px; color: #333; border-bottom: 1px solid #e0e0e0; padding-bottom: 8px; margin-bottom: 20px; font-weight: normal; }
.form-row { display: flex; margin-bottom: 15px; align-items: center; }
.form-label { width: 180px; font-size: 13px; color: #111; display: flex; align-items: center; gap: 8px; }
.form-control { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.form-control input[type="text"], .form-control input[type="password"], .form-control select { width: 320px; padding: 6px 10px; border: 1px solid #ccc; border-radius: 3px; font-size: 13px; color: #333; outline: none; background-color: white; }
.form-control input:focus, .form-control select:focus { border-color: #999; }
.checkbox-label { font-size: 13px; color: #333; display: flex; align-items: center; gap: 6px; cursor: pointer; }
.submit-row { margin-top: 25px; }
.action-controls { flex-direction: row; align-items: center; }
.btn-submit { background-color: #e0e0e0; border: 1px solid #ccc; padding: 10px 20px; font-size: 14px; font-weight: bold; color: #111; border-radius: 3px; cursor: pointer; transition: background-color 0.2s; }
.btn-submit:hover { background-color: #d5d5d5; }
</style>