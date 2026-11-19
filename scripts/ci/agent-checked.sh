#!/usr/bin/env bash
# Did the agent's own full check already cover the code of this PR? (ci.yml, PRs into dev; docs/agents/ci.md).
#   scripts/ci/agent-checked.sh [commit]   → exit 0: skip Gradle, 1: build as usual
# `agent.sh ship/finish` adds the trailer «Agent-Check: full» only when `check assembleDebug` passed locally on
# exactly the committed files (or on files that differ from them only in docs — check-cache.sh). On a PR the checkout
# is the merge commit (base + head): Gradle is skipped when the head carries the trailer and the merge adds no code
# beyond the head (dev has not moved on in code). Docs, *.md and scripts/agent/ are not built, so they may differ.
set -uo pipefail
commit=${1:-HEAD}

head=$(git rev-parse -q --verify "$commit^2") || head=$(git rev-parse "$commit")
if ! git log -1 --format=%B "$head" | git interpret-trailers --parse | grep -qx 'Agent-Check: full'; then
    echo "${head:0:7} has no «Agent-Check: full» — full build"; exit 1
fi
code=$(git diff --name-only "$head" "$commit" | grep -vE '^docs/|\.md$|^scripts/agent/' | head -3)
if [ -n "$code" ]; then
    echo "The base brings code the agent did not check ($(echo $code)) — full build"; exit 1
fi
echo "${head:0:7}: the agent's full check passed on this code (Agent-Check: full) — Gradle skipped"
