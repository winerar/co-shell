#!/usr/bin/env bash
# Is HEAD a merge whose files were already checked by CI? (ci.yml on dev). Needs GH_TOKEN and gh.
#   scripts/ci/tree-already-checked.sh [commit]   → exit 0: skip Gradle, 1: build as usual
# True when the commit is a merge, its tree equals the tree of the merged head (second parent: the PR
# branch already contained the base), and that head has a successful ci.yml run. Then the merge adds
# nothing new and the PR run has already linted, tested and built exactly these files.
set -uo pipefail
commit=${1:-HEAD}

head=$(git rev-parse -q --verify "$commit^2") || { echo "Not a merge — full build"; exit 1; }
if [ "$(git rev-parse "$commit^{tree}")" != "$(git rev-parse "$head^{tree}")" ]; then
    echo "Merge changes files beyond ${head:0:7} — full build"; exit 1
fi
green=$(gh run list --workflow ci.yml --commit "$head" --status success --limit 1 --json databaseId -q '.[0].databaseId // empty' 2>/dev/null)
[ -n "$green" ] || { echo "No green CI run on ${head:0:7} — full build"; exit 1; }
echo "Tree of $(git rev-parse --short "$commit") = PR head ${head:0:7}, CI green there (run $green) — Gradle skipped"
