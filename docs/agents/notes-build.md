# Заметки: сборка, CI, замеры

Часть `notes.md`. Формат: `- (дата) факт — что из этого следует`.

## Сборка и CI (замеры 2026-11-09)
- (11-12) Кончились минуты Actions: задачи падают за 2–4 с, `runner_id: 0`, логов нет (404), `billable` в API = 0 — это не код, а биллинг (#251).
- (11-09) Чистая локальная `check assembleDebug` — ~3 мин; CPU по задачам: lint ~26%, KSP `core:data`
  дважды (android + jvm) ~65 с, `mergeExtDexDebug` ~45 с. Тесты выполняются быстро (~50 с суммарно).
- (11-09) `gradle/actions/setup-gradle` по умолчанию пишет кэш **только с ветки по умолчанию** (`main`) —
  поэтому `cache-read-only` переопределён, чтобы писал и `dev`.
- (11-09) Configuration cache в CI сохраняется только с секретом `GRADLE_ENCRYPTION_KEY` (не задан).
- (11-09) `check` в Android-модулях запускает только debug-вариант (release unit-тесты не гоняются).

- (11-11) **10 000 строк Wallet (#131)**, JVM, файл БД (`agent.sh scale`): разбор 0,2 с, план 0,2–0,3 с,
  запись 0,2 с, список 0,08 с, балансы 0,01 с, синк 0,27 с (24 пакета); БД ≈ 5 МБ (≈ 0,5 КБ/операция).
  Телефон медленнее в разы; холодный запуск и прокрутку на 10k ещё замерить на телефоне (выгрузка — `gen-wallet-export.py`).

- (11-15) **Имена тестов в обратных кавычках — только ASCII в app:** «—» или кириллица в имени → класс лямбды
  `Test$имя$1.class`, компилятор падает `InvalidPathException: Malformed input` (локаль контейнера). Пиши «means», «shows».
- (11-15) **lint `MissingPermission` не видит проверку в своей функции** (`canNotify()`): `checkSelfPermission` — прямо перед `notify`.
- (11-19, #536) **Эмулятор CI: «Pixel Launcher isn't responding» поверх приложения** — смоук «tabs not on screen» без сбоя у нас; «Wait» не помогает
  (лаунчер зависает снова). Скрипт ставит `settings put global hide_error_dialogs 1` и закрывает чужое окно «Close app». Разбор: артефакт
  `emulator-logs` (`ui-fail.png`/`.xml`) — MCP `actions_get download_workflow_run_artifact` даёт временную ссылку, `curl` её скачивает.
