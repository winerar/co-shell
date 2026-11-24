"""Deposit account, step 3 of #634 (proposal, waits for the owner's «ок»): the conditions the person types once, the expected income the app shows.

- `DepositEdit` — the editor: type «Вклад», one tile «Условия вклада».
- `DepositTerms` — the sheet: rate, the end day, how the interest is paid.
- `AccountsDeposit` — the list: the row says the rate and the end day, a quiet line «Ожидаемый доход».
- `HomeDepositSoon` — «Скоро · 7 дней»: the deposit ends, the interest is expected (no operation is written by itself).
Imported by mockup_screens_forms.py.
"""
from mockup_parts import *  # noqa: F401,F403
import mockup_screens_fb_290 as p
import mockup_screens_fb_modals as md
import mockup_screens_main as m
from mockup_catalog_bricks import status_chip

COLOR = '#2E8B57'
DEP = ('Ameria · вклад', 'AM', COLOR, '#FFFFFF', 'Вклад · 8 % · до 15 мар 2027', '10 000', 'USD', '10 000')


def form():
    hero = p.hero_card('<div style="display: flex; font-size: 12px; color: %s;"><span style="flex-grow: 1;">Ameria · вклад</span><span>Вклад</span></div>'
                       '<div style="margin-top: 10px; %s font-size: 28px; font-weight: 700; color: %s;">10 000,00 <span style="font-size: 14px;">USD</span></div>'
                       % (HERO_HINT, MONO, LIME), pad='12px 14px')
    types = ''.join(p.chip(t, t == 'Вклад').replace('height: 36px', 'height: 32px') for t in ('Наличные', 'Карта', 'Вклад', 'Крипто', 'Другое', 'Проездной', 'Кредитка'))
    money = ('<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px;">%s%s</div>' % (p.detail('lock', 'Валюта', 'USD · заблок.'), p.detail('refresh', 'Баланс сейчас', '10 000,00'))
             + '<div style="height: 6px;"></div>' + p.detail('wallet', 'Условия вклада', '8 % · до 15 марта 2027 · в конце срока'))
    body = (hero + p.name_line('Ameria · вклад', 'wallet', COLOR) + p.block('Деньги', money)
            + p.block('Тип', '<div style="display: flex; flex-wrap: wrap; gap: 6px;">%s</div>' % types) + p.block('Цвет', p.color_row(COLOR)))
    return p.page('Счёт', body)


def edit():
    return board('Вклад: редактор счёта', form())


def terms():
    f = lambda lbl, val, hint='': field(lbl, val, hint=hint, mono=True)  # noqa: E731
    beta = ('<div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">%s<span style="font-size: 12px; color: %s;">Новая функция: сверьте доход с договором банка.</span></div>'
            % (status_chip('бета', 'manual'), MUTED))
    pay = p.seg_small(['В конце срока', 'Каждый месяц', 'К вкладу'], 'В конце срока')
    rows = (beta + f('Ставка, % годовых', '8') + f('Вклад закрывается', '15 марта 2027')
            + '<div style="margin-top: 4px; font-size: 12px; color: %s;">Проценты</div>' % MUTED + pay
            + note('«Каждый месяц» — выплата на счёт, «К вкладу» — проценты прибавляются к вкладу.', mt=6))
    return board('Условия вклада', md.modal_sheet(form(), 'Условия вклада', rows + button('Готово', mt=8), 'Приложение посчитает ожидаемый доход. Операций само не пишет.', top=150))


def list_():
    other = acc_row(('Galicia', 'GA', '#F26E22', '#1A0A00', 'Карта', '1 250 000', 'ARS', '1 000'))
    inc = '<div style="padding: 0 16px 14px; font-size: 12px; color: %s;">Ожидаемый доход 640 USD · 15 мар 2027</div>' % MUTED
    body = big_total('Всего в общем балансе · 2 счёта', '11 000', 'USD') + group_head('Вклады', 1, '10 000') + group([acc_row(DEP) + inc]) + group_head('Карты', 1, '1 000') + group([other])
    return board('Счета: вклад', inner_screen('Счета', body) + fab('Добавить счёт'))


def soon():
    r = ('<div style="display: flex; align-items: center; gap: 10px; padding: 6px 0;">%s<span style="flex-grow: 1;"><div style="font-size: 15px;">Вклад закрывается</div>'
         '<div style="font-size: 12px; color: %s;">Ameria · вклад · 15 мар</div></span><span style="%s font-size: 15px;">+640 USD</span></div>' % (badge('AM', COLOR, '#FFFFFF', 40, 12), MUTED, MONO))
    block = p.block('Скоро · 7 дней', r + note('Проценты придут от банка. Запишите их доходом, когда придут.', mt=4), right=p.small_link('Все 3 ›'))
    return board('Обзор: вклад закрывается', tab_screen('Обзор', m.balance_hero() + '<div style="height: 10px;"></div>' + block, 'home'))


BOARDS = [('DepositEdit', edit), ('DepositTerms', terms), ('AccountsDeposit', list_), ('HomeDepositSoon', soon)]
