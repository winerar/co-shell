"""The fan over «+» (#623, poll 2026-11-23 «А», poll board `EwHoldA`): hold «+» on any tab — three round buttons open on an arc (QR чека, Заметка,
Шаблон; «Голос» comes with voice entry); the finger led to one lights it. Board `HomeHoldFan`; imported by mockup_screens_forms.py."""
from mockup_parts import *  # noqa: F401,F403
import mockup_screens_fb_qr526 as qr526
import mockup_screens_main as m

PLUS_X, PLUS_Y = 195, 797  # centre of the «+» in the bottom bar of a board


def item(cx, cy, icon, label, on=False):
    glyph = (qr526.QR % (ONLIME if on else INK)) if icon == 'qr' else svg(icon, ONLIME if on else INK, 24, 1.8)
    circle = ('<span style="position: absolute; left: %dpx; top: %dpx; width: 56px; height: 56px; border-radius: 50%%; display: flex; align-items: center; justify-content: center; %s">%s</span>'
              % (cx - 28, cy - 28, ('background: %s; box-shadow: inset 0 0 0 2px %s;' % (LIME, ONLIME)) if on else ('background: %s; border: 1px solid %s;' % (CHIP, OUTLINE)), glyph))
    name = ('<span style="position: absolute; left: %dpx; top: %dpx; width: 88px; text-align: center; font-size: 12px;"><span style="padding: 2px 8px; border-radius: 999px; background: %s; color: %s;">%s</span></span>'
            % (cx - 44, cy + 32, CHIP, INK, label))
    return circle + name


def fan():
    arc = item(PLUS_X - 77, PLUS_Y - 92, 'qr', 'QR чека') + item(PLUS_X, PLUS_Y - 120, 'note', 'Заметка', True) + item(PLUS_X + 77, PLUS_Y - 92, 'list', 'Шаблон')
    return board('Обзор: удержали «+» — веер', m.home() + arc)


BOARDS = [('HomeHoldFan', fan)]
