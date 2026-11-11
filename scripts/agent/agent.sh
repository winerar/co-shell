#!/usr/bin/env bash
# One entry point for every routine agent action (#156). `scripts/agent/agent.sh` alone prints the list.
# Each command is a thin alias for a script in this folder — the scripts stay usable on their own.
set -uo pipefail
here="$(cd "$(dirname "$0")" && pwd)"
cd "$here/../.."

usage() {
    cat <<'TXT'
Команды агента (scripts/agent/agent.sh <команда> [аргументы]):

  Сессия
    start                 начало сессии: свежий dev, чужие ветки, STATUS, доска задач
    end                   конец сессии: что не закоммичено/не запушено, STATUS, размеры файлов
    tasks [статус…]       задачи с доски по колонкам (next, in-progress, needs-owner, triage…)
    sync                  влить origin/dev в текущую ветку, показать конфликты

  Сборка и тесты (Gradle — только так: зеркало Maven и автоповтор при 429)
    check                 перед коммитом: lint + все тесты + debug APK, краткий отчёт
    quick                 только unit-тесты всех модулей (для итераций)
    test <модуль> [фильтр] тесты одного модуля, напр.: test app '*UpdateControllerTest'
                          (модули: model, domain, data, app)
    gradle <задачи…>      любой вызов Gradle вместо ./gradlew
    profile [задачи]      где тратится время сборки

  Дизайн (холст)
    mockups               пересобрать доски холста из генераторов
    render <доски…>       PNG досок без холста (тёмная, Вольт) в scratchpad
    flows                 перерисовать доску «Воркфлоу»

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
    tasks) exec "$here/tasks.sh" "$@" ;;
    sync) exec "$here/sync-dev.sh" "$@" ;;
    check) exec "$here/check.sh" "$@" ;;
    quick) exec "$here/check.sh" --quick "$@" ;;
    test)
        module="${1:?модуль: model, domain, data, app}"; shift
        case "$module" in
            model|domain|data) task=":core:$module:jvmTest" ;;
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
    mockups) exec python3 "$here/gen-mockups.py" "$@" ;;
    render)
        out="$(scratch)/shots"; mkdir -p "$out"
        files=(); for b in "$@"; do files+=("docs/design/mockups/${b%.dc.html}.dc.html"); done
        exec node "$here/render-mockups.js" "$out" dark Вольт "${files[@]}" ;;
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
