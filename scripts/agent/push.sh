#!/usr/bin/env bash
# Push the current branch with retries on network errors (2, 4, 8, 16 s) — workflow.md «Git».
# Prints one line: what was pushed, or the git error after the last try. Extra args go to git push.
set -uo pipefail
cd "${PUSH_REPO:-$(dirname "$0")/../..}"
branch=$(git rev-parse --abbrev-ref HEAD)
# The target is the branch the local one already tracks on origin (a PR branch under another local name, #460);
# without an upstream — the local name.
target=$branch
up=$(git rev-parse --abbrev-ref --symbolic-full-name '@{u}' 2>/dev/null || true)
case "$up" in origin/*) target=${up#origin/} ;; esac
for name in "$branch" "$target"; do
    case "$name" in main | dev) echo "✗ refusing to push $name directly (PR into dev)"; exit 1 ;; esac
done
for wait in 0 2 4 8 16; do
    [ "$wait" -gt 0 ] && { echo "… push failed, retry in ${wait}s"; sleep "$wait"; }
    if out=$(git push -q -u origin "$branch:$target" "$@" 2>&1); then
        echo "OK: pushed $target ($(git rev-parse --short HEAD))"; exit 0
    fi
    grep -qiE 'rejected|non-fast-forward|denied|403|protected' <<<"$out" && break # not a network error
done
echo "✗ push failed:"; echo "$out" | grep -v '^remote: *$' | tail -8
grep -qi 'non-fast-forward\|fetch first' <<<"$out" && echo "  (branch moved: git pull --no-rebase origin $target, then push)"
exit 1
