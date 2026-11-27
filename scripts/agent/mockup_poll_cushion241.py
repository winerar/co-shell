#!/usr/bin/env python3
"""Poll boards for #241 («Подушка» — how many months the money lasts) and #186 (notification at 80 % of a limit), session 2026-11-25.
Asked: where the cushion lives («Отчёты» card / «Обзор» tile), how its edge states read, the wording of the notification and its setting.
The numbers are one story: 9 000 USD on the accounts in the total, average spending 2 000 USD over the last six full months → «≈ 4,5 мес.».
Usage: python3 scripts/agent/mockup_poll_cushion241.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402

MONTHS_OK = 4.5   # cushion in months
GOAL = 6          # the bar's scale: half a year


def big(t, value='≈ 4,5', unit='мес.'):
    return ('<div style="display:flex;align-items:baseline;gap:8px;margin-top:6px"><span style="%sfont-size:34px;font-weight:700">%s</span>'
            '<span style="font-size:18px;color:%s">%s</span></div>' % (C.MONO, value, t['muted'], unit))


def scale(t, frac):
    marks = ''.join('<span>%s</span>' % m for m in ('0', '3', '6 мес.'))
    return (C.meter(t, frac) + '<div style="display:flex;justify-content:space-between;font-size:11px;color:%s;margin-top:4px">%s</div>' % (t['muted'], marks))


def cushion_card(t, value='≈ 4,5', unit='мес.', gloss='Денег хватит при тратах как в среднем за полгода: 2 000 USD в месяц.',
                 extra='', frac=MONTHS_OK / GOAL):
    return C.card(t, 'Подушка', 'на счетах 9 000 USD', big(t, value, unit)
                  + '<div style="font-size:13px;color:%s;margin-top:4px;line-height:1.4">%s</div><div style="margin-top:12px">%s</div>%s' % (
                      t['muted'], gloss, scale(t, frac), extra))


def reports_feed(t, card_html):
    capital = C.card(t, 'Капитал', 'все счета', '<div style="%sfont-size:24px;font-weight:700;margin-top:4px">8 503 <span style="font-size:14px;color:%s">USD</span></div>'
                     '<div style="height:64px;margin-top:10px;border-radius:12px;background:%s"></div>' % (C.MONO, t['muted'], t['wash']))
    return (C.topbar(t, 'Отчёты', back=False) + '<div style="padding-top:72px">'
            + C.segmented(t, ['Обзор', 'Месяцы', 'Календарь'], 0) + capital + card_html + '</div>' + C.nav(t, 'chart'))


def cushion_reports(t):
    return reports_feed(t, cushion_card(t)) + C.caption(t, 'А: карточка в ленте «Отчётов» после «Капитала». Число, одна фраза и шкала 0–6 месяцев.', 0)


def cushion_home(t):
    hero = C.hero(t, '<div style="font-size:13px;opacity:.8">Всего на счетах</div><div style="font-size:34px;font-weight:700;font-family:monospace;margin-top:4px">9 000 USD</div>')
    tile = ('<div style="margin:12px 16px 0;padding:14px 16px;border-radius:24px;background:%s;border:1px solid %s;display:flex;align-items:center;gap:12px">'
            '%s<div style="flex:1"><div style="font-size:13px;color:%s">Подушка</div><div style="%sfont-size:20px;font-weight:700">≈ 4,5 мес.</div></div>'
            '<span style="font-size:13px;color:%s">при тратах как за полгода</span></div>' % (
                t['surf'], t['line'], C.badge(t, 'wallet', t['prim']), t['muted'], C.MONO, t['muted']))
    ops = C.card(t, 'Последние операции', '', C.row(t, 'cafe', '#FFB86B', 'Кафе «Моне»', 'Сегодня · Visa ARS', '−3 500 ARS'))
    return (C.topbar(t, 'Обзор', back=False) + '<div style="padding-top:72px">' + hero + tile + ops + '</div>' + C.nav(t, 'home')
            + C.caption(t, 'Б: плитка на «Обзоре» под итогом. Нажатие — «Отчёты», карточка подушки.', 0))


def cushion_edge(t):
    few = cushion_card(t, '≈ 4,5', 'мес.', 'Посчитано по 2 месяцам — с ростом записей станет точнее.')
    norates = cushion_card(t, '≈ 4,5', 'мес.', 'Без учёта: AMD — нет курса. «Ещё → Курсы».', frac=MONTHS_OK / GOAL)
    low = cushion_card(t, '≈ 0,8', 'мес.', 'Денег хватит на ближайшие три недели. Лимит — ориентир, а не запрет: посмотрите «Бюджеты».', frac=0.8 / GOAL)
    nodata = C.card(t, 'Подушка', '', '<div style="font-size:14px;margin-top:6px;line-height:1.4">Появится после первого полного месяца записей.</div>')
    return (C.topbar(t, 'Особые случаи', back=True) + '<div style="padding-top:72px">' + few + norates + low + nodata + '</div>'
            + C.caption(t, 'Мало истории · нет курса · денег немного (тон спокойный, без красного) · первого месяца ещё нет.', 0))


# ── #186: notification at 80 % of a limit ──────────────────────────────────────────────────────────────────

def push(t, title, text, actions=('Открыть',), time='9:00'):
    acts = ''.join('<span style="font-size:13px;font-weight:600;color:%s">%s</span>' % (C.ink2(t), a) for a in actions)
    return ('<div style="margin:12px 16px 0;padding:12px 14px;border-radius:24px;background:%s;border:1px solid %s"><div style="display:flex;align-items:center;gap:8px;font-size:12px;color:%s">'
            '%sco-shell · %s</div><div style="margin-top:6px;font-size:14px;font-weight:600">%s</div><div style="font-size:13px;color:%s;line-height:1.4">%s</div>'
            '<div style="display:flex;gap:20px;margin-top:10px">%s</div></div>' % (
                t['surf'], t['line'], t['muted'], C.glyph('M4 7h16v10H4z', t['muted'], 14), time, title, t['muted'], text, acts))


def lock(t, body, label):
    return ('<div style="padding:96px 0 0"><div style="text-align:center;%sfont-size:56px;font-weight:300">9:00</div>%s</div>' % (C.MONO, body)
            + C.caption(t, label, 0))


def note_a(t):
    return lock(t, push(t, 'Продукты: 80 % лимита', 'Потрачено 72 000 из 90 000 ARS. До конца месяца ещё 12 дней.')
                + push(t, 'Кафе: траты идут быстрее лимита', 'За 10 дней ушло 60 % лимита. Темп пока выше, чем нужно.'),
                'А: цифры — сколько потрачено. Два случая: 80 % лимита и темп быстрее плана.')


def note_b(t):
    return lock(t, push(t, 'Продукты: ещё можно 18 000 ARS', 'Это 20 % лимита на 12 дней. Лимит — ориентир, а не запрет.')
                + push(t, 'Кафе: траты опережают месяц', 'Если так пойдёт, лимит закончится раньше срока. Загляните в «Бюджеты».'),
                'Б: сначала сколько осталось, потом спокойное пояснение — как подсказка «limit_80» в приложении.')


def settings(t):
    def sw(title, sub, on):
        sw_ = ('<span style="width:52px;height:32px;border-radius:999px;background:%s;box-shadow:inset 0 0 0 1px %s;position:relative"><span style="position:absolute;top:4px;%s:4px;'
               'width:24px;height:24px;border-radius:50%%;background:%s"></span></span>' % (t['prim'] if on else t['wash'], C.edge(t), 'right' if on else 'left', t['onp'] if on else t['muted']))
        return ('<div style="display:flex;align-items:center;gap:12px;padding:10px 16px;min-height:56px;border-top:1px solid %s"><div style="flex:1"><div style="font-size:16px">%s</div>'
                '<div style="font-size:13px;color:%s">%s</div></div>%s</div>' % (t['line'], title, t['muted'], sub, sw_))
    body = (C.card(t, 'Платежи', '', '<div style="margin:8px -16px 0">%s</div>' % sw('Напоминания о платежах', 'за день до срока и в день платежа', True))
            + C.card(t, 'Бюджеты', '', '<div style="margin:8px -16px 0">%s</div>' % sw('Лимиты', 'раз в день: 80 % лимита или темп быстрее плана', True))
            + C.card(t, 'Время', '', '<div style="margin:6px 0 2px">%s</div>' % ''.join(C.pill(t, s, s == '9:00', extra='margin-right:6px') for s in ('8:00', '9:00', '12:00', '18:00'))))
    return (C.topbar(t, 'Уведомления') + '<div style="padding-top:72px">' + body + '</div>'
            + C.caption(t, 'Новая группа «Бюджеты» в «Ещё → Настройки → Уведомления». Время то же, что у напоминаний о платежах. Нажатие на уведомление — «Бюджеты» этой категории.', 0))


BOARDS = [('Cush241Reports', cushion_reports, 390, 820), ('Cush241Home', cushion_home, 390, 820), ('Cush241Edge', cushion_edge, 390, 900),
          ('Not186A', note_a, 390, 720), ('Not186B', note_b, 390, 720), ('Not186Set', settings, 390, 640)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
