# CI: проверки, мерж, релиз

Путь изменения: ветка агента → PR в `dev` (CI) → автослияние → CI на `dev` → кнопка релиза → PR `dev` → `main` → CI на `main`
публикует сборку. Цель — не делать одну и ту же работу дважды (#224).

## Воркфлоу
- `ci.yml` — push в `main`/`dev`, PR (кроме черновиков: сборка на `ready_for_review`), вручную (Actions → «Сборка и тесты (CI)» → Run workflow).
  Gradle пропускается, если изменены только `docs/`, `*.md`, `scripts/agent/`.
- **Минуты Actions** (#251, #253): приватный репозиторий — 2000 мин/мес, каждая задача оплачивается минимум минутой.
  Не добавляй запусков на каждый push/метку; пропуск — через `if:` у задачи (пропущенная не оплачивается).
- `auto-pr.yml` — draft PR на push, создающий агентскую ветку (CI не зовёт); `automerge.yml` — мерж в `dev` после зелёного CI
  (логика — `scripts/ci/automerge.sh`); `release.yml` — кнопка релиза (описание — `scripts/release_notes.py`).
- Автомерж зовут трое: `ready_for_review`/метки (`automerge.yml`), `workflow_run` и **последний шаг зелёного CI ветки**
  (`CI_GREEN_SHA`). Прогон CI, запущенный ботом (dispatch), `workflow_run` не порождает — без шага
  PR, снятый с черновика до конца CI, висел (#223, #229).
- Мерж/PR через `GITHUB_TOKEN` **не запускает** `push`/`pull_request`-workflow — поэтому они вызывают
  `gh workflow run ci.yml` (dispatch разрешён). `workflow_run` и кнопки работают из копии в `main`: правка
  `automerge.sh`/`automerge.yml`/`release.yml` действует после релиза.

## Что где проверяется
| Где | Что | Время (11.2026) |
|---|---|---|
| PR в `dev`, ветка | тесты скриптов + `check assembleDebug` одним вызовом, debug APK в артефактах (14 дней) | ~5,5 мин |
| PR, только `docs/` и `*.md` | только тесты скриптов (шаг «Detect code changes») | <1 мин |
| `dev` после мержа | Gradle **пропускается**, если дерево merge-коммита = дереву головы PR и там CI зелёный (`scripts/ci/tree-already-checked.sh`), кэш обновляет фоновый `warm-cache.yml`; иначе как PR | <1 мин |
| `main` (релиз) | без lint и тестов (дерево проверено на `dev`); `assembleDebug assembleRelease` **одним** вызовом Gradle | |

- **`BUILD_NUMBER` только на `main`.** Он попадает в `versionCode` (BuildConfig, манифест); новый номер на каждом прогоне
  выбивал из кэша все задачи `:app` (88 из 144 на PR только со скриптами, run 320). APK из PR — `versionCode` 1.
- «Не все тесты» обеспечивает build cache: задачи неизменённых модулей приходят FROM-CACHE (кэш пишут `main` и `dev`,
  PR стартуют с тёплым). Делить `check` на группы вручную не нужно — Gradle и так гонит модули параллельно.
- Release unit-тесты не запускаются (`check` берёт только debug), lintVital выключен (`lint.checkReleaseBuilds = false`).
- Скриншот-тесты дизайн-системы сравниваются в обычном `check` (`build-and-test.md` §«Скриншот-тесты»).

## Дубли и отмена
- `concurrency: ci-<ветка>` (`head_ref || ref_name`): dispatch из `auto-pr.yml` и `pull_request` того же коммита — одна
  группа, новый прогон отменяет старый. Раньше группы были `refs/heads/…` и `refs/pull/N/merge` и шли параллельно.
- `automerge.sh` и `release.yml` считают коммит зелёным, если **есть** успешный прогон (отменённые и пустые дубли не мешают).

## Кэши и секреты
- `warm-cache.yml` — `check assembleDebug` на `dev` с записью кэша, когда CI на `dev` пропустил Gradle; кнопка релиза
  его не ждёт. Dispatch работает, только когда файл уже есть в `main` (до релиза — предупреждение в логе).
- `android-all` для Robolectric кэшируется отдельно (`~/.m2/repository/org/robolectric`, `actions/cache`).
- Необязательный секрет `GRADLE_ENCRYPTION_KEY` (`openssl rand -base64 16`) включает сохранение configuration cache.

## Релизы и отчёты
- На push в `main` — GitHub Release `build-<N>` с `co-shell-debug.apk` (и `co-shell.apk`, если включено
  самообновление); последняя — `https://github.com/winerar/co-shell/releases/latest` (репозиторий приватный).
- Упавший `check` пишет короткую сводку (`scripts/agent/build-summary.sh --ci`) в summary джоба и одним
  комментарием `<!-- ci-summary -->` в PR (обновляется; после зелёного — «исправлено»).
