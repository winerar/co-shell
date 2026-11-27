#!/usr/bin/env python3
"""Poll boards for #785 «Умная аналитика без ИИ» (#786–#792): one question per feature, two variants each.
Usage: python3 scripts/agent/mockup_poll_smart785.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py docs/design/poll-smart-785.json <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import mockup_poll_f_common as C  # noqa: E402

M = C.MONO
CAFE, CART, TAXI, HOME, FUN = '#FFB86B', '#7FD6A0', '#FFD84D', '#8EC5FF', '#E59BFF'


def screen(t, title, body, cap, active='chart', back=False, right=''):
    return C.topbar(t, title, back=back, right=right) + '<div style="padding-top:72px">' + body + '</div>' + (C.nav(t, active) if active else '') + C.caption(t, cap, 0)


def small(t, text, extra=''):
    return '<div style="font-size:13px;color:%s;margin-top:4px;line-height:1.35;%s">%s</div>' % (t['muted'], extra, text)


def amt(v, cur, size=15, extra=''):
    return '<span style="%sfont-size:%dpx;%s">%s <span style="font-size:%dpx">%s</span></span>' % (M, size, extra, v, max(11, size - 4), cur)


def line(t, left, right, strong=False):
    return ('<div style="display:flex;justify-content:space-between;align-items:baseline;gap:12px;font-size:15px;margin-top:10px;%s"><span>%s</span>%s</div>'
            % ('font-weight:700' if strong else '', left, right))


def period(t, label='Ноябрь 2026'):
    return ('<div style="margin:4px 16px 0;display:flex;align-items:center;justify-content:space-between;font-size:16px;font-weight:600">%s<span>%s</span>%s</div>'
            % (C.glyph(C.CHEV_L, t['muted'], 20), label, C.glyph(C.CHEV_R, t['muted'], 20)))


def chip(t, text, color=None):
    c = color or t['prim']
    return ('<span style="height:22px;padding:0 8px;border-radius:999px;display:inline-flex;align-items:center;gap:5px;font-size:12px;background:%s;color:%s">'
            '<span style="width:6px;height:6px;border-radius:50%%;background:%s"></span>%s</span>' % (C.mix(t['page'], c, .18), t['ink'], c, text))


def coach(t, text, action, second='Не сейчас'):
    return C.card(t, '', '', '<div style="display:flex;gap:10px"><span>%s</span><div style="font-size:15px;line-height:1.4">%s</div></div>'
                  '<div style="display:flex;justify-content:flex-end;gap:20px;margin-top:10px;font-size:15px;font-weight:700;color:%s"><span style="color:%s;font-weight:500">%s</span><span>%s</span></div>'
                  % (C.glyph(C.B.ICON['bulb'], C.ink2(t), 22), text, C.ink2(t), t['muted'], second, action))


def tx(t, icon, color, title, sub, right, mark=''):
    r = C.row(t, icon, color, title, sub, right)
    return r.replace('</div></div><div style="text-align:right">', '</div>%s</div><div style="text-align:right">' % (('<div style="margin-top:4px">%s</div>' % mark) if mark else ''), 1) if mark else r


def day(t, label):
    return '<div style="margin:14px 16px 2px;font-size:13px;font-weight:600;letter-spacing:.04em;color:%s">%s</div>' % (C.ink2(t), label)


# ---------- #786 conversion cost ----------
def conv_card(t):
    rows = (line(t, 'Покупки в ARS с карты USDT', amt('1 284 000', 'ARS'))
            + line(t, 'Списано', amt('1 288,40', 'USDT'))
            + line(t, 'По официальному курсу', amt('1 265,10', 'USD'))
            + '<div style="border-top:1px solid %s;margin-top:10px"></div>' % t['line']
            + line(t, 'Конвертация обошлась', amt('23,30', 'USD', 17), True)
            + small(t, 'средний курс 1 USDT = 997 ARS · официальный 1 015 · хуже на 1,8 %'))
    by = (day(t, 'ПО КАРТАМ')
          + '<div style="margin:0 16px">' + line(t, 'USDT · Lemon', amt('−1,8 %', '', 14)) + line(t, 'USD · Galicia', amt('−3,9 %', '', 14)) + '</div>')
    return screen(t, 'Отчёты', period(t) + C.card(t, 'Оплата в другой валюте', 'Подробнее ›', rows) + by,
                  'А: карточка в «Отчётах»: сколько стоила конвертация за месяц и какая карта выгоднее. Нажатие — список покупок с курсом каждой.')


def conv_row(t):
    sheet = (line(t, 'Сумма покупки', amt('48 500', 'ARS')) + line(t, 'Списано', amt('49,20', 'USDT'))
             + line(t, 'Курс покупки', amt('1 USDT = 986', 'ARS', 14)) + line(t, 'Официальный в этот день', amt('1 015', 'ARS', 14))
             + '<div style="margin-top:12px">%s</div>' % chip(t, 'курс хуже на 2,9 % · 1,40 USD', '#FFB86B')
             + small(t, 'Итог за месяц — в «Итогах месяца»: «Конвертация обошлась в 23 USD».', 'margin-top:12px'))
    page = C.topbar(t, 'Операции', back=False) + '<div style="padding-top:72px">' + day(t, 'СЕГОДНЯ') + tx(t, 'cart', CART, 'Продукты', 'Lemon · 48 500 ARS', '−49,20 USDT') + '</div>'
    return page + C.sheet(t, 'Продукты', sheet, 250, sub='сегодня · Lemon') + C.caption(t, 'Б: у каждой покупки в другой валюте — метка «курс хуже на 2,9 %», итог за месяц — одной строкой в «Итогах месяца». Без своей карточки.', 0)


# ---------- #787 unusual spending ----------
def bar_row(t, icon, color, name, value, frac, mark='', ghost=None):
    g = ('<div style="position:absolute;left:%d%%;top:-3px;width:2px;height:14px;background:%s"></div>' % (round(ghost * 100), t['muted'])) if ghost else ''
    return ('<div style="display:flex;gap:12px;align-items:center;padding:8px 0">%s<div style="flex:1;min-width:0"><div style="display:flex;justify-content:space-between;align-items:center;gap:8px">'
            '<span style="font-size:15px">%s</span>%s</div><div style="display:flex;justify-content:flex-end;margin-top:2px">%s</div><div style="position:relative;margin-top:6px">%s%s</div></div></div>'
            % (C.badge(t, icon, color, 36), name, amt(value, 'ARS', 14), mark, C.meter(t, frac, color), g))


def unusual_card(t):
    rows = (bar_row(t, 'cafe', CAFE, 'Кафе', '182 000', .9) .replace('<div style="display:flex;justify-content:flex-end;margin-top:2px"></div>', small(t, 'примерно вдвое больше обычного (≈ 90 000)'))
            + bar_row(t, 'taxi', TAXI, 'Такси', '41 000', .3).replace('<div style="display:flex;justify-content:flex-end;margin-top:2px"></div>', small(t, 'меньше обычного на 30 % — хорошо идёте')))
    body = period(t) + C.card(t, 'Необычное в ноябре', '', rows + small(t, '«Обычно» — среднее за полгода, по курсу дня операции.', 'margin-top:8px'))
    return screen(t, 'Отчёты', body + C.card(t, 'На что ушли деньги', 'Все ›', small(t, 'Продукты, Дом, Кафе…')),
                  'А: своя карточка «Необычное» в «Отчётах» (до 3 строк) и тот же слайд в «Итогах месяца». Нет заметного — карточки нет.')


def unusual_marks(t):
    cats = (bar_row(t, 'cart', CART, 'Продукты', '420 000', .95, ghost=.9)
            + bar_row(t, 'cafe', CAFE, 'Кафе', '182 000', .42, chip(t, 'вдвое больше обычного', '#FFB86B'), .2)
            + bar_row(t, 'house', HOME, 'Дом', '150 000', .35, ghost=.36)
            + bar_row(t, 'taxi', TAXI, 'Такси', '41 000', .1, chip(t, 'меньше обычного', '#7FD6A0'), .14))
    return screen(t, 'Отчёты', period(t) + C.card(t, 'На что ушли деньги', 'Все ›', cats),
                  'Б: без новой карточки — метка прямо в строке категории; серая черта — «обычно». Тише, но легко не заметить.')


# ---------- #788 personal inflation ----------
def chart(t, a, b):
    w, h = 326, 130
    pts = lambda vs: ' '.join('%d,%d' % (round(i * w / (len(vs) - 1)), round(h - (v - 80) * h / 100)) for i, v in enumerate(vs))  # noqa: E731
    grid = '<line x1="0" y1="%d" x2="%d" y2="%d" stroke="%s" stroke-dasharray="3 4"/>' % (h - 20 * h // 100, w, h - 20 * h // 100, t['line'])
    return ('<svg width="%d" height="%d" style="display:block;margin-top:12px;overflow:visible">%s<polyline points="%s" fill="none" stroke="%s" stroke-width="3"/>'
            '<polyline points="%s" fill="none" stroke="%s" stroke-width="3" stroke-dasharray="1 0"/></svg>' % (w, h, grid, pts(a), t['prim'], pts(b), C.ink2(t)))


def legend(t):
    dot = lambda c, s: '<span style="display:inline-flex;align-items:center;gap:6px"><span style="width:12px;height:4px;border-radius:2px;background:%s"></span>%s</span>' % (c, s)  # noqa: E731
    return '<div style="display:flex;gap:16px;font-size:13px;color:%s;margin-top:8px">%s%s<span style="margin-left:auto">июн → ноя</span></div>' % (t['muted'], dot(t['prim'], 'в песо'), dot(C.ink2(t), 'в долларах'))


def infl_chart(t):
    head = ('<div style="font-size:15px;line-height:1.4">В песо траты выросли на <b>38 %</b>, в долларах — на <b>4 %</b>.</div>'
            + small(t, 'Почти весь рост — цены и курс, а не новые траты.'))
    body = period(t, 'Июнь — ноябрь 2026') + C.card(t, 'Траты в песо и в долларах', '', head + chart(t, [100, 106, 113, 121, 129, 138], [100, 99, 102, 101, 103, 104]) + legend(t))
    cats = C.card(t, 'Быстрее всего росли', '', line(t, 'Кафе', amt('+11 %', 'USD', 14)) + line(t, 'Продукты', amt('+6 %', 'USD', 14)) + line(t, 'Такси', amt('−8 %', 'USD', 14)))
    return screen(t, 'Отчёты', body + cats, 'А: график «в песо и в долларах» (начало = 100) и вывод одной фразой; ниже — категории, что росли быстрее всего в долларах.')


def infl_table(t):
    head = '<div style="display:grid;grid-template-columns:1fr 70px 80px;font-size:12px;color:%s;margin-top:4px"><span></span><span style="text-align:right">в ARS</span><span style="text-align:right">в USD</span></div>' % t['muted']
    r = lambda n, a, b: '<div style="display:grid;grid-template-columns:1fr 70px 80px;font-size:15px;padding:9px 0;border-top:1px solid %s"><span>%s</span><span style="%stext-align:right">%s</span><span style="%stext-align:right">%s</span></div>' % (t['line'], n, M, a, M, b)  # noqa: E731
    rows = r('<b>Все траты</b>', '+38 %', '+4 %') + r('Кафе', '+47 %', '+11 %') + r('Продукты', '+41 %', '+6 %') + r('Дом', '+35 %', '+2 %') + r('Такси', '+22 %', '−8 %') + r('Развлечения', '+30 %', '−2 %')
    body = period(t, 'Июнь — ноябрь 2026') + C.segmented(t, ['6 месяцев', '12 месяцев'], 0) + C.card(t, 'Рост трат', '', head + rows + small(t, 'Сравниваем средний месяц первых трёх и последних трёх месяцев.', 'margin-top:8px'))
    return screen(t, 'Отчёты', body, 'Б: таблица «категория · в песо · в долларах» без графика. Точнее, но читать дольше.')


# ---------- #789 recurring detection ----------
def ops_page(t, extra):
    rows = (day(t, 'СЕГОДНЯ') + tx(t, 'wifi', HOME, 'Интернет', 'Galicia', '−25 000 ARS') + tx(t, 'cafe', CAFE, 'Кафе', 'Lemon', '−2 450 ARS')
            + day(t, 'ВЧЕРА') + tx(t, 'cart', CART, 'Продукты', 'Lemon', '−18 300 ARS'))
    return C.topbar(t, 'Операции', back=False) + '<div style="padding-top:72px">' + extra + rows + '</div>' + C.nav(t, 'list')


def rec_coach(t):
    card = coach(t, 'Похоже на ежемесячный платёж: <b>Интернет</b>, ~25 000 ARS, около 5-го числа. Сделать регулярным? Приложение напомнит и покажет его в «Скоро».', 'Сделать регулярным')
    return ops_page(t, card) + C.caption(t, 'А: карточкой вверху «Операций», один раз на платёж: «Сделать регулярным» открывает готовый шаблон с повтором. «Не сейчас» — больше не спросит.', 0)


def rec_section(t):
    found = (day(t, 'ПОХОЖЕ НА РЕГУЛЯРНЫЕ')
             + C.row(t, 'wifi', HOME, 'Интернет', 'каждый месяц, около 5-го', '~25 000 ARS').replace('</div></div></div>', '</div><div style="font-size:13px;font-weight:700;color:%s">Добавить</div></div></div>' % C.ink2(t), 1)
             + C.row(t, 'film', FUN, 'Кино-подписка', 'каждый месяц, около 12-го', '~9,99 USD').replace('</div></div></div>', '</div><div style="font-size:13px;font-weight:700;color:%s">Добавить</div></div></div>' % C.ink2(t), 1))
    mine = day(t, 'МОИ') + C.row(t, 'house', HOME, 'Аренда', 'каждый месяц, 1-го', '650 USD')
    return screen(t, 'Регулярные', found + mine, 'Б: найденное — отдельной группой на экране «Ещё → Регулярные», «Добавить» в одно нажатие. Спокойно, но туда заходят редко.', active=None, back=True)


# ---------- #790 suggested limit ----------
def limit_sheet(t, prefilled):
    field = ('<div style="margin-top:12px;height:56px;border-radius:16px;background:%s;box-shadow:inset 0 0 0 1px %s;display:flex;align-items:center;justify-content:space-between;padding:0 16px">'
             '<span style="font-size:13px;color:%s">Лимит в месяц</span>%s</div>' % (t['wash'], t['line'], t['muted'], amt('420 000' if prefilled else '0', 'ARS', 22, '' if prefilled else 'color:%s' % t['muted'])))
    if prefilled:
        hint = small(t, 'Подставили, сколько обычно уходит за месяц (среднее за 3 месяца). Можно поменять.')
    else:
        hint = ('<div style="display:flex;align-items:center;justify-content:space-between;gap:8px;margin-top:8px"><span style="font-size:13px;color:%s">Обычно уходит ~420 000 ARS в месяц</span>%s</div>'
                % (t['muted'], C.pill(t, 'Подставить', h=36)))
    body = ('<div style="display:flex;align-items:center;gap:12px;margin-top:14px">%s<span style="font-size:16px">Продукты</span></div>' % C.badge(t, 'cart', CART) + field + hint
            + small(t, 'Лимит — ориентир, а не запрет.', 'margin-top:14px') + '<div style="position:absolute;left:16px;right:16px;bottom:24px">%s</div>' % C.button(t, 'Сохранить'))
    page = C.topbar(t, 'Бюджеты') + '<div style="padding-top:72px"></div>'
    cap = ('Б: цифра уже стоит в поле, под ним — откуда она. Быстрее, но легко сохранить, не подумав.' if prefilled
           else 'А: поле пустое, под ним «Обычно уходит ~420 000 ARS» и пилюля «Подставить». Человек решает сам.')
    return page + C.sheet(t, 'Новый лимит', body, 330, sub='с ноября, каждый месяц') + C.caption(t, cap, 0)


# ---------- #791 duplicates ----------
def dup_mark(t):
    mark = chip(t, 'похоже на дубль', '#FFB86B')
    rows = (day(t, 'СЕГОДНЯ') + tx(t, 'cart', CART, 'Продукты', 'Lemon · 14:02 · Аня', '−18 300 ARS', mark) + tx(t, 'cart', CART, 'Продукты', 'Lemon · 14:05 · Вы', '−18 300 ARS', mark)
            + tx(t, 'cafe', CAFE, 'Кафе', 'Galicia · 10:40', '−2 450 ARS'))
    sheet = (small(t, 'Две одинаковые траты с разницей в 3 минуты. Если это одна покупка, лишнюю можно удалить.')
             + '<div style="display:flex;flex-direction:column;gap:8px;margin-top:16px">%s<div style="height:48px;border-radius:999px;display:flex;align-items:center;justify-content:center;'
             'font-size:16px;box-shadow:inset 0 0 0 1px %s;color:%s">Это разные</div></div>' % (C.button(t, 'Удалить вторую'), t['line'], C.ink2(t)))
    page = C.topbar(t, 'Операции', back=False) + '<div style="padding-top:72px">' + rows + '</div>'
    return page + C.sheet(t, 'Похоже на дубль', sheet, 390) + C.caption(t, 'А: метка у обеих строк в «Операциях», нажатие — шторка «Удалить вторую · Это разные». Удаление — со снэкбаром «Отменить».', 0)


def dup_save(t):
    q = ('<div style="position:absolute;inset:0;background:rgba(0,0,0,.45);z-index:5"></div><div style="position:absolute;left:24px;right:24px;top:230px;z-index:6;border-radius:28px;background:%s;border:1px solid %s;padding:20px">'
         '<b style="font-size:20px">Такая операция уже есть</b>%s<div style="margin-top:12px">%s</div>'
         '<div style="display:flex;gap:8px;margin-top:18px"><div style="flex:1;height:48px;border-radius:999px;display:flex;align-items:center;justify-content:center;box-shadow:inset 0 0 0 1px %s;color:%s">Не сохранять</div>'
         '<div style="flex:1">%s</div></div></div>' % (t['page'], t['line'], small(t, 'Аня записала её 3 минуты назад.'), tx(t, 'cart', CART, 'Продукты', 'Lemon · 14:02', '−18 300 ARS').replace('padding:8px 16px', 'padding:4px 0'),
                                                       t['line'], C.ink2(t), C.button(t, 'Всё равно')))
    return C.topbar(t, 'Операции', back=False) + '<div style="padding-top:72px">' + day(t, 'СЕГОДНЯ') + tx(t, 'cart', CART, 'Продукты', 'Lemon · 14:02 · Аня', '−18 300 ARS') + '</div>' + q + \
        C.caption(t, 'Б: вопрос сразу при сохранении. Дубль не появится, но ловит только то, что уже пришло синхронизацией — с двух телефонов одновременно не поможет.', 0)


# ---------- #792 goal what-if ----------
def goal_card(t, extra=''):
    body = ('<div style="display:flex;gap:12px;align-items:center">%s<div style="flex:1"><div style="font-size:16px;font-weight:600">Отпуск</div>%s</div>%s</div>'
            '<div style="margin-top:10px">%s</div>%s' % (C.badge(t, 'flag', '#8EC5FF'), small(t, 'к июлю 2027 · 160 USD в месяц', 'margin-top:0'), amt('1 240 / 3 000', 'USD', 14), C.meter(t, .41), extra))
    return C.card(t, '', '', body)


def whatif_line(t):
    tip = ('<div style="margin-top:12px;padding:10px 12px;border-radius:16px;background:%s;font-size:14px;line-height:1.4">Если тратить на <b>кафе</b> на 20 %% меньше, накопите '
           'на <b>2 месяца раньше</b> — к маю. <span style="color:%s;font-weight:700">Что если ›</span></div>' % (t['wash'], C.ink2(t)))
    return screen(t, 'Цели', goal_card(t, tip) + goal_card(t).replace('Отпуск', 'Ноутбук'), 'А: одна строка прямо в карточке цели (самая большая гибкая категория), «Что если ›» — шторка, где можно выбрать другую.', active=None, back=True)


def whatif_sheet(t):
    cats = '<div style="display:flex;gap:8px;flex-wrap:wrap;margin-top:8px">%s%s%s%s</div>' % (C.pill(t, 'Кафе', True), C.pill(t, 'Такси'), C.pill(t, 'Развлечения'), C.pill(t, 'Покупки'))
    body = (small(t, 'На какой категории сэкономить', 'margin-top:14px') + cats + small(t, 'Насколько меньше тратить', 'margin-top:14px')
            + C.segmented(t, ['10 %', '20 %', '30 %'], 1).replace('margin:12px 16px 0', 'margin:8px 0 0')
            + '<div style="margin-top:16px;padding:14px;border-radius:20px;background:%s">' % C.mix(t['page'], t['prim'], .14)
            + line(t, 'Сэкономите в месяц', amt('≈ 18 000', 'ARS')) + line(t, 'Сейчас', '<span>июль 2027</span>') + line(t, 'С экономией', '<b>май 2027</b>', True) + '</div>'
            + small(t, 'Считаем по среднему за 3 месяца. Это прикидка, траты не меняются.', 'margin-top:12px'))
    return C.topbar(t, 'Цели') + '<div style="padding-top:72px">' + goal_card(t) + '</div>' + C.sheet(t, 'Что если', body, 230, sub='Отпуск · 3 000 USD') + \
        C.caption(t, 'Шторка «Что если» (общая для обоих вариантов): категория, доля, новая дата. Ничего не сохраняет.', 0)


def whatif_button(t):
    btn = '<div style="display:flex;justify-content:flex-end;margin-top:10px;font-size:14px;font-weight:700;color:%s">Что если…</div>' % C.ink2(t)
    return screen(t, 'Цели', goal_card(t, btn) + goal_card(t).replace('Отпуск', 'Ноутбук'), 'Б: в карточке только ссылка «Что если…», расчёт — в шторке по нажатию. Тише, но идею никто не увидит сам.', active=None, back=True)


W, H = 390, 820
BOARDS = [(n, f, W, H) for n, f in [
    ('Smart786Card', conv_card), ('Smart786Row', conv_row),
    ('Smart787Card', unusual_card), ('Smart787Marks', unusual_marks),
    ('Smart788Chart', infl_chart), ('Smart788Table', infl_table),
    ('Smart789Coach', rec_coach), ('Smart789Section', rec_section),
    ('Smart790Hint', lambda t: limit_sheet(t, False)), ('Smart790Prefill', lambda t: limit_sheet(t, True)),
    ('Smart791Mark', dup_mark), ('Smart791Save', dup_save),
    ('Smart792Line', whatif_line), ('Smart792Button', whatif_button), ('Smart792Sheet', whatif_sheet)]]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
