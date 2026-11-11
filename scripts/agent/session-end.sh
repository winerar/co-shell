#!/usr/bin/env bash
# End-of-session checklist as a script (docs/agents/workflow.md «Конец сессии»). Prints only problems
# (✗) and reminders; run before the final PR. Exit code 1 if something must be fixed.
set -uo pipefail
cd "$(dirname "$0")/../.."
fail=0
bad() { echo "✗ $*"; fail=1; }
warn() { echo "! $*"; }

[ -n "$(git status --porcelain)" ] && bad "uncommitted changes: $(git status --porcelain | wc -l) files"
git fetch -q origin 2>/dev/null
branch=$(git rev-parse --abbrev-ref HEAD)
if git rev-parse -q --verify "origin/$branch" >/dev/null; then
    ahead=$(git rev-list --count "origin/$branch..HEAD")
    [ "$ahead" -gt 0 ] && bad "$ahead commits not pushed to origin/$branch"
else
    bad "branch $branch is not pushed"
fi
behind=$(git rev-list --count HEAD..origin/dev)
[ "$behind" -gt 0 ] && warn "origin/dev is $behind commits ahead — merge it before the PR"

today=$(date +%F)
grep -q "^\*\*Обновлено:\*\* $today" docs/STATUS.md || bad "docs/STATUS.md «Обновлено» is not $today"
git diff --quiet origin/dev -- docs/STATUS.md && bad "docs/STATUS.md not changed in this branch"
# STATUS is read at every session start: keep it short (workflow.md «Сессии ИИ-агента»).
long=$(python3 -c 'import sys; print(*[i for i, l in enumerate(open(sys.argv[1], encoding="utf-8"), 1) if len(l) > 501], end=" ")' docs/STATUS.md)
[ -n "${long// /}" ] && bad "docs/STATUS.md lines over 500 chars: $long— shorten (history goes to ARCHITECTURE/ROADMAP)"
[ "$(wc -l <docs/STATUS.md)" -gt 150 ] && warn "docs/STATUS.md is $(wc -l <docs/STATUS.md) lines (aim ≤150) — drop finished items"
git diff --quiet origin/dev -- docs/agents/notes.md && warn "docs/agents/notes.md not changed — anything learned this session?"

# whats-new: the Release button stops if dev has no new top section (workflow.md «Конец сессии» п.3).
top() { git show "$1:docs/whats-new.md" 2>/dev/null | grep -m1 '^## '; }
if [ "$(top HEAD)" = "$(top origin/main)" ] && ! git diff --quiet origin/main -- app core; then
    warn "docs/whats-new.md top section is already released — add a new dated section if the family will notice this change"
fi

# Test count in STATUS vs the last test run (scripts/agent/check.sh leaves the XML results).
results=$(find . -path '*/build/test-results/*' -name 'TEST-*.xml' -not -path './.gradle/*' 2>/dev/null)
if [ -n "$results" ]; then
    total=$(cat $results | grep -o '<testsuite [^>]*tests="[0-9]*"' | grep -o 'tests="[0-9]*"' | grep -o '[0-9]*' | paste -sd+ | bc)
    grep -q "всего $total " docs/STATUS.md || bad "STATUS test count differs from the last run ($total)"
fi

# Task statuses (tasks.md «Конец сессии»): every in-progress issue ends as `Fixes #N` in the PR
# (automerge closes it) or goes back to status:next / needs-owner with a comment.
wip=$(timeout 20 gh api -X GET search/issues -f q="repo:winerar/co-shell is:issue is:open label:status:in-progress" \
    -q '.items[] | "#\(.number) \(.title[0:60])"' 2>/dev/null)
[ -n "$wip" ] && while read -r line; do
    warn "in progress: $line — «Fixes» in the PR, or status:next/needs-owner + comment"
done <<<"$wip"

# Size rules (AGENTS.md «Как поддерживать», project-structure.md «Файлы»).
[ "$(wc -l <AGENTS.md)" -gt 42 ] && bad "AGENTS.md is $(wc -l <AGENTS.md) lines (limit ~40)"
for f in docs/agents/*.md; do
    n=$(wc -l <"$f"); [ "$n" -gt 125 ] && bad "$f is $n lines (limit ~120) — split it"
done
find app core -name '*.kt' -path '*/src/*' -not -path '*/build/*' -exec wc -l {} + | grep -v ' total$' |
    awk '$1 > 220 {print "! " $2 " is " $1 " lines (aim ~200)"}'

[ $fail -eq 0 ] && echo "OK: session-end checks passed (now: mark the draft PR into dev ready — automerge — and report to the owner)"
exit $fail
