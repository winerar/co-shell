#!/usr/bin/env bash
# The PR of this branch, ready to hand over: title, description and the exact next GitHub MCP calls (workflow.md «PR»).
#   scripts/agent/pr.sh        (agent.sh pr; `finish` points here)
# Prints problems first (✗ not pushed, behind dev in code → `agent.sh update`, no Agent-Check → CI spends ~6 min),
# then the PR number (gh api search — the one gh call allowed in cloud sessions), title and body to pass as they are.
set -uo pipefail
cd "$(dirname "$0")/../.."
REPO=${REPO:-winerar/co-shell}
branch=$(git branch --show-current)
git fetch -q origin dev "$branch" 2>/dev/null

# ── Problems before the PR goes to CI
[ -z "$(git status --porcelain)" ] || echo "✗ not committed: $(git status --porcelain | wc -l) files — agent.sh ship/finish"
remote=$(git rev-parse -q --verify "origin/$branch" || true)
[ "$remote" = "$(git rev-parse HEAD)" ] || echo "✗ HEAD is not pushed to origin/$branch — agent.sh push"
behind=$(git diff --name-only HEAD...origin/dev | grep -vE '^docs/|\.md$|^scripts/agent/' | head -3)
[ -z "$behind" ] || echo "⚠ dev has new code ($(echo $behind)…): CI builds the merge (~6 min). Cheaper: agent.sh update, then agent.sh ship"
if [ -z "$(git diff --name-only origin/dev...HEAD | grep -vE '^docs/|\.md$|^scripts/agent/')" ]; then
    echo "✓ only docs/scripts/agent changed — PR CI skips Gradle (~1 min)"
elif git log -1 --format=%B | git interpret-trailers --parse | grep -qx 'Agent-Check: full'; then
    echo "✓ Agent-Check: full — PR CI skips Gradle (~1 min)"
else
    echo "⚠ HEAD has no «Agent-Check: full» — PR CI runs the full check (~6 min). Cheaper: agent.sh ship --recheck -m …"
fi

# ── PR number
num=$(timeout 30 gh api -X GET search/issues -f q="repo:$REPO is:pr is:open head:$branch" -q '.items[0].number // empty' 2>/dev/null)

# ── Title and body
skip='^(Stamp STATUS|Docs: STATUS|Trim STATUS|Update STATUS|Merge )'
title=$(git log --no-merges --reverse --format=%s origin/dev..HEAD | grep -vE "$skip" | head -1)
fixes=$(git log --format=%B origin/dev..HEAD | grep -oiE '^(fixes|closes|resolves) #[0-9]+' | sed -E 's/^[a-zA-Z]+/Fixes/' | sort -u)
refs=$(git log --format=%s origin/dev..HEAD | grep -oE '#[0-9]+' | sort -u | tr '\n' ' ')
echo
echo "== PR: ${num:+#$num}${num:-не найден → MCP create_pull_request (base dev, draft: true), затем шаг 1 (CI не запускается на opened)}"
echo "title: ${title:-<одной строкой: что изменилось>}"
echo "body:"
echo "## Что"
git log --no-merges --reverse --format='- %s' origin/dev..HEAD | grep -vE "^- ($skip)" | head -20
[ -z "$refs" ] || echo "Связано: $refs"
[ -z "$fixes" ] || echo "$fixes"
echo "<строки атрибуции из system-reminder>"
cat <<TXT

== Дальше (сам, без владельца)
1. MCP update_pull_request: pullNumber ${num:-N}, title, body (выше; «Fixes #N» закрывает задачу при мерже), draft: false — это запускает CI.
2. MCP subscribe_pr_activity (${num:-N}) и закончить ход: события придут сами.
   - CI красный → pull_request_read get_comments, комментарий <!-- ci-summary --> → починить → agent.sh ship.
   - Бот пишет о конфликте с dev → agent.sh update.  - Влит → unsubscribe_pr_activity; ветку не трогать.
   Зелёный CI → automerge сам вливает в dev (метка hold — стоп). Новые push после «ready» — новый прогон CI: сначала доделать всё.
TXT
