#!/usr/bin/env bash
# Session start helper (docs/agents/workflow.md «Начало сессии»): fetches and lists remote branches
# with commits that are not in origin/dev, newest first, with their commits and the STATUS line.
# Branches with nothing new (all merged into dev, or only merge commits like main) are only counted.
set -uo pipefail
cd "$(dirname "$0")/.."

git fetch --all --prune -q 2>/dev/null
merged=0
for ref in $(git for-each-ref --sort=-committerdate --format='%(refname:short)' refs/remotes/origin); do
    case "$ref" in origin/HEAD | origin | origin/dev) continue ;; esac
    count=$(git rev-list --no-merges --count "origin/dev..$ref") # main holds only merge commits
    if [ "$count" -eq 0 ]; then merged=$((merged + 1)); continue; fi
    echo "== $ref (+$count, $(git log -1 --format=%cs "$ref"))"
    git log --no-merges --format='   %h %s' "origin/dev..$ref" | head -5
    # The "Обновлено" line of that branch's STATUS says what it is about.
    git show "$ref:docs/STATUS.md" 2>/dev/null | grep -m1 '^\*\*Обновлено' | cut -c1-200 | sed 's/^/   STATUS: /'
done
echo "Merged into dev (nothing new): $merged branches"
echo "Current: $(git rev-parse --abbrev-ref HEAD) — $(git rev-list --count origin/dev..HEAD) ahead, $(git rev-list --count HEAD..origin/dev) behind origin/dev"
