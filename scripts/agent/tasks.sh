#!/usr/bin/env bash
# Task board in the terminal: open issues grouped by `status:*` label (docs/agents/tasks.md).
# Usage: scripts/agent/tasks.sh [status...]   e.g. `tasks.sh next in-progress`; no args — all columns.
# Uses `gh api search/issues` (the only gh call allowed in cloud sessions; GitHub MCP for the rest).
set -uo pipefail
REPO=${REPO:-winerar/co-shell}
statuses=("$@")
[ ${#statuses[@]} -gt 0 ] || statuses=(in-progress next needs-owner triage backlog)

json=$(timeout 30 gh api -X GET search/issues --paginate \
    -f q="repo:$REPO is:issue is:open" -f per_page=100 \
    -q '.items[] | {n: .number, t: .title, l: [.labels[].name]}' 2>/dev/null) || {
    echo "(tasks: GitHub search unavailable — use GitHub MCP list_issues)"; exit 0; }

row='"  #\(.n) \(.t[0:70])" + (([.l[] | select(startswith("status:") | not)] | join(",")) as $o | if $o == "" then "" else "  [\($o)]" end)'
for s in "${statuses[@]}"; do
    items=$(jq -r --arg s "status:$s" "select(.l | index(\$s)) | $row" <<<"$json")
    [ -n "$items" ] || continue
    echo "== $s ($(wc -l <<<"$items"))"
    if [ "$s" = backlog ] && [ $# -eq 0 ]; then head -8 <<<"$items"; else echo "$items"; fi
done
none=$(jq -r "select([.l[] | startswith(\"status:\")] | any | not) | $row" <<<"$json")
[ -z "$none" ] || { echo "== без статуса — разметить"; echo "$none"; }
