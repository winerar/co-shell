#!/usr/bin/env python3
"""Poll boards for #634, step 1 — the credit card account: what conditions to enter, where the debt shows, how to be reminded.

Standalone PNGs (not on the canvas): writes `<out>/mockups/Cc*.dc.html`, renders them dark, `<out>/img/Cc*.png`.
Usage: python3 scripts/agent/mockup_poll_credit634.py <out-dir>   then   poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import shutil
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from mockup_parts import *  # noqa: E402,F401,F403
import mockup_screens_fb_290 as p  # noqa: E402
import mockup_screens_fb_modals as md  # noqa: E402
import mockup_screens_main as m  # noqa: E402

COLOR = '#B01E2F'
CARD = ('Ameria · кредитка', 'AM', COLOR, '#FFFFFF', 'Кредитная карта · платёж до 15 дек', '−180 000', 'AMD', '−460')


def form():
    hero = p.hero_card('<div style="display: flex; font-size: 12px; color: %s;"><span style="flex-grow: 1;">Ameria · кредитка</span><span>Кредитная карта</span></div>'
                       '<div style="margin-top: 10px; %s font-size: 28px; font-weight: 700; color: %s;">−180 000,00 <span style="font-size: 14px;">AMD</span></div>'
                       % (HERO_HINT, MONO, LIME), pad='12px 14px')
    types = ''.join(p.chip(t, t == 'Кредитка').replace('height: 36px', 'height: 32px') for t in ('Наличные', 'Карта', 'Вклад', 'Крипто', 'Другое', 'Проездной', 'Кредитка'))
    money = ('<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px;">%s%s</div>' % (p.detail('lock', 'Валюта', 'AMD · заблок.'), p.detail('refresh', 'Баланс сейчас', '−180 000,00'))
             + '<div style="height: 6px;"></div>' + p.detail('wallet', 'Условия карты', 'Лимит 500 000 · до 15-го · от 5 %'))
    body = (hero + p.name_line('Ameria · кредитка', 'wallet', COLOR) + p.block('Деньги', money)
            + p.block('Тип', '<div style="display: flex; flex-wrap: wrap; gap: 6px;">%s</div>' % types) + p.block('Цвет', p.color_row(COLOR)))
    return p.page('Счёт', body)


def sheet_rows(full):
    f = lambda lbl, val, hint='': field(lbl, val, hint=hint, mono=True)  # noqa: E731
    rows = f('Кредитный лимит', '500 000 AMD') + f('Платёж до (число месяца)', '15') + f('Минимальный платёж', '5 % долга')
    if full:
        rows += f('Льготный период', '55 дней', 'Без процентов, если успеть погасить') + f('Ставка', '24 % годовых', 'Проценты на долг считаются сами') + f('День выписки', '25')
    return rows


def sheet(full):
    sub = 'Приложение посчитает «Доступно» и платёж.' if not full else 'Считаем доступно, платёж, льготный период и проценты.'
    return board('Условия кредитной карты', md.modal_sheet(form(), 'Условия карты', sheet_rows(full) + button('Готово', mt=8), sub, top=130 if full else 330))


def cc_min():
    return sheet(False)


def cc_full():
    return sheet(True)


def meter():
    return progress(0.36, mt=0) + '<div style="margin-top: 6px; font-size: 12px; color: %s;">Доступно 320 000 из 500 000 AMD</div>' % MUTED


def cc_list():
    """A — in the list: the debt and the payment date in the row, a meter under it."""
    row_ = acc_row(CARD) + '<div style="padding: 0 16px 14px;">%s</div>' % meter()
    other = acc_row(('Galicia', 'GA', '#F26E22', '#1A0A00', 'Карта', '1 250 000', 'ARS', '1 000'))
    body = big_total('Всего в общем балансе · 2 счёта', '540', 'USD') + group_head('Карты', 2, '540') + group([other, row_])
    return board('Счета: кредитка', inner_screen('Счета', body) + fab('Добавить счёт'))


def cc_detail():
    """B — a screen of its own: the debt, what is left, the next payment and «Внести платёж»."""
    hero = p.hero_card('<div style="font-size: 12px; color: %s;">Долг</div><div style="margin-top: 6px; %s font-size: 30px; font-weight: 700; color: %s;">180 000,00 <span style="font-size: 15px;">AMD</span></div>%s'
                       % (HERO_HINT, MONO, LIME, '<div style="margin-top: 10px;">%s</div>' % meter()), pad='16px 18px')
    pay = ('<div style="display: flex; align-items: center; gap: 10px;"><span style="flex-grow: 1;"><div style="font-size: 15px;">до 15 декабря</div>'
           '<div style="font-size: 12px; color: %s;">минимум 9 000 AMD · осталось 22 дня</div></span>%s</div>' % (MUTED, button('Внести платёж', mt=0).replace('width: 100%', 'width: auto')))
    ops = group([row('Продукты', 'Вчера', badge('ПР', '#8BC34A', '#0B0F0A', 40, 20), amount('−12 400', 'AMD', INK, 15)),
                 row('Платёж по карте', '15 нояб.', badge('ПЛ', '#C79BFF', '#0B0F0A', 40, 20), amount('+20 000', 'AMD', CYAN, 15))])
    body = ('<div style="display: flex; flex-direction: column; gap: 10px; padding-top: 4px;">%s%s%s</div>' % (hero, p.block('Ближайший платёж', pay), ops))
    return board('Кредитка: экран счёта', inner_screen('Ameria · кредитка', body))


def cc_soon():
    """Reminder through «Скоро» of «Обзор» (the regular payments of #143): a monthly line, «Провести» records the payment."""
    r = ('<div style="display: flex; align-items: center; gap: 10px; padding: 6px 0;">%s<span style="flex-grow: 1;"><div style="font-size: 15px;">Платёж по кредитке</div>'
         '<div style="font-size: 12px; color: %s;">Ameria · кредитка · 15 дек</div></span><span style="%s font-size: 15px;">9 000 AMD</span></div>'
         % (badge('AM', COLOR, '#FFFFFF', 40, 12), MUTED, MONO))
    soon = p.block('Скоро · 7 дней', r + button('Провести', mt=8), right=p.small_link('Все 3 ›'))
    return board('Обзор: платёж по кредитке', tab_screen('Обзор', m.balance_hero() + '<div style="height: 10px;"></div>' + soon, 'home'))


BOARDS = [('CcMin', cc_min), ('CcFull', cc_full), ('CcList', cc_list), ('CcDetail', cc_detail), ('CcSoon', cc_soon)]


def main():
    out = sys.argv[1]
    mk, img = os.path.join(out, 'mockups'), os.path.join(out, 'img')
    for d in (mk, img):
        os.makedirs(d, exist_ok=True)
    files = []
    for name, fn in BOARDS:
        path = os.path.join(mk, name + '.dc.html')
        open(path, 'w', encoding='utf-8').write(fn())
        files.append(path)
    raw = os.path.join(out, 'raw')
    subprocess.run(['node', os.path.join(HERE, 'render-mockups.js'), raw, 'dark', 'Вольт', *files], check=True)
    for name, _ in BOARDS:
        shutil.move(os.path.join(raw, '%s.dark.Вольт.png' % name), os.path.join(img, name + '.png'))
    print('boards → %s' % img)


if __name__ == '__main__':
    main()
