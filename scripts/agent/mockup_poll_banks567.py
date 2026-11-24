#!/usr/bin/env python3
"""Poll boards for #567 (bank/service picker by country) and #605 (more currencies, currency icons), session 2026-11-23.
Usage: python3 scripts/agent/mockup_poll_banks567.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402

BANKS = {
    'Аргентина': [('Galicia', 'GA', '#F26E22'), ('Santander', 'SA', '#EC0000'), ('BBVA', 'BB', '#00278E'), ('Nación', 'BN', '#00738D'),
                  ('Mercado Pago', 'MP', '#00B1EA'), ('Ualá', 'UA', '#062095'), ('Naranja X', 'NX', '#FE5000'), ('Brubank', 'BR', '#6A2EE8'), ('Lemon', 'LE', '#00E36B')],
    'Армения': [('Ameria', 'AM', '#68B92D'), ('Inecobank', 'IN', '#0096D6'), ('Ardshinbank', 'AR', '#0A61C7'), ('ACBA', 'AC', '#E30613'),
                ('IDBank', 'ID', '#F7A800'), ('Evocabank', 'EV', '#00A19B')],
    'Россия': [('Сбер', 'СБ', '#21A038'), ('Т-Банк', 'Т', '#FFDD2D'), ('Альфа', 'АБ', '#EF3124'), ('ВТБ', 'ВТ', '#0A2896'), ('Ozon Банк', 'OZ', '#005BFF'), ('Райффайзен', 'РА', '#FEE600')],
    'Мир': [('Binance', 'BI', '#F0B90B'), ('Bybit', 'BY', '#F7A600'), ('Tether', 'TE', '#26A17B'), ('Wise', 'WI', '#9FE870'), ('PayPal', 'PP', '#003087'), ('Revolut', 'RE', '#6A5CFF')],
}


def sq(t, ini, color, size=40):
    ink = '#000000' if sum(int(color[i:i + 2], 16) * w for i, w in ((1, .3), (3, .59), (5, .11))) > 150 else '#FFFFFF'
    return ('<span style="width:%dpx;height:%dpx;border-radius:12px;background:%s;color:%s;display:inline-flex;align-items:center;justify-content:center;'
            'font-size:%dpx;font-weight:700;flex:none;box-shadow:inset 0 0 0 1px %s">%s</span>' % (size, size, color, ink, size // 3 + 2, C.mix(color, '#000000', .35), ini))


def search(t, hint):
    return ('<div style="margin:8px 16px 0;height:48px;border-radius:999px;background:%s;display:flex;align-items:center;gap:8px;padding:0 16px;font-size:15px;color:%s">'
            '%s%s</div>' % (t['wash'], t['muted'], C.glyph('M11 4a7 7 0 100 14 7 7 0 000-14zM21 21l-5-5', t['muted'], 20), hint))


def chips(t, labels, sel):
    return '<div style="display:flex;gap:8px;margin:10px 16px 0;overflow:hidden">%s</div>' % ''.join(C.pill(t, s, i == sel) for i, s in enumerate(labels))


def bank_cell(t, n, ini, col, on=False):
    ring = 'box-shadow:0 0 0 2px %s;border-radius:14px;' % C.edge(t) if on else ''
    return ('<div style="width:76px;display:flex;flex-direction:column;align-items:center;gap:4px;font-size:12px;text-align:center;%s padding:4px 0">%s<span style="white-space:nowrap;'
            'overflow:hidden;text-overflow:ellipsis;max-width:72px">%s</span></div>' % (ring, sq(t, ini, col, 44), n))


def bank_row(t, n, ini, col, sub, on=False):
    chk = C.glyph('M5 12l5 5 9-10', C.edge(t), 22) if on else ''
    return ('<div style="display:flex;align-items:center;gap:12px;padding:6px 16px;min-height:52px">%s<div style="flex:1;font-size:16px">%s</div>'
            '<span style="font-size:13px;color:%s">%s</span>%s</div>' % (sq(t, ini, col, 36), n, t['muted'], sub, chk))


def hdr(t, text):
    return '<div style="padding:12px 16px 2px;font-size:13px;font-weight:600;letter-spacing:.04em;color:%s">%s</div>' % (t['muted'], text.upper())


def account_form(t):
    return (C.card(t, 'Новый счёт', '', '<div style="height:56px;border-radius:16px;border:1px solid %s;display:flex;align-items:center;padding:0 16px;margin-top:10px;font-size:16px">Galicia USD</div>'
                   '<div style="display:flex;align-items:center;gap:12px;margin-top:12px;height:48px;padding:0 12px;border-radius:999px;background:%s">%s<span style="flex:1">Банк или сервис</span>'
                   '<span style="color:%s">Galicia ›</span></div>' % (t['line'], t['wash'], sq(t, 'GA', '#F26E22', 28), t['muted'])))


# ---------- #567
def bank_a(t):
    cells = ''.join(bank_cell(t, *b, on=(i == 0)) for i, b in enumerate(BANKS['Аргентина']))
    body = (search(t, 'Найти банк или сервис') + chips(t, ['Аргентина', 'Армения', 'Россия', 'Мир'], 0) +
            '<div style="display:flex;flex-wrap:wrap;gap:6px 0;margin:12px 12px 0">%s</div>' % cells +
            '<div style="margin:14px 16px 0">%s</div>' % C.button(t, 'Свой банк', 'background:%s;color:%s;box-shadow:none' % (t['wash'], t['ink'])))
    return account_form(t) + C.sheet(t, 'Банк или сервис', body, 250, sub='Подставит цвет и буквы значка')


def bank_b(t):
    rows = ''
    for g in ('Аргентина', 'Армения', 'Россия', 'Мир'):
        rows += hdr(t, g) + ''.join(bank_row(t, n, i, c, g if g != 'Мир' else 'международный', on=(n == 'Galicia')) for n, i, c in BANKS[g][:4])
    body = search(t, 'Найти банк или сервис') + '<div style="margin-top:4px;height:520px;overflow:hidden">%s</div>' % rows
    return account_form(t) + C.sheet(t, 'Банк или сервис', body, 250, sub='Список по странам, поиск находит по любой части')


def bank_c(t):
    """Same as B but a country is chosen once in settings-like row: only «my» countries first, others behind «Другие страны»."""
    rows = hdr(t, 'Ваши страны') + ''.join(bank_row(t, n, i, c, 'Аргентина') for n, i, c in BANKS['Аргентина'][:5]) + \
        ''.join(bank_row(t, n, i, c, 'Армения') for n, i, c in BANKS['Армения'][:3]) + hdr(t, 'Международные') + \
        ''.join(bank_row(t, n, i, c, '') for n, i, c in BANKS['Мир'][:3]) + \
        '<div style="padding:12px 16px;color:%s;font-size:15px">Другие страны ›</div>' % ink2(t)
    body = search(t, 'Найти банк или сервис') + '<div style="margin-top:4px;height:520px;overflow:hidden">%s</div>' % rows
    return account_form(t) + C.sheet(t, 'Банк или сервис', body, 250, sub='Сначала страны, где у вас счета; остальные — одним нажатием')


# ---------- #605
CUR = [('USD', 'Доллар США', '🇺🇸'), ('EUR', 'Евро', '🇪🇺'), ('ARS', 'Аргентинское песо', '🇦🇷'), ('AMD', 'Армянский драм', '🇦🇲'), ('RUB', 'Российский рубль', '🇷🇺'),
       ('GBP', 'Фунт стерлингов', '🇬🇧'), ('TRY', 'Турецкая лира', '🇹🇷'), ('GEL', 'Грузинский лари', '🇬🇪'), ('CNY', 'Китайский юань', '🇨🇳'), ('BRL', 'Бразильский реал', '🇧🇷')]
CRYPTO = [('USDT', 'Tether', '₮', '#26A17B'), ('BTC', 'Bitcoin', '₿', '#F7931A'), ('ETH', 'Ethereum', 'Ξ', '#627EEA'), ('USDC', 'USD Coin', '$', '#2775CA'), ('TON', 'Toncoin', 'T', '#0098EA')]


def cur_icon(t, sym, color=None, size=36):
    bg = color or C.mix(t['page'], t['acc'], .3)
    return ('<span style="width:%dpx;height:%dpx;border-radius:50%%;background:%s;display:inline-flex;align-items:center;justify-content:center;font-size:%dpx;flex:none;color:%s">%s</span>' % (
        size, size, bg, size // 2, '#FFFFFF' if color else t['ink'], sym))


def cur_row(t, code, name, sym, color=None, on=False):
    chk = C.glyph('M5 12l5 5 9-10', C.edge(t), 22) if on else ''
    return ('<div style="display:flex;align-items:center;gap:12px;padding:6px 16px;min-height:52px">%s<div style="flex:1"><div style="font-size:16px"><b>%s</b></div>'
            '<div style="font-size:13px;color:%s">%s</div></div>%s</div>' % (cur_icon(t, sym, color), code, t['muted'], name, chk))


def cur_a(t):
    rows = hdr(t, 'Частые') + ''.join(cur_row(t, c, n, s, on=(c == 'USD')) for c, n, s in CUR[:5]) + hdr(t, 'Криптовалюты') + ''.join(cur_row(t, c, n, s, col) for c, n, s, col in CRYPTO[:3]) + \
        hdr(t, 'Все валюты') + ''.join(cur_row(t, c, n, s) for c, n, s in CUR[5:8])
    body = search(t, 'Код или название: «lira», «BTC»') + '<div style="margin-top:4px;height:560px;overflow:hidden">%s</div>' % rows
    return C.topbar(t, 'Валюты итогов') + C.sheet(t, 'Добавить валюту', body, 150, sub='Около 150 валют и 20 криптовалют; найдутся по коду и названию')


def cur_b(t):
    chip_row = '<div style="display:flex;flex-wrap:wrap;gap:8px;margin:12px 16px 0">%s</div>' % ''.join(
        C.pill(t, '%s %s' % (s, c), c in ('USD', 'ARS'), 40) for c, n, s in CUR[:8])
    cr = '<div style="display:flex;flex-wrap:wrap;gap:8px;margin:8px 16px 0">%s</div>' % ''.join(C.pill(t, '%s %s' % (s, c), False, 40) for c, n, s, col in CRYPTO)
    body = (search(t, 'Найти валюту') + C.card(t, 'Выбрано', '2', '<div style="margin-top:8px">%s %s</div>' % (C.pill(t, '🇺🇸 USD ✕', True), C.pill(t, '🇦🇷 ARS ✕', True)), 'padding:10px 16px') +
            '<div style="padding:12px 16px 0;font-size:13px;color:%s;font-weight:600">ВАЛЮТЫ</div>' % t['muted'] + chip_row +
            '<div style="padding:12px 16px 0;font-size:13px;color:%s;font-weight:600">КРИПТОВАЛЮТЫ</div>' % t['muted'] + cr)
    return C.topbar(t, 'Валюты итогов') + C.sheet(t, 'Добавить валюту', body, 150, sub='Чипы с флагом; нажатие добавляет, ещё раз — убирает')


def cur_icons(t):
    def money(sym, code, amt, col=None, icon=True):
        left = cur_icon(t, sym, col, 28) if icon else ''
        return ('<div style="display:flex;align-items:center;gap:10px;padding:6px 0">%s<span style="%sfont-size:18px;flex:1">%s <span style="font-size:14px;color:%s">%s</span></span></div>' % (
            left, C.MONO, amt, t['muted'], code))
    on = ''.join([money('🇺🇸', 'USD', '1 250,00'), money('🇦🇷', 'ARS', '860 000,00'), money('₮', 'USDT', '300,00', '#26A17B'), money('₿', 'BTC', '0,0125', '#F7931A')])
    sw = ('<div style="display:flex;align-items:center;gap:12px;padding:10px 16px;min-height:56px"><div style="flex:1"><div style="font-size:16px">Значки валют</div>'
          '<div style="font-size:13px;color:%s">Флаг или знак рядом с кодом</div></div>'
          '<span style="width:52px;height:32px;border-radius:999px;background:%s;box-shadow:inset 0 0 0 1px %s;position:relative"><span style="position:absolute;top:4px;right:4px;width:24px;height:24px;border-radius:50%%;background:%s"></span></span></div>' % (
              t['muted'], t['prim'], C.edge(t), t['onp']))
    body = (C.card(t, '', '', sw, 'padding:6px 0') + C.card(t, 'Так выглядит «Счета»', 'с значками', on) +
            '<div style="margin:12px 20px 0;font-size:13px;color:%s">Код валюты остаётся: значок только помогает глазу. Выключите — всё как раньше.</div>' % t['muted'])
    return (C.topbar(t, 'Внешний вид') + '<div style="padding-top:72px">' + body + '</div>' + C.nav(t, 'more') +
            C.caption(t, 'Ещё → Настройки → Внешний вид. По умолчанию выключено: код валюты остаётся рядом всегда.', 0))


BOARDS = [('Bank567A', bank_a, 390, 820), ('Bank567B', bank_b, 390, 820), ('Bank567C', bank_c, 390, 820),
          ('Cur605A', cur_a, 390, 820), ('Cur605B', cur_b, 390, 820), ('Cur605Icons', cur_icons, 390, 760)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
