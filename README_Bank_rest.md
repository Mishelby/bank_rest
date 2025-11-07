Система управления банковскими картами

Проект представляет собой сервис для управления банковскими картами с использованием Spring Boot, PostgreSQL и Liquibase для управления миграциями базы данных. Для удобного запуска используется Docker.

Стек технологий

Java 21 / Spring Boot

PostgreSQL 17

Liquibase

Docker / Docker Compose

Структура Docker Compose

postgres-dev – контейнер с PostgreSQL для разработки

app – Spring Boot приложение

Настройка проекта
1. Сборка JAR файла

Перед запуском Docker-контейнеров необходимо собрать JAR файл приложения:

./mvnw clean package


После успешной сборки JAR файл будет находиться в target/ (например, target/card-service.jar).

2. Запуск Docker-контейнеров

Для запуска всех сервисов используется Docker Compose:

docker-compose up --build

Опция --build гарантирует, что Docker заново соберет образ для приложения.

Контейнеры запускаются в следующем порядке:

PostgreSQL (postgres-dev)

Spring Boot приложение (app) – ждёт, пока база данных станет доступна

3. Доступ к сервисам

Приложение: http://localhost:8080

Сервер: postgres

База данных: dev_card_db

Пользователь: postgres

Пароль: postgres

4. Остановка контейнеров
   docker-compose down

5. Дополнительно

Данные PostgreSQL сохраняются в Docker volume card-db-data, чтобы при перезапуске контейнера данные не терялись.

Liquibase автоматически применяет миграции при запуске приложения, если включен параметр SPRING_LIQUIBASE_ENABLED=true.