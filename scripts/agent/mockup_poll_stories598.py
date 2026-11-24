#!/usr/bin/env python3
"""Poll boards for #598 (story cards «what the app can do» as part of onboarding) and #579 (hint about sharing a receipt), session 2026-11-23.
Usage: python3 scripts/agent/mockup_poll_stories598.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402


def group(t, rows):
    return '<div style="margin:8px 16px 0;border-radius:24px;background:%s;border:1px solid %s;padding:4px 0;overflow:hidden">%s</div>' % (t['surf'], t['line'], ''.join(rows))


def page(t, title, inner, cap, active='more', back=True):
    return C.topbar(t, title, back=back) + '<div style="padding-top:72px">' + inner + '</div>' + C.nav(t, active) + C.caption(t, cap, 0)


def line(t, title, sub, right='›'):
    return ('<div style="display:flex;align-items:center;gap:12px;padding:8px 16px;min-height:60px"><div style="flex:1"><div style="font-size:16px">%s</div>'
            '<div style="font-size:13px;color:%s">%s</div></div><span style="color:%s;font-size:18px">%s</span></div>' % (title, t['muted'], sub, t['muted'], right))


def scheme(t, w=300, h=150):
    """Vector scheme «bank app → Share → co-shell → entry sheet» in theme colours (no pictures)."""
    s, e, i = t['wash'], C.edge(t), t['ink']
    return ('<svg width="%d" height="%d" viewBox="0 0 300 150" fill="none" stroke-linecap="round" stroke-linejoin="round">'
            '<rect x="10" y="20" width="70" height="110" rx="12" fill="%s" stroke="%s" stroke-width="2"/><rect x="22" y="40" width="46" height="8" rx="4" fill="%s" opacity=".4"/>'
            '<rect x="22" y="56" width="34" height="8" rx="4" fill="%s" opacity=".4"/><rect x="22" y="96" width="46" height="18" rx="9" fill="%s"/>'
            '<path d="M92 75h44" stroke="%s" stroke-width="3"/><path d="M128 65l10 10-10 10" stroke="%s" stroke-width="3"/>'
            '<rect x="150" y="20" width="70" height="110" rx="12" fill="%s" stroke="%s" stroke-width="2"/><circle cx="185" cy="58" r="16" fill="%s" stroke="%s" stroke-width="2"/>'
            '<rect x="162" y="86" width="46" height="8" rx="4" fill="%s" opacity=".4"/>'
            '<path d="M232 75h20" stroke="%s" stroke-width="3"/><path d="M246 65l10 10-10 10" stroke="%s" stroke-width="3"/>'
            '<rect x="262" y="40" width="30" height="70" rx="8" fill="%s" stroke="%s" stroke-width="2"/></svg>' % (
                w, h, s, i, i, i, t['prim'], e, e, s, i, t['prim'], e, i, e, e, t['prim'], e))


def story_card(t, n, total, title, text, body, cta=None):
    bars = ''.join('<span style="flex:1;height:4px;border-radius:2px;background:%s"></span>' % (t['prim'] if k < n else t['wash']) for k in range(total))
    btn = '<div style="margin-top:14px">%s</div>' % C.button(t, cta) if cta else ''
    return ('<div style="position:absolute;inset:0;background:linear-gradient(180deg,%s,%s);z-index:2;padding:20px 16px"><div style="display:flex;gap:4px">%s</div>'
            '<div style="display:flex;justify-content:space-between;align-items:center;margin-top:12px"><span style="font-size:13px;color:%s">Что умеет co-shell</span>%s</div>'
            '<div style="margin-top:36px;display:flex;justify-content:center">%s</div><div style="font-size:26px;font-weight:700;margin-top:28px;line-height:1.15">%s</div>'
            '<div style="font-size:16px;color:%s;margin-top:10px;line-height:1.4">%s</div>%s</div>' % (
                C.mix(t['page'], t['prim'], .2 if t['dark'] else .45), t['page'], bars, t['muted'], C.glyph('M6 6l12 12M18 6L6 18', t['ink'], 22), body, title, t['muted'], text, btn))


# ---------- #598 where stories live
def story_after_update(t):
    card = ('<div style="margin:60px 16px 0;padding:20px;border-radius:28px;background:%s;border:1px solid %s;text-align:center"><div style="font-size:13px;color:%s">Обновлено до 1.9</div>'
            '<div style="font-size:22px;font-weight:700;margin-top:6px">Поделиться чеком</div><div style="font-size:15px;color:%s;margin-top:6px">Новое: чек из банка или магазина — в co-shell в одно нажатие.</div>'
            '<div style="margin-top:14px">%s</div><div style="margin-top:8px">%s</div></div>' % (
                t['surf'], t['line'], t['muted'], t['muted'], C.button(t, 'Смотреть историю'), C.button(t, 'Понятно', 'background:%s;color:%s;box-shadow:none' % (t['wash'], t['ink']))))
    return C.topbar(t, 'Обзор', back=False) + card + C.nav(t, 'home') + C.caption(t, 'А: после обновления — окно «Обновлено» получает кнопку «Смотреть историю» про главную новинку; все истории — в «Ещё → Настройки → Помощь».', 0)


def story_library(t):
    rows = [line(t, 'Первые шаги', 'Счёт, трата, лимит, семья'), line(t, 'Поделиться чеком', '4 карточки · новое'), line(t, 'Быстрые заметки', '3 карточки'), line(t, 'Семья и общий доступ', '5 карточек'),
            line(t, 'Резервная копия', '3 карточки'), line(t, 'Курсы и валюты', '4 карточки')]
    body = group(t, [C.row(t, 'info', '#6BB8FF', 'Подсказки', 'Показываются', '›')]) + \
        '<div style="padding:12px 16px 2px;font-size:13px;font-weight:600;letter-spacing:.04em;color:%s">ЧТО УМЕЕТ ПРИЛОЖЕНИЕ</div>' % t['muted'] + group(t, rows)
    return page(t, 'Помощь', body, 'Б: только библиотека — «Ещё → Настройки → Помощь → Что умеет приложение»; сама ничего не показывает и не напоминает.')


def story_steps(t):
    steps = C.card(t, 'Первые шаги', 'Сделано 2 из 4', ''.join(
        '<div style="display:flex;align-items:center;gap:12px;padding:8px 0"><span style="width:22px;height:22px;border-radius:7px;background:%s;box-shadow:inset 0 0 0 1px %s;flex:none"></span><div style="flex:1;font-size:15px">%s</div>%s</div>' % (
            t['prim'] if k < 2 else t['wash'], C.edge(t), s, '<span style="font-size:13px;color:%s">Как это работает ›</span>' % C.ink2(t) if k >= 2 else '')
        for k, s in enumerate(['Добавьте счёт', 'Запишите первую трату', 'Задайте первый лимит', 'Позовите близкого человека'])), 'margin-top:72px')
    return C.topbar(t, 'Обзор', back=False) + steps + C.nav(t, 'home') + C.caption(t, 'В: истории прикреплены к «Первым шагам» — у каждого шага «Как это работает ›»; про новинки после обновления — не напоминают.', 0)


def card_t1(t):
    return story_card(t, 2, 4, 'Поделитесь чеком', 'В приложении банка или магазина нажмите «Поделиться» и выберите co-shell. Сумма, дата и магазин подставятся сами.', scheme(t), 'Дальше')


def card_t2(t):
    steps = ''.join('<div style="display:flex;align-items:center;gap:12px;margin-top:12px"><span style="width:36px;height:36px;border-radius:50%%;background:%s;color:%s;display:flex;align-items:center;justify-content:center;font-weight:700;flex:none;box-shadow:inset 0 0 0 1px %s">%d</span>'
                    '<span style="font-size:16px">%s</span></div>' % (t['prim'], t['onp'], C.edge(t), k + 1, s) for k, s in enumerate(['Откройте чек в банке', 'Нажмите «Поделиться»', 'Выберите co-shell', 'Проверьте и «Сохранить»']))
    return story_card(t, 2, 4, 'Поделитесь чеком', 'Четыре нажатия — и трата записана.', '<div style="width:100%%">%s</div>' % steps, 'Дальше')


# ---------- #579 hint about «Поделиться»
def entry_base(t, extra=''):
    tiles = ''.join('<div style="display:flex;justify-content:space-between;align-items:center;height:48px;padding:0 16px;border-radius:999px;background:%s;margin:8px 0 0;font-size:15px">'
                    '<span style="color:%s">%s</span><span>%s</span></div>' % (t['wash'], t['muted'], a, b) for a, b in [('Сумма', '0 ARS'), ('Категория', 'Не выбрана')])
    return tiles + extra


def hint_a(t):
    hint = ('<div style="margin:12px 16px 0;padding:10px 14px;border-radius:20px;background:%s;border:1px solid %s;display:flex;gap:10px;align-items:center;font-size:14px">'
            '<span style="flex:1">Чек пришёл в банк или магазин? Нажмите там «Поделиться» и выберите co-shell.</span>%s</div>' % (
                t['surf'], t['line'], C.glyph('M6 6l12 12M18 6L6 18', t['muted'], 18)))
    ops = group(t, [C.row(t, 'cafe', '#FFB86B', 'Кафе «Моне»', 'Сегодня · Visa ARS', '−3 500 ARS'), C.row(t, 'cart', '#7FD6A0', 'Coto', 'Вчера · Mercado Pago', '−12 500 ARS')])
    return C.topbar(t, 'Операции', back=False) + '<div style="padding-top:72px">' + hint + ops + '</div>' + C.nav(t, 'list') + \
        C.caption(t, 'А: подсказка-слово один раз — в «Операциях» после третьей записанной траты (или первого скана QR); «✕» убирает навсегда.', 0)


def hint_b(t):
    line_ = ('<div style="margin:10px 16px 0;font-size:13px;color:%s;display:flex;gap:8px;align-items:center">%s<span>Можно и так: «Поделиться» чеком из банка → co-shell</span></div>' % (
        t['muted'], C.glyph('M4 12v7h16v-7M12 3v12M7 8l5-5 5 5', t['muted'], 18)))
    return C.topbar(t, 'Операции', back=False) + C.sheet(t, 'Новая операция', entry_base(t, line_), 120, sub='Расход · Visa ARS') + \
        C.caption(t, 'Б: тихая строка в шторке новой операции всегда; не закрывается.', 0)


BOARDS = [('StoryA', story_after_update, 390, 820), ('StoryB', story_library, 390, 820), ('StoryC', story_steps, 390, 820),
          ('StoryT1', card_t1, 390, 820), ('StoryT2', card_t2, 390, 820), ('ShareHintA', hint_a, 390, 820), ('ShareHintB', hint_b, 390, 820)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
