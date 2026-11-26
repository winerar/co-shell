"""Boards (#513, owner 2026-11-25: «сделаем их встраиваемыми на экран скрываемыми карточками»): hints as cards inside the screen.

The card is the approved `CoShellCoachCard` (text + «Понятно»), now lying in the screen's own list instead of floating over the bar:
it never leaves by itself, so there is time to read it; «Понятно» hides it for good.
А — everything inline: words under the header, gestures on top of their list, warm moments on top of «Обзора» (with «Открыть» where the text
names a screen). Nothing floats over the bar any more. Picked А (poll-513-tips.json, 2026-11-25); board Б removed. Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import *  # noqa: F401,F403
from mockup_screens_main import RECENT, balance_hero, tile, tiles, recent_card

FEEDBACK = [
    (513, 'Подсказки — карточками на экране · выбрано А, 2026-11-25',
     'Подсказка лежит в самом экране карточкой «текст + Понятно» и не исчезает сама — её успевают прочитать. А — так все подсказки, '
     'включая тёплые моменты: карточкой вверху вкладки, где человек сейчас (пойманный на внутреннем экране — ждёт вкладок). Над панелью подсказок больше нет.'),
    (745, 'Уведомления: предложение вечернего напоминания · выбрано А, 2026-11-25',
     'Локальные уведомления без сервера (#745, опрос 2026-11-25: экран — группы «А», по умолчанию всё выключено «А», тексты — коротко «А»). После первой записанной '
     'операции на «Обзоре» одна карточка с вопросом; «Включить» просит разрешение на уведомления, «Не сейчас» закрывает её насовсем. Экран «Ещё → Настройки → Уведомления» — доска Set1Notices.'),
]


def tip(text, action='Понятно', extra_action=None):
    """CoShellCoachCard inline: a lightbulb in a tinted circle, the text in the normal ink, the action(s) under it on the right."""
    acts = ''.join('<span style="color: %s; font-size: 14px; font-weight: 700; margin-left: 20px;">%s</span>' % (VIOLET, a)
                   for a in ([extra_action] if extra_action else []) + [action])
    return ('<div style="padding: 14px 16px; border-radius: 24px; background: %s; border: 1px solid %s;">'
            '<div style="display: flex; gap: 12px; align-items: flex-start;"><span style="flex: none; width: 32px; height: 32px; border-radius: 50%%; '
            'background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            '<div style="font-size: 15px; line-height: 1.4;">%s</div></div><div style="margin-top: 8px; text-align: right;">%s</div></div>'
            % (KEY, LINE2, mix(VIOLET, 18), svg('bulb', VIOLET, 20), text, acts))


def accounts():
    accs = group([row('<b>Galicia</b>', 'Карта · ARS', badge('GA', '#F26E22'), '<span style="%s">1 250 000 ARS</span>' % MONO),
                  row('<b>Наличные</b>', 'Наличные · USD', badge('НЛ', '#C9D3C2'), '<span style="%s">320 USD</span>' % MONO)])
    return inner_screen('Счета', '<div style="margin-top: 4px;">%s</div><div style="margin-top: 12px;">%s</div>' % (
        tip('Счёт — место, где лежат деньги. Баланс считается сам.'), accs))


def operations():
    rows = ''.join(tx(*r) for r in RECENT)
    return tab_screen('Операции', '<div style="margin-top: 8px;">%s</div><div style="margin: 8px -16px 0;">%s%s</div>' % (
        tip('Удерживайте операцию, чтобы выбрать несколько.'), day('СЕГОДНЯ', '−45 100', 'ARS'), rows), 'ops')


def home_moment():
    body = tip('Записей уже много. Сохраните резервную копию, чтобы ничего не потерять: Ещё → Резервная копия.', extra_action='Открыть') + \
        balance_hero() + tiles(tile('Траты за ноябрь', '1 240,50', 'USD'), tile('Без категории', '14', 'операций')) + recent_card(RECENT[:2])
    return tab_screen('Обзор', '<div style="margin-top: 8px;"></div>' + body, 'home')


def notice_offer():
    """#745 (poll 2026-11-25, А; board Tip745Offer): after the first entry — one card; «Включить» asks for the permission, «Не сейчас» answers it for good."""
    body = tip('Напоминать вечером о тратах? Раз в день, если записей нет. Выключить можно в «Ещё → Настройки → Уведомления».',
               action='Включить', extra_action='Не сейчас') + \
        balance_hero() + tiles(tile('Траты за ноябрь', '1 240,50', 'USD'), tile('Без категории', '14', 'операций')) + recent_card(RECENT[:2])
    return tab_screen('Обзор', '<div style="margin-top: 8px;"></div>' + body, 'home')


def recurring():
    pays = group([row('<b>Аренда</b>', '1-го числа · Galicia', '', '<span style="%s">650 USD</span>' % MONO, min_h=56),
                  row('<b>Интернет</b>', '15-го числа · Galicia', '', '<span style="%s">25 000 ARS</span>' % MONO, min_h=56)])
    return inner_screen('Регулярные', '<div style="margin-top: 4px;">%s</div><div style="margin-top: 12px;">%s</div>' % (
        tip('Шаблон с «Повтором» напомнит о платеже и появится в «Скоро» на «Обзоре».'), pays))


def clipboard():
    """The fourth word of the entry sheet (after «Это возврат»), floating over its dimmed strip like the others (`SheetCoach`)."""
    from mockup_screens_fb_hints import entry_hint
    return entry_hint().replace('Нужны только сумма и счёт. Остальное можно пропустить.', 'Скопировали сообщение банка? Нажмите значок буфера — сумма подставится сама.')


BOARDS = [  # name, title, fn, issue number
    ('Tip513Accounts', 'Слово: карточка под шапкой «Счетов»', accounts, 513),
    ('Tip513Ops', 'Жест: карточка над списком «Операций»', operations, 513),
    ('Tip513HomeA', 'А · момент: карточка вверху «Обзора» с «Открыть»', home_moment, 513),
    ('Tip745Offer', 'Обзор: предложение вечернего напоминания о тратах (#745)', notice_offer, 745),
    ('Tip513Recurring', 'Слово: «Регулярные» (аудит 2026-11-25)', recurring, 513),
    ('Tip513Clipboard', 'Слово в шторке ввода: «Вставить из буфера» (аудит 2026-11-25)', clipboard, 513),
]
