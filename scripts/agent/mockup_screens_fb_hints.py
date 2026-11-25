"""Generated boards: #513 — newcomer onboarding («Первые шаги» + soft hints), spec `docs/agents/hints.md` (owner 2026-11-18).

Views: А2 — a row on «Обзор» → sheet with the list; П1 — a word hint (`CoShellHint`, «✕»); П2 — a gesture bubble («Понятно»);
a warm one-time pill; the setting in «Ещё → Приложение». Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import *  # noqa: F401,F403
from mockup_screens_main import RECENT, balance_hero, tile, tiles, converter_card, recent_card
from mockup_screens_tabs import more_body
from mockup_catalog_rows import hint as hint_card  # noqa: F401
from mockup_screens_fb_tips import tip
from mockup_screens_fb_quick import entry_from, simple_amount, plain_details
from mockup_catalog_parts import peek_card  # noqa: F401  (same bubble family as the hold-to-peek one)

FEEDBACK = [
    (513, 'Обучение новичка: «Первые шаги» и мягкие подсказки · 2026-11-18',
     'Вид А2 (строка на «Обзоре» → шторка), П1 слова, П2 жесты, тёплые моменты, «Подсказки» в «Ещё → Приложение». '
     'Тексты и условия — docs/agents/hints.md. Без вины и красного, любой шаг можно пропустить.'),
]

STEPS = [('Добавьте счёт', 'Где лежат ваши деньги: карта, наличные или копилка. Начните с одного.'),
         ('Запишите первую трату', 'Любую, хоть кофе. Нажмите «+».'),
         ('Задайте первый лимит', 'Сколько готовы тратить на продукты за месяц. Цифру можно менять.'),
         ('Позовите близкого человека', 'Вместе проще. Можно и позже.')]


def steps_row(done, nxt):
    """А2: one calm row under the balance; progress is a 8 px meter (CoShellMeter), no red, no streaks."""
    sub = 'Начнём со счёта' if done == 0 else 'Сделано: %d из 4 · Дальше: %s' % (done, nxt)
    inner = row('<b>Первые шаги</b>', sub, tile_icon('bulb', LIME, LIMEBG), chevron(), min_h=64)
    return '<div style="margin-top: 10px;">%s</div>' % card(inner + '<div style="padding: 0 16px 14px;">%s</div>' % progress(done / 4, 0))


def home(done, nxt, extra='', steps=None, top=''):
    body = top + (balance_hero('0,00', 'USD', 'Счетов пока нет') if done == 0 else balance_hero())
    body += steps if steps is not None else steps_row(done, nxt)
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
    """П1: a word hint as a card under the header (owner poll 2026-11-25 «А»): lightbulb, text, «Понятно»; once per screen."""
    accs = group([row('<b>Galicia</b>', 'Карта · ARS', badge('GA', '#F26E22'), '<span style="%s">1 250 000 ARS</span>' % MONO),
                  row('<b>Наличные</b>', 'Наличные · USD', badge('НЛ', '#C9D3C2'), '<span style="%s">320 USD</span>' % MONO)])
    return board('Счета', inner_head('Счета') + '<div style="padding: 0 16px;"><div style="margin-top: 4px;">%s</div><div style="margin-top: 12px;">%s</div></div>' % (
        tip('Счёт — место, где лежат деньги. Баланс считается сам.'), accs))


def gesture():
    """П2: the gesture card on top of the list where the gesture works (owner poll 2026-11-25 «А»); «Понятно» closes it."""
    return _ops_rows('', top=tip('Удерживайте операцию, чтобы выбрать несколько.'))


def moment():
    """Warm one-time moment: the hint card on top of the tab (owner poll 2026-11-25 «А»); by the event itself (outside the daily limit), Confirm."""
    return board('Обзор: первая запись', home(2, 'первый лимит', top=tip_top('Первая запись есть. Так и начинается бюджет.')))


def moment_long():
    """Long moment (backup, month_end, theme): the card with «Открыть» to the named screen before «Понятно»."""
    return board('Обзор: резервная копия', home(2, 'первый лимит', top=tip_top(
        'Записей уже много. Сохраните резервную копию, чтобы ничего не потерять: Ещё → Резервная копия.', 'Открыть')))


def finale():
    """Finale: the «Первые шаги» row turns once into «Всё готово» (CoShellSuccessBurst, Confirm), then disappears (poll A)."""
    inner = ('<div style="padding: 16px; text-align: center;"><div style="font-size: 17px; font-weight: 700;">Всё готово</div>'
             '<div style="font-size: 13px; color: %s; margin-top: 4px;">Вы уже ведёте бюджет — а это самое трудное.</div></div>' % MUTED)
    return board('Обзор: финал', home(4, '', steps='<div style="margin-top: 10px;">%s</div>' % card(inner)))


def hints_reset():
    """After «Показать подсказки заново» (in «Настройки → Помощь»): back on the tabs, the card «Подсказки включены» on top of the list."""
    return board('Ещё: сброс подсказок', tab_screen('Ещё', tip_top('Подсказки включены') + label('Деньги', top=6) + group([
        row('Счета', '', tile_icon('wallet', VIOLET, VIOLETBG), chevron(), min_h=52)]), 'more'))


def app_settings():
    app = group([row('Внешний вид', 'Лайм · тёмная', tile_icon('spark', VIOLET, VIOLETBG), chevron(), min_h=52),
                 row('Подсказки', 'Показываются', tile_icon('bulb', CYAN, CYANBG), switch(True), min_h=52),
                 row('Показать подсказки заново', 'Вернёт «Первые шаги» и советы', tile_icon('refresh', LIME, LIMEBG), chevron(), min_h=52),
                 row('Обратная связь', 'Ошибка или идея', tile_icon('mail', PINK, PINKBG), chevron(), min_h=52)])
    return board('Ещё', tab_screen('Ещё', label('Приложение', top=6) + app + note('Сброс очищает «показано» и снимает «скрыто» у шагов.', 8), 'more'))


def coach_strip(text):
    """П2 over the entry sheet (owner poll 2026-11-19, Б): `CoShellCoachCard` as it is (text, «Понятно» under it), under the status bar over the sheet's
    top edge, non-modal — the form stays under it. In the app the sheet starts ≈ 102 dp from the top, so the card sits in the dimmed strip."""
    return '<div style="position: absolute; left: 16px; right: 16px; top: 30px; z-index: 5;">%s</div>' % tip(text)


def entry_hint():
    """Shown on the first entries: «Нужны только сумма и счёт», later «Можно складывать…» and «Это возврат»; one a day, «Понятно» closes for good."""
    return board('Шторка ввода: подсказка', entry_from(simple_amount('0', color=MUTED2), cat='', det=plain_details())
                 + coach_strip('Нужны только сумма и счёт. Остальное можно пропустить.'))


def hint_row(text):
    return '<div style="margin-bottom: 10px;">%s</div>' % tip(text)


def tip_top(text, extra=None):
    return '<div style="margin: 8px 0 4px;">%s</div>' % tip(text, extra_action=extra)


def tags_hint():
    """Word hint П1 under the title of the «Теги» sheet (owner poll 2026-11-19, А); once, at the first opening."""
    head = ('<div style="padding: 8px 20px 0; display: flex; align-items: center;"><div style="flex-grow: 1;"><h2 style="margin: 0; font-size: 20px;">Теги</h2>'
            '<div style="font-size: 13px; color: %s; margin-top: 2px;">Выберите метки для операции</div></div>%s</div>' % (MUTED, icon_button('close', 'Закрыть', INK)))
    rows = group([row('<b>%s</b>' % n, '%s операций' % c, tile_icon('spark', LIME, LIMEBG), '', min_h=56) for n, c in (('Отпуск', 12), ('Подарки', 7), ('Дети', 5), ('Работа', 3))])
    body = '<div style="padding: 12px 16px 0;">%s%s</div>' % (hint_row('Тег — цветная метка операции, например «отпуск» или «подарки».'), rows)
    return board('Теги: подсказка', scrim(home(4, '')) + sheet(head + body, top=300))


def currency_hint():
    """Word hint П1 under the title of «Валюта покупки» (owner poll 2026-11-19, А); once, when a payment in another currency is entered."""
    head = ('<div style="padding: 8px 20px 0; display: flex; align-items: center;"><div style="flex-grow: 1;"><h2 style="margin: 0; font-size: 20px;">Валюта покупки</h2>'
            '<div style="font-size: 13px; color: %s; margin-top: 2px;">Если списали в другой валюте</div></div>%s</div>' % (MUTED, icon_button('close', 'Закрыть', INK)))
    rows = group([row('<b>%s</b>' % n, d, tile_icon('rate', LIME, LIMEBG), '', min_h=56) for n, d in (('ARS', 'песо'), ('USD', 'доллар'), ('EUR', 'евро'), ('RUB', 'рубль'))])
    body = '<div style="padding: 12px 16px 0;">%s%s</div>' % (hint_row('Укажите сумму покупки, и курс посчитается сам.'), rows)
    return board('Валюта покупки: подсказка', scrim(home(4, '')) + sheet(head + body, top=300))


def _ops_rows(bottom_overlay, top=''):
    rows = ''.join(tx(*r) for r in RECENT)
    head = '<div style="margin-top: 8px;">%s</div>' % top if top else ''
    return board('Операции', tab_screen('Операции', head + '<div style="margin: 8px -16px 0;">%s%s</div>' % (day('СЕГОДНЯ', '−45 100', 'ARS'), rows), 'ops') + bottom_overlay)


def saved_snack():
    """#325: after a NEW operation is saved, «Сохранено · сумма · Отменить» for 4 s (the same window lets a shake undo it); edits show nothing."""
    return _ops_rows(snackbar('Сохранено · 3 500 ARS', 'Отменить', 96))


def undone_snack():
    """#325: a shake in that window removes the save: «Отменено · сумма · Вернуть» (4 s); later a shake opens the receipt scanner as before."""
    return _ops_rows(snackbar('Отменено · 3 500 ARS', 'Вернуть', 96))


def shake_hint():
    """#325 gesture hint (once, after the first saved operation, 0,6 s after its snackbar left): the card on top of the tab, «Понятно»."""
    return _ops_rows('', top=tip('Ошиблись? Сразу после сохранения встряхните телефон — операция отменится.'))


def pin_forgot():
    """#602 (poll 2026-11-23 «А»): right after the PIN is turned on, one warm moment names the way back; it shows once, as the card on top of
    the tab the person comes back to (it waits off the tabs, owner poll 2026-11-25 «А»)."""
    return board('Обзор: PIN включён', home(4, '', top=tip_top('PIN включён. Забудете — на экране входа нажмите «Забыли PIN?».')))


BOARDS = [  # name, title, fn, issue number
    ('Hint513Home0', '#513 · «Обзор», новичок: строка «Первые шаги»', home_new, 513),
    ('Hint513Home2', '#513 · «Обзор», два шага из четырёх', home_two, 513),
    ('Hint513Steps', '#513 · шторка «Первые шаги»', steps_sheet, 513),
    ('Hint513Word', '#513 · П1: подсказка-слово на «Счетах»', accounts_hint, 513),
    ('Hint513Gesture', '#513 · П2: подсказка-жест в «Операциях»', gesture, 513),
    ('Hint513Moment', '#513 · тёплый момент: карточка «Первая запись есть»', moment, 513),
    ('Hint513MomentLong', '#513 · тёплый момент: карточка с «Открыть»', moment_long, 513),
    ('Hint513Finale', '#513 · финал «Первых шагов»: «Всё готово»', finale, 513),
    ('Hint513Reset', '#513 · «Подсказки включены» после сброса', hints_reset, 513),
    ('Hint513EntryCoach', '#513 · шторка ввода: карточка «Понятно» над шторкой', entry_hint, 513),
    ('Hint513Tags', '#513 · «Теги»: подсказка под заголовком', tags_hint, 513),
    ('Hint513Currency', '#513 · «Валюта покупки»: подсказка под заголовком', currency_hint, 513),
    ('Hint602PinForgot', '#602 · карточка после включения PIN: «Забыли PIN?»', pin_forgot, 602),
    ('Hint513Settings', '#513 · «Ещё → Приложение»: подсказки', app_settings, 513),
    ('Undo325Saved', '#325 · снэкбар «Сохранено · Отменить» (4 с)', saved_snack, 325),
    ('Undo325Shaken', '#325 · встряхнули: «Отменено · Вернуть»', undone_snack, 325),
    ('Hint325Shake', '#325 · подсказка-жест «Встряхните» (один раз)', shake_hint, 325),
]
