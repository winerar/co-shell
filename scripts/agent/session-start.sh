#!/usr/bin/env bash
# Session start in one command (docs/agents/workflow.md «Начало сессии»):
# 1) fetch; 2) remote branches with commits not in origin/dev (commits + their STATUS line);
# 3) move the current branch onto origin/dev if it has no own work yet (cloud sessions clone main);
# 4) print the STATUS date line and «Следующие шаги». Then read docs/agents/notes.md yourself.
set -uo pipefail
cd "$(dirname "$0")/../.."

git fetch --all --prune -q 2>/dev/null
echo "## Branches with work not in dev"
merged=0
for ref in $(git for-each-ref --sort=-committerdate --format='%(refname:short)' refs/remotes/origin); do
    case "$ref" in origin/HEAD | origin | origin/dev) continue ;; esac
    count=$(git rev-list --no-merges --count "origin/dev..$ref") # main holds only merge commits
    if [ "$count" -eq 0 ]; then merged=$((merged + 1)); continue; fi
    echo "== $ref (+$count, $(git log -1 --format=%cs "$ref"))"
    git log --no-merges --format='   %h %s' "origin/dev..$ref" | head -5
    git show "$ref:docs/STATUS.md" 2>/dev/null | grep -m1 '^\*\*Обновлено' | cut -c1-200 | sed 's/^/   STATUS: /'
done
echo "Nothing new (merged into dev): $merged branches"

branch=$(git rev-parse --abbrev-ref HEAD)
own=$(git rev-list --no-merges --count origin/dev..HEAD)
if [ "$own" -eq 0 ] && [ -z "$(git status --porcelain)" ]; then
    git checkout -q -B "$branch" origin/dev && echo "## Branch $branch reset onto origin/dev (no own commits)"
else
    echo "## Branch $branch: $own own commits, $(git rev-list --count HEAD..origin/dev) behind origin/dev — not moved"
fi

echo "## STATUS"
sed -n '/^\*\*Обновлено/p' docs/STATUS.md | cut -c1-400
sed -n '/^## Следующие шаги/,/^## /p' docs/STATUS.md | head -20
