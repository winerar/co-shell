"""«Компоненты v3» (#367, owner 2026-11-15: «по два варианта каждого компонента, везде единый код»).

Two rule sets; every component is drawn in both, so any pick reads as one language:
  А «Капсула» — pills and circles; chosen = neon fill + ink frame + bold; tracks are filled.
  Б «Плитка»  — rounded squares; chosen = tint + ink frame + bold; tracks are outlined; neon only for the main action.
Both keep the #385 decisions: chosen — ink frame + bold, empty frames and fields — outline, tag labels — ink.
Only «Вольт · тёмная» constants (mockup_theme repaints them).
"""
from mockup_comp_v2_parts import *  # noqa: F401,F403

RULES = {
    'A': dict(name='Капсула', ctl=999, card=24, tile=20, icon='50%', small=999, h=44),
    'B': dict(name='Плитка', ctl=12, card=16, tile=12, icon='10px', small=8, h=44),
}


def r(v, k):
    """Radius token of rule set v: ctl (controls), card, tile, icon (icon holder), small (chips, pills)."""
    x = RULES[v][k]
    return x if isinstance(x, str) else '%dpx' % x


def sel(v):
    """Chosen state (#385 Б): А — neon + ink frame; Б — tint + ink frame. Both bold."""
    if v == 'A':
        return 'background: %s; color: %s; font-weight: 700; box-shadow: inset 0 0 0 2px %s;' % (LIME, ONLIME, INK)
    return 'background: %s; color: %s; font-weight: 700; box-shadow: inset 0 0 0 2px %s;' % (LIMEBG, INK, INK)


def idle(v):
    """Not chosen: А — on a filled track (no frame); Б — outline frame (#385 Б)."""
    return 'color: %s;' % INK2 if v == 'A' else 'color: %s; box-shadow: inset 0 0 0 1px %s;' % (INK2, OUTLINE)


def track(v):
    return 'background: %s; padding: 4px;' % CHIP if v == 'A' else 'box-shadow: inset 0 0 0 1px %s; padding: 3px;' % OUTLINE


def ink_on_sel(v):
    return ONLIME if v == 'A' else INK


def holder(v, icon, color, size=36, tint=True):
    """Icon holder: circle (А) or rounded square (Б); tinted with the colour or filled by it."""
    bg = mix(color, 20) if tint else color
    return ('<span style="width: %dpx; height: %dpx; flex: none; border-radius: %s; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            % (size, size, r(v, 'icon'), bg, svg(icon, color if tint else ONLIME, size // 2 + 1, 1.9)))


def box(v, inner, pad='14px', bg=None):
    return '<div style="border-radius: %s; background: %s; border: 1px solid %s; padding: %s;">%s</div>' % (r(v, 'card'), bg or CARD, LINE, pad, inner)


def btn(v, text, kind='primary', icon=None, w='auto', disabled=False):
    st = {
        'primary': 'background: %s; color: %s; font-weight: 600;' % (LIME, ONLIME),
        'outline': 'box-shadow: inset 0 0 0 1px %s; color: %s;' % (OUTLINE, INK),
        'danger': 'background: %s; color: %s; font-weight: 600;' % (PINKBG, PINK),
        'tonal': 'background: %s; color: %s; font-weight: 600;' % (VIOLETPILL, VIOLETINK),
        'text': 'color: %s; font-weight: 600;' % LIME,
    }[kind]
    ic = svg(icon, ONLIME if kind == 'primary' else (PINK if kind == 'danger' else INK), 18, 2) if icon else ''
    return ('<span style="height: %dpx; padding: 0 18px; width: %s; box-sizing: border-box; border-radius: %s; font-size: 15px; display: inline-flex; align-items: center; '
            'justify-content: center; gap: 8px; white-space: nowrap; %s%s">%s%s</span>' % (52 if v == 'A' else 48, w, r(v, 'ctl') if v == 'A' else '14px', st,
                                                                    ' opacity: 0.4;' if disabled else '', ic, text))


def fld(v, lbl, value, placeholder=False, error=''):
    """Text field: frame outline (#385 В) in both; А — pill-ish r16 with label inside, Б — r12 with label above."""
    vc = MUTED2 if placeholder else INK
    frame = PINK if error else OUTLINE
    err = '<span style="font-size: 12px; color: %s;">%s</span>' % (PINK, error) if error else ''
    if v == 'A':
        f = ('<div style="min-height: 56px; padding: 7px 18px; box-sizing: border-box; border-radius: 16px; background: %s; box-shadow: inset 0 0 0 1px %s; display: flex; '
             'flex-direction: column; justify-content: center;"><span style="font-size: 12px; color: %s;">%s</span><span style="font-size: 16px; color: %s;">%s</span></div>'
             % (FIELD, frame, PINK if error else MUTED, lbl, vc, value))
        return col(f, err, gap=4)
    f = ('<div style="height: 48px; padding: 0 14px; box-sizing: border-box; border-radius: 12px; box-shadow: inset 0 0 0 1px %s; display: flex; align-items: center; '
         'font-size: 16px; color: %s;">%s</div>' % (frame, vc, value))
    return col('<span style="font-size: 13px; font-weight: 600; color: %s;">%s</span>' % (PINK if error else MUTED, lbl), f, err, gap=6)


def pair(a, b):
    """Two variants side by side with the rule-set name over each."""
    head = '<div style="font-size: 12px; font-weight: 700; letter-spacing: 0.06em; color: %s;">%s</div>'
    return ('<div style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 20px;">'
            '<div style="display: flex; flex-direction: column; gap: 10px; min-width: 0;">%s%s</div>'
            '<div style="display: flex; flex-direction: column; gap: 10px; min-width: 0; padding-left: 20px; border-left: 1px dashed %s;">%s%s</div></div>'
            % (head % (MUTED, 'А · КАПСУЛА'), a, LINE2, head % (MUTED, 'Б · ПЛИТКА'), b))


def both(fn, *args, **kw):
    return pair(fn('A', *args, **kw), fn('B', *args, **kw))
