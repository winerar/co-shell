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
import re
import sys

BG = (0x05, 0x08, 0x05)
WHITE = (255, 255, 255)
NEON = {  # role letter -> Volt dark colour (what boards are drawn with)
    'P': (0xB8, 0xFF, 0x2E),  # primary
    'a': (0xB2, 0x66, 0xFF),  # accent (violet)
    'i': (0x00, 0xF0, 0xFF),  # income (cyan)
    'e': (0xFF, 0x2E, 0x63),  # expense
    'y': (0xFF, 0xE6, 0x00),  # tertiary (acid yellow)
    'w': (0xFF, 0xB5, 0x47),  # warning (status, same in all palettes)
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
# Hero card gradient (#249): Volt dark stops of `heroDarkStops` (Theme.kt) -> roles H0..H2, painted from CO_HERO.
OVERRIDES = {'314617': 'H0', '0E2523': 'H1', '271E36': 'H2', '3A4A1E': 'Pt28', '2D4A00': 'Pt30', '3F6B00': 'Pt42', '3D5A16': 'Pt33', '3B7A1C': 'Pt45'}
ON_INKS = {'1A0533': 'Oa', '002A2E': 'Oi', '16003A': 'Oa'}
KEEP = {'FFFFFF', '000000'}


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
var CO_SPECS = {
  'Вольт': ['#B8FF2E','#0E1F00','#B266FF','#7A2BD6','#FFE600','#6E6200','#00F0FF','#00708A','#FF2E63','#D1004B','#050805','#F9FFE9'],
  'Мохито': ['#91E668','#122A06','#A77BFF','#6236E0','#38D9FF','#00709E','#38D9FF','#00709E','#FF5C7A','#D6004A','#0B0F0C','#F7FBF2'],
  'Манго': ['#FF8A3D','#2B0E00','#FF3D7F','#C2185B','#FFC93C','#7A5600','#B6FF3D','#3F7300','#FF4D5E','#C8102E','#160C16','#FFF5EA'],
  'Ультра': ['#3D7BFF','#000B24','#FF2BD6','#B0008F','#91E668','#2F6B12','#7CFF6B','#2E7A00','#FF3B6B','#C4004A','#07090C','#F4F7FF'],
  'Лагуна': ['#2EE6C5','#00261F','#FF6B5C','#B8321F','#FFD27A','#735200','#7CF8FF','#006A78','#FF4D8D','#C2185B','#04100F','#F0FFFB'],
  'Сакура': ['#FF7AB8','#33001A','#7CF2C4','#00704F','#B9A4FF','#5B3FD0','#6BE4FF','#006A87','#FF4D5E','#C8102E','#120A10','#FFF4F8']
};
// Hero card gradient per palette: 3 dark stops (heroDarkStops, Oklab lerp in Theme.kt), then the 3 light ones (heroLight).
var CO_HERO = {
  'Вольт': ['#314617','#0E2523','#271E36','#B8FF2E','#8CFFA0','#5CF6FF'],
  'Мохито': ['#2E4727','#162A2B','#2B273D','#91E668','#8FE9C2','#8FE3FF'],
  'Манго': ['#522E27','#2D2C23','#461A2D','#FFB27A','#FFC2A0','#FFB3CC'],
  'Ультра': ['#16284A','#17291C','#3A1835','#A9C4FF','#D4C2FF','#FFB3F0'],
  'Лагуна': ['#12473E','#122E2E','#392721','#7CF2DD','#A8F0FF','#FFD2B8'],
  'Сакура': ['#4F283C','#23272F','#2D3935','#FFB3D6','#FFD0E4','#C9F7E4']
};
var CO_NOW = { dark: true, pal: 'Вольт' };
function __coTheme(props) {
  var s = {};
  try { s = JSON.parse(window.localStorage.getItem('coshell-mock-theme') || '{}') || {}; } catch (e) { s = {}; }
  var t = props && props.theme, p = props && props.palette;
  var dark = t === 'тёмная' ? true : t === 'светлая' ? false : s.dark !== false;
  var pal = CO_SPECS[p] ? p : CO_SPECS[s.pal] ? s.pal : 'Вольт';
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
  if (CO_NOW.dark && CO_NOW.pal === 'Вольт') return orig;
  var S = CO_SPECS[CO_NOW.pal], d = CO_NOW.dark, bg = d ? S[10] : S[11];
  var neon = { P: [S[0], S[0]], a: [S[2], S[3]], y: [S[4], S[5]], i: [S[6], S[7]], e: [S[8], S[9]], w: ['#FFB547', '#8A5F00'], g: ['#5CFFB0', '#0B7F3D'] };
  if (role === 'O') return S[1];
  if (/^H[0-2]$/.test(role)) return CO_HERO[CO_NOW.pal][(d ? 0 : 3) + parseInt(role.charAt(1), 10)];
  if (role === 'Oa' || role === 'Oi') return orig;
  if (role === 'Pi') return d ? S[0] : __coMix('#000000', bg, 0.1);
  if (neon[role]) return neon[role][0];
  if (role.length === 2 && role.charAt(1) === 'I' && neon[role.charAt(0)]) return d ? neon[role.charAt(0)][0] : neon[role.charAt(0)][1];
  var r = role.charAt(0), kind = role.charAt(1), t = parseInt(role.slice(r === 'n' ? 1 : 2), 10) / 100;
  if (r === 'n') {
    if (d) return __coMix(bg, '#FFFFFF', t);
    if (t < 0.012) return bg;
    if (t <= 0.045 || (t >= 0.065 && t <= 0.075)) return '#FFFFFF'; // cards, bars, sheets: white (owner's light boards)
    if (t <= 0.35) return __coMix(bg, '#000000', 0.04 + (t - 0.045) * 0.8); // fields, chips, lines
    return __coMix(bg, '#000000', __coLight(t));
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

THEME_PROPS = ('"theme":{"editor":"enum","options":["общая","тёмная","светлая"],"default":"общая","section":"Тема"},'
               '"palette":{"editor":"enum","options":["общая","Вольт","Мохито","Манго","Ультра","Лагуна","Сакура"],'
               '"default":"общая","section":"Тема"},')

CLASS_HOOKS = """  /*co-hooks*/componentDidMount() { var self = this; this.__coL = function (e) { if (!e || !e.key || e.key === 'coshell-mock-theme') self.forceUpdate(); }; try { window.addEventListener('storage', this.__coL); } catch (e) {} if (this.__coDidMount) this.__coDidMount(); }
  componentWillUnmount() { try { window.removeEventListener('storage', this.__coL); } catch (e) {} if (this.__coWillUnmount) this.__coWillUnmount(); }
  renderVals() { __coTheme(this.props); var v = this.__coRender(); v.co = __coMap(); v.coDark = CO_NOW.dark; return v; }/*co-hooks-end*/
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
    js = '\n' + RUNTIME % ('[' + ','.join("'%s'" % k for k in keys) + ']') + js.lstrip('\n')
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
