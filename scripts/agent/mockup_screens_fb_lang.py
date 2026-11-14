"""Generated boards: app language (#352) — follows the phone, changeable in «Внешний вид», asked on first launch
and once after the update (so nobody's language changes silently). Variants for the owner's poll.

Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import footer
from mockup_screens_main import balance_hero, tile, tiles, recent_card, RECENT

FEEDBACK = [
    (352, 'Задача #352 · язык приложения', 'По системе, выбор в «Внешнем виде» и при первом запуске. Рекомендую А: короткая шторка поверх «Обзора».'),
]

GLOBE = 'M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18zM3 12h18M12 3c2.5 2.7 3.8 5.7 3.8 9s-1.3 6.3-3.8 9c-2.5-2.7-3.8-5.7-3.8-9S9.5 5.7 12 3z'
LANGS = [  # title, subtitle
    ('Как в телефоне', 'сейчас — Русский'),
    ('Русский', ''),
    ('English', 'английский'),
    ('Español', 'испанский'),
]


def radio(on):
    if on:
        return ('<span style="width: 22px; height: 22px; border-radius: 50%%; border: 2px solid %s; box-sizing: border-box; display: flex; '
                'align-items: center; justify-content: center; flex: none;"><span style="width: 10px; height: 10px; border-radius: 50%%; background: %s;"></span></span>' % (LIME, LIME))
    return '<span style="width: 22px; height: 22px; border-radius: 50%%; border: 2px solid %s; box-sizing: border-box; flex: none;"></span>' % MUTED2


def lang_list(selected=0, items=LANGS):
    return group([row(t, s, '', radio(i == selected), min_h=56) for i, (t, s) in enumerate(items)])


def home_under():
    return tab_screen('Обзор', balance_hero() + tiles(tile('Траты за ноябрь', '2 140', 'USD'), tile('Бюджет · 17 дней', '860', 'осталось')) + recent_card(RECENT[:3]), 'home')


def sheet_head(title, text):
    return ('<div style="display: flex; gap: 12px; align-items: center; padding: 14px 20px 0;">%s<h2 style="margin: 0; font-size: 20px;">%s</h2></div>'
            '<p style="margin: 8px 20px 4px; font-size: 14px; line-height: 1.45; color: %s;">%s</p>' % (svg(GLOBE, LIME, 26, 1.8), title, MUTED, text))


def first_sheet():
    """A (recommended): a short sheet over «Обзор» on the very first start; the phone's language is preselected."""
    body = (sheet_head('Язык приложения', 'Выбрали язык телефона. Сменить можно в любой момент: «Ещё → Внешний вид».')
            + '<div style="padding: 0 16px;">%s</div>' % lang_list(0)
            + footer(button('Готово', 'primary', 'check', 0)))
    return scrim(home_under()) + sheet(body, top=330)


def first_screen():
    """Б: a full welcome screen before anything else — room for the future setup steps (#353)."""
    body = (hero(GLOBE, 'Язык · Language · Idioma', 'Выбрали язык телефона. Сменить можно в любой момент: «Ещё → Внешний вид».', top=28)
            + '<div style="margin-top: 22px;">%s</div>' % lang_list(0)
            + '<div style="text-align: center; margin-top: 14px; font-size: 12px; color: %s;">Шаг 1 из 1 · дальше появятся валюта и семья (#353)</div>' % MUTED)
    return '<div style="padding: 0 16px;">%s</div>' % body + footer(button('Продолжить', 'primary', None, 0))


def appearance_under():
    body = (label('Язык', top=8) + group([row('Язык', 'Как в телефоне · Русский', svg(GLOBE, INK, 22), chevron(), min_h=60)])
            + label('Тема') + segmented(['Системная', 'Светлая', 'Тёмная'], 2)
            + label('Сочные · яркий неон') + card('<div style="height: 120px;"></div>')
            + label('Нежные · пастель') + card('<div style="height: 120px;"></div>'))
    return inner_screen('Внешний вид', body)


def settings():
    """Common: «Ещё → Внешний вид» gets a «Язык» row at the top; tap → the same list in a sheet."""
    body = (sheet_head('Язык приложения', 'Экран обновится сразу. Даты и названия месяцев — тоже на выбранном языке.')
            + '<div style="padding: 0 16px;">%s</div>' % lang_list(3))
    return scrim(appearance_under()) + sheet(body, top=360)


def after_update():
    """Common: once after the update for those already using the app — nothing changes unless they choose so."""
    items = [('Оставить русский', 'как сейчас'), ('Как в телефоне', 'сейчас — Español'), ('English', 'английский'), ('Español', 'испанский')]
    body = (sheet_head('Приложение говорит на трёх языках', 'Русский, English и Español. Оставим русский или переключимся на язык телефона?')
            + '<div style="padding: 0 16px;">%s</div>' % lang_list(0, items)
            + footer(button('Готово', 'primary', 'check', 0)))
    return scrim(home_under()) + sheet(body, top=300)


BOARDS = [  # name, title, fn, issue number
    ('Fb352A', 'А: первый запуск — шторка поверх «Обзора», язык телефона уже выбран', first_sheet, 352),
    ('Fb352B', 'Б: первый запуск — отдельный экран приветствия (задел под шаги #353)', first_screen, 352),
    ('Fb352Set', 'Общее: «Внешний вид» → строка «Язык» → шторка со списком', settings, 352),
    ('Fb352Update', 'Общее: один раз после обновления — по умолчанию «Оставить русский»', after_update, 352),
]
