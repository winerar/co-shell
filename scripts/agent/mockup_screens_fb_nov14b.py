"""Generated boards: feedback of 2026-11-13/14, part 2 — bank statements → drafts (#331, #332), per-screen settings (#333),
incompatible version gate (#334), «новое» mark (#335), screenshots in feedback (#336). Variants for the owner's poll.

Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS). Long lists on purpose: 18 drafts, 24 payee rules.
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import pill, textarea, footer, nav_tile, grid, LIMEBG, CYANBG, VIOLETBG, YELLOWBG
from mockup_screens_main import balance_hero, tile, tiles, recent_card, RECENT
from mockup_screens_fb_cats import circle

FEEDBACK = [
    (331, 'Отзывы #331, #332 · выписка или чек через «Поделиться» → черновики',
     'Знакомые получатели проводятся сами, новые — черновиками; сопоставление «получатель → категория» запоминается. Рекомендую А: список черновиков.'),
    (333, 'Отзыв #333 · настройки прямо на экране', 'Рекомендую А: значок настройки в заголовке → шторка «Вид экрана», те же пункты — в «Настройках».'),
    (334, 'Отзыв #334 · вторая половина семьи на новой версии', 'Рекомендую А: смотреть можно, вносить — после обновления; ничего не теряется.'),
    (335, 'Отзыв #335 · метка «новое» на неделю', 'Рекомендую А: пилюля «новое» у пункта; пропадает через 7 дней или после первого открытия.'),
    (336, 'Отзыв #336 · скриншоты в обращении', 'Рекомендую А: до 3 картинок плитками + «последний скриншот» одним нажатием.'),
]

TUNE = 'M4 7h10M18 7h2M4 17h4M12 17h8M14 4v6M8 14v6'
DRAFTS = [  # payee, amount, cur, category or '', icon, colour rgb, flag
    ('COTO SUC 115', '−32 450', 'ARS', 'Продукты', 'cart', '168,255,62', ''), ('MERPAGO*UBER', '−6 800', 'ARS', 'Такси', 'taxi', '255,212,59', ''),
    ('FARMACITY 0231', '−12 900', 'ARS', '', '', '', 'new'), ('CAFE MARTINEZ', '−9 200', 'ARS', 'Кафе', 'cafe', '255,181,71', 'dup'),
    ('NETFLIX.COM', '−7 999', 'ARS', 'Подписки', 'film', '255,79,216', ''), ('TRANSF. A. PETROVA', '+150 000', 'ARS', '', '', '', 'new'),
    ('DIA %', '−18 300', 'ARS', 'Продукты', 'cart', '168,255,62', ''), ('YPF 4471', '−41 000', 'ARS', '', '', '', 'new'),
]


def draft_row(d):
    payee, amt, cur, cat, icon, col, flag = d
    if cat:
        lead, sub = circle(icon, col, 36), cat + (' · по правилу' if not flag else '')
    else:
        lead = '<span style="width: 36px; height: 36px; border-radius: 50%%; border: 1px dashed %s; box-sizing: border-box; flex: none;"></span>' % WARN
        sub = '<span style="color: %s;">Выберите категорию</span>' % WARN
    if flag == 'dup':
        sub = '<span style="color: %s;">Похоже на дубль: уже есть «Кафе» −9 200 ARS вчера</span>' % PINK
    color = OK if amt.startswith('+') else INK
    return row('<span style="%s font-size: 14px;">%s</span>' % (MONO, payee), sub, lead, amount(amt, cur, color, 14), min_h=56)


def head_card():
    return hero_card('<div style="font-size: 12px; color: %s;">Galicia · выписка 1–14 нояб.</div><div style="display: flex; gap: 14px; margin-top: 6px; font-size: 13px;">'
                     '<span><b style="%s font-size: 22px; color: %s;">12</b> проведены сами</span><span><b style="%s font-size: 22px; color: %s;">8</b> ждут</span>'
                     '<span><b style="%s font-size: 22px; color: %s;">1</b> дубль</span></div>' % (HERO_HINT, MONO, LIME, MONO, INK, MONO, PINK), pad='12px 14px')


def drafts_a():
    body = head_card() + label('Сегодня · 4') + group([draft_row(d) for d in DRAFTS[:4]]) + label('Вчера · 4') + group([draft_row(d) for d in DRAFTS[4:]])
    return inner_screen('Черновики · 8', body, icon_button('search', 'Поиск')) + footer(button('Провести 5 с категорией', 'primary', 'check', 0))


def rules_a():
    rules = [('COTO*', 'Продукты', 'cart', '168,255,62', 14), ('DIA %', 'Продукты', 'cart', '168,255,62', 9), ('MERPAGO*UBER', 'Такси', 'taxi', '255,212,59', 22),
             ('CABIFY', 'Такси', 'taxi', '255,212,59', 4), ('NETFLIX.COM', 'Подписки', 'film', '255,79,216', 3), ('SPOTIFY', 'Подписки', 'film', '255,79,216', 3),
             ('CAFE MARTINEZ', 'Кафе', 'cafe', '255,181,71', 7), ('FARMACITY*', 'Аптека', 'pill', '255,84,112', 5), ('PEDIDOSYA', 'Доставка', 'taxi', '255,165,61', 11)]
    rows = [row('<span style="%s font-size: 14px;">%s</span>' % (MONO, p), '→ %s · %d раз' % (c, n), circle(i, col, 34), chevron(), min_h=52) for p, c, i, col, n in rules]
    return inner_screen('Получатели → категории', search('Найти получателя') + label('24 правила · проводить сами') + group(rows)
                        + note('Правило появляется, когда вы выбрали категорию черновику. «*» — любое окончание (номер кассы, город).'))


def card_b():
    cats = ''.join(chip(n, n == 'Аптека', dot=c) for n, c in (('Аптека', '#FF5470'), ('Врачи', '#FF8FA3'), ('Продукты', LIME), ('Красота', '#FFB38A'))) + chip('Все 34', dashed=True)
    big = card('<div style="font-size: 12px; color: %s;">Galicia · 13 нояб., 18:42</div><div style="%s font-size: 18px; margin-top: 8px;">FARMACITY 0231</div>'
               '<div style="%s font-size: 36px; font-weight: 700; color: %s; margin: 6px 0 14px;">−12 900 <span style="font-size: 16px; color: %s;">ARS</span></div>'
               '<div style="font-size: 13px; color: %s; margin-bottom: 8px;">Категория</div><div style="display: flex; gap: 6px; flex-wrap: wrap;">%s</div>'
               '<div style="display: flex; align-items: center; gap: 10px; margin-top: 14px; font-size: 13px;">%s<span>Запомнить для FARMACITY*</span></div>'
               % (MUTED, MONO, MONO, INK, MUTED, MUTED, cats, switch(True)), pad='16px', radius=24)
    prog = '<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 12px; font-size: 13px; color: %s;">3 из 8%s</div>' % (MUTED, progress(0.37, 0).replace('margin-top: 0px;', 'flex-grow: 1;'))
    btns = '<div style="display: grid; grid-template-columns: 1fr 2fr; gap: 8px;">%s%s</div>' % (button('Пропустить', 'outline', mt=0), button('Провести', 'primary', 'check', 0))
    return inner_screen('Черновики', prog + big + '<div style="height: 12px;"></div>' + btns + note('Свайп влево — пропустить, вправо — провести.'))


def share_done():
    rows = group([row('Проведены сами', 'знакомые получатели', svg('check', OK), '<b style="%s">12</b>' % MONO, min_h=52),
                  row('Черновики', 'нужно выбрать категорию', svg('note', WARN), '<b style="%s">8</b>' % MONO, min_h=52),
                  row('Дубли', 'уже есть в co-shell — пропустили', svg('copy', PINK), '<b style="%s">1</b>' % MONO, min_h=52)])
    body = hero('file', 'Выписка разобрана', 'Galicia · PDF · 21 строка · 1–14 ноября') + '<div style="margin-top: 22px;">%s</div>' % rows
    return inner_screen('Из «Поделиться»', body) + footer(button('Открыть черновики', 'primary', mt=0))


def unparsed():
    masked = ('<div style="%s font-size: 12px; line-height: 1.6; color: %s; padding: 10px 12px; border-radius: 12px; background: %s;">'
              'BANCO ****** — RESUMEN<br>CUENTA ****-****-1234<br>13/11 COMPRA ******** $99.999,99<br>14/11 TRANSF ****** $99.999,99</div>' % (MONO, INK2, BAR))
    body = (hero('alert', 'Не узнали этот документ', 'Ameria · PDF. Такой формат мы ещё не читаем.', WARN)
            + label('Можно прислать образец без личных данных') + masked
            + note('Имена, номера и суммы заменены звёздочками и 9-ками — остаётся только расположение строк. Придёт задачей, после разбора формат станет читаться.'))
    return inner_screen('Из «Поделиться»', body) + footer(button('Отправить образец', 'primary', 'upload', 0), button('Не нужно', 'text', mt=6))


# ---- #333 per-screen settings ---------------------------------------------------------------------------------------

def ops_list():
    rows = ''.join(tx(*r) for r in RECENT[:5])
    return '<div style="margin: 6px -16px 0;">%s%s</div>' % (day('СЕГОДНЯ', '−45 100', 'ARS'), rows)


def screen_opts():
    return group([row('Группировать', 'по дням', '', '<span style="color: %s;">Дни ▾</span>' % LIME, min_h=50), row('Итог дня в заголовке', '', '', switch(True), min_h=50),
                  row('Вторая валюта под суммой', '≈ USD', '', switch(True), min_h=50), row('Плотные строки', '', '', switch(False), min_h=50),
                  row('Переводы в списке', '', '', switch(True), min_h=50)])


def settings_a():
    under = tab_screen('Операции', search('Поиск: заметка, сумма, категория') + ops_list(), 'ops', icon_button(TUNE, 'Вид экрана'))
    inner = ('<div style="padding: 8px 16px;"><div style="display: flex; align-items: center; height: 44px;"><b style="flex-grow: 1; font-size: 20px;">Вид «Операций»</b>'
             '<span style="color: %s; font-weight: 600;">Готово</span></div>%s<div style="margin-top: 12px; display: flex; align-items: center; gap: 6px; color: %s; font-size: 14px; font-weight: 600;">'
             '%sВсе настройки приложения</div></div>' % (LIME, screen_opts(), LIME, svg('gear', LIME, 18)))
    return under + scrim() + sheet(inner, 330)


def settings_all():
    screens = [('home', 'Обзор', 'плитки, порядок, герой'), ('list', 'Операции', 'группировка, итоги, плотность'), ('wallet', 'Счета', 'группы, архив, порядок'),
               ('budget', 'Бюджеты', 'период, показывать прогноз'), ('chart', 'Отчёты', 'период, валюта'), ('folder', 'Категории', 'порядок, частые сверху')]
    others = [('spark', 'Внешний вид', 'тема, палитра, крупный шрифт'), ('plus', 'Ввод', 'чаевые, черновик, буфер, «−»'), ('bolt', 'Жесты', 'встряхнуть, экраном вниз')]
    return inner_screen('Настройки', search('Найти настройку: итог, тема, жест') + label('Экраны · у каждого значок ⚙ в заголовке')
                        + group([row(t, s, svg(i), chevron(), min_h=52) for i, t, s in screens]) + label('Приложение') + group([row(t, s, svg(i), chevron(), min_h=52) for i, t, s in others]))


def settings_b():
    return inner_screen('Операции · вид', label('Настройки → Экраны → Операции') + screen_opts()
                        + note('Только в «Настройках»: на самих экранах значков нет — чище, но дальше идти (3 нажатия).'))


# ---- #334 version gate ---------------------------------------------------------------------------------------------

def home_body(extra):
    return extra + balance_hero() + tiles(tile('Траты за ноябрь', '2 140', 'USD'), tile('Бюджет · 17 дней', '860', 'осталось')) + recent_card(RECENT[:3])


def nav_locked():
    return nav('home').replace('background: %s; display: flex; align-items: center; justify-content: center;">%s</span></a>' % (LIME, svg('plus', ONLIME, 26, 2.2)),
                               'background: %s; display: flex; align-items: center; justify-content: center;">%s</span></a>' % (CHIP, svg('lock', MUTED, 22, 2)))


def gate_a():
    banner = ('<div style="padding: 12px 14px; border-radius: 16px; background: %s; border: 1px solid %s; margin-bottom: 12px;"><div style="display: flex; gap: 8px; align-items: center; '
              'font-weight: 600; color: %s; font-size: 14px;">%sНужна версия 0.41</div><div style="font-size: 13px; line-height: 1.4; margin-top: 4px;">Аня обновилась, и её новые данные '
              'эта версия не понимает. Смотреть можно, вносить — после обновления: так ничего не испортится.</div><div style="display: flex; justify-content: flex-end; margin-top: 10px;">'
              '<span style="height: 36px; padding: 0 16px; border-radius: 999px; background: %s; color: #2A1E08; font-weight: 600; font-size: 13px; display: flex; align-items: center;">Обновить · 5,6 МБ</span></div></div>'
              % (WARNBG, WARNLINE, WARN, svg('download', WARN, 18), WARN))
    return tab_screen('Обзор', home_body(banner), 'home', bar=False) + nav_locked()


def gate_b():
    body = hero('download', 'Обновите приложение', 'Аня уже на версии 0.41 — данные изменились. Пока вы не обновитесь, приложение закрыто, чтобы ничего не испортить.', WARN, True, 120)
    return '<div style="padding: 0 16px;">%s</div>' % body + footer(button('Обновить · 5,6 МБ', 'primary', 'download', 0), button('Что нового', 'text', mt=6))


# ---- #335 «новое» --------------------------------------------------------------------------------------------------

def new_pill():
    return '<span style="height: 20px; padding: 0 8px; border-radius: 999px; background: %s; color: %s; font-size: 11px; font-weight: 700; display: inline-flex; align-items: center; margin-left: 6px; vertical-align: 2px;">новое</span>' % (LIME, ONLIME)


def new_dot():
    return '<span style="width: 8px; height: 8px; border-radius: 50%%; background: %s; display: inline-block; margin-left: 8px; vertical-align: 3px;"></span>' % LIME


def more_screen(mark, tab_dot=False):
    def g(title, rows):
        return label(title) + group([row(t + (mark if m else ''), s, svg(i), chevron(), min_h=52) for i, t, s, m in rows])
    body = (g('Деньги', [('folder', 'Категории', '34 · 6 родителей', False), ('rate', 'Курсы валют', 'обновлены сегодня', False), ('budget', 'Бюджеты', '18 лимитов', False),
                        ('file', 'Черновики', '8 ждут категории', True)])
            + g('Семья и данные', [('user', 'Семья', 'синхронизировано · 2 чел.', False), ('download', 'Резервная копия', 'авто · 3 ноября', False), ('mail', 'Обратная связь', 'скриншоты в обращении', True)])
            + g('Приложение', [('spark', 'Внешний вид', 'тема Вольт', False), ('gear', 'Настройки', 'экраны, ввод, жесты', True)]))
    screen = tab_screen('Ещё', body, 'more', bar=False) + nav('more', tab_dot)
    return screen


def new_a():
    return more_screen(new_pill())


def new_b():
    tip = ('<div style="position: absolute; left: 96px; top: 318px; padding: 8px 12px; border-radius: 12px; background: %s; border: 1px solid %s; font-size: 12px; max-width: 220px; '
           'box-shadow: 0 10px 24px rgba(0,0,0,0.5);">Новое в 0.41: черновики из выписок</div>' % (CHIP, LINE2))
    return more_screen(new_dot(), True) + tip


# ---- #336 screenshots ----------------------------------------------------------------------------------------------

def shot(h):
    return ('<span style="position: relative; height: 112px; border-radius: 12px; background: linear-gradient(180deg, %s, %s); border: 1px solid %s; overflow: hidden;">'
            '<span style="position: absolute; left: 8px; right: 8px; top: 10px; height: 8px; border-radius: 4px; background: %s;"></span>'
            '<span style="position: absolute; left: 8px; right: 20px; top: %dpx; height: 26px; border-radius: 6px; background: %s;"></span>'
            '<span style="position: absolute; right: 4px; top: 4px; width: 20px; height: 20px; border-radius: 50%%; background: rgba(0,0,0,0.7); display: flex; align-items: center; justify-content: center;">%s</span></span>'
            % (CARD, BAR, LINE2, LINE2, h, LIMEPILL, svg('close', INK, 12, 2.4)))


def fb_form(attach):
    chips = '<div style="display: flex; gap: 8px;">%s</div>' % (pill('Ошибка').replace('height: 24px', 'height: 36px').replace('padding: 0 10px', 'padding: 0 16px') + chip('Идея') + chip('Дизайн') + chip('Другое'))
    area = card('<div style="font-size: 13px; font-weight: 600; color: %s;">Что случилось и где?</div>%s' % (LIME, textarea('', 58, 'После импорта Wallet список счетов не листается дальше Binance.').replace('min-height: 180px', 'min-height: 110px')), pad='14px', extra='margin-top: 14px;')
    return inner_screen('Обратная связь', chips + area + attach + note('Приложим: версия 0.41 · Xiaomi · Android 16 · экран «Счета»')) + footer(button('Отправить', 'primary', 'upload', 0))


def shots_a():
    add = ('<span style="height: 112px; border-radius: 12px; border: 1px dashed %s; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px; color: %s; font-size: 12px; font-weight: 600;">%sЕщё</span>' % (LINE2, LIME, svg('plus', LIME, 20, 2.2)))
    last = ('<div style="display: flex; align-items: center; gap: 8px; margin-top: 8px; padding: 8px 10px; border-radius: 12px; background: %s; font-size: 13px;">%s'
            '<span style="flex-grow: 1;">Последний скриншот · 2 мин назад</span><span style="color: %s; font-weight: 600;">Приложить</span></div>' % (BAR, svg('download', MUTED, 16), LIME))
    grid_ = '<div style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 8px;">%s%s%s</div>' % (shot(40), shot(60), add)
    return fb_form(label('Скриншоты · 2 из 3') + grid_ + last)


def shots_b():
    att = ('<div style="display: flex; gap: 8px; flex-wrap: wrap; margin-top: 14px;">%s%s</div>'
           % (chip('Screenshot_1114_1802.jpg · 180 КБ', icon='file'), chip('Приложить скриншот', icon='plus', dashed=True)))
    return fb_form(att)


BOARDS = [  # name, title, fn, issue number
    ('Fb331A', 'А: черновики списком — по дням, категория по правилу, дубль помечен', drafts_a, 331),
    ('Fb331ARules', 'А: «Получатели → категории» — 24 правила, правка', rules_a, 331),
    ('Fb331B', 'Б: черновики по одному — карточка, свайп «провести/пропустить»', card_b, 331),
    ('Fb331Done', 'Общее: итог разбора после «Поделиться»', share_done, 331),
    ('Fb332', 'Общее (#332): не узнали документ — прислать обезличенный образец', unparsed, 331),
    ('Fb333A', 'А: ⚙ в заголовке экрана → шторка «Вид «Операций»»', settings_a, 333),
    ('Fb333All', 'Общее: «Настройки» — раздел «Экраны» + поиск', settings_all, 333),
    ('Fb333B', 'Б: только в «Настройках → Экраны»', settings_b, 333),
    ('Fb334A', 'А: смотреть можно, ввод закрыт до обновления', gate_a, 334),
    ('Fb334B', 'Б: приложение закрыто экраном «Обновите»', gate_b, 334),
    ('Fb335A', 'А: пилюля «новое» у пункта на 7 дней', new_a, 335),
    ('Fb335B', 'Б: точка у пункта и на вкладке «Ещё» + подсказка', new_b, 335),
    ('Fb336A', 'А: до 3 скриншотов плитками + «последний скриншот»', shots_a, 336),
    ('Fb336B', 'Б: вложение чипом с именем файла', shots_b, 336),
]
