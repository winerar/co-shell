#!/usr/bin/env python3
"""Poll boards, part 1 (#583): captions of automatic categories (#584/#594), voice entry (#585), reading a receipt with AI (#593). Bricks — `mockup_poll_ai_parts`."""
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_poll_ai_parts as X  # noqa: E402
import mockup_poll_accounts4 as A  # noqa: E402
import mockup_poll_qr526 as Q  # noqa: E402
import mockup_poll_update as U  # noqa: E402

ic, MONO = X.ic, X.MONO


def head(t, mic=False, qr=True):
    items = ''.join('<span style="flex:1;text-align:center;height:36px;line-height:36px;border-radius:999px;font-size:14px;%s">%s</span>' % (
        ('background:%s;color:%s;font-weight:700;box-shadow:inset 0 0 0 2px %s' % (t['prim'], t['onp'], A.edge(t))) if i == 0 else '', n) for i, n in enumerate(['Расход', 'Доход', 'Перевод']))
    btns = X.round_btn(t, 'list') + (X.round_btn(t, 'qr') if qr else '') + (X.round_btn(t, 'mic') if mic else '')
    return '<div style="display:flex;gap:6px;align-items:center;margin:0 12px 8px"><div style="flex:1;padding:4px;border-radius:999px;background:%s;display:flex;min-width:0">%s</div>%s</div>' % (t['bar'], items, btns)


def sheet(t, head_html, amount, cats, source='', chips=True):
    return A.sheet(t, head_html + A.strip(t, 1) + source + amount + cats + Q.date_chips(t, chips) + A.keypad(t, 44))


def amount(t, v='3 500'):
    return Q.filled_amount(t, v, 'ARS')


def src(t, text, icon='mic'):
    return '<div style="margin:0 16px 8px">%s</div>' % X.caption(t, text, icon)


# --- Q1: captions of an automatic category (#584 history, #594 AI) ---
def cap_a(t, v):
    return sheet(t, head(t), amount(t), X.cat_card(t, 0, X.caption(t, 'по прошлым операциям')), chips=False)


def cap_ai_a(t, v):
    return sheet(t, head(t), amount(t), X.cat_card(t, 0, X.caption(t, 'предложено ИИ', 'spark')), chips=False)


def _chip_card(t, text, icon):
    c = X.cat_card(t, 0)
    chip = '<div style="margin-top:6px;display:flex;align-items:center;justify-content:space-between">%s<span style="font-size:13px;color:%s">Изменить</span></div>' % (X.tag(t, text, icon), X.ink_acc(t))
    return c[:c.rfind('</div>')] + chip + '</div>'


def cap_b(t, v):
    return sheet(t, head(t), amount(t), _chip_card(t, 'по прошлым операциям', 'hist'), chips=False)


def cap_ai_b(t, v):
    return sheet(t, head(t), amount(t), _chip_card(t, 'предложено ИИ', 'spark'), chips=False)


# --- Q3: voice entry (#585) ---
def voice_a(t, v):
    return A.sheet(t, head(t, mic=True) + A.strip(t) + Q.empty_amount(t) + X.cat_card(t, -1) + A.details(t) + A.keypad(t))


def voice_b(t, v):
    field = Q.empty_amount(t, True).replace(Q.qr(20), ic('mic', t['ink'], 20, 1.9))
    return A.sheet(t, head(t) + A.strip(t) + field + X.cat_card(t, -1) + A.details(t) + A.keypad(t))


def listen(t, v):
    base = voice_a(t, v)
    bars = ''.join('<span style="width:6px;border-radius:3px;background:%s;height:%dpx"></span>' % (t['prim'], h) for h in [10, 22, 36, 18, 44, 30, 14, 40, 26, 12, 34, 20, 8])
    panel = ('<div style="position:absolute;left:0;right:0;bottom:0;padding:18px 16px 24px;border-radius:28px 28px 0 0;background:%s;border-top:1px solid %s">'
             '<div style="display:flex;justify-content:space-between;align-items:center"><b style="font-size:20px">Говорите</b>%s</div>'
             '<div style="margin:14px 0;padding:14px;border-radius:20px;background:%s;font-size:20px;min-height:56px">кофе три пятьсот с карты<span style="color:%s">…</span></div>'
             '<div style="display:flex;align-items:center;justify-content:center;gap:5px;height:56px">%s</div>'
             '<div style="font-size:13px;color:%s;text-align:center;margin:6px 0 14px">Сумма, счёт и категория подставятся сами. Проверьте и нажмите «Готово».</div>'
             '<div style="display:flex;gap:8px"><div style="flex:1">%s</div><div style="flex:1.5">%s</div></div></div>'
             % (t['page'], t['line'], X.round_btn(t, 'close', 40), t['surf'], t['muted'], bars, t['muted'], X.btn_row(t, 'Отмена'), X.btn_row(t, 'Готово', True, 'check')))
    return X.overlay(base, panel)


def voice_done(t, v):
    cats = X.cat_card(t, 0, X.caption(t, 'по прошлым операциям'))
    return A.sheet(t, head(t, mic=True) + A.strip(t, 1) + src(t, 'Из голоса · «кофе три пятьсот с карты»') + amount(t) + cats + Q.date_chips(t, False) + A.keypad(t, 40))


def voice_no(t, v):
    btns = X.btn_row(t, 'Разрешить микрофон', True, 'mic') + X.btn_row(t, 'Ввести вручную')
    d = X.dialog(t, 'mic', 'Микрофон выключен', 'Голос распознаётся на телефоне и нигде не сохраняется. Разрешите микрофон, чтобы диктовать операции.', btns)
    return X.overlay(voice_a(t, v), d)


# --- Q6: a receipt no format could read (#593, window of #561) ---
def _unread(t, text, buttons):
    base = A.sheet(t, head(t) + A.strip(t) + Q.empty_amount(t) + X.cat_card(t, -1) + A.details(t) + A.keypad(t))
    return X.overlay(base, X.dialog(t, 'note', 'Не удалось прочитать чек', text, buttons, 230))


def rec_on(t, v):
    return _unread(t, 'Такого формата мы ещё не знаем. ИИ на телефоне прочитает чек сам, без интернета, а вы проверите поля.',
                   X.btn_row(t, 'Прочитать с ИИ', True, 'spark') + X.btn_row(t, 'Отправить образец') + X.btn_row(t, 'Ввести вручную'))


def rec_off(t, v):
    return _unread(t, 'Такого формата мы ещё не знаем. Отправьте образец — добавим формат. Или включите ИИ на телефоне: «Ещё → Настройки → ИИ на телефоне».',
                   X.btn_row(t, 'Отправить образец', True) + X.btn_row(t, 'Включить ИИ', False, 'spark') + X.btn_row(t, 'Ввести вручную'))


def rec_busy(t, v):
    bar = ('<div style="height:8px;border-radius:4px;background:%s;margin-top:14px"><div style="width:55%%;height:8px;border-radius:4px;background:%s;box-shadow:0 0 0 1px %s"></div></div>' % (t['bar'], t['prim'], A.edge(t)))
    base = A.sheet(t, head(t) + A.strip(t) + Q.empty_amount(t) + X.cat_card(t, -1) + A.details(t) + A.keypad(t))
    d = X.dialog(t, 'spark', 'Читаю чек…', 'Это займёт несколько секунд. Чек не уходит с телефона.' + bar, X.btn_row(t, 'Отменить'), 300)
    return X.overlay(base, d)


def rec_filled_a(t, v):
    line = '<div style="margin:0 16px 8px;display:flex;flex-wrap:wrap;gap:6px;align-items:center">%s<span style="font-size:13px;color:%s">Проверьте сумму и дату</span></div>' % (X.tag(t, 'Прочитано ИИ', 'spark'), t['muted'])
    return A.sheet(t, head(t) + A.strip(t, 1) + line + Q.filled_amount(t, '37 914', 'ARS') + X.cat_card(t, 1, X.caption(t, 'по прошлым операциям')) + Q.date_chips(t, True) + A.keypad(t, 40))


def rec_filled_b(t, v):
    amt = Q.filled_amount(t, '37 914', 'ARS').replace('СУММА', 'СУММА &nbsp;' + X.tag(t, 'ИИ', 'spark'), 1)
    dates = Q.date_chips(t, True).replace('◷ 19 нояб.', '✦ 19 нояб.')
    return A.sheet(t, head(t) + A.strip(t, 1) + amt + X.cat_card(t, 1, X.tag(t, 'ИИ', 'spark')) + dates + A.keypad(t, 40))


BOARDS = [('AiCapA', cap_a), ('AiCapAiA', cap_ai_a), ('AiCapB', cap_b), ('AiCapAiB', cap_ai_b),
          ('AiVoiceBtnA', voice_a), ('AiVoiceBtnB', voice_b), ('AiVoiceListen', listen), ('AiVoiceDone', voice_done), ('AiVoiceNo', voice_no),
          ('AiRecOn', rec_on), ('AiRecOff', rec_off), ('AiRecBusy', rec_busy), ('AiRecFilledA', rec_filled_a), ('AiRecFilledB', rec_filled_b)]
