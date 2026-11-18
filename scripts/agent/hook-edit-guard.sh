#!/usr/bin/env bash
# PostToolUse hook (Edit|Write): fast guard for golden rules 1, 3, 9 (AGENTS.md) right after a .kt edit,
# instead of waiting for `check` (3–6 min). Same rules as ProjectRulesTest, but only on the edited file.
# Violation → exit 2 with a message for the agent; anything else → silent exit 0.
f=$(python3 -c 'import json,sys; print(json.load(sys.stdin).get("tool_input",{}).get("file_path",""))' 2>/dev/null)
case "$f" in *.kt) ;; *) exit 0 ;; esac
[ -f "$f" ] || exit 0
root="$(cd "$(dirname "$0")/../.." && pwd)"; rel="${f#"$root"/}"
msg=""
case "$rel" in
    core/model/src/*|core/domain/src/*)
        grep -nE '(^|[^[:alnum:]_.])(android|androidx|java|javax)\.[a-z]' "$f" | head -3 | sed 's/^/  /' >/tmp/.g1
        [ -s /tmp/.g1 ] && msg+="Правило 3: core:model/domain — чистый Kotlin, без android.*/java.*:\n$(cat /tmp/.g1)\n"
        grep -nE '\b(Double|Float)\b|\.to(Double|Float)(OrNull)?\(|roundToLong' "$f" | head -3 | sed 's/^/  /' >/tmp/.g2
        [ -s /tmp/.g2 ] && msg+="Правило 1: деньги — только Money (Long), без Double/Float:\n$(cat /tmp/.g2)\n" ;;
    core/data/src/*)
        grep -nE '\bDouble\b|\.toDouble(OrNull)?\(|roundToLong' "$f" | head -3 | sed 's/^/  /' >/tmp/.g2
        [ -s /tmp/.g2 ] && msg+="Правило 1: без Double в core:data (исключения — FLOAT_ALLOWED в ProjectRulesTest):\n$(cat /tmp/.g2)\n" ;;
esac
case "$rel" in
    app/src/main/*Preview*.kt|app/src/main/*Samples*.kt|*/test/*|*/androidTest/*) ;;
    app/src/main/*.kt)
        grep -nP '"[^"]*[А-Яа-яЁё]' "$f" | grep -v '^\s*[0-9]*:\s*//' | head -3 | sed 's/^/  /' >/tmp/.g3
        [ -s /tmp/.g3 ] && msg+="Правило 9: текст в коде запрещён — ресурс на всех языках (glossary.md, texts.md):\n$(cat /tmp/.g3)\n" ;;
esac
rm -f /tmp/.g1 /tmp/.g2 /tmp/.g3
[ -z "$msg" ] && exit 0
printf '%b' "$msg" >&2
exit 2
