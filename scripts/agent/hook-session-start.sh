#!/usr/bin/env bash
# SessionStart hook: golden rule 7 «Начало» as code — stdout goes into the session context.
cd "$(dirname "$0")/../.." || exit 0
echo "# agent.sh start (автоматически, SessionStart)"
timeout 60 scripts/agent/agent.sh start 2>&1 | head -80
echo "Дальше: прочитать docs/agents/owner.md и notes.md."
