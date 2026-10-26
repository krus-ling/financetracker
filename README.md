<div align="center">

# 💳 Finance Tracker

**Современное нативное Android-приложение для учёта личных финансов, планирования бюджетов и наглядной аналитики расходов.**

Приложение спроектировано по принципу **Offline-First**: полноценная функциональность доступна локально без подключения к интернету с последующей бесшовной облачной синхронизацией.

---

<!-- Платформа и UI -->
![Android](https://img.shields.io/badge/Android-API%2029–37-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%20Design%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-9.2-02303A?style=for-the-badge&logo=gradle&logoColor=white)

<!-- Архитектура и Данные -->
![Clean Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVVM-0EA5E9?style=for-the-badge)
![Room DB](https://img.shields.io/badge/Room%20DB-2.8%20(SQLite)-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Koin](https://img.shields.io/badge/DI-Koin%204.2-F59E0B?style=for-the-badge)
![Coroutines](https://img.shields.io/badge/Async-Coroutines%20%26%20Flow-6366F1?style=for-the-badge&logo=kotlin&logoColor=white)
![Network](https://img.shields.io/badge/Network-Retrofit%203%20%2B%20OkHttp%205-10B981?style=for-the-badge)

</div>

---

## 🚀 Ключевые возможности

* **100% Offline-First:** мгновенный отклик интерфейса, локальное хранение всех счетов, категорий и транзакций в базе данных устройства без обязательного интернета.
* **Финансовый учёт:** фиксация расходов, доходов и внутренних переводов между счетами с автоматическим пересчётом балансов.
* **Умные лимиты и категории:** отслеживание прогресса месячного бюджета по категориям трат с цветовой индикацией рисков и превышений.
* **Интерактивная аналитика:** круговые и столбчатые диаграммы структуры доходов, расходов и динамики баланса за выбранный период.
* **Локальный экспорт и облако:** выгрузка истории операций в форматы `CSV` / `JSON` и бесшовная миграция гостевого профиля в облачный аккаунт.

---

## 🛠 Стек технологий

| Направление | Стек и библиотеки |
| :--- | :--- |
| **Платформа и язык** | Android (`minSdk 29`, `targetSdk 37`, `compileSdk 37`), Kotlin 2.4, Java 17 |
| **UI-фреймворк** | Jetpack Compose, Material Design 3, Material Icons Extended |
| **Архитектура** | Clean Architecture (`domain` / `data` / `ui` / `di`), MVVM / MVI, Single Activity |
| **Навигация** | AndroidX Navigation 3 (`runtime`, `ui`, `viewmodel`, `material3-adaptive`) |
| **Внедрение зависимостей** | Koin 4.2 (`koin-bom`, `koin-android`, `koin-androidx-compose`) |
| **Локальное хранилище** | Room 2.8 (`room-runtime`, `room-ktx`, кодогенерация через KSP) |
| **Сетевой слой** | Retrofit 3.0, OkHttp 5.5, HttpLoggingInterceptor |
| **Сериализация** | Kotlinx Serialization 1.11 (`json`, `converter-kotlinx-serialization`) |
| **Асинхронность** | Kotlin Coroutines, `StateFlow`, `SharedFlow`, `Flow` |
| **Загрузка изображений** | Coil 3.6 (`coil-compose`, `coil-network-okhttp`) |
| **Система сборки** | Gradle Version Catalog (`libs.versions.toml`), AGP 9.2, KSP 2.3 |

---

## 🗺 Карта разработки (Roadmap)

| Итерация | Фокус этапа | Статус | Отчёт |
| :---: | :--- | :---: | :---: |
| **1** | **Аналитика и проектирование:** ТЗ, ролевые модели, сущности, схема переходов, USM, прототип | ![Done](https://img.shields.io/badge/Выполнено-22C55E?style=flat-square) | [iteration-1](./iterations/iteration-1/README.md) |
| **2** | **Архитектурный каркас:** конфигурация сборки, стек зависимостей, пакеты Clean Architecture, Domain-модели | ![Done](https://img.shields.io/badge/Выполнено-22C55E?style=flat-square) | [iteration-2](./iterations/iteration-2/README.md) |
| **3** | **Слой данных и навигация:** Room DB, DAO, предзаполнение БД, мапперы, Koin DI, граф Navigation 3 | ![Next](https://img.shields.io/badge/Следующий%20этап-3B82F6?style=flat-square) | `iteration-3` |
| **4** | **Дашборд, счета и бюджеты:** главный экран, карусель счетов, управление категориями и лимитами | ![Planned](https://img.shields.io/badge/По%20плану-64748B?style=flat-square) | `iteration-4` |
| **5** | **Финансовые операции:** создание/редактирование доходов, расходов и переводов, лента истории, свайп-удаление | ![Planned](https://img.shields.io/badge/По%20плану-64748B?style=flat-square) | `iteration-5` |
| **6** | **Аналитика и сервисы Гостя:** интерактивные диаграммы, прогресс-бары лимитов, профиль, экспорт CSV/JSON | ![Planned](https://img.shields.io/badge/По%20плану-64748B?style=flat-square) | `iteration-6` |
| **7** | **Облачная синхронизация:** регистрация и вход, миграция данных Гостя (`AuthMerge`), фоновая синхронизация | ![Planned](https://img.shields.io/badge/По%20плану-64748B?style=flat-square) | `iteration-7` |

---

## 📁 Архитектура и структура проекта

```agsl
com.example.financetracker/
├── App.kt                  # Наследник Application (точка старта приложения и инициализация Koin)
├── MainActivity.kt         # Единственная Activity (хост для Compose и навигации)
│
├── di/                     # Dependency Injection (Koin)
│   ├── appModule.kt
│   ├── databaseModule.kt
│   └── repositoryModule.kt
│
│
├── domain/                 # Бизнес-логика
│   ├── model/              # Transaction, Account, Category, Enums
│   ├── repository/         # Интерфейсы репозиториев
│   └── usecase/            # Интеракторы
│
├── data/                   # Работа с данными
│   ├── local/              # Room DB, Entity, DAO, Converters
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   └── entity/
│   ├── remote/             # DTO, сетевые клиенты для будущей синхронизации
│   ├── repository/         # Реализация интерфейсов из domain
│   └── mapper/             # Мапперы (Entity -> Domain, Domain -> Entity)
│
└── ui/                     # Слой представления (Jetpack Compose)
    ├── navigation/         # Навигация (NavHost, маршруты экранов)
    ├── theme/              # Color, Type, Theme (Material Design 3)
    ├── components/         # Общие UI-компоненты (AppButton, CategoryBadge, TopBar)
    ├── util/               # Форматирование для UI (валюты, даты, строковые хелперы)
    └── screens/            # Экраны приложения
        ├── dashboard/      # Главный экран
        │   └── components/ # Локальные виджеты (AccountsCarousel, TransactionItem)
        ├── transaction/    # Добавление / редактирование операции
        │   └── components/
        ├── analytics/      # Экран отчетов и аналитики
        │   └── components/
        └── profile/        # Профиль и настройки
            └── components/
```

---

## 📋 Правила работы с репозиторием и процесс разработки

### 1. Декомпозиция задач
Разработка ведётся итеративно. Запрещается заливать всю итерацию одним гигантским коммитом — каждая итерация обязательно дробится на **мелкие атомарные задачи и подзадачи**:
* **Уровень итерации:** крупный этап разработки (например, *Слой локальной БД и навигация*).
* **Уровень подзадачи:** конкретный логический блок внутри слоя (например, *Создание Entity и конвертеров Room*, *Реализация DAO*, *Настройка Koin-модуля БД*).
* **Уровень микрозадачи (один коммит):** минимальное законченное изменение, которое компилируется и не ломает сборку (например, добавление одного экрана, одного маппера или репозитория).

### 2. Ведение документации по итерациям (`iterations/`)
В корне репозитория находится директория `iterations/`, внутри которой для каждого этапа создаётся папка текущей итерации (например, `iterations/iteration_1/`, `iterations/iteration_2/` и т.д.):

```agsl
iterations/
├── iteration_1/
│   └── README.md           # Чеклист задач и результатов 1-й итерации
├── iteration_2/
│   └── README.md           # План, текущие (в работе) и выполненные задачи 2-й итерации
└── ...
```

В файле итерации фиксируются:
* **Цель итерации**;
* **Выполненные задачи** (`- [x]`);
* **Задачи в работе / План на итерацию** (`- [ ]`).
  *(В качестве эталона оформления используется отчёт первой выполненной итерации).*

### 3. Правила именования веток
* `main` — стабильная ветка с завершёнными итерациями.
* `iter-N` (или `iteration-N`) — основная ветка текущей итерации.
* При командной или параллельной разработке под мелкие подзадачи от ветки итерации заводятся короткие ветки: `feat/<название-задачи>`, `refactor/<название>`, `fix/<название>`.

### 4. Правила оформления коммитов (Conventional Commits)
Каждый коммит должен иметь строгую двухчастную структуру: **заголовок в одну строку** и **подробное описание списком** через пустую строку.

**Формат:**

```text
<тип>(<область>): <краткое описание сути изменений в одну строку>

- Подробный пункт 1: что именно было создано или изменено.
- Подробный пункт 2: какие компоненты, модели или зависимости затронуты.
- Подробный пункт 3: причина изменений или архитектурная деталь.
```

**Допустимые типы (<тип>):**

* `feat` — добавление новой функциональности, моделей, экранов или слоёв;
* `refactor` — изменение структуры кода без изменения внешнего поведения;
* `fix` — исправление ошибок и багов;
* `build` / `chore` — настройка Gradle, обновление зависимостей в libs.versions.toml, конфигурация проекта;
* `docs` — обновление README.md или отчётов в папке iterations/;
* `ui` / `style` — вёрстка компонентов, правка тем, цветов и отступов;
* `test` — добавление или обновление модульных и инструментальных тестов.

**Пример правильного коммита:**
```agsl
feat(core): настроить архитектурный каркас проекта и базовые domain-модели

  - Настроена многослойная структура пакетов Clean Architecture (data, domain, ui, di).
  - Сконфигурирован каталог зависимостей libs.versions.toml и скрипты Gradle под API 37 и Java 17.
  - Подключен базовый стек: Jetpack Compose M3, Room с KSP, Koin, Navigation 3, Kotlinx Serialization, Coil 3, Retrofit и OkHttp.
  - Созданы базовые domain-модели: Account, Category, CategoryType, Transaction, TransactionType.
  - Документирован стек и этапы итерации в README.md и папке iterations/.
```

---

## ⚙️ Сборка и запуск проекта

1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/krus-ling/financetracker.git

2. Откройте проект в Android Studio.
3. Дождитесь завершения синхронизации Gradle (требуется JDK 17+).
4. Запустите конфигурацию app на эмуляторе или физическом устройстве с Android 10 (API 29) или выше.