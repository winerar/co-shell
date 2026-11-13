#!/usr/bin/env bash
# Session start in one command (docs/agents/workflow.md «Начало сессии»):
# 1) fetch; 2) recent remote branches with commits not in origin/dev and main (by patch: squash-merged drop out);
# 3) move the current branch onto origin/dev if it has no own work yet (cloud sessions clone main);
# 4) STATUS date line and «В работе»; 5) task board without the long backlog. Then read owner.md and notes.md.
#   session-start.sh            branches touched in the last 3 days (older ones only counted, not checked)
#   session-start.sh --all      every unmerged branch
# Speed (~3 s, was ~27 s): the board is fetched in the background; old branches are skipped by date before
# any patch comparison; «not in main» is one rev-list per branch instead of a merge-base per commit.
set -uo pipefail
here="$(cd "$(dirname "$0")" && pwd)"
cd "$here/../.."
days=3; [ "${1:-}" = "--all" ] && days=100000
# Cut to N characters, not bytes (`cut -c` breaks Cyrillic in half).
clip() { LC_ALL=C.UTF-8 sed -E "s/^(.{$1}).+/\1…/"; }

git fetch --all --prune -q 2>/dev/null
board=$(mktemp); "$here/tasks.sh" in-progress next needs-owner triage >"$board" 2>&1 & board_pid=$!

echo "## Branches with work not in dev (last $days days; --all for older)"
since=$(date -d "-$days days" +%s 2>/dev/null || echo 0)
merged=0; old=0
while read -r ref ts; do
    case "$ref" in origin/HEAD | origin | origin/dev | origin/main) continue ;; esac # main: releases
    if [ "$ts" -lt "$since" ]; then old=$((old + 1)); continue; fi
    # own = not in dev by patch (squash-merges drop out) and not already released in main
    count=$(comm -12 <(git cherry origin/dev "$ref" 2>/dev/null | sed -n 's/^+ //p' | sort) \
        <(git rev-list "$ref" ^origin/main 2>/dev/null | sort) | wc -l)
    if [ "$count" -eq 0 ]; then merged=$((merged + 1)); continue; fi
    echo "== $ref (+$count, $(git log -1 --format=%cs "$ref"))"
    git log --no-merges --format="   %h %s" "$ref" ^origin/dev ^origin/main | head -3
    git show "$ref:docs/STATUS.md" 2>/dev/null | grep -m1 '^\*\*Обновлено' | clip 150 | sed 's/^/   STATUS: /'
done < <(git for-each-ref --sort=-committerdate --format='%(refname:short) %(committerdate:unix)' refs/remotes/origin)
echo "Recent, already in dev: $merged · older than $days days (not checked; --all): $old"

branch=$(git rev-parse --abbrev-ref HEAD)
own=$(git rev-list --no-merges --count origin/dev..HEAD)
if [ "$own" -eq 0 ] && [ -z "$(git status --porcelain)" ]; then
    git checkout -q -B "$branch" origin/dev && echo "## Branch $branch reset onto origin/dev (no own commits)"
else
    echo "## Branch $branch: $own own commits, $(git rev-list --count HEAD..origin/dev) behind origin/dev — not moved"
fi

# Long STATUS lines are cut: the full text is `agent.sh doc docs/STATUS.md "В работе"` when needed.
echo "## STATUS (lines cut at 200 chars)"
sed -n '/^\*\*Обновлено/p' docs/STATUS.md | clip 300
sed -n '/^## В работе/,/^## /p' docs/STATUS.md | sed '1,4d;$d' | head -12 | clip 200
echo "## Tasks (GitHub issues by status; backlog: agent.sh tasks backlog)"
wait "$board_pid"; cat "$board"; rm -f "$board"
echo "## Next: read docs/agents/owner.md and notes.md (in parallel); take an issue → status:in-progress (GitHub MCP)"
