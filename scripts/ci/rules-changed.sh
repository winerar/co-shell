#!/usr/bin/env bash
# Did a change touch the Firebase rules or their tests? (ci.yml: run scripts/test-rules.sh, deploy on dev)
#   scripts/ci/rules-changed.sh <base>   base = "origin/dev...HEAD" (PR) or "HEAD^1" (merge into dev)
# Exit 0: changed, 1: not. The deploy script is in the list so that changing it (or adding it) redeploys.
set -uo pipefail
base=${1:?usage: rules-changed.sh <base>}
if [[ "$base" == *..* ]]; then changed=$(git diff --name-only "$base") || exit 1
else changed=$(git diff --name-only "$base" HEAD) || exit 1; fi
grep -E '^firebase/(firestore\.rules|database\.rules\.json|firebase\.json|rules-test/)|^scripts/ci/deploy-rules\.sh$' \
    <<<"$changed" || { echo "Firebase rules unchanged"; exit 1; }
