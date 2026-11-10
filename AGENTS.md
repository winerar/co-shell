# AGENTS.md — co-shell

Android-приложение для учёта семейного бюджета (2 пользователя, мультивалютность,
офлайн-first, синхронизация через Firestore). Kotlin + Jetpack Compose.

## Золотые правила
1. Деньги — только `Money` (`Long` минимальных единиц + `CurrencyCode`). Никаких `Double`/`Float`.
2. Room — единственный источник истины для UI. Сеть пишет в Room, а не в экран.
3. `core:model` и `core:domain` — чистый Kotlin (готовность к KMP/iOS): без `android.*` и `java.*`.
4. Любое изменение поведения — с unit-тестами. Перед коммитом: `scripts/agent/check.sh`.
5. Документация — часть изменения: поменял решение/структуру → обнови соответствующий файл ниже.
6. Маленькие файлы по одной теме (~200 строк), экран — пакет из блоков: `project-structure.md`.
7. **Начало сессии:** `scripts/agent/session-start.sh`, затем `owner.md` и `notes.md`. **Конец:**
   чек-лист `workflow.md` + `scripts/agent/session-end.sh`. Узнал новое — в `notes.md`; рутина
   повторилась — скрипт в `scripts/agent/`; ввёл практику — правило (`workflow.md` §«Самоулучшение»).

## Карта документации
| Файл | О чём |
|---|---|
| [docs/STATUS.md](docs/STATUS.md) | **текущее состояние, следующие шаги, открытые вопросы** |
| [docs/agents/owner.md](docs/agents/owner.md) | контекст продукта и предпочтения владельца |
| [docs/agents/notes.md](docs/agents/notes.md) | **заметки агента**: выясненные факты, грабли, замеры, служебные скрипты |
| [docs/agents/project-structure.md](docs/agents/project-structure.md) | модули, что где лежит, куда класть новое |
| [docs/agents/build-and-test.md](docs/agents/build-and-test.md) | сборка, тесты, SDK, CI |
| [docs/agents/code-style.md](docs/agents/code-style.md) | стиль Kotlin/Compose, слои, UDF |
| [docs/agents/money-and-currency.md](docs/agents/money-and-currency.md) | правила работы с деньгами и валютами |
| [docs/agents/database.md](docs/agents/database.md) | Room: схема, мягкое удаление, миграции, резервная копия |
| [docs/design/](docs/design/README.md) | **макеты всех экранов, воркфлоу и компонентов** (холст claude.ai + копии) |
| [docs/agents/design-system.md](docs/agents/design-system.md), [components.md](docs/agents/components.md), [fonts.md](docs/agents/fonts.md) | дизайн-система: токены, цвета, навигация; компоненты; шрифты |
| [docs/agents/motion.md](docs/agents/motion.md) | движение: тактильная система, анимации, вибрация |
| [docs/agents/performance.md](docs/agents/performance.md) | лёгкость, память, плавность 120 Гц: правила и цели |
| [docs/agents/kmp-readiness.md](docs/agents/kmp-readiness.md) | как не закрыть дорогу к iOS |
| [docs/agents/workflow.md](docs/agents/workflow.md) | git, коммиты, язык, обновление документации |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | архитектура и журнал решений |
| [docs/ROADMAP.md](docs/ROADMAP.md) | план и текущий этап |
| [docs/research/](docs/research/), [docs/setup/](docs/setup/) | исследования; инструкции для владельца |

## Как поддерживать эти файлы
- `AGENTS.md` — только индекс и золотые правила, **не длиннее ~40 строк**.
- Каждый файл в `docs/agents/` — одна тема, **не длиннее ~120 строк**. Разросся — дели на
  подтемы (`money-and-currency.md` → `money.md` + `exchange-rates.md`) и обнови таблицу выше.
- Обновлять при каждом изменении, которое делает написанное неверным или неполным.
- Пиши кратко и проверяемо: команды, пути, правила. Историю решений — в ARCHITECTURE §«Принятые решения».
