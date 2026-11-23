"""Travel pass (SUBE, «Тройка»; #645, poll 2026-11-23 «Б»): an account of type «Проездной» — a top-up is an ordinary expense from a card,
the balance on the pass is a number the person checks by hand («Сверить баланс», the adjustment of #581).
Boards `AccountPass` (editor), `AccountPassCheck` («Сверить баланс» sheet), `AccountsPass` (the list row). Imported by mockup_screens_forms.py.
"""
from mockup_parts import *  # noqa: F401,F403
import mockup_screens_fb_290 as p
import mockup_screens_fb_modals as md

PASS_COLOR = '#00A0DF'
BAL, REAL, DIFF = '2 400,00', '2 100,00', '−300,00'


def pass_form(balance=BAL):
    hero = p.hero_card('<div style="display: flex; font-size: 12px; color: %s;"><span style="flex-grow: 1;">SUBE</span><span>Проездной</span></div>'
                       '<div style="margin-top: 10px; %s font-size: 28px; font-weight: 700; color: %s;">%s <span style="font-size: 14px;">ARS</span></div>'
                       % (HERO_HINT, MONO, LIME, balance), pad='12px 14px')
    money = ('<div style="display: grid; grid-template-columns: 1fr; gap: 6px;">%s</div>' % p.detail('lock', 'Валюта', 'ARS · заблок.')
             + '<div style="height: 6px;"></div>' + p.detail('refresh', 'Баланс на карте · Сверить', balance + ' ARS'))
    types = ''.join(p.chip(t, t == 'Проездной').replace('height: 36px', 'height: 32px') for t in ('Наличные', 'Карта', 'Вклад', 'Крипто', 'Другое', 'Проездной'))
    sw = ('<div style="display: flex; align-items: center; gap: 10px;"><span style="flex-grow: 1; font-size: 14px;">Учитывать в общем балансе'
          '<span style="display: block; font-size: 11px; color: %s;">Выключено: пополнение уже записано расходом</span></span>%s</div>' % (MUTED, switch(False)))
    body = (hero + p.name_line('SUBE', 'wallet', PASS_COLOR) + p.block('Деньги', money)
            + p.block('Тип', '<div style="display: flex; flex-wrap: wrap; gap: 6px;">%s</div>' % types)
            + p.block('Цвет', p.color_row(PASS_COLOR)) + p.block('Баланс', sw))
    return p.page('Счёт', body)


def pass_editor():
    return board('Проездной: редактор счёта', pass_form())


def pass_check():
    """«Сверить баланс»: the real number on the card; «Сохранить баланс» records the difference as a balance adjustment (not spending, not income)."""
    head = ('<div style="padding: 6px 16px 0;"><div style="display: flex; align-items: center; height: 48px;"><b style="flex-grow: 1; font-size: 20px;">Сверить баланс</b></div>')
    body = field('Баланс на карте', REAL + ' <span style="font-size: 14px; color: %s;">ARS</span>' % MUTED,
                 hint='Разница: %s ARS — не трата и не доход' % DIFF, mono=True)
    return board('Проездной: сверить баланс', scrim(pass_form()) + sheet(head + body + button('Сохранить баланс', mt=16) + '</div>', 470))


def pass_list():
    """The pass sits in the list like any account (type «Другое» group); the subtitle says what it is and that it is out of the total, its balance is not counted."""
    card = ('Galicia', 'GA', '#F26E22', '#1A0A00', 'Карта', '1 250 000', 'ARS', '1 000')
    sube = ('SUBE', 'SU', PASS_COLOR, '#FFFFFF', 'Проездной · не в общем балансе', '2 400', 'ARS', '2')
    body = (big_total('Всего в общем балансе · 1 счёт', '1 000', 'USD') + group_head('Карты', 1, '1 000') + group([acc_row(card)])
            + group_head('Другое', 1, '0') + group([acc_row(sube)]))
    return board('Счета: проездной', inner_screen('Счета', body) + fab('Добавить счёт'))


BOARDS = [('AccountPass', pass_editor), ('AccountPassCheck', pass_check), ('AccountsPass', pass_list)]
