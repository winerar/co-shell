"""Proposal boards: the «Ещё» tab tidied up (owner's question 2026-11-20, poll-more-menu.json).

А — today's «Ещё» as in the code (`MoreSections.kt`): version card on top, 14 rows in three groups.
Б — 11 rows in four groups: «Подсказки» and «Валюты итогов» move to «Настройки», «Деньги» split into «Деньги» and «Валюты»,
«Экспорт» (#204) next to «Импорт», a quiet version row at the bottom; the version card goes on top only when an update is out.
Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403

FEEDBACK = [
    (1120, 'Вкладка «Ещё»: меньше строк, четыре группы · 2026-11-20',
     'А — как сейчас (14 строк). Б — 11 строк: «Подсказки» и «Валюты итогов» уходят в «Настройки», «Деньги» и «Валюты» отдельно, '
     '«Экспорт» рядом с «Импортом», версия — тихой строкой внизу (карточкой сверху — только когда вышло обновление).'),
]


def r(title, sub, icon, color, trail=None):
    """`CoShellMenuRow`: every icon on the second colour's tone (#442 Б, `MenuIcon`); `color` is the old per-row colour, ignored."""
    return row(title, sub, tile_icon(icon, VIOLET), trail if trail is not None else chevron(), min_h=56)


def version_card(update=None):
    """`VersionCard`: today — always on top; in Б — only when a newer release is out."""
    if update:
        btn = ('<span style="flex: none; height: 48px; padding: 0 18px; border-radius: 999px; background: %s; color: %s; display: flex; '
               'align-items: center; font-size: 14px; font-weight: 600;">Обновить</span>' % (LIME, ONLIME))
        return group([row('Co-Shell', 'Вышла версия %s' % update, tile_icon('download', VIOLET), btn)])
    return group([r('Co-Shell', 'Версия 1.2 · сборка 512', 'download', LIME)])


# ---- А: as today ------------------------------------------------------------------------------------------------------

def now_money():
    return group([r('Счета', 'Карты, наличные, вклады', 'wallet', PINK), r('Категории', 'Подкатегории, иконки и цвета', 'folder', LIME),
                  r('Бюджеты', 'Лимиты на месяц, план и факт', 'wallet', PINK), r('Регулярные платежи', 'Аренда, подписки, зарплата', 'refresh', CYAN),
                  r('Курсы валют', 'Обновляются сами раз в день', 'rate', CYAN), r('Конвертер', 'Сколько это в других валютах', 'transfer', LIME),
                  r('Валюты итогов', 'Основная: USD', 'rate', VIOLET)])


def now_top():
    return version_card() + label('Деньги', top=14) + now_money()


def now_rest():
    data = group([r('Семья', 'Общий бюджет с близкими', 'user', LIME), r('Резервная копия', 'Сохранить или восстановить данные', 'download', YELLOW),
                  r('Импорт', 'Перенести операции из выгрузки CSV', 'download', CYAN)])
    app = group([r('Настройки', 'Лайм · тёмная', 'spark', VIOLET), r('Подсказки', 'Показываются', 'bulb', CYAN, switch(True)),
                 r('Показать подсказки заново', 'Вернёт «Первые шаги» и советы', 'refresh', YELLOW), r('Обратная связь', 'Ошибка или идея', 'mail', PINK)])
    return label('Семья и данные', top=6) + data + label('Приложение', top=14) + app


# ---- Б: four groups, 11 rows ------------------------------------------------------------------------------------------

def new_money():
    return group([r('Счета', 'Карты, наличные, вклады', 'wallet', PINK), r('Категории', 'Подкатегории, иконки и цвета', 'folder', LIME),
                  r('Бюджеты', 'Лимиты на месяц, план и факт', 'wallet', PINK), r('Регулярные платежи', 'Аренда, подписки, зарплата', 'refresh', CYAN)])


def new_rates():
    return group([r('Курсы валют', 'Обновляются сами раз в день', 'rate', CYAN), r('Конвертер', 'Сколько это в других валютах', 'transfer', LIME)])


def new_data():
    return group([r('Семья', 'Общий бюджет с близкими', 'user', LIME), r('Резервная копия', 'Сохранить или восстановить данные', 'archive', YELLOW),
                  r('Импорт', 'Перенести операции из выгрузки CSV', 'download', CYAN), r('Экспорт', 'Операции за период в CSV', 'upload', VIOLET)])


def new_app():
    # «Конфиденциальность» (#145, board Priv145More) — between «Настройки» and «Обратная связь».
    shield = 'M12 3l8 3v6c0 4.5-3.4 8.2-8 9-4.6-.8-8-4.5-8-9V6zM9 12l2 2 4-4'
    return group([r('Настройки', 'Тема, ввод, уведомления, экраны', 'gear', VIOLET), r('Конфиденциальность', 'PIN, отпечаток, скрытие сумм', shield, CYAN),
                  r('Обратная связь', 'Ошибка или идея', 'mail', PINK)])


def quiet_version():
    return ('<div style="margin: 14px 4px 0; text-align: center; font-size: 13px; color: %s;">Co-Shell · версия 1.2 · актуальная</div>' % MUTED)


def new_top(update=None):
    head = version_card(update) + label('Деньги', top=14) if update else label('Деньги', top=6)
    return head + new_money() + label('Валюты', top=14) + new_rates()


def new_rest():
    return label('Семья и данные', top=6) + new_data() + label('Приложение', top=14) + new_app() + quiet_version()


def settings_hints():
    """Where the moved rows land: a «Помощь» card in «Настройки» (`SettingsCards.kt`) and «Валюты итогов» already in «Деньги и даты»."""
    title = ('<div style="display: flex; align-items: center; gap: 8px; margin: 10px 4px 8px;">%s'
             '<span style="flex-grow: 1; font-size: 13px; font-weight: 600; letter-spacing: 0.04em; text-transform: uppercase; color: %s;">Помощь</span></div>'
             % (dot_icon('bulb', CYAN, 32), MUTED))
    help_ = group([row('Подсказки', 'Показываются', '', switch(True), min_h=52),
                   row('Показать подсказки заново', 'Вернёт «Первые шаги» и советы', '', chevron(), min_h=52)])
    money = ('<div style="display: flex; align-items: center; gap: 8px; margin: 18px 4px 8px;">%s'
             '<span style="flex-grow: 1; font-size: 13px; font-weight: 600; letter-spacing: 0.04em; text-transform: uppercase; color: %s;">Деньги и даты</span>'
             '<span style="font-size: 14px; font-weight: 600; color: %s;">Все ›</span></div>' % (dot_icon('rate', CYAN, 32), MUTED, CYAN))
    money_g = group([row('Валюты итогов', 'USD · ARS · AMD', '', chevron(), min_h=52), row('Неделя начинается с', 'понедельника', '', chevron(), min_h=52)])
    return title + help_ + money + money_g


def a_top():
    return tab_screen('Ещё', now_top(), 'more')


def a_rest():
    return tab_screen('Ещё', now_rest(), 'more')


def b_top():
    return tab_screen('Ещё', new_top(), 'more')


def b_rest():
    return tab_screen('Ещё', new_rest(), 'more')


def b_update():
    return tab_screen('Ещё', new_top('1.3'), 'more', sub=None)


def b_settings():
    return inner_screen('Настройки', settings_hints())


# Picked 2026-11-20: Б (name stays «Ещё»). Board А removed from the canvas (functions stay for the poll page history);
# board «More» (mockup_screens_tabs.py) draws Б.
BOARDS = [  # name, title, fn, issue number
    ('MoreB', '«Ещё» Б: четыре группы', b_top, 1120),
    ('MoreBEnd', '«Ещё» Б: прокрутка', b_rest, 1120),
    ('MoreBUpdate', '«Ещё» Б: вышло обновление', b_update, 1120),
    ('MoreBSettings', '«Ещё» Б: куда ушли подсказки', b_settings, 1120),
]
