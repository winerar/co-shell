#!/usr/bin/env bash
# Remember which working tree passed check.sh, so flows (ship/finish) skip a repeat 3–6 min check when only
# docs changed since then — the usual `finish` after `ship` (STATUS, notes). Stored in build/agent-check/.
#   check-cache.sh record <full|quick>   after a passing check (check.sh calls it)
#   check-cache.sh fresh <full|quick>    exit 0 if a check at least that strong passed on this tree,
#                                        or on one that differs only in docs (docs/**, *.md); prints why
# The tree id covers uncommitted and untracked files (a throwaway index, `git add -A`, write-tree).
set -uo pipefail
cd "$(dirname "$0")/../.."
dir=build/agent-check

tree_id() {
    local idx; idx=$(mktemp)
    cp "$(git rev-parse --git-path index)" "$idx" 2>/dev/null
    GIT_INDEX_FILE="$idx" git add -A >/dev/null 2>&1 && GIT_INDEX_FILE="$idx" git write-tree
    rm -f "$idx"
}

case "${1:-}" in
    record)
        mode="${2:?full|quick}"; mkdir -p "$dir"; tree_id >"$dir/$mode" ;;
    fresh)
        mode="${2:?full|quick}"; now=$(tree_id) || exit 1
        candidates=(full); [ "$mode" = quick ] && candidates=(full quick) # full covers quick
        for m in "${candidates[@]}"; do
            [ -s "$dir/$m" ] || continue
            was=$(cat "$dir/$m")
            git cat-file -e "$was" 2>/dev/null || continue
            if [ "$was" = "$now" ]; then echo "same files passed «$m» check"; exit 0; fi
            code=$(git diff --name-only "$was" "$now" | grep -vE '^docs/|\.md$' | head -3)
            if [ -z "$code" ]; then echo "only docs changed since the last passing «$m» check"; exit 0; fi
        done
        exit 1 ;;
    *) sed -n '2,7p' "$0" | sed 's/^# \{0,1\}//'; exit 2 ;;
esac
