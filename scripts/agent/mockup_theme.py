#!/usr/bin/env python3
"""Make design-canvas boards (docs/design/mockups/*.dc.html) follow the mockup theme switch.

Boards are authored in the «Вольт · тёмная» colours (#050805 background, #EEF5E6 text, #B8FF2E lime…).
This script replaces each colour with a token `k_<HEX>_<role>` and injects a tiny runtime that paints the
token in the chosen theme: Вольт · тёмная gives back the original hex (approved boards stay pixel-same),
every other palette / the light theme derives the colour by the formulas of
core/designsystem/.../theme/Color.kt (neutral ladder from the background) and PaletteSpecs.kt.

The theme comes from the board's Tweaks (theme, palette) or, when they say «общая», from the canvas-wide
switch (board «Тема макетов», localStorage key `coshell-mock-theme`; all boards share one origin).

Usage:  python3 scripts/agent/mockup_theme.py docs/design/mockups/Foo.dc.html [...]
Idempotent: only raw hexes are converted; the runtime block is replaced on every run.
"""
import json
import os
import re
import sys

from colorlab import _gam, _lin, hexs, rgb as _rgb  # noqa: E402

SPECS_KT = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))),
                        'core/designsystem/src/main/kotlin/app/coshell/core/designsystem/theme/PaletteSpecs.kt')
SPEC_FIELDS = ('primary', 'onPrimary', 'accentDark', 'accentLight', 'tertiaryDark', 'tertiaryLight', 'incomeDark', 'incomeLight',
               'expenseDark', 'expenseLight', 'backgroundDark', 'backgroundLight')

BG = (0x05, 0x08, 0x05)
WHITE = (255, 255, 255)
NEON = {  # role letter -> Volt dark colour (what boards are drawn with)
    'P': (0xB8, 0xFF, 0x2E),  # primary
    'a': (0xB2, 0x66, 0xFF),  # accent (violet)
    'i': (0x00, 0xF0, 0xFF),  # income (cyan)
    'e': (0xFF, 0x2E, 0x63),  # expense
    'y': (0xFF, 0xE6, 0x00),  # tertiary (acid yellow)
    'w': (0xFF, 0xB5, 0x47),  # warning — boards draw amber; the runtime paints the theme's own (CO_P.warn, poll 2026-11-20 Б)
    'g': (0x5C, 0xFF, 0xB0),  # ok / synced (status)
}
ON_PRIMARY = (0x0E, 0x1F, 0x00)
TOL = 13  # max channel error to accept a fit


def rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def fit(c, a0, b0):
    """Best t with c ≈ a0 + t (b0 - a0); returns (t, max error)."""
    d = [b - a for a, b in zip(a0, b0)]
    den = sum(x * x for x in d)
    if den == 0:
        return 0, 999
    t = sum((ci - ai) * di for ci, ai, di in zip(c, a0, d)) / den
    t = max(0.0, min(1.0, t))
    err = max(abs(ci - (ai + t * di)) for ci, ai, di in zip(c, a0, d))
    return t, err


# Colours of the board vocabulary that no formula fits: lime pill / container shades, inks on neon fills.
# Hero card gradient (#249): Volt dark stops of `heroDarkStops` (Theme.kt) -> roles H0..H2, painted from CO_P.hero.
# '161D13' — the top / bottom capsule (`PANEL` in mockup_bricks, `panelColor()` in code): role Cap, #493 Б.
OVERRIDES = {'161D13': 'Cap', 'B8FF2F': 'Ha', '314617': 'H0', '0E2523': 'H1', '271E36': 'H2', '3A4A1E': 'Pt28', '2D4A00': 'Pt30', '3F6B00': 'Pt42', '3D5A16': 'Pt33', '3B7A1C': 'Pt45'}
ON_INKS = {'1A0533': 'Oa', '002A2E': 'Oi', '16003A': 'Oa'}
KEEP = {'FFFFFF', '000000', '0B0F0A', '1A0A00', '1A1400', '002A3A', '2A1100'}  # + inks on fixed brand / category badges


def classify(hexv, ink):
    """Role of a Volt-dark colour; None = keep as is (brand colours, category palette…)."""
    c = rgb(hexv)
    hu = hexv.lstrip('#').upper()
    if hu in KEEP:
        return None
    if c == ON_PRIMARY:
        return 'O'
    if ink and hu in ON_INKS:
        return ON_INKS[hu]
    if hu in OVERRIDES:
        return OVERRIDES[hu]
    for k, v in NEON.items():
        if c == v:  # ink (text, strokes) turns to the deep shade in the light theme; fills stay neon
            return k + (('i' if k == 'P' else 'I') if ink else '')
    best = None
    t, err = fit(c, BG, WHITE)
    if err <= TOL:
        best = (err, 'n%02d' % round(t * 100))
    for k, v in NEON.items():
        t, err = fit(c, BG, v)  # dark tint of a neon
        if t <= 0.6 and err <= TOL and (best is None or err < best[0]):
            best = (err, '%st%02d' % (k, round(t * 100)))
        t, err = fit(c, v, WHITE)  # lighter neon (pastel ink)
        if 0 < t <= 0.75 and err <= TOL and (best is None or err < best[0]):
            best = (err, '%sw%02d' % (k, round(t * 100)))
    return best[1] if best else None


HEX = re.compile(r'#([0-9A-Fa-f]{6})\b')
INK_CTX = re.compile(r'(?:^|[;\s"\'{])(color|stroke|fill|caret-color)\s*[:=]\s*["\']?\s*$')
INK_KEY = re.compile(r'(ink|text|fg|stroke|color|link|Ink)\w*\s*:\s*$', re.I)


def token(hexv, before, js_key=False):
    ink = bool(INK_CTX.search(before[-40:])) or (js_key and bool(INK_KEY.search(before[-30:])))
    role = classify('#' + hexv, ink)
    return None if role is None else 'k_%s_%s' % (hexv.upper(), role)


def convert_markup(html):
    out, pos = [], 0
    for m in HEX.finditer(html):
        start = m.start()
        # skip hexes shown as text (between > and <) — e.g. token labels on the Components board
        lt, gt = html.rfind('<', 0, start), html.rfind('>', 0, start)
        if gt > lt:
            continue
        k = token(m.group(1), html[max(0, start - 60):start])
        if k:
            out.append(html[pos:start] + '{{co.%s}}' % k)
            pos = m.end()
    out.append(html[pos:])
    return ''.join(out)


STR = re.compile(r"'(?:[^'\\\n]|\\.)*'|\"(?:[^\"\\\n]|\\.)*\"|`(?:[^`\\]|\\.)*`")


def convert_js(js):
    def lit(m):
        s = m.group(0)
        q, body = s[0], s[1:-1]
        before = js[max(0, m.start() - 40):m.start()]
        parts, pos, changed = [], 0, False
        for h in HEX.finditer(body):
            k = token(h.group(1), before + q + body[:h.start()], js_key=True)
            if not k:
                continue
            changed = True
            parts.append((body[pos:h.start()], k))
            pos = h.end()
        if not changed:
            return s
        tail = body[pos:]
        if q == '`':
            return '`' + ''.join(a + "${__co('%s')}" % k for a, k in parts) + tail + '`'
        pieces = []
        for a, k in parts:
            if a:
                pieces.append(q + a + q)
            pieces.append("__co('%s')" % k)
        if tail:
            pieces.append(q + tail + q)
        return '(' + ' + '.join(pieces) + ')'
    return STR.sub(lit, js)


RUNTIME = r"""/*co-theme*/var CO_KEYS = %s;
/*CO_DATA*/
var CO_NOW = { dark: true, pal: 'Лайм' };
function __coTheme(props) {
  var s = {};
  try { s = JSON.parse(window.localStorage.getItem('coshell-mock-theme') || '{}') || {}; } catch (e) { s = {}; }
  var t = props && props.theme, p = props && props.palette;
  var dark = t === 'тёмная' ? true : t === 'светлая' ? false : s.dark !== false;
  var pal = CO_P[p] ? p : p === 'Вольт' ? 'Лайм' : CO_P[s.pal] ? s.pal : 'Лайм'; // «Вольт» (boards' authoring palette, the old name) = Лайм
  CO_NOW = { dark: dark, pal: pal };
  return CO_NOW;
}
function __coRgb(h) { var n = parseInt(h.slice(1), 16); return [n >> 16 & 255, n >> 8 & 255, n & 255]; }
function __coHex(c) { return '#' + c.map(function (v) { var x = Math.round(Math.max(0, Math.min(255, v))).toString(16); return x.length < 2 ? '0' + x : x; }).join('').toUpperCase(); }
function __coMix(a, b, t) { var x = __coRgb(a), y = __coRgb(b); return __coHex([0, 1, 2].map(function (i) { return x[i] + (y[i] - x[i]) * t; })); }
function __coLight(t) {
  var pts = [[0, 0], [0.35, 0.3], [0.5, 0.55], [0.62, 0.63], [0.8, 0.8], [0.92, 0.9], [1, 0.95]];
  for (var i = 1; i < pts.length; i++) if (t <= pts[i][0]) { var a = pts[i - 1], b = pts[i]; return a[1] + (b[1] - a[1]) * (t - a[0]) / (b[0] - a[0]); }
  return 0.95;
}
function __co(k) {
  var m = /^k_([0-9A-F]{6})_(.*)$/.exec(k);
  if (!m) return k;
  var orig = '#' + m[1], role = m[2];
  if (CO_NOW.dark && CO_NOW.pal === 'Лайм' && role.charAt(0) !== 'w') return orig;
  var T = CO_P[CO_NOW.pal], S = T.s, d = CO_NOW.dark;
  // Light (#493 Б / нежные А, poll 2026-11-23): white page, cards a wash of the primary, «black» = the palette's deep ink.
  var bg = d ? S[10] : '#FFFFFF', dim = d ? '#FFFFFF' : T.text;
  var neon = { P: [S[0], T.ink], a: [S[2], S[3]], y: [S[4], S[5]], i: [S[6], S[7]], e: [S[8], S[9]], w: T.warn, g: ['#5CFFB0', '#0B7F3D'] };
  if (role === 'O') return S[1];
  if (role === 'Cap') return d ? __coMix(bg, '#FFFFFF', 0.07) : T.cap; // top and bottom capsules: tone of the primary (neon) / of the accent (soft)
  if (role === 'Ha') return d ? S[0] : T.text; // hero amount: neon on the dark hero, body text on the light (neon) one
  if (/^H[0-2]$/.test(role)) return T.hero[(d ? 0 : 3) + parseInt(role.charAt(1), 10)];
  if (role === 'Oa' || role === 'Oi') return orig;
  if (role === 'Pi') return d ? S[0] : T.ink;
  if (neon[role]) return neon[role][0];
  if (role.length === 2 && role.charAt(1) === 'I' && neon[role.charAt(0)]) return d ? neon[role.charAt(0)][0] : neon[role.charAt(0)][1];
  var r = role.charAt(0), kind = role.charAt(1), t = parseInt(role.slice(r === 'n' ? 1 : 2), 10) / 100;
  if (r === 'n') {
    if (d) return __coMix(bg, dim, t);
    if (t < 0.012) return bg;
    if (t <= 0.045 || (t >= 0.065 && t <= 0.075)) return T.surf; // cards, sheets: 16 pct of the neon (12 pct soft)
    if (t <= 0.35) return __coMix(bg, dim, 0.04 + (t - 0.045) * 0.8); // fields, chips, lines — tinted, not grey
    return __coMix(bg, dim, __coLight(t));
  }
  var n = neon[r];
  if (!n) return orig;
  if (kind === 't') return d ? __coMix(bg, n[0], t) : __coMix(bg, n[0], Math.min(0.5, t * 0.8));
  if (kind === 'w') return d ? __coMix(n[0], '#FFFFFF', t) : n[1];
  return orig;
}
function __coMap() { var o = {}; for (var i = 0; i < CO_KEYS.length; i++) o[CO_KEYS[i]] = __co(CO_KEYS[i]); return o; }
/*co-theme-end*/
"""

def _oklab(h):
    r, g, b = (_lin(v) for v in _rgb(h))
    l_ = (0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b) ** (1 / 3)
    m_ = (0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b) ** (1 / 3)
    s_ = (0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b) ** (1 / 3)
    return (0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_, 1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_,
            0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_)


def ok_mix(a, b, t):
    """Compose `lerp(Color, Color, t)` — Oklab (notes-compose.md), so the boards match the app."""
    A, B = _oklab(a), _oklab(b)
    L, x, y = (A[i] + (B[i] - A[i]) * t for i in range(3))
    l_, m_, s_ = ((L + 0.3963377774 * x + 0.2158037573 * y) ** 3, (L - 0.1055613458 * x - 0.0638541728 * y) ** 3,
                  (L - 0.0894841775 * x - 1.2914855480 * y) ** 3)
    c = (4.0767416621 * l_ - 3.3077115913 * m_ + 0.2309699292 * s_, -1.2684380046 * l_ + 2.6097574011 * m_ - 0.3413193965 * s_,
         -0.0041960863 * l_ - 0.7034186147 * m_ + 1.7076147010 * s_)
    return hexs([_gam(max(0.0, min(1.0, v))) for v in c]).upper().lstrip('#')


def palette_specs(path=SPECS_KT):
    """The 9 themes of PaletteSpecs.kt: name -> {field: 'RRGGBB', 'soft': bool}, in file order."""
    out, name = {}, None
    for line in open(path, encoding='utf-8'):
        m = re.match(r'// (\S+) — (неон|пастель),', line)
        if m:
            name = m.group(1)
            out[name] = {'soft': False}
        m = re.match(r'\s+(\w+) = Color\(0xFF([0-9A-Fa-f]{6})\)', line)
        if m and name:
            out[name][m.group(1)] = m.group(2).upper()
        if name and re.match(r'\s+soft = true', line):
            out[name]['soft'] = True
    return out


def palette_data(specs=None):
    """Per theme what the runtime paints — the same formulas as Theme.kt / Color.kt (light: #493 Б, нежные — А)."""
    data = {}
    for n, p in (specs or palette_specs()).items():
        soft, pr, w = p['soft'], p['primary'], 'FFFFFF'
        hero_l = ([pr, ok_mix(pr, w, 0.3), ok_mix(p['accentDark'], w, 0.2)] if soft
                  else [ok_mix(pr, w, 0.45), ok_mix(pr, w, 0.15), pr])
        hero_d = [ok_mix(p['backgroundDark'], pr, 0.30), ok_mix(p['backgroundDark'], p['incomeDark'], 0.16),
                  ok_mix(p['backgroundDark'], p['accentDark'], 0.24)]
        data[n] = {'s': ['#' + p[f] for f in SPEC_FIELDS], 'ink': '#' + p['inkLight'], 'text': '#' + ok_mix(p['inkLight'], '000000', 0.55),
                   'surf': '#' + ok_mix(w, pr, 0.12 if soft else 0.16),
                   'cap': '#' + (ok_mix(w, p['accentLight'], 0.14) if soft else ok_mix(w, pr, 0.30)),
                   'warn': ['#' + p['warningDark'], '#' + p['warningLight']], 'hero': ['#' + c for c in hero_d + hero_l]}
    return data


def runtime_data():
    return 'var CO_P = %s;\n' % json.dumps(palette_data(), ensure_ascii=False)


THEME_PROPS = ('"theme":{"editor":"enum","options":["общая","тёмная","светлая"],"default":"общая","section":"Тема"},'
               '"palette":{"editor":"enum","options":["общая","Лайм","Гранат","Банан","Мате","Эспрессо","Квас","Сакура","Василёк","Лотос"],'
               '"default":"общая","section":"Тема"},')

CLASS_HOOKS = """  /*co-hooks*/componentDidMount() { var self = this; this.__coL = function (e) { if (!e || !e.key || e.key === 'coshell-mock-theme') self.forceUpdate(); }; try { window.addEventListener('storage', this.__coL); } catch (e) {} if (this.__coDidMount) this.__coDidMount(); }
  componentWillUnmount() { try { window.removeEventListener('storage', this.__coL); } catch (e) {} if (this.__coWillUnmount) this.__coWillUnmount(); }
  renderVals() { __coTheme(this.props); var v = this.__coRender(); v.co = __coMap(); v.coDark = CO_NOW.dark; v.coPal = CO_NOW.pal; v.coMode = CO_NOW.dark ? 'тёмная' : 'светлая'; return v; }/*co-hooks-end*/
"""

SCRIPT = re.compile(r'(<script type="text/x-dc" data-dc-script data-props=\')(.*?)(\'>)(.*?)(</script>)', re.S)


def convert(html):
    m = SCRIPT.search(html)
    if not m:
        raise SystemExit('no data-dc-script block')
    head, props, js = html[:m.start()], m.group(2), m.group(4)
    head = convert_markup(head)
    js = re.sub(r'/\*co-theme\*/.*?/\*co-theme-end\*/\n?', '', js, flags=re.S)
    js = re.sub(r'  /\*co-hooks\*/.*?/\*co-hooks-end\*/\n?', '', js, flags=re.S)
    js = convert_js(js)
    if '__coRender' not in js:
        js = re.sub(r'\brenderVals\s*\(\s*\)\s*\{', '__coRender() {', js, count=1)
        js = re.sub(r'\bcomponentDidMount\s*\(\s*\)\s*\{', '__coDidMount() {', js, count=1)
        js = re.sub(r'\bcomponentWillUnmount\s*\(\s*\)\s*\{', '__coWillUnmount() {', js, count=1)
    js = re.sub(r'(class Component extends DCLogic \{\n)', r'\1' + CLASS_HOOKS.replace('\\', '\\\\'), js, count=1)
    keys = sorted(set(re.findall(r'\{\{\s*co\.(k_[0-9A-F]{6}_\w+)\s*\}\}', head)) | set(re.findall(r"__co\('(k_[0-9A-F]{6}_\w+)'\)", js)))
    js = '\n' + (RUNTIME % ('[' + ','.join("'%s'" % k for k in keys) + ']')).replace('/*CO_DATA*/\n', runtime_data()) + js.lstrip('\n')
    if '"theme":' not in props:
        props = props.replace('{', '{' + THEME_PROPS, 1)
    return head + m.group(1) + props + m.group(3) + js + m.group(5) + html[m.end():]


if __name__ == '__main__':
    for path in sys.argv[1:]:
        with open(path, encoding='utf-8') as f:
            src = f.read()
        res = convert(src)
        with open(path, 'w', encoding='utf-8') as f:
            f.write(res)
        print('%s: %d tokens' % (path, len(set(re.findall(r'k_[0-9A-F]{6}_\w+', res)))))
