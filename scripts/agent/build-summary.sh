#!/usr/bin/env bash
# Short build report from a Gradle log + test XML + lint SARIF (used by check.sh and by CI on failure).
#   scripts/agent/build-summary.sh <gradle.log>        — log minus noise (for `-q` logs, check.sh)
#   scripts/agent/build-summary.sh --ci <gradle.log>   — only compiler errors and «What went wrong» blocks
# Then: `Tests: N`, failed tests with the first line of the failure, lint errors. Markdown-friendly.
set -uo pipefail
cd "$(dirname "$0")/../.."

mode=local
[ "${1:-}" = "--ci" ] && { mode=ci; shift; }
log=${1:-}

if [ -n "$log" ] && [ -f "$log" ]; then
    if [ $mode = ci ]; then
        awk '/^e: /{print; next} /^\* What went wrong:/{on=1} /^\* Try:/{on=0} on' "$log" | head -60
    else
        # Drop proxy/JVM noise, Kotlin warnings and report paths; keep everything else (errors).
        grep -vE '^(Picked up JAVA_TOOL_OPTIONS|w: |Wrote (HTML|SARIF|XML) report|$)' "$log" | head -60
    fi
fi

python3 - <<'PY'
import glob, json, xml.etree.ElementTree as ET

total, failures = 0, []
for path in glob.glob("**/build/test-results/**/TEST-*.xml", recursive=True):
    try:
        suite = ET.parse(path).getroot()
    except ET.ParseError:
        continue
    total += int(suite.get("tests", 0))
    for case in suite.iter("testcase"):
        for bad in list(case.findall("failure")) + list(case.findall("error")):
            first = (bad.get("message") or bad.text or "").strip().splitlines()
            failures.append(f"{case.get('classname')}.{case.get('name')}: {(first or [''])[0][:200]}")
if total:
    print(f"Tests: {total}")
if failures:
    print(f"Failed tests ({len(failures)}):")
    for f in failures[:20]:
        print(f"- {f}")

for path in glob.glob("**/build/reports/lint-results-debug.sarif", recursive=True):
    for r in json.load(open(path))["runs"][0]["results"]:
        if r.get("level") == "error":
            loc = r["locations"][0]["physicalLocation"]["artifactLocation"]["uri"]
            print(f"Lint error {r['ruleId']}: {loc}: {r['message']['text'][:120]}")
PY
