#!/usr/bin/env bash
# Has the nightly check (warm-cache.yml) passed on this dev commit? It leaves the Actions cache key nightly-dev-<sha>.
#   scripts/ci/nightly-green.sh <sha>          → exit 0: green, 1: not checked
#   scripts/ci/nightly-green.sh <sha> --run    → not checked: dispatch the nightly check on dev and wait for it
# Needs GH_TOKEN, GH_REPO and gh. release.yml calls it before releasing (docs/agents/ci.md).
set -uo pipefail
sha=${1:?dev commit sha}; run=${2:-}
green() { [ -n "$(gh cache list --key "nightly-dev-$sha" --json key -q '.[].key' 2>/dev/null)" ]; }

if green; then echo "Nightly check is green on dev ${sha:0:7}"; exit 0; fi
[ "$run" = --run ] || { echo "No green nightly check on dev ${sha:0:7}"; exit 1; }

echo "No green nightly check on dev ${sha:0:7} — starting one"
gh workflow run warm-cache.yml --ref dev || exit 1
id=
for _ in $(seq 20); do
    sleep 6
    id=$(gh run list --workflow warm-cache.yml --event workflow_dispatch --limit 1 --json databaseId,status \
        -q '.[] | select(.status != "completed") | .databaseId')
    [ -n "$id" ] && break
done
[ -n "$id" ] || { echo "::error::The nightly check did not start"; exit 1; }
gh run watch "$id" >/dev/null || true
green && { echo "Nightly check is green on dev ${sha:0:7} (run $id)"; exit 0; }
echo "::error::Nightly check on dev ${sha:0:7} is not green: $(gh run view "$id" --json url -q .url)"; exit 1
