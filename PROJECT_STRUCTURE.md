# Finance Tracker – Project Structure Overview

This document provides a detailed walkthrough of the **Finance Tracker** Android application source tree. All paths are absolute (Windows style) and link directly to the corresponding files for quick navigation.

---

## 1. High‑Level Layout

```
finance_tracker/                     # Root of the repository
├─ .gitignore                       # Git ignore rules
├─ README.md                        # Project description
├─ PROJECT_STRUCTURE.md             # Architecture & module map
├─ settings.gradle.kts              # Gradle settings – registers all 8 modules
├─ build.gradle.kts                 # Top‑level Gradle script (plugins, repositories)
├─ gradle/                          # Gradle wrapper & version catalog
│   ├─ libs.versions.toml           # Version catalog for dependencies
│   └─ wrapper/                     # Gradle wrapper binaries
├─ app/                             # Application root module (composite root / APK assembler)
│   ├─ build.gradle.kts
│   └─ src/main/
│       ├─ AndroidManifest.xml
│       └─ java/com/example/financetracker/
│           ├─ FinanceApp.kt        # Application class – Koin start‑up
│           ├─ MainActivity.kt      # Host activity – edge-to-edge Compose host
│           └─ navigation/          # Navigation scaffolding
│               ├─ MainScaffold.kt   # Root Scaffold orchestrator (HorizontalPager, NavDisplay, BottomSheets)
│               ├─ BottomNavBar.kt   # Material 3 Bottom Navigation Bar
│               └─ NavTab.kt         # Enum of root tabs (HOME, ANALYTICS, PROFILE)
├─ core/                            # Shared lower-level foundation modules
│   ├─ navigation/                  # Navigation contracts & typed route keys
│   │   ├─ build.gradle.kts
│   │   └─ src/main/java/com/example/financetracker/core/navigation/
│   │       └─ ScreenKey.kt         # Sealed interface ScreenKey (Navigation 3 destinations)
│   ├─ model/                       # Pure Kotlin domain entities & repository interfaces
│   │   ├─ build.gradle.kts
│   │   └─ src/main/java/com/example/financetracker/core/model/
│   │       ├─ Account.kt
│   │       ├─ AccountType.kt
│   │       ├─ Category.kt
│   │       ├─ CategoryType.kt
│   │       ├─ Transaction.kt
│   │       ├─ TransactionType.kt
│   │       └─ repository/          # Domain repository contracts (AccountRepository, etc.)
│   ├─ database/                    # Room persistence implementation (hidden behind interfaces)
│   │   ├─ build.gradle.kts
│   │   └─ src/main/java/com/example/financetracker/core/database/
│   │       ├─ AppDatabase.kt
│   │       ├─ dao/
│   │       ├─ entity/
│   │       ├─ repository/          # Repository implementations (AccountRepositoryImpl, etc.)
│   │       └─ di/DatabaseModule.kt
│   └─ uikit/                       # Reusable UI design system & components
│       ├─ build.gradle.kts
│       └─ src/main/java/com/example/financetracker/core/uikit/
│           ├─ components/          # Account cards, category chips, inputs, sheets, text
│           ├─ theme/               # Color, Theme, Type
│           └─ util/                # AmountFormatter, AmountVisualTransformation
└─ feature/                         # Horizontally-isolated feature modules (compiled in parallel)
    ├─ home/                        # Home tab – dashboard, accounts carousel, transactions, details
    │   ├─ build.gradle.kts
    │   └─ src/main/java/com/example/financetracker/feature/home/
    │       ├─ HomeScreen.kt
    │       ├─ AccountDetailScreen.kt
    │       ├─ CategoriesScreen.kt
    │       ├─ HomeViewModel.kt
    │       └─ components/sheet/
    ├─ analytics/                   # Analytics tab – diagrams, spending charts & statistics
    │   ├─ build.gradle.kts
    │   └─ src/main/java/com/example/financetracker/feature/analytics/
    │       └─ AnalyticsScreen.kt
    └─ profile/                     # Profile tab – settings, categories navigation, currency
        ├─ build.gradle.kts
        └─ src/main/java/com/example/financetracker/feature/profile/
            └─ ProfileScreen.kt
```

---

## 2. Architecture & Module Dependency Graph

The project follows a clean **3-layer horizontal modular architecture**. All feature modules (`:feature:*`) are completely decoupled from each other, allowing **100% parallel Gradle compilation**.

```mermaid
graph TD
    subgraph Layer 1: Application (Composite Root)
        APP[":app<br>(MainActivity, FinanceApp, MainScaffold)"]
    end

    subgraph Layer 2: Feature Layer (Compiled in Parallel)
        F_HOME[":feature:home<br>(Dashboard, Accounts, Details)"]
        F_ANALYTICS[":feature:analytics<br>(Charts, Statistics)"]
        F_PROFILE[":feature:profile<br>(Settings, Categories)"]
    end

    subgraph Layer 3: Core Foundation
        C_NAV[":core:navigation<br>(ScreenKey contracts)"]
        C_UIKIT[":core:uikit<br>(Theme, Components)"]
        C_MODEL[":core:model<br>(Domain, Repo Interfaces)"]
        C_DB[":core:database<br>(Room DB, DAOs, Impls)"]
    end

    APP --> F_HOME
    APP --> F_ANALYTICS
    APP --> F_PROFILE
    APP --> C_DB
    APP --> C_UIKIT
    APP --> C_MODEL
    APP --> C_NAV

    F_HOME --> C_NAV
    F_HOME --> C_UIKIT
    F_HOME --> C_MODEL

    F_ANALYTICS --> C_NAV
    F_ANALYTICS --> C_UIKIT
    F_ANALYTICS --> C_MODEL

    F_PROFILE --> C_NAV
    F_PROFILE --> C_UIKIT
    F_PROFILE --> C_MODEL

    C_DB --> C_MODEL
    C_UIKIT --> C_MODEL
```

---

## 3. Core Modules

### 3.1 `:core:navigation`
*Provides typed Navigation 3 routes/keys without depending on heavy UI libraries.*
- `ScreenKey.kt`: `@Serializable` sealed interface defining destinations:
  - `ScreenKey.Home`
  - `ScreenKey.Analytics`
  - `ScreenKey.Profile`
  - `ScreenKey.AccountDetail(val accountId: String)`
  - `ScreenKey.Categories`

### 3.2 `:core:model`
*Pure Kotlin JVM library holding domain models and repository interfaces.*
- Pure entities: `Account`, `Transaction`, `Category`, `AccountType`, `CategoryType`, `TransactionType`.
- Repository interfaces: `AccountRepository`, `TransactionRepository`, `CategoryRepository`.
- No Android SDK or Compose dependencies — compiles in milliseconds.

### 3.3 `:core:database`
*Room implementation of the persistence layer.*
- Room database `AppDatabase`, entity tables, DAOs with Kotlin Coroutines `Flow`.
- Implements domain repository interfaces (`AccountRepositoryImpl`, etc.).
- Internal implementation details are hidden from feature modules via Koin DI.

### 3.4 `:core:uikit`
*Design system and shared UI widgets.*
- `theme/`: `Theme.kt`, `Color.kt`, `Type.kt`.
- `components/`: Account cards, category icons/chips, bottom sheet wrappers, formatted amount texts.
- `util/`: `AmountFormatter.kt`, `AmountVisualTransformation.kt`.

---

## 4. Feature Modules

Features are strictly isolated horizontally and do **not** depend on each other.

### 4.1 `:feature:home`
- `HomeScreen.kt`: Dashboard with total balance, accounts carousel, and recent transactions.
- `AccountDetailScreen.kt`: Account balance, operations filtered by account, editing and deletion.
- `CategoriesScreen.kt`: Income and expense category limits and management.
- `HomeViewModel.kt`: Orchestrates reactive `HomeUiState` using repository interfaces.

### 4.2 `:feature:analytics`
- `AnalyticsScreen.kt`: Visual diagrams, income/expense summaries, category breakdowns.

### 4.3 `:feature:profile`
- `ProfileScreen.kt`: Settings, profile info, entry points to category management and app preferences.

---

## 5. Application Module (`:app`)

- **`MainActivity.kt`**: Sets edge-to-edge window insets and hosts `MainScaffold`.
- **`FinanceApp.kt`**: `Application` class, bootstraps Koin DI (`databaseModule`, `homeModule`).
- **`navigation/MainScaffold.kt`**: Root orchestrator:
  - Hosts `HorizontalPager` with 3 tabs (`NavTab`).
  - Hosts `NavDisplay` for detailed screens (`AccountDetail`, `Categories`).
  - Holds modal bottom sheets (`QuickAddTransactionSheet`, `AddAccountBottomSheet`).
- **`navigation/BottomNavBar.kt`**: Material 3 bottom navigation bar with icons and animated transitions.

---

## 6. How Features Interact (Decoupled Pattern)

Feature modules never import each other. Their coordination is accomplished through:
1. **Shared Reactive Data (Single Source of Truth):**
   When `:feature:home` records a transaction via `TransactionRepository.addTransaction(...)`, Room DB emits new data via `Flow`. The `:feature:analytics` module, listening to the same repository flow, updates its charts automatically.
2. **Navigation via Route Keys & Host Orchestration:**
   When `:feature:profile` needs to open Categories, it invokes a callback (`onOpenCategories`). The root `:app` module receives it and navigates to `ScreenKey.Categories` via the back stack.
