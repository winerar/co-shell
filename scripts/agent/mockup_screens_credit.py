"""Credit card account, step 1 (#686 of #634; poll 2026-11-23, all «А», owner's note: the minimum payment may be a sum too).

- `CreditEdit` — the editor: type «Кредитка», one tile «Условия карты» (the conditions sheet keeps the editor without scrolling).
- `CreditTerms` / `CreditTermsSum` — the sheet «Условия карты»: limit, the day of the month, the minimum payment as «% долга» or «Суммой».
- `AccountsCredit` — the list: the row says the payment date, a meter «Доступно … из …» under it (no screen of its own).
- `HomeCreditSoon` — «Скоро · 7 дней» on «Обзор»: the monthly payment, «Провести» records a transfer to the card.
Imported by mockup_screens_forms.py. Poll variants (not chosen) lived in mockup_poll_credit634.py.
"""
from mockup_parts import *  # noqa: F401,F403
import mockup_poll_credit634 as P
import mockup_screens_fb_290 as p
from mockup_catalog_bricks import status_chip
import mockup_screens_fb_modals as md
import mockup_screens_main as m


def edit():
    return board('Кредитка: редактор счёта', P.form())


def terms(sum_mode):
    f = lambda lbl, val, hint='': field(lbl, val, hint=hint, mono=True)  # noqa: E731
    min_pay = ('<div style="margin-top: 4px;">%s</div>' % p.seg_small(['% долга', 'Суммой'], 'Суммой' if sum_mode else '% долга')
               + f('Минимальный платёж', '9 000 AMD' if sum_mode else '5 %', 'Сумма или процент — как в договоре'))
    beta = ('<div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">%s<span style="font-size: 12px; color: %s;">Новая функция: сверьте «Доступно» и платёж с выпиской банка.</span></div>' % (status_chip('бета', 'manual'), MUTED))
    rows = beta + f('Кредитный лимит', '500 000 AMD') + f('Платёж до (число месяца)', '15') + min_pay
    return board('Условия карты', md.modal_sheet(P.form(), 'Условия карты', rows + button('Готово', mt=8), 'Приложение посчитает «Доступно» и срок платежа.', top=230))


def sheet_pct():
    return terms(False)


def sheet_sum():
    return terms(True)


def credit_list():
    return P.cc_list()


def home_soon():
    return P.cc_soon()


def terms_grace():
    """Step 2 (#634, poll «А»: the grace period is a reminder only, no interest is counted): two more optional fields — the statement day and the grace days."""
    f = lambda lbl, val, hint='': field(lbl, val, hint=hint, mono=True)  # noqa: E731
    min_pay = p.seg_small(['% долга', 'Суммой'], '% долга') + f('Минимальный платёж', '5 %')
    rows = (f('Кредитный лимит', '500 000 AMD') + f('Платёж до (число месяца)', '15') + min_pay
            + f('День выписки (число месяца)', '25', 'Не обязательно') + f('Льготный период, дней', '25', 'Не обязательно: покажем, до какого числа без процентов'))
    return board('Условия карты: льготный период', md.modal_sheet(P.form(), 'Условия карты', rows + button('Готово', mt=8), 'Проценты приложение не считает — только напоминает срок.', top=60))


def list_grace():
    """The row says the interest-free day beside the payment day; the meter stays."""
    card = ('Ameria · кредитка', 'AM', P.COLOR, '#FFFFFF', 'Кредитная карта · платёж до 15 дек · без процентов до 19 дек', '−180 000', 'AMD', '−460')
    other = acc_row(('Galicia', 'GA', '#F26E22', '#1A0A00', 'Карта', '1 250 000', 'ARS', '1 000'))
    row_ = acc_row(card) + '<div style="padding: 0 16px 14px;">%s</div>' % P.meter()
    body = big_total('Всего в общем балансе · 2 счёта', '540', 'USD') + group_head('Карты', 2, '540') + group([other, row_])
    return board('Счета: кредитка, льготный период', inner_screen('Счета', body) + fab('Добавить счёт'))


def soon_grace():
    """«Скоро»: the payment line gets a quiet second line with the interest-free day (never red)."""
    r = ('<div style="display: flex; align-items: center; gap: 10px; padding: 6px 0;">%s<span style="flex-grow: 1;"><div style="font-size: 15px;">Платёж по кредитке</div>'
         '<div style="font-size: 12px; color: %s;">Ameria · кредитка · 15 дек</div><div style="font-size: 12px; color: %s;">без процентов до 19 дек</div></span><span style="%s font-size: 15px;">9 000 AMD</span></div>'
         % (badge('AM', P.COLOR, '#FFFFFF', 40, 12), MUTED, MUTED, MONO))
    soon = p.block('Скоро · 7 дней', r + button('Провести', mt=8), right=p.small_link('Все 3 ›'))
    return board('Обзор: платёж по кредитке и льготный период', tab_screen('Обзор', m.balance_hero() + '<div style="height: 10px;"></div>' + soon, 'home'))


BOARDS = [('CreditEdit', edit), ('CreditTerms', sheet_pct), ('CreditTermsSum', sheet_sum), ('AccountsCredit', credit_list), ('HomeCreditSoon', home_soon),
          ('CreditTermsGrace', terms_grace), ('AccountsCreditGrace', list_grace), ('HomeCreditGrace', soon_grace)]
