"""Generated boards: release stories (#693, owner poll 2026-11-24: card on «Обзор» — «Б», five stories — «А»; poll boards Rel693*).
The card «Версия N · Что нового» stands at the top of «Обзор» once after an update; «Смотреть» opens the stories,
«✕» — not now, for good. Schemes come from `story_art_693.py` — the same paths as the app's vector layers.

ROW — (key, row title, sticky text) for the «Экраны» page; BOARDS — (name, title, fn, key).
"""
from mockup_plan_bricks import *  # noqa: F401,F403
from mockup_screens_main import balance_hero, tiles, tile, recent_card, RECENT, SEARCH_BTN, converter_card
from mockup_screens_notes import footer
import story_art_693 as ART

ROW = ('release', 'Истории релиза',
       'Макет #693 — опрос 2026-11-24: карточка вверху «Обзора» («Б»), пять историй («А»). Карточка — один раз после обновления (метка «новое»: '
       'на новой установке её нет), «Смотреть» и «✕» убирают её навсегда. Истории — полоски, схема, заголовок, 1–2 предложения, «Дальше»/«Готово».')

STORIES = [
    ('bulk', 'Изменить сразу несколько', 'Удерживайте операцию, отметьте ещё и нажмите «Изменить».'),
    ('repeat', 'Повторы и дубли', '«Операции» предложат сделать платёж регулярным и заметят дубль.'),
    ('insights', 'Отчёты стали умнее', 'Необычное за месяц, прогноз остатка и цена конвертации.'),
    ('bar', 'Своя нижняя панель', 'Ещё → Настройки → Экраны → Нижняя панель.'),
    ('widgets', 'Виджеты', 'Расход, доход и шаблоны — в одно нажатие с рабочего стола.'),
]


def notice():
    words = ('<div style="flex: 1;"><div style="font-size: 12px; color: %s;">Версия 26.12.1</div><div style="font-size: 16px; font-weight: 600;">Что нового</div></div>' % MUTED)
    return card('<div style="display: flex; align-items: center; gap: 8px;">%s<span style="flex: none;">%s</span>%s</div>'
                % (words, button('Смотреть', 'primary', None, 0), icon_button('close', 'Не сейчас', INK)), pad='8px 4px 8px 16px', extra='margin-top: 8px;')


def home():
    body = notice() + balance_hero() + tiles(tile('Траты за ноябрь', '1 240,50', 'USD'), tile('Без категории', '14', 'операций')) + converter_card() + recent_card(RECENT)
    return tab_screen('Обзор', body, 'home', SEARCH_BTN)


def bars(at):
    return '<div style="display: flex; gap: 4px;">%s</div>' % ''.join(
        '<div style="flex: 1; height: 8px; border-radius: 999px; background: %s; %s"></div>' % (LIME if i <= at else LINE2, 'box-shadow: inset 0 0 0 1px %s;' % LIME if i <= at else '')
        for i in range(len(STORIES)))


def story(at):
    key, title, text = STORIES[at]
    scheme = '<div style="display: flex; justify-content: center; padding: 32px 0 20px;">%s</div>' % ART.svg(key, INK, LIME, ONLIME)
    inner = scheme + '<div style="font-size: 24px; font-weight: 600; line-height: 1.2;">%s</div><div style="margin-top: 12px; font-size: 16px; color: %s; line-height: 1.45;">%s</div>' % (title, MUTED, text)
    last = at == len(STORIES) - 1
    return inner_screen('Что нового · 26.12.1', bars(at) + story_card(inner, pad='16px', extra='margin-top: 12px; min-height: 560px;')) + \
        footer(button('Готово' if last else 'Дальше', 'primary', None, 0))


BOARDS = [('Rel693Home', '«Обзор»: карточка «Что нового» после обновления', home, 'release')] + [
    ('Rel693S%d' % (i + 1), 'История %d из 5: %s' % (i + 1, STORIES[i][1]), (lambda i=i: story(i)), 'release') for i in range(len(STORIES))]
