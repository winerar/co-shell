#!/usr/bin/env python3
"""Poll boards for #745 (local notifications), session 2026-11-25.
Asked: how «Уведомления» is laid out for seven kinds (groups vs a flat list + shared time), the defaults and the soft offer to turn the evening
reminder on, and the tone of the notification texts (short vs with numbers).
Usage: python3 scripts/agent/mockup_poll_notices745.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402


def sw(t, title, sub, on, top=True):
    knob = ('<span style="width:52px;height:32px;border-radius:999px;background:%s;box-shadow:inset 0 0 0 1px %s;position:relative;flex:none"><span style="position:absolute;'
            'top:4px;%s:4px;width:24px;height:24px;border-radius:50%%;background:%s"></span></span>' % (
                t['prim'] if on else t['wash'], C.edge(t), 'right' if on else 'left', t['onp'] if on else t['muted']))
    return ('<div style="display:flex;align-items:center;gap:12px;padding:10px 16px;min-height:56px;%s"><div style="flex:1"><div style="font-size:16px">%s</div>'
            '<div style="font-size:13px;color:%s">%s</div></div>%s</div>' % ('border-top:1px solid %s' % t['line'] if top else '', title, t['muted'], sub, knob))


def group(t, head, rows):
    inner = ''.join(sw(t, r[0], r[1], r[2], top=i > 0) for i, r in enumerate(rows))
    return C.card(t, head, '', '<div style="margin:8px -16px -4px">%s</div>' % inner)


def times(t, label, opts, sel):
    pills = ''.join(C.pill(t, s, s == sel, extra='margin-right:6px') for s in opts)
    return '<div style="padding:8px 16px 10px"><div style="font-size:13px;color:%s;margin-bottom:6px">%s</div>%s</div>' % (t['muted'], label, pills)


# ── Q1: the settings screen ───────────────────────────────────────────────────────────────────────────

def set_a(t):
    body = (group(t, 'Платежи', [('Напоминания о платежах', 'шаблоны, кредитка, вклад: за день и в день срока', True)])
            + group(t, 'Траты', [('Вечером — записать траты', 'если за день записей нет', True)])
            + '<div style="margin:0 16px">%s</div>' % times(t, 'Когда напоминать', ('19:00', '20:00', '21:00', '22:00'), '21:00')
            + group(t, 'Бюджет', [('Лимиты', '80 % лимита или темп быстрее плана', True), ('Итоги месяца', 'в первые дни нового месяца', True)])
            + group(t, 'Порядок', [('Резервная копия', 'раз в две недели, если давно не сохраняли', True), ('Черновики', 'когда накопилось три готовых', False),
                                   ('С возвращением', 'после трёх дней без записей, один раз', False)])
            + group(t, 'Семья', [('Трата партнёра', 'когда внесли крупную операцию', True)]))
    return (C.topbar(t, 'Уведомления') + '<div style="padding-top:72px">' + body + '</div>'
            + C.caption(t, 'А: пять групп по смыслу, у каждого вида свой переключатель. Время — одно, у вечернего напоминания. Не больше одного необязательного в день.', 0))


def set_b(t):
    rows = [('Напоминания о платежах', 'за день и в день срока', True), ('Вечером — записать траты', 'если записей за день нет', True),
            ('Лимиты', '80 % или темп быстрее плана', True), ('Итоги месяца', 'в первые дни месяца', True),
            ('Резервная копия', 'раз в две недели', True), ('Черновики', 'от трёх готовых', False), ('С возвращением', 'после трёх дней тишины', False),
            ('Трата партнёра', 'крупная операция семьи', True)]
    inner = ''.join(sw(t, r[0], r[1], r[2], top=i > 0) for i, r in enumerate(rows))
    body = (C.card(t, 'Когда', '', times(t, 'Время напоминаний', ('8:00', '12:00', '18:00', '21:00'), '21:00').replace('padding:8px 16px 10px', 'padding:6px 0 4px')
                   + '<div style="display:flex;justify-content:space-between;font-size:16px;padding:8px 0 2px;border-top:1px solid %s"><span>Тихие часы</span>'
                     '<span style="color:%s">22:00 – 8:00</span></div>' % (t['line'], t['muted']))
            + C.card(t, 'Что напоминать', '', '<div style="margin:8px -16px -4px">%s</div>' % inner))
    return (C.topbar(t, 'Уведомления') + '<div style="padding-top:72px">' + body + '</div>'
            + C.caption(t, 'Б: один плоский список без заголовков групп. Время и тихие часы — общие, сверху: утренние и вечерние виды приходят в выбранное время.', 0))


# ── Q2: defaults — the soft offer after the first entry ───────────────────────────────────────────────

def offer(t):
    hero = C.hero(t, '<div style="font-size:13px;opacity:.8">Всего на счетах</div><div style="font-size:34px;font-weight:700;font-family:monospace;margin-top:4px">412 300 ARS</div>')
    ops = C.card(t, 'Последние операции', '', C.row(t, 'cafe', '#FFB86B', 'Кафе «Моне»', 'Сегодня · Visa ARS', '−3 500 ARS'))
    pillc = ('<div style="position:absolute;left:32px;right:32px;bottom:200px;padding:12px 14px;border-radius:24px;background:%s;border:1px solid %s;z-index:4">'
             '<div style="font-size:14px;font-weight:600">Напоминать вечером о тратах?</div><div style="font-size:13px;color:%s;margin-top:2px">Один раз в день, если записей нет. Выключить можно в «Ещё → Настройки → Уведомления».</div>'
             '<div style="display:flex;gap:20px;margin-top:10px"><span style="font-size:14px;font-weight:600;color:%s">Включить</span><span style="font-size:14px;color:%s">Не сейчас</span></div></div>' % (
                 t['bar'], C.edge(t), t['muted'], C.ink2(t), t['muted']))
    return (C.topbar(t, 'Обзор', back=False) + '<div style="padding-top:72px">' + hero + ops + '</div>' + pillc + C.nav(t, 'home')
            + C.caption(t, 'А: всё выключено, кроме платежей. После первой записанной операции — одна карточка с предложением; «Включить» просит разрешение.', 0))


def defaults_b(t):
    body = (group(t, 'Платежи', [('Напоминания о платежах', 'шаблоны, кредитка, вклад', True)])
            + group(t, 'Траты', [('Вечером — записать траты', 'если за день записей нет', True)])
            + group(t, 'Бюджет', [('Лимиты', '80 % или темп быстрее плана', True), ('Итоги месяца', 'в первые дни месяца', True)])
            + group(t, 'Порядок', [('Резервная копия', 'раз в две недели', True), ('Черновики', 'от трёх готовых', True), ('С возвращением', 'после трёх дней тишины', True)]))
    return (C.topbar(t, 'Уведомления') + '<div style="padding-top:72px">' + body + '</div>'
            + C.caption(t, 'Б: всё включено сразу; разрешение спрашивается при первом запуске после первой операции. Необязательные всё равно не чаще одного в день.', 0))


# ── Q3: the texts on the lock screen ─────────────────────────────────────────────────────────────────

def push(t, title, text, actions=('Открыть',), time='21:00'):
    acts = ''.join('<span style="font-size:13px;font-weight:600;color:%s">%s</span>' % (C.ink2(t), a) for a in actions)
    return ('<div style="margin:10px 16px 0;padding:12px 14px;border-radius:24px;background:%s;border:1px solid %s"><div style="display:flex;align-items:center;gap:8px;font-size:12px;color:%s">'
            '%sco-shell · %s</div><div style="margin-top:6px;font-size:14px;font-weight:600">%s</div><div style="font-size:13px;color:%s;line-height:1.4">%s</div>'
            '<div style="display:flex;gap:20px;margin-top:10px">%s</div></div>' % (
                t['surf'], t['line'], t['muted'], C.glyph('M4 7h16v10H4z', t['muted'], 14), time, title, t['muted'], text, acts))


def lock(t, items, label):
    return ('<div style="padding:70px 0 0"><div style="text-align:center;%sfont-size:48px;font-weight:300">21:00</div>%s</div>' % (C.MONO, ''.join(items))
            + C.caption(t, label, 0))


def text_a1(t):
    return lock(t, [push(t, 'Как прошёл день?', 'Запишите сегодняшние траты — это минута.', ('Записать',)),
                    push(t, 'Продукты: ещё можно 18 000 ARS', 'Это 20 % лимита на 12 дней.'),
                    push(t, 'Завтра платёж: Аренда', '650 USD · Visa ARS.', ('Провести', 'Открыть')),
                    push(t, 'Итоги ноября готовы', 'Посмотрите, на что ушли деньги.', time='9:00')],
                'А · коротко (1/2): один факт и мягкая фраза. Платёж, лимит и итоги.')


def text_a2(t):
    return lock(t, [push(t, 'Сохраните резервную копию', 'Последняя — три недели назад.', ('Сохранить',)),
                    push(t, 'Черновики ждут', 'Готово к записи: 4. Проверьте и запишите.'),
                    push(t, 'С возвращением', 'Можно записать траты за эти дни: дату выберете в операции.', ('Записать',))],
                'А · коротко (2/2): копия, черновики, возвращение.')


def text_b1(t):
    return lock(t, [push(t, 'Сегодня записей нет', 'Кофе, проезд, магазин — внесите за минуту.', ('Записать',)),
                    push(t, 'Продукты: потрачено 72 000 из 90 000 ARS', '80 % лимита, до конца месяца 12 дней. Лимит — ориентир.'),
                    push(t, 'Аренда: 650 USD завтра', 'Нажмите, чтобы провести со счёта Visa ARS.', ('Провести', 'Открыть')),
                    push(t, 'Ноябрь: потрачено 412 000 ARS', 'На 8 % меньше октября. Откройте итоги.', time='9:00')],
                'Б · с цифрами (1/2): итог и сравнение прямо в тексте, без открытия приложения.')


def text_b2(t):
    return lock(t, [push(t, 'Копия не сохранялась 21 день', 'Ещё → Резервная копия.', ('Сохранить',)),
                    push(t, '4 черновика готовы к записи', 'Нажмите, чтобы проверить.'),
                    push(t, 'С возвращением', 'Записей за три дня нет. Внесите траты за них — дату выберете в операции.', ('Записать',))],
                'Б · с цифрами (2/2): путь в тексте, число в заголовке.')


BOARDS = [('Not745SetA', set_a, 390, 1320), ('Not745SetB', set_b, 390, 1060), ('Not745Offer', offer, 390, 760), ('Not745DefB', defaults_b, 390, 960),
          ('Not745TextA1', text_a1, 390, 880), ('Not745TextA2', text_a2, 390, 700), ('Not745TextB1', text_b1, 390, 940), ('Not745TextB2', text_b2, 390, 700)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
