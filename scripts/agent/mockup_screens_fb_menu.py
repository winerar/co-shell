"""Boards of the «Ещё» tab: sections as icons (owner's poll 2026-11-28 «А», poll-more-settings.json; was four groups of rows, 2026-11-20 Б).

Two grids (`CoShellMenuGrid`): «Деньги» (8) and «Валюты и семья» (4, «Данные» = backup, import, export — `DataSettingsScreen`),
then three rows of «Приложение»: «Настройки», «Обратная связь», «О приложении» with the version. The whole menu fits one screen;
«Конфиденциальность» moved into «Настройки». Code — `more/MoreScreen.kt`, `more/components/MoreSections.kt`.
Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS); board «More» (mockup_screens_tabs.py) draws the same.
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import pill

FEEDBACK = [
    (1128, 'Вкладка «Ещё»: значки вместо строк · 2026-11-28',
     'Опрос «Ещё и Настройки» — А: разделы сеткой значков по 4 в ряд, «Приложение» — тремя строками; всё на одном экране. '
     'Резервная копия, импорт и экспорт — один раздел «Данные»; «Конфиденциальность» — в «Настройках».'),
]

MONEY = [('wallet', 'Счета', None), ('folder', 'Категории', None), ('budget', 'Бюджеты', None), ('flag', 'Цели', None),
         ('refresh', 'Регулярные', None), ('user', 'Долги', None), ('note', 'Заметки', None), ('draft', 'Черновики', '3')]
REST = [('rate', 'Курсы', None), ('transfer', 'Конвертер', None), ('user', 'Семья', None), ('archive', 'Данные', None)]


def app_rows(update=None):
    version = row('О приложении', 'Вышла версия %s' % update if update else 'Версия 1.2 · сборка 512 · актуальная', tile_icon('info', VIOLET),
                  (pill('новое') + ' ' if update else '') + chevron(), min_h=52)
    return group([row('Настройки', '', tile_icon('gear', VIOLET), chevron(), min_h=52), row('Обратная связь', '', tile_icon('mail', VIOLET), chevron(), min_h=52), version])


def body(update=None):
    return (label('Деньги', top=6) + menu_grid(MONEY) + label('Валюты и семья', top=14) + menu_grid(REST)
            + label('Приложение', top=14) + app_rows(update))


def tiles():
    return tab_screen('Ещё', body(), 'more')


def tiles_update():
    return tab_screen('Ещё', body('1.3'), 'more')


BOARDS = [  # name, title, fn, issue number
    ('MoreTiles', '«Ещё»: значки по 4 в ряд (опрос 2026-11-28 «А»)', tiles, 1128),
    ('MoreTilesUpdate', '«Ещё»: вышло обновление — «новое» у «О приложении»', tiles_update, 1128),
]
