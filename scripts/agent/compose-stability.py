#!/usr/bin/env python3
"""Compose stability check (#383): every screen state the UI reads must be stable.

Reads the Compose compiler report (`app/build/compose_reports/app-classes.txt`, written by
`agent.sh stability`) and fails on an unstable state class: a name with `UiState` or ending in
`State` (nested classes included, e.g. `HomeUiState.Row`, `PastReleasesState.Loaded`).
An unstable state redraws its whole screen on every emission even when nothing changed.
Fix: an immutable type from a module without the Compose compiler → `compose-stability.conf`;
a recursive class of `app` → `@Immutable`; a mutable field → make it immutable.
"""
import re
import sys

STATE = re.compile(r"(UiState|State)(\.|$)")


def unstable_states(text):
    found = []
    for block in re.findall(r"^unstable class (\S+) \{\n(.*?)^\}", text, re.S | re.M):
        name, body = block
        simple = name.split("app.coshell.", 1)[-1]
        parts = simple.split(".")
        # the class or one of its enclosing classes is a state holder (not a ViewModel or a controller)
        if not any(STATE.search(p) for p in parts):
            continue
        fields = [line.strip() for line in body.splitlines() if line.strip().startswith("unstable ")]
        found.append((simple, fields))
    return found


def main(path="app/build/compose_reports/app-classes.txt"):
    try:
        text = open(path, encoding="utf-8").read()
    except FileNotFoundError:
        print(f"нет отчёта {path} — запусти `scripts/agent/agent.sh stability`")
        return 2
    stable = len(re.findall(r"^stable class", text, re.M))
    bad = unstable_states(text)
    if not bad:
        print(f"OK: все классы состояний стабильны (стабильных классов в отчёте: {stable})")
        return 0
    print(f"НЕСТАБИЛЬНЫЕ состояния экранов ({len(bad)}) — см. compose-stability.conf / @Immutable:")
    for name, fields in bad:
        print(f"  {name}")
        for f in fields:
            print(f"      {f}")
    return 1


if __name__ == "__main__":
    sys.exit(main(*sys.argv[1:]))
