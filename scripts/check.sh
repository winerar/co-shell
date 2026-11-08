#!/usr/bin/env bash
# Pre-commit check with short output (saves AI-agent tokens): runs `./gradlew check assembleDebug`
# quietly and prints only errors, failed tests and a one-line summary. Extra args go to Gradle.
set -uo pipefail
cd "$(dirname "$0")/.."

log=$(mktemp)
./gradlew check assembleDebug -q --console=plain "$@" >"$log" 2>&1
status=$?

# Drop proxy/JVM noise, Kotlin warnings and report paths; keep everything else (errors).
grep -vE '^(Picked up JAVA_TOOL_OPTIONS|w: |Wrote (HTML|SARIF|XML) report|$)' "$log" | head -60
rm -f "$log"

results=$(find . -path '*/build/test-results/*' -name 'TEST-*.xml' -not -path './.gradle/*' 2>/dev/null)
if [ -n "$results" ]; then
    total=$(cat $results | grep -o '<testsuite [^>]*tests="[0-9]*"' | grep -o 'tests="[0-9]*"' | grep -o '[0-9]*' | paste -sd+ | bc)
    failed=$(grep -l '<failure' $results | sed 's|.*/TEST-||; s|\.xml$||')
    echo "Tests: ${total:-0}"
    [ -n "$failed" ] && echo "Failed test classes:" && echo "$failed"
fi
for sarif in $(find . -path '*/build/reports/lint-results-debug.sarif' 2>/dev/null); do
    python3 - "$sarif" <<'PY'
import json, sys
results = json.load(open(sys.argv[1]))["runs"][0]["results"]
for r in results:
    if r.get("level") == "error":
        loc = r["locations"][0]["physicalLocation"]["artifactLocation"]["uri"]
        print(f"Lint error {r['ruleId']}: {loc}: {r['message']['text'][:120]}")
PY
done
[ $status -eq 0 ] && echo "OK: check assembleDebug passed" || echo "FAILED (exit $status)"
exit $status
