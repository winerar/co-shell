"""Generated boards: #408 — one «Add» on every list screen (poll 2026-11-16).

Today four kinds: a floating «+ Добавить» (Категории), «+» in the header (Счета, Шаблоны, Регулярные, Бюджеты),
a row in the list (Валюты отчётов), a dashed slot (Курсы). Variants: А — floating «+ Добавить» everywhere,
Б — «+» in the header everywhere, В — floating button named by what it adds («+ Счёт»).
Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_parts import *  # noqa: F401,F403
from mockup_logic import proto
from mockup_screens_forms import FOLDERS, LOOSE, folder_card
from mockup_screens_settings import RATES, code_badge

FEEDBACK = [
    (408, 'Отзыв #408 · одна кнопка «Добавить» на всех экранах-списках',
     'Сейчас 4 вида: плавающая «+ Добавить» (Категории), «+» в шапке (Счета, Шаблоны, Регулярные, Бюджеты), строка в списке '
     '(Валюты отчётов), пунктир (Курсы). Варианты: А — плавающая «+ Добавить» везде (рекомендую), Б — «+» в шапке везде, '
     'В — плавающая с названием («+ Счёт»).'),
]

PAD = '<div style="height: 88px;"></div>'  # the list ends above the floating button


def head_plus():
    return ('<span style="width: 48px; height: 48px; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center; '
            'margin-left: 6px;">%s</span>' % (LIME, svg('plus', ONLIME, 24, 2.2)))


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


SCREENS = {'acc': ('Счета', accounts_body, 'Счёт'), 'cat': ('Категории', categories_body, 'Категория'), 'rate': ('Курсы валют', rates_body, 'Курс')}


def screen(key, v):
    title, body, noun = SCREENS[key]
    search = icon_button('search', 'Поиск', INK, True) if key == 'acc' else ''
    # Category folders fold on tap: the board needs the toggle state (Еда open, like «Категории»).
    wrap = (lambda t, h: proto(t, h, food=True, home=False, transport=False)) if key == 'cat' else board
    if v == 'B':
        return wrap(title, inner_screen(title, body(), search + head_plus()))
    if key == 'acc':
        # «Счета» scrolled to the end: the header keeps its hairline, the last row stays above the button (88 px tail).
        inner = inner_head(title, search, scrolled=True) + ('<div style="height: 780px; overflow: hidden; display: flex; flex-direction: column; '
                                                            'justify-content: flex-end; padding: 0 16px;"><div>%s</div></div>' % (body() + PAD))
    else:
        inner = inner_screen(title, body() + PAD, search)
    return wrap(title, inner + fab('Добавить' if v == 'A' else noun))


def where():
    """Common for all variants: the seven list screens and what the button adds there."""
    rows = [('Счета', 'новый счёт', 'был «+» в шапке'), ('Категории', 'категория или группа — выбор в редакторе', 'уже так'),
            ('Шаблоны', 'новый шаблон', 'был «+» в шапке'), ('Регулярные платежи', 'новый платёж', 'был «+» в шапке'),
            ('Бюджеты', 'новый лимит', 'был «+» в шапке'), ('Курсы валют', 'ручной курс', 'был пунктир внизу'),
            ('Валюты отчётов', 'ещё валюта', 'была строка в списке')]
    body = note('Одна и та же кнопка, одно и то же место, на всех экранах-списках. Что она добавляет:', 0) + '<div style="margin-top: 12px;">%s</div>' % group(
        [row('<b>%s</b>' % t, '%s · %s' % (what, was), dot_icon('plus', LIME, 36)) for t, what, was in rows])
    body += note('Кнопка не прячется при прокрутке; под концом списка — пустое место, чтобы последняя строка не уходила под кнопку. '
                 'Внутри карточек («Добавить подкатегорию», «+ В группу») ссылки остаются — они добавляют в конкретное место, а не на экран.', 14)
    return board('Где кнопка', inner_head('Экраны-списки') + '<div style="padding: 0 16px;">%s</div>' % body)


BOARDS = [  # name, title, fn, issue
    ('Add408AAcc', '#408 А · «Счета»: плавающая «+ Добавить», конец списка', lambda: screen('acc', 'A'), 408),
    ('Add408ACat', '#408 А · «Категории»: плавающая «+ Добавить»', lambda: screen('cat', 'A'), 408),
    ('Add408ARate', '#408 А · «Курсы»: плавающая вместо пунктира', lambda: screen('rate', 'A'), 408),
    ('Add408BAcc', '#408 Б · «Счета»: «+» в шапке', lambda: screen('acc', 'B'), 408),
    ('Add408BCat', '#408 Б · «Категории»: «+» в шапке', lambda: screen('cat', 'B'), 408),
    ('Add408BRate', '#408 Б · «Курсы»: «+» в шапке', lambda: screen('rate', 'B'), 408),
    ('Add408VAcc', '#408 В · «Счета»: «+ Счёт»', lambda: screen('acc', 'V'), 408),
    ('Add408VCat', '#408 В · «Категории»: «+ Категория»', lambda: screen('cat', 'V'), 408),
    ('Add408VRate', '#408 В · «Курсы»: «+ Курс»', lambda: screen('rate', 'V'), 408),
    ('Add408Where', '#408 · общее: где кнопка и что добавляет', where, 408),
]
