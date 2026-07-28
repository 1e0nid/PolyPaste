# --- Этап сборки (Maven) остается без изменений ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests -U

# --- Этап запуска (МЕНЯЕМ ОБРАЗ ТУТ) ---
# Убираем -alpine, используем обычный jre (он на базе Debian/Ubuntu)
FROM eclipse-temurin:21-jre
WORKDIR /app

# В Debian/Ubuntu библиотеки уже на месте, 
# никаких apk add делать не нужно!

COPY --from=build /app/target/*.jar app.jar
EXPOSE 80

ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]