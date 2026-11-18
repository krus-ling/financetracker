# 🏛️ Общая архитектура приложения

Приложение «Трекер личных финансов» построено по принципам **Clean Architecture** с однонаправленным потоком данных (**UDF — Unidirectional Data Flow**) и разделением на независимые Gradle-модули.

---

## 📦 Модульная структура

Граф зависимостей модулей:

```
        ┌─────────────┐
        │    :app     │ (Запуск, Koin Application, MainScaffold)
        └──────┬──────┘
               │
        ┌──────▼──────┐
        │:feature:home│ (Экраны дашборда, деталки счетов, шторки, HomeViewModel)
        └──┬───┬───┬──┘
           │   │   │
   ┌───────┘   │   └───────┐
   ▼           ▼           ▼
┌──────────────┐ ┌──────────────┐ ┌─────────────┐
│:core:database│ │ :core:uikit  │ │ :core:model │
└──────┬───────┘ └──────┬───────┘ └─────────────┘
       │                │                ▲
       └────────────────┴────────────────┘
```

### Назначение модулей:
1. **`:core:model`** — чистые Kotlin-модели (`Account`, `Category`, `Transaction`, `TransactionType`, `CategoryType`). Не зависит от Android SDK и сторонних библиотек.
2. **`:core:uikit`** — дизайн-система приложения: фирменные цвета, шрифты, базовые компоненты ввода, шторки, кнопки и карточки.
3. **`:core:database`** — локальное хранилище данных: Room DB, DAO, сущности, миграции, транзакционные репозитории.
4. **`:feature:home`** — функциональный модуль главного экрана, аналитики счетов, истории операций и шторок создания сущностей.
5. **`:app`** — точка входа в приложение: класс `FinanceApp` с инициализацией Koin, `MainActivity`, навигационный каркас `MainScaffold`.

---

## 🔄 Поток данных (Unidirectional Data Flow)

Приложение использует реактивную модель на базе Kotlin Coroutines & `Flow`:

```
┌────────────────┐      Room Flow      ┌──────────────────────┐
│ SQLite Room DB ├────────────────────►│  Data Repositories   │
└────────────────┘                     └──────────┬───────────┘
                                                  │
                                                  ▼ Flow<List<T>>
┌────────────────┐  collectAsStateWithLifecycle  ┌──────────────────────┐
│ Compose Screen │◄──────────────────────────────┤    HomeViewModel     │
└───────┬────────┘                               │ (StateFlow<UiState>) │
        │                                        └──────────────────────┘
        │ UI Events (addTransaction, etc.)                  ▲
        └───────────────────────────────────────────────────┘
```

1. **Реактивный источник истины:** изменения в базе данных через `Room` автоматически триггерят `Flow`.
2. **Агрегация во ViewModel:** `HomeViewModel` объединяет потоки счетов, категорий, последних операций и ошибок в единый поток `StateFlow<HomeUiState>` с помощью оператора `combine`:
   ```kotlin
   val uiState: StateFlow<HomeUiState> = combine(
       accountRepository.getAllAccounts(),
       transactionRepository.getRecentTransaction(limit = 10),
       categoryRepository.getAllCategories(),
       _errorState.asStateFlow()
   ) { accounts, transactions, categories, error ->
       HomeUiState(...)
   }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(isLoading = true))
   ```
3. **Безопасное чтение в UI:** экран подписывается на состояние через `collectAsStateWithLifecycle()`, автоматически останавливая подписку при сворачивании приложения.

---

## 🧭 Навигация и паттерн Stateful / Stateless

Навигационный каркас приложения реализован в `MainScaffold.kt`:

### 1. Stateful-компонент (`MainScaffold`)
Отвечает за взаимодействие с архитектурными компонентами Android:
- Получает `HomeViewModel` через Koin DI (`koinViewModel()`).
- Собирает `uiState`.
- Передаёт события в методы ViewModel.

### 2. Stateless-компонент (`MainScaffoldContent`)
Чистая UI-функция, принимающая только состояние и колбэки:
- Управляет состоянием вкладок нижней навигации (`NavTab.HOME`, `NavTab.ANALYTICS`, `NavTab.PROFILE`).
- Управляет видимостью модальных окон (`showQuickAdd`, `showAddAccountSheet`).
- Обрабатывает системную кнопку «Назад» (`BackHandler`) для выхода из экрана детальной информации о счёте.
- Используется в `@Preview` для моментальной отрисовки без запуска DI и эмулятора.

### 3. Бесшовная цепочка шторок (BottomSheet Chaining)
Если при добавлении операции у пользователя нет нужного счёта:
1. Нажатие на карточку «+ Добавить счёт» внутри `QuickAddTransactionSheet` временно закрывает шторку операции и открывает `AddAccountBottomSheet`.
2. После сохранения счёта приложение автоматически возвращает пользователя обратно в `QuickAddTransactionSheet` с уже обновлённым списком счетов.

---

## 📱 Адаптивность и Edge-to-Edge

1. Системные полосы навигации настроены прозрачными в соответствии со стандартами Android 15+.
2. Нижний отступ `Scaffold(innerPadding)` передаётся напрямую в `contentPadding` у `LazyColumn`:
   - Контент красиво скроллится за полупрозрачный навбар и плавающую кнопку FAB.
   - Последний элемент списка гарантированно не перекрывается нижней панелью ни на кнопочной, ни на жестовой навигации любых устройств.
