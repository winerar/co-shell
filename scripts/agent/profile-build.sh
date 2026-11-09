#!/usr/bin/env bash
# Where does build time go? Runs Gradle with --profile (default: check assembleDebug, all tasks rerun)
# and prints wall time, the 20 slowest tasks and lint/test totals. Takes ~3-5 min. Extra args = tasks.
set -uo pipefail
cd "$(dirname "$0")/../.."
tasks=("$@"); [ $# -eq 0 ] && tasks=(check assembleDebug)
./gradlew "${tasks[@]}" --profile --rerun-tasks -q --console=plain >/dev/null 2>&1 || echo "Gradle failed"
report=$(ls -t build/reports/profile/*.html | head -1)
python3 - "$report" <<'PY'
import re, sys
s = open(sys.argv[1]).read()
def sec(t): return sum(float(v) * (60 if u == "m" else 1) for v, u in re.findall(r"([\d.]+)(m|s)(?![a-z])", t))
wall = re.search(r"Total Build Time</td>\s*<td[^>]*>([^<]+)", s)
print("Wall:", wall.group(1) if wall else "?")
tasks = {n: sec(t) for n, t in re.findall(r'<td class="indentPath">(:[^<]+)</td>\s*<td class="numeric">([^<]+)</td>', s)}
for n, v in sorted(tasks.items(), key=lambda x: -x[1])[:20]: print(f"{v:6.1f}s {n}")
print(f"lint {sum(v for n, v in tasks.items() if 'lint' in n.lower()):.0f}s, "
      f"test run {sum(v for n, v in tasks.items() if n.endswith('Test') and 'compile' not in n):.0f}s, "
      f"all tasks {sum(tasks.values()):.0f}s (CPU, parallel)")
PY
