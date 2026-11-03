# 📚 Документация проекта «Трекер личных финансов»

Добро пожаловать в базу знаний и архитектурную документацию проекта. Ниже представлены подробные руководства по ключевым слоям приложения.

---

## 📑 Разделы документации

1. [**🎨 UI Kit и Дизайн-система (`docs/uikit-components.md`)**](uikit-components.md)
   - Архитектурные правила построения компонентов.
   - Каталог всех UI-элементов: карточки счетов, чипы категорий, специализированные поля ввода, кнопки, пружинный селектор типа операции, компонент денежной суммы и базовая адаптивная шторка `AppFormBottomSheet`.
   - Оптимизация производительности: утилита `AmountFormatter`, исключение лишних рекомпозиций и нагрузки на GC.

2. [**🗄️ База данных и слой хранения (`docs/database-architecture.md`)**](database-architecture.md)
   - Схема таблиц Room (`accounts`, `categories`, `transactions`).
   - Внешние ключи (Foreign Keys), каскадное удаление и защита от рассинхронизации.
   - Атомарность операций через `appDatabase.withTransaction`.
   - Безопасная инициализация базы без гонок потоков и автопосев категорий.
   - Внедрение зависимостей через Koin (`databaseModule`).

3. [**🏛️ Общая архитектура приложения (`docs/app-architecture.md`)**](app-architecture.md)
   - Многомодульная структура (`:app`, `:feature:home`, `:core:database`, `:core:uikit`, `:core:model`).
   - Однонаправленный поток данных (Unidirectional Data Flow) и связка `Flow` $\rightarrow$ `HomeViewModel` $\rightarrow$ `StateFlow` $\rightarrow$ `Compose`.
   - Паттерн Stateful/Stateless для `MainScaffold` и быстрая работа Compose Preview.
   - Цепочки вызова модальных окон (BottomSheet Chaining) и адаптивный Edge-to-Edge дизайн.

---

## 📌 Отчёты по итерациям

- [Итерация 1: Формирование ТЗ и прототипирование](../iterations/iteration-1/README.md)
- [Итерация 2: Scaffolding, зависимости и чистая архитектура](../iterations/iteration-2/README.md)
- [Итерация 3: Многомодульность, Room DB и навигация](../iterations/iteration-3/README.md)
