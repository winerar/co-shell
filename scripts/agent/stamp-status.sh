#!/usr/bin/env bash
# Bring docs/STATUS.md facts up to date before the session ends (session-end.sh checks both):
# «**Обновлено:** <today>» and «всего N» tests from the last full test run (test-count.sh).
# Prints what changed; the prose (stage, «Последняя сессия») is still written by hand.
set -uo pipefail
cd "$(dirname "$0")/../.."
f=docs/STATUS.md
today=$(date +%F)
sed -i -E "s/^\*\*Обновлено:\*\* [0-9]{4}-[0-9]{2}-[0-9]{2}/**Обновлено:** $today/" "$f"
total=$("$(dirname "$0")/test-count.sh")
if [ -n "$total" ]; then sed -i -E "s/всего [0-9]+ /всего $total /" "$f"; else echo "! test count not updated"; fi
if git diff --quiet -- "$f"; then echo "OK: STATUS already has $today${total:+ and $total tests}"; else
    git diff -U0 -- "$f" | grep -E '^[-+]' | grep -vE '^(---|\+\+\+) ' | grep -E 'Обновлено|всего [0-9]' | cut -c1-140; fi
