"""Entry sheet boards in one step (#505, = `transactions/edit/components/DetailsStep.kt`).

Type + paste + camera + templates (new only) → ribbon of account badges («Откуда»/«Куда» for a transfer) → cards «Сумма»,
«Категория · частые» (not for a transfer), «Детали» → keypad with «Готово». Used by `Main` (interactive, `mockup_screens_tabs.py`)
and the static `Entry*` boards listed in `mockup_screens_main.BOARDS`.
"""
from mockup_parts import *  # noqa: F401,F403
from mockup_catalog_bricks import status_chip
from mockup_logic import go
import mockup_screens_main as m
import mockup_screens_fb_290 as p
import mockup_screens_fb_qr526 as q

TYPES = [('exp', 'Расход', PINK), ('inc', 'Доход', CYAN), ('tr', 'Перевод', INK)]
TYPE_ICON = {'exp': 'out', 'inc': 'in', 'tr': 'swaph'}  # CoShellSegmented(icons): the words do not fit next to three buttons
RIBBON = 4  # badges before «Все ›» on 390 dp


def type_bar(cur, taps=False):
    """CoShellSegmented + paste (not for a transfer) + QR camera + «Шаблоны»; `taps` — each type is a tap target `go_t_<key>` (interactive board)."""
    def cell(k, name, _):
        on = k == cur
        name = svg(TYPE_ICON[k], ONLIME if on else MUTED, 20)
        style = ('background: %s; color: %s; font-weight: 700; box-shadow: inset 0 0 0 2px %s;' % (LIME, ONLIME, ONLIME) if on
                 else 'color: %s;' % MUTED)
        html = ('<span style="flex: 1; height: 40px; border-radius: 999px; %s font-size: 14px; display: flex; align-items: center; '
                'justify-content: center;">%s</span>' % (style, name))
        return go('t', k, html) if taps else html
    pill = ('<span style="flex-grow: 1; height: 48px; border-radius: 999px; background: %s; display: flex; align-items: center; padding: 4px; gap: 4px;">%s</span>'
            % (BAR, ''.join(cell(*x) for x in TYPES)))
    paste = q.round_btn(q.CLIP % MUTED) if cur != 'tr' else ''
    return ('<div style="display: flex; align-items: center; gap: 4px;">%s%s%s%s</div>'
            % (pill, paste, q.round_btn(q.QR % LIME), q.round_btn(svg('list', MUTED, 22))))


def ribbon(chosen=0, caption=''):
    """CoShellAccountRibbon: badges 44, the chosen one in a theme ring, «Все ›» at the end (board `CompChoice`)."""
    cells = ''.join('<span style="display: flex; border-radius: 14px; %s">%s</span>'
                    % ('box-shadow: 0 0 0 2px %s, 0 0 0 4px %s;' % (SHEET, LIME) if i == chosen else '', badge(a[1], a[2], a[3], 44, 12))
                    for i, a in enumerate(m.ACC[:RIBBON]))
    more = ('<span style="height: 44px; padding: 0 14px; border-radius: 999px; display: inline-flex; align-items: center; font-size: 14px; background: %s; color: %s;">Все ›</span>'
            % (CHIP, INK2))
    cap = '<span style="font-size: 12px; color: %s; width: 46px;">%s</span>' % (MUTED, caption) if caption else ''
    return '<div style="display: flex; align-items: center; gap: 10px; padding: 0 4px;">%s%s%s</div>' % (cap, cells, more)


def big(value, cur, color):
    return ('<div style="display: flex; align-items: baseline; justify-content: center; gap: 8px;"><span style="%s font-size: 40px; font-weight: 700; color: %s;">%s</span>'
            '<span style="%s font-size: 16px; color: %s;">%s</span></div>' % (MONO, color, value, MONO, MUTED, cur))


def slot(lbl, value, cur, hint=''):
    """AmountSlot (small): the second amount of a transfer, «Сумма покупки» of a receipt."""
    h = '<div style="font-size: 12px; color: %s; margin-top: 4px;">%s</div>' % (MUTED, hint) if hint else ''
    return ('<div style="margin-top: 8px; padding-top: 8px; border-top: 1px solid %s;"><div style="font-size: 11px; color: %s;">%s</div>'
            '<div style="display: flex; align-items: baseline; gap: 6px;"><span style="%s font-size: 20px; font-weight: 700;">%s</span>'
            '<span style="%s font-size: 13px; color: %s;">%s</span></div>%s</div>' % (LINE2, MUTED, lbl, MONO, value, MONO, MUTED, cur, h))


HISTORY = 'M4 12a8 8 0 1 0 3-6.2M4 4v4h4M12 8v4l3 2'


def guess_caption(source):
    """The grey line of an automatic category (#584; `GuessCaption`): where it came from."""
    icon = svg(HISTORY, MUTED, 14, 2)
    word = 'по прошлым операциям'
    return ('<span style="display: flex; align-items: center; gap: 4px; font-size: 12px; color: %s; white-space: nowrap;">%s%s</span>' % (MUTED, icon, word))


def categories(selected=None, error=False, caption=''):
    cells = [p.cat_cell(n, ic, col, n == selected) for n, ic, col in m.CATS[:7]] + [p.all_cell('Все')]
    err = '<div style="font-size: 12px; color: %s; margin-top: 6px;">Выберите категорию</div>' % PINK if error else ''
    title = 'Категория' if selected else 'Категория · частые'
    return p.block(title, p.grid4(cells) + err, right=caption or (p.small_link(selected + ' ✓') if selected else ''))


def detail_chip(icon, text='', fixed=False):
    """CoShellDetailChip (#818): pill 40 — icon + one line; an empty detail is the icon alone."""
    pad = 'padding: 0 14px 0 12px;' if text else 'width: 40px; justify-content: center;'
    word = ('<span style="font-size: 14px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: %s;">%s</span>' % (INK, text)) if text else ''
    return ('<span style="height: 40px; box-sizing: border-box; border-radius: 999px; background: %s; display: flex; align-items: center; gap: 8px; min-width: 0; %s%s">%s%s</span>'
            % (CHIP, pad, 'flex: 0 1 auto;' if text and not fixed else 'flex: none;', svg(icon, MUTED, 18), word))


def details(day='Сегодня', note=None, refund=True, save_as=False, payee=None, tags=None):
    """«Детали» in one line of chips, no card (#818, poll «А»): date and payee in words, empty note and tags as icons.
    `refund`/`save_as` stay for the callers: «Возврат» is in the «Сумма» caption, «В шаблон» — in the top row of a saved one."""
    chips = detail_chip('calendar', day, fixed=True) + (detail_chip('cart', payee) if payee else '') + detail_chip('note', note or '') + detail_chip('tag', tags or '')
    return '<div style="display: flex; gap: 6px;">%s</div>' % chips


def sheet(top, *cards, keys=None):
    """`top` stays; the cards sit in a box that is cut at the keypad, as the app scrolls them on a short screen (`DetailsStep`)."""
    middle = ('<div style="flex: 1 1 0; min-height: 0; overflow: hidden; display: flex; flex-direction: column; gap: 8px;">%s</div>' % ''.join(cards))
    return p.frame(top + middle + (keys or m.keypad()))


def amount_card(value, cur, color, extra='', debited=False, links='+ Сумма покупки', refund=None):
    """`refund` — «Возврат» of an expense on the left of the links (#818); by default — whenever there are links (not a transfer)."""
    ref = bool(links) if refund is None else refund
    words = ('Возврат' + ('&nbsp;&nbsp;&nbsp;' if links else '') if ref else '') + links
    return p.block('Сумма · списано' if debited else 'Сумма', big(value, cur, color) + extra, right=p.small_link(words) if words else '')


def receipt_line(value='13 300', cur='ARS', rate='1 USDT = 1 015 ARS'):
    """«Покупка 13 300 ARS ▾ · курс · ✕» in one line (#818): CoShellAmountSlot(compact) + the rate + ✕."""
    pill = ('<span style="height: 40px; padding: 0 4px 0 14px; border-radius: 999px; background: %s; box-shadow: inset 0 0 0 1px %s; display: inline-flex; align-items: center; gap: 8px; '
            'white-space: nowrap; flex: none;"><span style="font-size: 12px; color: %s;">Покупка</span><b style="%s font-size: 16px;">%s</b><span style="height: 32px; padding: 0 12px; '
            'border-radius: 999px; box-shadow: inset 0 0 0 1px %s; display: inline-flex; align-items: center; font-size: 13px;">%s ▾</span></span>' % (FIELD, LINE2, MUTED, MONO, value, LINE2, cur))
    return ('<div style="display: flex; align-items: center; gap: 4px; margin-top: 8px;">%s<span style="flex: 1; font-size: 11px; color: %s; padding-left: 4px;">%s</span>%s</div>'
            % (pill, MUTED, rate, icon_button('close', 'Убрать сумму покупки', INK)))


# ---- static boards ------------------------------------------------------------------------------------------------

def transfer():
    return sheet(type_bar('tr') + ribbon(2, 'Откуда') + ribbon(0, 'Куда'),
                 amount_card('500', 'USD', INK, slot('Сумма зачисления', '512 000', 'ARS', 'Сколько пришло на «Galicia». Курс посчитается сам.'), links='', refund=False),
                 details(refund=False))


def receipt():
    return sheet(type_bar('exp') + ribbon(3), amount_card('13,10', 'USDT', PINK, receipt_line(), debited=True, links='+ Чаевые'), categories('Кафе'),
                 details(payee='Кофейня «Зерно»'))


def guess(source):
    """A new expense whose category was filled by itself: by past entries (#584) or by the model (#594); one tap changes it."""
    return sheet(type_bar('exp') + ribbon(0), amount_card('3 500', 'ARS', PINK), categories('Кафе', caption=guess_caption(source)), details(note='Starbucks'))


def edit_types(cur='exp'):
    """CoShellSegmented of a saved operation: no camera and «Шаблоны» — those are for a new one (#509, poll «А»)."""
    def cell(k, name, _):
        on = k == cur
        name = svg(TYPE_ICON[k], ONLIME if on else MUTED, 20)
        style = ('background: %s; color: %s; font-weight: 700; box-shadow: inset 0 0 0 2px %s;' % (LIME, ONLIME, ONLIME) if on
                 else 'color: %s;' % MUTED)
        return ('<span style="flex: 1; height: 40px; border-radius: 999px; %s font-size: 13px; display: flex; align-items: center; '
                'justify-content: center;">%s</span>' % (style, name))
    return ('<span style="flex-grow: 1; min-width: 0; height: 48px; border-radius: 999px; background: %s; display: flex; align-items: center; '
            'padding: 4px; gap: 2px;">%s</span>' % (BAR, ''.join(cell(*x) for x in TYPES)))


def edit():
    """A saved operation: its type switch next to «Дублировать» and «Удалить» (#509); amount, account and date stay on a change."""
    head = ('<div style="display: flex; align-items: center; gap: 4px;">%s%s%s%s</div>'
            % (edit_types(), icon_button('bookmark_add', 'Сохранить как шаблон', INK, True), icon_button('copy', 'Дублировать', INK, True), icon_button('trash', 'Удалить', PINK, True)))
    return sheet(head + ribbon(0), amount_card('32 000', 'ARS', PINK), categories('Продукты'), details('10 нояб.', 'Coto, на неделю', save_as=True))


def error():
    return sheet(type_bar('exp') + ribbon(0), amount_card('4 500', 'ARS', PINK), categories(error=True), details('Вчера'))


def no_accounts():
    """`TransactionEditScreen` without accounts: the sheet title, one line and «Добавить счёт» — no type switch."""
    inner = ('<div style="padding: 20px 16px; display: flex; flex-direction: column; gap: 16px;"><h2 style="margin: 0; font-size: 22px;">Новая операция</h2>'
             '<div style="font-size: 16px; color: %s;">Чтобы записать операцию, сначала добавьте счёт.</div>%s</div>' % (MUTED, button('Добавить счёт', mt=0)))
    return scrim() + m.sheet(inner, 520)


def all_accounts():
    """«Все ›» of the ribbon (`AccountPicker`) — the same look as «Счета» (owner 2026-11-20: one look for the same data):
    groups by type with title and count, rows (`AccountLine`: badge 40, name, type, balance) in one card, ✓ on the chosen one;
    «Недавние» first, search at the bottom under the thumb."""
    def head(title, count):
        return ('<div style="display: flex; align-items: center; gap: 6px; margin: 14px 4px 8px;"><span style="font-size: 13px; font-weight: 600; color: %s; '
                'letter-spacing: 0.04em; text-transform: uppercase;">%s</span><span style="font-size: 12px; color: %s;">%s</span></div>' % (LIME, title, MUTED, count))

    def line(a, on=False):
        trail = '<span style="display: flex; align-items: center; gap: 8px;">%s%s</span>' % (amount(a[5], a[6], INK, 15), svg('check', LIME, 20) if on else '')
        return row(a[0], a[4], badge(a[1], a[2], a[3], 40, 12), trail)

    chosen = 'Galicia'
    recent = [acc('Galicia'), acc('Песо'), acc('Binance')]
    body = head('Недавние', len(recent)) + group([line(a, a[0] == chosen) for a in recent])
    for g, title, _ in GROUPS:
        items = in_group(g)
        body += head(title, len(items)) + group([line(a, a[0] == chosen) for a in items])
    inner = ('<div style="padding: 8px 16px 0; display: flex; align-items: flex-end;"><h2 style="margin: 0; flex-grow: 1; font-size: 22px;">Откуда списали?</h2>'
             '</div><div style="padding: 0 16px; height: 560px; overflow: hidden;">%s</div>'
             '<div style="padding: 8px 16px 12px;">%s</div>' % (body, search('Найти счёт')))
    return scrim() + m.sheet(inner, 120)
