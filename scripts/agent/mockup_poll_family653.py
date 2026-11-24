#!/usr/bin/env python3
"""Poll boards for #653 (leave / remove / delete family and account) and #661 (member limit 10).

Standalone PNGs like mockup_poll_notes628.py: `FmXxx.png` dark + `FmXxx@light.png`.
Usage: python3 scripts/agent/mockup_poll_family653.py <out-dir>  → <out-dir>/img/*.png   then   poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_bricks as B  # noqa: E402
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_accounts4 as A  # noqa: E402
import mockup_poll_notes628 as P  # noqa: E402
import mockup_poll_update as U  # noqa: E402

H = 900
LOGOUT = 'M10 5H6v14h4M14 8l4 4-4 4M18 12H9'
TRASH = P.TRASH
PERSON = 'M12 12a4 4 0 100-8 4 4 0 000 8zM5 20c0-4 3-6 7-6s7 2 7 6'
PLUS = 'M12 5v14M5 12h14'
CHEV = 'M9 6l6 6-6 6'
KEY = 'M14 10a4 4 0 11-3.5-4M10.5 13.5L4 20h3v-2h2v-2h2'
MEMBERS = [('М', 'Вы · владелец', 'masha@gmail.com'), ('А', 'Аня', 'anya@gmail.com'), ('П', 'Паша', 'pasha@gmail.com')]


def ink2(t):
    return t['acc'] if t['dark'] else '#5B1FA8'


def bad(t):
    return '#FF7A8A' if t['dark'] else '#B3203A'


def avatar(t, ch, size=40):
    return ('<span style="width:%dpx;height:%dpx;flex:none;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center;'
            'font-weight:700;font-size:%dpx">%s</span>' % (size, size, t['disc'], size // 2, ch))


def row(t, title, sub='', lead='', trail='', color=None, muted=False):
    c = color or t['ink']
    return ('<div style="min-height:56px;padding:8px 14px;display:flex;align-items:center;gap:12px;opacity:%s">%s<span style="flex:1;min-width:0">'
            '<div style="font-size:16px;color:%s">%s</div>%s</span>%s</div>' % (
                .45 if muted else 1, lead, c, title,
                ('<div style="font-size:13px;color:%s">%s</div>' % (t['muted'], sub)) if sub else '', trail))


def group(t, rows):
    sep = '<div style="height:1px;background:%s;opacity:.4;margin:0 14px"></div>' % t['line']
    return ('<div style="margin:8px 12px 0;border-radius:24px;background:%s;border:1px solid %s;overflow:hidden">%s</div>' % (t['surf'], t['line'], sep.join(rows)))


def label(t, text):
    return '<div style="margin:16px 28px 0;font-size:13px;font-weight:600;letter-spacing:.04em;color:%s">%s</div>' % (t['muted'], text)


def glyph(t, path, color=None, size=22):
    return N.glyph(path, color or t['muted'], size)


def hero(t, count, limit_text, full=False):
    pills = ''.join('<span style="margin-left:%dpx">%s</span>' % (0 if i == 0 else -10, avatar(t, m[0], 44)) for i, m in enumerate(MEMBERS[:count]))
    tone = bad(t) if full else t['muted']
    return ('<div style="margin:84px 12px 0;padding:16px;border-radius:24px;background:linear-gradient(135deg,%s,%s);border:1px solid %s;display:flex;'
            'align-items:center;gap:14px"><span style="display:flex">%s</span><span><div style="font-size:20px;font-weight:700">Общий бюджет</div>'
            '<div style="font-size:13px;color:%s">%s</div></span></div>' % (U.mix(t['page'], t['prim'], .28), U.mix(t['page'], t['acc'], .3), t['line'], pills, tone, limit_text))


def members(t, count=3, chev=False, invite=True, full=False):
    rows = []
    for i, m in enumerate(MEMBERS[:count]):
        rows.append(row(t, m[1], m[2], avatar(t, m[0]), glyph(t, CHEV, size=20) if chev and i else ''))
    if full:
        rows.append(row(t, 'Ещё 7 участников', 'список прокручивается', avatar(t, '+7')))
    if invite:
        plus = ('<span style="width:40px;height:40px;border-radius:50%%;border:1px dashed %s;display:flex;align-items:center;justify-content:center">%s</span>'
                % (t['line'], glyph(t, PLUS, ink2(t), 18)))
        if full:
            rows.append(row(t, 'Пригласить', 'Семья заполнена: 10 из 10', plus, muted=True))
        else:
            rows.append(row(t, 'Пригласить', 'по адресу Google-аккаунта', plus, color=ink2(t)))
    return group(t, rows)


def scrim(t):
    return '<div style="position:absolute;inset:0;background:rgba(0,0,0,.55);z-index:4"></div>'


def dialog(t, title, text, buttons, top=250):
    btns = ''
    for i, (txt, kind) in enumerate(buttons):
        if kind == 'primary':
            st = 'background:%s;color:%s;box-shadow:inset 0 0 0 1.5px %s' % (t['prim'], t['onp'], A.edge(t))
        elif kind == 'danger':
            st = 'background:%s;color:%s' % (bad(t), '#FFFFFF' if not t['dark'] else '#2A0008')
        else:
            st = 'color:%s;box-shadow:inset 0 0 0 1.5px %s' % (ink2(t), t['line'])
        btns += '<div style="height:48px;border-radius:999px;display:flex;align-items:center;justify-content:center;font-weight:600;margin-top:8px;%s">%s</div>' % (st, txt)
    return ('<div style="position:absolute;left:24px;right:24px;top:%dpx;z-index:5;padding:20px;border-radius:28px;background:%s;border:1px solid %s">'
            '<div style="font-size:20px;font-weight:700">%s</div><div style="font-size:14px;color:%s;margin:8px 0 12px">%s</div>%s</div>' % (
                top, t['page'], t['line'], title, t['muted'], text, btns))


def sheet(t, title, sub, rows, top=420):
    return ('<div style="position:absolute;left:0;right:0;top:%dpx;bottom:0;z-index:5;padding:12px 16px;border-radius:28px 28px 0 0;background:%s;border:1px solid %s">'
            '<div style="width:40px;height:4px;border-radius:2px;background:%s;margin:0 auto 12px"></div><div style="font-size:20px;font-weight:700">%s</div>'
            '<div style="font-size:13px;color:%s;margin-bottom:8px">%s</div>%s</div>' % (top, t['page'], t['line'], t['line'], title, t['muted'], sub, rows))


def screen(t, body, cap, overlay=''):
    return U.phone(t, P.topbar(t, 'Семья') + body + overlay + P.caption(t, cap, 740), H)


def account_group(t, leave='Выйти из семьи', extra=''):
    return label(t, 'АККАУНТ') + group(t, [row(t, leave, 'Вы перестанете видеть общие данные', glyph(t, LOGOUT)) + extra])


# ---- Q1: member leaves --------------------------------------------------------------------------------------------

def leave_a(t, v):
    return screen(t, hero(t, 3, '3 участника из 10') + members(t) + account_group(t),
                  'А: два пути. «Оставить копию» — данные остаются на телефоне, синк останавливается. «Стереть» — телефон очищается (перед этим сохранится копия файла).',
                  scrim(t) + dialog(t, 'Выйти из семьи?', 'Другие участники продолжат пользоваться общим бюджетом.',
                                    [('Выйти и оставить данные', 'primary'), ('Выйти и стереть с телефона', 'danger'), ('Отмена', 'text')], 230))


def leave_b(t, v):
    return screen(t, hero(t, 3, '3 участника из 10') + members(t) + account_group(t),
                  'Б: один путь, без выбора. Данные всегда остаются на телефоне; стереть их можно позже — «Резервная копия → Стереть все данные».',
                  scrim(t) + dialog(t, 'Выйти из семьи?', 'Данные на телефоне останутся, синхронизация остановится. Другие участники продолжат пользоваться общим бюджетом.',
                                    [('Выйти', 'primary'), ('Отмена', 'text')], 270))


# ---- Q2: owner removes a member / owner leaves --------------------------------------------------------------------

def remove_sheet(t):
    rows = (row(t, 'Исключить из семьи', 'Аня потеряет доступ. Данные на её телефоне останутся.', glyph(t, LOGOUT, bad(t)), color=bad(t))
            + row(t, 'Закрыть', '', glyph(t, '', t['muted'])))
    return sheet(t, 'Аня', 'anya@gmail.com · в семье с 3 ноября', rows, 500)


def owner_a(t, v):
    return screen(t, hero(t, 3, '3 участника из 10') + members(t, chev=True) + account_group(t, 'Выйти из семьи'),
                  'А: нажали на участника → шторка «Исключить из семьи». Владелец уходит через «Выйти из семьи»: приложение предложит передать владение (шторка со списком участников).',
                  scrim(t) + remove_sheet(t))


def owner_b(t, v):
    return screen(t, hero(t, 3, '3 участника из 10') + members(t, chev=True)
                  + label(t, 'АККАУНТ') + group(t, [row(t, 'Выйти из семьи', 'Владелец не может выйти: сначала удалите семью', glyph(t, LOGOUT), muted=True)]),
                  'Б: то же исключение, но владелец не может просто уйти — только удалить семью целиком (передача владения — позже, если понадобится).',
                  scrim(t) + remove_sheet(t))


# ---- Q3: where the destructive actions live -----------------------------------------------------------------------

def danger_rows(t, account_here):
    rows = [row(t, 'Удалить семью', 'Общие данные удалятся из облака у всех', glyph(t, TRASH, bad(t)), color=bad(t))]
    if account_here:
        rows.append(row(t, 'Удалить аккаунт', 'Войти снова можно, данные облака не вернуть', glyph(t, PERSON, bad(t)), color=bad(t)))
    return label(t, 'ОПАСНАЯ ЗОНА') + group(t, rows)


def danger_a(t, v):
    return screen(t, hero(t, 3, '3 участника из 10') + members(t) + account_group(t, 'Выйти из семьи') + danger_rows(t, True),
                  'А: всё на экране «Семья», внизу: «Удалить семью» (только владелец) и «Удалить аккаунт». Удаление аккаунта видно сразу — так хочет Google Play.')


def danger_b(t, v):
    return screen(t, hero(t, 3, '3 участника из 10') + members(t) + account_group(t, 'Выйти из семьи') + danger_rows(t, False)
                  + label(t, 'ЕЩЁ → НАСТРОЙКИ') + group(t, [row(t, 'Аккаунт', 'masha@gmail.com · удалить аккаунт', glyph(t, PERSON), glyph(t, CHEV, size=20))]),
                  'Б: «Удалить семью» — на экране «Семья»; «Удалить аккаунт» — в «Ещё → Настройки → Аккаунт» (рядом с почтой). «Семья» остаётся чище.')


# ---- Q4: the removed phone ----------------------------------------------------------------------------------------

def removed_a(t, v):
    msg = ('<div style="margin:84px 12px 0;padding:16px;border-radius:24px;background:%s;border:1px solid %s"><b style="font-size:16px">Вас убрали из семьи</b>'
           '<div style="font-size:14px;color:%s;margin-top:6px">Данные на телефоне остались. Общий бюджет больше не обновляется.</div></div>' % (
               U.mix(t['page'], t['warm'], .16), t['warm'], t['ink']))
    btns = ''.join('<div style="margin:10px 12px 0;height:48px;border-radius:999px;display:flex;align-items:center;justify-content:center;font-weight:600;%s">%s</div>' % (s, x) for x, s in [
        ('Создать свою семью', 'background:%s;color:%s;box-shadow:inset 0 0 0 1.5px %s' % (t['prim'], t['onp'], A.edge(t))),
        ('Выйти из аккаунта', 'color:%s;box-shadow:inset 0 0 0 1.5px %s' % (ink2(t), t['line']))])
    return screen(t, msg + btns, 'А: карточка на экране «Семья» (там, куда человек и придёт посмотреть). Синк останавливается, плашка «Обзора» обычная.')


def removed_b(t, v):
    base = ('<div style="margin:84px 12px 0;padding:16px;border-radius:24px;background:%s;border:1px solid %s;font-size:16px">Вы вошли как pasha@gmail.com</div>' % (t['surf'], t['line']))
    return screen(t, base, 'Б: окно при следующем открытии приложения; после «Понятно» — тот же экран «Семья» без доступа (как сейчас).',
                  scrim(t) + dialog(t, 'Вас убрали из семьи', 'Данные на телефоне остались. Общий бюджет больше не обновляется.', [('Понятно', 'primary')], 280))


# ---- Q5: limit of 10 ----------------------------------------------------------------------------------------------

def limit_a(t, v):
    return screen(t, hero(t, 3, '10 участников из 10', True) + members(t, full=True)
                  + '<div style="margin:10px 28px 0;font-size:13px;color:%s">Больше 10 человек в одной семье нельзя.</div>' % t['muted'],
                  'А: «N из 10» видно всегда; на 10-м «Пригласить» гаснет с понятной подписью — человек не нажмёт зря.')


def limit_b(t, v):
    return screen(t, hero(t, 3, '9 участников из 10') + members(t), 'Б: счётчик не показываем; лимит всплывает ошибкой, когда пытаются пригласить 11-го.',
                  scrim(t) + dialog(t, 'Семья заполнена', 'В семье может быть не больше 10 участников. Чтобы пригласить ещё кого-то, уберите одного из участников.',
                                    [('Понятно', 'primary')], 280))


BOARDS = [('FmLeaveA', leave_a), ('FmLeaveB', leave_b), ('FmOwnerA', owner_a), ('FmOwnerB', owner_b), ('FmDangerA', danger_a), ('FmDangerB', danger_b),
          ('FmRemovedA', removed_a), ('FmRemovedB', removed_b), ('FmLimitA', limit_a), ('FmLimitB', limit_b)]


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
