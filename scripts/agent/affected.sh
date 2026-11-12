#!/usr/bin/env bash
# Unit tests only for modules touched since origin/dev (committed + working tree) and modules that depend
# on them: model → domain → data → app; designsystem → app. Docs/scripts only — nothing to run.
#   affected.sh            run the tests (quiet, build-summary report)
#   affected.sh --list     only print the Gradle tasks
set -uo pipefail
cd "$(dirname "$0")/../.."
here=scripts/agent
files=$( { git diff --name-only origin/dev...HEAD; git diff --name-only HEAD; git ls-files -o --exclude-standard; } 2>/dev/null | sort -u)

declare -A on
for f in $files; do
    case "$f" in
        core/model/*) on[model]=1; on[domain]=1; on[data]=1; on[app]=1 ;;
        core/domain/*) on[domain]=1; on[data]=1; on[app]=1 ;;
        core/data/*) on[data]=1; on[app]=1 ;;
        core/designsystem/*) on[designsystem]=1; on[app]=1 ;;
        app/*) on[app]=1 ;;
        gradle/* | *.gradle.kts | gradle.properties) on[model]=1; on[domain]=1; on[data]=1; on[designsystem]=1; on[app]=1 ;;
    esac
done
tasks=()
[ -n "${on[model]:-}" ] && tasks+=(:core:model:jvmTest)
[ -n "${on[domain]:-}" ] && tasks+=(:core:domain:jvmTest)
[ -n "${on[data]:-}" ] && tasks+=(:core:data:jvmTest)
[ -n "${on[designsystem]:-}" ] && tasks+=(:core:designsystem:testDebugUnitTest)
[ -n "${on[app]:-}" ] && tasks+=(:app:testDebugUnitTest)

if [ ${#tasks[@]} -eq 0 ]; then echo "OK: no Kotlin/Gradle changes since origin/dev — no tests to run"; exit 0; fi
[ "${1:-}" = "--list" ] && { echo "${tasks[*]}"; exit 0; }
log=$(mktemp)
$here/gradle.sh "${tasks[@]}" -q --console=plain >"$log" 2>&1
status=$?
$here/build-summary.sh "$log"; rm -f "$log"
[ $status -eq 0 ] && echo "OK: ${tasks[*]}" || echo "FAILED (exit $status)"
exit $status
