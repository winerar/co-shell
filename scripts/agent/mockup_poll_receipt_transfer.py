#!/usr/bin/env python3
"""Poll boards for #646/#578 (receipt of a transfer between own accounts), #577 (account ↔ card digits), #645 (travel pass).

Standalone PNGs like mockup_poll_receipts561.py: `XxName.png` dark + `XxName@light.png`.
Usage: python3 scripts/agent/mockup_poll_receipt_transfer.py <out-dir>  → <out-dir>/img/*.png   then   poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_accounts as P  # noqa: E402
import mockup_poll_accounts4 as A  # noqa: E402
import mockup_poll_update as U  # noqa: E402

MONO = U.MONO
CARD = ('Ameria · карта', None, '#E4002B', 'AM')
SAVE = ('Ameria · накопит.', None, '#E4002B', 'AM')
PASS = ('SUBE', None, '#00A0DF', 'SU')
GAL = ('Galicia ARS', None, '#F26E22', 'GA')
DOC = 'M7 3h7l5 5v13H7zM14 3v5h5M10 13h6M10 17h6'


def acc_row(t, acc, label, value):
    return ('<div style="display:flex;align-items:center;gap:10px;margin-top:4px;padding:6px 10px;border-radius:16px;background:%s">%s<span style="flex:1"><div style="font-size:11px;color:%s">%s</div>'
            '<div style="font-size:15px">%s</div></span><span style="%sfont-size:13px;color:%s">%s</span></div>' % (t['bar'], P.badge(acc, 40, 'A'), t['muted'], label, acc[0], MONO, t['muted'], value))


def strip(t, sel=0):
    items = ''.join('<span style="display:flex;border-radius:14px;%s">%s</span>' % (
        ('box-shadow:0 0 0 2px %s,0 0 0 4px %s;' % (t['page'], A.edge(t))) if i == sel else '', P.badge(a, 44, 'A')) for i, a in enumerate([CARD, SAVE, GAL, PASS]))
    return '<div style="display:flex;gap:10px;margin:0 12px 8px;align-items:center;padding:2px 4px">%s%s</div>' % (items, A.pill(t, 'Все ›', 44))


def source(t, text):
    return '<div style="margin:-2px 16px 8px;display:flex;align-items:center;gap:6px;font-size:13px;color:%s">%s<span>%s</span></div>' % (t['muted'], N.glyph(DOC, t['muted'], 16, 1.6), text)


def amount_card(t, value, hint):
    field = ('<div style="margin-top:6px;height:52px;border-radius:16px;border:2px solid %s;background:%s;display:flex;align-items:center;justify-content:space-between;padding:0 14px">'
             '<span style="%sfont-size:26px">%s</span><span style="%sfont-size:13px;color:%s">%s</span></div>' % (A.edge(t), t['bar'], MONO, value, MONO, t['muted'], hint))
    return A.card(t, 'СУММА', field)


def overlay(page, extra):
    i = page.rfind('</div>')
    return page[:i] + '<div style="position:absolute;inset:0;background:rgba(0,0,0,.45)"></div>' + extra + page[i:]


def btn(t, text, main=False, grow=1):
    st = 'background:%s;color:%s;font-weight:700;box-shadow:inset 0 0 0 2px %s' % (t['prim'], t['onp'], A.edge(t)) if main else 'background:%s' % t['bar']
    return '<span style="flex:%s;height:48px;border-radius:999px;display:flex;align-items:center;justify-content:center;font-size:14px;%s">%s</span>' % (grow, st, text)


def transfer_found(t, v):
    body = (A.seg(t, 2) + source(t, 'Из чека · Америабанк') + A.card(t, 'ПЕРЕВОД', acc_row(t, CARD, 'Откуда · …3900', '120 000 AMD') + acc_row(t, SAVE, 'Куда · …4906', '800 000 AMD'))
            + amount_card(t, '3 000', 'AMD → 3 000 AMD') + A.details(t) + A.keypad(t))
    return A.sheet(t, body)


def transfer_hint(t, v):
    banner = ('<div style="margin:0 12px 8px;padding:10px 14px;border-radius:20px;background:%s;border:1px solid %s;display:flex;align-items:center;gap:10px">'
              '<span style="flex:1;font-size:13px">Похоже на перевод между вашими счетами</span>'
              '<span style="height:36px;padding:0 14px;border-radius:999px;display:flex;align-items:center;font-size:13px;font-weight:700;background:%s;color:%s;box-shadow:inset 0 0 0 2px %s">Как перевод</span></div>' % (
                  t['surf'], t['line'], t['prim'], t['onp'], A.edge(t)))
    body = A.seg(t, 0) + banner + source(t, 'Из чека · Америабанк') + amount_card(t, '3 000', 'AMD') + A.cats(t) + A.details(t) + A.keypad(t, 44)
    return A.sheet(t, body)


def digits_field(t, v):
    tile = lambda a, b, on=False: ('<div style="flex:1;padding:8px 12px;border-radius:16px;background:%s%s"><div style="font-size:11px;color:%s">%s</div><div style="font-size:15px%s">%s</div></div>' % (
        t['bar'], ';box-shadow:inset 0 0 0 2px %s' % A.edge(t) if on else '', t['muted'], a, ';%s' % MONO if on else '', b))  # noqa: E731
    main = A.card(t, 'СЧЁТ', '<div style="display:flex;flex-direction:column;gap:6px;margin-top:6px"><div style="display:flex;gap:6px">%s%s</div></div>' % (tile('Название', 'Ameria · карта'), tile('Валюта', 'AMD')))
    cheques = A.card(t, 'ДЛЯ ЧЕКОВ', '<div style="display:flex;margin-top:6px">%s</div><div style="font-size:12px;color:%s;margin-top:6px">Чек банка сам найдёт этот счёт.</div>' % (
        tile('Последние цифры карты или счёта', '3900', True), t['muted']))
    return A.sheet(t, '<div style="padding:4px 16px 10px;font-size:20px;font-weight:700">Счёт</div>' + main + cheques, 520)


def digits_ask(t, v):
    row = lambda acc, on: ('<div style="display:flex;align-items:center;gap:10px;padding:8px 4px">%s<span style="flex:1;font-size:15px">%s</span>'
                           '<span style="width:22px;height:22px;border-radius:50%%;box-sizing:border-box;border:2px solid %s;%s"></span></div>' % (
                               P.badge(acc, 36, 'A'), acc[0], A.edge(t) if on else t['muted'], 'background:%s;box-shadow:inset 0 0 0 4px %s' % (t['prim'], t['page']) if on else ''))  # noqa: E731
    card = ('<div style="position:absolute;left:20px;right:20px;top:200px;padding:20px;border-radius:28px;background:%s;border:1px solid %s">'
            '<div style="font-size:18px;font-weight:700">Какой это счёт?</div><div style="font-size:14px;color:%s;margin:6px 0 8px">В чеке карта …3900. В следующий раз выберем сами.</div>%s%s%s'
            '<div style="display:flex;gap:8px;margin-top:14px">%s%s</div></div>' % (
                t['page'], t['line'], t['muted'], row(CARD, True), row(SAVE, False), row(GAL, False), btn(t, 'Пропустить'), btn(t, 'Запомнить', True, 1.4)))
    return overlay(A.sheet(t, A.seg(t) + strip(t) + amount_card(t, '2 734', 'AMD') + A.cats(t) + A.details(t)), card)


def list_card(t, acc, name, sub, right, tag=''):
    chip = '<span style="margin-left:6px;height:20px;padding:0 8px;border-radius:999px;display:inline-flex;align-items:center;font-size:11px;background:%s">%s</span>' % (t['bar'], tag) if tag else ''
    return ('<div style="margin:0 16px 8px;padding:12px 14px;border-radius:24px;background:%s;border:1px solid %s;display:flex;align-items:center;gap:12px">%s'
            '<span style="flex:1;min-width:0"><div style="font-size:15px">%s%s</div><div style="font-size:12px;color:%s">%s</div></span><span style="%sfont-size:15px;text-align:right">%s</span></div>' % (
                t['surf'], t['line'], P.badge(acc, 40, 'A'), name, chip, t['muted'], sub, MONO, right))


def op_row(t, title, sub, amount):
    return ('<div style="margin:0 16px;padding:10px 4px;display:flex;border-bottom:1px solid %s"><span style="flex:1"><div style="font-size:15px">%s</div><div style="font-size:12px;color:%s">%s</div></span>'
            '<span style="%sfont-size:15px">%s</span></div>' % (t['bar'], title, t['muted'], sub, MONO, amount))


def title(t, text):
    return '<div style="margin:8px 32px 12px;height:56px;border-radius:999px;background:%s;display:flex;align-items:center;padding:0 20px"><b style="font-size:22px">%s</b></div>' % (t['bar'], text)


def pass_account(t, v):
    body = (title(t, 'Счета') + list_card(t, CARD, 'Ameria · карта', 'AMD', '120 000') + list_card(t, PASS, 'SUBE', 'Не в общем балансе', '2 400 ARS', 'Проездной')
            + '<div style="margin:12px 20px 4px;font-size:12px;letter-spacing:.04em;color:%s">ОПЕРАЦИИ</div>' % t['muted']
            + op_row(t, 'Пополнение SUBE', 'Перевод на проездной · Транспорт', '3 000 ARS') + op_row(t, 'Поездка', 'Расход с проездного · Транспорт', '−420 ARS'))
    return U.phone(t, body, 844)


def pass_balance(t, v):
    note = '<div style="font-size:12px;color:%s;margin-top:6px">Баланс на карте · введён 20 нояб.</div>' % t['muted']
    sube = ('<div style="margin:0 16px 8px;padding:12px 14px;border-radius:24px;background:%s;border:1px solid %s"><div style="display:flex;align-items:center;gap:12px">%s'
            '<span style="flex:1;font-size:15px">SUBE</span><span style="%sfont-size:15px">2 400 ARS</span></div>%s'
            '<div style="margin-top:10px">%s</div></div>' % (t['surf'], t['line'], P.badge(PASS, 40, 'A'), MONO, note, btn(t, 'Сверить баланс')))
    body = (title(t, 'Счета') + list_card(t, CARD, 'Ameria · карта', 'AMD', '120 000') + sube
            + '<div style="margin:12px 20px 4px;font-size:12px;letter-spacing:.04em;color:%s">ОПЕРАЦИИ</div>' % t['muted']
            + op_row(t, 'Пополнение SUBE', 'Расход с карты · Транспорт', '−3 000 ARS'))
    return U.phone(t, body, 844)


BOARDS = [('TfFound', transfer_found), ('TfHint', transfer_hint), ('CdField', digits_field), ('CdAsk', digits_ask), ('PsAccount', pass_account), ('PsBalance', pass_balance)]


def main():
    out = sys.argv[1]
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    for name, fn in BOARDS:
        for suffix, dark in (('', True), ('@light', False)):
            t = U.theme(dark)
            open(os.path.join(out, 'html', name + suffix + '.html'), 'w', encoding='utf-8').write(N.page(fn(t, name), 390, 844, dict(page=t['page'])))
    js = os.path.join(out, 'render.js')
    src = open(os.path.join(HERE, 'mockup_poll_update.py'), encoding='utf-8').read()
    open(js, 'w').write(src[src.index("'''const") + 3:src.index("''')")].replace('\\\\d', '\\d'))
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('boards → %s/img' % out)


if __name__ == '__main__':
    main()
