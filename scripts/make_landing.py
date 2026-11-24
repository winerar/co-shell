#!/usr/bin/env python3
"""Builds the landing page of the closed test (#660): static pages in ru/en/es with the current release baked in.

Usage: make_landing.py <version.json> <out-dir>

Reads the release manifest CI writes (scripts/make-update-files.py): version, size, SHA-256 and the fixed APK URL.
Writes <out>/{ru,en,es}/index.html from site/index.template.html, an <out>/index.html that sends the visitor to the page of
their browser's language (Russian when unsure), and .nojekyll. CI pushes <out> to the `gh-pages` branch of the public releases repo
(GitHub Pages) when the repository variable LANDING is true (.github/workflows/ci.yml). The page asks nobody for anything:
the app itself is closed by an invitation code (docs/research/closed-testing-plan.md).
"""
import html
import json
import os
import sys

import privacy_pages

LANGS = [("ru", "RU"), ("en", "EN"), ("es", "ES")]
TEMPLATE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "site", "index.template.html")

TEXTS = {
    "ru": {
        "title": "Co-Shell — семейный бюджет", "h1": "Семейный бюджет на двоих",
        "lead": "Для нескольких валют, без лишнего. Работает без интернета, общий бюджет — на всех телефонах семьи.",
        "download": "Скачать", "mb": "МБ", "meta": "Версия {version} · Android 8+ · обновляется само",
        "sha": "Контрольная сумма SHA-256", "invite_title": "Нужен код приглашения",
        "invite_text": "Приложение пока для приглашённых: при первом запуске введите код. Попросите его у того, кто вас позвал.",
        "steps": "Как установить", "step1": "Нажмите «Скачать» и откройте файл.", "step2": "Разрешите установку из этого источника.",
        "step3": "Если Play Protect предупредит — выберите «Всё равно установить».",
        "footer": "Только для Android. Данные хранятся на вашем телефоне.",
    },
    "en": {
        "title": "Co-Shell — family budget", "h1": "A budget for two",
        "lead": "Several currencies, nothing extra. Works offline; a shared budget on every phone in the family.",
        "download": "Download", "mb": "MB", "meta": "Version {version} · Android 8+ · updates itself",
        "sha": "SHA-256 checksum", "invite_title": "You need an invitation code",
        "invite_text": "The app is by invitation for now: enter the code on first launch. Ask the person who invited you.",
        "steps": "How to install", "step1": "Tap “Download” and open the file.", "step2": "Allow installing from this source.",
        "step3": "If Play Protect warns you, choose “Install anyway”.",
        "footer": "Android only. Your data stays on your phone.",
    },
    "es": {
        "title": "Co-Shell — presupuesto familiar", "h1": "Un presupuesto para dos",
        "lead": "Varias monedas, sin florituras. Funciona sin internet; un presupuesto compartido en todos los teléfonos de la familia.",
        "download": "Descargar", "mb": "MB", "meta": "Versión {version} · Android 8+ · se actualiza sola",
        "sha": "Suma de comprobación SHA-256", "invite_title": "Necesitas un código de invitación",
        "invite_text": "Por ahora la app es solo por invitación: introduce el código al abrirla. Pídeselo a quien te invitó.",
        "steps": "Cómo instalar", "step1": "Toca «Descargar» y abre el archivo.", "step2": "Permite instalar desde esta fuente.",
        "step3": "Si Play Protect avisa, elige «Instalar de todos modos».",
        "footer": "Solo para Android. Tus datos se quedan en tu teléfono.",
    },
}


def size_mb(size):
    return f"{size / 1_000_000:.1f}"


def page(template, lang, manifest):
    t = TEXTS[lang]
    mb = size_mb(manifest["size"])
    if lang in ("ru", "es"):
        mb = mb.replace(".", ",")
    langs = " ".join(("<b>%s</b>" % label) if code == lang else f'<a href="../{code}/">{label}</a>' for code, label in LANGS)
    values = {
        "LANG": lang, "TITLE": t["title"], "H1": t["h1"], "LEAD": t["lead"], "DOWNLOAD": t["download"], "MB": t["mb"],
        "META": t["meta"].format(version=manifest["versionName"]), "SHA_LABEL": t["sha"], "INVITE_TITLE": t["invite_title"],
        "INVITE_TEXT": t["invite_text"], "STEPS_TITLE": t["steps"], "STEP1": t["step1"], "STEP2": t["step2"], "STEP3": t["step3"],
        "FOOTER": t["footer"], "SIZE_MB": mb, "SHA256": manifest["sha256"], "APK_URL": manifest["apk"], "LANGS": langs,
    }
    out = template
    for key, value in values.items():
        out = out.replace("{{%s}}" % key, value if key in ("LANGS",) else html.escape(str(value), quote=True))
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
