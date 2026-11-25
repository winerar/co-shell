#!/usr/bin/env python3
"""Builds the landing page of the closed test (#660): static pages in ru/en/es with the current release baked in.

Usage: make_landing.py <version.json> <out-dir>

Reads the release manifest CI writes (scripts/make-update-files.py): version, size, SHA-256, notes and the APK URL.
Layout — variant «Б» of poll 2026-11-25 (docs/design/poll-660.json): app screen from the screenshot references,
three points, QR to the page (computers only), install steps for any Android (one list, owner 2026-11-25), «Что нового» on the Russian page only.
Texts — landing_texts.py; the page address for the QR — env LANDING_URL, else GitHub Pages of the releases repo.
"""
import html
import json
import os
import re
import shutil
import sys

import privacy_pages
import qr_code
from landing_texts import TEXTS

LANGS = [("ru", "RU"), ("en", "EN"), ("es", "ES")]
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
TEMPLATE = os.path.join(ROOT, "site", "index.template.html")
SHOTS_DIR = os.path.join(ROOT, "app", "src", "test", "screenshots")
# Screen of the app per language: (light, dark or None) — screenshot references of the Overview (ScreensScreenshotTest).
SHOTS = {"ru": ("home_light.png", "home_dark.png"), "en": ("home_en.png", None), "es": ("home_es.png", None)}
NEWS_LANGS = ("ru",)  # whats-new.md is Russian only (poll 2026-11-25, «А»)

esc = lambda s: html.escape(str(s), quote=True)  # noqa: E731


def size_mb(size):
    return f"{size / 1_000_000:.1f}"


def site_url(manifest):
    """Root of the landing site: LANDING_URL, else https://<owner>.github.io/<repo>/ from the APK URL; None if unknown."""
    custom = os.environ.get("LANDING_URL", "").strip()
    if custom:
        return custom.rstrip("/") + "/"
    m = re.match(r"https://github\.com/([^/]+)/([^/]+)/releases/", manifest.get("apk", ""))
    return f"https://{m.group(1).lower()}.github.io/{m.group(2)}/" if m else None


def steps_html(steps):
    out = []
    for title, hint, dialog in steps:
        item = esc(title)
        if hint:
            item += f"<small>{esc(hint)}</small>"
        if dialog:
            head, text, cancel, action = dialog
            item += (f'<div class="sys" aria-hidden="true"><b>{esc(head)}</b>{esc(text)}'
                     f'<div class="act"><span>{esc(cancel)}</span><span class="go">{esc(action)}</span></div></div>')
        out.append(f"<li>{item}</li>")
    return "".join(out)


def news_html(lang, manifest, t):
    if lang not in NEWS_LANGS:
        return ""
    lines = [ln.strip().lstrip("•-").strip() for ln in manifest.get("notes", "").splitlines()]
    lines = [ln for ln in lines if ln and not ln.startswith("#")]
    if not lines:
        return ""
    items = "".join(f"<li>{esc(ln)}</li>" for ln in lines)
    return f'<details class="news" open><summary>{esc(t["news"].format(version=manifest["versionName"]))}</summary><ul>{items}</ul></details>'


def shot_html(lang, t):
    light, dark = SHOTS[lang]
    imgs = f'<img class="light-img" src="../img/{light}" alt="{esc(t["shot_alt"])}">'
    if dark:
        imgs += f'<img class="dark-img" src="../img/{dark}" alt="{esc(t["shot_alt"])}">'
    return f'<div class="phone{" has-dark" if dark else ""}">{imgs}</div>'


def qr_html(lang, manifest, t):
    root = site_url(manifest)
    if not root:
        return ""
    return (f'<div class="card qr">{qr_code.svg(root + lang + "/", module=4, quiet=2)}'
            f'<div><b>{esc(t["qr_title"])}</b><p>{esc(t["qr_text"])}</p></div></div>')


def page(template, lang, manifest):
    t = TEXTS[lang]
    links = privacy_pages.TEXTS[lang]
    mb = size_mb(manifest["size"])
    if lang in ("ru", "es"):
        mb = mb.replace(".", ",")
    text = {
        "LANG": lang, "TITLE": t["title"], "H1": t["h1"], "LEAD": t["lead"], "DOWNLOAD": t["download"], "MB": t["mb"],
        "META": t["meta"].format(version=manifest["versionName"]), "SHA_LABEL": t["sha"], "INVITE_TITLE": t["invite_title"],
        "INVITE_TEXT": t["invite_text"], "STEPS_TITLE": t["steps"],
        "UPDATE_TITLE": t["update_title"], "UPDATE_TEXT": t["update_text"], "FOOTER": t["footer"],
        "PRIVACY_LINK": links["privacy_link"], "DELETE_LINK": links["delete_link"],
        "SIZE_MB": mb, "SHA256": manifest["sha256"], "APK_URL": manifest["apk"],
    }
    raw = {
        "LANGS": " ".join(("<b>%s</b>" % label) if code == lang else f'<a href="../{code}/">{label}</a>' for code, label in LANGS),
        "QR": qr_html(lang, manifest, t), "SHOT": shot_html(lang, t),
        "POINTS": '<div class="points">' + "".join(
            f'<div class="point"><i aria-hidden="true">{esc(icon)}</i><div><b>{esc(head)}</b><p>{esc(body)}</p></div></div>'
            for icon, head, body in t["points"]) + "</div>",
        "INSTALL_STEPS": steps_html(t["install"]), "NEWS": news_html(lang, manifest, t),
    }
    out = template
    for key, value in raw.items():
        out = out.replace("{{%s}}" % key, value)
    for key, value in text.items():
        out = out.replace("{{%s}}" % key, esc(value))
    return out


def chooser():
    """`/` — the visitor's language (the first of ru/en/es the browser asks for, else Russian); without scripts, three links."""
    links = " · ".join(f'<a href="{code}/">{label}</a>' for code, label in LANGS)
    return (
        '<!doctype html><html lang="ru"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">'
        "<title>Co-Shell</title><script>"
        "var l=(navigator.languages||[navigator.language||'ru']).map(function(x){return x.slice(0,2)})"
        ".filter(function(x){return x==='ru'||x==='en'||x==='es'})[0]||'ru';location.replace(l+'/');"
        f"</script></head><body><p>{links}</p></body></html>\n"
    )


def build(manifest, out_dir, template):
    for lang, _ in LANGS:
        os.makedirs(os.path.join(out_dir, lang), exist_ok=True)
        with open(os.path.join(out_dir, lang, "index.html"), "w", encoding="utf-8") as f:
            f.write(page(template, lang, manifest))
    os.makedirs(os.path.join(out_dir, "img"), exist_ok=True)
    for name in {n for pair in SHOTS.values() for n in pair if n}:
        shutil.copyfile(os.path.join(SHOTS_DIR, name), os.path.join(out_dir, "img", name))
    with open(os.path.join(out_dir, "index.html"), "w", encoding="utf-8") as f:
        f.write(chooser())
    privacy_pages.write_all(out_dir)
    open(os.path.join(out_dir, ".nojekyll"), "w").close()


def main():
    manifest_path, out_dir = sys.argv[1], sys.argv[2]
    with open(manifest_path, encoding="utf-8") as f:
        manifest = json.load(f)
    with open(TEMPLATE, encoding="utf-8") as f:
        template = f.read()
    build(manifest, out_dir, template)
    print(f"landing {manifest['versionName']} → {out_dir}")


if __name__ == "__main__":
    main()
