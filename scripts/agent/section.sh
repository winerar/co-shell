#!/usr/bin/env bash
# Print one Markdown section instead of the whole file (saves tokens): from the heading that contains
# the text to the next heading of the same or higher level.
#   section.sh docs/agents/workflow.md "Конец сессии"
#   section.sh docs/agents/workflow.md            — only the list of headings with line numbers
set -uo pipefail
cd "$(dirname "$0")/../.."
file=${1:?файл}; want=${2:-}
[ -z "$want" ] && { grep -n '^#' "$file"; exit 0; }
awk -v w="$want" '
    /^#+ / { lvl = length($1)
             if (on && lvl <= start) exit
             if (!on && index($0, w)) { on = 1; start = lvl } }
    on { printf "%d: %s\n", NR, $0 }' "$file" | grep . || { echo "нет раздела «$want» в $file:"; grep -n '^#' "$file"; exit 1; }
