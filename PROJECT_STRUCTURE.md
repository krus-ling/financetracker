# Finance Tracker – Project Structure Overview

This document provides a detailed walkthrough of the **Finance Tracker** Android application source tree. All paths are absolute (Windows style) and link directly to the corresponding files for quick navigation.

---

## 1. High‑Level Layout

```
finance_tracker/                     # Root of the repository
├─ .gitignore                       # Git ignore rules
├─ README.md                        # Project description (already present)
├─ settings.gradle.kts              # Gradle settings – includes modules
├─ build.gradle.kts                 # Top‑level Gradle script (plugins, repositories)
├─ gradle/                          # Gradle wrapper & version catalog
│   ├─ libs.versions.toml           # Version catalog for dependencies
│   └─ wrapper/                     # Gradle wrapper binaries
├─ app/                             # Android application module (launches the app)
│   ├─ build.gradle.kts
│   ├─ src/main/                    # Main source set
│   │   ├─ AndroidManifest.xml
│   │   ├─ java/com/example/financetracker/
│   │   │   ├─ FinanceApp.kt        # Application class – Koin start‑up
│   │   │   ├─ MainActivity.kt      # Host activity – sets the Compose UI
│   │   │   ├─ navigation/          # Navigation scaffolding (bottom bar, tabs, scaffold)
│   │   │   │   ├─ MainScaffold.kt   # Root `Scaffold` with BottomNavBar & FAB
│   │   │   │   ├─ BottomNavBar.kt   # Bottom navigation bar UI
│   │   │   │   ├─ NavTab.kt         # Enum of navigation tabs (Home, Analytics, Profile)
│   │   │   │   └─ ...
│   │   │   ├─ component/           # UI components used across the app
│   │   │   │   └─ QuickAddTransactionSheet.kt  # Modal bottom sheet for adding a transaction
│   │   │   └─ placeholder/          # Simple placeholder screens for unfinished features
│   │   │   └─ placeholder/AnalyticsPlaceholderScreen.kt
│   │   │   └─ placeholder/ProfilePlaceholderScreen.kt
│   │   └─ res/                     # Resources (icons, themes, strings, etc.)
│   └─ ...
├─ core/                            # Shared library modules (feature‑agnostic)
│   ├─ model/                       # Plain Kotlin data models (domain layer)
│   │   ├─ build.gradle.kts
│   │   └─ src/main/java/com/example/financetracker/core/model/
│   │       ├─ Account.kt            # `data class Account`
│   │       ├─ Category.kt           # `data class Category`
│   │       ├─ CategoryType.kt       # Enum (EXPENSE, INCOME)
│   │       ├─ Transaction.kt        # `data class Transaction`
│   │       └─ TransactionType.kt    # Enum (EXPENSE, INCOME, TRANSFER)
│   ├─ database/                    # Room database implementation
│   │   ├─ build.gradle.kts
│   │   ├─ src/main/java/com/example/financetracker/core/database/
│   │   │   ├─ AppDatabase.kt        # `RoomDatabase` with pre‑populate callback
│   │   │   ├─ entity/               # Room `@Entity` classes
│   │   │   │   ├─ AccountEntity.kt
│   │   │   │   ├─ CategoryEntity.kt
│   │   │   │   └─ TransactionEntity.kt
│   │   │   ├─ dao/                  # DAO interfaces (query / insert / update)
│   │   │   │   ├─ AccountDao.kt
│   │   │   │   ├─ CategoryDao.kt
│   │   │   │   └─ TransactionDao.kt
│   │   │   ├─ converters/           # `@TypeConverter` for enum ↔ String
│   │   │   │   └─ DatabaseConverters.kt
│   │   │   ├─ mapper/               # Extension functions converting Entity ↔ Domain model
│   │   │   │   └─ DatabaseMappers.kt
│   │   │   ├─ repository/           # Repository layer exposing `Flow` of domain models
│   │   │   │   ├─ AccountRepository.kt
│   │   │   │   ├─ CategoryRepository.kt
│   │   │   │   └─ TransactionRepository.kt
│   │   │   └─ di/                   # Koin module providing DB, DAOs, and repositories
│   │   │       └─ DatabaseModule.kt
│   └─ uikit/                       # UI‑kit (design system) – reusable Compose components & theme
│       ├─ build.gradle.kts
│       └─ src/main/java/com/example/financetracker/core/uikit/
│           ├─ component/            # Common UI components (cards, rows, icons, amount text)
│           │   ├─ AccountCardItem.kt
│           │   ├─ AmountText.kt
│           │   ├─ CategoryIcon.kt
│           │   ├─ TransactionRowItem.kt
│           │   └─ ...
│           └─ theme/                # Color palette, typography, and MaterialTheme wrapper
│               ├─ Color.kt
│               ├─ Theme.kt
│               ├─ Type.kt
│               └─ ...
├─ feature/                         # Feature‑specific modules (each can be a separate screen)
│   └─ home/                        # Home screen – dashboard + recent transactions
│       ├─ build.gradle.kts
│       ├─ src/main/java/com/example/financetracker/feature/home/
│       │   ├─ HomeScreen.kt          # Top‑level Compose UI for the Home tab
│       │   ├─ HomeViewModel.kt       # ViewModel (combines repos, exposes `uiState`)
│       │   ├─ HomeUiState.kt         # UI state data class
│       │   ├─ di/                    # Koin module providing the ViewModel
│       │   │   └─ HomeModule.kt
│       │   └─ components/            # Small composables used only by Home
│       │       ├─ HomeComponents.kt   # `HomeAccountCard` and `HomeTransactionRow`
│       │       └─ ...
└─ iterations/                     # Documentation of development iterations (not code)
    ├─ iteration-1/README.md
    ├─ iteration-2/README.md
    └─ iteration-3/README.md
```
---

## 2. Core Modules

### 2.1 `core:model`
| File                 | Purpose                                                                                      |
|----------------------|----------------------------------------------------------------------------------------------|
| `Account.kt`         | Represents a financial account (id, name, balance, currency).                                |
| `Category.kt`        | Represents a spending/income category (type, icon, optional budget limit).                   |
| `CategoryType.kt`    | Enum – **EXPENSE** or **INCOME**.                                                            |
| `Transaction.kt`     | Represents a transaction (amount, type, linked account/category, timestamp, note).           |
| `TransactionType.kt` | Enum – **EXPENSE**, **INCOME**, **TRANSFER**.                                                |
| `build.gradle.kts`   | Minimal Java/Kotlin library module; brings in Kotlinx serialization for model JSON handling. |

### 2.2 `core:database`
*Provides a persistent Room database and a clean repository API.*

| File                                  | Purpose                                                                                                                                                                         |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `AppDatabase.kt`                      | Defines the Room database, lists entities, registers `DatabaseConverters`, and contains a `Callback` that pre‑populates a default account and a few categories on first launch. |
| `entity/AccountEntity.kt`             | Room `@Entity` for accounts (auto‑generated `Long` primary key).                                                                                                                |
| `entity/CategoryEntity.kt`            | Room `@Entity` for categories; stores `CategoryType` enum via a type converter.                                                                                                 |
| `entity/TransactionEntity.kt`         | Room `@Entity` for transactions; foreign‑keys to `AccountEntity` and `CategoryEntity`.                                                                                          |
| `dao/AccountDao.kt`                   | DAO for CRUD on accounts; provides a `Flow<List<AccountEntity>>`.                                                                                                               |
| `dao/CategoryDao.kt`                  | DAO for categories; exposes a `Flow<List<CategoryEntity>>`.                                                                                                                     |
| `dao/TransactionDao.kt`               | DAO for transactions; provides recent‑transaction query (`LIMIT 10`).                                                                                                           |
| `converters/DatabaseConverters.kt`    | `@TypeConverter` implementations converting `TransactionType` and `CategoryType` enums to/from `String`.                                                                        |
| `mapper/DatabaseMappers.kt`           | Extension functions mapping between Entity ↔ Domain (`AccountEntity.toDomain()`, `Account.toEntity()`, etc.).                                                                   |
| `repository/AccountRepository.kt`     | Reads accounts as `Flow<List<Account>>` and inserts new accounts.                                                                                                               |
| `repository/CategoryRepository.kt`    | Supplies a flow of domain categories.                                                                                                                                           |
| `repository/TransactionRepository.kt` | Supplies recent transactions flow; adds a transaction while updating the related account balance based on transaction type.                                                     |
| `di/DatabaseModule.kt`                | Koin module exposing a singleton `AppDatabase`, DAOs, and repository instances.                                                                                                 |
| `build.gradle.kts`                    | Android library module – pulls in Room, Koin, Coroutines, and core UI dependencies.                                                                                             |

### 2.3 `core:uikit`
*Design system and reusable UI widgets used across features.*

| File                              | Purpose                                                                                                               |
|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| `theme/Color.kt`                  | Central colour definitions (brand, surfaces, text, semantic financial colours, category palette).                     |
| `theme/Theme.kt`                  | `FinancetrackerTheme` composable that selects light/dark colour schemes and optionally dynamic colour on Android 12+. |
| `theme/Type.kt`                   | Basic Material typography (`Typography` instance).                                                                    |
| `component/AccountCardItem.kt`    | Card UI for an account shown in the horizontal carousel on the Home screen.                                           |
| `component/AmountText.kt`         | Formats a monetary amount with appropriate prefix (`+`, `−`) and colour based on `TransactionType`.                   |
| `component/CategoryIcon.kt`       | Utility to map a string `iconRes` to a Material Icons vector and display it inside a coloured circle.                 |
| `component/TransactionRowItem.kt` | Row UI for a single transaction in the recent‑transactions list.                                                      |
| `build.gradle.kts`                | Android library module – adds Compose BOM, core UI, and a dependency on `core:model`.                                 |

---

## 3. Feature Modules

### 3.1 `feature:home`
*Main dashboard – shows total balance, accounts carousel, and recent transactions.*

| File                           | Purpose                                                                                                                                                                                         |
|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `HomeScreen.kt`                | Root composable for the Home tab – orchestrates loading state, error handling, balance display, accounts carousel, and transaction list.                                                        |
| `HomeViewModel.kt`             | ViewModel that combines `AccountRepository`, `TransactionRepository`, and `CategoryRepository` into a single `StateFlow<HomeUiState>`. Handles adding a new transaction via `addTransaction()`. |
| `HomeUiState.kt`               | Data class representing UI state (loading flag, lists of accounts/categories, recent transactions, total balance, error message).                                                               |
| `di/HomeModule.kt`             | Koin module that provides `HomeViewModel`.                                                                                                                                                      |
| `components/HomeComponents.kt` | Small composables: `HomeAccountCard` (wraps `AccountCardItem`) and `HomeTransactionRow` (wraps `TransactionRowItem` with proper formatting).                                                    |
| `build.gradle.kts`             | Android library module – depends on `core:uikit`, `core:model`, `core:database`, plus Compose, Lifecycle, ViewModel, Koin, Coroutines.                                                          |

---

## 4. Application Module (`app`)

| File                                        | Purpose                                                                                                                                                                                                |
|---------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `FinanceApp.kt`                             | Subclass of `Application`; starts Koin with `databaseModule` and `homeModule`.                                                                                                                         |
| `MainActivity.kt`                           | `ComponentActivity` that enables edge‑to‑edge system bars and sets the composable content to `MainScaffold`.                                                                                           |
| `navigation/MainScaffold.kt`                | Top‑level `Scaffold` that holds the `BottomNavBar`, FAB (opens the quick‑add sheet), and swaps between Home, Analytics placeholder, and Profile placeholder based on selected tab.                     |
| `navigation/BottomNavBar.kt`                | Implements the bottom navigation bar using Material 3 `NavigationBar` and `NavigationBarItem`.                                                                                                         |
| `navigation/NavTab.kt`                      | Enum defining three tabs (`HOME`, `ANALYTICS`, `PROFILE`) with route, title, and icon.                                                                                                                 |
| `component/QuickAddTransactionSheet.kt`     | Modal bottom sheet allowing the user to enter amount, select type (expense/income/transfer), pick a category (icon grid), and add an optional note. Communicates back via callbacks to `MainScaffold`. |
| `placeholder/AnalyticsPlaceholderScreen.kt` | Simple placeholder UI for the Analytics screen (future implementation).                                                                                                                                |
| `placeholder/ProfilePlaceholderScreen.kt`   | Simple placeholder UI for the Profile screen.                                                                                                                                                          |
| `AndroidManifest.xml`                       | Declares the application class, main activity, backup rules, etc.                                                                                                                                      |
| `res/values/strings.xml`                    | Contains the app name resource.                                                                                                                                                                        |
| `res/values/themes.xml`                     | Minimal theme definition; actual styling lives in the Compose `FinancetrackerTheme`.                                                                                                                   |
| `build.gradle.kts`                          | Application module – pulls in AndroidX, Compose, Navigation 3, Koin, Room, Retrofit, Coil, and all core/feature modules.                                                                               |

---

## 5. Build Configuration

- **Root `settings.gradle.kts`** – Registers modules `:app`, `:core:model`, `:core:database`, `:core:uikit`, `:feature:home`.
- **Root `build.gradle.kts`** – Declares plugin aliases (Android application/library, Kotlin Compose, serialization, KSP) and disables default version catalog. No direct dependencies here; each sub‑module manages its own.
- **`gradle/libs.versions.toml`** – Centralised version catalog for all third‑party libraries (AGP, Kotlin, Compose BOM, Koin, Room, Retrofit, Coil, etc.).
- **Gradle wrapper** – `gradle-wrapper.jar`/`gradle-wrapper.properties` allow reproducible Gradle builds.

---

## 6. Documentation & Iterations

The `iterations/` folder stores Markdown files (`iteration-1/README.md`, etc.) that document the development roadmap, completed tasks, and upcoming work. They are not part of the compiled app but useful for project management.

---

## 7. How the Pieces Fit Together (Runtime Flow)

1. **App start** – Android launches `MainActivity`. `FinanceApp`'s `onCreate` starts Koin, loading `databaseModule` (creates a singleton `AppDatabase`) and `homeModule` (provides `HomeViewModel`).
2. `MainActivity.setContent` applies `FinancetrackerTheme` and displays `MainScaffold`.
3. `MainScaffold` renders the `BottomNavBar`; the default tab is **Home**.
4. `HomeScreen` obtains `HomeViewModel` via `koinViewModel()`. The ViewModel combines flows from the three repositories into a `HomeUiState` which is observed via `collectAsStateWithLifecycle()`.
5. UI displays:
   - Total balance (computed from all accounts).
   - Accounts carousel using `HomeAccountCard` → `AccountCardItem`.
   - Recent transaction list using `HomeTransactionRow` → `TransactionRowItem`.
6. When the FAB is pressed, `showQuickAdd` toggles the `QuickAddTransactionSheet` modal.
7. The sheet collects user input, validates amount, and on "Add" calls `homeViewModel.addTransaction(...)`.
8. `HomeViewModel.addTransaction` creates a `Transaction` domain object and asks `TransactionRepository` to insert it.
9. `TransactionRepository.addTransaction`:
   - Converts the domain model to a `TransactionEntity`.
   - Computes the balance delta based on transaction type.
   - Calls `AccountDao.updateBalance` to adjust the account.
   - Inserts the transaction via `TransactionDao.insertTransaction`.
10. Room’s `Flow`s automatically emit new data; `HomeViewModel` recomputes `HomeUiState`, causing the UI to re‑render with updated balances and transaction list.

---

## 8. Key Design Decisions

- **Feature‑based multi‑module**: each functional area lives in its own Gradle module, making the codebase scalable and allowing independent testing.
- **Clean Architecture**: domain models (`core:model`) are pure Kotlin data classes, completely decoupled from the persistence layer (`core:database`).
- **Koin for DI**: lightweight and Kotlin‑friendly; modules are defined close to where they are used (DatabaseModule, HomeModule).
- **Jetpack Compose + Material 3**: UI is fully declarative; theming is handled by a custom `FinancetrackerTheme` that mirrors the Material design system.
- **Room with Flow**: reactive data streams ensure UI stays in sync with the database without manual callbacks.
- **Pre‑populate database**: the `Callback` in `AppDatabase` adds a default “Основная карта” account and a handful of categories on first run, providing a ready‑to‑use demo experience.

---

## 9. Future Extension Points

| Area                   | What to add                                                                                                                           |
|------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| **Analytics**          | Replace `AnalyticsPlaceholderScreen` with proper composables (charts, filters) using libraries like MPAndroidChart or Compose‑Charts. |
| **Profile / Auth**     | Implement user authentication, cloud sync, and profile management (currently placeholder).                                            |
| **Networking**         | Existing Retrofit/OkHttp dependencies are ready for a sync service – create API interfaces and repository wrappers.                   |
| **Testing**            | Add unit tests for repositories (using in‑memory Room) and UI tests (Compose test framework).                                         |
| **DI Expansion**       | Extract feature‑specific modules (`feature:analytics`, `feature:profile`) each with their own Koin modules.                           |
| **Modular Navigation** | Migrate from the simple enum‑based navigation to Navigation 3 with typed destinations and arguments.                                  |

---

*Document generated automatically on* **2026‑10‑30** *by Antigravity.*
