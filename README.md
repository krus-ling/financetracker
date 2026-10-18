<div align="center">

# 💳 Finance Tracker

**Современное нативное Android-приложение для учёта личных финансов, планирования бюджетов и наглядной аналитики расходов.**

Приложение спроектировано по принципу **Offline-First**: полноценная функциональность доступна локально без подключения к интернету с последующей бесшовной облачной синхронизацией.

---

<!-- Badges -->
![Android](https://img.shields.io/badge/Android-API%2029%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-7986CB?style=for-the-badge&logo=materialdesign&logoColor=white)
![Room](https://img.shields.io/badge/Room%20DB-Offline--First-00599C?style=for-the-badge&logo=sqlite&logoColor=white)
![Koin](https://img.shields.io/badge/DI-Koin-F88C00?style=for-the-badge)

</div>

---

## 🚀 Ключевые возможности

* **100% Offline-First:** мгновенный отклик, локальное хранение всех счетов, категорий и транзакций в базе данных устройства.
* **Финансовый учёт:** фиксация расходов, доходов и внутренних переводов между счетами с автопересчётом балансов.
* **Умные лимиты и категории:** отслеживание прогресса бюджета по категориям трат с цветовой индикацией превышений.
* **Аналитика:** наглядные графики и диаграммы структуры расходов по временным периодам.
* **Масштабируемость:** заложена архитектурная база для авторизации и облачной синхронизации между устройствами.

---

## 🛠 Стек технологий

| Направление | Стек / Библиотеки |
| :--- | :--- |
| **Язык и среда** | Kotlin, Java 17 |
| **UI-фреймворк** | Jetpack Compose, Material Design 3, Compose Material Icons Extended |
| **Архитектура** | Clean Architecture (Domain / Data / UI / DI), MVVM / MVI |
| **Навигация** | AndroidX Navigation 3 |
| **Внедрение зависимостей** | Koin (`koin-android`, `koin-androidx-compose`) |
| **Локальная база данных** | Room DB (Runtime, Coroutines KTX, KSP Code Generation) |
| **Сетевой слой & Синхронизация** | Retrofit 3, OkHttp 5, HttpLoggingInterceptor |
| **Сериализация данных** | Kotlinx Serialization (Core, JSON) |
| **Асинхронность** | Kotlin Coroutines, StateFlow, SharedFlow, Flow |
| **Работа с изображениями** | Coil 3 (`coil-compose`, `coil-network-okhttp`) |

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
   