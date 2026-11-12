#!/usr/bin/env bash
# Push the current branch with retries on network errors (2, 4, 8, 16 s) — workflow.md «Git».
# Prints one line: what was pushed, or the git error after the last try. Extra args go to git push.
set -uo pipefail
cd "$(dirname "$0")/../.."
branch=$(git rev-parse --abbrev-ref HEAD)
case "$branch" in main | dev) echo "✗ refusing to push $branch directly (PR into dev)"; exit 1 ;; esac
for wait in 0 2 4 8 16; do
    [ "$wait" -gt 0 ] && { echo "… push failed, retry in ${wait}s"; sleep "$wait"; }
    if out=$(git push -q -u origin "$branch" "$@" 2>&1); then
        echo "OK: pushed $branch ($(git rev-parse --short HEAD))"; exit 0
    fi
    grep -qiE 'rejected|non-fast-forward|denied|403|protected' <<<"$out" && break # not a network error
done
echo "✗ push failed:"; echo "$out" | grep -v '^remote: *$' | tail -8
grep -qi 'non-fast-forward\|fetch first' <<<"$out" && echo "  (branch moved: git pull --no-rebase origin $branch, then push)"
exit 1
