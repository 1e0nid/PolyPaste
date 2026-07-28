<template>
  <div class="settings-container">
    <h3 class="settings-title">Настройки пасты</h3>

    <div class="form-row">
      <div class="form-label">Syntax Highlighting:</div>
      <div class="form-control">
        <select v-model="modelValue.syntax" @change="updateSettings">
          <option>None</option>
          <option>HTML</option>
          <option>CSS</option>
          <option>JavaScript</option>
          <option>Vue</option>
          <option>Java</option>
        </select>
      </div>
    </div>

    <div class="form-row">
      <div class="form-label">Paste Expiration:</div>
      <div class="form-control">
        <select v-model="modelValue.expiration" @change="updateSettings">
          <option>Never</option>
          <option>Burn after read</option>
          <option>10 Minutes</option>
          <option>1 Hour</option>
          <option>1 Day</option>
        </select>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue'])

const updateSettings = () => {
  emit('update:modelValue', props.modelValue)
}
</script>

<style scoped>
/* Твои стандартные стили */
.settings-container { margin-top: 30px; font-family: Arial, sans-serif; }
.settings-title { font-size: 16px; color: #333; border-bottom: 1px solid #e0e0e0; padding-bottom: 8px; margin-bottom: 20px; font-weight: normal; }
.form-row { display: flex; margin-bottom: 15px; align-items: center; }
.form-label { width: 180px; font-size: 13px; color: #111; display: flex; align-items: center; gap: 8px; }
.form-control { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.form-control select { width: 320px; padding: 6px 10px; border: 1px solid #ccc; border-radius: 3px; font-size: 13px; color: #333; outline: none; background-color: white; }
.form-control select:focus { border-color: #999; }
</style>