"""Boards (#513, owner 2026-11-25: «сделаем их встраиваемыми на экран скрываемыми карточками»): hints as cards inside the screen.

The card is the approved `CoShellCoachCard` (text + «Понятно»), now lying in the screen's own list instead of floating over the bar:
it never leaves by itself, so there is time to read it; «Понятно» hides it for good.
А — everything inline: words under the header, gestures on top of their list, warm moments on top of «Обзора» (with «Открыть» where the text
names a screen). Nothing floats over the bar any more. Б — words and gestures inline; warm moments stay a pill over the bar, but up to 4 lines
and 8–15 s by length. Poll boards — until «ок». Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import *  # noqa: F401,F403
from mockup_screens_main import RECENT, balance_hero, tile, tiles, recent_card

FEEDBACK = [
    (513, 'Подсказки — карточками на экране · опрос 2026-11-25',
     'Подсказка лежит в самом экране карточкой «текст + Понятно» и не исчезает сама — её успевают прочитать. А — так все подсказки, '
     'включая тёплые моменты вверху «Обзора»; Б — моменты остаются плашкой над панелью, но длиннее и без обрезки.'),
]


def tip(text, action='Понятно', extra_action=None):
    """CoShellCoachCard inline: a lightbulb in a tinted circle, the text in the normal ink, the action(s) under it on the right."""
    acts = ''.join('<span style="color: %s; font-size: 14px; font-weight: 700; margin-left: 20px;">%s</span>' % (c, a)
                   for a, c in ([(extra_action, CYAN)] if extra_action else []) + [(action, LIME)])
    return ('<div style="padding: 14px 16px; border-radius: 24px; background: %s; border: 1px solid %s;">'
            '<div style="display: flex; gap: 12px; align-items: flex-start;"><span style="flex: none; width: 32px; height: 32px; border-radius: 50%%; '
            'background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            '<div style="font-size: 15px; line-height: 1.4;">%s</div></div><div style="margin-top: 8px; text-align: right;">%s</div></div>'
            % (KEY, LINE2, mix(LIME, 18), svg('bulb', LIME, 20), text, acts))


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


def home_pill():
    """Б: the warm moment stays a pill over the bar — four lines at most, 8–15 s by length, so the long text reads to the end."""
    pill = ('<div style="position: absolute; left: 24px; right: 24px; bottom: 96px; border-radius: 28px; background: %s; border: 1px solid %s; display: flex; '
            'align-items: center; gap: 10px; padding: 8px 6px;"><span style="width: 40px; height: 40px; flex: none; border-radius: 50%%; background: %s; display: flex; '
            'align-items: center; justify-content: center;">%s</span><span style="flex-grow: 1; font-size: 14px; line-height: 1.35;">%s</span>'
            '<span style="color: %s; font-size: 14px; font-weight: 700; padding: 0 6px;">Открыть</span>%s</div>'
            % (KEY, LINE2, LIME, svg('check', ONLIME, 22, 2.4), 'Записей уже много. Сохраните резервную копию, чтобы ничего не потерять: Ещё → Резервная копия.',
               CYAN, icon_button('close', 'Закрыть', MUTED)))
    body = balance_hero() + tiles(tile('Траты за ноябрь', '1 240,50', 'USD'), tile('Без категории', '14', 'операций')) + recent_card(RECENT[:2])
    return tab_screen('Обзор', body, 'home') + pill


BOARDS = [  # name, title, fn, issue number
    ('Tip513Accounts', 'Слово: карточка под шапкой «Счетов»', accounts, 513),
    ('Tip513Ops', 'Жест: карточка над списком «Операций»', operations, 513),
    ('Tip513HomeA', 'А · момент: карточка вверху «Обзора» с «Открыть»', home_moment, 513),
    ('Tip513HomeB', 'Б · момент: плашка над панелью, до 4 строк', home_pill, 513),
]
