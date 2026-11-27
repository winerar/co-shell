#!/usr/bin/env python3
"""Poll boards for #771 «Подписки и постоянные траты за год»: where the yearly sum of repeating expenses lives and whether it shows
its share of spending. Data — expense templates with «Повтор» (#143): monthly ×12, weekly ×52, yearly ×1, in the main currency.
Usage: python3 scripts/agent/mockup_poll_subs771.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402
import mockup_bricks as B  # noqa: E402

NBSP = ' '
# name, icon, colour key, rule, a month, a year — sorted by the year
ITEMS = [('Аренда', 'house', 'prim', 'Каждый месяц, 1-го', '650', '7' + NBSP + '800'),
         ('Спортзал', 'bolt', 'acc', 'Каждый месяц, 5-го', '40', '480'),
         ('Страховка', 'file', 'acc', 'Каждый год, 12 марта', '30', '360'),
         ('Интернет', 'wifi', 'prim', 'Каждый месяц, 10-го', '25', '300'),
         ('Телефон', 'phone', 'acc', 'Каждый месяц, 20-го', '15', '180'),
         ('Кино и сериалы', 'film', 'prim', 'Каждый месяц, 3-го', '12', '144'),
         ('Музыка', 'note', 'acc', 'Каждый месяц, 7-го', '6', '72')]
YEAR, MONTH, SHARE = '9' + NBSP + '336', '778', '41'


def amount(t, value, size=15, code=True):
    c = '<span style="font-size:%dpx;color:%s"> USD</span>' % (round(size * .8), t['muted']) if code else ''
    return '<span style="%sfont-size:%dpx">%s%s</span>' % (C.MONO, size, value, c)


def item_row(t, it, by_year=True):
    name, icon, col, rule, month, year = it
    right, sub = (('−' + year, 'в год · %s в месяц' % month) if by_year else ('−' + month, rule))
    return C.row(t, icon, t[col], name, rule if by_year else 'в месяц', amount(t, right), sub if by_year else '')


def card_subs(t, rows=3, share=True):
    head = '<div style="display:flex;justify-content:space-between;align-items:center"><b style="font-size:16px">Постоянные траты</b><span style="font-size:14px;font-weight:600;color:%s">Все %d ›</span></div>' % (C.ink2(t), len(ITEMS))
    total = '<div style="margin-top:8px">%s<span style="font-size:15px;color:%s"> в год</span></div>' % (amount(t, YEAR, 26), t['muted'])
    line = '<div style="font-size:14px;color:%s">%s USD в месяц%s</div>' % (t['muted'], MONTH, (' · %s %% ваших трат' % SHARE) if share else '')
    bar = '<div style="margin-top:10px">%s</div>' % C.meter(t, int(SHARE) / 100) if share else ''
    body = ''.join('<div style="margin:0 -16px">%s</div>' % item_row(t, it) for it in ITEMS[:rows])
    return C.card(t, body=head + total + line + bar + '<div style="margin-top:6px">%s</div>' % body)


def reports_top(t):
    return C.topbar(t, 'Отчёты', back=False) + '<div style="padding-top:72px">' + C.segmented(t, ['Траты', 'Доходы'], 0)


def subs_a(t):
    return reports_top(t) + card_subs(t) + '</div>' + C.nav(t) + C.caption(
        t, 'А: карточка «Постоянные траты» в «Отчётах»; «Все 7 ›» — свой список по убыванию суммы в год.', 0)


def subs_a_list(t):
    rows = ''.join(item_row(t, it) for it in ITEMS)
    hero = C.hero(t, '<div style="font-size:14px;color:%s">Постоянные траты в год</div><div>%s</div><div style="font-size:14px;color:%s">%s USD в месяц · %s %% ваших трат</div>' % (
        t['muted'], amount(t, YEAR, 32), t['muted'], MONTH, SHARE))
    return C.topbar(t, 'Постоянные траты') + '<div style="padding-top:72px">' + hero + C.card(t, body='<div style="margin:-8px -16px">%s</div>' % rows) + '</div>' + C.caption(
        t, 'А, «Все ›»: отдельный экран — итог за год и все платежи, сначала самые дорогие. Нажатие — шаблон платежа.', 0)


def subs_b(t):
    rows = ''.join(item_row(t, it) for it in ITEMS[:5])
    hero = C.hero(t, '<div style="font-size:14px;color:%s">Расходы за год · 7 платежей</div><div>%s</div><div style="font-size:14px;color:%s">%s USD в месяц · %s %% ваших трат</div>' % (
        t['muted'], amount(t, '−' + YEAR, 32), t['muted'], MONTH, SHARE))
    return C.topbar(t, 'Регулярные платежи') + '<div style="padding-top:72px">' + C.segmented(t, ['До конца месяца', 'За год'], 1) + hero + C.card(
        t, body='<div style="margin:-8px -16px">%s</div>' % rows) + '</div>' + C.caption(
        t, 'Б: в «Ещё → Регулярные» — переключатель «До конца месяца · За год»; за год — только расходы, сначала самые дорогие.', 0)


def subs_c(t):
    return reports_top(t) + card_subs(t) + '</div>' + C.nav(t) + C.caption(
        t, 'В: карточка в «Отчётах», а «Все 7 ›» открывает «Регулярные» сразу в режиме «За год» (экран Б). Один список платежей, без нового экрана.', 0)


def share_a(t):
    return reports_top(t) + card_subs(t, share=True) + '</div>' + C.caption(
        t, 'А: с долей — «41 % ваших трат» и полоса: доля постоянных в тратах за 3 последних месяца.', 0)


def share_b(t):
    return reports_top(t) + card_subs(t, share=False) + '</div>' + C.caption(
        t, 'Б: без доли — только сумма в год и в месяц. Проще, но не видно, много это или мало.', 0)


BOARDS = [('Subs771A', subs_a, 390, 820), ('Subs771AList', subs_a_list, 390, 820), ('Subs771B', subs_b, 390, 820), ('Subs771C', subs_c, 390, 820),
          ('Subs771ShareA', share_a, 390, 820), ('Subs771ShareB', share_b, 390, 820)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
