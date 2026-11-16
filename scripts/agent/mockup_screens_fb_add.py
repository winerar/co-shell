"""Generated boards: #408 — one «Add» on every list screen (poll 2026-11-16).

Today four kinds: a floating «+ Добавить» (Категории), «+» in the header (Счета, Шаблоны, Регулярные, Бюджеты),
a row in the list (Валюты отчётов), a dashed slot (Курсы). Chosen А with the owner's change (2026-11-16): a ROUND «+» without text, floating bottom right, on every list screen,
and this is the style for future screens. Б (header «+») and В («+ Счёт») are dropped.
Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_parts import *  # noqa: F401,F403
from mockup_logic import proto
from mockup_screens_forms import FOLDERS, LOOSE, folder_card
from mockup_screens_settings import RATES, code_badge

FEEDBACK = [
    (408, 'Задача #408 · одна кнопка «Добавить» на всех экранах-списках',
     'Выбрано А с правкой (2026-11-16): круглая «+» без текста, плавающая внизу справа, на всех 7 экранах-списках; '
     'дальше новые экраны — так же. Ссылки внутри карточек остаются.'),
]

PAD = '<div style="height: 88px;"></div>'  # the list ends above the floating button


def accounts_body():
    out = big_total('Всего в общем балансе · 12 счетов', '9 640', 'USD') + '<div style="margin-top: 14px;">%s</div>' % segmented(['По типу', 'По валюте'], 'По типу', 'lime')
    for g, title, total in GROUPS:
        items = in_group(g)
        out += group_head(title, len(items), total) + group([acc_row(a) for a in items])
    return out


def categories_body():
    exp = ''.join(folder_card(*f) for f in FOLDERS).replace('＋ В папку', '＋ В группу') + ''.join('<div style="margin-top: 10px;">%s</div>' % card(row('<b>%s</b>' % t, 'без группы', dot_icon(i, c))) for t, i, c in LOOSE)
    return segmented(['Расходы', 'Доходы'], 'Расходы', 'lime') + exp + note('Показать архивные (2)', 14)


def rates_body():
    head = hero_card('<div style="display: flex; align-items: center; gap: 12px;">%s<span style="flex-grow: 1;"><div style="font-size: 15px; font-weight: 600;">Курсы на сегодня</div>'
                     '<div style="font-size: 12px; color: %s;">Загружено 16 ноября · currency-api</div></span>%s</div>' % (dot_icon('check', CYAN, 40), HERO_HINT, icon_button('refresh', 'Обновить', INK, True)),
                     pad='12px 12px 12px 14px', radius=20)
    rows = group([row('<span style="%s">%s</span>' % (MONO, r), s, code_badge(c), amount(d, '', OK if d.startswith('+') else PINK, 12) if d else '') for c, r, s, d in RATES])
    manual = ('<div style="display: flex; gap: 12px; padding: 12px 14px; border-radius: 24px; background: %s; border: 1px solid %s;"><span style="width: 4px; border-radius: 2px; background: %s;"></span>'
              '<span><div style="%s font-size: 15px;">1 USD = 1 185,00 ARS</div><div style="font-size: 12px; color: %s;">вручную · с 1 ноября · важнее загруженного</div></span></div>'
              % (VIOLETBG, '#3A2257', VIOLET, MONO, VIOLETINK))
    return head + label('Валюты ваших счетов') + rows + label('Ручные курсы') + manual


SCREENS = {'acc': ('Счета', accounts_body, 'Новый счёт'), 'cat': ('Категории', categories_body, 'Новая категория'), 'rate': ('Курсы валют', rates_body, 'Новый курс')}


def screen(key, v):
    title, body, noun_label = SCREENS[key]
    search = icon_button('search', 'Поиск', INK, True) if key == 'acc' else ''
    # Category folders fold on tap: the board needs the toggle state (Еда open, like «Категории»).
    wrap = (lambda t, h: proto(t, h, food=True, home=False, transport=False)) if key == 'cat' else board
    if key == 'acc':
        # «Счета» scrolled to the end: the header keeps its hairline, the last row stays above the button (88 px tail).
        inner = inner_head(title, search, scrolled=True) + ('<div style="height: 780px; overflow: hidden; display: flex; flex-direction: column; '
                                                            'justify-content: flex-end; padding: 0 16px;"><div>%s</div></div>' % (body() + PAD))
    else:
        inner = inner_screen(title, body() + PAD, search)
    return wrap(title, inner + fab(noun_label))


def where():
    """Common for all variants: the seven list screens and what the button adds there."""
    rows = [('Счета', 'новый счёт', 'был «+» в шапке'), ('Категории', 'категория или группа — выбор в редакторе', 'уже так'),
            ('Шаблоны', 'новый шаблон', 'был «+» в шапке'), ('Регулярные платежи', 'новый платёж', 'был «+» в шапке'),
            ('Бюджеты', 'новый лимит', 'был «+» в шапке'), ('Курсы валют', 'ручной курс', 'был пунктир внизу'),
            ('Валюты отчётов', 'ещё валюта', 'была строка в списке')]
    body = note('Одна и та же кнопка, одно и то же место, на всех экранах-списках. Что она добавляет:', 0) + '<div style="margin-top: 12px;">%s</div>' % group(
        [row('<b>%s</b>' % t, '%s · %s' % (what, was), dot_icon('plus', LIME, 36)) for t, what, was in rows])
    body += note('Круглая «+» 56, без текста; название читает озвучка («Новый счёт»). Не прячется при прокрутке; под концом списка — пустое место, чтобы последняя строка не уходила под кнопку. '
                 'Внутри карточек («Добавить подкатегорию», «+ В группу») ссылки остаются — они добавляют в конкретное место, а не на экран.', 14)
    return board('Где кнопка', inner_head('Экраны-списки') + '<div style="padding: 0 16px;">%s</div>' % body)


BOARDS = [  # name, title, fn, issue
    ('Add408Acc', '#408 · «Счета»: круглая «+», конец списка', lambda: screen('acc', 'A'), 408),
    ('Add408Cat', '#408 · «Категории»: круглая «+»', lambda: screen('cat', 'A'), 408),
    ('Add408Rate', '#408 · «Курсы»: круглая «+» вместо пунктира', lambda: screen('rate', 'A'), 408),
    ('Add408Where', '#408 · где кнопка и что добавляет', where, 408),
]
