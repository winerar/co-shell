#!/usr/bin/env bash
# Session start in one command (docs/agents/workflow.md «Начало сессии»):
# 1) fetch; 2) recent remote branches with commits not in origin/dev and main (by patch: squash-merged drop out);
# 3) move the current branch onto origin/dev if it has no own work yet (cloud sessions clone main);
# 4) STATUS date line and «В работе»; 5) task board without the long backlog. Then read notes.md yourself.
#   session-start.sh            branches touched in the last 3 days (older ones only counted)
#   session-start.sh --all      every unmerged branch
set -uo pipefail
cd "$(dirname "$0")/../.."
days=3; [ "${1:-}" = "--all" ] && days=100000

git fetch --all --prune -q 2>/dev/null
echo "## Branches with work not in dev (last $days days; --all for older)"
since=$(date -d "-$days days" +%s 2>/dev/null || echo 0)
merged=0; old=0
for ref in $(git for-each-ref --sort=-committerdate --format='%(refname:short)' refs/remotes/origin); do
    case "$ref" in origin/HEAD | origin | origin/dev | origin/main) continue ;; esac # main: releases
    # own = not in dev by patch (squash-merges drop out) and not already released in main
    count=0
    for h in $(git cherry origin/dev "$ref" 2>/dev/null | sed -n 's/^+ //p'); do
        git merge-base --is-ancestor "$h" origin/main || count=$((count + 1))
    done
    if [ "$count" -eq 0 ]; then merged=$((merged + 1)); continue; fi
    if [ "$(git log -1 --format=%ct "$ref")" -lt "$since" ]; then old=$((old + 1)); continue; fi
    echo "== $ref (+$count, $(git log -1 --format=%cs "$ref"))"
    git log --no-merges --format="   %h %s" "$ref" ^origin/dev ^origin/main | head -3
    git show "$ref:docs/STATUS.md" 2>/dev/null | grep -m1 '^\*\*Обновлено' | cut -c1-160 | sed 's/^/   STATUS: /'
done
echo "Merged into dev: $merged · older unmerged (stale): $old"

branch=$(git rev-parse --abbrev-ref HEAD)
own=$(git rev-list --no-merges --count origin/dev..HEAD)
if [ "$own" -eq 0 ] && [ -z "$(git status --porcelain)" ]; then
    git checkout -q -B "$branch" origin/dev && echo "## Branch $branch reset onto origin/dev (no own commits)"
else
    echo "## Branch $branch: $own own commits, $(git rev-list --count HEAD..origin/dev) behind origin/dev — not moved"
fi

echo "## STATUS"
sed -n '/^\*\*Обновлено/p' docs/STATUS.md | cut -c1-400
sed -n '/^## В работе/,/^## /p' docs/STATUS.md | sed '1,4d;$d' | head -12
echo "## Tasks (GitHub issues by status; backlog: agent.sh tasks backlog)"
"$(dirname "$0")/tasks.sh" in-progress next needs-owner triage
echo "## Next: read docs/agents/owner.md and notes.md; take an issue → status:in-progress (GitHub MCP)"
