#!/usr/bin/env python3
"""Poll boards for #826 «Отчёты»: order and hiding of the feed cards, then custom reports («свои отчёты»).
Usage: python3 scripts/agent/mockup_poll_reports826.py <out-dir>  → <out-dir>/img/*.png, then
poll-page.py docs/design/poll-reports-826.json <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import mockup_poll_f_common as C  # noqa: E402

B = C.B
M = C.MONO
CAFE, CART, TAXI, HOME, FUN, SCHOOL = '#FFB86B', '#7FD6A0', '#FFD84D', '#8EC5FF', '#E59BFF', '#FF8FA3'

# The feed of «Отчёты» as the code draws it today (ReportsItems.overviewItems), one line of what each card shows.
CARDS = [('На что ушли деньги', 'категории за период'), ('Необычное за ноябрь', 'что выбилось из обычного'),
         ('Оплата в другой валюте', 'цена конвертации'), ('По месяцам', 'столбики за полгода'),
         ('Траты в ARS и в USD', 'личная инфляция'), ('Быстрее всего подорожало', 'категории с ростом'),
         ('Доходы и расходы', 'по месяцам'), ('Капитал', 'все счета по месяцам'), ('Подушка', 'на сколько месяцев хватит'),
         ('Прогноз остатка', '30 дней вперёд'), ('Постоянные траты', 'регулярные платежи'), ('Кто сколько потратил', 'участники семьи')]


def screen(t, title, body, cap, right='', back=False):
    return C.topbar(t, title, back=back, right=right) + '<div style="padding-top:72px">' + body + '</div>' + C.nav(t, 'chart') + C.caption(t, cap, 0)


def small(t, text, extra=''):
    return '<div style="font-size:13px;color:%s;margin-top:4px;line-height:1.35;%s">%s</div>' % (t['muted'], extra, text)


def amt(v, cur, size=15, extra=''):
    return '<span style="%sfont-size:%dpx;%s">%s <span style="font-size:%dpx;opacity:.7">%s</span></span>' % (M, size, extra, v, max(11, size - 4), cur)


def period(t, label='Ноябрь 2026 · USD'):
    return ('<div style="margin:4px 16px 0;display:flex;align-items:center;justify-content:space-between;font-size:16px;font-weight:600">%s<span>%s</span>%s</div>'
            % (C.glyph(C.CHEV_L, t['muted'], 20), label, C.glyph(C.CHEV_R, t['muted'], 20)))


def tabs(t):
    return C.segmented(t, ['Обзор', 'Календарь'], 0)


def icon_btn(t, icon, label, bg=True):
    return ('<span aria-label="%s" style="width:40px;height:40px;border-radius:50%%;%sdisplay:inline-flex;align-items:center;justify-content:center;flex:none">%s</span>'
            % (label, ('background:%s;' % t['wash']) if bg else '', B.svg(B.ICON[icon], t['ink'], 20, 1.8)))


def bar_row(t, color, name, value, frac):
    return ('<div style="margin-top:10px"><div style="display:flex;justify-content:space-between;font-size:14px"><span>%s</span>%s</div>'
            '<div style="margin-top:4px">%s</div></div>' % (name, amt(value, 'USD', 14), C.meter(t, frac, color)))


def spending(t):
    body = (bar_row(t, HOME, 'Дом', '290', .9) + bar_row(t, CART, 'Продукты', '121', .42) + bar_row(t, SCHOOL, 'Образование', '120', .41)
            + bar_row(t, CAFE, 'Кафе', '89', .3))
    return C.card(t, 'На что ушли деньги', 'Все ›', body)


def months(t):
    cols = ''.join('<div style="flex:1;display:flex;flex-direction:column;align-items:center;gap:4px"><div style="width:22px;height:%dpx;border-radius:8px;background:%s"></div>'
                   '<span style="font-size:11px;color:%s">%s</span></div>' % (h, t['prim'] if i == 5 else C.mix(t['page'], t['prim'], .35), t['muted'], m)
                   for i, (h, m) in enumerate([(50, 'июн'), (62, 'июл'), (44, 'авг'), (70, 'сен'), (58, 'окт'), (76, 'ноя')]))
    return C.card(t, 'По месяцам', 'ср. 712 USD', '<div style="display:flex;align-items:flex-end;height:100px;margin-top:10px">%s</div>' % cols)


def handle_row(t, title, sub, hidden=False, first=False):
    lead = B.svg(B.ICON['drag'], t['muted'], 22, 2.4) if not hidden else '<span style="width:22px"></span>'
    trail = icon_btn(t, 'plus', 'Показать') if hidden else icon_btn(t, 'close', 'Скрыть')
    return ('<div style="display:flex;align-items:center;gap:12px;min-height:56px;padding:6px 8px 6px 12px;%s">%s'
            '<div style="flex:1;min-width:0;opacity:%s"><div style="font-size:15px">%s</div><div style="font-size:12px;color:%s">%s</div></div>%s</div>'
            % ('' if first else 'border-top:1px solid %s;' % C.mix(t['surf'], t['line'], .55), lead, '.5' if hidden else '1', title, t['muted'], sub, trail))


def group(t, rows):
    return '<div style="margin:8px 16px 0;border-radius:24px;background:%s;border:1px solid %s;overflow:hidden">%s</div>' % (t['surf'], t['line'], rows)


def label(t, text, top=14):
    return '<div style="margin:%dpx 20px 0;font-size:13px;font-weight:600;letter-spacing:.04em;color:%s">%s</div>' % (top, C.ink2(t), text)


def done(t):
    return '<div style="position:absolute;left:16px;right:16px;bottom:16px;z-index:4">%s</div>' % C.button(t, B.svg(B.ICON['check'], t['onp'], 20, 2.2) + '&nbsp;Готово')


# ---------- Q1: order and hiding ----------
def order_compact(t):
    hidden = {'Оплата в другой валюте', 'Кто сколько потратил'}
    shown = [c for c in CARDS if c[0] not in hidden]
    rows = ''.join(handle_row(t, n, s, first=i == 0) for i, (n, s) in enumerate(shown[:8]))
    rest = ''.join(handle_row(t, n, s, hidden=True, first=i == 0) for i, (n, s) in enumerate(c for c in CARDS if c[0] in hidden))
    body = (small(t, 'Тяните ⠿, чтобы переставить. ✕ — скрыть.', 'margin:4px 20px 0;font-size:14px') + group(t, rows)
            + small(t, '+ ещё 2 ниже', 'margin:6px 20px 0;text-align:center') + label(t, 'СКРЫТЫ') + group(t, rest))
    return C.topbar(t, 'Отчёты', back=False) + '<div style="padding-top:72px">' + body + '</div>' + done(t) + \
        C.caption(t, 'А: удержали карточку — лента сворачивается в список названий: все 12 видны почти без прокрутки, тянуть легко. '
                     'Скрытые — внизу с «+». «Готово» — карточки разворачиваются.', 0).replace('bottom:84px', 'bottom:76px')


def order_home(t):
    def head(title):
        return ('<div style="display:flex;align-items:center;gap:8px;margin:12px 20px 4px"><span style="flex:1;font-size:13px;font-weight:600;color:%s">%s</span>%s%s</div>'
                % (t['muted'], title, icon_btn(t, 'drag', 'Тянуть'), icon_btn(t, 'close', 'Скрыть')))

    def dashed(inner):
        return '<div style="margin:0 12px;border-radius:28px;border:1.5px dashed %s;padding:2px 0 10px">%s</div>' % (t['line'], inner.replace('margin:12px 16px 0', 'margin:8px 4px 0'))
    hidden_bar = ('<div style="margin:12px 16px 0;padding:12px 8px 12px 16px;border-radius:24px;border:1px solid %s;display:flex;align-items:center">'
                  '<span style="flex:1;font-size:15px">Скрыто: 2</span><span style="padding:0 12px;font-size:14px;font-weight:700;color:%s">Вернуть</span></div>' % (t['line'], C.ink2(t)))
    body = (small(t, 'Тяните ⠿, чтобы переставить. ✕ — скрыть.', 'margin:4px 20px 0;font-size:14px') + head('На что ушли деньги') + dashed(spending(t))
            + head('По месяцам') + dashed(months(t)) + hidden_bar)
    return C.topbar(t, 'Отчёты', back=False) + '<div style="padding-top:72px">' + body + '</div>' + done(t) + \
        C.caption(t, 'Б: точно как на «Обзоре» — карточки остаются целыми, над каждой ⠿ и ✕, внизу «Скрыто: 2 · Вернуть». '
                     'Привычно, но 12 карточек — долгая прокрутка и тянуть далеко.', 0).replace('bottom:84px', 'bottom:76px')


def order_sheet(t):
    tune = '<span style="width:48px;display:flex;justify-content:center">%s</span>' % B.svg(B.ICON['gear'], t['ink'], 22, 1.8)
    rows = ''.join(('<div style="display:flex;align-items:center;gap:12px;min-height:52px;%s">%s<span style="flex:1;font-size:15px">%s</span>%s</div>'
                    % ('' if i == 0 else 'border-top:1px solid %s;' % C.mix(t['page'], t['line'], .5), B.svg(B.ICON['drag'], t['muted'], 22, 2.4), n,
                       '<span style="width:44px;height:26px;border-radius:999px;background:%s;position:relative;box-shadow:inset 0 0 0 1px %s">'
                       '<span style="position:absolute;top:3px;%s;width:20px;height:20px;border-radius:50%%;background:%s"></span></span>'
                       % (t['prim'] if on else t['wash'], C.edge(t), 'right:3px' if on else 'left:3px', t['onp'] if on else t['muted'])))
                   for i, (n, on) in enumerate([(c[0], c[0] not in ('Оплата в другой валюте',)) for c in CARDS[:8]]))
    under = period(t) + tabs(t) + spending(t)
    return C.topbar(t, 'Отчёты', back=False, right=tune) + '<div style="padding-top:72px">' + under + '</div>' + \
        C.sheet(t, 'Карточки', rows, 200, sub='Тяните ⠿, чтобы переставить') + \
        C.caption(t, 'В: значок в шапке открывает шторку со списком: переключатель — показать или скрыть, ⠿ — порядок. Лента не меняется, '
                     'но жест другой, чем на «Обзоре».', 0)


def settings_reports(t):
    rows = ''.join(handle_row(t, n, s, hidden=n == 'Кто сколько потратил', first=i == 0) for i, (n, s) in enumerate(CARDS[:9]))
    body = (label(t, 'КАРТОЧКИ', 4) + group(t, rows) + small(t, '+ ещё 3 ниже', 'margin:6px 20px 0;text-align:center'))
    return C.topbar(t, 'Отчёты') + '<div style="padding-top:72px">' + body + '</div>' + \
        C.caption(t, 'Общее: тот же список — «Ещё → Настройки → Экраны → Отчёты» (там же период и сравнение). Порядок и скрытие — на этом телефоне.', 0)


# ---------- Q2: custom reports ----------
def mine_card(t, title='Отпуск', sub='тег «отпуск» · расходы'):
    body = (small(t, sub) + '<div style="margin-top:8px;font-size:24px;font-weight:600">%s</div>' % amt('1 284,50', 'USD', 24)
            + bar_row(t, HOME, 'Жильё', '640', .9) + bar_row(t, CAFE, 'Кафе', '312', .45) + bar_row(t, TAXI, 'Такси', '186', .27))
    chip = ('<span style="height:22px;padding:0 8px;border-radius:999px;display:inline-flex;align-items:center;gap:5px;font-size:12px;font-weight:400;'
            'background:%s;color:%s"><span style="width:6px;height:6px;border-radius:50%%;background:%s"></span>свой</span>' % (C.mix(t['page'], t['acc'], .18), t['ink'], t['acc']))
    return C.card(t, title + '&nbsp;&nbsp;' + chip, 'Изменить', body)


def mine_feed(t):
    slot = ('<div style="margin:12px 16px 0;height:52px;border-radius:24px;border:1.5px dashed %s;display:flex;align-items:center;justify-content:center;'
            'font-size:15px;font-weight:600;color:%s">+ Свой отчёт</div>' % (t['line'], C.ink2(t)))
    return screen(t, 'Отчёты', period(t) + tabs(t) + mine_card(t) + slot,
                  'А: свой отчёт — обычная карточка ленты: тот же период и валюта, переставляется и скрывается как остальные. '
                  'Создать — «+ Свой отчёт» в конце ленты.')


def mine_sheet(t):
    def chips(items):
        return '<div style="display:flex;gap:8px;flex-wrap:wrap;margin-top:8px">%s</div>' % ''.join(items)
    field = ('<div style="margin-top:12px;height:56px;border-radius:16px;background:%s;box-shadow:inset 0 0 0 1px %s;padding:6px 16px;box-sizing:border-box">'
             '<div style="font-size:12px;color:%s">Название</div><div style="font-size:16px">Отпуск</div></div>' % (t['wash'], t['line'], t['muted']))
    tag = ('<span style="height:34px;padding:0 12px;border-radius:999px;display:inline-flex;align-items:center;gap:6px;font-size:14px;background:%s;color:%s;font-weight:700">'
           '<span style="width:8px;height:8px;border-radius:50%%;background:#0E1F00"></span>отпуск ✕</span>' % (FUN, '#0E1F00'))
    body = (field + small(t, 'Что считать', 'margin-top:14px') + C.segmented(t, ['Расходы', 'Доходы'], 0).replace('margin:12px 16px 0', 'margin:8px 0 0')
            + small(t, 'Какие операции', 'margin-top:14px')
            + chips([tag, C.pill(t, 'Категории: все'), C.pill(t, 'Счета: все'), C.pill(t, '+ Получатель')])
            + small(t, 'Разбить', 'margin-top:14px')
            + chips([C.pill(t, 'По категориям', True), C.pill(t, 'По месяцам'), C.pill(t, 'По получателям'), C.pill(t, 'По счетам')])
            + '<div style="margin-top:18px">%s</div>' % C.button(t, 'Сохранить'))
    return C.topbar(t, 'Отчёты', back=False) + '<div style="padding-top:72px">' + period(t) + '</div>' + C.sheet(t, 'Свой отчёт', body, 150) + \
        C.caption(t, 'А: шторка «Свой отчёт» без прокрутки: название, расходы или доходы, отбор (теги, категории, счета, получатели — как фильтры «Операций»), разбивка. '
                     'Вид графика подбирается сам.', 0)


def slices(t):
    row = ('<div style="margin:12px 16px 0;display:flex;gap:8px;overflow:hidden">%s</div>'
           % ''.join([C.pill(t, 'Всё'), C.pill(t, 'Отпуск', True), C.pill(t, 'Еда вне дома'), C.pill(t, 'Ребёнок'), C.pill(t, '+')]))
    body = period(t) + row + spending(t).replace('Дом', 'Жильё').replace('Продукты', 'Кафе').replace('Образование', 'Такси').replace('>Кафе<', '>Музеи<') + months(t)
    return screen(t, 'Отчёты', body, 'Б: сохранённые срезы — чипы над лентой. «Отпуск» переключает всю ленту на операции с этим тегом: '
                                     'все карточки, но только по отбору. Проще, но сравнить с общими тратами — переключаться туда-обратно.')


W, H = 390, 820
BOARDS = [(n, f, W, H) for n, f in [
    ('Rep826Compact', order_compact), ('Rep826Home', order_home), ('Rep826Sheet', order_sheet), ('Rep826Settings', settings_reports),
    ('Rep826Mine', mine_feed), ('Rep826MineSheet', mine_sheet), ('Rep826Slices', slices)]]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
