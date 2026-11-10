#!/usr/bin/env bash
# Bring origin/dev into the current branch (a PR conflicts or dev moved on). Short output:
# merged cleanly, or the conflicted files with hunk counts and how to resolve the usual ones.
# notes.md and whats-new.md merge by `union` (.gitattributes) and normally do not conflict.
set -uo pipefail
cd "$(dirname "$0")/../.."

[ -n "$(git status --porcelain --untracked-files=no)" ] && { echo "✗ commit or stash changes first"; exit 1; }
git fetch -q origin dev
behind=$(git rev-list --count HEAD..origin/dev)
[ "$behind" -eq 0 ] && { echo "OK: already up to date with origin/dev"; exit 0; }

if git merge -q --no-edit origin/dev >/dev/null 2>&1; then
    echo "OK: merged $behind commits from origin/dev — run check.sh --quick, then push"
    exit 0
fi

echo "✗ conflicts after merging $behind commits from origin/dev:"
for f in $(git diff --name-only --diff-filter=U); do
    echo "  $f ($(grep -c '^<<<<<<< ' "$f" 2>/dev/null || echo ?) hunks)"
    case "$f" in
        docs/STATUS.md) echo "    keep your «Последняя сессия», merge «Следующие шаги» from both, newest «Обновлено»" ;;
        core/data/schemas/*) echo "    regenerate: ./gradlew :core:data:jvmTest (do not hand-merge JSON)" ;;
    esac
done
echo "Resolve, then: git grep -n '^<<<<<<< ' (must be empty), git add, git commit --no-edit"
exit 1
