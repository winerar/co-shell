"""Generated boards: #513 — newcomer onboarding («Первые шаги» + soft hints), spec `docs/agents/hints.md` (owner 2026-11-18).

Views: А2 — a row on «Обзор» → sheet with the list; П1 — a word hint (`CoShellHint`, «✕»); П2 — a gesture bubble («Понятно»);
a warm one-time pill; the setting in «Ещё → Приложение». Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import *  # noqa: F401,F403
from mockup_screens_main import RECENT, balance_hero, tile, tiles, converter_card, recent_card
from mockup_screens_tabs import more_body
from mockup_catalog_rows import hint as hint_card
from mockup_catalog_parts import peek_card  # noqa: F401  (same bubble family as the hold-to-peek one)

FEEDBACK = [
    (513, 'Обучение новичка: «Первые шаги» и мягкие подсказки · 2026-11-18',
     'Вид А2 (строка на «Обзоре» → шторка), П1 слова, П2 жесты, тёплые моменты, «Подсказки» в «Ещё → Приложение». '
     'Тексты и условия — docs/agents/hints.md. Без вины и красного, любой шаг можно пропустить.'),
]

STEPS = [('Добавьте счёт', 'Где лежат ваши деньги: карта, наличные или копилка. Начните с одного.'),
         ('Запишите первую трату', 'Любую, хоть кофе. Нажмите «+».'),
         ('Задайте первый лимит', 'Сколько готовы тратить на продукты за месяц. Цифру можно менять.'),
         ('Позовите близкого человека', 'Вдвоём проще. Можно и позже.')]


def steps_row(done, nxt):
    """А2: one calm row under the balance; progress is a 8 px meter (CoShellMeter), no red, no streaks."""
    sub = 'Начнём со счёта' if done == 0 else 'Сделано: %d из 4 · Дальше: %s' % (done, nxt)
    inner = row('<b>Первые шаги</b>', sub, tile_icon('bulb', LIME, LIMEBG), chevron(), min_h=64)
    return '<div style="margin-top: 10px;">%s</div>' % card(inner + '<div style="padding: 0 16px 14px;">%s</div>' % progress(done / 4, 0))


def home(done, nxt, extra=''):
    body = balance_hero('0,00', 'USD', 'Счетов пока нет') if done == 0 else balance_hero()
    body += steps_row(done, nxt)
    if done == 0:
        body += empty('wallet', 'Операций пока нет. Нажмите «+», чтобы записать первую.').replace('padding: 96px', 'padding: 40px')
    else:
        body += tiles(tile('Траты за ноябрь', '1 240,50', 'USD'), tile('Без категории', '14', 'операций')) + recent_card(RECENT[:2])
    return tab_screen('Обзор', body, 'home', icon_button('search', 'Поиск', INK, True)) + extra


def home_new():
    return board('Обзор: новичок', home(0, ''))


def home_two():
    return board('Обзор: два шага', home(2, 'первый лимит'))


def mark(done):
    if done:
        return ('<span style="width: 28px; height: 28px; flex: none; border-radius: 50%%; background: %s; border: 2px solid %s; display: flex; align-items: center; '
                'justify-content: center;">%s</span>' % (LIME, INK, svg('check', ONLIME, 16, 2.6)))
    return '<span style="width: 22px; height: 22px; flex: none; border-radius: 7px; border: 2px solid %s; box-sizing: border-box;"></span>' % OUTLINE


def steps_sheet(done=2):
    rows = [row('<b>%s</b>' % t, s, mark(i < done), '', min_h=72) for i, (t, s) in enumerate(STEPS)]
    head = ('<div style="padding: 8px 20px 0; display: flex; align-items: center;"><div style="flex-grow: 1;"><h2 style="margin: 0; font-size: 20px;">Первые шаги</h2>'
            '<div style="font-size: 13px; color: %s; margin-top: 2px;">Сделано: %d из 4. Любой шаг можно пропустить.</div></div>%s</div>'
            % (MUTED, done, icon_button('close', 'Закрыть', INK)))
    body = '<div style="padding: 12px 16px 0;">%s%s<div style="text-align: center; margin-top: 18px;"><span style="color: %s; font-size: 15px; font-weight: 600;">Скрыть шаги</span></div></div>' % (
        progress(done / 4, 0), '<div style="margin-top: 12px;">%s</div>' % group(rows), MUTED)
    return board('Первые шаги', scrim(home(done, 'первый лимит')) + sheet(head + body, top=150))


def accounts_hint():
    """П1: a word hint under the header, once per screen; «✕» = shown."""
    h = '<div style="display: flex; align-items: flex-start; gap: 4px;"><div style="flex-grow: 1;">%s</div>%s</div>' % (
        hint_card('A', 'Счёт — место, где лежат деньги. Баланс считается сам.'), icon_button('close', 'Закрыть подсказку', MUTED))
    accs = group([row('<b>Galicia</b>', 'Карта · ARS', badge('GA', '#F26E22'), '<span style="%s">1 250 000 ARS</span>' % MONO),
                  row('<b>Наличные</b>', 'Наличные · USD', badge('НЛ', '#C9D3C2'), '<span style="%s">320 USD</span>' % MONO)])
    return board('Счета', inner_head('Счета') + '<div style="padding: 0 16px;"><div style="margin-top: 4px;">%s</div><div style="margin-top: 12px;">%s</div></div>' % (h, accs))


def gesture():
    """П2: a bubble over the list; non-modal, «Понятно» closes it."""
    bubble = ('<div style="position: absolute; left: 24px; right: 24px; top: 400px; padding: 14px 16px; border-radius: 24px; background: %s; border: 1px solid %s; '
              'box-shadow: 0 12px 32px rgba(0,0,0,0.45);"><div style="font-size: 14px; line-height: 1.4;">Удерживайте операцию, чтобы выбрать несколько.</div>'
              '<div style="margin-top: 10px; text-align: right;"><span style="color: %s; font-size: 14px; font-weight: 700;">Понятно</span></div></div>' % (KEY, LINE2, LIME))
    rows = ''.join(tx(*r) for r in RECENT)
    return board('Операции', tab_screen('Операции', '<div style="margin: 8px -16px 0;">%s%s</div>' % (day('СЕГОДНЯ', '−45 100', 'ARS'), rows), 'ops') + bubble)


def moment():
    """Warm one-time pill above the bar (CoShellNoticePill): one line, «×»; vibration Confirm like a save."""
    pill = ('<div style="position: absolute; left: 24px; right: 24px; bottom: 96px; height: 52px; border-radius: 999px; background: %s; border: 1px solid %s; display: flex; '
            'align-items: center; gap: 10px; padding: 0 6px 0 6px;"><span style="width: 40px; height: 40px; border-radius: 50%%; background: %s; display: flex; align-items: center; '
            'justify-content: center;">%s</span><span style="flex-grow: 1; font-size: 14px;">Первая запись есть. Так и начинается бюджет.</span>%s</div>'
            % (KEY, LINE2, LIME, svg('check', ONLIME, 22, 2.4), icon_button('close', 'Закрыть', MUTED)))
    return board('Обзор: первая запись', home(2, 'первый лимит', pill))


def app_settings():
    app = group([row('Внешний вид', 'Лайм · тёмная', tile_icon('spark', VIOLET, VIOLETBG), chevron(), min_h=52),
                 row('Подсказки', 'Показываются', tile_icon('bulb', CYAN, CYANBG), switch(True), min_h=52),
                 row('Показать подсказки заново', 'Вернёт «Первые шаги» и советы', tile_icon('refresh', LIME, LIMEBG), chevron(), min_h=52),
                 row('Обратная связь', 'Ошибка или идея', tile_icon('mail', PINK, PINKBG), chevron(), min_h=52)])
    return board('Ещё', tab_screen('Ещё', label('Приложение', top=6) + app + note('Сброс очищает «показано» и снимает «скрыто» у шагов.', 8), 'more'))


BOARDS = [  # name, title, fn, issue number
    ('Hint513Home0', '#513 · «Обзор», новичок: строка «Первые шаги»', home_new, 513),
    ('Hint513Home2', '#513 · «Обзор», два шага из четырёх', home_two, 513),
    ('Hint513Steps', '#513 · шторка «Первые шаги»', steps_sheet, 513),
    ('Hint513Word', '#513 · П1: подсказка-слово на «Счетах»', accounts_hint, 513),
    ('Hint513Gesture', '#513 · П2: подсказка-жест в «Операциях»', gesture, 513),
    ('Hint513Moment', '#513 · тёплая плашка: первая запись', moment, 513),
    ('Hint513Settings', '#513 · «Ещё → Приложение»: подсказки', app_settings, 513),
]
