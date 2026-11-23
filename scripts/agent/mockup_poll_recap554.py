#!/usr/bin/env python3
"""Poll boards for #554: «Итоги месяца» as stories (opened from the «Обзор» card; builds on #236 MonthReview).
Usage: python3 scripts/agent/mockup_poll_recap554.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402

H = 780
CATS = [('Продукты', 410, '#7FD6A0'), ('Дом', 380, '#6BB8FF'), ('Кафе', 120, '#FFB86B'), ('Такси', 70, '#E58BD0'), ('Другое', 160, '#FFE14D')]
WEEKS = [('1–7', 220), ('8–14', 640), ('15–21', 180), ('22–28', 150), ('29–31', 60)]


def stage(t):
    """Story background: hero gradient of the theme."""
    return ('linear-gradient(160deg,%s,%s)' % (C.mix(t['page'], t['prim'], .26), C.mix(t['page'], t['acc'], .34))) if t['dark'] else \
        ('linear-gradient(160deg,%s,%s)' % (C.mix('#FFFFFF', t['prim'], .6), C.mix('#FFFFFF', t['acc'], .3)))


def frame(t, k, n, title, body, phrase, share=True):
    bars = ''.join('<span style="flex:1;height:4px;border-radius:2px;background:%s"></span>' % (t['ink'] if i <= k else C.mix(t['page'], t['ink'], .25)) for i in range(n))
    sh = ('<div style="position:absolute;left:24px;right:24px;bottom:28px;display:flex;gap:8px;align-items:center"><span style="flex:1;font-size:14px;color:%s">нажмите — дальше</span>'
          '%s</div>' % (t['muted'], C.pill(t, 'Поделиться', h=40))) if share else ''
    return ('<div style="position:absolute;inset:0;background:%s">'
            '<div style="position:absolute;top:14px;left:16px;right:16px;display:flex;gap:4px">%s</div>'
            '<div style="position:absolute;top:30px;left:16px;right:16px;display:flex;justify-content:space-between;align-items:center;font-size:14px;color:%s"><span>Итоги октября</span>%s</div>'
            '<div style="position:absolute;top:84px;left:24px;right:24px"><div style="font-size:26px;font-weight:700;line-height:1.2">%s</div>%s'
            '<div style="margin-top:22px;font-size:18px;line-height:1.35">%s</div></div>%s</div>' % (
                stage(t), bars, t['muted'], C.glyph(B.ICON['close'], t['ink'], 22), title, body, phrase, sh))


def amount(t, v, sub=''):
    return '<div style="margin-top:18px;%sfont-size:40px">%s</div><div style="font-size:14px;color:%s">%s</div>' % (C.MONO, v, t['muted'], sub)


def bar_list(t, items, top):
    return ''.join('<div style="display:flex;justify-content:space-between;font-size:15px;margin-top:12px"><span>%s</span><span style="%s">%s</span></div>%s' % (
        n, C.MONO, v, C.meter(t, v_ / top)) for n, v, v_ in items)


def s_top(t):
    rows = ''.join('<div style="display:flex;align-items:center;gap:10px;margin-top:10px"><span style="width:14px;height:14px;border-radius:50%%;background:%s"></span><span style="flex:1;font-size:16px">%s</span>'
                   '<span style="%sfont-size:15px">%d USD</span></div>' % (c, n, C.MONO, v) for n, v, c in CATS)
    ring = ('<div style="width:150px;height:150px;border-radius:50%%;margin:20px auto 6px;background:conic-gradient(%s 0 %d%%,%s %d%% %d%%,%s %d%% %d%%,%s %d%% %d%%,%s %d%% 100%%);'
            'display:flex;align-items:center;justify-content:center"><div style="width:96px;height:96px;border-radius:50%%;background:%s;display:flex;flex-direction:column;align-items:center;justify-content:center">'
            '<b style="%sfont-size:18px">1 140</b><span style="font-size:11px;color:%s">USD</span></div></div>' % (
                CATS[0][2], 36, CATS[1][2], 36, 69, CATS[2][2], 69, 80, CATS[3][2], 80, 86, CATS[4][2], 86, C.mix(t['page'], t['acc'], .1) if t['dark'] else '#FFFFFF', C.MONO, t['muted']))
    return frame(t, 0, 6, 'Больше всего — на продукты', ring + rows, 'Это 36 % всех трат. Дом и продукты вместе — две трети месяца.')


def s_vs(t):
    cols = ''.join('<div style="flex:1;display:flex;flex-direction:column;align-items:center;justify-content:flex-end;gap:6px"><span style="%sfont-size:13px">%s</span><span style="width:60%%;height:%dpx;border-radius:12px;background:%s;%s"></span>'
                   '<span style="font-size:14px;color:%s">%s</span></div>' % (C.MONO, v, h, c, ('box-shadow:inset 0 0 0 1.5px %s' % C.edge(t)) if c == t['prim'] else '', t['muted'], m)
                   for m, v, h, c in [('сентябрь', '1 320', 190, t['wash']), ('октябрь', '1 140', 164, t['prim'])])
    return frame(t, 1, 6, 'Месяц легче прошлого на 14 %', '<div style="display:flex;height:250px;margin-top:20px;gap:24px;padding:0 24px">%s</div>' % cols, 'На 180 USD меньше, чем в сентябре. Так держать — без напряжения.')


def s_weeks(t):
    top = max(v for _, v in WEEKS)
    cols = ''.join('<div style="flex:1;display:flex;flex-direction:column;align-items:center;justify-content:flex-end;gap:6px"><span style="%sfont-size:12px">%d</span><span style="width:70%%;height:%dpx;border-radius:10px;background:%s;%s"></span>'
                   '<span style="font-size:12px;color:%s">%s</span></div>' % (C.MONO, v, max(10, round(v / top * 180)), t['prim'] if v == top else t['wash'], ('box-shadow:inset 0 0 0 1.5px %s' % C.edge(t)) if v == top else '', t['muted'], w) for w, v in WEEKS)
    return frame(t, 2, 6, 'Самая дорогая неделя — 8–14 октября', '<div style="display:flex;height:240px;margin-top:20px;gap:6px">%s</div>' % cols, 'В эту неделю была аренда. Остальные недели — спокойнее.')


def s_fun(t):
    return frame(t, 3, 6, 'На кафе ушло как 40 чашек кофе', amount(t, '120 USD', 'кафе за октябрь') + '<div style="font-size:44px;margin-top:14px;letter-spacing:2px">☕×40</div>',
                 'Самый тихий день — 11 октября: ни одной траты. Записей в месяце — 63.')


def s_weekend(t):
    pair = ''.join('<div style="flex:1"><div style="font-size:13px;color:%s">%s</div><div style="%sfont-size:24px">%s</div>%s</div>' % (t['muted'], a, C.MONO, v, C.meter(t, f)) for a, v, f in [('Будни', '740 USD', .65), ('Выходные', '400 USD', .35)])
    return frame(t, 4, 6, 'Выходные дороже по дням', '<div style="display:flex;gap:16px;margin-top:22px">%s</div>' % pair + amount(t, '57 → 100', 'средняя трата в день: будни → выходные'),
                 'Выходные — 35 % трат за 29 % дней. Новая категория месяца — «Подарки».')


def s_end(t):
    return frame(t, 5, 6, 'Месяц позади — и всё записано', '<div style="margin-top:24px;font-size:56px;text-align:center">✦</div>' + amount(t, '23 дня', 'подряд были записи — рекорд'),
                 'Лимиты соблюдены в 5 из 6 категорий. Хороший месяц.')


def home(t):
    card = C.hero(t, '<div style="display:flex;align-items:center;gap:12px"><div style="flex:1"><div style="font-size:13px;color:%s">Итоги октября</div><div style="font-size:18px;font-weight:600;margin-top:2px">Месяц легче прошлого на 14 %%</div>'
                     '<div style="font-size:13px;color:%s;margin-top:4px">6 историй · одна минута</div></div>%s</div>' % (t['muted'], t['muted'], C.glyph(CHEV_R, t['ink'], 24)))
    rows = C.card(t, 'Последние операции', '', C.row(t, 'cart', '#7FD6A0', 'Продукты', 'Карта', '−28,40 USD'))
    return C.topbar(t, 'Обзор', back=False) + '<div style="padding-top:72px">' + card + rows + '</div>' + C.nav(t, 'home') + C.caption(
        t, 'Карточка «Итоги октября ›» — первые 7 дней месяца на «Обзоре» (как сейчас «Итог месяца»). Нажатие открывает истории.', 0)


def scroll_b(t):
    top = max(v for _, v in WEEKS)
    s1 = C.card(t, 'Больше всего — на продукты', '36 %', bar_list(t, [(n, '%d USD' % v, v) for n, v, _ in CATS[:3]], 410))
    s2 = C.card(t, 'Легче прошлого на 14 %', '−180 USD', '<div style="font-size:14px;color:%s;margin-top:8px">1 320 → 1 140 USD</div>' % t['muted'])
    s3 = C.card(t, 'Самая дорогая неделя — 8–14', '640 USD', bar_list(t, [(w, '%d USD' % v, v) for w, v in WEEKS[:3]], top))
    s4 = C.card(t, 'На кафе — как 40 чашек кофе', '120 USD', '<div style="font-size:14px;color:%s;margin-top:6px">Самый тихий день — 11 октября.</div>' % t['muted'])
    return C.topbar(t, 'Итоги октября') + '<div style="padding-top:72px">' + C.hero(t, '<div style="font-size:18px;font-weight:600">Месяц легче прошлого на 14 %</div>') + s1 + s2 + s3 + s4 + '</div>' + \
        C.caption(t, 'Б: одна прокручиваемая страница из карточек — читается в своём темпе, без жестов. Картинка «Поделиться» — снимок выбранной карточки.', 0)


def phrases(t):
    rules = [('Потратили меньше прошлого', '«Месяц легче прошлого на 8 %»'), ('Потратили больше', '«Месяц был насыщенным, зато всё записано»'), ('Записей мало (до 10)', '«Хорошее начало. Дальше будет нагляднее»'),
             ('Все лимиты соблюдены', '«Лимиты в порядке — ориентир держится»'), ('Лимит превышен', '«Пара лимитов тесны — можно поправить цифры»'), ('Много дней подряд с записями', '«23 дня подряд — это привычка»'),
             ('Тихий день без трат', '«Самый спокойный день — 11 октября»'), ('Нет данных за прошлый месяц', '«Первый месяц в учёте — с него и начинается картина»')]
    body = ''.join('<div style="display:flex;gap:12px;padding:10px 0;border-top:1px solid %s;font-size:14px"><span style="flex:1;color:%s">%s</span><span style="flex:1.2">%s</span></div>' % (t['line'], t['muted'], a, b) for a, b in rules)
    return C.topbar(t, 'Слова итогов', back=False) + '<div style="padding-top:72px">' + C.card(t, 'Результат → фраза', 'без вины и красного', body) + '</div>' + C.caption(
        t, 'Правила подбора фраз. Берётся первая подходящая сверху вниз; тон — как в подсказках (hints.md).', 0)


def share(t):
    sw = ('<div style="display:flex;justify-content:space-between;align-items:center;margin-top:12px;font-size:15px"><span>Скрыть суммы</span>'
          '<span style="width:52px;height:30px;border-radius:999px;background:%s;box-shadow:inset 0 0 0 2px %s;position:relative"><span style="position:absolute;right:3px;top:3px;width:24px;height:24px;border-radius:50%%;background:%s"></span></span></div>' % (t['prim'], C.edge(t), t['onp']))
    img = ('<div style="margin:12px auto 0;width:200px;height:230px;border-radius:20px;background:%s;border:1px solid %s;padding:14px;font-size:14px"><b>Итоги октября</b><div style="margin-top:12px;font-size:16px;line-height:1.3">Месяц легче прошлого на 14 %%</div>'
           '<div style="margin-top:16px;%sfont-size:22px">•••• USD</div><div style="margin-top:14px;font-size:12px;color:%s">Больше всего — продукты, 36 %%</div></div>' % (stage(t), t['line'], C.MONO, t['muted']))
    page = C.topbar(t, 'Итоги октября') + '<div style="padding-top:72px">' + C.hero(t, '<div style="font-size:18px;font-weight:600">Месяц легче прошлого на 14 %</div>') + '</div>'
    return page + C.sheet(t, 'Поделиться картинкой', img + sw + '<div style="margin-top:14px">%s</div>' % C.button(t, 'Поделиться'), 140) + C.caption(t, 'Картинка карточки; по умолчанию суммы скрыты — остаются проценты и слова.', 0)


BOARDS = [('RecapHome', home, 390, 640), ('RecapS1', s_top, 390, H), ('RecapS2', s_vs, 390, H), ('RecapS3', s_weeks, 390, H), ('RecapS4', s_fun, 390, H), ('RecapS5', s_weekend, 390, H),
          ('RecapS6', s_end, 390, H), ('RecapScrollB', scroll_b, 390, 1000), ('RecapPhrases', phrases, 390, 760), ('RecapShare', share, 390, 780)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
