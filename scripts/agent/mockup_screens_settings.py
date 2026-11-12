"""Boards from «Ещё»: rates, report currencies, family, backup (static); wipe, update, feedback (interactive)."""
from mockup_parts import *  # noqa: F401,F403
from mockup_logic import flip, go, per, proto, toggled, when




# ---- rates -------------------------------------------------------------------------------------------------------

RATES = [('USD', '1 USD = 1 185,00 ARS', 'вручную', ''), ('EUR', '1 EUR = 1,08 USD', 'на 9 ноября', '+0,4%'), ('AMD', '1 USD = 387,20 AMD', 'на 9 ноября', '−0,1%'),
         ('RUB', '1 USD = 81,40 RUB', 'на 9 ноября', '+0,2%'), ('USDT', '1 USDT = 1,00 USD', 'через USD', '')]


def code_badge(code):
    return ('<span style="width: 44px; height: 36px; flex: none; border-radius: 10px; background: %s; color: %s; display: flex; align-items: center; justify-content: center; '
            '%s font-size: 11px; font-weight: 700;">%s</span>' % (CHIP, WARN, MONO, code))


def rates():
    head = hero_card('<div style="display: flex; align-items: center; gap: 12px;">%s<span style="flex-grow: 1;"><div style="font-size: 15px; font-weight: 600;">Курсы на сегодня</div>'
                     '<div style="font-size: 12px; color: %s;">Загружено 9 ноября · currency-api</div></span>%s</div>' % (dot_icon('check', CYAN, 40), HERO_HINT, icon_button('refresh', 'Обновить', INK, True)),
                     pad='12px 12px 12px 14px', radius=20)
    lbl = ('<div style="display: flex; margin: 18px 4px 8px; font-size: 13px;"><span style="flex-grow: 1; font-weight: 600; color: %s;">Валюты ваших счетов</span>'
           '<span style="color: %s;">тап — задать вручную</span></div>' % (MUTED, MUTED))
    rows = group([row('<span style="%s">%s</span>' % (MONO, r), s, code_badge(c), amount(d, '', OK if d.startswith('+') else PINK, 12) if d else '') for c, r, s, d in RATES])
    manual = ('<div style="display: flex; gap: 12px; padding: 12px 14px; border-radius: 16px; background: %s; border: 1px solid %s;"><span style="width: 4px; border-radius: 2px; background: %s;"></span>'
              '<span><div style="%s font-size: 15px;">1 USD = 1 185,00 ARS</div><div style="font-size: 12px; color: %s;">вручную · с 1 ноября · важнее загруженного</div></span></div>'
              % (VIOLETBG, '#3A2257', VIOLET, MONO, VIOLETINK))
    foot = '<div style="position: absolute; left: 20px; right: 20px; bottom: 20px;">%s</div>' % note('Источники: fawazahmed0/currency-api и ExchangeRate-API. Курсы обновляются сами раз в день.', 0)
    return board('Курсы валют', inner_screen('Курсы валют', head + lbl + rows + label('Ручные курсы') + manual + dashed('Добавить курс')) + foot)


# ---- report currencies (tap a row — it becomes the main one) -------------------------------------------------------

CURS = [('usd', 'USD', 'доллар США', '3 412,80', '4 044 168,00 ARS · 277 802,00 RUB'), ('ars', 'ARS', 'аргентинское песо', '4 044 168', '3 412,80 USD · 277 802,00 RUB'),
        ('rub', 'RUB', 'российский рубль', '277 802', '3 412,80 USD · 4 044 168,00 ARS')]


def report_currencies():
    def screen(main):
        k, code, _, total, sub = next(c for c in CURS if c[0] == main)
        hero = hero_card('<div style="font-size: 13px;">Общий баланс · так будет на «Обзоре»</div><div style="margin-top: 8px; %s font-size: 30px; font-weight: 700; color: %s;">%s %s</div>'
                         '<div style="margin-top: 4px; %s font-size: 12px; color: %s;">%s</div>' % (MONO, LIME, total, code, MONO, HERO_HINT, sub), pad='16px 18px')
        rows = ''
        for key, c, name, _, _ in CURS:
            on = key == main
            r = row('<span style="%s font-size: 17px; font-weight: 700;">%s</span><span style="margin-left: 16px; font-size: 13px; color: %s;">%s</span>' % (MONO, c, MUTED, name),
                    lead=svg('list', MUTED, 18), trail=pill('Основная') if on else '', min_h=60)
            rows += '<div style="margin-top: 10px;">%s</div>' % go('main', key, card(r, extra='border-color: %s;' % LIME if on else ''))
        return hero + '<div style="margin-top: 6px;">%s</div>' % rows
    body = note('Общий баланс на «Обзоре» показывается в каждой из этих валют. Тап — сделать основной.', 0) + '<div style="margin-top: 14px;"></div>' + per('main', [c[0] for c in CURS], screen) + dashed('Добавить валюту')
    return proto('Валюты отчётов', inner_screen('Валюты отчётов', body), main='usd')


# ---- family / backup ------------------------------------------------------------------------------------------------

def family():
    pair = ('<span style="display: flex;">%s<span style="margin-left: -12px; border-radius: 50%%; box-shadow: 0 0 0 3px %s;">%s</span></span>'
            % (badge('М', VIOLET, '#1A0533', 52, 26), '#314617', badge('А', CYAN, '#002A2E', 52, 26)))
    hero = hero_card('<div style="display: flex; align-items: center; gap: 14px;">%s<span><div style="font-size: 20px; font-weight: 700;">Общий бюджет</div>'
                '<div style="font-size: 13px; color: %s;">2 участника</div></span></div><div style="display: flex; align-items: center; gap: 8px; margin-top: 14px; font-size: 13px;">'
                '<span style="width: 8px; height: 8px; border-radius: 50%%; background: %s;"></span><span style="flex-grow: 1;">Синхронизировано: 2 мин назад</span>%s</div>'
                % (pair, HERO_HINT, OK, amount('за 2–3 с', '', HERO_HINT, 12)), pad='16px', radius=20)
    invite = '<span style="width: 40px; height: 40px; border-radius: 50%%; border: 1px dashed %s; display: flex; align-items: center; justify-content: center;">%s</span>' % (LINE2, svg('plus', MUTED, 18))
    members = group([row('Вы', 'создатель семьи', badge('М', VIOLET, '#1A0533', 40, 20)), row('Второй участник', 'вошёл · 12 операций за неделю', badge('А', CYAN, '#002A2E', 40, 20)),
                     row('Пригласить', 'по адресу Google-аккаунта', invite, '', LIME)])
    body = hero + button('Синхронизировать сейчас', 'violet', 'refresh') + label('Участники') + members + label('Аккаунт') + group([row('Выйти', 'Данные останутся, синхронизация остановится', svg('logout'))])
    return board('Семья', inner_screen('Семья', body))


def backup():
    hero = ('<div style="margin-top: 8px;">%s</div><h2 style="margin: 18px 0 0; text-align: center; font-size: 22px;">Автокопия включена</h2>'
            '<div style="text-align: center; font-size: 13px; color: %s; margin-top: 4px;">Последняя: 3 ноября · раз в неделю</div>'
            % (ring(0.72, '<span style="%s font-size: 40px; font-weight: 700;">2</span><span style="font-size: 12px; color: %s;">дня до копии</span>' % (MONO, MUTED), YELLOW), MUTED))
    auto = group([row('Папка: Google Диск / Co-Shell', 'тап — выбрать другую', tile_icon('folder', YELLOW, YELLOWBG), chevron()), row('Автоматически раз в неделю', '', '', switch(True))])
    manual = grid([card('%s<div style="margin-top: 14px; font-size: 15px; font-weight: 600;">%s</div><div style="font-size: 12px; color: %s;">%s</div>' % (svg(i, c), t, MUTED, s), pad='14px')
                   for i, c, t, s in [('download', LIME, 'Сохранить копию', 'в папку или на Диск'), ('upload', CYAN, 'Восстановить', 'записи добавятся к текущим')]], 2)
    body = hero + '<div style="margin-top: 16px;">%s</div>' % auto + label('Вручную') + manual + note('Копия — один файл со всеми счетами, категориями, операциями и ручными курсами.')
    return board('Резервная копия', inner_screen('Резервная копия', body))


# ---- wipe: settings → confirm → done --------------------------------------------------------------------------------

def wipe():
    rest = group([row('Валюты отчётов', 'Основная: USD', tile_icon('rate'), chevron()), row('Обновление приложения', 'Версия 43 — последняя', tile_icon('download'), chevron())])
    danger = go('st', 'confirm', group([row('Стереть все данные', 'Чтобы загрузить Wallet на чистую. Сначала — копия', tile_icon('trash', PINK, PINKBG), chevron(), PINK)]))
    settings = inner_screen('Настройки', label('Общее', top=4) + rest + label('Данные') + danger + note('Тапните строку — откроется подтверждение.'))
    counts = count_grid([('12', 'счетов'), ('3 418', 'операций'), ('46', 'категорий'), ('8', 'ручных курсов')])
    word = flip('word', toggled('word', field('Чтобы подтвердить, введите', 'СТЕРЕТЬ', mono=True), field('Чтобы подтвердить, введите', 'СТЕРЕТЬ', mono=True, placeholder=True, hint='Демо: тапните поле — слово впишется')))
    confirm = (go('st', 'settings', top_bar('Стереть все данные')) + '<div style="padding: 0 16px;">'
               + message('На обоих телефонах, при следующей синхронизации', 'warn', 'Удалится у всей семьи') + '<div style="margin-top: 12px;">%s</div>' % counts
               + note('Останутся: семья и участники, настройки, тема. Категории по умолчанию появятся заново.')
               + message('Сначала сохраним копию co-shell-2026-11-10.json в «Загрузки». Вернуть — «Ещё → Резервная копия».', 'info') + word + '</div>'
               + footer(toggled('word', go('st', 'done', button('Стереть у всей семьи', 'danger', 'trash', 0)), button('Стереть у всей семьи', 'danger', 'trash', 0, True))))
    done = ('<div style="padding: 0 16px;">' + hero('check', 'Данные стёрты', 'Копия: co-shell-2026-11-10.json в «Загрузках». Второй телефон очистится при следующей синхронизации.', LIME, True, 120)
            + '</div>' + footer(button('Импорт из Wallet', 'outline', 'file', 0), go('st', 'settings', button('Готово'))))
    return proto('Стереть все данные', when('is_st_settings', settings) + when('is_st_confirm', confirm) + when('is_st_done', done), st='settings')


# ---- update: available → downloading → ready → latest ---------------------------------------------------------------

def update():
    news = card('<div style="display: flex; font-size: 13px;"><b style="flex-grow: 1; color: %s;">Что нового в версии 42</b><span style="color: %s;">10 ноября</span></div>'
                '<ul style="margin: 8px 0 0; padding-left: 18px; font-size: 13px; line-height: 1.45;">%s</ul>'
                % (LIME, MUTED, ''.join('<li>%s</li>' % t for t in ['Новая шторка ввода: сначала счёт, своя клавиатура', 'Счета группами, поиск по счетам', 'Цвета банков и приложений для счетов'])),
                pad='14px', extra='margin-top: 20px;')
    meta = '<div style="display: flex; justify-content: space-between; margin: 12px 4px 0; font-size: 12px; color: %s;"><span>Установлена: 41 · 9 ноября</span><span>Проверка: только что</span></div>' % MUTED
    icon = '<span style="width: 84px; height: 84px; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
    states = {
        'avail': (1, icon % (LIME, svg('download', ONLIME, 40, 2.2)), 'Доступна версия 42', '8,1 МБ · вышла сегодня', button('Скачать и установить', mt=0), 'load'),
        'load': (0.45, '<span style="%s font-size: 30px; font-weight: 700;">45 %%</span><span style="font-size: 12px; color: %s;">3,6 из 8,1 МБ</span>' % (MONO, MUTED),
                 'Скачиваем…', 'Можно выйти — продолжится в фоне. Тапните кольцо — загрузится', button('Отменить', 'outline', None, 0), 'avail'),
        'ready': (1, icon % (LIME, svg('check', ONLIME, 40, 2.4)), 'Готово к установке', 'Версия 42 · 8,1 МБ', button('Установить', mt=0), 'latest'),
        'latest': (0, icon % (CHIP, svg('check', LIME, 40, 2.4)), 'У вас последняя версия', 'Версия 42 установлена', button('Проверить ещё раз', 'outline', 'refresh', 0), 'avail'),
    }

    def screen(k):
        frac, mid, title, sub, btn, nxt = states[k]
        r = ring(frac, mid)
        r = go('st', 'ready', r) if k == 'load' else r
        return ('<div style="padding: 16px 16px 0;">%s<h2 style="margin: 22px 0 0; text-align: center; font-size: 22px;">%s</h2><div style="text-align: center; margin-top: 4px; font-size: 13px; color: %s;">%s</div>%s%s</div>'
                % (r, title, MUTED, sub, news, meta) + footer(go('st', nxt, btn)))
    return proto('Обновление', top_bar('Обновление') + per('st', list(states), screen), st='avail')


# ---- feedback: kind → text → sent -----------------------------------------------------------------------------------

KINDS = [('bug', 'Ошибка', 'Что случилось и где?', 'Например: на «Обзоре» итог не обновился после перевода'), ('idea', 'Идея', 'Что добавить или упростить?', 'Например: итог дня в списке операций'),
         ('design', 'Дизайн', 'Что выглядит не так?', 'Например: мелкий шрифт на «Курсах»'), ('other', 'Другое', 'Напишите как удобно', 'Любой вопрос или мысль')]


def feedback():
    hist = group([row('Хочу видеть итог дня в списке операций', '9 ноября, 14:20', pill('Идея', 'violet')), row('Шторка дёргается при открытии клавиатуры', '8 ноября, 22:05', pill('Ошибка', 'pink'))])

    def form(k):
        _, name, prompt, ph = next(x for x in KINDS if x[0] == k)
        chips = '<div style="display: flex; gap: 8px;">%s</div>' % ''.join(go('k', key, pill(n) if key == k else chip(n)).replace('height: 24px', 'height: 36px').replace('padding: 0 10px', 'padding: 0 16px')
                                                                           for key, n, _, _ in KINDS)
        area = flip('typed', toggled('typed', textarea(ph, 58, 'После импорта Wallet список счетов не листается дальше Binance.'), textarea(ph, 0)))
        return chips + card('<div style="font-size: 13px; font-weight: 600; color: %s;">%s</div>%s' % (LIME, prompt, area), pad='14px', extra='margin-top: 14px;')
    write = (per('k', [x[0] for x in KINDS], form) + note('Приложим: версия 41 · Pixel 8 · Android 16 · экран «Обзор»'))
    sent = (message('Сообщение попадёт в задачи разработки в течение нескольких часов.', 'ok', 'Отправлено') + go('st', 'write', button('Написать ещё', 'outline')))
    send = footer(toggled('typed', go('st', 'sent', button('Отправить', 'primary', 'upload', 0)), button('Отправить', 'primary', 'upload', 0, True)))
    body = when('is_st_write', write) + when('is_st_sent', sent) + label('Отправленные', True) + hist
    return proto('Отзыв: написать', inner_screen('Обратная связь', body) + when('is_st_write', send), st='write', k='bug')


BOARDS = [('Rates', rates), ('ReportCurrencies', report_currencies), ('Family', family), ('Backup', backup), ('Wipe', wipe), ('Update', update), ('Feedback', feedback)]
