# Инструменты окружения (облачная сессия)

Выяснено агентами: прокси, Maven 429, SDK, холст, GitHub. Вынесено из `notes.md` (2026-11-11).
- (11-09) Время шагов CI без логов: GitHub MCP `actions_list` → `list_workflow_runs` (`resource_id: ci.yml`),
  затем `list_workflow_jobs` (run id) — у каждого шага `started_at`/`completed_at`.
- (11-10) `mawk` считает `length` в байтах (кириллица ×2) — длину строк в символах мерить `python3`/`wc -m`.
- (11-10) `actionlint` без установки: бинарник из релиза `rhysd/actionlint` (v1.7.7) в scratchpad, `actionlint -shellcheck=`.
- (11-09) `gh` CLI в облачной сессии — только для поиска; всё остальное — GitHub MCP.
- (11-10) Задачи: `gh api -X GET search/issues -f q='repo:winerar/co-shell is:open label:status:next'` работает
  (на нём `tasks.sh`). Ветка по умолчанию в репо — **`dev`**: `Fixes #N` закрывает issue при мерже в `dev`.
  `sub_issue_write` отвечает ~15 КБ JSON — подзадачу создавать `issue_write` с `parent_issue_number`.
  `issue_write` с `parent_issue_number` не создаёт новые метки (ошибка «label not found»), без него — создаёт серые.
- (11-09) «Загрузи отзывы»: GitHub MCP `actions_run_trigger` (`run_workflow`, `feedback.yml`, ref `main`) —
  issue появляются через ~20 с; затем `list_issues` с `labels: [source:app]`, `state: OPEN`.
- (11-09) Долгую сборку — в фон (`run_in_background`), дождаться — `until grep -q …; do sleep 3; done` в фоне.
- (11-09, скрипт 11-10) Maven Central отвечает Gradle 429 на большие jar (Robolectric) — `scripts/agent/m2-mirror.sh
  <scratchpad> group:artifact:ver` (curl, зеркало googleapis) → `check.sh --init-script <scratchpad>/m2-init.gradle`.
  Нужен был `org.robolectric:nativeruntime-dist-compat:1.0.19`. Robolectric сам качает `android-all-instrumented`
  («Failed to fetch maven artifact») — `M2_REPO=~/.m2/repository scripts/agent/m2-mirror.sh <dir> <coord>`
  (11-10: `org.robolectric:android-all-instrumented:16-robolectric-13921718-i7`).
- (11-09) Maven Central из облачной сессии отвечает Gradle 429 на большие файлы (Robolectric), повтор не помогает,
  а curl качает тот же файл без ошибок. Обход без правок репо: jar+pom curl-ом в `<scratchpad>/m2/<group>/<id>/<ver>/`,
  init-скрипт с `settingsEvaluated { … def m = r.maven{…}; r.remove(m); r.addFirst(m) }` и
  `check.sh --init-script <файл>` (так прошёл `nativeruntime-dist-compat` 1.0.19). (11-09) curl тоже может
  получить 429 — повторить через 4 с или с `repo1.maven.org`; init-скрипт — `<scratchpad>/m2-init.gradle`. Если 429 и там — зеркало
  `maven-central.storage-download.googleapis.com/maven2` (сработало 11-09).
- (11-10) Robolectric сам качает `android-all-instrumented` и тоже ловит 429 («Failed to fetch maven artifact») —
  скачать curl-ом (зеркало googleapis) в `~/.m2/repository/org/robolectric/android-all-instrumented/<версия>/` (jar+pom).
- (11-10) Холст дизайна (`docs/design/README.md`) — артефакт типа Design: файлы `project/*.dc.html` + `canvas.json`;
  читать `Artifact read` с `paths`, публиковать с `root` (порядок — инструкции типа в ответе `read`). Вкладки = `pages`.
  `root` — папка, где файлы лежат как `project/<имя>` (скопировать в scratchpad); перед первой публикацией в сессии — `read` без `paths`.
- (11-11) Публикация на холст отказывает, если файлы не читались в этой сессии (даже удаляемые `null`): сначала `Artifact read`
  с `paths` всех трогаемых досок, сверить с `git show HEAD:` (`cmp`), потом publish. Список файлов брать не из `git status --short`:
  похожие доски git показывает как переименование `R old -> new` — старое имя теряется; удаляемые перечислить явно.
- (11-11) Варианты, темы и генератор досок холста — `docs/design/README.md` §«Правила для агента».
- (11-10) Конструктор с хвостовой лямбдой (`WalletImportUseCase(...) { "id" }`): новый параметр с дефолтом
  в конце перехватывает лямбду — в тестах передавать `ids = { … }` по имени.
- (11-10) В облачной сессии сайты банков (ameriabank.am) закрыты сетью — цвета брендов не сверить, только по памяти.
- (11-09) Первый запуск Robolectric качает `android-all-instrumented` (~160 МБ) в `~/.m2`; дальше `MainShellSmokeTest` ~8 с.
  Если тест падает «Failed to fetch maven artifact …android-all-instrumented…» (429) — скачать jar+pom curl-ом
  с зеркала GCS прямо в `~/.m2/repository/org/robolectric/android-all-instrumented/<ver>/` (сработало 11-09).

- (11-09) Посмотреть экран глазами без телефона: временный Robolectric-тест с `@GraphicsMode(NATIVE)`,
  `@Config(qualifiers = "w393dp-h1000dp-xxhdpi")`, `onRoot().captureToImage()` → PNG в scratchpad, `Read` PNG;
  перед `captureToImage` — `mainClock.advanceTimeBy(3000)` (лесенка входа). Тест потом удалить.
