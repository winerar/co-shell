#!/usr/bin/env python3
"""Poll boards (#635): the «Облачный разбор» switch (Claude Haiku through our Worker), its consent and what notes show while it works.
The AI screen look is decided (#587); here only the cloud part is new. Bricks — `mockup_poll_ai_parts`, `mockup_poll_ai_more`.

Usage: python3 scripts/agent/mockup_poll_cloud_ai.py <out-dir>  → <out-dir>/img/*.png   then   agent.sh poll docs/design/poll-cloud-ai.json --img <out-dir>/img
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_accounts4 as A  # noqa: E402
import mockup_poll_ai_more as M  # noqa: E402
import mockup_poll_ai_parts as X  # noqa: E402
import mockup_poll_update as U  # noqa: E402

ic = X.ic
TITLE = 'Облачный разбор'
BETA = ' <span style="display:inline-flex;vertical-align:middle;margin-left:4px">%s</span>'
TEXT = 'Необязательно. Если телефон слабый или ИИ на нём не включён, сложные строки заметок, нечитаемые чеки и голос прочитает облачный ИИ.'
SENT = [('note', 'Уходит', 'текст заметки или чека и названия ваших категорий'),
        ('wallet', 'Не уходит', 'суммы на счетах, история, имена и данные семьи'),
        ('cloud', 'Нужен интернет', 'без сети всё работает как раньше')]
USE = [('draft', 'Быстрые заметки и голос', 'разбор сложных фраз'), ('note', 'Незнакомые чеки', '«Прочитать с ИИ»'), ('spark', 'Категория для нового получателя', 'подпись «предложено ИИ»')]


def switch(t, on):
    """CoShellSwitch: 52×32 pill, the knob moves; on = neon with a thin edge."""
    track = ('background:%s;box-shadow:0 0 0 1px %s' % (t['prim'], A.edge(t))) if on else 'background:%s;box-shadow:inset 0 0 0 2px %s' % (t['bar'], t['line'])
    knob = ('background:%s;left:24px' % t['onp']) if on else 'background:%s;left:4px' % t['muted']
    return '<span style="position:relative;width:52px;height:32px;border-radius:999px;flex:none;%s"><span style="position:absolute;top:6px;width:20px;height:20px;border-radius:50%%;%s"></span></span>' % (track, knob)


def screen(t, title, body, right=''):
    return X.screen(t, title + BETA % X.tag(t, 'бета', 'spark'), body, right=right)


def sec(t, s):
    return '<div style="margin:14px 20px 4px;font-size:13px;color:%s">%s</div>' % (t['muted'], s)


def group(t, rows):
    return X.card(t, ''.join(rows), '4px 16px')


# --- Q1 A: a row with an arrow → its own screen ---
def settings_a(t, v, state='Выключен · бета'):
    return X.screen(t, 'Настройки', sec(t, 'Внешний вид') + group(t, [X.row(t, 'gear', 'Тема', 'Системная'), X.row(t, 'wallet', 'Валюты итогов', 'USD · ARS')])
                    + sec(t, 'Умные функции') + group(t, [X.row(t, 'spark', 'ИИ на телефоне', 'Выключен · бета', ic('chev', t['muted'], 18, 2)),
                                                          X.row(t, 'cloud', 'Облачный разбор', state, ic('chev', t['muted'], 18, 2))])
                    + sec(t, 'Помощь') + group(t, [X.row(t, 'note', 'Подсказки', 'Показываются')]))


def screen_a(t, v, on=False, left=''):
    status = 'Включён' if on else 'Выключен'
    sub = ('Работает, когда есть интернет. Осталось сегодня: 87 из 100.' if on else TEXT)
    chip = X.tag(t, 'готов' if on else 'без него всё работает как раньше', 'check')
    body = (M.hero(t, status, sub, '<div style="margin-top:8px">%s</div>' % chip)
            + X.card(t, '<div style="display:flex;align-items:center;gap:12px;min-height:48px"><span style="flex:1;font-size:16px">Облачный разбор</span>%s</div>' % switch(t, on), '6px 16px')
            + M.facts(t, SENT))
    return screen(t, TITLE, body)


def screen_a_on(t, v):
    use = X.card(t, '<div style="font-size:12px;letter-spacing:.04em;color:%s;margin-bottom:2px">ГДЕ РАБОТАЕТ</div>' % t['muted'] + ''.join(X.row(t, i, a, b) for i, a, b in USE), '8px 16px')
    body = (M.hero(t, 'Включён', 'Работает, когда есть интернет. Осталось сегодня: 87 из 100.', '<div style="margin-top:8px">%s</div>' % X.tag(t, 'готов', 'check'))
            + X.card(t, '<div style="display:flex;align-items:center;gap:12px;min-height:48px"><span style="flex:1;font-size:16px">Облачный разбор</span>%s</div>' % switch(t, True), '6px 16px') + use)
    return screen(t, TITLE, body)


# --- Q1 B: the switch right in the «Умные функции» group ---
def settings_b(t, v, on=False):
    sub = 'Тексты заметок уходят в облако' if on else 'Выключен · сложное читает телефон'
    cloud = ('<div style="display:flex;align-items:center;gap:12px;min-height:56px;padding:6px 0"><span style="width:32px;height:32px;border-radius:10px;flex:none;background:%s;display:flex;align-items:center;justify-content:center">%s</span>'
             '<span style="flex:1;min-width:0"><div style="font-size:16px">Облачный разбор%s</div><div style="font-size:13px;color:%s">%s</div></span>%s</div>'
             % (U.mix(t['page'], t['acc'], .18), ic('cloud', X.ink_acc(t), 18, 1.9), BETA % X.tag(t, 'бета', 'spark'), t['muted'], sub, switch(t, on)))
    note = '<div style="margin:2px 28px 0;font-size:13px;color:%s;line-height:1.4">Уходит только текст заметки или чека и названия категорий. Суммы счетов и история остаются на телефоне.</div>' % t['muted']
    return X.screen(t, 'Настройки', sec(t, 'Умные функции') + group(t, [X.row(t, 'spark', 'ИИ на телефоне', 'Выключен · бета', ic('chev', t['muted'], 18, 2)), cloud]) + note
                    + sec(t, 'Помощь') + group(t, [X.row(t, 'note', 'Подсказки', 'Показываются')]))


def settings_b_on(t, v):
    return settings_b(t, v, True)


# --- Q2 A: a question before the first switch-on ---
def consent_a(t, v):
    d = X.dialog(t, 'cloud', 'Включить облачный разбор?', 'Тексты заметок и чеков и названия ваших категорий уйдут на сервер ИИ. Суммы счетов и история останутся на телефоне. Выключить можно в любой момент.',
                 X.btn_row(t, 'Включить', True, 'check') + X.btn_row(t, 'Не сейчас'), top_px=250)
    return X.overlay(screen_a(t, v), d)


# --- Q2 B: no window, the switch works at once, a pill confirms ---
def toast_b(t, v):
    pill = ('<div style="position:absolute;left:32px;right:32px;bottom:84px;height:56px;border-radius:999px;background:%s;border:1px solid %s;display:flex;align-items:center;gap:10px;padding:0 8px 0 8px">'
            '<span style="width:40px;height:40px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span>'
            '<span style="flex:1;font-size:14px;line-height:1.25">Облако включено. Выключить можно здесь же.</span>%s</div>'
            % (t['bar'], t['line'], U.mix(t['page'], t['acc'], .2), ic('cloud', X.ink_acc(t), 22, 1.9), ic('close', t['muted'], 20, 2)))
    # No scrim: the pill floats over the live screen.
    return X.overlay(settings_b(t, v, True), pill).replace('background:rgba(0,0,0,.45)', 'background:transparent')


# --- Q3: what quick notes show ---
LINES = ['кофе 3500', 'такси 2000', 'хлеб 850 карта', 'вчера аптека 4200', 'netflix 12 usd', 'aporte para vaca']


def notes_box(t):
    return X.card(t, '<div style="font-size:12px;letter-spacing:.04em;color:%s">ЗАМЕТКА</div>' % t['muted'] + ''.join('<div style="font-size:18px;line-height:1.6">%s</div>' % l for l in LINES)
                  + '<div style="font-size:13px;color:%s;margin-top:6px">Каждая строка — одна трата. Счёт и день — по желанию.</div>' % t['muted'], '14px 16px', 'min-height:260px')


def notes_a(t, v):
    line = '<div style="position:absolute;left:28px;right:28px;bottom:78px;text-align:center;font-size:13px;color:%s">%s Сложные строки прочитает облачный ИИ</div>' % (t['muted'], ic('cloud', t['muted'], 14, 2))
    return X.screen(t, 'Быстрые заметки', notes_box(t) + line + X.cta(t, 'Обработать · 6', True, 16, 'spark'))


def notes_busy_a(t, v):
    line = '<div style="position:absolute;left:28px;right:28px;bottom:78px;text-align:center;font-size:13px;color:%s">%s Читает облачный ИИ…</div>' % (t['muted'], ic('cloud', t['muted'], 14, 2))
    return X.screen(t, 'Быстрые заметки', notes_box(t) + line + X.cta(t, 'Разбираю…', False, 16))


def notes_b(t, v):
    return X.screen(t, 'Быстрые заметки', notes_box(t) + X.cta(t, 'Обработать · 6', True, 16, 'spark'))


def notes_busy_b(t, v):
    return X.screen(t, 'Быстрые заметки', notes_box(t) + X.cta(t, 'Разбираю…', False, 16))


DR = [('Кофе', 'Карта · сегодня', '−3 500', 'Кафе', 'hist'), ('Такси', 'Карта · сегодня', '−2 000', 'Такси', 'hist'), ('Хлеб', 'Наличные · сегодня', '−850', 'Продукты', 'hist'),
      ('Аптека', 'Карта · вчера', '−4 200', 'Здоровье', 'spark'), ('Netflix', 'Карта · сегодня', '−12 USD', 'Развлечения', 'spark'), ('Взнос на корову', 'Наличные · сегодня', '', 'нет категории', 'hist')]


def limit_common(t, v):
    rows = ''.join(M.draft(t, *d[:3], cat=d[3], src=d[4], warn=(i == 5), check=(i != 5)) for i, d in enumerate(DR))
    note = ('<div style="margin:0 16px 10px;padding:12px 16px;border-radius:24px;background:%s;font-size:14px;line-height:1.4;display:flex;gap:10px;align-items:flex-start">%s<span>Дневной лимит облачного разбора. Остальное разобрали правила и ИИ на телефоне, ничего не потеряно.</span></div>'
            % (U.mix(t['page'], t['warm'], .18), ic('cloud', t['warm'], 20, 2)))
    return X.screen(t, 'Черновики · 6', note + X.card(t, rows, '2px 16px') + X.cta(t, 'Записать 5', True, 16, 'check'))


BOARDS = [('CaSetA', settings_a), ('CaScreenA', screen_a), ('CaScreenAOn', screen_a_on), ('CaSetB', settings_b), ('CaSetBOn', settings_b_on),
          ('CaConsentA', consent_a), ('CaToastB', toast_b), ('CaNotesA', notes_a), ('CaNotesBusyA', notes_busy_a), ('CaNotesB', notes_b),
          ('CaNotesBusyB', notes_busy_b), ('CaLimit', limit_common)]


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
