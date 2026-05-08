<template>
  <div class="paste-area">
    <div class="editor-header">
      <h2>New Paste</h2>
    </div>
    
    <textarea 
      class="paste-input" 
      placeholder="Write your code or text here..."
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
    ></textarea>
  </div>
</template>

<script setup>
// Говорим Vue, что компонент принимает значение (modelValue)
// и умеет сообщать о его изменении (update:modelValue)
defineProps(['modelValue'])
defineEmits(['update:modelValue'])
</script>

<style scoped>
.paste-area {
  width: 100%;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.editor-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: normal;
  color: #333;
}

.paste-input {
  width: 100%;
  min-height: 350px;
  padding: 15px;
  border: 1px solid #d1d1d1;
  border-radius: 4px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  resize: vertical;
  box-sizing: border-box;
  outline: none;
}

.paste-input:focus {
  border-color: #1b5e20;
}
</style>