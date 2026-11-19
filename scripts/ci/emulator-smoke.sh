#!/usr/bin/env bash
# Smoke test of the R8 release build on a running emulator (called by emulator.yml; docs/agents/ci.md §«Эмулятор»).
#   scripts/ci/emulator-smoke.sh path/to/app-release.apk
# Fails on: a crash or ANR in logcat, the process dying, a tab label missing from the screen, a cold start slower
# than COLD_START_MAX_MS (emulator ≈ 3–5× slower than the phones; the goal is only to catch regressions).
# Writes a table to $GITHUB_STEP_SUMMARY (or stdout).
set -uo pipefail

APK=${1:?usage: emulator-smoke.sh app-release.apk}
PKG=app.coshell
ACT=$PKG/.MainActivity
COLD_START_MAX_MS=${COLD_START_MAX_MS:-6000}
PSS_MAX_KB=${PSS_MAX_KB:-300000}
MONKEY_EVENTS=${MONKEY_EVENTS:-1500}
OUT=${GITHUB_STEP_SUMMARY:-/dev/stdout}
fail=0
rows=()

note() { rows+=("| $1 | $2 | $3 |"); }
bad() { fail=1; echo "::error::$1"; }

crashes() {
  # Our process only: other apps on the image may crash on their own.
  { adb logcat -d -b crash 2>/dev/null | grep -F "$PKG" ; adb logcat -d -b events,main 2>/dev/null | grep -E "ANR in $PKG"; } || true
}
alive() { adb shell pidof "$PKG" >/dev/null 2>&1; }
ui() { adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1 && adb shell cat /sdcard/ui.xml; }
check_clean() { # $1 = step name
  local c; c=$(crashes)
  if [ -n "$c" ]; then bad "$1: crash/ANR"; echo "$c" | head -30; note "$1" "❌" "crash/ANR"; return 1; fi
  if ! alive; then bad "$1: process is gone"; note "$1" "❌" "process died"; return 1; fi
  return 0
}

adb wait-for-device
adb shell input keyevent 82 >/dev/null 2>&1 || true
# A slow emulator's own apps (Pixel Launcher) hang and cover the app with «… isn't responding» (#536): hide those
# system dialogs. Our own ANR still fails the run — crashes() reads it from the event log.
adb shell settings put global hide_error_dialogs 1 >/dev/null 2>&1 || true
adb install -r -g "$APK" || { bad "install failed"; exit 1; }
adb logcat -c

# 1. Cold start × 3 (median of TotalTime).
times=()
for _ in 1 2 3; do
  adb shell am force-stop "$PKG"; sleep 1
  t=$(adb shell am start -W -n "$ACT" 2>&1 | tr -d '\r' | awk -F': ' '/TotalTime/ {print $2}')
  times+=("${t:-0}"); sleep 2
done
median=$(printf '%s\n' "${times[@]}" | sort -n | sed -n 2p)
note "Cold start (median of 3)" "$([ "${median:-0}" -gt 0 ] && [ "$median" -le "$COLD_START_MAX_MS" ] && echo ✅ || echo ❌)" "${median:-?} ms (limit $COLD_START_MAX_MS)"
if ! [ "${median:-0}" -gt 0 ] || [ "$median" -gt "$COLD_START_MAX_MS" ]; then bad "cold start ${median:-?} ms > $COLD_START_MAX_MS"; fi
check_clean "Start"

# 2. Bottom bar: every tab label is on screen (labels are values/strings.xml → en on the emulator, ru fallback).
sleep 3
# Tap the centre of the first node whose text matches $1 (regex); returns 1 if there is none.
tap_text() {
  local b; b=$(echo "$xml" | grep -oE "text=\"($1)\"[^>]*bounds=\"\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]\"" | head -1 | grep -oE '[0-9]+' | tail -4 | tr '\n' ' ')
  [ -n "$b" ] || return 1
  set -- $b; adb shell input tap $(( ($1 + $3) / 2 )) $(( ($2 + $4) / 2 ))
}
# Over the tabs there can be a system «isn't responding» dialog of a slow emulator and the first-launch «App language»
# sheet. A dialog of another app (Pixel Launcher, #536) is closed — «Wait» let it hang again 4 times in a row; ours is
# waited for (a real ANR of ours fails check_clean anyway). The sheet is confirmed. Up to 8 tries.
for _ in 1 2 3 4 5 6 7 8; do
  xml=$(ui)
  if echo "$xml" | grep -qE "isn't responding|не отвечает|no responde"; then
    if echo "$xml" | grep -qE "text=\"co-shell[^\"]*(isn't responding|не отвечает|no responde)"; then
      tap_text "Wait|Подождать|Esperar" || adb shell input keyevent 4
    else
      tap_text "Close app|Закрыть приложение|Cerrar app|Cerrar aplicación" || adb shell input keyevent 4
    fi
  else
    tap_text "Done|Готово|Hecho" || break
  fi
  sleep 3
done
xml=$(ui)
missing=()
for tab in "Overview|Обзор|Home" "Transactions|Операции" "Reports|Отчёты|Informes" "More|Ещё"; do
  echo "$xml" | grep -Eq "text=\"($tab)\"|content-desc=\"($tab)\"" || missing+=("$tab")
done
if [ ${#missing[@]} -gt 0 ]; then
  echo "$xml" > ui-fail.xml; adb exec-out screencap -p > ui-fail.png 2>/dev/null || true
  adb shell dumpsys activity activities 2>/dev/null | grep -E "topResumedActivity|mResumedActivity" | head -3
  bad "tabs not on screen: ${missing[*]}"; note "Bottom bar" "❌" "missing: ${missing[*]}"; else note "Bottom bar" "✅" "4 tabs visible"; fi

# 3. Night mode and font scale on the fly: no crash, still alive (#230).
adb shell cmd uimode night yes >/dev/null; sleep 2
adb shell cmd uimode night no >/dev/null; sleep 2
check_clean "Night mode toggle" && note "Night mode toggle" "✅" "no crash"
adb shell settings put system font_scale 2.0; sleep 3
adb shell am start -n "$ACT" >/dev/null; sleep 3
check_clean "Font scale 2.0" && note "Font scale 2.0" "✅" "no crash"

# 4. Monkey: random taps/swipes inside the package; a crash makes it exit non-zero.
adb shell settings put system font_scale 1.0
adb shell am start -n "$ACT" >/dev/null; sleep 2
adb shell monkey -p "$PKG" -s 42 --throttle 60 --pct-syskeys 0 --pct-appswitch 0 --pct-anyevent 0 \
  --pct-motion 25 --pct-touch 55 --pct-nav 0 --pct-majornav 15 -v "$MONKEY_EVENTS" > monkey.log 2>&1
mk=$?
if [ $mk -ne 0 ] || grep -q "// CRASH" monkey.log; then bad "monkey: crash (exit $mk)"; grep -A25 "// CRASH" monkey.log | head -40; note "Monkey ($MONKEY_EVENTS events)" "❌" "crash"; else check_clean "Monkey" && note "Monkey ($MONKEY_EVENTS events)" "✅" "no crash"; fi

# 5. Memory after all that.
pss=$(adb shell dumpsys meminfo "$PKG" 2>/dev/null | tr -d '\r' | awk '/TOTAL PSS:/ {print $3} /^ *TOTAL +[0-9]/ {print $2; exit}' | head -1)
note "PSS after monkey" "$([ "${pss:-0}" -gt 0 ] && [ "$pss" -le "$PSS_MAX_KB" ] && echo ✅ || echo ⚠️)" "$(( ${pss:-0} / 1024 )) MB (limit $(( PSS_MAX_KB / 1024 )))"

# 6. Reinstall over itself keeps the app starting (data kept: `install -r`).
adb install -r "$APK" >/dev/null && adb shell am start -W -n "$ACT" >/dev/null; sleep 3
check_clean "Reinstall over" && note "Reinstall over (data kept)" "✅" "starts"

{
  echo "### Emulator: R8 release build"
  echo "| Step | | Result |"; echo "|---|---|---|"
  printf '%s\n' "${rows[@]}"
} >> "$OUT"
adb logcat -d > logcat.txt 2>/dev/null || true
exit $fail
