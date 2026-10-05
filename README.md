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

* **100% Offline-First:** мгновенный отклик, локальное хранение всех счетов, категорий и транзакций.
* **Финансовый учёт:** фиксация расходов, доходов и внутренних переводов между счетами с автопересчётом балансов.
* **Умные лимиты и категории:** отслеживание прогресса бюджета по категориям трат с цветовой индикацией превышений.
* **Аналитика:** наглядные графики и диаграммы структуры расходов по временным периодам.

---

## 🛠 Стек технологий

| Направление                      | Стек / Библиотеки                                                   |
|:---------------------------------|:--------------------------------------------------------------------|
| **Язык и среда**                 | Kotlin, Java 17                                                     |                                                                      |
| **UI-фреймворк**                 | Jetpack Compose, Material Design 3, Compose Material Icons Extended |
| **Архитектура**                  | Clean Architecture (Domain / Data / Ui / Di)                        |
| **Навигация**                    | AndroidX Navigation 3                                               |
| **Внедрение зависимостей**       | Koin                                                                |
| **Локальная база данных**        | Room DB (Runtime, Coroutines KTX, KSP Code Generation)              |
| **Сетевой слой & Синхронизация** | Retrofit, OkHttp, HttpLoggingInterceptor                            |
| **Сериализация данных**          | Kotlinx Serialization (Core, JSON)                                  |
| **Асинхронность**                | Kotlin Coroutines, StateFlow, SharedFlow, Flow                      |
| **Работа с изображениями**       | Coil 3                                                              |

---

## Архитектура и структура проекта
```
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

## ⚙️ Сборка и запуск проекта

1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/krus-ling/financetracker.git
   