#!/usr/bin/env python3
"""Poll boards for #666 (invite-code gate in the app) and #660 (landing for the APK).

Standalone PNGs like mockup_poll_family653.py: `GtXxx.png` dark + `GtXxx@light.png`.
Usage: python3 scripts/agent/mockup_poll_gate666.py <out-dir>  → <out-dir>/img/*.png   then   poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_accounts4 as A  # noqa: E402
import mockup_poll_family653 as F  # noqa: E402
import mockup_poll_notes628 as P  # noqa: E402
import mockup_poll_update as U  # noqa: E402

H = 820
LOCK = 'M7 11V8a5 5 0 0110 0v3M6 11h12v9H6z'
KEY = 'M14 10a4 4 0 11-3.5-4M10.5 13.5L4 20h3v-2h2v-2h2'
DOWN = 'M12 4v11M7 11l5 5 5-5M5 20h14'
CAP = 740


def pill(t, text, filled=True, w=None):
    st = ('background:%s;color:%s;box-shadow:inset 0 0 0 1.5px %s' % (t['prim'], t['onp'], A.edge(t)) if filled
          else 'color:%s;box-shadow:inset 0 0 0 1.5px %s' % (F.ink2(t), t['line']))
    return '<div style="height:48px;border-radius:999px;display:flex;align-items:center;justify-content:center;font-weight:600;margin-top:10px;%s">%s</div>' % (st, text)


def code_field(t, value='', hint='Код приглашения'):
    shown = ('<span style="font-family:%s;font-size:20px;letter-spacing:.08em">%s</span>' % (U.MONO.split(':')[1].split(';')[0] if False else 'monospace', value)
             if value else '<span style="color:%s">K7QX-M2PD-9WTA</span>' % t['muted'])
    return ('<div style="margin-top:16px;height:56px;border-radius:16px;border:1.5px solid %s;background:%s;padding:0 16px;display:flex;align-items:center;'
            'justify-content:space-between"><span style="display:flex;flex-direction:column"><span style="font-size:12px;color:%s">%s</span>%s</span>%s</div>'
            % (t['line'], t['surf'], t['muted'], hint, shown, F.glyph(t, KEY, t['muted'], 22)))


def intro(t, title, text):
    return ('<div style="margin:90px 24px 0;text-align:center"><div style="width:72px;height:72px;margin:0 auto;border-radius:24px;background:%s;display:flex;'
            'align-items:center;justify-content:center">%s</div><div style="font-size:24px;font-weight:700;margin-top:18px">%s</div>'
            '<div style="font-size:15px;color:%s;margin-top:8px;line-height:1.4">%s</div></div>' % (t['disc'], F.glyph(t, LOCK, F.ink2(t), 34), title, t['muted'], text))


def phone(t, body, cap):
    return U.phone(t, body + P.caption(t, cap, CAP), H)


# ---- Q1: where the code is asked ------------------------------------------------------------------------------------

def gate_a(t, v):
    form = '<div style="margin:0 24px">%s%s<div style="text-align:center;margin-top:14px;font-size:14px;color:%s">Где взять код?</div></div>' % (
        code_field(t), pill(t, 'Продолжить'), F.ink2(t))
    return phone(t, intro(t, 'Co-Shell для приглашённых', 'Введите код, который вам прислали. Он нужен один раз — потом всё работает без интернета.') + form,
                 'А: отдельный экран при первом запуске, за ним пока ничего нет. Простое и понятное; «Где взять код?» — подсказка «Попросите у того, кто вас позвал».')


def gate_b(t, v):
    back = ('<div style="margin:84px 12px 0;padding:16px;border-radius:24px;background:linear-gradient(135deg,%s,%s);border:1px solid %s"><div style="font-size:13px;color:%s">Всего на счетах</div>'
            '<div style="font-size:30px;font-weight:600;margin-top:4px">0 ARS</div></div>' % (U.mix(t['page'], t['prim'], .28), U.mix(t['page'], t['acc'], .3), t['line'], t['muted']))
    body = ('<div style="padding:4px 4px 0"><div style="font-size:20px;font-weight:700">Код приглашения</div>'
            '<div style="font-size:14px;color:%s;margin-top:4px">Co-Shell пока для приглашённых. Введите код, который вам прислали.</div>%s%s'
            '<div style="text-align:center;margin-top:12px;font-size:14px;color:%s">Где взять код?</div></div>' % (t['muted'], code_field(t), pill(t, 'Продолжить'), F.ink2(t)))
    return phone(t, P.topbar(t, 'Обзор').replace(N.glyph(N.ARROW, t['ink'], 22), '') + back + F.scrim(t) + F.sheet(t, '', '', body, 330),
                 'Б: шторка поверх пустого «Обзора» — как выбор языка при первом запуске. Видно, что за дверью, но Обзор можно листать только после кода.')


# ---- Q2: the code was revoked -----------------------------------------------------------------------------------------

def overview(t):
    rows = ''.join(F.row(t, n, s, F.avatar(t, n[0]), '<span style="font-size:15px">%s</span>' % a) for n, s, a in
                   [('Кафе', 'Galicia · сегодня', '−3 500'), ('Продукты', 'Galicia · вчера', '−12 222'), ('Такси', 'Наличные · сегодня', '−2 000')])
    hero = ('<div style="margin:84px 12px 0;padding:16px;border-radius:24px;background:linear-gradient(135deg,%s,%s);border:1px solid %s"><div style="font-size:13px;color:%s">Всего на счетах</div>'
            '<div style="font-size:30px;font-weight:600;margin-top:4px">1 284 300 ARS</div></div>' % (U.mix(t['page'], t['prim'], .28), U.mix(t['page'], t['acc'], .3), t['line'], t['muted']))
    return P.topbar(t, 'Обзор').replace(N.glyph(N.ARROW, t['ink'], 22), '') + hero + F.group(t, [rows])


def locked_a(t, v):
    return phone(t, overview(t) + F.scrim(t) + F.dialog(t, 'Доступ закрыт', 'Код приглашения больше не действует. Данные на телефоне остались, но записывать новое нельзя. Обратитесь к тому, кто вас пригласил.',
                                                          [('Ввести другой код', 'primary'), ('Сохранить резервную копию', 'text')], 250),
                 'А: окно поверх «Обзора» при открытии. Данные видны, резервная копия и экспорт работают; «+» и синхронизация закрыты — как «ввод закрыт до обновления».')


def locked_b(t, v):
    body = (intro(t, 'Доступ закрыт', 'Код приглашения больше не действует. Данные на телефоне остались. Обратитесь к тому, кто вас пригласил.')
            + '<div style="margin:0 24px">%s%s</div>' % (pill(t, 'Ввести другой код'), pill(t, 'Сохранить резервную копию', False)))
    return phone(t, body, 'Б: целый экран, как при первом запуске, без «Обзора» за ним. Строже, но данные не видны, пока нет кода — только копия.')


# ---- Q3: the landing ----------------------------------------------------------------------------------------------------

def page_top(t, title):
    return ('<div style="margin:0;padding:18px 20px;display:flex;align-items:center;gap:10px;border-bottom:1px solid %s"><span style="width:28px;height:28px;border-radius:9px;background:%s"></span>'
            '<b style="font-size:18px">%s</b><span style="margin-left:auto;font-size:13px;color:%s">RU · EN · ES</span></div>' % (t['line'], t['prim'], title, t['muted']))


def step(t, n, text):
    return ('<div style="display:flex;gap:12px;align-items:flex-start;margin-top:12px"><span style="width:26px;height:26px;flex:none;border-radius:50%%;background:%s;display:flex;'
            'align-items:center;justify-content:center;font-weight:700;font-size:13px">%d</span><span style="font-size:15px;line-height:1.35">%s</span></div>' % (t['disc'], n, text))


def land_a(t, v):
    body = (page_top(t, 'Co-Shell') + '<div style="padding:28px 20px 0"><div style="font-size:28px;font-weight:700;line-height:1.15">Семейный бюджет на двоих</div>'
            '<div style="font-size:15px;color:%s;margin-top:10px;line-height:1.4">Для нескольких валют, без лишнего. Работает без интернета, общий бюджет — на всех телефонах семьи.</div>' % t['muted']
            + '<div style="height:56px;border-radius:999px;margin-top:22px;display:flex;align-items:center;justify-content:center;gap:10px;font-weight:700;font-size:17px;'
            'background:%s;color:%s;box-shadow:inset 0 0 0 1.5px %s">%s Скачать · 6,9 МБ</div>' % (t['prim'], t['onp'], A.edge(t), F.glyph(t, DOWN, t['onp'], 22))
            + '<div style="font-size:12px;color:%s;margin-top:8px;text-align:center">Версия 1.8 · Android 8+ · SHA-256 a3f1…c92e</div>' % t['muted']
            + '<div style="margin-top:26px;padding:14px 16px;border-radius:20px;background:%s;border:1px solid %s"><b>Нужен код приглашения</b>'
            '<div style="font-size:14px;color:%s;margin-top:4px">Приложение пока для приглашённых: при первом запуске введите код. Попросите его у того, кто вас позвал.</div></div>' % (t['surf'], t['line'], t['muted'])
            + '<div style="font-size:13px;font-weight:600;letter-spacing:.04em;color:%s;margin-top:26px">КАК УСТАНОВИТЬ</div>' % t['muted']
            + step(t, 1, 'Нажмите «Скачать» и откройте файл.') + step(t, 2, 'Разрешите установку из этого источника.') + step(t, 3, 'Если Play Protect предупредит — «Всё равно установить».')
            + '<div style="font-size:13px;color:%s;margin-top:16px">Политика конфиденциальности · Удалить данные</div></div>' % t['muted'])
    return phone(t, body, 'А: «Скачать» сразу, под ним — «нужен код» и три шага установки. Быстро для тех, у кого код уже есть.')


def land_b(t, v):
    body = (page_top(t, 'Co-Shell') + '<div style="padding:28px 20px 0"><div style="font-size:28px;font-weight:700;line-height:1.15">Закрытое тестирование</div>'
            '<div style="font-size:15px;color:%s;margin-top:10px;line-height:1.4">Co-Shell — семейный бюджет на нескольких валютах. Сейчас им пользуются только приглашённые.</div>' % t['muted']
            + '<div style="font-size:13px;font-weight:600;letter-spacing:.04em;color:%s;margin-top:22px">ТРИ ШАГА</div>' % t['muted']
            + step(t, 1, '<b>Получите код</b> у того, кто вас пригласил.') + step(t, 2, '<b>Скачайте и установите</b>: разрешите установку из этого источника.')
            + step(t, 3, '<b>Введите код</b> при первом запуске.')
            + '<div style="height:56px;border-radius:999px;margin-top:24px;display:flex;align-items:center;justify-content:center;gap:10px;font-weight:700;font-size:17px;'
            'background:%s;color:%s;box-shadow:inset 0 0 0 1.5px %s">%s Скачать · 6,9 МБ</div>' % (t['prim'], t['onp'], A.edge(t), F.glyph(t, DOWN, t['onp'], 22))
            + '<div style="font-size:12px;color:%s;margin-top:8px;text-align:center">Версия 1.8 · Android 8+ · SHA-256 a3f1…c92e</div>' % t['muted']
            + '<div style="font-size:13px;color:%s;margin-top:16px">Политика конфиденциальности · Удалить данные</div></div>' % t['muted'])
    return phone(t, body, 'Б: сначала объяснение «три шага» (код → установка → ввод), кнопка «Скачать» в конце. Меньше людей скачают зря, но дольше до кнопки.')


BOARDS = [('GtGateA', gate_a), ('GtGateB', gate_b), ('GtLockedA', locked_a), ('GtLockedB', locked_b), ('GtLandA', land_a), ('GtLandB', land_b)]


def main():
    out = sys.argv[1]
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    for name, fn in BOARDS:
        for suffix, dark in (('', True), ('@light', False)):
            t = U.theme(dark)
            open(os.path.join(out, 'html', name + suffix + '.html'), 'w', encoding='utf-8').write(N.page(fn(t, name), 390, H, dict(page=t['page'])))
    js = os.path.join(out, 'render.js')
    src = open(os.path.join(HERE, 'mockup_poll_update.py'), encoding='utf-8').read()
    open(js, 'w').write(src[src.index("'''const") + 3:src.index("''')")].replace('\\\\d', '\\d'))
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('boards → %s/img' % out)


if __name__ == '__main__':
    main()
