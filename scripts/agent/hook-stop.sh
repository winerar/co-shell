#!/usr/bin/env bash
# Stop hook: golden rule 7 «Конец». Only when there is work (uncommitted or unpushed); blocks the stop once
# with the list of problems from `session-end.sh --pre` + git state. No work → silent.
cd "$(dirname "$0")/../.." || exit 0
input=$(cat)
python3 -c 'import json,sys; sys.exit(0 if json.loads(sys.argv[1]).get("stop_hook_active") else 1)' "$input" 2>/dev/null && exit 0
up=$(git rev-parse -q --verify '@{u}' 2>/dev/null)
dirty=$(git status --porcelain | wc -l)
ahead=0; [ -n "$up" ] && ahead=$(git rev-list --count '@{u}..HEAD')
[ "$dirty" -eq 0 ] && [ "$ahead" -eq 0 ] && exit 0
out=$(scripts/agent/session-end.sh --pre 2>&1 | grep '^✗')
[ "$dirty" -gt 0 ] && out+=$'\n'"✗ не закоммичено файлов: $dirty — agent.sh ship/finish"
[ "$ahead" -gt 0 ] && out+=$'\n'"✗ не запушено коммитов: $ahead — agent.sh push"
[ -z "${out//[$'\n ']/}" ] && exit 0
python3 -c 'import json,sys; print(json.dumps({"decision":"block","reason":"Конец сессии (workflow.md): "+sys.argv[1]}))' "$out"
