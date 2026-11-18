#!/usr/bin/env python3
"""Check bank brand colours in BankColors.kt against the services' own icons (#505, owner 2026-11-18).

For each preset: icon of the service's domain (Google favicon, then DuckDuckGo) -> most frequent saturated
colour -> compare with the colour in BankColors.kt. Prints a table; never edits the code: a flagged row is
judged by eye on the contact sheet (`--sheet out.png`), because a favicon is sometimes not the brand mark
(Tether, Revolut, VTB were left as is for that reason). Needs Pillow (`pip install pillow`) and network.

  python3 scripts/agent/bank-colors.py [--sheet out.png] [--only Name,Name] [--limit 45]
"""
import argparse
import collections
import colorsys
import io
import math
import re
import sys
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

SRC = Path(__file__).resolve().parents[2] / "core/designsystem/src/main/kotlin/app/coshell/core/designsystem/category/BankColors.kt"
# Preset name (as in BankColors.kt) -> domain whose icon shows the brand. New preset = new line here (checked).
DOMAINS = {
    "Galicia": "galicia.ar", "Santander": "santander.com.ar", "BBVA": "bbva.com.ar", "Nación": "bna.com.ar",
    "Mercado Pago": "mercadopago.com.ar", "Ualá": "uala.com.ar", "Naranja X": "naranjax.com", "Brubank": "brubank.com",
    "Lemon": "lemon.me", "Ameria": "ameriabank.am", "Inecobank": "inecobank.am", "Ardshinbank": "ardshinbank.am",
    "ACBA": "acba.am", "IDBank": "idbank.am", "Evocabank": "evoca.am", "Сбер": "sberbank.ru", "Т-Банк": "tbank.ru",
    "Альфа": "alfabank.ru", "ВТБ": "vtb.ru", "Ozon Банк": "ozon.ru", "Райффайзен": "raiffeisen.ru",
    "Binance": "binance.com", "Bybit": "bybit.com", "Tether": "tether.to", "Wise": "wise.com",
    "PayPal": "paypal.com", "Payoneer": "payoneer.com", "Revolut": "revolut.com",
}


def parse_presets(text):
    """[(name, 0xRRGGBB)] from `Preset("Name", "XX", 0xFFRRGGBB…` lines."""
    return [(m.group(1), int(m.group(2), 16) & 0xFFFFFF)
            for m in re.finditer(r'Preset\("([^"]+)",\s*"[^"]+",\s*0xFF([0-9A-Fa-f]{6})', text)]


def dominant(pixels):
    """Most frequent saturated, not-too-dark colour of RGBA [pixels] as (r, g, b, count); None if all grey/white."""
    bins = collections.Counter()
    for r, g, b, a in pixels:
        if a < 200:
            continue
        _, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
        if s < 0.35 or v < 0.25 or (v > 0.98 and s < 0.5):
            continue
        bins[(r // 24, g // 24, b // 24)] += 1
    if not bins:
        return None
    (br, bg, bb), _ = bins.most_common(1)[0]
    members = [(r, g, b) for r, g, b, a in pixels if a >= 200 and (r // 24, g // 24, b // 24) == (br, bg, bb)]
    n = len(members)
    return tuple(sum(p[i] for p in members) // n for i in range(3)) + (sum(bins.values()),)


def fetch_icon(domain):
    from PIL import Image
    urls = (f"https://www.google.com/s2/favicons?domain={domain}&sz=128", f"https://icons.duckduckgo.com/ip3/{domain}.ico")
    for url in urls:
        try:
            data = urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"}), timeout=20).read()
            im = Image.open(io.BytesIO(data))
            im.load()
            if im.size[0] >= 32 and len(data) > 500:  # Google's grey globe placeholder is tiny
                return im.convert("RGBA")
        except Exception:
            continue
    return None


def analyse(name, color):
    im = fetch_icon(DOMAINS[name])
    if im is None:
        return name, color, None, None
    small = im.resize((64, 64))
    return name, color, dominant(list(small.getdata())), im


def contact_sheet(rows, path):
    from PIL import Image, ImageDraw
    sheet = Image.new("RGB", (max(1, len(rows)) * 150, 170), "white")
    draw = ImageDraw.Draw(sheet)
    for i, (name, color, _, im) in enumerate(rows):
        bg = Image.new("RGBA", (128, 128), "white")
        bg.alpha_composite(im.resize((128, 128)))
        sheet.paste(bg.convert("RGB"), (i * 150 + 10, 5))
        draw.rectangle([i * 150 + 10, 138, i * 150 + 70, 165], fill=((color >> 16) & 255, (color >> 8) & 255, color & 255))
        draw.text((i * 150 + 75, 145), name[:8], fill="black")
    sheet.save(path)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--sheet")
    ap.add_argument("--only")
    ap.add_argument("--limit", type=float, default=45, help="RGB distance above which a row is flagged")
    args = ap.parse_args()
    presets = parse_presets(SRC.read_text(encoding="utf8"))
    missing = [n for n, _ in presets if n not in DOMAINS]
    if missing:
        sys.exit(f"✗ no domain in DOMAINS for: {', '.join(missing)} — add it to this script")
    if args.only:
        wanted = set(args.only.split(","))
        presets = [p for p in presets if p[0] in wanted]
    try:
        import PIL  # noqa: F401
    except ImportError:
        sys.exit("✗ Pillow is needed: pip install pillow")
    with ThreadPoolExecutor(8) as pool:
        rows = list(pool.map(lambda p: analyse(*p), presets))
    flagged = []
    for name, color, dom, im in rows:
        cur = ((color >> 16) & 255, (color >> 8) & 255, color & 255)
        if dom is None:
            print(f"{name:14} #{color:06X}  — no usable icon (keep, check by hand)")
            continue
        d = math.dist(dom[:3], cur)
        mark = "ok" if d <= args.limit else "LOOK"
        print(f"{name:14} #{color:06X}  icon #{dom[0]:02X}{dom[1]:02X}{dom[2]:02X}  Δ={d:4.0f}  {mark}")
        if d > args.limit:
            flagged.append((name, color, dom, im))
    print(f"{len(flagged)} to look at of {len(rows)}; judge by eye, the favicon may not be the brand mark")
    if args.sheet and flagged:
        contact_sheet(flagged, args.sheet)
        print(f"contact sheet: {args.sheet} (icon above, current colour below)")


if __name__ == "__main__":
    main()
