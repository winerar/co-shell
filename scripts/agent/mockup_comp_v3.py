"""«Компоненты v3» (#367): every new and pending component in two rule sets, А «Капсула» and Б «Плитка» (mockup_comp_v3_kit).

Four wide boards under «Компоненты» on the canvas; each card shows А | Б of one component. The owner picks per
component; after «ок» the picked variants move to mockup_catalog.py (ApprovedComponentsTest reads it).
"""
from mockup_comp_v3_kit import *  # noqa: F401,F403
from mockup_comp_v3_new import NEW
from mockup_comp_v3_choice import CHOICE
from mockup_comp_v3_rows import ROWS
from mockup_comp_v3_data import DATA
from mockup_catalog import section
from mockup_bricks import board

W = 1280
HEIGHTS = {'CompV3New': 2440, 'CompV3Choice': 2000, 'CompV3Rows': 2280, 'CompV3Data': 1756}  # content height + ~20, measured in Chromium

LEGEND = ('<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px;">'
          '<div style="padding: 14px 16px; border-radius: 20px; background: %s; border: 1px solid %s; font-size: 13px; line-height: 1.55;"><b>А · Капсула</b> — пилюли и круги; '
          'выбранное — неон + рамка цветом текста + жирный; дорожки залиты; строки — одной карточкой; радиус карточки 24.</div>'
          '<div style="padding: 14px 16px; border-radius: 12px; background: %s; border: 1px solid %s; font-size: 13px; line-height: 1.55;"><b>Б · Плитка</b> — скруглённые квадраты; '
          'выбранное — тон + рамка цветом текста + жирный; дорожки — контуром; строки — отдельными плитками; неон — только у главной кнопки; радиус 16.</div></div>'
          % (CARD, LINE, CARD, LINE))


def wide(title, intro, items):
    cards = ''.join(section('%d · %s' % (i + 1, t), pair(fn('A'), fn('B')), code) for i, (t, fn, code) in enumerate(items))
    body = ('<div style="padding: 40px;"><h1 style="margin: 0; font-size: 34px;">%s</h1><p style="margin: 8px 0 20px; max-width: 1100px; font-size: 15px; line-height: 1.45; color: %s;">%s</p>%s'
            '<div style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; align-items: start;">%s</div></div>' % (title, MUTED, intro, LEGEND, cards))
    return body


INTRO = ('Каждый компонент — в двух вариантах. А и Б — два набора правил: что бы вы ни выбрали по отдельности, форма, выбранное и рамки '
         'останутся одним языком. Контраст — по решениям #385: выбранное — рамка + жирный, пустые рамки и поля — outline, подписи — цветом текста.')

GROUPS = [
    ('CompV3New', 'Компоненты v3 · новые (10)', 'Чего нет в наборе — экраны рисуют это Material-виджетами или сами. ' + INTRO, NEW),
    ('CompV3Choice', 'Компоненты v3 · выбор (9)', 'Есть в коде, нет на доске (1 из 3): всё, чем выбирают. ' + INTRO, CHOICE),
    ('CompV3Rows', 'Компоненты v3 · строки и шторки (7)', 'Есть в коде, нет на доске (2 из 3): строки, формы, вопросы, шторки. ' + INTRO, ROWS),
    ('CompV3Data', 'Компоненты v3 · данные и теги (9)', 'Есть в коде, нет на доске (3 из 3): суммы, графики, теги. Цвета тегов и категорий — из палитры. ' + INTRO, DATA),
]


def make(name, title, intro, items):
    h = HEIGHTS[name]
    return board(title, wide(title, intro, items), height=h).replace('width: 390px; height: %dpx' % h, 'width: %dpx; height: %dpx' % (W, h)).replace(
        '"width":390,"height":%d' % h, '"width":%d,"height":%d' % (W, h))


BOARDS = [(n, (lambda n=n, t=t, i=i, it=it: make(n, t, i, it))) for n, t, i, it in GROUPS]
