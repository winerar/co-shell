"""Generated boards: «Быстрые заметки» и «Черновики» (#586, stage 3; owner polls 2026-11-21: two steps «заметка → Черновики» = «А»,
drafts only on this phone = «А», a draft opens the usual entry sheet = «А», «Записать» takes the ready ones = «А»).

Notes: one card with the lines, «Обработать · N» turns every line into a draft and empties the note. Drafts: a list with a check mark each
(ready ones are ticked), a grey caption of where the category came from, marks «Нужна сумма» / «Выберите категорию» on the unready,
«Записать N» posts the ticked ones; any row can be ticked, then «Удалить · N» stands beside «Записать» (#628, poll 2026-11-23 «А»).
A category from the AI carries «бета» (#643 «А»). The row «Быстрые заметки» is always on «Обзор» (#641 «Б»), drafts are also in «Ещё → Деньги».
ROW — (key, row title, sticky text); BOARDS — (name, title, fn, key).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_catalog_bricks import status_chip
from mockup_screens_main import balance_hero, tiles, tile, recent_card, RECENT, SEARCH_BTN, converter_card
import mockup_screens_fb_menu as menu

ROW = ('notes', 'Быстрые заметки и черновики',
       'Макет #586 (владелец 2026-11-21): заметка — строка = трата («кофе 3500», «вчера аптека 4200», «хлеб 850 карта»). «Обработать» делает из строк черновики '
       'и очищает заметку. Черновики живут только на этом телефоне; нажмите на строку — откроется обычная шторка ввода. «Записать N» проводит отмеченные '
       '(готовы: есть сумма и категория), остальные ждут с пометкой. Вход — строка на «Обзоре» и «Ещё → Деньги → Черновики».')

LINES = ['кофе 3500', 'такси 2000', 'хлеб 850 карта', 'вчера аптека 4200']
# title, sub, amount, category, how it was found ('hist' — past entries, 'spark' — AI, 'dict' — by name), ready
DRAFTS = [('Кофе', 'Galicia · сегодня', '−3 500', 'Кафе', 'hist'), ('Такси', 'Galicia · сегодня', '−2 000', 'Такси', 'hist'),
          ('Хлеб', 'Наличные · сегодня', '−850', 'Продукты', 'dict'), ('Аптека', 'Galicia · вчера', '−4 200', 'Здоровье', 'spark')]


# The category's small badge in the caption (`CategoryBadge`, size `Sizes.categoryMini` 18 dp; poll 2026-11-24 «А»).
CAT_LOOK = {'Кафе': ('cafe', WARN), 'Такси': ('taxi', CYAN), 'Продукты': ('cart', LIME), 'Здоровье': ('pill', PINK)}


def caption(text, src):
    """The grey «by past entries / by name / by AI» caption of an auto-picked category (`CategorySource`); the spark marks «made by AI»."""
    spark = svg('spark', LIME, 12, 2) if src == 'spark' else ''
    word = {'hist': 'по прошлым операциям', 'dict': 'по названию', 'spark': 'предложено ИИ'}[src]
    beta = '<span style="margin-left: 4px;">%s</span>' % status_chip('бета', 'manual') if src == 'spark' else ''
    icon, color = CAT_LOOK.get(text, ('folder', LIME))
    return ('<span style="display: flex; align-items: center; gap: 6px; font-size: 12px; color: %s;">%s<span style="display: flex; align-items: center; gap: 4px;">%s%s · %s%s</span></span>'
            % (MUTED, dot_icon(icon, color, 18), spark, text, word, beta))


def mark(text):
    """CoShellStatusChip: a dot in the attention tone and the words."""
    return ('<span style="display: inline-flex; align-items: center; gap: 6px; height: 22px; padding: 0 8px; border-radius: 999px; background: %s; font-size: 12px;">'
            '<span style="width: 6px; height: 6px; border-radius: 50%%; background: %s;"></span>%s</span>' % (mix(WARN, 18), WARN, text))


def tick(on):
    """CoShellCheckMark: a ticked square in the theme fill, or an empty one."""
    if on:
        return ('<span style="width: 22px; height: 22px; flex: none; border-radius: 7px; background: %s; box-shadow: inset 0 0 0 2px %s; display: flex; align-items: center; '
                'justify-content: center;">%s</span>' % (LIME, INK, svg('check', ONLIME, 16, 3)))
    return '<span style="width: 22px; height: 22px; flex: none; border-radius: 7px; border: 2px solid %s; box-sizing: border-box;"></span>' % OUTLINE


def draft(title, sub, amt, cat=None, src='hist', on=True, need=None):
    """One draft row (`DraftRow`): tick, title, mini badge of the account + account · day, then the category caption or the mark of what is missing; amount on the right."""
    under = mark(need) if need else caption(cat, src)
    money = amount(amt, '', INK, 15) if amt else '<span style="font-size: 15px; color: %s;">—</span>' % MUTED
    return ('<div style="display: flex; align-items: center; gap: 12px; min-height: 64px; padding: 6px 16px; {{div}}">%s<span style="flex-grow: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px;">'
            '<span style="display: flex; align-items: center; gap: 8px;"><span style="flex-grow: 1; font-size: 16px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">%s</span>%s</span>'
            '<span style="display: flex; align-items: center; gap: 4px; font-size: 13px; color: %s; white-space: nowrap;">%s<span>%s</span></span>%s</span></div>'
            % (tick(on), title, money, MUTED, mini_badge(ROW_ACCOUNTS.get(sub.split(' · ')[0])), sub, under))


def footer(inner):
    return '<div style="position: absolute; left: 16px; right: 16px; bottom: 16px;">%s</div>' % inner


def note_card(lines, hint):
    text = ''.join('<div style="font-size: 18px; line-height: 1.7;">%s</div>' % l for l in lines)
    caret = '<span style="display: inline-block; width: 2px; height: 20px; margin-left: 2px; vertical-align: middle; background: %s;"></span>' % LIME
    return card('<div style="font-size: 12px; letter-spacing: 0.04em; color: %s;">ЗАМЕТКА</div>%s%s<div style="margin-top: 8px; font-size: 13px; color: %s;">%s</div>'
                % (MUTED, text, '<div style="height: 14px;">%s</div>' % caret, MUTED, hint), pad='14px 16px', extra='min-height: 260px;')


def notes_input():
    body = note_card(LINES, 'Каждая строка — одна трата. Счёт и день — по желанию.')
    return inner_screen('Быстрые заметки', '<div style="margin-top: 8px;">%s</div>' % body) + footer(button('Обработать · 4', 'primary', 'spark', 0))


def notes_empty():
    body = card('<div style="font-size: 12px; letter-spacing: 0.04em; color: %s;">ЗАМЕТКА</div><div style="margin-top: 8px; font-size: 18px; color: %s;">кофе 3500</div>'
                '<div style="margin-top: 6px; font-size: 13px; color: %s;">Запишите траты строками, например «такси 2000» или «вчера аптека 4200». Потом нажмите «Обработать».</div>' % (MUTED, MUTED2, MUTED),
                pad='14px 16px', extra='min-height: 260px;')
    return inner_screen('Быстрые заметки', '<div style="margin-top: 8px;">%s</div>' % body) + footer(button('Обработать', 'primary', 'spark', 0, True))


def drafts_body(rows, summary, hint):
    cap = '<div style="margin: 8px 20px 6px; font-size: 13px; color: %s;">%s</div>' % (MUTED, summary)
    return cap + group(rows) + '<div style="margin: 8px 20px 0; font-size: 13px; color: %s;">%s</div>' % (MUTED, hint)


def drafts():
    rows = [draft(*d) for d in DRAFTS]
    body = drafts_body(rows, '4 черновика · все готовы', 'Нажмите на строку, чтобы поправить.')
    return inner_screen('Черновики', body) + footer(button('Записать · 4', 'primary', 'check', 0))


def drafts_unready():
    rows = [draft(*DRAFTS[0]), draft('Купил хлеб', 'Galicia · сегодня', '', on=False, need='Нужна сумма'),
            draft('Netflix', 'Galicia · сегодня', '−7 500', on=False, need='Выберите категорию'), draft(*DRAFTS[3])]
    body = drafts_body(rows, '4 черновика · готовы 2', 'Нажмите на строку, чтобы дописать сумму или выбрать категорию.')
    return inner_screen('Черновики', body) + footer(button('Записать · 2', 'primary', 'check', 0))


def drafts_long():
    names = ['Кофе', 'Такси', 'Хлеб', 'Аптека', 'Disco', 'Kiosco', 'Netflix', 'YPF', 'Uber', 'Farmacity', 'Carrefour', 'Starbucks', 'Spotify', 'Cabify', 'Dia', 'Libros', 'Subte', 'Kiosco 2']
    cats = ['Кафе', 'Такси', 'Продукты', 'Здоровье']
    rows = []
    for i, n in enumerate(names):
        acc = ['Galicia', 'Наличные', 'Mercado Pago'][i % 3]
        sub = '%s · %s' % (acc, ['сегодня', 'вчера', '17 нояб.'][i % 3])
        amt = '−%s' % '{:,}'.format(900 + 713 * i).replace(',', ' ')
        if i in (6, 11):
            rows.append(draft(n, sub, '', on=False, need='Нужна сумма'))
        elif i == 9:
            rows.append(draft(n, sub, amt, on=False, need='Выберите категорию'))
        else:
            rows.append(draft(n, sub, amt, cats[i % 4], ['hist', 'dict', 'spark'][i % 3]))
    body = drafts_body(rows, '18 черновиков · готовы 15', 'Нажмите на строку, чтобы поправить.')
    return inner_screen('Черновики', body) + footer(button('Записать · 15', 'primary', 'check', 0))


def drafts_empty():
    return inner_screen('Черновики', empty('draft', 'Черновиков нет. Запишите траты в «Быстрых заметках» — они появятся здесь.'
                                                  + '<div style="margin-top: 12px;">%s</div>' % button('Быстрые заметки', 'outline', 'note', 0)))


def drafts_posted():
    rows = [draft(*DRAFTS[0]), draft(*DRAFTS[1])]
    body = drafts_body(rows, '2 черновика · все готовы', 'Нажмите на строку, чтобы поправить.')
    return inner_screen('Черновики', body) + footer(button('Записать · 2', 'primary', 'check', 0)) + snackbar('Записано: 2', 'Отменить', 96)


def strip(drafts=5):
    """`DraftsStrip` (#641 «Б»): «Быстрые заметки» under the balance, always; «Записать» opens the note, the row — the drafts while any wait."""
    sub = 'Траты строками · черновиков: %d' % drafts if drafts else 'Траты строками: кофе 3500'
    write = ('<span style="height: 40px; padding: 0 16px; border-radius: 999px; background: %s; color: %s; font-weight: 600; font-size: 15px; display: flex; '
             'align-items: center;">Записать</span>' % (LIME, ONLIME))
    return card('<div style="display: flex; align-items: center; gap: 12px;">%s<span style="flex-grow: 1;"><span style="font-size: 16px; font-weight: 600;">Быстрые заметки</span>'
                '<span style="display: block; font-size: 13px; color: %s;">%s</span></span>%s</div>' % (tile_icon('note', CYAN), MUTED, sub, write),
                pad='10px 14px', extra='margin-top: 10px;')


def drafts_delete():
    rows = [draft(*DRAFTS[0], on=False), draft('Тест заметки', 'Galicia · сегодня', '', on=True, need='Нужна сумма'),
            draft(*DRAFTS[1], on=False), draft('Позвонить маме', 'Galicia · сегодня', '', on=True, need='Нужна сумма')]
    body = drafts_body(rows, '4 черновика · готовы 2', 'Нажмите на строку, чтобы дописать сумму или выбрать категорию.')
    two = ('<div style="display: flex; gap: 12px;"><span style="flex: 1;">%s</span><span style="flex: 1;">%s</span></div>'
           % (button('Удалить · 2', 'outline', None, 0), button('Записать', 'primary', None, 0, True)))
    return inner_screen('Черновики · 4', body) + footer(two)


def home_notes():
    body = balance_hero() + strip(0) + tiles(tile('Траты за ноябрь', '1 240,50', 'USD'), tile('Без категории', '14', 'операций')) + converter_card() + recent_card(RECENT)
    return tab_screen('Обзор', body, 'home', SEARCH_BTN)


def home_drafts():
    body = balance_hero() + strip() + tiles(tile('Траты за ноябрь', '1 240,50', 'USD'), tile('Без категории', '14', 'операций')) + converter_card() + recent_card(RECENT)
    return tab_screen('Обзор', body, 'home', SEARCH_BTN)


def more_drafts():
    cnt = ('<span style="display: inline-flex; align-items: center; gap: 6px; height: 22px; padding: 0 8px; border-radius: 999px; background: %s; font-size: 12px;">'
           '<span style="width: 6px; height: 6px; border-radius: 50%%; background: %s;"></span>5</span>' % (mix(LIME, 18), LIME))
    money = group([menu.r('Счета', 'Карты, наличные, вклады', 'wallet', PINK), menu.r('Категории', 'Подкатегории, иконки и цвета', 'folder', LIME),
                   menu.r('Бюджеты', 'Лимиты на месяц, план и факт', 'wallet', PINK), menu.r('Регулярные платежи', 'Аренда, подписки, зарплата', 'refresh', CYAN),
                   menu.r('Быстрые заметки', 'Траты строками: кофе 3500', 'note', VIOLET), menu.r('Черновики', 'Из заметок: проверьте и запишите', 'draft', CYAN, cnt)])
    body = label('Деньги', top=6) + money + label('Валюты', top=14) + menu.new_rates()
    return tab_screen('Ещё', body, 'more')


BOARDS = [  # name, title, fn, row key (the functions already return a whole board)
    ('Notes586Input', 'Быстрые заметки', notes_input, 'notes'),
    ('Notes586Empty', 'Быстрые заметки: пусто', notes_empty, 'notes'),
    ('Notes586Drafts', 'Черновики', drafts, 'notes'),
    ('Notes586Unready', 'Черновики: не всё готово', drafts_unready, 'notes'),
    ('Notes586Long', 'Черновики: длинный список', drafts_long, 'notes'),
    ('Notes586Posted', 'Черновики: записано, «Отменить»', drafts_posted, 'notes'),
    ('Notes586Empty2', 'Черновики: пусто', drafts_empty, 'notes'),
    ('Notes628Delete', 'Черновики: отмечены ненужные, «Удалить · N»', drafts_delete, 'notes'),
    ('Notes586Home', 'Обзор: «Быстрые заметки» с черновиками', home_drafts, 'notes'),
    ('Notes641Home', 'Обзор: «Быстрые заметки» без черновиков', home_notes, 'notes'),
    ('Notes586More', 'Ещё: заметки и черновики', more_drafts, 'notes'),
]
