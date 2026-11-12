#!/usr/bin/env bash
# One issue in the terminal: title, state, labels, body (trimmed) — cheaper than reading it through MCP.
# Uses `gh api search/issues` (the only gh call allowed in cloud sessions). Comments: GitHub MCP issue_read.
#   issue.sh 219 [строк]     body limited to 40 lines by default
set -uo pipefail
n=${1:?номер задачи}; lines=${2:-40}
REPO=${REPO:-winerar/co-shell}
json=""
for kind in issue pull-request; do # the bare number matches the issue number (and text mentioning it)
    json=$(timeout 30 gh api -X GET search/issues -f q="repo:$REPO is:$kind $n" -f per_page=100 \
        -q ".items[] | select(.number == $n)" 2>/dev/null) && [ -n "$json" ] && break
done
[ -n "$json" ] || { echo "(#$n not found via search — use GitHub MCP issue_read)"; exit 1; }
jq -r '"#\(.number) \(.title)\n\(.state) · \([.labels[].name] | join(", ")) · комментариев: \(.comments) · \(.html_url)\n"' <<<"$json"
jq -r '.body // ""' <<<"$json" | tr -d '\r' | head -n "$lines"
total=$(jq -r '.body // ""' <<<"$json" | wc -l)
[ "$total" -gt "$lines" ] && echo "… ещё $((total - lines)) строк (issue.sh $n $total)"
exit 0
