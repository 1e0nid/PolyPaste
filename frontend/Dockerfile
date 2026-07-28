FROM node:20-alpine
WORKDIR /app

# Копируем конфиги и устанавливаем зависимости
COPY package*.json ./
RUN npm install

# Копируем остальной код
COPY . .

# Пробрасываем порт Vite
EXPOSE 5173

# Запускаем в режиме разработки с флагом --host
# Без --host докер не пропустит трафик внутрь контейнера
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
