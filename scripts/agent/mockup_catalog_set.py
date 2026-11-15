"""Approved component set (#367): owner's poll «Компоненты v3» 2026-11-15 — А «Капсула» for every group.

Four boards under «Компоненты»: each card is the А variant of one component plus «→ Compose name».
ApprovedComponentsTest reads the text of every mockup_catalog*.py — a proposal must live in another module
(mockup_comp_*.py) until the owner's «ок».
"""
from mockup_catalog_kit import *  # noqa: F401,F403
from mockup_catalog_new import NEW
from mockup_catalog_choice import CHOICE
from mockup_catalog_rows import ROWS
from mockup_catalog_data import DATA
from mockup_catalog import section
from mockup_bricks import board

W = 1280
HEIGHTS = {'CompNew': 1812, 'CompChoice': 1300, 'CompRows': 1530, 'CompData': 1480}  # content height + ~20, measured in Chromium

LEGEND = ('<div style="margin-bottom: 20px; padding: 14px 16px; border-radius: 20px; background: %s; border: 1px solid %s; font-size: 13px; line-height: 1.55;">'
          '<b>Капсула</b> — пилюли и круги; выбранное — неон + рамка цветом текста + жирный; невыбранное — на залитой дорожке; '
          'пустые рамки и поля — outline; подписи тегов — цветом текста; строки — одной карточкой; радиусы 28 · 24 · 16 · капсула; все кнопки 48; правила семей — доски «Единый стиль».</div>' % (CARD, LINE))


def wide(title, intro, items):
    cards = ''.join(section('%d · %s' % (i + 1, t), fn('A'), code) for i, (t, fn, code) in enumerate(items))
    return ('<div style="padding: 40px;"><h1 style="margin: 0; font-size: 34px;">%s</h1><p style="margin: 8px 0 20px; max-width: 1100px; font-size: 15px; line-height: 1.45; color: %s;">%s</p>%s'
            '<div style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; align-items: start;">%s</div></div>' % (title, MUTED, intro, LEGEND, cards))


INTRO = 'Утверждено владельцем 2026-11-15 (опрос «Компоненты v3», #367): вариант А «Капсула» для всех. Экраны собираются только из этих компонентов.'

GROUPS = [
    ('CompNew', 'Компоненты · новые (10)', 'Раньше экраны рисовали это Material-виджетами или сами. ' + INTRO, NEW),
    ('CompChoice', 'Компоненты · выбор (9)', 'Всё, чем выбирают. ' + INTRO, CHOICE),
    ('CompRows', 'Компоненты · строки и шторки (7)', 'Строки, формы, вопросы, шторки. ' + INTRO, ROWS),
    ('CompData', 'Компоненты · данные и теги (11)', 'Суммы, графики, теги. Цвета тегов и категорий — из палитры. ' + INTRO, DATA),
]


def make(name, title, intro, items):
    h = HEIGHTS[name]
    return board(title, wide(title, intro, items), height=h).replace('width: 390px; height: %dpx' % h, 'width: %dpx; height: %dpx' % (W, h)).replace(
        '"width":390,"height":%d' % h, '"width":%d,"height":%d' % (W, h))


BOARDS = [(n, (lambda n=n, t=t, i=i, it=it: make(n, t, i, it))) for n, t, i, it in GROUPS]
