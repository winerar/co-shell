#!/usr/bin/env bash
# Total unit tests from the last full run (XML in */build/test-results). Prints the number only when every
# test module has results — a partial run (agent.sh test/affected) would give a wrong total; then prints
# nothing and the reason to stderr. Used by stamp-status.sh and session-end.sh.
set -uo pipefail
cd "$(dirname "$0")/../.."
python3 - <<'PY'
import glob, sys, xml.etree.ElementTree as ET
modules = ["core/model", "core/domain", "core/data", "core/designsystem", "app"]
total, missing = 0, []
for m in modules:
    files = glob.glob(f"{m}/build/test-results/**/TEST-*.xml", recursive=True)
    if not files:
        missing.append(m)
    for p in files:
        try:
            total += int(ET.parse(p).getroot().get("tests", 0))
        except ET.ParseError:
            pass
if missing:
    sys.stderr.write(f"no test results for {', '.join(missing)} — run agent.sh check (or quick)\n")
    sys.exit(1)
print(total)
PY
