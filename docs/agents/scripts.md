# Скрипты агента `scripts/agent/`

Всё вызывается через **`scripts/agent/agent.sh <команда>`** (без аргументов — список по-русски); скрипты
работают и сами по себе. Вывод короткий: только ошибки, итог и подсказка «что дальше». Новый скрипт —
команда в `agent.sh` + строка здесь (`workflow.md` §«Самоулучшение»). Bash/python3, без новых зависимостей.

## Флоу — цепочки, которые всегда идут подряд (`flow.sh`, стоп на первой ошибке)
| Команда | Шаги | Когда |
|---|---|---|
| `start [--all]` | fetch → свежие (3 дня) чужие ветки не в `dev`/`main` (старше — только счёт) → перенос своей пустой ветки на `origin/dev` → STATUS (строки ≤200 знаков) → задачи без бэклога (грузятся параллельно); ~3 с | первая команда сессии |
| `ship -m "Msg" [-m трейлер] [--quick\|--affected\|--no-check] [--recheck]` | `check` (полный по умолчанию) → `git add -A` → коммит (+ `Agent-Check: full`, если файлы прошли полный `check`: CI в PR без Gradle) → `push` | каждый коммит |
| `update` | `sync` (влить `origin/dev`) → `affected` → `push` | бот написал о конфликте, `dev` ушёл вперёд |
| `finish [-m "Msg"] [--no-check] [--recheck]` | `stamp` → правила доков (`end --pre`, за секунду) → `check` → коммит → `push` → `end` | конец сессии, после правки текста STATUS |
| `pr` (`pr.sh`) | проблемы PR (не запушено, `dev` ушёл вперёд по коду, нет `Agent-Check`) → номер PR → заголовок и описание → шаги MCP до мержа | после `finish` |

`check`/`quick` в флоу **пропускается**, если эти же файлы (или они же плюс только `docs/**`, `*.md`) уже прошли
проверку не слабее (`check-cache.sh`, хэш дерева в `build/agent-check/`): `finish` после `ship` не ждёт 3–6 мин.
`--recheck` — прогнать всё равно. Трейлеры (Co-Authored-By и т. п.) — дополнительными `-m`. После `finish` — `agent.sh pr` и его шаги MCP (`workflow.md` §«Git»).

## Отдельные команды
| Команда (скрипт) | Что делает |
|---|---|
| `end` (`session-end.sh`) | только проблемы (✗) и напоминания: незакоммиченное/незапушенное, дата и число тестов в STATUS, размеры файлов, задачи in-progress; `--pre` — только правила доков, без git и сети |
| `check-cache.sh record\|fresh <full\|quick>` | какое дерево файлов прошло `check.sh` (пишет сам `check.sh` без доп. аргументов); `fresh` — можно ли пропустить повтор |
| `tasks [статус…]` (`tasks.sh`) | открытые задачи по колонкам (`gh api search/issues`); со статусами — счётчик скрытых колонок |
| `issue <N> [строк]` (`issue.sh`) | одна задача или PR: заголовок, метки, описание (обрезано) — дешевле MCP; комментарии — MCP `issue_read` |
| `doc <файл> [раздел]` (`section.sh`) | один раздел Markdown с номерами строк; без раздела — оглавление |
| `changes` | коммиты и файлы ветки относительно `dev`, незакоммиченное |
| `sync` (`sync-dev.sh`) | влить `origin/dev`: «OK» или конфликтующие файлы с подсказками (STATUS, схемы Room); на хотфикс-ветке отказ |
| `hotfix` (`hotfix.sh`) | срочный фикс сбоя: ветка сессии → `origin/main` (только чистая), напоминание шагов — `hotfix.md` |
| `push` (`push.sh`) | push с повтором при сетевых ошибках (2, 4, 8, 16 с) **в ветку, которую локальная отслеживает на origin** (иное имя локальной ветки не плодит `origin/<имя>`, #460); `main`/`dev` не пушит; тест `scripts/agent/test_push.py` |
| `stamp` (`stamp-status.sh`) | STATUS: «Обновлено» = сегодня, «всего N» = `test-count.sh` |
| `test-count.sh` | число тестов последнего прогона, только если есть результаты всех 5 модулей (частичный прогон не годится) |

## Сборка и тесты (Gradle — только через `gradle.sh`: зеркало Maven, повтор при 429, #156)
| Команда (скрипт) | Что делает |
|---|---|
| `check` (`check.sh`) | перед коммитом: `check assembleDebug`, краткий отчёт (`build-summary.sh`); ~3–6 мин |
| `quick` (`check.sh --quick`) | unit-тесты всех 5 модулей (с `core:designsystem`); ~6 мин с холодным демоном |
| `affected [--list]` (`affected.sh`) | unit-тесты затронутых в ветке модулей и зависящих: model → domain → data → app, designsystem → app; только доки — ничего |
| `test <model\|domain\|data\|ds\|app> [фильтр]` | тесты одного модуля |
| `gradle <задачи>` (`gradle.sh`) | любой вызов вместо `./gradlew` |
| `profile [задачи]` (`profile-build.sh`) | где тратится время сборки (~3–5 мин) |
| `build-summary.sh [--ci] <лог>` | ошибки, `Tests: N`, упавшие тесты с причиной, lint; зовут `check.sh` и CI |

## Дизайн и данные
| Команда (скрипт) | Что делает |
|---|---|
| `mockups` (`gen-mockups.py` + `check-mockups.js`) | пересобрать холст: **все** доски из кирпичей (`mockup_bricks.py` → `mockup_parts.py` → `mockup_screens_*.py`, `mockup_catalog*.py`), темы, `canvas.json` (ряды — `LAYOUT`); затем проверка: у каждой `{{дырки}}` есть значение, каждый тап переключает состояние; `test_mockup_rules.py` — нижняя панель досок = код, без теней |
| `mockup_theme.py <доска>…` | перевести цвета доски (рисуем во «Вольт · тёмная») на токены темы; повтор безопасен |
| `render <доски…>` (`render-mockups.js`) | PNG досок без холста в scratchpad (упрощённый рантайм) |
| `poll <spec.json>` (`poll-page.py` + `poll-template.html`) | опрос владельца по макетам: варианты-радио с превью досок, «рекомендую», поле правок; `Доска@light` — та же доска в светлой теме; `--img <dir>` — готовые PNG вместо досок холста (иконка, палитры); ответы — в БД артефакта (`workflow.md` §«Визуальные изменения») |
| `python3 scripts/agent/alert_colors.py [--html dir]` | цвет внимания (карточки `Warning`) под каждую из 9 тем: оттенок, далёкий от цветов темы, текст ≥ 4,5; `--html` — превью для опроса |
| `python3 scripts/agent/palette-distinct.py` | различимость цветов категорий (ΔE2000, обе темы), предложение набора; данные для досок #105 |
| `flows` (`gen-flows-board.py`) | только доска «Воркфлоу» (таблица `ROWS`); высоту в `canvas.json` ставит `mockups` |
| `bankcolors [--sheet f.png]` (`bank-colors.py`) | цвета банков из `BankColors.kt` против иконок сервисов (фавикон Google/DuckDuckGo → частый насыщенный цвет); таблица + контактный лист расхождений, **код не правит**; нужны сеть и Pillow; тест `test_bank_colors.py` (#505) |
| `backup [-o файл]` (`gen-test-backup.py`) | тестовый бэкап ~9 тыс. операций (`--months`, `--per-day`, `--seed`) |
| `wallet [строк]` (`gen-wallet-export.py`) | синтетическая выгрузка Wallet (10 000 строк) в scratchpad (#131) |
| `scale` | замер на 10 тыс. операций: импорт, список, синк, размер БД |
| `stability` (`compose-stability.py`) | отчёты компилятора Compose для `app` (`--rerun`, ~1,5 мин) → падает на нестабильном классе состояния (`*UiState*`, `*State`) (#383) |

Скрипты CI и для владельца — в `scripts/` (`feedback_to_issues.py`, `make-update-files.py`,
`material-symbol-to-vector.py`, `test-rules.sh`) и `scripts/ci/`.

## Хуки Claude Code (`hook-*.sh`; подключаются в `.claude/settings.json`)
| Скрипт | Событие | Что делает |
|---|---|---|
| `hook-session-start.sh` | SessionStart | `agent.sh start` автоматически, вывод — в контекст (правило 7 «Начало») |
| `hook-edit-guard.sh` | PostToolUse `Edit\|Write` | правки `.kt`: без `android.*`/`java.*` и `Double`/`Float` в `core:model`/`domain`, без русских литералов в `app` — сразу, не через `check`; нарушение → exit 2 |
| `hook-stop.sh` | Stop | если есть незакоммиченное/незапушенное — один раз блокирует остановку списком ✗ из `end --pre` |

Правила проекта по путям — `.claude/rules/*.md` (`paths:` + `@docs/agents/…`): подгружаются в контекст только при работе с
подходящими файлами (деньги/KMP → `core/**`, БД → `core/data`, дизайн → `core/designsystem` и `app`, тексты → `app`…).
Источник истины — `docs/agents/`; новый файл правил добавь в подходящий `.claude/rules/*.md`.
