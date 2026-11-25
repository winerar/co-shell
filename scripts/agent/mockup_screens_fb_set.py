"""Generated boards: «Настройки» and screen customization (owner OK on the list 2026-11-17, docs/design/settings-customization.md).

Scope picked: only the ★ items; the baseline card of «Обзор» can't be hidden; PIN/fingerprint — after 1.0; card layout is per phone.
Static, one state — for the «ок» before code. Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import *  # noqa: F401,F403
from mockup_screens_main import RECENT, balance_hero, tile, tiles
import mockup_screens_fb_setcards as sc

FEEDBACK = [
    (1118, 'Настройки и кастомизация экранов · 2026-11-17',
     'Корень «Настройки» — группы; «Экраны» — Обзор (карточки: показать/скрыть, порядок, «Скоро», «Последние», плитки), Операции, '
     'Отчёты, Счета. Режим правки прямо на «Обзоре» (удержание). Только рекомендованное (★); PIN и отпечаток — после 1.0.'),
]


def r(title, sub, icon, color=LIME, bg=CHIP, trail=None):
    return row(title, sub, tile_icon(icon, color, bg), trail if trail is not None else chevron(), min_h=52)


def handle():
    return svg('drag', MUTED, 22, 2.6)


def reset_link():
    return ('<div style="margin: 18px 0 0; text-align: center; font-size: 14px; font-weight: 600; color: %s;">Вернуть стандартные</div>' % INK2)


def pick_row(title, options, on):
    """A setting with a few values: title over a segmented control, in a card."""
    return card('<div style="padding: 12px 16px;"><div style="font-size: 15px; margin-bottom: 10px;">%s</div>%s</div>' % (title, segmented(options, on)))


def sw(title, sub='', on=True):
    return row(title, sub, '', switch(on), min_h=52)


# ---- root ------------------------------------------------------------------------------------------------------------

def root():
    """Group cards (poll 2026-11-20, В): the main settings of each group inside its card, «Все ›» — the group screen."""
    return inner_screen(sc.RU['title'], sc.cards_top(sc.RU))


def root_more():
    return inner_screen(sc.RU['title'], sc.cards_rest(sc.RU))


def root_es():
    return inner_screen(sc.ES['title'], sc.stress(sc.ES, sc.cards_top(sc.ES)))


# ---- look ------------------------------------------------------------------------------------------------------------

def look():
    body = (label('Язык', top=4) + group([r('Язык', 'Русский · как в телефоне', 'user', CYAN, CYANBG)])
            + label('Тема') + segmented(['Система', 'Тёмная', 'Светлая'], 'Тёмная')
            + label('Палитра') + group([r('Лайм', 'Девять цветовых тем', 'spark', LIME, LIMEBG)])
            + label('Списки') + pick_row('Плотность строк в «Операциях» и «Счетах»', ['Обычная', 'Компактная'], 'Обычная')
            + note('Компактная — больше строк на экране в «Операциях» и «Счетах».', 8))
    return inner_screen('Внешний вид', body)


# ---- screens ---------------------------------------------------------------------------------------------------------

def screens():
    g = group([r('Обзор', '5 карточек · «Скоро» на 7 дней', 'home', LIME, LIMEBG),
               r('Операции', 'Счёт и теги в строке · сумма в валюте счёта', 'list', CYAN, CYANBG),
               r('Отчёты', 'Месяц · сравнение с прошлым', 'chart', VIOLET, VIOLETBG),
               r('Счета', 'По типу · архивные скрыты', 'wallet', YELLOW, YELLOWBG)])
    return inner_screen('Экраны', label('Экраны', top=10) + g)


def card_row(title, sub, on, fixed=False):
    trail = svg('lock', MUTED, 20) if fixed else switch(on)
    if not fixed:
        trail = switch(on)
    lead = '' if fixed else ''
    return ('<div style="display: flex; align-items: center; gap: 12px; min-height: 56px; padding: 8px 8px 8px 12px; box-sizing: border-box; {{div}}">%s'
            '<span style="flex-grow: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px;"><span style="font-size: 15px; color: %s;">%s</span>'
            '<span style="font-size: 12px; color: %s;">%s</span></span>%s</div>'
            % (handle() if not fixed else '<span style="width: 22px;"></span>', INK if on or fixed else MUTED, title, MUTED, sub, trail))


def home_cards():
    g = group([card_row('Баланс', 'всегда сверху', True, fixed=True),
               card_row('Итог месяца', 'в первые дни месяца', True),
               card_row('Скоро', 'регулярные платежи', True),
               card_row('Плитки', 'траты и бюджет', True),
               card_row('Горит в бюджете', 'когда лимит под угрозой', False),
               card_row('Последние операции', '5 последних', True)])
    body = (note('Удерживайте ⠿ и тяните, чтобы поменять порядок.', 4) + label('Карточки', top=10) + g
            + label('Подробности') + group([r('«Скоро»: горизонт', '7 дней', 'calendar', LIME, LIMEBG),
                                           r('«Последние»: сколько', '5 операций', 'list', LIME, LIMEBG),
                                           r('Плитки', 'Траты за месяц · Бюджет', 'budget', LIME, LIMEBG)])
            + label('Баланс') + group([r('Счета в общем балансе', 'Все, кроме 2', 'wallet', YELLOW, YELLOWBG)])
            + reset_link())
    return inner_screen('Обзор', body)


def home_horizon():
    body = (pick_row('«Скоро»: показывать платежи на', ['3 дня', '7 дней', '14 дней', '30 дней'], '7 дней')
            + label('Последние операции') + pick_row('Сколько показывать', ['3', '5', '10'], '5')
            + label('Плитки') + group([r('Первая', 'Траты за месяц', 'cart', LIME, LIMEBG), r('Вторая', 'Бюджет месяца', 'budget', LIME, LIMEBG)])
            + note('Нет курса — на втором месте сама появится «Внимание». Пока лимитов нет, вместо «Бюджета» — «Без категории».', 8))
    return inner_screen('Обзор: подробности', body)


def home_edit():
    """Edit mode on «Обзор» itself: hold a card → every card gets ⠿ and ✕ hide, «Готово» at the bottom."""
    def small(icon, name):
        return ('<span aria-label="%s" style="width: 36px; height: 36px; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
                % (name, CHIP, svg(icon, INK, 18)))

    def wrap(inner, label_):
        head = ('<div style="display: flex; align-items: center; gap: 8px; margin: 12px 0 6px 4px;"><span style="flex-grow: 1; font-size: 13px; font-weight: 600; color: %s;">%s</span>%s%s</div>'
                % (MUTED, label_, small('drag', 'Тянуть'), small('close', 'Скрыть')))
        return head + '<div style="border-radius: 26px; border: 1.5px dashed %s; padding: 4px;">%s</div>' % (LINE2, inner)
    body = (balance_hero() + wrap(tiles(tile('Траты за ноябрь', '1 240,50', 'USD'), tile('Бюджет · 13 дней', '68 %', 'осталось 410')).replace('margin-top: 10px; ', ''), 'Плитки')
            + wrap(card(''.join(tx(*x) for x in RECENT[:2])), 'Последние операции')
            + button('Готово', 'primary', 'check', 16))
    return tab_screen('Обзор', body, 'home')


def ops():
    body = (label('В строке', top=4) + group([sw('Счёт', 'Galicia · Visa', True), sw('Теги', 'пилюли под названием', True), sw('Заметка', 'первая строка', False)])
            + label('Сумма') + pick_row('Показывать в строке', ['В валюте счёта', 'В основной'], 'В валюте счёта')
            + note('Удержание суммы всё равно показывает её во всех валютах отчётов.', 8))
    return inner_screen('Операции', body)


def reports():
    body = (label('Период', top=4) + pick_row('При открытии «Отчётов»', ['Месяц', 'Квартал', 'Год'], 'Месяц')
            + label('Сравнение') + group([sw('Сравнивать с прошлым периодом', 'пилюли «↑ 45 %» и черта прошлого', True)]))
    return inner_screen('Отчёты', body)


def accounts():
    body = (label('Группировка', top=4) + pick_row('Счета сгруппированы', ['По типу', 'По валюте'], 'По типу')
            + label('Архив') + group([sw('Показывать архивные счета', 'внизу, серым', False)]))
    return inner_screen('Счета', body)


# ---- the rest of the groups --------------------------------------------------------------------------------------------

def money():
    body = (label('Для всей семьи', top=4) + group([r('Валюты отчётов', 'основная USD · ещё ARS, AMD', 'rate', CYAN, CYANBG),
                                                     r('День начала месяца', '1-е число', 'calendar', CYAN, CYANBG)])
            + label('На этом телефоне') + pick_row('Неделя начинается с', ['Пн', 'Вс', 'Как в системе'], 'Пн'))
    return inner_screen('Деньги и даты', body)


def entry():
    body = (label('Быстрый ввод', top=4) + group([sw('Встряхнуть — новая операция', 'встряхните телефон в приложении', True)])
            + label('Шторка «+»') + pick_row('Счёт и валюта', ['Последние', 'Всегда эти'], 'Последние')
            + label('Отклик') + group([sw('Вибрация', 'касания и успешное сохранение', True)]))
    return inner_screen('Ввод операции', body)


def entry_account():
    """«Ввод операции» with «Всегда этот»: the account (and with it the currency) the «+» sheet opens on."""
    body = (label('Быстрый ввод', top=4) + group([sw('Встряхнуть — новая операция', 'встряхните телефон в приложении', True)])
            + label('Шторка «+»') + pick_row('Счёт и валюта', ['Последние', 'Всегда этот'], 'Всегда этот')
            + '<div style="height: 10px;"></div>' + group([r('Счёт', 'Galicia · ARS', 'wallet', YELLOW, YELLOWBG)])
            + note('Шторка «+» откроется с этим счётом. Сменить его можно там же.', 8)
            + label('Отклик') + group([sw('Вибрация', 'касания и успешное сохранение', True)]))
    return inner_screen('Ввод операции', body)


def balance():
    """«Обзор → Баланс → Счета в общем балансе»: the same flags as in the account editor, in one list; shared by the family."""
    g = group([sw('Galicia', 'ARS · карта', True), sw('USDT-карта', 'USDT · карта', True), sw('Bybit', 'USDT · биржа', True),
               sw('Копилка', 'USD · наличные', False), sw('Кредитка', 'RUB · карта', False)])
    return inner_screen('Счета в общем балансе', note('Что входит в «Общий баланс» на «Обзоре». Общая для всей семьи.', 4) + label('Счета', top=10) + g)


def notices():
    body = (label('Платежи', top=4) + group([sw('Напоминания о платежах', 'за день до срока и в день платежа', True)])
            + label('Бюджеты') + group([sw('Лимиты', 'раз в день: 80 % лимита или темп быстрее плана', True)])
            + label('Когда') + pick_row('Время напоминания', ['8:00', '9:00', '12:00', '18:00'], '9:00')
            + '<div style="height: 10px;"></div>' + pick_row('Предупреждать за', ['В день', '1 день', '3 дня'], '1 день')
            + label('Семья') + group([sw('Трата партнёра', 'уведомление, когда внесли операцию', True), r('Только от суммы', '50 USD', 'cart', YELLOW, YELLOWBG)]))
    return inner_screen('Уведомления', body)


def data():
    body = (label('Данные', top=4) + group([r('Резервная копия', 'вчера 23:00 · авто', 'archive', CYAN, CYANBG),
                                           r('Экспорт в CSV', 'операции за период', 'upload', VIOLET, VIOLETBG),
                                           r('Импорт из Wallet', 'последний — 3 нояб.', 'download', YELLOW, YELLOWBG)])
            + label('Сеть') + group([sw('Синхронизировать только по Wi-Fi', '', False)])
            + label('Осторожно') + group([row('Стереть все данные', 'сначала сделаем копию', tile_icon('trash', WARN, CHIP), chevron(), title_color=WARN, min_h=52)]))
    return inner_screen('Данные и копия', body)


def more_row():
    """«Ещё»: «Внешний вид» becomes «Настройки»."""
    g = group([r('Настройки', 'Тёмная · Лайм', 'gear', VIOLET, VIOLETBG), r('Обратная связь', '1 ответ: сделано в 1.44', 'mail', LIME, LIMEBG)])
    return tab_screen('Ещё', label('Приложение', top=6) + g, 'more')


BOARDS = [  # name, title, fn, issue number
    ('Set1Root', 'Настройки: карточки групп', root, 1118),
    ('Set1RootMore', 'Настройки: прокрутка', root_more, 1118),
    ('Set1RootEs', 'Настройки: es, 320 dp, ×1,3', root_es, 1118),
    ('Set1More', 'Ещё: «Внешний вид» → «Настройки»', more_row, 1118),
    ('Set1Look', 'Внешний вид: язык, тема, палитра, плотность списков', look, 1118),
    ('Set1Screens', 'Экраны: Обзор, Операции, Отчёты, Счета', screens, 1118),
    ('Set1Home', 'Обзор: карточки — показать/скрыть и порядок', home_cards, 1118),
    ('Set1HomeMore', 'Обзор: «Скоро», «Последние», плитки', home_horizon, 1118),
    ('Set1HomeEdit', 'Обзор: режим правки на экране (удержание)', home_edit, 1118),
    ('Set1Ops', 'Операции: что в строке, в какой валюте сумма', ops, 1118),
    ('Set1Reports', 'Отчёты: период и сравнение', reports, 1118),
    ('Set1Accounts', 'Счета: группировка и архив', accounts, 1118),
    ('Set1Money', 'Деньги и даты', money, 1118),
    ('Set1Entry', 'Ввод операции', entry, 1118),
    ('Set1EntryAcc', 'Ввод операции: всегда этот счёт', entry_account, 1118),
    ('Set1Balance', 'Обзор: счета в общем балансе', balance, 1118),
    ('Set1Notices', 'Уведомления', notices, 1118),
    ('Set1Data', 'Данные и копия', data, 1118),
]
