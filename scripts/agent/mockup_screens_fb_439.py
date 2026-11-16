"""Generated boards: #439 — one look for every picker sheet (poll 2026-11-16).

Today: «Категория» in forms is a flat list with doubles («Дети» and «Дети / Дети»), no icons, huge gaps; «Все категории» is a grid
with names cut to «Корректиро…», a header shifted against the search and no close button; the paddings differ between sheets (36 / 46 px).
Variants: the category list (А groups with headers / Б collapsible groups), «Все категории» (А grid, 2-line names / Б the same list),
the other pickers (А one frame / Б the same + «Недавние»). Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_screens_fb_290 import cat_cell, grid4

FEEDBACK = [
    (439, 'Задача #439 · шторки выбора — один вид',
     'Опрос 2026-11-16: список «Категория» (группы или сворачивание), «Все категории» (сетка или тот же список), остальные шторки (счёт, валюта) — одна рамка.'),
]

# (group, [(category, icon, colour)]) — a group of one is just a category, no header and no «X / X» double.
CATS_G = [
    ('Еда', [('Продукты', 'cart', '#8BC34A'), ('Кафе', 'cafe', '#FF9800'), ('Рестораны и бары', 'cafe', '#FF7A59')]),
    ('Транспорт', [('Такси', 'taxi', '#FFEB3B'), ('Общественный транспорт', 'bus', '#00D1FF'), ('Бензин и парковка', 'bolt', '#FFB547')]),
    ('Дом', [('Аренда', 'house', '#00BCD4'), ('Коммунальные платежи', 'bolt', '#2EE6C5'), ('Ремонт', 'gear', '#FFB547'), ('Связь', 'wifi', '#C79BFF')]),
    ('Дети', [('Дети', 'user', '#FFD43B')]),
    ('Для дома', [('Для дома', 'house', '#E0A96B')]),
    ('Документы', [('Документы', 'file', '#B8C2D6')]),
    ('Одежда и обувь', [('Одежда и обувь', 'gift', '#FF8FA3')]),
    ('Здоровье', [('Аптека', 'pill', '#E91E63'), ('Врач', 'pill', '#FF5C7F')]),
    ('Прочее', [('Корректировка баланса', 'more', '#9AA594'), ('Общественные мероприятия', 'spark', '#B266FF')]),
]
ON = 'Кафе'


def head(title, sub=''):
    s = '<div style="font-size: 13px; color: %s; margin-top: 2px;">%s</div>' % (MUTED, sub) if sub else ''
    return ('<div style="display: flex; align-items: center; gap: 8px; padding: 6px 8px 0 20px;"><div style="flex-grow: 1; padding: 6px 0;">'
            '<b style="font-size: 22px;">%s</b>%s</div>%s</div>' % (title, s, icon_button('close', 'Закрыть', MUTED)))


def frame(title, body, sub='', field='Найти категорию', top=64):
    inner = head(title, sub) + '<div style="padding: 4px 20px 0;">%s%s</div>' % (search(field), body)
    return scrim() + sheet(inner, top)


def check():
    return svg('check', LIME, 22, 2.4)


def cat_row(n, ic, col, on=False, indent=0):
    return ('<div style="display: flex; align-items: center; gap: 12px; min-height: 52px; padding: 6px 0 6px %dpx; box-sizing: border-box;">%s'
            '<span style="flex-grow: 1; min-width: 0; font-size: 15px; line-height: 1.25; color: %s;">%s</span>%s</div>'
            % (indent, dot_icon(ic, col, 36), INK if on else INK2, n, check() if on else ''))


def group_label(g, n, color=MUTED):
    return ('<div style="display: flex; align-items: center; margin: 14px 0 2px; font-size: 12px; font-weight: 600; letter-spacing: 0.05em; text-transform: uppercase; color: %s;">'
            '<span style="flex-grow: 1;">%s</span><span style="font-size: 12px; text-transform: none; letter-spacing: 0;">%d</span></div>' % (color, g, n))


def cat_list_a():
    body = '<div style="margin-top: 4px;">'
    singles = [i[0] for g, i in CATS_G if len(i) == 1]
    for g, items in CATS_G:
        if len(items) == 1:
            continue
        body += group_label(g, len(items)) + ''.join(cat_row(n, ic, c, n == ON) for n, ic, c in items)
    body += group_label('Без группы', len(singles)) + ''.join(cat_row(n, ic, c) for g, i in CATS_G if len(i) == 1 for n, ic, c in i)
    return frame('Категория', body, sub='Расход · 25 категорий')


def cat_list_b():
    body = '<div style="margin-top: 8px;">'
    for g, items in CATS_G:
        first = items[0]
        if len(items) == 1:
            body += cat_row(first[0], first[1], first[2], first[0] == ON)
            continue
        open_ = any(n == ON for n, _, _ in items)
        body += ('<div style="display: flex; align-items: center; gap: 12px; min-height: 52px;">%s<span style="flex-grow: 1; font-size: 15px; font-weight: 600;">%s</span>'
                 '<span style="font-size: 12px; color: %s;">%d</span><span style="color: %s;">%s</span></div>'
                 % (dot_icon('folder', items[0][2], 36), g, MUTED, len(items), MUTED, '▾' if open_ else '▸'))
        if open_:
            body += ''.join(cat_row(n, ic, c, n == ON, 24) for n, ic, c in items)
    return frame('Категория', body, sub='Расход · 25 категорий')


def grid_a():
    def cells(items):
        return [cat_cell(n, ic, c, n == ON, 40, 12).replace('white-space: nowrap; overflow: hidden; max-width: 100%; text-overflow: ellipsis;',
                                                            'text-align: center; line-height: 1.2; overflow-wrap: anywhere;') for n, ic, c in items]
    body = ''
    for g, items in CATS_G:
        if len(items) > 1:
            body += label(g, top=10) + grid4(cells(items), '10px 4px', 3)
    singles = [i for g, items in CATS_G if len(items) == 1 for i in items]
    body += label('Без группы', top=10) + grid4(cells(singles), '10px 4px', 3)
    return frame('Все категории · 25', body)


def other_head(title, field, rows, recent=None):
    body = ''
    if recent:
        body += label('Недавние', top=12) + ''.join(recent) + label('Все', top=6)
    return frame(title, '<div style="margin-top: 6px;">%s%s</div>' % (body, ''.join(rows)), field=field)


def acc_row(n, k, i, c, ink, v, cur, on=False):
    return ('<div style="display: flex; align-items: center; gap: 12px; min-height: 56px; box-sizing: border-box;">%s<span style="flex-grow: 1; min-width: 0; display: flex; flex-direction: column;">'
            '<span style="font-size: 15px; color: %s;">%s</span><span style="font-size: 12px; color: %s;">%s</span></span>%s%s</div>'
            % (badge(i, c, ink, 36), INK if on else INK2, n, MUTED, k, amount(v, cur, INK2, 13), check() if on else ''))


ACCS = [('Galicia', 'Карта', 'GA', '#F26E22', '#1A0A00', '1 250 000', 'ARS', True), ('Тинькофф Накопительный Анастасии', 'Карта', 'ТИ', YELLOW, '#1A1400', '42 300', 'RUB', False),
        ('Binance', 'Крипто', 'BN', '#F3BA2F', '#1A1400', '2 140,55', 'USDT', False), ('Ameria', 'Карта', 'AM', '#2E6BFF', '#FFFFFF', '845 000', 'AMD', False),
        ('Наличные', 'Кошелёк', 'НА', LIME, ONLIME, '310 000', 'ARS', False), ('Mercado Pago', 'Кошелёк', 'MP', '#00B1EA', '#00141A', '98 450', 'ARS', False)]
CURS = [('ARS', 'Аргентинское песо', True), ('USD', 'Доллар США', False), ('USDT', 'Tether', False), ('AMD', 'Армянский драм', False), ('RUB', 'Российский рубль', False),
        ('EUR', 'Евро', False), ('BRL', 'Бразильский реал', False)]


def cur_row(c, n, on=False):
    return ('<div style="display: flex; align-items: center; gap: 12px; min-height: 52px;">%s<span style="flex-grow: 1; font-size: 15px; color: %s;">%s</span>%s</div>'
            % (badge(c[:2], CHIP, INK, 36), INK if on else INK2, n, check() if on else ''))


def acc_a():
    return other_head('Счёт', 'Найти счёт', [acc_row(*a) for a in ACCS])


def cur_a():
    return other_head('Валюта', 'Найти валюту или ввести код', [cur_row(*c) for c in CURS])


def acc_b():
    return other_head('Счёт', 'Найти счёт', [acc_row(*a) for a in ACCS[3:]], [acc_row(*a) for a in ACCS[:3]])


def cur_b():
    return other_head('Валюта', 'Найти валюту или ввести код', [cur_row(*c) for c in CURS[3:]], [cur_row(*c) for c in CURS[:3]])


def anatomy():
    def item(n, t):
        return ('<div style="padding: 10px 0; border-top: 1px solid %s;"><b style="font-size: 14px;">%s</b>'
                '<div style="font-size: 13px; line-height: 1.4; color: %s; margin-top: 2px;">%s</div></div>' % (DIV, n, INK2, t))
    return inner_screen('Общее для всех шторок', ''.join([
        item('Шапка', 'Заголовок слева, ✕ справа, подпись-счётчик под заголовком; отступ слева 20 dp — один на всех шторках.'),
        item('Поиск', 'Сразу под шапкой, тот же отступ; от 9 строк; пустой результат — «Ничего не нашли».'),
        item('Строка', 'Минимум 52 dp, значок слева, галочка у выбранного; длинное название — две строки, потом «…».'),
        item('Без дублей', 'Группа из одной категории — просто категория, без «Дети / Дети».'),
        item('Одна рамка', 'Категория, счёт, валюта, получатель, тег, период: один компонент `ChoiceSheet`, тексты — ресурсами.'),
    ]))


BOARDS = [  # name, title, fn, issue number
    ('Fb439CatA', 'А: «Категория» — группы с заголовками, иконки, без дублей', cat_list_a, 439),
    ('Fb439CatB', 'Б: «Категория» — группы сворачиваются, открыта группа выбранной', cat_list_b, 439),
    ('Fb439GridA', 'А: «Все категории» — сетка в 3 колонки, подпись в две строки', grid_a, 439),
    ('Fb439AccA', 'А: «Счёт» — одна рамка', acc_a, 439),
    ('Fb439CurA', 'А: «Валюта» — одна рамка', cur_a, 439),
    ('Fb439AccB', 'Б: «Счёт» — с «Недавними»', acc_b, 439),
    ('Fb439CurB', 'Б: «Валюта» — с «Недавними»', cur_b, 439),
    ('Fb439All', 'Общее: из чего состоит шторка выбора', anatomy, 439),
]
