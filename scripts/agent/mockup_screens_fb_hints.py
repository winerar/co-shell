"""Generated boards: #513 — newcomer onboarding («Первые шаги» + soft hints), spec `docs/agents/hints.md` (owner 2026-11-18).

Views: А2 — a row on «Обзор» → sheet with the list; П1 — a word hint (`CoShellHint`, «✕»); П2 — a gesture bubble («Понятно»);
a warm one-time pill; the setting in «Ещё → Приложение». Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import *  # noqa: F401,F403
from mockup_screens_main import RECENT, balance_hero, tile, tiles, converter_card, recent_card
from mockup_screens_tabs import more_body
from mockup_catalog_rows import hint as hint_card
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


def home(done, nxt, extra='', steps=None):
    body = balance_hero('0,00', 'USD', 'Счетов пока нет') if done == 0 else balance_hero()
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
    """П1: a word hint under the header, once per screen; «✕» = shown."""
    h = '<div style="display: flex; align-items: flex-start; gap: 4px;"><div style="flex-grow: 1;">%s</div>%s</div>' % (
        hint_card('A', 'Счёт — место, где лежат деньги. Баланс считается сам.'), icon_button('close', 'Закрыть подсказку', MUTED))
    accs = group([row('<b>Galicia</b>', 'Карта · ARS', badge('GA', '#F26E22'), '<span style="%s">1 250 000 ARS</span>' % MONO),
                  row('<b>Наличные</b>', 'Наличные · USD', badge('НЛ', '#C9D3C2'), '<span style="%s">320 USD</span>' % MONO)])
    return board('Счета', inner_head('Счета') + '<div style="padding: 0 16px;"><div style="margin-top: 4px;">%s</div><div style="margin-top: 12px;">%s</div></div>' % (h, accs))


def gesture():
    """П2: a bubble over the list; non-modal, «Понятно» closes it."""
    bubble = ('<div style="position: absolute; left: 24px; right: 24px; top: 400px; padding: 14px 16px; border-radius: 24px; background: %s; border: 1px solid %s; '
              '"><div style="font-size: 14px; line-height: 1.4;">Удерживайте операцию, чтобы выбрать несколько.</div>'
              '<div style="margin-top: 10px; text-align: right;"><span style="color: %s; font-size: 14px; font-weight: 700;">Понятно</span></div></div>' % (KEY, LINE2, LIME))
    rows = ''.join(tx(*r) for r in RECENT)
    return board('Операции', tab_screen('Операции', '<div style="margin: 8px -16px 0;">%s%s</div>' % (day('СЕГОДНЯ', '−45 100', 'ARS'), rows), 'ops') + bubble)


def pill_box(text, ic, action=None, two=False):
    """Warm pill above the bar (CoShellNoticePill): icon in a circle, 1-2 lines, optional «Открыть» (opens the screen the text names), «×»."""
    act = ('<span style="color: %s; font-size: 14px; font-weight: 700; padding: 0 10px;">%s</span>' % (LIME, action)) if action else ''
    return ('<div style="position: absolute; left: 24px; right: 24px; bottom: 96px; min-height: 52px; border-radius: 999px; background: %s; border: 1px solid %s; display: flex; '
            'align-items: center; gap: 10px; padding: 6px;"><span style="width: 40px; height: 40px; flex: none; border-radius: 50%%; background: %s; display: flex; align-items: center; '
            'justify-content: center;">%s</span><span style="flex-grow: 1; font-size: 14px; line-height: 1.35;">%s</span>%s%s</div>'
            % (KEY, LINE2, LIME, svg(ic, ONLIME, 22, 2.4), text, act, icon_button('close', 'Закрыть', MUTED)))


def moment():
    """Warm one-time moment, one line; shown by the event itself (outside the daily limit), vibration Confirm like a save."""
    return board('Обзор: первая запись', home(2, 'первый лимит', pill_box('Первая запись есть. Так и начинается бюджет.', 'check')))


def moment_long():
    """Long moment (backup, month_end, theme): up to two lines + «Открыть» to the named screen (owner poll 2026-11-19, A)."""
    return board('Обзор: резервная копия', home(2, 'первый лимит', pill_box(
        'Записей уже много. Сохраните резервную копию, чтобы ничего не потерять.', 'check', 'Открыть')))


def finale():
    """Finale: the «Первые шаги» row turns once into «Всё готово» (CoShellSuccessBurst, Confirm), then disappears (poll A)."""
    inner = ('<div style="padding: 16px; text-align: center;"><div style="font-size: 17px; font-weight: 700;">Всё готово</div>'
             '<div style="font-size: 13px; color: %s; margin-top: 4px;">Вы уже ведёте бюджет — а это самое трудное.</div></div>' % MUTED)
    return board('Обзор: финал', home(4, '', steps='<div style="margin-top: 10px;">%s</div>' % card(inner)))


def hints_reset():
    """After «Показать подсказки заново»: a one-line pill «Подсказки включены» above the bar."""
    return board('Ещё: сброс подсказок', tab_screen('Ещё', label('Приложение', top=6) + group([
        row('Показать подсказки заново', 'Вернёт «Первые шаги» и советы', tile_icon('refresh', LIME, LIMEBG), chevron(), min_h=52)]), 'more')
        + pill_box('Подсказки включены', 'check'))


def app_settings():
    app = group([row('Внешний вид', 'Лайм · тёмная', tile_icon('spark', VIOLET, VIOLETBG), chevron(), min_h=52),
                 row('Подсказки', 'Показываются', tile_icon('bulb', CYAN, CYANBG), switch(True), min_h=52),
                 row('Показать подсказки заново', 'Вернёт «Первые шаги» и советы', tile_icon('refresh', LIME, LIMEBG), chevron(), min_h=52),
                 row('Обратная связь', 'Ошибка или идея', tile_icon('mail', PINK, PINKBG), chevron(), min_h=52)])
    return board('Ещё', tab_screen('Ещё', label('Приложение', top=6) + app + note('Сброс очищает «показано» и снимает «скрыто» у шагов.', 8), 'more'))


def coach_strip(text):
    """П2 over the entry sheet (owner poll 2026-11-19, Б): `CoShellCoachCard` as it is (text, «Понятно» under it), under the status bar over the sheet's
    top edge, non-modal — the form stays under it. In the app the sheet starts ≈ 102 dp from the top, so the card sits in the dimmed strip."""
    return ('<div style="position: absolute; left: 16px; right: 16px; top: 30px; padding: 12px 8px 4px 16px; border-radius: 24px; background: %s; border: 1px solid %s; z-index: 5;">'
            '<div style="font-size: 14px; line-height: 1.4; padding-right: 12px;">%s</div><div style="text-align: right; color: %s; font-size: 14px; font-weight: 700; padding: 8px 12px;">Понятно</div></div>'
            % (KEY, LINE2, text, LIME))


def entry_hint():
    """Shown on the first entries: «Нужны только сумма и счёт», later «Можно складывать…» and «Это возврат»; one a day, «Понятно» closes for good."""
    return board('Шторка ввода: подсказка', entry_from(simple_amount('0', color=MUTED2), cat='', det=plain_details())
                 + coach_strip('Нужны только сумма и счёт. Остальное можно пропустить.'))


def hint_row(text):
    return ('<div style="display: flex; align-items: flex-start; gap: 4px; margin-bottom: 10px;"><div style="flex-grow: 1;">%s</div>%s</div>'
            % (hint_card('A', text), icon_button('close', 'Закрыть подсказку', MUTED)))


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


def _ops_rows(bottom_overlay):
    rows = ''.join(tx(*r) for r in RECENT)
    return board('Операции', tab_screen('Операции', '<div style="margin: 8px -16px 0;">%s%s</div>' % (day('СЕГОДНЯ', '−45 100', 'ARS'), rows), 'ops') + bottom_overlay)


def saved_snack():
    """#325: after a NEW operation is saved, «Сохранено · сумма · Отменить» for 4 s (the same window lets a shake undo it); edits show nothing."""
    return _ops_rows(snackbar('Сохранено · 3 500 ARS', 'Отменить', 96))


def undone_snack():
    """#325: a shake in that window removes the save: «Отменено · сумма · Вернуть» (4 s); later a shake opens the receipt scanner as before."""
    return _ops_rows(snackbar('Отменено · 3 500 ARS', 'Вернуть', 96))


def shake_hint():
    """#325 gesture hint (once, after the first saved operation, 0,6 s after its snackbar left): `CoShellCoachCard`, «Понятно»."""
    card_html = ('<div style="position: absolute; left: 16px; right: 16px; bottom: 96px; padding: 12px 8px 4px 16px; border-radius: 24px; background: %s; border: 1px solid %s;">'
                 '<div style="font-size: 14px; line-height: 1.4; padding-right: 12px;">Ошиблись? Сразу после сохранения встряхните телефон — операция отменится.</div>'
                 '<div style="text-align: right; color: %s; font-size: 14px; font-weight: 700; padding: 8px 12px;">Понятно</div></div>' % (KEY, LINE2, LIME))
    return _ops_rows(card_html)


def pin_forgot():
    """#602 (poll 2026-11-23 «А»): right after the PIN is turned on, one warm moment names the way back; it shows once."""
    from mockup_screens_fb_privacy import settings
    return board('Конфиденциальность: PIN включён', settings(True) + pill_box('PIN включён. Забудете — на экране входа нажмите «Забыли PIN?».', 'check', two=True))


BOARDS = [  # name, title, fn, issue number
    ('Hint513Home0', '#513 · «Обзор», новичок: строка «Первые шаги»', home_new, 513),
    ('Hint513Home2', '#513 · «Обзор», два шага из четырёх', home_two, 513),
    ('Hint513Steps', '#513 · шторка «Первые шаги»', steps_sheet, 513),
    ('Hint513Word', '#513 · П1: подсказка-слово на «Счетах»', accounts_hint, 513),
    ('Hint513Gesture', '#513 · П2: подсказка-жест в «Операциях»', gesture, 513),
    ('Hint513Moment', '#513 · тёплая плашка: первая запись', moment, 513),
    ('Hint513MomentLong', '#513 · тёплая плашка: две строки + «Открыть»', moment_long, 513),
    ('Hint513Finale', '#513 · финал «Первых шагов»: «Всё готово»', finale, 513),
    ('Hint513Reset', '#513 · «Подсказки включены» после сброса', hints_reset, 513),
    ('Hint513EntryCoach', '#513 · шторка ввода: карточка «Понятно» над шторкой', entry_hint, 513),
    ('Hint513Tags', '#513 · «Теги»: подсказка под заголовком', tags_hint, 513),
    ('Hint513Currency', '#513 · «Валюта покупки»: подсказка под заголовком', currency_hint, 513),
    ('Hint602PinForgot', '#602 · плашка после включения PIN: «Забыли PIN?»', pin_forgot, 602),
    ('Hint513Settings', '#513 · «Ещё → Приложение»: подсказки', app_settings, 513),
    ('Undo325Saved', '#325 · снэкбар «Сохранено · Отменить» (4 с)', saved_snack, 325),
    ('Undo325Shaken', '#325 · встряхнули: «Отменено · Вернуть»', undone_snack, 325),
    ('Hint325Shake', '#325 · подсказка-жест «Встряхните» (один раз)', shake_hint, 325),
]
