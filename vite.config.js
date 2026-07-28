import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())

  return {
    plugins: [vue()],

    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src')
      }
    },

    server: {
      // --- ВОТ ЭТО КРИТИЧНО ДЛЯ DOCKER ---
      host: '0.0.0.0', 
      port: 5173,
      watch: {
        usePolling: true // Спасает, если файлы не обновляются на лету в Windows
      },
      // -----------------------------------

      proxy: {
        '/api': {
          target: env.VITE_API_URL,
          changeOrigin: true,
          secure: false // <--- Ставим false для локального http-трафика
        },

        '/auth': {
          target: env.VITE_API_URL,
          changeOrigin: true,
          secure: false
        }
      }
    }
  }
})