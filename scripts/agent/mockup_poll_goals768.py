#!/usr/bin/env python3
"""Poll boards for #768: savings goals and sinking funds («сначала заплати себе», копилки на годовые платежи).
Usage: python3 scripts/agent/mockup_poll_goals768.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402

B = C.B
COLORS = ['#7FD6A0', '#FFB86B', '#6BB8FF', '#E58BD0', '#FFE14D', '#9C8CFF']
# (icon, name, saved, target, currency, sub (deadline · per month), state) — state: '' | 'done' | 'yearly'
GOALS = [('gift', 'Отпуск', '1 250', 2000, 'USD', 'к 1 июля · ≈ 125 USD в месяц', ''),
         ('house', 'Страховка квартиры', '180', 360, 'USD', 'каждый год, 15 марта · 45 USD в месяц', 'yearly'),
         ('school', 'Курсы английского', '240 000', 600000, 'ARS', 'к 1 февраля · ≈ 180 000 ARS в месяц', ''),
         ('wallet', 'Подушка', '3 000', 3000, 'USD', 'цель достигнута', 'done'),
         ('bus', 'Новый велосипед', '40', 450, 'USD', 'без срока', '')]
MORE = ['Ремонт кухни', 'Ноутбук', 'Подарки к праздникам', 'Налог на машину', 'Зубной врач', 'Свадьба друзей', 'Телефон', 'Годовой абонемент',
        'Летний лагерь', 'Шины', 'Диван', 'Поездка к родителям']


def num(s):
    return int(s.replace(' ', ''))


def goal_row(t, i, icon, name, saved, target, cur, sub, state, meter=True, right_sub='из %s'):
    frac = min(1.0, num(saved) / target)
    tick = (' <span style="display:inline-flex;vertical-align:-3px">%s</span>' % B.svg('check', C.ink2(t), 16, 2.2)) if state == 'done' else ''
    bar = ('<div style="margin-top:6px">%s</div>' % C.meter(t, frac)) if meter else ''
    total = '{:,}'.format(target).replace(',', ' ')
    return ('<div style="display:flex;align-items:center;gap:12px;padding:8px 16px;min-height:64px">%s<div style="flex:1;min-width:0"><div style="font-size:16px">%s%s</div>'
            '<div style="font-size:13px;color:%s">%s</div>%s</div><div style="text-align:right;%sfont-size:15px">%s<div style="font-size:12px;color:%s;font-family:Onest">%s</div></div></div>' % (
                C.badge(t, icon, COLORS[i % 6], 40), name, tick, t['muted'], sub, bar, C.MONO, saved + ' ' + cur, t['muted'], right_sub % total))


def group(t, rows):
    return '<div style="margin:8px 16px 0;border-radius:24px;background:%s;border:1px solid %s;padding:4px 0;overflow:hidden">%s</div>' % (t['surf'], t['line'], ''.join(rows))


def fab(t):
    return ('<span style="position:absolute;right:20px;bottom:160px;width:56px;height:56px;border-radius:50%%;background:%s;box-shadow:inset 0 0 0 1px %s;display:flex;align-items:center;'
            'justify-content:center;z-index:4">%s</span>' % (t['prim'], C.edge(t), B.svg('plus', t['onp'], 26, 2.2)))


def totals(t, saved='4 710 USD', month='≈ 350 USD', sub='ещё 240 000 ARS · по курсу на сегодня'):
    cell = lambda l, v: ('<div style="flex:1"><div style="font-size:13px;color:%s">%s</div><div style="%sfont-size:22px;margin-top:2px">%s</div></div>' % (t['muted'], l, C.MONO, v))  # noqa: E731
    return C.hero(t, '<div style="display:flex;gap:12px">%s%s</div><div style="font-size:12px;color:%s;margin-top:8px">%s</div>' % (
        cell('Накоплено', saved), cell('Откладывать в месяц', month), t['muted'], sub))


def base(t, inner, cap, cap_top):
    return C.topbar(t, 'Цели') + '<div style="padding-top:72px">' + inner + '</div>' + fab(t) + C.nav(t, 'more') + C.caption(t, cap, cap_top)


def acc_line(t, name, color, letters):
    return ('<span style="display:inline-flex;align-items:center;gap:4px"><span style="width:14px;height:14px;border-radius:4px;background:%s;display:inline-block"></span>%s</span>' % (color, name))


def list_a(t):
    rows = [goal_row(t, i, *g) for i, g in enumerate(GOALS)]
    rows[0] = rows[0].replace('к 1 июля', acc_line(t, 'Копилка USD', '#7FD6A0', 'КО') + ' · к 1 июля')
    inner = totals(t) + group(t, rows)
    return base(t, inner, 'А: цель привязана к счёту — накоплено = баланс счёта (копилка, вклад). Отдельно откладывать не нужно: перевели на счёт — цель выросла.', 735)


def list_b(t):
    rows = [goal_row(t, i, *g) for i, g in enumerate(GOALS)]
    btn = ('<span style="height:36px;padding:0 14px;border-radius:999px;background:%s;display:inline-flex;align-items:center;font-size:14px;font-weight:600;color:%s">Отложить</span>' % (t['wash'], C.ink2(t)))
    rows[0] = rows[0].replace('<div style="text-align:right;', '<div style="display:flex;flex-direction:column;align-items:flex-end;gap:6px">%s</div><div style="text-align:right;' % btn, 1)
    inner = totals(t) + group(t, rows)
    return base(t, inner, 'Б: откладываем вручную — «Отложить» на цели (деньги остаются на любом счёте, цель — метка на сумме). Гибче, но нужно не забывать нажимать.', 735)


def many(t):
    rows = [goal_row(t, i, ['gift', 'house', 'school', 'cart', 'bolt', 'user'][i % 6], n, str(50 * (i + 1)), 1000 + 250 * i, 'USD',
                     'к %d %s' % (i + 3, ['января', 'марта', 'мая', 'июля'][i % 4]), '', meter=False) for i, n in enumerate(MORE[:8])]
    search = ('<div style="margin:12px 16px 0;height:48px;border-radius:999px;background:%s;display:flex;align-items:center;gap:10px;padding:0 16px;color:%s;font-size:15px">%s Найти цель</div>' % (
        t['wash'], t['muted'], B.svg('search', t['muted'], 20, 1.8)))
    done = ('<div style="margin:12px 16px 0;height:48px;border-radius:999px;background:%s;display:flex;align-items:center;justify-content:space-between;padding:0 16px;font-size:14px">'
            '<span>Достигнутые: 4</span><span style="color:%s">показать ▾</span></div>' % (t['wash'], t['muted']))
    inner = totals(t, saved='12 480 USD', month='≈ 1 900 USD') + search + group(t, rows) + done
    return base(t, inner, 'А/Б: 25 целей — поиск, строки без полосы (полоса — в карточке цели), достигнутые свёрнуты внизу; порядок — по сроку.', 735)


def empty(t):
    body = ('<div style="margin:80px 32px 0;text-align:center"><div style="width:72px;height:72px;border-radius:50%%;background:%s;margin:0 auto 16px;display:flex;align-items:center;justify-content:center">%s</div>'
            '<div style="font-size:16px;line-height:1.4">Целей пока нет.<br>Нажмите «+», чтобы начать копить на отпуск, ремонт или годовой платёж.</div></div>' % (
                t['wash'], B.svg('gift', t['ink'], 34, 1.6)))
    hint = C.card(t, '', '', '<div style="font-size:14px;color:%s;line-height:1.4">Цель — сумма, на которую копите. Укажите срок, и приложение подскажет, сколько откладывать в месяц.</div>' % t['muted'])
    return base(t, body + hint, 'Пустой экран и подсказка-слово «Цель» (первый заход).', 735)


def new_sheet(t):
    acc = ''.join('<span style="width:44px;height:44px;border-radius:12px;background:%s;display:inline-flex;align-items:center;justify-content:center;font-size:13px;font-weight:700;color:#1A1A1A;%s">%s</span>' % (c, ('box-shadow:0 0 0 2px %s;' % C.edge(t)) if i == 0 else '', l)
                  for i, (c, l) in enumerate([('#7FD6A0', 'КО'), ('#FFB86B', 'ВК'), ('#6BB8FF', 'US')]))
    field = lambda lab, val, mono=False: ('<div style="height:56px;border-radius:16px;border:1px solid %s;background:%s;padding:8px 14px;margin-top:8px"><div style="font-size:12px;color:%s">%s</div>'  # noqa: E731
                                          '<div style="font-size:16px;%s">%s</div></div>' % (t['line'], t['wash'], t['muted'], lab, C.MONO if mono else '', val))
    switch = ('<div style="display:flex;align-items:center;justify-content:space-between;height:48px;margin-top:8px;font-size:15px"><span>Каждый год<div style="font-size:12px;color:%s">для страховки, налогов, годовых подписок</div></span>'
              '<span style="width:52px;height:32px;border-radius:999px;background:%s;box-shadow:inset 0 0 0 2px %s"></span></div>' % (t['muted'], t['wash'], t['line']))
    plan = ('<div style="margin-top:8px;padding:10px 14px;border-radius:16px;background:%s;font-size:14px">Откладывайте <b style="%s">≈ 125 USD</b> в месяц, чтобы успеть к 1 июля</div>' % (
        C.mix(t['surf'], t['prim'], .14), C.MONO))
    body = (field('Название', 'Отпуск') + '<div style="display:flex;gap:8px">%s</div>' % ''.join('<div style="flex:1">%s</div>' % f for f in [field('Сколько нужно', '2 000,00 USD', True), field('К какому дню', '1 июл 2027')]) +
            '<div style="margin-top:8px;font-size:13px;color:%s">Где копите</div><div style="display:flex;gap:8px;margin-top:6px">%s</div>' % (t['muted'], acc) +
            switch + plan + '<div style="margin-top:12px">%s</div>' % C.button(t, 'Сохранить'))
    page = C.topbar(t, 'Цели') + '<div style="padding-top:72px">' + totals(t) + '</div>'
    return page + C.sheet(t, 'Новая цель', body, 120) + C.caption(t, 'Новая цель: название, сумма, срок (необязательно), счёт (вариант А) и «Каждый год» для копилки на годовой платёж. «В месяц» считается сразу.', 40)


def detail(t):
    hist = ''.join('<div style="display:flex;justify-content:space-between;font-size:14px;margin-top:8px"><span style="color:%s">%s</span><span style="%s">%s</span></div>' % (t['muted'], d, C.MONO, a)
                   for d, a in [('ноябрь', '+300 USD'), ('октябрь', '+250 USD'), ('сентябрь', '+200 USD')])
    body = ('<div style="margin-top:12px"><div style="%sfont-size:28px">1 250 USD</div><div style="font-size:13px;color:%s;margin:2px 0 8px">из 2 000 USD · осталось 750 USD · к 1 июля</div>%s</div>' % (
        C.MONO, t['muted'], C.meter(t, .625)) +
            '<div style="margin-top:12px;padding:10px 14px;border-radius:16px;background:%s;font-size:14px">Чтобы успеть — <b style="%s">≈ 125 USD</b> в месяц. В прошлом месяце отложили 300 USD.</div>' % (
                C.mix(t['surf'], t['prim'], .14), C.MONO) +
            '<div style="margin-top:12px;font-size:13px;color:%s">По месяцам</div><div style="border-top:1px solid %s;padding-top:4px;margin-top:4px">%s</div>' % (t['muted'], t['line'], hist) +
            '<div style="display:flex;gap:8px;margin-top:16px"><div style="flex:1">%s</div><div style="flex:1">%s</div></div>' % (
                C.button(t, 'Пополнить'), C.button(t, 'Изменить', 'background:%s;color:%s;box-shadow:none' % (t['wash'], t['ink']))))
    page = C.topbar(t, 'Цели') + '<div style="padding-top:72px">' + totals(t) + '</div>'
    return page + C.sheet(t, 'Отпуск', body, 150, sub='Копилка USD') + C.caption(
        t, 'Карточка цели: сколько осталось, «в месяц», сколько отложили по месяцам. «Пополнить» — перевод на счёт цели (А) или «Отложить» (Б).', 60)


def home(t):
    goal = C.card(t, 'Цели', 'Все ›', goal_row(t, 0, *GOALS[0]).replace('padding:8px 16px', 'padding:8px 0') +
                  goal_row(t, 1, *GOALS[1]).replace('padding:8px 16px', 'padding:8px 0'))
    return C.topbar(t, 'Обзор', back=False) + '<div style="padding-top:72px">' + goal + '</div>' + C.nav(t, 'home') + C.caption(
        t, 'Карточка «Цели» на «Обзоре»: две ближайшие по сроку. Появляется, когда есть хоть одна цель; без целей её нет.', 420)


def done(t):
    burst = ('<div style="margin:40px 16px 0;text-align:center"><div style="width:72px;height:72px;border-radius:50%%;background:%s;margin:0 auto 12px;display:flex;align-items:center;justify-content:center;box-shadow:inset 0 0 0 1px %s">%s</div>'
             '<div style="font-size:20px;font-weight:600">Цель достигнута</div><div style="font-size:14px;color:%s;margin-top:6px;line-height:1.4">Подушка: 3 000 USD. Вы откладывали 8 месяцев — это и есть привычка.</div></div>' % (
                 t['prim'], C.edge(t), B.svg('check', t['onp'], 34, 2.4), t['muted']))
    yearly = C.card(t, 'Страховка квартиры', 'каждый год', '<div style="font-size:14px;color:%s;line-height:1.4;margin-top:6px">Срок прошёл — копилка начинается заново: к 15 марта 2028, 30 USD в месяц.</div>' % t['muted'])
    return C.topbar(t, 'Цели') + '<div style="padding-top:72px">' + burst + yearly + '</div>' + C.nav(t, 'more') + C.caption(
        t, 'Цель достигнута — один раз «Готово» с искрами (CoShellSuccessBurst). Годовая копилка после срока сама начинает новый круг.', 520)


BOARDS = [('GoalListA', list_a, 390, 820), ('GoalListB', list_b, 390, 820), ('GoalMany', many, 390, 820), ('GoalEmpty', empty, 390, 700),
          ('GoalNew', new_sheet, 390, 820), ('GoalDetail', detail, 390, 780), ('GoalHome', home, 390, 620), ('GoalDone', done, 390, 640)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
