# 🗄️ Слой данных и архитектура базы данных (`:core:database`)

Модуль `:core:database` отвечает за надёжное локальное хранение, реактивное чтение и транзакционную модификацию данных приложения на базе **Room** и **SQLite**.

---

## 🏗️ Общая схема базы данных

База данных `finance_tracker.db` управляется классом `AppDatabase` (актуальная версия схемы — **2**).

```
 ┌────────────────┐          ┌──────────────────────┐
 │    accounts    │ 1      * │     transactions     │
 ├────────────────┤◄─────────┼──────────────────────┤
 │ id (PK)        │          │ id (PK)              │
 │ name           │          │ accountId (FK)       │────► accounts.id (CASCADE)
 │ balance        │          │ targetAccountId (FK) │────► accounts.id (SET_NULL)
 │ currency       │          │ categoryId (FK)      │────► categories.id (SET_NULL)
 │ colorHex       │          │ amount               │
 └────────────────┘          │ type (ENUM)          │
                             │ dateTime             │
 ┌────────────────┐          │ note                 │
 │   categories   │ 1      * │                      │
 ├────────────────┤◄─────────┘                      │
 │ id (PK)        │                                 │
 │ name           │                                 │
 │ type (ENUM)    │                                 │
 │ iconRes        │                                 │
 │ budgetLimit    │                                 │
 └────────────────┘                                 │
```

---

## 📋 Таблицы и Сущности (`entity/`)

### 1. `AccountEntity` (Таблица `accounts`)
- `id: Long` — первичный автоинкрементный ключ.
- `name: String` — наименование счёта («Основная карта», «Наличные»).
- `balance: Double` — текущий остаток на счёте.
- `currency: String` — валюта (по умолчанию `"RUB"`).
- `colorHex: String` — цвет оформления карточки (HEX-код).

### 2. `CategoryEntity` (Таблица `categories`)
- `id: Long` — первичный автоинкрементный ключ.
- `name: String` — название категории («Продукты», «Кафе», «Зарплата»).
- `type: CategoryType` — тип (`EXPENSE`, `INCOME`).
- `iconRes: String` — идентификатор иконки (`"shopping_cart"`, `"restaurant"`).
- `budgetLimit: Double?` — опциональный месячный лимит расходов.

### 3. `TransactionEntity` (Таблица `transactions`)
- `id: Long` — первичный автоинкрементный ключ.
- `accountId: Long` — счёт списания (внешний ключ к `accounts.id`).
- `targetAccountId: Long?` — счёт зачисления для операций типа `TRANSFER` (внешний ключ к `accounts.id`).
- `categoryId: Long?` — категория операции (внешний ключ к `categories.id`). Для перевода строго `null`.
- `amount: Double` — сумма транзакции.
- `type: TransactionType` — тип операции (`EXPENSE`, `INCOME`, `TRANSFER`).
- `dateTime: Long` — метка времени в миллисекундах (UTC).
- `note: String?` — текстовый комментарий к операции.

---

## 🔒 Внешние ключи и целостность данных (Foreign Keys)

В `AppDatabase.Callback` при каждом открытии базы принудительно активируется поддержка внешних ключей:
```sql
PRAGMA foreign_keys = ON;
```

### Правила каскадирования:
1. **`accountId -> accounts.id` (`onDelete = CASCADE`):**
   При удалении счёта все привязанные к нему операции автоматически удаляются из истории.
2. **`categoryId -> categories.id` (`onDelete = SET_NULL`):**
   При удалении категории операция сохраняется в истории, но её `categoryId` сбрасывается в `NULL` (отображается как «Разное» или «Без категории»).
3. **`targetAccountId -> accounts.id` (`onDelete = SET_NULL`):**
   При удалении целевого счёта операция перевода сохраняется в исходном счёте, а ссылка на второй счёт обнуляется.

---

## ⚡ Транзакционность и атомарность (ACID)

Для операций изменения счетов и создания транзакций критически важна атомарность:
В `TransactionRepositoryImpl.kt` все действия объединены в атомарный блок Room:

```kotlin
suspend fun addTransaction(transaction: Transaction): Long = appDatabase.withTransaction {
    val entity = transaction.toEntity()

    // 1. Вставляем запись об операции
    val insertedId = transactionDao.insertTransaction(entity)

    // 2. Корректируем балансы счетов
    when (transaction.type) {
        TransactionType.EXPENSE -> {
            accountDao.updateBalance(entity.accountId, -transaction.amount)
        }
        TransactionType.INCOME -> {
            accountDao.updateBalance(entity.accountId, transaction.amount)
        }
        TransactionType.TRANSFER -> {
            accountDao.updateBalance(entity.accountId, -transaction.amount)
            entity.targetAccountId?.let { targetId ->
                accountDao.updateBalance(targetId, transaction.amount)
            }
        }
    }

    insertedId
}
```

> [!IMPORTANT]
> Благодаря `appDatabase.withTransaction`:
> Если на этапе обновления баланса целевого счёта произойдёт ошибка или процесс будет завершён системой, SQLite автоматически выполнит **ROLLBACK** — ни транзакция, ни списание не сохранятся, гарантируя 100% целостность данных.

---

## 🔄 Инициализация и автопосев данных (Data Seeding)

1. **Создание дефолтного счёта (`onCreate`):**
   Выполняется синхронно напрямую через `SQLiteConnection.execSQL` без использования корутин и `lateinit`:
   ```sql
   INSERT INTO accounts (name, balance, currency, colorHex) 
   VALUES ('Основная карта', 0.0, 'RUB', '#0F5C4A');
   ```
2. **Динамический посев категорий (`CategoryRepository.checkAndSeedDefaultCategories`):**
   При первом старте экрана `HomeViewModel` вызывает проверку: если таблица `categories` пуста (`getCount() == 0`), автоматически загружаются 9 предустановленных категорий. Это защищает базу от рассинхронизации при деструктивных миграциях.

---

## 💉 Внедрение зависимостей (Koin DI)

Модуль `databaseModule` предоставляет экземпляры базы и репозиториев как синглтоны:
```kotlin
val databaseModule = module {
    single { AppDatabase.buildDatabase(androidContext()) }
    single { get<AppDatabase>().accountDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().transactionDao() }

    single { AccountRepository(accountDao = get()) }
    single { TransactionRepository(appDatabase = get(), transactionDao = get(), accountDao = get()) }
    single { CategoryRepository(categoryDao = get()) }
}
```
