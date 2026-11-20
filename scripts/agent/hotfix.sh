#!/usr/bin/env bash
# Start a hotfix (docs/agents/hotfix.md): put the current branch on origin/main — what the phones run — instead of dev.
#   scripts/agent/hotfix.sh            (agent.sh hotfix)
# Refuses on main/dev, with uncommitted changes or with own commits that are not a hotfix yet.
set -uo pipefail
cd "$(dirname "$0")/../.."

branch=$(git rev-parse --abbrev-ref HEAD)
case "$branch" in main | dev) echo "✗ on $branch — switch to the session branch first"; exit 1 ;; esac
[ -z "$(git status --porcelain --untracked-files=no)" ] || { echo "✗ commit or stash changes first"; exit 1; }
git fetch -q origin main dev || { echo "✗ fetch failed"; exit 1; }

if git log --no-merges --format=%s origin/main..HEAD | grep -q '^Hotfix:'; then
    echo "OK: $branch is already a hotfix branch (on main + $(git rev-list --count origin/main..HEAD) commits)"
else
    own=$(git rev-list --no-merges --count HEAD --not origin/main origin/dev)
    [ "$own" -eq 0 ] || { echo "✗ $branch has $own own commits — a hotfix starts from a clean branch"; exit 1; }
    git checkout -q -B "$branch" origin/main && git branch -q --unset-upstream 2>/dev/null
    echo "OK: $branch now starts at origin/main ($(git rev-parse --short HEAD)) — what the phones run"
fi
cat <<'TXT'
Дальше (docs/agents/hotfix.md):
  1. Тест, который воспроизводит сбой, затем минимальная правка (или git revert виновного PR).
  2. docs/whats-new.md: новый верхний раздел «## <дата> · исправление» + «• Исправлено: …».
  3. agent.sh ship -m "Hotfix: <что чиним>" -m "Fixes #N" (сообщение коммита начинается с «Hotfix:»).
  4. PR в main (GitHub MCP create_pull_request, base main, не черновик, «Fixes #N» в описании).
     CI + эмулятор → автомерж в main → сборка на телефонах → main сам вливается в dev.
  Не делать: agent.sh update/sync (влил бы dev в хотфикс).
TXT
