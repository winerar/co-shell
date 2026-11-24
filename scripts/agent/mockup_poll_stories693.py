#!/usr/bin/env python3
"""Poll boards for #693 (release stories): the «Что нового» notice over «Обзор» and the stories of 26.11.60, session 2026-11-24.
Placement is decided (#693 poll 2026-11-24 «А»: warm notice once after the update), the card look too (#598 «А»: scheme, title, 1–2 sentences,
«Дальше»). Asked here: the notice's look and which stories go into the release.
Usage: python3 scripts/agent/mockup_poll_stories693.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402

CLOSE = 'M6 6l12 12M18 6L6 18'
SPARK = 'M12 3l2 6 6 2-6 2-2 6-2-6-6-2 6-2z'


def overview(t):
    hero = C.hero(t, '<div style="font-size:13px;opacity:.8">Всего на счетах</div><div style="font-size:34px;font-weight:700;font-family:monospace;margin-top:4px">1 284 500 ARS</div>'
                     '<div style="font-size:14px;margin-top:6px;opacity:.8">≈ 1 052 USD</div>')
    ops = C.card(t, 'Последние операции', '', ''.join([C.row(t, 'cafe', '#FFB86B', 'Кафе «Моне»', 'Сегодня · Visa ARS', '−3 500 ARS'),
                                                      C.row(t, 'cart', '#7FD6A0', 'Coto', 'Вчера · Mercado Pago', '−12 500 ARS')]))
    return hero + ops


def notice_a(t):
    pill = ('<div style="position:absolute;left:16px;right:16px;bottom:200px;padding:12px 12px 12px 14px;border-radius:24px;background:%s;border:1px solid %s;'
            'display:flex;align-items:center;gap:12px;z-index:3"><span style="width:40px;height:40px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center;flex:none">%s</span>'
            '<div style="flex:1;font-size:15px;line-height:1.3"><b>Что нового</b> · 5 историй<div style="font-size:13px;color:%s">Долги, новые счета, веер «+»…</div></div>'
            '<span style="height:40px;padding:0 16px;border-radius:999px;background:%s;color:%s;display:flex;align-items:center;font-weight:600;box-shadow:inset 0 0 0 1px %s">Смотреть</span>'
            '<span style="flex:none">%s</span></div>') % (
        t['surf'], t['line'], C.mix(t['surf'], t['prim'], .35), C.glyph(SPARK, t['ink'], 20), t['muted'], t['prim'], t['onp'], C.edge(t), C.glyph(CLOSE, t['muted'], 20))
    return C.topbar(t, 'Обзор', back=False) + '<div style="padding-top:72px">' + overview(t) + '</div>' + pill + C.nav(t, 'home') + \
        C.caption(t, 'А: плашка над нижней панелью (готовый компонент, как «Месяц закончился · Открыть»). «Смотреть» — истории, «✕» — не сейчас, больше не появится. Сама не уходит. (На телефоне она стоит прямо над панелью.)', 0)


def notice_b(t):
    card = ('<div style="margin:8px 16px 0;padding:14px 16px;border-radius:24px;background:linear-gradient(135deg,%s,%s);border:1px solid %s;display:flex;align-items:center;gap:12px">'
            '<div style="flex:1"><div style="font-size:13px;color:%s">Версия 26.11.60</div><div style="font-size:17px;font-weight:700;margin-top:2px">Что нового · 5 историй</div></div>'
            '<span style="height:40px;padding:0 16px;border-radius:999px;background:%s;color:%s;display:flex;align-items:center;font-weight:600;box-shadow:inset 0 0 0 1px %s">Смотреть</span>%s</div>') % (
        C.mix(t['surf'], t['prim'], .3), t['surf'], t['line'], t['muted'], t['prim'], t['onp'], C.edge(t), C.glyph(CLOSE, t['muted'], 20))
    return C.topbar(t, 'Обзор', back=False) + '<div style="padding-top:72px">' + card + overview(t) + '</div>' + C.nav(t, 'home') + \
        C.caption(t, 'Б: карточка вверху «Обзора», на месте строки «Первые шаги». Заметнее, но сдвигает итог вниз; нужен новый вид карточки (опрос компонента).', 0)


def story(t, n, title, text, body, last=False):
    bars = ''.join('<span style="flex:1;height:4px;border-radius:2px;background:%s"></span>' % (t['prim'] if k < n else t['wash']) for k in range(5))
    btn = C.button(t, 'Готово' if last else 'Дальше')
    return ('<div style="position:absolute;inset:0;background:linear-gradient(180deg,%s,%s);padding:20px 16px"><div style="display:flex;gap:4px">%s</div>'
            '<div style="display:flex;justify-content:space-between;align-items:center;margin-top:12px"><span style="font-size:13px;color:%s">Что нового · 26.11.60</span>%s</div>'
            '<div style="margin-top:40px;height:190px;display:flex;justify-content:center;align-items:center">%s</div><div style="font-size:26px;font-weight:700;margin-top:28px;line-height:1.15">%s</div>'
            '<div style="font-size:16px;color:%s;margin-top:10px;line-height:1.4">%s</div><div style="position:absolute;left:16px;right:16px;bottom:28px">%s</div></div>') % (
        C.mix(t['page'], t['prim'], .2 if t['dark'] else .45), t['page'], bars, t['muted'], C.glyph(CLOSE, t['ink'], 22), body, title, t['muted'], text, btn)


def svg(t, inner, w=300, h=180):
    return '<svg width="%d" height="%d" viewBox="0 0 300 180" fill="none" stroke-linecap="round" stroke-linejoin="round">%s</svg>' % (w, h, inner)


def chip(t, x, y, w, label, on=False):
    fill = t['prim'] if on else t['surf']
    ink = t['onp'] if on else t['ink']
    return ('<rect x="%d" y="%d" width="%d" height="36" rx="18" fill="%s" stroke="%s" stroke-width="1.5"/>'
            '<text x="%d" y="%d" font-size="14" font-weight="600" fill="%s" text-anchor="middle" font-family="sans-serif">%s</text>') % (
        x, y, w, fill, C.edge(t) if on else t['line'], x + w // 2, y + 23, ink, label)


def s_debts(t):
    i, p = t['ink'], t['prim']
    body = svg(t, chip(t, 20, 20, 120, 'Мне должны', True) + chip(t, 160, 20, 120, 'Я должен') +
               '<circle cx="70" cy="120" r="26" fill="%s" stroke="%s" stroke-width="2"/><circle cx="230" cy="120" r="26" fill="%s" stroke="%s" stroke-width="2"/>'
               '<path d="M104 112h92" stroke="%s" stroke-width="3"/><path d="M186 102l10 10-10 10" stroke="%s" stroke-width="3"/>'
               '<path d="M196 134h-92" stroke="%s" stroke-width="3" stroke-dasharray="6 6"/><path d="M114 124l-10 10 10 10" stroke="%s" stroke-width="3"/>' % (
                   t['wash'], i, t['wash'], i, p, p, i, i))
    return story(t, 1, 'Долги — отдельно от трат', 'Одолжили или взяли в долг? «Ещё → Долги»: баланс счёта меняется, а расходы и доходы — нет.', body)


def s_accounts(t):
    p, i = t['prim'], t['ink']
    cards = ''.join('<rect x="%d" y="%d" width="120" height="70" rx="14" fill="%s" stroke="%s" stroke-width="2"/><text x="%d" y="%d" font-size="13" fill="%s" font-family="sans-serif">%s</text>' % (
        x, y, f, i, x + 12, y + 26, t['onp'] if f == p else i, lab) for x, y, f, lab in [(20, 10, p, 'Кредитка'), (160, 10, t['wash'], 'Проездной'), (20, 100, t['wash'], 'Вклад 8 %'), (160, 100, t['surf'], 'Только у меня')])
    lock = '<rect x="250" y="140" width="16" height="13" rx="3" stroke="%s" stroke-width="2"/><path d="M253 140v-4a5 5 0 0 1 10 0v4" stroke="%s" stroke-width="2"/>' % (i, i)
    return story(t, 2, 'Новые счета', 'Кредитка с лимитом и сроком платежа, проездной, вклад со ставкой. Счёт «Только у меня» не попадёт в семью.', svg(t, cards + lock))


def s_fan(t):
    p, i = t['prim'], t['ink']
    arcs = ''.join('<circle cx="%d" cy="%d" r="26" fill="%s" stroke="%s" stroke-width="2"/><text x="%d" y="%d" font-size="11" fill="%s" text-anchor="middle" font-family="sans-serif">%s</text>' % (
        x, y, t['surf'], i, x, y + 44, t['muted'], lab) for x, y, lab in [(70, 70, 'QR чека'), (150, 40, 'Заметка'), (230, 70, 'Шаблон')])
    plus = '<circle cx="150" cy="150" r="24" fill="%s" stroke="%s" stroke-width="2"/><path d="M150 140v20M140 150h20" stroke="%s" stroke-width="3"/>' % (p, C.edge(t), t['onp'])
    return story(t, 3, 'Удерживайте «+»', 'Откроется веер: QR чека, заметка или шаблон. Проведите палец к нужному и отпустите.', svg(t, arcs + plus))


def s_receipt(t):
    i, p = t['ink'], t['prim']
    paper = ('<path d="M30 10h90v150l-15-10-15 10-15-10-15 10-15-10-15 10z" fill="%s" stroke="%s" stroke-width="2"/>'
             '<rect x="44" y="32" width="60" height="7" rx="3" fill="%s" opacity=".4"/><rect x="44" y="50" width="44" height="7" rx="3" fill="%s" opacity=".4"/>'
             '<text x="44" y="96" font-size="15" font-family="monospace" fill="%s">*1234</text>') % (t['wash'], i, i, i, i)
    arrow = '<path d="M134 85h40" stroke="%s" stroke-width="3"/><path d="M166 75l10 10-10 10" stroke="%s" stroke-width="3"/>' % (p, p)
    card = ('<rect x="186" y="50" width="96" height="64" rx="12" fill="%s" stroke="%s" stroke-width="2"/><text x="198" y="78" font-size="13" fill="%s" font-family="sans-serif">Visa ARS</text>'
            '<text x="198" y="100" font-size="13" font-family="monospace" fill="%s">··1234</text>') % (p, C.edge(t), t['onp'], t['onp'])
    return story(t, 4, 'Чек сам находит счёт', 'По последним цифрам карты. Перевод между своими счетами откроется переводом, а незнакомый чек можно прочитать с ИИ.', svg(t, paper + arrow + card))


def s_recap(t):
    p, i = t['prim'], t['ink']
    phone = '<rect x="95" y="5" width="110" height="170" rx="18" fill="%s" stroke="%s" stroke-width="2"/>' % (t['wash'], i)
    bars = ''.join('<rect x="%d" y="16" width="22" height="4" rx="2" fill="%s"/>' % (105 + k * 24, p if k < 2 else t['surf']) for k in range(4))
    donut = '<circle cx="150" cy="90" r="34" stroke="%s" stroke-width="14"/><path d="M150 56a34 34 0 0 1 32 46" stroke="%s" stroke-width="14"/>' % (t['surf'], p)
    share = '<path d="M232 110v24h36v-24M250 92v32M240 102l10-10 10 10" stroke="%s" stroke-width="2.5"/>' % i
    return story(t, 5, 'Итоги месяца — историями', 'Листайте и отправляйте картинкой, с суммами или без. В «Отчётах» — календарь трат.', svg(t, phone + bars + donut + share), last=True)


def three(t):
    rows = ''.join('<div style="display:flex;align-items:center;gap:12px;padding:10px 16px;border-top:1px solid %s"><span style="width:28px;height:28px;border-radius:50%%;background:%s;color:%s;'
                   'display:flex;align-items:center;justify-content:center;font-weight:700;box-shadow:inset 0 0 0 1px %s">%d</span><span style="font-size:16px">%s</span></div>' % (
                       t['line'] if k else 'transparent', t['prim'], t['onp'], C.edge(t), k + 1, s) for k, s in enumerate(['Долги — отдельно от трат', 'Новые счета', 'Удерживайте «+»']))
    body = '<div style="margin:16px;border-radius:24px;background:%s;border:1px solid %s;padding:6px 0">%s</div>' % (t['surf'], t['line'], rows)
    return C.topbar(t, 'Что нового · 3 истории') + '<div style="padding-top:72px">' + body + '</div>' + \
        C.caption(t, 'Б: только три самых новых возможности; чеки и «Итоги месяца» остаются строками списка «Что нового».', 0)


BOARDS = [('Rel693NoticeA', notice_a, 390, 820), ('Rel693NoticeB', notice_b, 390, 820),
          ('Rel693S1', s_debts, 390, 820), ('Rel693S2', s_accounts, 390, 820), ('Rel693S3', s_fan, 390, 820),
          ('Rel693S4', s_receipt, 390, 820), ('Rel693S5', s_recap, 390, 820), ('Rel693Three', three, 390, 820)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
