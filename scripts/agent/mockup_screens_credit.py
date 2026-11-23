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


BOARDS = [('CreditEdit', edit), ('CreditTerms', sheet_pct), ('CreditTermsSum', sheet_sum), ('AccountsCredit', credit_list), ('HomeCreditSoon', home_soon)]
