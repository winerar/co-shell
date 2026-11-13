#!/usr/bin/env bash
# One entry point for every routine agent action (#156). `scripts/agent/agent.sh` alone prints the list.
# Each command is a thin alias for a script in this folder — the scripts stay usable on their own.
set -uo pipefail
here="$(cd "$(dirname "$0")" && pwd)"
cd "$here/../.."

usage() {
    cat <<'TXT'
Команды агента (scripts/agent/agent.sh <команда> [аргументы]):

  Флоу (цепочки шагов; стоп на первой ошибке)
    ship -m "Msg" [-m …] [--quick|--affected|--no-check]
                          проверка (полная) → git add -A → коммит → push с повтором
    update                влить origin/dev → тесты затронутых модулей → push
    finish [-m "Msg"] [--no-check]
                          проверка → дата и число тестов в STATUS → коммит → push → чек-лист end

  Сессия
    start [--all]         начало: свежие чужие ветки (--all — все), перенос на dev, STATUS, задачи без бэклога
    end                   конец сессии: что не закоммичено/не запушено, STATUS, размеры файлов
    tasks [статус…]       задачи с доски по колонкам (next, in-progress, needs-owner, triage, backlog)
    issue <N> [строк]     одна задача: заголовок, метки, описание (дешевле MCP)
    sync                  влить origin/dev в текущую ветку, показать конфликты
    push                  push текущей ветки с повтором при сетевых ошибках (2–16 с)
    stamp                 STATUS: «Обновлено» = сегодня, «всего N» тестов из последнего прогона
    changes               что в ветке относительно dev: коммиты и файлы (кратко)

  Чтение (экономия токенов)
    doc <файл> [раздел]   один раздел Markdown (без раздела — список заголовков с номерами строк)

  Сборка и тесты (Gradle — только так: зеркало Maven и автоповтор при 429)
    check                 перед коммитом: lint + все тесты + debug APK, краткий отчёт
    quick                 только unit-тесты всех модулей (для итераций)
    affected [--list]     unit-тесты только затронутых в ветке модулей и зависящих от них
    test <модуль> [фильтр] тесты одного модуля, напр.: test app '*UpdateControllerTest'
                          (модули: model, domain, data, ds — designsystem, app)
    gradle <задачи…>      любой вызов Gradle вместо ./gradlew
    profile [задачи]      где тратится время сборки
    shots [фильтр]        перезаписать эталоны скриншот-тестов (core:designsystem и экраны app) после
                          намеренной смены вида; PNG — */src/test/screenshots/ (показать владельцу)

  Дизайн (холст)
    mockups               пересобрать доски холста из генераторов + проверка логики тапов
    render <доски…>       PNG досок без холста (тёмная, Вольт) в scratchpad
    poll <spec.json>      страница-опрос «выбери вариант» для владельца → scratchpad/poll (workflow.md)
    flows                 перерисовать только доску «Воркфлоу» (mockups делает это сам)

  Данные
    backup [-o файл]      тестовый бэкап на ~9 тыс. операций
    wallet [строк]        выгрузка Wallet на 10 тыс. строк в scratchpad (импорт на телефоне)
    scale                 замер на 10 тыс. операций: импорт, список, синк, размер БД (#131)
TXT
}

scratch() { echo "${CLAUDE_SCRATCHPAD:-${TMPDIR:-/tmp}}"; }

cmd="${1:-help}"; [ $# -gt 0 ] && shift
case "$cmd" in
    start) exec "$here/session-start.sh" "$@" ;;
    end) exec "$here/session-end.sh" "$@" ;;
    ship | update | finish) exec "$here/flow.sh" "$cmd" "$@" ;;
    tasks) exec "$here/tasks.sh" "$@" ;;
    issue) exec "$here/issue.sh" "$@" ;;
    sync) exec "$here/sync-dev.sh" "$@" ;;
    push) exec "$here/push.sh" "$@" ;;
    stamp) exec "$here/stamp-status.sh" "$@" ;;
    changes)
        git fetch -q origin dev 2>/dev/null
        echo "== commits not in dev ($(git rev-list --no-merges --count origin/dev..HEAD)), behind dev: $(git rev-list --count HEAD..origin/dev)"
        git log --no-merges --format='  %h %s' origin/dev..HEAD | head -15
        echo "== files"; git diff --stat=100 origin/dev...HEAD | tail -25
        [ -z "$(git status --porcelain)" ] || { echo "== not committed"; git status --short | head -20; } ;;
    doc) exec "$here/section.sh" "$@" ;;
    affected) exec "$here/affected.sh" "$@" ;;
    check) exec "$here/check.sh" "$@" ;;
    quick) exec "$here/check.sh" --quick "$@" ;;
    test)
        module="${1:?модуль: model, domain, data, ds, app}"; shift
        case "$module" in
            model|domain|data) task=":core:$module:jvmTest" ;;
            ds|designsystem) task=":core:designsystem:testDebugUnitTest" ;;
            app) task=":app:testDebugUnitTest" ;;
            *) echo "неизвестный модуль: $module"; exit 2 ;;
        esac
        if [ $# -gt 0 ]; then set -- --tests "$1"; fi
        log=$(mktemp)
        "$here/gradle.sh" "$task" "$@" -q --console=plain >"$log" 2>&1
        status=$?
        "$here/build-summary.sh" "$log"; rm -f "$log"
        [ $status -eq 0 ] && echo "OK: $task" || echo "FAILED (exit $status)"
        exit $status ;;
    gradle) exec "$here/gradle.sh" "$@" ;;
    profile) exec "$here/profile-build.sh" "$@" ;;
    shots)
        # The record task alone; verify from gradle.properties is switched off for this run.
        # A filter goes to both modules (`--tests` binds to the task before it); no match in one is fine.
        tasks=()
        for t in :core:designsystem:recordRoborazziDebug :app:recordRoborazziDebug; do
            tasks+=("$t"); [ $# -gt 0 ] && tasks+=(--tests "$1")
        done
        log=$(mktemp)
        "$here/gradle.sh" "${tasks[@]}" -Proborazzi.test.verify=false -q --console=plain >"$log" 2>&1
        status=$?
        "$here/build-summary.sh" "$log"; rm -f "$log"
        git status --short core/designsystem/src/test/screenshots app/src/test/screenshots
        [ $status -eq 0 ] && echo "OK: эталоны записаны — проверь PNG и покажи изменения владельцу" || echo "FAILED (exit $status)"
        exit $status ;;
    mockups) python3 "$here/gen-mockups.py" "$@" && exec node "$here/check-mockups.js" docs/design/mockups/*.dc.html ;;
    render)
        out="$(scratch)/shots"; mkdir -p "$out"
        files=(); for b in "$@"; do files+=("docs/design/mockups/${b%.dc.html}.dc.html"); done
        exec node "$here/render-mockups.js" "$out" dark Вольт "${files[@]}" ;;
    poll) exec python3 "$here/poll-page.py" "$1" "$(scratch)/poll" ;;
    flows) exec python3 "$here/gen-flows-board.py" "$@" ;;
    backup) exec python3 "$here/gen-test-backup.py" "$@" ;;
    wallet)
        out="$(scratch)/wallet-${1:-10000}.csv"
        python3 "$here/gen-wallet-export.py" "$@" >"$out" && echo "$out" ;;
    scale)
        "$here/gradle.sh" :core:data:jvmTest --tests '*ImportScaleTest*' --rerun -q --console=plain -i 2>&1 | grep -E 'SCALE|FAILED' ;;
    help|-h|--help) usage ;;
    *) echo "неизвестная команда: $cmd"; echo; usage; exit 2 ;;
esac
