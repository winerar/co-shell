#!/usr/bin/env bash
# Pre-commit check with short output (saves AI-agent tokens): runs `./gradlew check assembleDebug`
# quietly and prints only errors, failed tests and a one-line summary. Extra args go to Gradle.
# `scripts/agent/check.sh --quick` — unit tests only (no lint, no APK): for iterations; full run before commit.
set -uo pipefail
cd "$(dirname "$0")/../.."

tasks=(check assembleDebug)
if [ "${1:-}" = "--quick" ]; then
    shift
    tasks=(:core:model:jvmTest :core:domain:jvmTest :core:data:jvmTest :app:testDebugUnitTest)
fi

log=$(mktemp)
./gradlew "${tasks[@]}" -q --console=plain "$@" >"$log" 2>&1
status=$?
scripts/agent/build-summary.sh "$log"
rm -f "$log"
[ $status -eq 0 ] && echo "OK: ${tasks[*]} passed" || echo "FAILED (exit $status)"
exit $status
