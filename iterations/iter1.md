# Итерация 1: Архитектурный фундамент и базовая конфигурация (Scaffolding)

**Цель итерации:** Развернуть проект, настроить зависимости, подготовить сборочную конфигурацию и зафиксировать многослойную архитектуру приложения под требования ТЗ (офлайн-режим «Гость», учет транзакций, категорий и счетов).

---

### Выполненные задачи

- [x] Инициализирован Git-репозиторий проекта, корректно настроен `.gitignore` (исключены служебные файлы среды разработки `.idea/`).
- [x] Настроен единый каталог версий `gradle/libs.versions.toml` (Version Catalog).
- [x] Сконфигурированы сборочные скрипты проекта (`build.gradle.kts` корневой и уровня модуля `app`):
    - Установлены `minSdk = 29` и `targetSdk = 36`;
    - Задана целевая версия компиляции `JavaVersion.VERSION_17`.
- [x] Подключен и сконфигурирован плагин компиляции аннотаций KSP.
- [x] Интегрирован UI-стек Compose:
    - Compose BOM;
    - Material Design 3;
    - Material Icons Extended для иконок категорий расходов и доходов.
- [x] Подключена навигация Jetpack Compose Navigation 3.
- [x] Подключен DI-фреймворк Koin.
- [x] Подключены зависимости локального хранилища Room (Runtime, KTX, Compiler через KSP) под офлайн-архитектуру.
- [x] Подключен стек сериализации Kotlinx Serialization (Core + JSON).
- [x] Подключен сетевой стек (Retrofit, OkHttp, Logging Interceptor) как задел под будущую облачную синхронизацию.
- [x] Подключена библиотека Coil 3 для загрузки изображений и логотипов.
- [x] Спроектирована Clean Architecture (`di`, `domain`, `data`, `ui`).
- [x] Созданы базовые Domain-модели и перечисления приложения:
  - `TransactionType` (Expense, Income, Transfer);
  - `CategoryType` (Expense, Income);
  - `Account` (id, name, balance, currency);
  - `Category` (id, name, type, iconRes, budgetLimit);
  - `Transaction` (id, amount, type, categoryId, accountId, targetAccountId, dateTime, note).