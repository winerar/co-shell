"""Receipt ↔ account by card digits (#577, #646, #578; poll 2026-11-23: transfer «А», digits «А»).

- `EntryReceiptTransfer` — a receipt «Перевод между своими счетами» opens the sheet as «Перевод»: both accounts found by the digits.
- `ReceiptWhichAccount` — digits not known yet: the choice sheet «Какой это счёт?», the answer is remembered in the account.
- `AccountDigits` — the account's «Для чеков» tile opens a small sheet with the digits (`AccountEdit` shows the tile).
Imported by mockup_screens_forms.py (boards) — titles and rows are in gen-mockups.py.
"""
from mockup_bricks import *  # noqa: F401,F403
import mockup_screens_entry as e
import mockup_screens_fb_290 as p
import mockup_screens_fb_modals as md
import mockup_screens_main as m

DOC = '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="%s" stroke-width="1.6"><path d="M7 3h7l5 5v13H7zM14 3v5h5M10 13h6M10 17h6"/></svg>'
AMERIA = ('AM', '#B01E2F', '#FFFFFF')


def source_line(text='Из чека · Америабанк'):
    return ('<div style="display: flex; align-items: center; gap: 6px; font-size: 12px; color: %s;">%s<span>%s</span></div>' % (MUTED, DOC % MUTED, text))


def receipt_transfer():
    """Both accounts found by the digits: «Откуда» …3900, «Куда» …4906; the same sum on both sides (one currency) — no second amount."""
    sums = e.amount_card('3 000', 'AMD', INK, source_line(), links='')
    return board('Перевод из чека', e.sheet(e.type_bar('tr') + e.ribbon(2, 'Откуда') + e.ribbon(0, 'Куда'), sums, e.details(refund=False)))


def choice_row(name, on):
    mark = svg('check', LIME, 22, 2.2) if on else '<span style="width: 22px;"></span>'
    return ('<div style="display: flex; align-items: center; gap: 12px; height: 52px; padding: 0 4px;">%s<span style="flex-grow: 1; font-size: 16px; %s">%s</span>%s</div>'
            % (badge(AMERIA[0], AMERIA[1], AMERIA[2], 36, 10), 'font-weight: 700;' if on else '', name, mark))


def which_account():
    """ChoiceSheet: «Какой это счёт?» — the card of the receipt is not linked yet; the choice fills «Для чеков» of the account."""
    rows = '<div>%s%s%s</div>' % (choice_row('Ameria · карта', True), choice_row('Ameria · накопительный', False), choice_row('Galicia', False))
    note = '<div style="margin: 8px 0 12px; font-size: 12px; color: %s;">Запомним: в следующий раз выберем сами.</div>' % MUTED
    under = e.sheet(e.type_bar('exp') + e.ribbon(0), e.amount_card('3 000', 'AMD', PINK, source_line(), links=''), e.categories(), e.details())
    return board('Какой это счёт?', md.modal_sheet(under, 'Какой это счёт?', rows + note + button('Запомнить', mt=0),
                                                     'В чеке карта …3900. Выберите счёт, и в следующий раз чек найдёт его сам.', top=330))


def digits_sheet():
    """FormSheet over the account editor: the last digits of the card or the account number; the keypad is digits only."""
    body = (field('Последние цифры', '3900', hint='Из чека банка: «4083********3900» → 3900', mono=True)
            + '<div style="height: 12px;"></div>' + button('Готово', mt=0))
    return board('Цифры карты или счёта', md.modal_sheet(p.acc_form_a(), 'Цифры карты или счёта', body, 'По ним чек из банка найдёт этот счёт.', top=420))


BOARDS = [('EntryReceiptTransfer', receipt_transfer), ('ReceiptWhichAccount', which_account), ('AccountDigits', digits_sheet)]
