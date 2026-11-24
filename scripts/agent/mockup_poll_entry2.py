#!/usr/bin/env python3
"""Poll boards for #325 (shake right after saving = undo) and #320 (share any bank text → entry), session 2026-11-23.
Usage: python3 scripts/agent/mockup_poll_entry2.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402


def group(t, rows):
    return '<div style="margin:8px 16px 0;border-radius:24px;background:%s;border:1px solid %s;padding:4px 0;overflow:hidden">%s</div>' % (t['surf'], t['line'], ''.join(rows))


def snackbar(t, text, action, sub='', bottom=84):
    s = '<div style="font-size:12px;color:%s">%s</div>' % (t['muted'], sub) if sub else ''
    return ('<div style="position:absolute;left:16px;right:16px;bottom:%dpx;z-index:7;min-height:52px;border-radius:999px;background:%s;border:1px solid %s;display:flex;align-items:center;'
            'gap:8px;padding:6px 8px 6px 20px"><div style="flex:1;font-size:15px">%s%s</div><span style="height:40px;padding:0 14px;border-radius:999px;display:inline-flex;align-items:center;'
            'font-size:14px;font-weight:700;color:%s">%s</span></div>' % (bottom, t['surf'], t['line'], text, s, C.ink2(t), action))


def ops(t):
    return group(t, [C.row(t, 'cafe', '#FFB86B', 'Кафе «Моне»', 'Сегодня · Visa ARS', '−3 500 ARS'), C.row(t, 'cart', '#7FD6A0', 'Coto', 'Вчера · Mercado Pago', '−12 500 ARS'),
                     C.row(t, 'bus', '#6BB8FF', 'Такси', 'Вчера · Visa ARS', '−2 100 ARS'), C.row(t, 'cafe', '#FFB86B', 'Кофе', '20 ноября · Наличные', '−900 ARS')])


def page(t, title, inner, cap, active='list'):
    return C.topbar(t, title, back=False) + '<div style="padding-top:72px">' + inner + '</div>' + C.nav(t, active) + C.caption(t, cap, 0)


# ---------- #325
def shake_a(t):
    sn = snackbar(t, 'Сохранено · 3 500 ARS', 'Отменить', 'Или встряхните телефон', bottom=160)
    return page(t, 'Операции', ops(t), 'А: подсказка в самом снэкбаре на 4 секунды: «Или встряхните телефон». Встряхнули — «Отменено · Вернуть».') + sn


def shake_b(t):
    sn = snackbar(t, 'Сохранено · 3 500 ARS', 'Отменить', '', bottom=240)
    coach = ('<div style="position:absolute;left:16px;right:16px;bottom:160px;z-index:7;padding:12px 16px;border-radius:24px;background:%s;border:1px solid %s;display:flex;align-items:center;gap:12px">'
             '<div style="flex:1;font-size:14px">Ошиблись? Сразу после сохранения встряхните телефон — операция отменится.</div>%s</div>' % (
                 t['surf'], t['line'], C.pill(t, 'Понятно', True, 40)))
    return page(t, 'Операции', ops(t), 'Б: один раз после первого сохранения — карточка «Понятно» (как другие подсказки-жесты); в снэкбаре без лишнего текста.') + sn + coach


def shake_undone(t):
    sn = snackbar(t, 'Отменено · 3 500 ARS', 'Вернуть', '', bottom=160)
    return page(t, 'Операции', ops(t), 'Общее для А и Б: после встряхивания снэкбар «Отменено · Вернуть» (4 секунды). Через 4 секунды встряхивание снова открывает новый ввод.') + sn


# ---------- #320
def tile(t, label, value, hi=False):
    st = 'box-shadow:inset 0 0 0 2px %s;' % C.edge(t) if hi else ''
    return ('<div style="display:flex;justify-content:space-between;align-items:center;height:48px;padding:0 16px;border-radius:999px;background:%s;margin:8px 0 0;font-size:15px;%s">'
            '<span style="color:%s">%s</span><span>%s</span></div>' % (t['wash'], st, t['muted'], label, value))


def sms_card(t):
    return ('<div style="margin:12px 16px 0;padding:12px 16px;border-radius:20px;background:%s;border:1px solid %s;font-size:14px"><div style="color:%s;font-size:12px">SMS · Galicia</div>'
            '<div style="margin-top:4px">Compra $12.500,00 en COTO SUC 12 con tarjeta Visa terminada en 4821</div></div>' % (t['surf'], t['line'], t['muted']))


def share_a(t):
    body = ('<div style="font-size:13px;color:%s;margin-top:2px">Из текста · «Compra … COTO»</div>' % t['muted'] + tile(t, 'Сумма', '12 500,00 ARS', True) +
            tile(t, 'Счёт', 'Visa ···4821') + tile(t, 'Категория', 'Продукты · по названию') + tile(t, 'Заметка', 'COTO SUC 12') +
            '<div style="margin-top:12px">%s</div>' % C.button(t, 'Сохранить'))
    return sms_card(t) + C.sheet(t, 'Новая операция', body, 230, sub='«Поделиться» → co-shell: сумма, счёт по цифрам карты и заметка подставлены')


def share_b(t):
    card = ('<div style="margin:16px 16px 0;padding:16px;border-radius:24px;background:%s;border:1px solid %s"><b style="font-size:17px">Записать трату?</b>'
            '<div style="font-size:14px;color:%s;margin-top:4px">12 500,00 ARS · COTO SUC 12 · Visa ···4821</div>'
            '<div style="display:flex;gap:8px;margin-top:12px"><span style="flex:1">%s</span><span style="flex:1">%s</span></div></div>' % (
                t['surf'], t['line'], t['muted'], C.button(t, 'Записать'),
                C.button(t, 'Открыть', 'background:%s;color:%s;box-shadow:none' % (t['wash'], t['ink']))))
    return sms_card(t) + card + '<div style="margin:10px 20px 0;font-size:13px;color:%s">Записать — сразу в операции со снэкбаром «Отменить»; Открыть — шторка ввода, как в А.</div>' % t['muted'] + \
        C.caption(t, 'Б: сначала короткая карточка с итогом: записать в одно нажатие или открыть шторку.', 0)


def share_none(t):
    body = ('<div style="font-size:15px;margin-top:8px">В тексте не нашлась сумма.</div><div style="font-size:13px;color:%s;margin-top:4px">Ничего не записано. Можно ввести вручную.</div>' % t['muted'] +
            '<div style="margin-top:16px">%s</div>' % C.button(t, 'Ввести вручную'))
    return ('<div style="margin:12px 16px 0;padding:12px 16px;border-radius:20px;background:%s;border:1px solid %s;font-size:14px">Привет! Как дела? Увидимся в пятницу.</div>' % (t['surf'], t['line'])) + \
        C.sheet(t, 'Не удалось прочитать текст', body, 330) + C.caption(t, 'Общее для А и Б: нет суммы — окно «Не удалось прочитать» с «Ввести вручную» (как у чека).', 0)


BOARDS = [('Shk325A', shake_a, 390, 820), ('Shk325B', shake_b, 390, 820), ('Shk325Undone', shake_undone, 390, 820),
          ('Shr320A', share_a, 390, 820), ('Shr320B', share_b, 390, 820), ('Shr320None', share_none, 390, 820)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
