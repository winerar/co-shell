"""Generated boards: «Итоги месяца» as stories (#554, owner poll 2026-11-23, all «А»). The card on «Обзор» (`Mon236A`) opens
the first story; a tap goes on, the last one ends with an encouraging phrase and «Подробнее» → `Mon236Screen`.
Data — October 2026 (shown on 2 November): spent 1 140 USD, September 1 320.

ROW — (key, row title, sticky text) for the «Экраны» page; BOARDS — (name, title, fn, key).
"""
from mockup_plan_bricks import *  # noqa: F401,F403
from mockup_catalog_parts import columns_v2, donut

ROW = ('recap', 'Итоги месяца',
       'Макет #554 — выбрано «А» (владелец 2026-11-23): истории на градиенте темы, полоски сверху, нажатие — дальше. Карточка на «Обзоре» — `Mon236A`, '
       'её нажатие открывает истории, «Подробнее» на последней — `Mon236Screen`. Слова — первое подходящее правило `RecapPhrases` (8 правил).')

CATS = [('Продукты', 410, '#7FD6A0'), ('Дом', 380, '#6BB8FF'), ('Кафе', 120, '#FFB86B'), ('Такси', 70, '#E58BD0'), ('Другое', 160, '#FFE14D')]
N = 6


def bars(at):
    return '<div style="display: flex; gap: 4px;">%s</div>' % ''.join(
        '<div style="flex: 1; height: 8px; border-radius: 999px; background: %s; %s"></div>' % (LIME if i <= at else LINE2, 'box-shadow: inset 0 0 0 1px %s;' % LIME if i <= at else '')
        for i in range(N))


def story(at, title, body, caption=''):
    cap = '<div style="margin-top: 12px; font-size: 14px; color: %s;">%s</div>' % (HERO_HINT, caption) if caption else ''
    inner = '<div style="font-size: 20px; font-weight: 700; line-height: 1.25;">%s</div><div style="margin-top: 16px;">%s</div>%s' % (title, body, cap)
    return inner_screen('Итоги октября', bars(at) + hero_card(inner, pad='20px', extra='margin-top: 12px; min-height: 520px;'))


def top():
    slices = [(v / 1140, c) for _, v, c in CATS]
    rows = ''.join('<div style="display: flex; align-items: center; gap: 10px; margin-top: 10px;"><span style="width: 14px; height: 14px; border-radius: 50%%; background: %s;"></span>'
                   '<span style="flex: 1; font-size: 16px;">%s</span><span style="%s font-size: 15px;">%d USD</span></div>' % (c, n, MONO, v) for n, v, c in CATS)
    center = '<b style="%s font-size: 18px;">1 140</b><span style="font-size: 11px; color: %s;">USD</span>' % (MONO, HERO_HINT)
    return story(0, 'Больше всего — на «Продукты»', '<div style="display: flex; justify-content: center;">%s</div>%s' % (donut(slices, center, 150, 20), rows))


def vs():
    return story(1, 'Месяц легче прошлого на 14 %', columns_v2([1320, 1140], ['Прошлый месяц', 'Этот месяц'], 1, h=220))


def weeks():
    return story(2, 'Самая дорогая неделя: 8–14', columns_v2([220, 640, 180, 150, 60], ['1–7', '8–14', '15–21', '22–28', '29–31'], 1, h=220), 'Траты по неделям месяца')


def new():
    return story(3, 'Новая категория месяца', '<div style="font-size: 28px; font-weight: 700;">Подарки</div>', 'Раньше трат в ней не было.')


def weekend():
    return story(4, 'Выходные дороже будней', columns_v2([57, 100], ['Будни', 'Выходные'], 1, h=220), 'Средняя трата за день с записями')


def final():
    btn = button('Подробнее')
    return story(5, 'Месяц легче прошлого на 14 %', btn, 'Дней с записями: 22')


BOARDS = [
    ('Rec554Top', 'Истории «А»: 1 — больше всего ушло на…', top, 'recap'),
    ('Rec554Vs', 'Истории «А»: 2 — сравнение с прошлым месяцем', vs, 'recap'),
    ('Rec554Weeks', 'Истории «А»: 3 — самая дорогая неделя', weeks, 'recap'),
    ('Rec554New', 'Истории «А»: 4 — новая категория месяца', new, 'recap'),
    ('Rec554Weekend', 'Истории «А»: 5 — будни и выходные', weekend, 'recap'),
    ('Rec554Final', 'Истории «А»: 6 — слова по результату и «Подробнее»', final, 'recap'),
]
