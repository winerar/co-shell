"""Generated boards: variants for #129 (operation tags — coloured pills — and the payee from Wallet).

Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS). Long lists on purpose (owner 2026-11-11): 34 tags.
Tag colours are fixed (drawn as rgb(): mockup_theme.py repaints only #hex), like category colours.
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_screens_main import ACC, acc_chip, big_amount, cat_grid, chips_row, entry, keypad

FEEDBACK = [
    (129, 'Задача #129 · теги операций: цветные пилюли (+ получатель)',
     'Общее: тег = название + цвет, общий список семьи (синк), метки из Wallet становятся тегами. Шаблон помнит теги. Получатель из Wallet — '
     'в строке операции и в правке. 34 тега на досках. Выбрать: А — строка недавних тегов прямо в шторке (тап — вкл/выкл, «Все 34» — шторка с поиском); '
     'Б — один чип «Теги» в ряду с датой и заметкой, всё — в шторке с поиском. Рекомендую А: частые теги ставятся одним тапом, без лишнего окна.'),
]

TAG_ICON = 'M4 4h7l9 9-7 7-9-9zM8 8h.01'
TAGS = [  # name, colour (hex without #), uses
    ('Испанский', 'FF8A3D', 24), ('Аня', 'FF7AB8', 61), ('Миша', '3D9BFF', 48), ('отпуск', '2EE6C5', 19), ('Барилоче', '7CFF6B', 12),
    ('подарок', 'D36BFF', 9), ('ремонт', 'FFB547', 15), ('работа', '9AA5FF', 22), ('школа', 'FFE14D', 17), ('кот', 'FF9E7A', 14),
    ('врач', 'FF5C7F', 8), ('спорт', '5CFFB0', 21), ('доставка', '00D1FF', 27), ('гости', 'C79BFF', 6), ('машина', 'B8C2D6', 11),
    ('квартира', 'FFD43B', 13), ('кэшбэк', 'A8FF3E', 18), ('Ереван', 'FF6B6B', 7), ('Россия', '6BA8FF', 4), ('ДР', 'FF4FD8', 5),
    ('Новый год', '38D9A9', 3), ('книги', 'E0A96B', 9), ('кино', '9D7BFF', 10), ('одежда', 'FF8FA3', 12), ('техника', '7FD3FF', 6),
    ('на двоих', 'B8FF2E', 33), ('в долг', 'FFC46B', 4), ('вернут', '5CE1E6', 3), ('Mercado Libre', 'FFE600', 16), ('подписки', 'B266FF', 20),
    ('аптека', 'FF7A6B', 9), ('бассейн', '4DD4FF', 8), ('музыка', 'E6A3FF', 5), ('такси ночью', 'FFD166', 6),
]


def rgbh(h):
    return 'rgb(%d,%d,%d)' % tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def tag(name, on=True, small=False, x=False):
    col = next(c for n, c, _ in TAGS if n == name)
    h, fs = (22, 11) if small else (32, 13)
    if on:
        st = 'background: rgba(%s, 0.18); color: %s; border: 1px solid rgba(%s, 0.45);' % (rgbh(col)[4:-1], rgbh(col), rgbh(col)[4:-1])
    else:
        st = 'background: transparent; color: %s; border: 1px solid %s;' % (INK2, LINE2)
    d = 6 if small else 8
    dot = '<span style="width: %dpx; height: %dpx; border-radius: 50%%; background: %s;"></span>' % (d, d, rgbh(col))
    tail = svg('close', rgbh(col), 12) if x else ''
    return ('<span style="height: %dpx; padding: 0 %dpx; border-radius: 999px; %s font-size: %dpx; display: inline-flex; align-items: center; gap: 5px; '
            'white-space: nowrap; flex: none;">%s%s%s</span>' % (h, 7 if small else 11, st, fs, dot, name, tail))


def plus_tag(text='＋'):
    return ('<span style="height: 32px; padding: 0 11px; border-radius: 999px; border: 1px dashed %s; color: %s; font-size: 13px; display: inline-flex; '
            'align-items: center; flex: none; white-space: nowrap;">%s</span>' % (LINE2, LIME, text))


def tag_row(*items):
    return '<div style="display: flex; gap: 6px; padding: 0 4px; overflow: hidden;">%s</div>' % ''.join(items)


def head_expense():
    return ('<div style="display: flex; align-items: center; gap: 8px; padding: 0 4px;">%s<span style="flex-grow: 1;"></span>'
            '<span style="font-size: 13px; font-weight: 600; color: %s;">Расход</span></div>' % (acc_chip(ACC[0]), PINK))


def entry_a():
    tags = tag_row(tag('Испанский'), tag('Аня'), tag('на двоих', False), plus_tag('Все 34'))
    hint = '<div style="font-size: 12px; color: %s; padding: 0 4px; margin-top: -6px;">Недавние теги — тап включает, «Все 34» — поиск и новый тег</div>' % MUTED
    return entry(head_expense() + big_amount('18 000', 'ARS') + cat_grid('Подарки') + tags + hint
                 + chips_row(chip('Сегодня ▾'), chip('＋ Заметка'), chip('Получатель', False, 'user')) + keypad())


def entry_b():
    chip_tags = ('<span style="height: 36px; padding: 0 12px; border-radius: 999px; background: %s; color: %s; font-size: 13px; font-weight: 600; '
                 'display: inline-flex; align-items: center; gap: 6px;">%s Испанский, Аня</span>' % (VIOLETPILL, VIOLETINK, svg(TAG_ICON, VIOLETINK, 16)))
    return entry(head_expense() + big_amount('18 000', 'ARS') + cat_grid('Подарки')
                 + chips_row(chip('Сегодня ▾'), chip_tags, chip('＋ Заметка'), chip('Получатель', False, 'user')) + keypad())


def pick_sheet():
    def line(n, c, uses, on):
        mark = (('<span style="width: 22px; height: 22px; border-radius: 7px; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
                 % (LIME, svg('check', ONLIME, 16, 2.6))) if on else
                '<span style="width: 22px; height: 22px; border-radius: 7px; border: 2px solid %s; box-sizing: border-box;"></span>' % LINE2)
        return ('<div style="display: flex; align-items: center; gap: 12px; height: 44px; padding: 0 4px;"><span style="width: 12px; height: 12px; border-radius: 50%%; '
                'background: %s;"></span><span style="flex-grow: 1; font-size: 15px;">%s</span><span style="font-size: 12px; color: %s;">%d</span>%s</div>'
                % (rgbh(c), n, MUTED, uses, mark))
    chosen = tag_row(tag('Испанский', x=True), tag('Аня', x=True))
    recent = ''.join(line(n, c, u, n in ('Испанский', 'Аня')) for n, c, u in TAGS[:4])
    rest = ''.join(line(n, c, u, False) for n, c, u in sorted(TAGS[4:], key=lambda t: t[0].lower())[:9])
    inner = ('<div style="padding: 8px 16px 0;"><div style="display: flex; align-items: center; height: 48px;"><b style="flex-grow: 1; font-size: 20px;">Теги</b>'
             '<span style="color: %s; font-weight: 600;">Готово</span></div>%s<div style="margin-top: 10px;">%s</div>%s'
             '<div style="margin-top: 8px; display: flex; align-items: center; gap: 10px; height: 44px; padding: 0 4px; color: %s; font-size: 15px;">%sНовый тег — название и цвет</div>'
             '%s%s%s%s</div>' % (LIME, search('Найти или создать тег'), chosen, '', LIME, svg('plus', LIME, 18), label('Недавние', top=8), recent,
                                 label('Все · 34 · по алфавиту', top=10), rest))
    return scrim() + sheet(inner, 70)


OPS_A = [('Подарки', 'Galicia · Librería Ateneo', ('Испанский', 'Аня'), '−18 000', 'ARS', 'gift', '#FF8A5C', ''),
         ('Кафе', 'USDT-карта · Café Martínez', ('на двоих',), '−13,10', 'USDT', 'cafe', WARN, 'чек 13 300 ARS'),
         ('Продукты', 'Galicia · Coto', ('доставка', 'Миша', 'кот'), '−41 900', 'ARS', 'cart', LIME, ''),
         ('Такси', 'Наличные', (), '−6 500', 'ARS', 'taxi', YELLOW, ''),
         ('Отпуск', 'Mercado Pago · Despegar', ('отпуск', 'Барилоче'), '−412 000', 'ARS', 'spark', CYAN, ''),
         ('Зарплата', 'Ameria · ООО «Норк»', ('работа',), '+1 200', 'USD', 'salary', CYAN, '')]


def op_row(o, mode):
    title, sub, tags, amt, cur, icon, col, second = o
    kind = CYAN if amt.startswith('+') else PINK
    if mode == 'pills':
        shown = list(tags[:2])
        more = ('<span style="font-size: 11px; color: %s; align-self: center;">+%d</span>' % (MUTED, len(tags) - 2)) if len(tags) > 2 else ''
        extra = ('<span style="display: flex; gap: 4px; margin-top: 4px;">%s%s</span>' % (''.join(tag(t, small=True) for t in shown), more)) if tags else ''
        subline = '<span style="font-size: 13px; color: %s;">%s</span>' % (MUTED, sub)
    else:
        dots = ''.join('<span style="color: %s;">● </span><span style="color: %s;">%s</span> ' % (rgbh(next(c for n, c, _ in TAGS if n == t)), INK2, t) for t in tags[:2])
        more = ' +%d' % (len(tags) - 2) if len(tags) > 2 else ''
        subline = ('<span style="font-size: 13px; color: %s; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">%s%s%s</span>'
                   % (MUTED, sub, (' · ' + dots) if tags else '', more))
        extra = ''
    tail = ('<span style="display: flex; flex-direction: column; align-items: flex-end;">%s%s</span>'
            % (amount(amt, cur, kind), '<span style="%s font-size: 12px; color: %s;">%s</span>' % (MONO, MUTED, second) if second else ''))
    return ('<div style="display: flex; align-items: center; gap: 12px; min-height: 64px; padding: 6px 16px; box-sizing: border-box;">%s'
            '<span style="display: flex; flex-direction: column; flex-grow: 1; min-width: 0;"><span style="font-size: 16px;">%s</span>%s%s</span>%s</div>'
            % (dot_icon(icon, col), title, subline, extra, tail))


def ops_with(mode):
    chips = '<div style="display: flex; gap: 8px; margin-top: 12px; overflow: hidden;">%s</div>' % (
        chip('Расходы ▾') + chip('# Тег ▾') + chip('Ноябрь ▾') + chip('Счёт ▾'))
    rows = day('СЕГОДНЯ', '−31 100', 'ARS') + ''.join(op_row(o, mode) for o in OPS_A[:3]) + day('ВЧЕРА', '−418 500', 'ARS') + ''.join(op_row(o, mode) for o in OPS_A[3:])
    return tab_screen('Операции', search('Поиск: заметка, тег, получатель') + chips + '<div style="margin: 6px -16px 0;">%s</div>' % rows, 'ops')


def ops_a():
    return ops_with('pills')


def ops_b():
    return ops_with('dots')


def manage():
    def line(n, c, u):
        return ('<div style="display: flex; align-items: center; gap: 12px; height: 48px; padding: 0 14px; border-top: 1px solid %s;">'
                '<span style="width: 14px; height: 14px; border-radius: 50%%; background: %s;"></span><span style="flex-grow: 1; font-size: 15px;">%s</span>'
                '<span style="font-size: 12px; color: %s;">%d операций</span>%s</div>' % (DIV, rgbh(c), n, MUTED, u, chevron()))
    top = sorted(TAGS, key=lambda t: -t[2])
    body = (search('Найти тег') + label('Часто · 34 тега · общие для семьи')
            + card(''.join(line(*t) for t in top[:12]).replace('border-top: 1px solid %s;' % DIV, '', 1))
            + note('Тап — переименовать, цвет, объединить с другим, удалить (операции останутся без тега). Метки из Wallet уже здесь.'))
    return inner_screen('Теги', body, icon_button('plus', 'Новый тег', LIME))


BOARDS = [  # name, title, fn, issue number
    ('Fb129A', 'А: строка недавних тегов в шторке, «Все 34» — поиск (рекомендую)', entry_a, 129),
    ('Fb129AOps', 'А: в списке — пилюли под названием (до 2 + «+N»)', ops_a, 129),
    ('Fb129B', 'Б: один чип «Теги» рядом с датой и заметкой', entry_b, 129),
    ('Fb129BOps', 'Б: в списке — точки с названием в строке получателя', ops_b, 129),
    ('Fb129Pick', 'Общее: шторка тегов — поиск, недавние, все 34, создать', pick_sheet, 129),
    ('Fb129All', 'Общее: «Ещё → Теги» — список, цвет, объединить', manage, 129),
]
