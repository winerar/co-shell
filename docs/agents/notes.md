# Заметки агента

Факты, которые агент выяснил сам и которые экономят время следующей сессии: замеры, грабли,
приёмы. Не правила (они — в тематических файлах) и не история решений (ARCHITECTURE).
Пополнять в конце сессии; устаревшее — удалять. Файл ≤ ~120 строк: разросся — вынести тему.
Формат: `- (дата) факт — что из этого следует`.

## Служебные скрипты `scripts/agent/`
| Скрипт | Когда |
|---|---|
| `session-start.sh` | первая команда сессии: fetch, чужие невлитые ветки, перенос ветки на `origin/dev`, шаги из STATUS |
| `check.sh` | перед коммитом (`check assembleDebug`, краткий вывод); `--quick` — только unit-тесты |
| `session-end.sh` | перед финальным PR: незакоммиченное/незапушенное, дата и число тестов в STATUS, размеры файлов |
| `gen-test-backup.py [-o файл]` | тестовый бэкап JSON с большим объёмом данных (~9 тыс. операций за 24 мес., `--months`, `--per-day`, `--seed`); проверен импортом |
| `profile-build.sh [задачи]` | где тратится время сборки: самые долгие задачи, итог lint/тестов (~3–5 мин) |

Скрипты CI и для владельца остаются в `scripts/` (`feedback_to_issues.py`, `make-update-files.py`,
`material-symbol-to-vector.py`). Новый служебный скрипт — сюда же в таблицу.

## Сборка и CI (замеры 2026-11-09)
- (11-09) Чистая локальная `check assembleDebug` — ~3 мин; CPU по задачам: lint ~26%, KSP `core:data`
  дважды (android + jvm) ~65 с, `mergeExtDexDebug` ~45 с. Тесты выполняются быстро (~50 с суммарно).
- (11-09) CI до ускорения: PR ~5 мин (из них `check` ~4), релиз на `main` ~10 мин (`check` 3.5 +
  debug 1.7 + release 4). После PR winerar/co-shell#41 ожидается: правки документации ~1 мин, релиз ~6.
- (11-09) `gradle/actions/setup-gradle` по умолчанию пишет кэш **только с ветки по умолчанию** (`main`) —
  поэтому `cache-read-only` переопределён, чтобы писал и `dev`.
- (11-09) Configuration cache в CI сохраняется только с секретом `GRADLE_ENCRYPTION_KEY` (не задан).
- (11-09) `check` в Android-модулях запускает только debug-вариант (release unit-тесты не гоняются).

## Инструменты окружения
- (11-09) Время шагов CI без логов: GitHub MCP `actions_list` → `list_workflow_runs` (`resource_id: ci.yml`),
  затем `list_workflow_jobs` (run id) — у каждого шага `started_at`/`completed_at`.
- (11-09) `gh` CLI в облачной сессии — только для поиска; всё остальное — GitHub MCP.
- (11-09) «Загрузи отзывы»: GitHub MCP `actions_run_trigger` (`run_workflow`, `feedback.yml`, ref `main`) —
  issue появляются через ~20 с; затем `list_issues` с `labels: [feedback]`, `state: OPEN`.
- (11-09) Долгую сборку — в фон (`run_in_background`), дождаться — `until grep -q …; do sleep 3; done` в фоне.
- (11-09) Maven Central из облачной сессии отвечает Gradle 429 на большие файлы (Robolectric), повтор не помогает,
  а curl качает тот же файл без ошибок. Обход без правок репо: jar+pom curl-ом в `<scratchpad>/m2/<group>/<id>/<ver>/`,
  init-скрипт с `settingsEvaluated { … def m = r.maven{…}; r.remove(m); r.addFirst(m) }` и
  `check.sh --init-script <файл>` (так прошёл `nativeruntime-dist-compat` 1.0.19). (11-09) curl тоже может
  получить 429 — повторить через 4 с или с `repo1.maven.org`; init-скрипт — `<scratchpad>/m2-init.gradle`. Если 429 и там — зеркало
  `maven-central.storage-download.googleapis.com/maven2` (сработало 11-09).
- (11-10) Robolectric сам качает `android-all-instrumented` и тоже ловит 429 («Failed to fetch maven artifact») —
  скачать curl-ом (зеркало googleapis) в `~/.m2/repository/org/robolectric/android-all-instrumented/<версия>/` (jar+pom).
- (11-10) В облачной сессии сайты банков (ameriabank.am) закрыты сетью — цвета брендов не сверить, только по памяти.
- (11-09) Первый запуск Robolectric качает `android-all-instrumented` (~160 МБ) в `~/.m2`; дальше `MainShellSmokeTest` ~8 с.

- (11-09) Посмотреть экран глазами без телефона: временный Robolectric-тест с `@GraphicsMode(NATIVE)`,
  `@Config(qualifiers = "w393dp-h1000dp-xxhdpi")`, `onRoot().captureToImage()` → PNG в scratchpad, `Read` PNG;
  перед `captureToImage` — `mainClock.advanceTimeBy(3000)` (лесенка входа). Тест потом удалить.

## Грабли проекта
- (11-09) `rememberGraphicsLayer().record{}` хранит ссылки на слои детей, а не их копию — снимок «старого» кадра
  делать сразу `toImageBitmap()` до смены (так `motion/ThemeReveal.kt`), иначе дети перерисуются уже новыми.
- (11-09) Слияние нескольких веток: читать **весь** вывод `git merge` (не `tail`), перед коммитом —
  `git grep -n '^<<<<<<< '`; так маркер конфликта чуть не уехал в `ARCHITECTURE.md`.
- (11-09) `MaterialTheme(motionScheme = …)` и интерфейс `MotionScheme` в material3 **1.4.0 закрыты** (`internal`),
  в `1.5.0-rc01` открыты — поэтому версия material3 закреплена в `libs.versions.toml`; при выходе стабильной 1.5 — перейти на неё.
- (11-09) Пересечение касаний в Compose: родительский `detectTapGestures` получает тап по ребёнку, если тот не
  поглотил событие (`Surface` без `onClick` не поглощает) — скрим шторки сделан **соседом** под ней, а не родителем.
- (11-09) `AnimatedVisibility` внутри колонки с `spacedBy`: зазор появляется/исчезает рывком вместе с ребёнком —
  отступ класть внутрь раскрывашки, а её — в колонку, где всегда что-то видно (так в форме операции).
- (11-09) Lint `UnusedContentLambdaTargetStateParameter`: в `AnimatedContent` рисовать из параметра лямбды, а не из
  внешнего состояния; ключ анимации — `contentKey` (иначе уходящий кадр показывает уже новое состояние).
- (11-09) Облачные сессии создают ветки `ccr-*`/`claude/*`; сессия может закончиться, не открыв PR —
  `session-start.sh` показывает такие ветки (так нашёлся невлитый пункт (ж) в `ccr-08ccf27a-wf9rbo`).
- (11-09) `main` содержит только merge-коммиты релизов: «коммиты не в `dev`» считать с `--no-merges`.
- (11-09) Тест «пока следующая страница не пришла» нельзя писать на `UnconfinedTestDispatcher` — поток
  пересобирается синхронно; нужен обычный `runTest` + `runCurrent()` (так в `GrowingWindowTest`).
- (11-09) M3 красит `primary` и как заливку, и как текст (`TextButton`, фокус поля, `Tab`, «сегодня» в `DatePicker`).
  При светлом неоновом `primary` текстовые места переопределять на `accentInk()`, иначе текст исчезает на белом.
- (11-09) Firestore 429 (`Too Many Requests`) = исчерпана дневная квота Spark (50 тыс. чтений), а не частота.
  Опрос раз в 10 с: любое лишнее чтение × 360/ч × 2 телефона. Курсор pull — строго после последней записи
  (`FirestoreRemoteStore.Position`); тест на «пустые опросы ничего не читают» — `SyncEngineTest`.
