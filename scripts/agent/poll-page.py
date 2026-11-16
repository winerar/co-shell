#!/usr/bin/env python3
"""Build the owner's poll page: pick a mockup variant per question (format approved 2026-11-13).

Usage: poll-page.py <spec.json> <out-dir> [--img <dir>]
--img: take ready <dir>/<Board>.png (standalone mockups, e.g. mockup_icon_426.py) instead of rendering canvas boards.
spec.json: {"title": "...", "h1": "...", "intro": "...", "questions": [
  {"id": "i129", "num": "#129", "title": "...", "lead": "...",
   "opts": [{"v": "A", "t": "А: ...", "rec": true, "d": "...", "img": ["Fb129A"]}, ...],
   "common": ["Fb129All"], "link": {"href": "https://…", "text": "живая страница"}}]}  # link — optional
Writes <out-dir>/index.html and <out-dir>/img/<Board>.png (dark «Вольт», via render-mockups.js);
"<Board>@light" in img/common — the same board in the light theme (img/<Board>@light.png).
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
    args = sys.argv[1:]
    ready = None
    if "--img" in args:
        i = args.index("--img")
        ready, args = args[i + 1], args[:i] + args[i + 2:]
    if len(args) != 2:
        sys.exit(__doc__)
    spec = json.load(open(args[0], encoding="utf-8"))
    out = args[1]
    img = os.path.join(out, "img")
    os.makedirs(img, exist_ok=True)
    names = boards(spec)
    if ready:
        missing = [n for n in names if not os.path.exists(os.path.join(ready, n + ".png"))]
        if missing:
            sys.exit("нет картинок: " + ", ".join(missing))
        for n in names:
            shutil.copy(os.path.join(ready, n + ".png"), os.path.join(img, n + ".png"))
        names = []
    path = lambda n: os.path.join(ROOT, "docs/design/mockups", n.split("@")[0] + ".dc.html")  # noqa: E731
    missing = [path(n) for n in names if not os.path.exists(path(n))]
    if missing:
        sys.exit("нет досок: " + ", ".join(missing))
    raw = os.path.join(out, "raw")
    for theme, group in (("dark", [n for n in names if "@" not in n]), ("light", [n for n in names if n.endswith("@light")])):
        if group:
            subprocess.run(["node", os.path.join(HERE, "render-mockups.js"), raw, theme, "Вольт", *dict.fromkeys(map(path, group))], check=True)
        for n in group:
            shutil.move(os.path.join(raw, "%s.%s.Вольт.png" % (n.split("@")[0], theme)), os.path.join(img, n + ".png"))
    shutil.rmtree(raw, ignore_errors=True)
    page = open(os.path.join(HERE, "poll-template.html"), encoding="utf-8").read()
    qs = json.dumps(spec["questions"], ensure_ascii=False).replace("</", "<\\/")
    for key in ("title", "h1", "intro"):
        page = page.replace("__" + key.upper() + "__", html.escape(spec[key]))
    page = page.replace("__QUESTIONS__", qs)
    open(os.path.join(out, "index.html"), "w", encoding="utf-8").write(page)
    print(f"{out}/index.html · картинок: {len(os.listdir(img))}")


if __name__ == "__main__":
    main()
