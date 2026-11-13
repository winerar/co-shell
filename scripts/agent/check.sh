#!/usr/bin/env bash
# Pre-commit check with short output (saves AI-agent tokens): runs `check assembleDebug` through gradle.sh
# (Maven mirror + retry on 429, #156) quietly; prints only errors, failed tests and a summary. Extra args go to Gradle.
# `scripts/agent/check.sh --quick` — unit tests only (no lint, no APK): for iterations; full run before commit.
# A pass without extra Gradle args is recorded (check-cache.sh): ship/finish then skip a repeat on docs-only changes.
set -uo pipefail
cd "$(dirname "$0")/../.."

tasks=(check assembleDebug); mode=full
if [ "${1:-}" = "--quick" ]; then
    shift; mode=quick
    tasks=(:core:model:jvmTest :core:domain:jvmTest :core:data:jvmTest :core:designsystem:testDebugUnitTest :app:testDebugUnitTest)
fi

log=$(mktemp)
scripts/agent/gradle.sh "${tasks[@]}" -q --console=plain "$@" >"$log" 2>&1
status=$?
scripts/agent/build-summary.sh "$log"
rm -f "$log"
[ $status -eq 0 ] && [ $# -eq 0 ] && scripts/agent/check-cache.sh record "$mode"
[ $status -eq 0 ] && echo "OK: ${tasks[*]} passed" || echo "FAILED (exit $status)"
exit $status
