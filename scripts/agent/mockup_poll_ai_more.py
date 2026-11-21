#!/usr/bin/env python3
"""Poll boards, part 2 (#583): the AI package (#587), quick notes (#586), drafts and bank notifications (#592), hints. Bricks — `mockup_poll_ai_parts`."""
import math
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_poll_ai_parts as X  # noqa: E402
import mockup_poll_accounts4 as A  # noqa: E402
import mockup_poll_update as U  # noqa: E402

ic, MONO = X.ic, X.MONO


def meter(t, frac):
    return ('<div style="height:8px;border-radius:4px;background:%s;margin:12px 0 6px"><div style="width:%d%%;height:8px;border-radius:4px;background:%s;box-shadow:0 0 0 1px %s"></div></div>'
            % (t['bar'], frac * 100, t['prim'], A.edge(t)))


def hero(t, title, text, extra=''):
    disc = '<span style="width:64px;height:64px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center;margin-bottom:12px">%s</span>' % (U.mix(t['page'], t['acc'], .2), ic('spark', X.ink_acc(t), 32, 1.7))
    return X.card(t, disc + '<div style="font-size:22px;font-weight:700">%s</div><div style="font-size:14px;color:%s;margin-top:6px;line-height:1.45">%s</div>%s' % (title, t['muted'], text, extra), '18px')


def facts(t, items):
    return X.card(t, ''.join(X.row(t, i, a, b) for i, a, b in items), '4px 16px')


FACTS = [('cloud', 'Размер', '≈ 0,6 ГБ, скачать один раз'), ('chip', 'Память телефона', 'Нужно от 6 ГБ'), ('wallet', 'Приватность', 'Всё считается на телефоне, без интернета')]
TEXT = 'Необязательно. ИИ читает незнакомые чеки, разбирает заметки и голос и подсказывает категорию для нового получателя.'


def pkg_off(t, v):
    return X.screen(t, 'ИИ на телефоне', hero(t, 'Выключен', TEXT, '<div style="margin-top:8px">%s</div>' % X.tag(t, 'без него всё работает как раньше', 'check')) + facts(t, FACTS) + X.cta(t, 'Скачать · 0,6 ГБ', True, 16, 'cloud'))


def pkg_confirm(t, v):
    d = X.dialog(t, 'cloud', 'Скачать ИИ?', '0,6 ГБ. Лучше по Wi-Fi: мобильный интернет может быть платным. Удалить можно в любой момент.', X.btn_row(t, 'Скачать', True, 'cloud') + X.btn_row(t, 'Отмена'))
    return X.overlay(pkg_off(t, v), d)


def pkg_load_a(t, v):
    body = hero(t, 'Скачиваю…', 'Можно закрыть экран: загрузка продолжится.', meter(t, .42) + '<div style="display:flex;justify-content:space-between;font-size:13px;color:%s"><span style="%s">250 из 600 МБ</span><span>42 %%</span></div>' % (t['muted'], MONO))
    return X.screen(t, 'ИИ на телефоне', body + facts(t, FACTS[1:]) + X.cta(t, 'Отменить', False))


def pkg_load_b(t, v):
    r, c = 70, 2 * math.pi * 70
    ring = ('<div style="display:flex;justify-content:center;margin:34px 0 18px;position:relative"><svg width="180" height="180" viewBox="0 0 180 180"><circle cx="90" cy="90" r="%d" fill="none" stroke="%s" stroke-width="14"/>'
            '<circle cx="90" cy="90" r="%d" fill="none" stroke="%s" stroke-width="14" stroke-linecap="round" stroke-dasharray="%.1f %.1f" transform="rotate(-90 90 90)"/></svg>'
            '<div style="position:absolute;inset:0;display:flex;flex-direction:column;align-items:center;justify-content:center"><b style="font-size:34px;%s">42%%</b><span style="font-size:13px;color:%s">250 из 600 МБ</span></div></div>' % (r, t['bar'], r, t['prim'], c * .42, c, MONO, t['muted']))
    return X.screen(t, 'ИИ на телефоне', ring + '<div style="text-align:center;font-size:16px;font-weight:700">Скачиваю ИИ</div><div style="text-align:center;font-size:14px;color:%s;margin:6px 24px">Можно закрыть экран: загрузка продолжится.</div>' % t['muted'] + X.cta(t, 'Отменить', False))


def pkg_on(t, v):
    use = X.card(t, '<div style="font-size:12px;letter-spacing:.04em;color:%s;margin-bottom:2px">ГДЕ РАБОТАЕТ</div>' % t['muted'] + ''.join(X.row(t, i, a, b) for i, a, b in [
        ('note', 'Незнакомые чеки', '«Прочитать с ИИ»'), ('draft', 'Быстрые заметки и голос', 'разбор сложных фраз'), ('spark', 'Категория для нового получателя', 'подпись «предложено ИИ»')]), '8px 16px')
    return X.screen(t, 'ИИ на телефоне', hero(t, 'Включён', 'Скачано 0,6 ГБ. Работает без интернета.', '<div style="margin-top:8px">%s</div>' % X.tag(t, 'готов', 'check')) + use + X.cta(t, 'Удалить с телефона', False, 16, 'trash'))


def pkg_nano(t, v):
    return X.screen(t, 'ИИ на телефоне', hero(t, 'Встроенный ИИ телефона', 'Этот телефон умеет это сам. Ничего скачивать не нужно, место не занимается.', '<div style="margin-top:8px">%s</div>' % X.tag(t, 'готов', 'check')) + facts(t, [FACTS[2]]))


def pkg_weak(t, v):
    return X.screen(t, 'ИИ на телефоне', hero(t, 'Этому телефону не подойдёт', 'Для ИИ нужно от 6 ГБ памяти, здесь 4. Всё остальное работает как раньше: чеки по QR, шаблоны, умная категория по истории.') + facts(t, FACTS[1:2]))


def set_row(t, v):
    sec = lambda s: '<div style="margin:14px 20px 4px;font-size:13px;color:%s">%s</div>' % (t['muted'], s)
    g = lambda rows: X.card(t, ''.join(rows), '4px 16px')
    return X.screen(t, 'Настройки', sec('Внешний вид') + g([X.row(t, 'gear', 'Тема', 'Системная'), X.row(t, 'wallet', 'Валюты итогов', 'USD · ARS')])
                    + sec('Умные функции') + g([X.row(t, 'spark', 'ИИ на телефоне', 'Выключен · необязательно', ic('chev', t['muted'], 18, 2))])
                    + sec('Помощь') + g([X.row(t, 'note', 'Подсказки', 'Показываются')]))


# --- Q4: quick notes (#586) ---
LINES = ['кофе 3500', 'такси 2000', 'хлеб 850 карта', 'вчера аптека 4200']


def notes_a(t, v):
    box = X.card(t, '<div style="font-size:12px;letter-spacing:.04em;color:%s">ЗАМЕТКА</div>' % t['muted'] + ''.join('<div style="font-size:18px;line-height:1.7;%s">%s</div>' % ('' , l) for l in LINES)
                 + '<div style="font-size:13px;color:%s;margin-top:6px">Каждая строка — одна трата. Счёт и день — по желанию.</div>' % t['muted'], '14px 16px', 'min-height:230px')
    return X.screen(t, 'Быстрые заметки', box + X.cta(t, 'Обработать · 4', True, 16, 'spark'))


def draft(t, title, sub, amt, cat=None, src='hist', check=True, warn=False):
    mark = X.tag(t, 'нет категории', 'close', True) if warn else X.caption(t, cat, src)
    if check:
        box = '<span style="width:24px;height:24px;border-radius:8px;flex:none;background:%s;box-shadow:inset 0 0 0 2px %s;display:flex;align-items:center;justify-content:center">%s</span>' % (t['prim'], A.edge(t), ic('check', t['onp'], 16, 3))
    else:
        box = '<span style="width:24px;height:24px;border-radius:8px;flex:none;border:2px solid %s;box-sizing:border-box"></span>' % t['line']
    return ('<div style="display:flex;align-items:center;gap:12px;min-height:60px;padding:6px 0;border-top:1px solid %s">%s<span style="flex:1;min-width:0"><div style="font-size:16px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">%s</div>'
            '<div style="font-size:13px;color:%s;white-space:nowrap">%s</div><div style="margin-top:2px">%s</div></span><span style="%sfont-size:15px;white-space:nowrap">%s</span></div>' % (t['line'], box, title, t['muted'], sub, mark, MONO, amt))


DR = [('Кофе', 'Карта · сегодня', '−3 500', 'Кафе', 'hist'), ('Такси', 'Карта · сегодня', '−2 000', 'Такси', 'hist'), ('Хлеб', 'Наличные · сегодня', '−850', 'Продукты', 'hist'),
      ('Аптека', 'Карта · вчера', '−4 200', 'Здоровье', 'spark')]


def notes_res(t, v):
    rows = ''.join(draft(t, *d) for d in DR)
    return X.screen(t, 'Черновики · 4', X.card(t, rows, '2px 16px') + '<div style="margin:0 20px;font-size:13px;color:%s">Нажмите на строку, чтобы поправить.</div>' % t['muted'] + X.cta(t, 'Записать 4', True, 16, 'check'))


def notes_one(t, v):
    box = X.card(t, '<div style="font-size:12px;letter-spacing:.04em;color:%s">ЗАМЕТКА</div>' % t['muted'] + ''.join('<div style="font-size:17px;line-height:1.6">%s</div>' % l for l in LINES[:3]), '12px 16px')
    rows = X.card(t, '<div style="font-size:12px;letter-spacing:.04em;color:%s">РАЗОБРАНО</div>' % t['muted'] + ''.join(draft(t, *d) for d in DR[:3]), '12px 16px')
    return X.screen(t, 'Быстрые заметки', box + rows + X.cta(t, 'Записать 3', True, 16, 'check'))


def long_drafts(t, v):
    names = ['Кофе', 'Такси', 'Хлеб', 'Аптека', 'Disco', 'Kiosco', 'Netflix', 'YPF', 'Uber', 'Farmacity', 'Carrefour', 'Starbucks', 'Spotify', 'Cabify', 'Dia', 'Libros']
    rows = ''
    for i, n in enumerate(names):
        rows += draft(t, n, ['Карта', 'Наличные', 'Galicia'][i % 3] + ' · ' + ['сегодня', 'вчера', '17 нояб.'][i % 3] + (' · из уведомления' if i % 4 == 1 else ''), '−%s' % ('{:,}'.format(900 + 713 * i).replace(',', ' ')),
                      ['Кафе', 'Такси', 'Продукты', 'Здоровье'][i % 4], 'hist' if i % 5 else 'spark', check=i not in (6, 11), warn=i in (6, 11))
    head_line = '<div style="margin:0 20px 6px;font-size:13px;color:%s">16 черновиков · 2 без категории · выбрано 14</div>' % t['muted']
    return X.screen(t, 'Черновики', head_line + '<div style="margin:0 16px;padding:0 16px;border-radius:24px;background:%s;border:1px solid %s;overflow:hidden;height:560px">%s</div>' % (t['surf'], t['line'], rows) + X.cta(t, 'Записать 14', True, 16, 'check'))


# --- Q5: bank notifications and the common drafts list (#592) ---
def drafts_list(t, v):
    rows = (draft(t, 'Kiosco', 'Galicia · сегодня · из уведомления', '−12 500', 'Продукты', 'hist') + draft(t, 'Starbucks', 'Mercado Pago · сегодня · из уведомления', '−4 800', 'Кафе', 'spark')
            + draft(t, 'Кофе', 'Карта · вчера · из заметки', '−3 500', 'Кафе', 'hist') + draft(t, 'Перевод', 'Т-Банк · вчера · из выписки', '−15 000', warn=True, check=False))
    return X.screen(t, 'Черновики · 4', X.card(t, rows, '2px 16px') + '<div style="margin:0 20px;font-size:13px;color:%s">Заметки, уведомления банков и выписки — в одном месте.</div>' % t['muted'] + X.cta(t, 'Записать 3', True, 16, 'check'))


def notif_perm(t, v):
    pts = ''.join('<div style="display:flex;gap:8px;margin-top:8px;font-size:14px">%s<span>%s</span></div>' % (ic('check', X.ink_acc(t), 18, 2.4), s) for s in
                  ['Читаются только уведомления выбранных приложений', 'Текст остаётся на телефоне и нигде не хранится', 'Выключить можно в любой момент'])
    d = X.dialog(t, 'bell', 'Читать уведомления банков?', 'Покупка в банке — и в «Черновиках» уже готовая операция. Остаётся подтвердить.' + pts,
                 X.btn_row(t, 'Открыть настройки', True) + X.btn_row(t, 'Не сейчас'), 190)
    return X.overlay(X.screen(t, 'Уведомления банков', ''), d)


def notif_apps(t, v):
    apps = [('Galicia', 'разбор готов', 1), ('Mercado Pago', 'разбор готов', 1), ('Т-Банк', 'нужен ИИ', 1), ('Binance', 'нужен ИИ', 0), ('Сбер', 'нужен ИИ', 0), ('Revolut', 'нужен ИИ', 0)]
    rows = ''
    for n, s, on in apps:
        sw = ('<span style="width:48px;height:28px;border-radius:999px;background:%s;box-shadow:inset 0 0 0 2px %s;position:relative;display:block"><span style="position:absolute;right:4px;top:4px;width:20px;height:20px;border-radius:50%%;background:%s"></span></span>' % (t['prim'], A.edge(t), t['onp'])
              if on else '<span style="width:48px;height:28px;border-radius:999px;border:2px solid %s;position:relative;display:block"><span style="position:absolute;left:4px;top:4px;width:16px;height:16px;border-radius:50%%;background:%s"></span></span>' % (t['line'], t['muted']))
        mark = X.tag(t, s, 'spark') if s == 'нужен ИИ' else X.caption(t, s, 'check')
        rows += '<div style="display:flex;align-items:center;gap:12px;min-height:60px;border-top:1px solid %s"><span style="flex:1"><div style="font-size:16px">%s</div>%s</span>%s</div>' % (t['line'], n, mark, sw)
    note = '<div style="margin:0 20px;font-size:13px;color:%s">«Нужен ИИ» — работает после установки «ИИ на телефоне» (Ещё → Настройки).</div>' % t['muted']
    return X.screen(t, 'Уведомления банков', X.card(t, rows, '0 16px') + note)


def _home(t, extra, more_dot=False):
    ops = ''.join('<div style="display:flex;justify-content:space-between;padding:10px 0;border-top:1px solid %s;font-size:15px"><span>%s</span><span style="%s">%s</span></div>' % (t['line'], a, MONO, b) for a, b in [('Продукты', '−3 200'), ('Кафе', '−1 450'), ('Такси', '−900')])
    hero_card = '<div style="margin:0 16px 10px;height:96px;border-radius:24px;padding:16px;background:linear-gradient(135deg,%s,%s)"><div style="font-size:12px;color:%s">Всего</div><b style="font-size:28px;%s">412 800 ARS</b></div>' % (U.mix(t['page'], t['prim'], .35), U.mix(t['page'], t['acc'], .3), t['muted'], MONO)
    return U.phone(t, '<div style="position:absolute;inset:0;padding-top:30px"><div style="margin:0 16px;height:56px;display:flex;align-items:center;font-size:28px;font-weight:700">Обзор</div>%s%s%s</div>%s' % (hero_card, extra, X.card(t, ops, '4px 16px'), X.navbar(t, 0)), 844)


def home_drafts(t, v):
    strip = X.card(t, '<div style="display:flex;align-items:center;gap:10px"><span style="width:40px;height:40px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span><span style="flex:1"><b>Черновики · 5</b><div style="font-size:13px;color:%s">Из заметок и уведомлений · проверьте</div></span>%s</div>' % (U.mix(t['page'], t['acc'], .2), ic('draft', X.ink_acc(t), 20, 1.9), t['muted'], ic('chev', t['muted'], 20, 2)), '10px 14px')
    return _home(t, strip)


def more_drafts(t, v):
    cnt = '<span style="min-width:24px;height:24px;border-radius:999px;background:%s;color:%s;font-size:13px;font-weight:700;display:flex;align-items:center;justify-content:center;box-shadow:inset 0 0 0 1.5px %s">5</span>' % (t['prim'], t['onp'], A.edge(t))
    g = lambda rows: X.card(t, ''.join(rows), '4px 16px')
    sec = lambda s: '<div style="margin:12px 20px 4px;font-size:13px;color:%s">%s</div>' % (t['muted'], s)
    body = (sec('Деньги') + g([X.row(t, 'wallet', 'Счета'), X.row(t, 'draft', 'Черновики', 'заметки, уведомления, выписки', cnt), X.row(t, 'note', 'Быстрые заметки')])
            + sec('Приложение') + g([X.row(t, 'gear', 'Настройки')]))
    return U.phone(t, '<div style="position:absolute;inset:0;padding-top:30px"><div style="margin:0 16px;height:56px;display:flex;align-items:center;font-size:28px;font-weight:700">Ещё</div>%s</div>%s' % (body, X.navbar(t, 4)), 844)


def hints(t, v):
    def h(text):
        return ('<div style="margin:0 16px 10px;padding:12px 14px;border-radius:20px;background:%s;border:1px solid %s;display:flex;gap:10px;align-items:flex-start;font-size:14px;line-height:1.4">%s<span style="flex:1">%s</span>%s</div>'
                % (U.mix(t['page'], t['acc'], .1), t['line'], ic('spark', X.ink_acc(t), 18, 1.9), text, ic('close', t['muted'], 18, 2)))
    return X.screen(t, 'Подсказки ИИ', '<div style="margin:0 20px 8px;font-size:13px;color:%s">Один раз, по делу, без давления</div>' % t['muted']
                    + h('Есть микрофон? Скажите «кофе три пятьсот с карты» — операция заполнится сама.')
                    + h('Много мелких трат? Запишите их списком в «Быстрых заметках» и обработайте разом.')
                    + h('Не удалось прочитать чек? С «ИИ на телефоне» он прочитается сам: Ещё → Настройки.')
                    + h('Новые уведомления банков ждут в «Черновиках». Проверьте и запишите.'))


BOARDS = [('AiSetRow', set_row), ('AiPkgOff', pkg_off), ('AiPkgConfirm', pkg_confirm), ('AiPkgLoadA', pkg_load_a), ('AiPkgLoadB', pkg_load_b), ('AiPkgOn', pkg_on), ('AiPkgNano', pkg_nano), ('AiPkgWeak', pkg_weak),
          ('AiNotesA', notes_a), ('AiNotesRes', notes_res), ('AiNotesOne', notes_one), ('AiNotesLong', long_drafts),
          ('AiNotifPerm', notif_perm), ('AiNotifApps', notif_apps), ('AiDraftsList', drafts_list), ('AiHomeDrafts', home_drafts), ('AiMoreDrafts', more_drafts), ('AiHints', hints)]
