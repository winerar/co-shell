#!/usr/bin/env python3
"""Poll boards for #552: «Долги» («Мне должны» / «Я должен», срок возврата, заметка).
Usage: python3 scripts/agent/mockup_poll_debts552.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402

# (person, left to return, of total, currency, due, note, state) — state: '' | 'soon' | 'late'
OWED_TO_ME = [('Саша', '120', 300, 'USD', 'до 5 декабря', 'за билеты', ''), ('Мама', '45 000', 45000, 'RUB', 'срок не задан', '', ''),
              ('Игорь', '80', 80, 'USD', 'через 3 дня', 'за ремонт', 'soon'), ('Лена', '15 000', 30000, 'ARS', 'срок прошёл 4 дня назад', 'за обед', 'late')]
I_OWE = [('Диа', '200', 500, 'USD', 'до 20 декабря', 'аванс за аренду', ''), ('Коля', '6 000', 6000, 'ARS', 'срок не задан', '', '')]
MORE = [('Артём', '40', 'USD'), ('Вика', '12 500', 'ARS'), ('Олег', '90', 'USD'), ('Таня', '3 000', 'RUB'), ('Рома', '25', 'USD'), ('Света', '8 000', 'ARS'),
        ('Паша', '60', 'USD'), ('Оля', '1 500', 'RUB'), ('Миша', '110', 'USD'), ('Нина', '4 200', 'ARS'), ('Глеб', '35', 'USD'), ('Юля', '700', 'RUB')]
COLORS = ['#7FD6A0', '#FFB86B', '#6BB8FF', '#E58BD0', '#FFE14D', '#9C8CFF']


def due_text(t, state, text):
    c = {'': t['muted'], 'soon': t['warm'] if t['dark'] else '#6E6200', 'late': t['warm'] if t['dark'] else '#6E6200'}[state]
    return '<span style="color:%s">%s</span>' % (c, text)


def person_row(t, i, name, left, total, cur, due, note, state, show_meter=True, sign=''):
    frac = (1 - float(left.replace(' ', '').replace('+', '').replace('−', '')) / total) if total else 0
    sub = due_text(t, state, due) + ((' · ' + note) if note else '')
    meter = ('<div style="margin-top:6px">%s</div>' % C.meter(t, frac)) if show_meter and frac > 0 else ''
    return ('<div style="display:flex;align-items:center;gap:12px;padding:8px 16px;min-height:64px">%s<div style="flex:1;min-width:0"><div style="font-size:16px">%s</div>'
            '<div style="font-size:13px">%s</div>%s</div><div style="text-align:right;%sfont-size:15px">%s%s<div style="font-size:12px;color:%s;font-family:Onest">осталось</div></div></div>' % (
                C.badge(t, 'user', COLORS[i % 6], 40), name, sub, meter, C.MONO, sign, left + ' ' + cur, t['muted']))


def group(t, rows):
    return '<div style="margin:8px 16px 0;border-radius:24px;background:%s;border:1px solid %s;padding:4px 0;overflow:hidden">%s</div>' % (t['surf'], t['line'], ''.join(rows))


def fab(t):
    return ('<span style="position:absolute;right:20px;bottom:160px;width:56px;height:56px;border-radius:50%%;background:%s;box-shadow:inset 0 0 0 1px %s;display:flex;align-items:center;'
            'justify-content:center;z-index:4">%s</span>' % (t['prim'], C.edge(t), B.svg('plus', t['onp'], 26, 2.2)))


def totals(t, a_lab='Мне должны', a='120 USD', b_lab='Я должен', b='200 USD', sub='ещё 45 000 RUB, 15 000 ARS'):
    cell = lambda l, v: ('<div style="flex:1"><div style="font-size:13px;color:%s">%s</div><div style="%sfont-size:22px;margin-top:2px">%s</div></div>' % (t['muted'], l, C.MONO, v))  # noqa: E731
    return C.hero(t, '<div style="display:flex;gap:12px">%s%s</div><div style="font-size:12px;color:%s;margin-top:8px">%s</div>' % (cell(a_lab, a), cell(b_lab, b), t['muted'], sub))


def base(t, title, inner, cap, cap_top, h=0):
    return C.topbar(t, title) + '<div style="padding-top:72px">' + inner + '</div>' + fab(t) + C.nav(t, 'more') + C.caption(t, cap, cap_top)


def closed_row(t):
    return ('<div style="margin:12px 16px 0;height:48px;border-radius:999px;background:%s;display:flex;align-items:center;justify-content:space-between;padding:0 16px;font-size:14px">'
            '<span>Закрытые: 3</span><span style="color:%s">показать ▾</span></div>' % (t['wash'], t['muted']))


def list_a(t):
    inner = totals(t) + C.segmented(t, ['Мне должны · 4', 'Я должен · 2'], 0) + group(t, [person_row(t, i, *r) for i, r in enumerate(OWED_TO_ME)]) + closed_row(t)
    return base(t, 'Долги', inner, 'А: два раздела-вкладки (листаются свайпом); сверху — итоги по обоим. Закрытые свёрнуты внизу.', 735)


def list_b(t):
    rows = [person_row(t, 0, 'Саша', '+120', 300, 'USD', 'мне должны · до 5 декабря', 'за билеты', ''), person_row(t, 1, 'Диа', '−200', 500, 'USD', 'я должен · до 20 декабря', 'аванс', ''),
            person_row(t, 2, 'Игорь', '+80', 80, 'USD', 'мне должны · через 3 дня', '', 'soon'), person_row(t, 3, 'Мама', '+45 000', 45000, 'RUB', 'мне должны', '', ''),
            person_row(t, 4, 'Лена', '+15 000', 30000, 'ARS', 'срок прошёл 4 дня назад', '', 'late'), person_row(t, 5, 'Коля', '−6 000', 6000, 'ARS', 'я должен', '', '')]
    inner = totals(t) + C.card(t, 'Все долги', 'сначала срочные', '', 'padding:10px 16px').replace('margin:12px 16px 0', 'margin:12px 16px 0;border-bottom-left-radius:0;border-bottom-right-radius:0;border-bottom:0') + \
        '<div style="margin:0 16px;border:1px solid %s;border-top:0;border-radius:0 0 24px 24px;background:%s;padding-bottom:4px">%s</div>' % (t['line'], t['surf'], ''.join(rows)) + closed_row(t)
    return base(t, 'Долги', inner, 'Б: один список по срочности; знак «+» / «−» показывает, кто кому должен. Вкладок нет.', 735)


def many(t):
    names = [(n, l, c) for n, l, c in MORE]
    rows = [person_row(t, i, n, l, 1000000, c, 'срок не задан', '', '', show_meter=False) for i, (n, l, c) in enumerate(names[:8])]
    search = ('<div style="margin:12px 16px 0;height:48px;border-radius:999px;background:%s;display:flex;align-items:center;gap:10px;padding:0 16px;color:%s;font-size:15px">%s Найти человека</div>' % (
        t['wash'], t['muted'], B.svg(B.ICON['search'], t['muted'], 20, 1.8)))
    inner = totals(t, a='1 050 USD', sub='ещё 17 200 RUB, 24 700 ARS') + C.segmented(t, ['Мне должны · 22', 'Я должен · 2'], 0) + search + group(t, rows)
    return base(t, 'Долги', inner, 'А/Б: при 20+ долгах — поиск по имени, строки плотнее, итоги остаются сверху.', 735)


def empty(t):
    body = ('<div style="margin:80px 32px 0;text-align:center"><div style="width:72px;height:72px;border-radius:50%%;background:%s;margin:0 auto 16px;display:flex;align-items:center;justify-content:center">%s</div>'
            '<div style="font-size:16px;line-height:1.4">Долгов пока нет.<br>Нажмите «+», чтобы записать, кто вам должен или кому должны вы.</div></div>' % (
                t['wash'], B.svg(B.ICON['user'], t['ink'], 34, 1.6)))
    hint = C.card(t, '', '', '<div style="font-size:14px;color:%s;line-height:1.4">Долг — деньги, которые вы одолжили или заняли. Вернуть можно по частям, а срок — ориентир, не запрет.</div>' % t['muted'])
    return base(t, 'Долги', body + hint, 'Пустой экран и подсказка для новичка (слово «Долг» объясняем в первый раз).', 735)


def new_sheet(t):
    acc = ''.join('<span style="width:44px;height:44px;border-radius:12px;background:%s;display:inline-flex;align-items:center;justify-content:center;font-size:13px;font-weight:700;color:#1A1A1A;%s">%s</span>' % (c, ('box-shadow:0 0 0 2px %s;' % C.edge(t)) if i == 0 else '', l)
                  for i, (c, l) in enumerate([('#7FD6A0', 'КА'), ('#FFB86B', 'НА'), ('#6BB8FF', 'US')]))
    field = lambda lab, val, mono=False: ('<div style="height:56px;border-radius:16px;border:1px solid %s;background:%s;padding:8px 14px;margin-top:8px"><div style="font-size:12px;color:%s">%s</div>'  # noqa: E731
                                          '<div style="font-size:16px;%s">%s</div></div>' % (t['line'], t['wash'], t['muted'], lab, C.MONO if mono else '', val))
    body = (C.segmented(t, ['Одолжил я', 'Занял я'], 0).replace('margin:12px 16px 0', 'margin:12px 0 0') + field('Кто', 'Саша') +
            '<div style="display:flex;gap:8px">%s</div>' % ''.join('<div style="flex:1">%s</div>' % f for f in [field('Сумма', '300,00 USD', True), field('Срок возврата', '5 дек 2026')]) +
            '<div style="margin-top:8px;font-size:13px;color:%s">С какого счёта</div><div style="display:flex;gap:8px;margin-top:6px">%s</div>' % (t['muted'], acc) +
            field('Заметка', 'за билеты') + '<div style="margin-top:12px">%s</div>' % C.button(t, 'Сохранить'))
    page = C.topbar(t, 'Долги') + '<div style="padding-top:72px">' + totals(t) + '</div>'
    return page + C.sheet(t, 'Новый долг', body, 150) + C.caption(t, 'Новый долг: кто, сумма, срок и заметка — одна шторка без прокрутки. «С какого счёта» меняет баланс счёта (выдача — расход, но не трата в отчётах).', 60)


def detail(t):
    hist = ''.join('<div style="display:flex;justify-content:space-between;font-size:14px;margin-top:8px"><span style="color:%s">%s</span><span style="%s">%s</span></div>' % (t['muted'], d, C.MONO, a)
                   for d, a in [('1 ноября · выдано', '300 USD'), ('14 ноября · вернул', '−100 USD'), ('20 ноября · вернул', '−80 USD')])
    body = ('<div style="margin-top:12px"><div style="%sfont-size:28px">120 USD</div><div style="font-size:13px;color:%s;margin:2px 0 8px">осталось из 300 USD · срок: до 5 декабря</div>%s</div>' % (C.MONO, t['muted'], C.meter(t, .6)) +
            '<div style="margin-top:10px;font-size:14px">За билеты</div><div style="margin-top:10px;border-top:1px solid %s;padding-top:4px">%s</div>' % (t['line'], hist) +
            '<div style="display:flex;gap:8px;margin-top:16px"><div style="flex:1">%s</div><div style="flex:1">%s</div></div>' % (
                C.button(t, 'Вернули часть'), C.button(t, 'Закрыть долг', 'background:%s;color:%s;box-shadow:none' % (t['wash'], t['ink']))))
    page = C.topbar(t, 'Долги') + '<div style="padding-top:72px">' + totals(t) + '</div>'
    return page + C.sheet(t, 'Саша', body, 150, sub='Мне должны') + C.caption(t, 'Карточка долга: сколько осталось, история возвратов, «Вернули часть» (шторка с суммой) и «Закрыть долг».', 60)


def remind(t):
    note = ('<div style="margin:12px 16px 0;padding:10px 12px 10px 16px;border-radius:999px;background:%s;display:flex;align-items:center;gap:10px;box-shadow:inset 0 0 0 1px %s;font-size:14px">'
            '<span style="flex:1">Игорь обещал вернуть 80 USD через 3 дня</span><span style="color:%s;font-weight:700">Открыть</span></div>' % (C.mix(t['page'], t['warm'], .18), C.mix(t['page'], t['warm'], .6), C.ink2(t)))
    ov = C.card(t, 'Скоро', 'ближайшие платежи и долги', C.row(t, 'user', '#FFB86B', 'Игорь вернёт', 'через 3 дня', '+80 USD').replace('padding:8px 16px', 'padding:8px 0') +
                C.row(t, 'house', '#6BB8FF', 'Аренда', 'через 6 дней', '−650 USD').replace('padding:8px 16px', 'padding:8px 0'))
    return C.topbar(t, 'Обзор', back=False) + '<div style="padding-top:72px">' + note + ov + '</div>' + C.nav(t, 'home') + C.caption(
        t, 'Срок возврата: тихая плашка (без красного и уведомлений телефона) за 3 дня и в день срока + строка в «Скоро» на «Обзоре».', 600)


BOARDS = [('DebtListA', list_a, 390, 820), ('DebtListB', list_b, 390, 820), ('DebtMany', many, 390, 820), ('DebtEmpty', empty, 390, 700), ('DebtNew', new_sheet, 390, 820),
          ('DebtDetail', detail, 390, 760), ('DebtRemind', remind, 390, 720)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
