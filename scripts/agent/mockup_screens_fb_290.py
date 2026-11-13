"""Generated boards: variants for #290 (create/edit without scrolling, entities clearly separated).

Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS). Worst case on purpose: receipt in another currency, tags,
note, 24 categories, 48 icons, 20 colours — and still no scroll on a 390×844 board (the phone of #290 has more room).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_screens_main import ACC, CATS, acc_chip, keypad

FEEDBACK = [
    (290, 'Отзыв #290 · создание и правка — без прокрутки, сущности явно разделены',
     'Выбрано 2026-11-13 (опрос): шторка А — один экран блоками: «Сумма» (с чеком), «Категория» (7 частых + «Все 24» — шторка по группам), '
     '«Детали» плитками 2×2 (дата, заметка, теги, «Это возврат» — галочкой, правка владельца), клавиатура внизу. Редакторы А — карточки-секции с подписью, '
     'иконки и цвета одной строкой + «Все 48» / «+11» (одна шторка «Иконка и цвет»). Так же — лимит, шаблон, курс. Ничего не листается.'),
]

MORE_CATS = CATS + [('Одежда', 'gift', '#FF8FA3'), ('Спорт', 'bolt', '#5CFFB0'), ('Кот', 'house', '#FF9E7A'), ('Школа', 'school', '#FFE14D'),
                    ('Транспорт', 'bus', '#00D1FF'), ('Подписки', 'film', '#B266FF'), ('Красота', 'spark', '#FF7AB8'), ('Техника', 'phone', '#7FD3FF'),
                    ('Книги', 'note', '#E0A96B'), ('Врач', 'pill', '#FF5C7F'), ('Ремонт', 'gear', '#FFB547'), ('Путешествия', 'spark', '#2EE6C5'),
                    ('Налоги', 'file', '#B8C2D6'), ('Прочее', 'more', '#9AA594'), ('Связь', 'wifi', '#C79BFF'), ('Дети', 'user', '#FFD43B')]
ICONS48 = ['cart', 'cafe', 'taxi', 'pill', 'house', 'phone', 'film', 'gift', 'school', 'bus', 'bolt', 'wifi', 'spark', 'salary', 'wallet', 'chart',
           'note', 'file', 'gear', 'user', 'mail', 'cloud', 'calendar', 'budget', 'rate', 'home', 'transfer', 'folder', 'lock', 'download',
           'upload', 'list', 'archive', 'info', 'alert', 'copy', 'refresh', 'search', 'check', 'plus', 'more', 'google', 'logout', 'trash',
           'close', 'back', 'chev', 'down']
COLORS20 = ['#B8FF2E', '#00F0FF', '#B266FF', '#FF4FD8', '#FFE600', '#FF8A3D', '#FF5C7F', '#5CFFB0', '#FF6B6B', '#E07A4F',
            '#FF8FA3', '#FFC46B', '#E0A96B', '#EADDB5', '#6BA8FF', '#8A93A6', '#38D9A9', '#9D7BFF', '#FFD43B', '#4DD4FF']


def block(title, inner, pad='10px 12px', right=''):
    """One entity = one card with its own caption (the core of #290)."""
    cap = ('<div style="display: flex; align-items: center; font-size: 11px; font-weight: 600; letter-spacing: 0.06em; text-transform: uppercase; '
           'color: %s; margin-bottom: 6px;"><span style="flex-grow: 1;">%s</span>%s</div>' % (MUTED, title, right))
    return ('<div style="border-radius: 18px; background: %s; border: 1px solid %s; padding: %s;">%s%s</div>' % (CARD, LINE, pad, cap, inner))


def small_link(text):
    return '<span style="font-size: 12px; font-weight: 600; color: %s; text-transform: none; letter-spacing: 0;">%s</span>' % (LIME, text)


def cat_cell(n, ic, col, on=False, size=34, fs=11):
    ring = 'box-shadow: 0 0 0 2px %s; ' % LIME if on else ''
    dot = ('<span style="width: %dpx; height: %dpx; border-radius: 50%%; background: %s; %sdisplay: flex; align-items: center; justify-content: center;">%s</span>'
           % (size, size, col, ring, svg(ic, '#0B0F0A', size * 0.55, 2)))
    return ('<span style="display: flex; flex-direction: column; align-items: center; gap: 3px; min-width: 0;">%s<span style="font-size: %dpx; color: %s; '
            'white-space: nowrap; overflow: hidden; max-width: 100%%; text-overflow: ellipsis;">%s</span></span>' % (dot, fs, INK if on else INK2, n))


def all_cell(text, size=34):
    return ('<span style="display: flex; flex-direction: column; align-items: center; gap: 3px;"><span style="width: %dpx; height: %dpx; border-radius: 50%%; '
            'border: 1px dashed %s; box-sizing: border-box; display: flex; align-items: center; justify-content: center; color: %s; font-size: 12px; font-weight: 700;">'
            '24</span><span style="font-size: 11px; color: %s;">%s</span></span>' % (size, size, LINE2, LIME, LIME, text))


def grid4(cells, gap='8px 2px', cols=4):
    return '<div style="display: grid; grid-template-columns: repeat(%d, minmax(0, 1fr)); gap: %s;">%s</div>' % (cols, gap, ''.join(cells))


def head(type_text='Расход', color=PINK, extra=''):
    return ('<div style="display: flex; align-items: center; gap: 8px;">%s<span style="flex-grow: 1;"></span>%s'
            '<span style="height: 36px; padding: 0 14px; border-radius: 999px; background: %s; color: %s; font-size: 13px; font-weight: 600; display: flex; align-items: center;">%s ▾</span></div>'
            % (acc_chip(ACC[3]), extra, PINKBG, color, type_text))


def amount_block(compact=False):
    big = ('<div style="display: flex; align-items: baseline; justify-content: center; gap: 8px;"><span style="%s font-size: %dpx; font-weight: 700; color: %s;">13,10</span>'
           '<span style="%s font-size: 16px; color: %s;">USDT</span></div>' % (MONO, 30 if compact else 40, PINK, MONO, MUTED))
    receipt = ('<div style="display: flex; align-items: center; gap: 8px; margin-top: 6px; padding-top: 8px; border-top: 1px dashed %s; font-size: 12px; color: %s;">'
               '<span>Чек</span><span style="%s font-size: 15px; color: %s; font-weight: 700;">13 300 ARS</span><span style="flex-grow: 1;"></span>'
               '<span>1 USDT = 1 015 ARS</span>%s</div>' % (LINE2, MUTED, MONO, INK, svg('close', MUTED, 14)))
    return block('Сумма · списано', big + receipt, right=small_link('Изменить') if compact else '')


def detail(icon, lbl, value, on=True):
    vcol = INK if on else MUTED2
    return ('<span style="display: flex; align-items: center; gap: 8px; height: 40px; padding: 0 10px; border-radius: 12px; background: %s; min-width: 0;">%s'
            '<span style="display: flex; flex-direction: column; min-width: 0;"><span style="font-size: 10px; color: %s;">%s</span>'
            '<span style="font-size: 13px; color: %s; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">%s</span></span></span>'
            % (BAR, svg(icon, MUTED, 16), MUTED, lbl, vcol, value))


def refund_check(on=False):
    """Owner 2026-11-13: refund as a plain checkbox with words, not a «Возврат: нет» field."""
    box = (('<span style="width: 20px; height: 20px; border-radius: 6px; background: %s; display: flex; align-items: center; justify-content: center; flex: none;">%s</span>'
            % (LIME, svg('check', ONLIME, 14, 2.6))) if on else
           '<span style="width: 20px; height: 20px; border-radius: 6px; border: 2px solid %s; box-sizing: border-box; flex: none;"></span>' % LINE2)
    return ('<span style="display: flex; align-items: center; gap: 8px; height: 40px; padding: 0 10px; border-radius: 12px; background: %s;">%s'
            '<span style="display: flex; flex-direction: column;"><span style="font-size: 13px;">Это возврат</span>'
            '<span style="font-size: 10px; color: %s;">вернули на счёт</span></span></span>' % (BAR, box, MUTED))


def details_block():
    cells = (detail('calendar', 'Дата', 'Сегодня, 13 нояб.') + detail('note', 'Заметка', 'Café Martínez, с Аней')
             + detail('spark', 'Теги · 2', '● Аня  ● на двоих') + refund_check())
    return block('Детали', '<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px;">%s</div>' % cells)


def frame(inner, top=40):
    return scrim() + sheet('<div style="position: absolute; left: 0; right: 0; top: 22px; bottom: 0; padding: 0 12px 14px; display: flex; flex-direction: column; '
                           'gap: 8px;">%s</div>' % inner, top)


def entry_a():
    cats = [cat_cell(n, ic, col, n == 'Кафе') for n, ic, col in CATS[:7]] + [all_cell('Все')]
    cat = block('Категория · частые', grid4(cats), right=small_link('Кафе ✓'))
    gap = '<div style="flex-grow: 1;"></div>'
    return frame(head() + amount_block() + cat + details_block() + gap + keypad())


def all_cats_sheet():
    groups = [('Еда', MORE_CATS[:4]), ('Дом и быт', MORE_CATS[4:12]), ('Личное', MORE_CATS[12:20]), ('Прочее', MORE_CATS[20:24])]
    body = ''
    for g, items in groups:
        body += label(g, top=10) + grid4([cat_cell(n, ic, col, n == 'Кафе', 32) for n, ic, col in items], '6px 2px')
    inner = ('<div style="padding: 6px 16px 0;"><div style="display: flex; align-items: center; height: 48px;"><b style="flex-grow: 1; font-size: 20px;">Все категории · 24</b>'
             '<span style="color: %s;">✕</span></div>%s%s</div>' % (MUTED, search('Найти категорию'), body))
    return scrim() + sheet(inner, 64)


# ---- editors -----------------------------------------------------------------------------------------------------

def top(title):
    return ('<div style="display: flex; align-items: center; gap: 4px; height: 60px; padding: 0 8px;">%s<h1 style="margin: 0; font-size: 22px; flex-grow: 1;">%s</h1>%s</div>'
            % (icon_button('close', 'Закрыть'), title, icon_button('archive', 'В архив')))


def page(title, inner):
    return (top(title) + '<div style="position: absolute; left: 16px; right: 16px; top: 60px; bottom: 20px; display: flex; flex-direction: column; gap: 8px;">%s'
            '<div style="flex-grow: 1;"></div>%s</div>' % (inner, button('Сохранить', mt=0)))


def name_line(name, icon, col, lbl='Название'):
    badge_ = ('<span style="width: 48px; height: 48px; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center; flex: none;">%s</span>'
              % (col, svg(icon, '#0B0F0A', 24, 2)))
    return ('<div style="display: flex; align-items: center; gap: 12px;">%s<span style="flex-grow: 1; display: flex; flex-direction: column; padding: 6px 12px; '
            'border-radius: 12px; background: %s; border: 1px solid %s;"><span style="font-size: 11px; color: %s;">%s</span><span style="font-size: 16px;">%s</span></span></div>'
            % (badge_, FIELD, LIME, MUTED, lbl, name))


def seg_small(options, sel):
    return segmented(options, sel, 'lime').replace('height: 40px', 'height: 34px')


def icon_row(sel='school'):
    cells = ''.join('<span style="height: 38px; border-radius: 12px; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
                    % (WARN if i == sel else BAR, svg(i, '#0B0F0A' if i == sel else INK2, 20)) for i in [x for x in ICONS48[4:12] if x != sel][:5] + [sel])
    more = ('<span style="height: 38px; border-radius: 12px; border: 1px dashed %s; color: %s; font-size: 12px; font-weight: 600; display: flex; '
            'align-items: center; justify-content: center;">Все 48</span>' % (LINE2, LIME))
    return '<div style="display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); gap: 6px;">%s%s</div>' % (cells, more)


def color_row(sel='#E07A4F', n=9):
    dots = ''.join('<span style="width: 28px; height: 28px; border-radius: 50%%; background: %s; %s"></span>'
                   % (c, 'box-shadow: 0 0 0 2px %s, 0 0 0 4px %s;' % (SHEET, INK) if c == sel else '') for c in COLORS20[:n])
    return ('<div style="display: flex; align-items: center; justify-content: space-between;">%s<span style="font-size: 12px; font-weight: 600; color: %s;">+11</span></div>'
            % (dots, LIME))


def cat_form_a():
    body = (name_line('Пекарня', 'school', '#E07A4F')
            + block('Что это', seg_small(['Категория', 'Папка'], 'Категория') + '<div style="height: 6px;"></div>' + seg_small(['Расход', 'Доход'], 'Расход'))
            + block('Папка', detail('folder', 'Внутри папки', 'Еда · 6 категорий'))
            + block('Иконка', icon_row(), right=small_link('Недавние'))
            + block('Цвет', color_row(), right=small_link('Похожий на соседей — нет')))
    return page('Категория', body)


def acc_form_a():
    hero = hero_card('<div style="display: flex; font-size: 12px; color: %s;"><span style="flex-grow: 1;">Галисия · карта</span><span>Карта</span></div>'
                     '<div style="margin-top: 10px; %s font-size: 28px; font-weight: 700; color: %s;">245 300,00 <span style="font-size: 14px;">ARS</span></div>'
                     % (HERO_HINT, MONO, LIME), pad='12px 14px')
    money = ('<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px;">%s%s</div>'
             % (detail('lock', 'Валюта', 'ARS · заблок.'), detail('wallet', 'Нач. баланс', '120 000,00')))
    types = ''.join(chip(t, t == 'Карта').replace('height: 36px', 'height: 32px') for t in ('Наличные', 'Карта', 'Вклад', 'Крипто', 'Другое'))
    sw = ('<div style="display: flex; align-items: center; gap: 10px;"><span style="flex-grow: 1; font-size: 14px;">Учитывать в общем балансе'
          '<span style="display: block; font-size: 11px; color: %s;">Выключите для долга или чужих денег</span></span>%s</div>' % (MUTED, switch(True)))
    body = (hero + name_line('Галисия · карта', 'wallet', '#F26E22')
            + block('Деньги', money) + block('Тип', '<div style="display: flex; flex-wrap: wrap; gap: 6px;">%s</div>' % types)
            + block('Цвет', color_row('#FF8A3D'), right=small_link('Как у Galicia')) + block('Баланс', sw))
    return page('Счёт', body)


def icons_sheet():
    cells = ''.join('<span style="height: 44px; border-radius: 12px; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
                    % (WARN if i == 'school' else BAR, svg(i, '#0B0F0A' if i == 'school' else INK2, 22)) for i in ICONS48)
    dots = ''.join('<span style="width: 30px; height: 30px; border-radius: 50%%; background: %s; justify-self: center; %s"></span>'
                   % (c, 'box-shadow: 0 0 0 2px %s, 0 0 0 4px %s;' % (SHEET, INK) if c == '#E07A4F' else '') for c in COLORS20)
    inner = ('<div style="padding: 6px 16px 0;"><div style="display: flex; align-items: center; height: 48px;"><b style="flex-grow: 1; font-size: 20px;">Иконка и цвет</b>'
             '<span style="color: %s; font-weight: 600;">Готово</span></div>%s%s'
             '<div style="display: grid; grid-template-columns: repeat(8, minmax(0, 1fr)); gap: 6px;">%s</div>%s'
             '<div style="display: grid; grid-template-columns: repeat(10, minmax(0, 1fr)); gap: 10px 0;">%s</div></div>'
             % (LIME, search('Найти иконку: хлеб, кот, такси'), label('Иконки · 48', top=10), cells, label('Цвета · 20', top=12), dots))
    return scrim() + sheet(inner, 150)


BOARDS = [  # name, title, fn, issue number
    ('Fb290A', 'Шторка А (выбрано): всё на одном экране блоками — сумма, категория (8 частых), детали, клавиатура', entry_a, 290),
    ('Fb290ACats', 'Шторка А: «Все 24» — шторка категорий по группам, без прокрутки', all_cats_sheet, 290),
    ('Fb290FA', 'Редакторы А (выбрано): карточки-секции, иконки и цвета одной строкой + «Все»', cat_form_a, 290),
    ('Fb290FAAcc', 'Редакторы А: «Счёт»', acc_form_a, 290),
    ('Fb290Icons', 'Общее: «Все 48» — иконки и цвета одной шторкой, без прокрутки', icons_sheet, 290),
]
