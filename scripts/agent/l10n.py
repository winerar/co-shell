#!/usr/bin/env python3
"""Untranslated texts (translations paused until publication, owner 2026-11-24).

l10n.py          list keys present in res/values (ru) but missing in values-en / values-es, and the pending-file state
l10n.py --write  rewrite app/src/test/resources/translations-pending.txt from the real difference
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RES = ROOT / "app/src/main/res"
PENDING = ROOT / "app/src/test/resources/translations-pending.txt"


def keys(folder):
    out = set()
    for f in (RES / folder).glob("strings*.xml"):
        text = f.read_text(encoding="utf-8")
        for m in re.finditer(r'<string\s+name="([^"]+)"([^>]*)>', text):
            if 'translatable="false"' not in m.group(2):
                out.add(m.group(1))
        for m in re.finditer(r'<plurals\s+name="([^"]+)"', text):
            out.add("plural:" + m.group(1))
    return out


def main():
    base = keys("values")
    langs = sorted(d.name for d in RES.glob("values-??") if (d / "strings.xml").exists())
    missing = set()
    for lang in langs:
        miss = base - keys(lang)
        print(f"{lang}: не хватает {len(miss)}")
        missing |= miss
    header = [l for l in PENDING.read_text(encoding="utf-8").splitlines() if l.startswith("#")]
    current = {l.strip() for l in PENDING.read_text(encoding="utf-8").splitlines() if l.strip() and not l.startswith("#")}
    if "--write" in sys.argv:
        PENDING.write_text("\n".join(header + sorted(missing)) + "\n", encoding="utf-8")
        print(f"записано: {len(missing)} ключей в {PENDING.relative_to(ROOT)}")
        return
    new, stale = sorted(missing - current), sorted(current - missing)
    print(f"в списке ожидания: {len(current)}; не внесено: {len(new)}; устарело: {len(stale)}")
    for k in new[:30]:
        print("  + " + k)
    for k in stale[:30]:
        print("  - " + k + " (уже переведён)")
    if new or stale:
        print("Исправить: agent.sh l10n --write")


main()
