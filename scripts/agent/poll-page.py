#!/usr/bin/env python3
"""Build the owner's poll page: pick a mockup variant per question (format approved 2026-11-13).

Usage: poll-page.py <spec.json> <out-dir>
spec.json: {"title": "...", "h1": "...", "intro": "...", "questions": [
  {"id": "i129", "num": "#129", "title": "...", "lead": "...",
   "opts": [{"v": "A", "t": "А: ...", "rec": true, "d": "...", "img": ["Fb129A"]}, ...],
   "common": ["Fb129All"]}]}
Writes <out-dir>/index.html and <out-dir>/img/<Board>.png (dark «Вольт», via render-mockups.js).
Publish: Artifact publish file_path=<out>/index.html, files {"img/X.png": "<out>/img/X.png"}, capabilities {"db": {}}.
Read answers: ArtifactData list, collection "answers" (doc id = question id: {choice, note?, at}).
"""
import html
import json
import os
import shutil
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(HERE))


def boards(spec):
    names = []
    for q in spec["questions"]:
        for o in q["opts"]:
            names += o.get("img", [])
        names += q.get("common", [])
    return list(dict.fromkeys(names))


def main():
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    spec = json.load(open(sys.argv[1], encoding="utf-8"))
    out = sys.argv[2]
    img = os.path.join(out, "img")
    os.makedirs(img, exist_ok=True)
    names = boards(spec)
    files = [os.path.join(ROOT, "docs/design/mockups", n + ".dc.html") for n in names]
    missing = [f for f in files if not os.path.exists(f)]
    if missing:
        sys.exit("нет досок: " + ", ".join(missing))
    raw = os.path.join(out, "raw")
    subprocess.run(["node", os.path.join(HERE, "render-mockups.js"), raw, "dark", "Вольт", *files], check=True)
    for n in names:
        shutil.move(os.path.join(raw, n + ".dark.Вольт.png"), os.path.join(img, n + ".png"))
    shutil.rmtree(raw, ignore_errors=True)
    page = open(os.path.join(HERE, "poll-template.html"), encoding="utf-8").read()
    qs = json.dumps(spec["questions"], ensure_ascii=False).replace("</", "<\\/")
    for key in ("title", "h1", "intro"):
        page = page.replace("__" + key.upper() + "__", html.escape(spec[key]))
    page = page.replace("__QUESTIONS__", qs)
    open(os.path.join(out, "index.html"), "w", encoding="utf-8").write(page)
    print(f"{out}/index.html · досок: {len(names)}")


if __name__ == "__main__":
    main()
