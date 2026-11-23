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


def share_action():
    return icon_button('upload', 'Поделиться', INK)


def share_screen(body, overlay=''):
    return inner_head('Итоги октября', share_action()) + '<div style="padding: 0 16px;">%s</div>%s' % (body, overlay)


def share_final():
    """Share icon in the top bar of every story (not only the last): one tap → picture of the card in the system «Поделиться»."""
    inner = ('<div style="font-size: 20px; font-weight: 700; line-height: 1.25;">Месяц легче прошлого на 14 %%</div>'
             '<div style="margin-top: 16px;">%s</div><div style="margin-top: 12px; font-size: 14px; color: %s;">Дней с записями: 22</div>' % (button('Подробнее'), HERO_HINT))
    return share_screen(bars(5) + hero_card(inner, pad='20px', extra='margin-top: 12px; min-height: 520px;'))


def share_image():
    """The picture itself (1080 × 1350 at 4:5, drawn at 390 wide): the story card on the theme gradient, amounts hidden, a quiet mark below."""
    mark = '<div style="margin-top: 20px; font-size: 13px; color: %s; text-align: center;">co-shell · Итоги октября</div>' % HERO_HINT
    inner = ('<div style="font-size: 22px; font-weight: 700; line-height: 1.25;">Больше всего — на «Продукты»</div><div style="margin-top: 20px;">%s</div>%s%s'
             % ('<div style="display: flex; justify-content: center;">%s</div>' % donut([(v / 1140, c) for _, v, c in CATS],
                '<b style="%s font-size: 18px;">••••</b><span style="font-size: 11px; color: %s;">USD</span>' % (MONO, HERO_HINT), 170, 22),
                ''.join('<div style="display: flex; align-items: center; gap: 10px; margin-top: 10px;"><span style="width: 14px; height: 14px; border-radius: 50%%; background: %s;"></span>'
                        '<span style="flex: 1; font-size: 16px;">%s</span><span style="font-size: 15px; color: %s;">%d %%</span></div>' % (c, n, HERO_HINT, round(v * 100 / 1140)) for n, v, c in CATS),
                mark))
    return '<div style="padding: 40px 16px;">%s</div>' % hero_card(inner, pad='24px', extra='min-height: 700px;')


def share_choice():
    """Variant Б: «Поделиться» first asks how — amounts hidden (default) or shown — in the small sheet."""
    body = ('<div style="padding: 8px 16px 24px;"><div style="font-size: 20px; font-weight: 700; margin-bottom: 12px;">Поделиться итогами</div>'
            '<div style="display: flex; flex-direction: column; gap: 8px;">%s%s</div>%s</div>' % (
        row_line('Без сумм', 'Проценты и слова — безопаснее для чата', True), row_line('С суммами', 'Как на экране', False), button('Поделиться')))
    return share_screen('<div style="height: 560px;"></div>', sheet(body, top=480))


def row_line(title, sub, on):
    edge = 'box-shadow: inset 0 0 0 2px %s;' % LIME if on else 'box-shadow: inset 0 0 0 1px %s;' % LINE2
    return ('<div style="padding: 10px 16px; border-radius: 999px; %s"><div style="font-size: 16px; font-weight: %d;">%s</div>'
            '<div style="font-size: 13px; color: %s;">%s</div></div>' % (edge, 700 if on else 400, title, MUTED, sub))


def streak():
    """Story «Серия записей»: the longest run of days with entries in the month; no pressure, a missed day is not mentioned."""
    days = ''.join('<div style="flex: 1; height: 28px; border-radius: 8px; background: %s;"></div>' % (LIME if i in range(6, 19) else LINE2) for i in range(31))
    grid = '<div style="display: flex; gap: 3px;">%s</div>' % days
    return story(5, 'Записывали 13 дней подряд', grid + '<div style="margin-top: 16px; font-size: 28px; font-weight: 700;">7–19 октября</div>', 'Самая длинная серия месяца')


def coffee():
    """Story «Кафе в чашках»: the month's «Кафе» total in typical «Кафе» entries (median of the last 3 months) — offline, own data."""
    cups = ''.join('<span style="display: inline-block; width: 22px; height: 22px; border-radius: 50%%; margin: 0 4px 4px 0; background: %s;"></span>' % '#FFB86B' for _ in range(34))
    return story(4, 'На кафе ушло как 34 обычные траты в кафе', '<div style="max-width: 300px;">%s</div>' % cups, 'Обычная трата в кафе — 3 500 ARS, это ваша середина за 3 месяца')


BOARDS = [
    ('Rec554Top', 'Истории «А»: 1 — больше всего ушло на…', top, 'recap'),
    ('Rec554Vs', 'Истории «А»: 2 — сравнение с прошлым месяцем', vs, 'recap'),
    ('Rec554Weeks', 'Истории «А»: 3 — самая дорогая неделя', weeks, 'recap'),
    ('Rec554New', 'Истории «А»: 4 — новая категория месяца', new, 'recap'),
    ('Rec554Weekend', 'Истории «А»: 5 — будни и выходные', weekend, 'recap'),
    ('Rec554Final', 'Истории «А»: 6 — слова по результату и «Подробнее»', final, 'recap'),
    ('Rec554Streak', 'Новая история: серия дней подряд с записями', streak, 'recap'),
    ('Rec554Coffee', 'Новая история: кафе в «обычных тратах» (по своим данным)', coffee, 'recap'),
    ('Rec554Share', 'Поделиться: значок в шапке историй (один нажим → картинка)', share_final, 'recap'),
    ('Rec554ShareImg', 'Поделиться: сама картинка 4:5, суммы скрыты', share_image, 'recap'),
    ('Rec554ShareAsk', 'Поделиться, вариант Б: сначала шторка «Без сумм / С суммами»', share_choice, 'recap'),
]
