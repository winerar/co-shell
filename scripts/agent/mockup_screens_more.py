"""Generated boards: Ещё → rates, currencies, family, backup, update, feedback, wipe, Wallet import."""
from mockup_bricks import *  # noqa: F401,F403


def rate_rows(rows):
    return group([row('1 %s = %s USD' % (c, r), s, badge(c[:2], CHIP, INK, 36, 18), '') for c, r, s in rows])


RATES = [('ARS', '0,000977', 'на 11 нояб.'), ('AMD', '0,00258', 'на 11 нояб.'), ('RUB', '0,0123', 'на 11 нояб.'), ('USDT', '1,0004', 'на 11 нояб. · через USD')]


def refresh_block(state):
    if state == 'running':
        st = '<span style="display: flex; align-items: center; gap: 8px; color: %s; font-size: 14px;"><svg class="spin" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="%s" stroke-width="2.4" stroke-linecap="round" aria-hidden="true"><path d="M12 3a9 9 0 1 0 9 9"></path></svg>Загружаем курсы…</span>' % (INK, LIME)
        return card(st + progress(0.45, 10), pad='14px')
    if state == 'failed':
        return message('Не удалось загрузить курсы. Проверьте интернет и попробуйте ещё раз', 'error') + button('Повторить', 'outline', 'refresh')
    return ''


def rates(state):
    body = (note('Загружено: курсы на 11 нояб., источник fawazahmed0/currency-api', 0) + refresh_block(state)
            + label('Курсы на сегодня') + rate_rows(RATES))
    return inner_screen('Курсы валют', body)


def rates_empty():
    body = (message('Курсы ещё не загружались — нужен интернет', 'warn') + button('Обновить сейчас', 'outline', 'refresh')
            + empty('rate', 'Здесь появятся курсы валют ваших счетов и операций'))
    return inner_screen('Курсы валют', body)


def rates_manual():
    form = (field('Валюта', 'USDT', mono=True) + field('Сколько стоит в', 'ARS', mono=True) + field('Курс', '1 015', mono=True)
            + field('Действует с', '10 нояб. 2026', trail=svg('calendar', MUTED, 18)))
    return inner_screen('Курсы валют', label('Курсы на сегодня', top=4) + rate_rows(RATES)) + scrim() + dialog('Курс вручную', '', [('Отмена', 'text'), ('Сохранить', 'primary')], form)


def currency_add():
    sug = '<div style="display: flex; gap: 8px; margin-top: 12px; flex-wrap: wrap;">%s</div>' % ''.join(chip(c) for c in ['EUR', 'BRL', 'USDT', 'GEL'])
    under = inner_screen('Валюты отчётов', group([row('USD', 'Основная', '', ''), row('ARS', '', '', ''), row('AMD', '', '', '')]))
    return under + scrim() + dialog('Добавить валюту', '', [('Отмена', 'text'), ('Добавить', 'primary')], field('Код валюты', 'EU', mono=True) + sug)


# ---- family -----------------------------------------------------------------------------------------------------

def fam(body):
    return inner_screen('Семья и синхронизация', body)


def avatar(ch, color, ink):
    return badge(ch, color, ink, 40, 20)


def family_signed_out():
    return fam(hero('user', 'Общий бюджет на двоих', 'Войдите через Google, чтобы вести общий бюджет с семьёй. Данные хранятся на телефоне и в облаке Firebase, '
                    'приложение работает и без интернета. Доступ — только по приглашению.', VIOLET, top=24)
               + '<div style="margin-top: 28px;">' + button('Войти через Google', 'primary', 'google', 0) + note('Выберите свой Google-аккаунт', 8) + '</div>')


def family_no_access():
    return fam(message('Вы вошли как masha@gmail.com. Доступа к семье нет: попросите участника пригласить этот адрес в «Семья → Пригласить», затем нажмите «Проверить снова».', 'warn')
               + button('Проверить снова', 'primary', 'refresh') + button('Создать семью', 'outline', 'plus')
               + note('«Создать» — только для первого участника. Данные этого телефона станут общими.', 8)
               + label('Аккаунт') + group([row('Выйти', 'Данные останутся на телефоне, синхронизация остановится', svg('logout'))]))


def family_invited():
    return fam(hero('mail', 'Вас пригласили в семью', 'Вы вошли как anya@gmail.com. На телефон загрузится общий бюджет.', VIOLET, top=24)
               + '<div style="margin-top: 28px;">' + button('Присоединиться к семье', 'primary', None, 0) + button('Выйти', 'outline') + '</div>')


def family_join_mode():
    return family_invited() + scrim() + dialog('На телефоне есть свои операции', 'Заменить их общим бюджетом семьи (перед этим сохранится копия) или добавить их в семью? '
                                               'Если это одни и те же траты, выберите «Заменить» — иначе будут дубли.', [('Добавить в семью', 'text'), ('Заменить', 'primary')])


def family_offline():
    members = group([row('Вы', 'создатель семьи', avatar('М', VIOLET, '#1A0533')), row('Аня', 'anya@gmail.com', avatar('А', CYAN, '#002A2E')),
                     row('Пригласить', 'По адресу Google-аккаунта', '<span style="width: 40px; height: 40px; border-radius: 50%%; border: 1px dashed %s; display: flex; align-items: center; justify-content: center;">%s</span>' % (LINE2, svg('plus', MUTED, 18)), '', LIME)])
    return fam(message('Вы вошли как masha@gmail.com. Нет связи с сервером — проверьте интернет.', 'warn')
               + card('<div style="display: flex; align-items: center; gap: 8px; font-size: 13px;"><span style="width: 8px; height: 8px; border-radius: 50%%; background: %s;"></span>'
                      'Синхронизировано: сегодня 09:12 · ждут отправки: 3</div>' % WARN, pad='14px', extra='margin-top: 10px;')
               + label('Участники') + members)


def family_invite_dialog():
    return family_offline() + scrim() + dialog('Пригласить', 'Адрес Google-аккаунта, которым человек войдёт в приложение.', [('Отмена', 'text'), ('Пригласить', 'primary')],
                                               field('Email', 'anya@gmail', error='Похоже, адрес введён с ошибкой.'))


def family_invite_sent():
    members = group([row('Вы', 'создатель семьи', avatar('М', VIOLET, '#1A0533')),
                     row('anya@gmail.com', 'Приглашён — ждём входа', '<span style="width: 40px; height: 40px; border-radius: 50%%; border: 1px dashed %s; display: flex; align-items: center; justify-content: center;">%s</span>' % (LINE2, svg('mail', MUTED, 18)), svg('close', MUTED, 18))])
    return fam(message('Приглашение для anya@gmail.com готово. Пусть установит приложение и войдёт через Google этим адресом.', 'ok')
               + label('Участники') + members + button('Синхронизировать сейчас', 'violet', 'refresh', 16))


def family_not_configured():
    return fam(empty('cloud', 'Синхронизация в этой сборке не настроена — приложение работает только на этом телефоне.'))


# ---- backup / update / feedback / wipe ------------------------------------------------------------------------

def backup_actions():
    return group([row('Сохранить копию', 'Выберите папку или Google Диск', tile_icon('download')), row('Восстановить из копии', 'Записи из файла добавятся к текущим', tile_icon('upload', CYAN))])


def backup_auto(msg=''):
    auto = group([row('Автоматически раз в неделю', 'Папка: Google Диск / Co-Shell', '', switch(True)), row('Последняя копия: вчера, 03:10', 'хранятся последние 8', svg('check', OK))])
    return inner_screen('Резервная копия', msg + label('Вручную', top=12) + backup_actions() + label('Автокопия') + auto)


def backup_restore():
    return backup_auto() + scrim() + dialog('Восстановить из копии?', 'Записи из файла добавятся в приложение, а совпадающие с ними — заменятся версией из файла. Остальные данные останутся как есть.',
                                            [('Отмена', 'text'), ('Восстановить', 'primary')])


def whats_new():
    items = ''.join('<li style="margin-top: 8px;">%s</li>' % t for t in ['Операции: удержите строку — выбор нескольких', 'Вкладки листаются свайпом', 'Импорт из Wallet: кнопка «±» для долга'])
    under = inner_screen('Обзор', '')
    return under + scrim() + dialog('Что нового в 1.43', '', [('Понятно', 'primary')], '<ul style="margin: 8px 0 0; padding-left: 20px; font-size: 14px; line-height: 1.4; color: %s;">%s</ul>' % (INK2, items))


def feedback_sent():
    hist = group([row('Ошибка · сегодня 10:40', 'Не листается список счетов после импорта', svg('check', OK)), row('Идея · 8 нояб.', 'Добавить бюджет на месяц', svg('check', OK))])
    return inner_screen('Обратная связь', hero('check', 'Спасибо! Сообщение отправлено.', 'Сообщение станет задачей разработки в течение нескольких часов.', LIME, top=8)
                        + button('Написать ещё', 'outline', None, 20) + label('Отправленные') + hist)


def feedback_error():
    text = ('<div style="margin-top: 12px; min-height: 160px; padding: 12px 14px; border-radius: 12px; background: %s; border: 1px solid %s; font-size: 15px; line-height: 1.45;">'
            'После импорта Wallet список счетов не листается дальше Binance.</div><div style="text-align: right; font-size: 12px; color: %s; margin-top: 6px;">58 / 2000</div>' % (FIELD, LINE2, MUTED))
    return inner_screen('Обратная связь', segmented(['Ошибка', 'Идея', 'Дизайн', 'Другое'], 'Ошибка') + text
                        + message('Не удалось отправить. Проверьте интернет и попробуйте ещё раз — текст сохранён.', 'error')
                        + note('Приложим: версия 1.43 · Pixel 8 · Android 15 · экран «Счета»') + button('Отправить', 'primary', 'upload'))


def wipe_failed():
    counts = ''.join('<div style="padding: 12px; border-radius: 14px; background: %s;"><div style="%s font-size: 20px; font-weight: 700;">%s</div><div style="font-size: 12px; color: %s;">%s</div></div>' % (CHIP, MONO, n, MUTED, t)
                     for n, t in [('12', 'счетов'), ('1 284', 'операций'), ('31', 'категорий'), ('2', 'ручных курсов')])
    return inner_screen('Стереть все данные', message('Удалится у всей семьи — на обоих телефонах, при следующей синхронизации', 'warn')
                        + '<div style="margin-top: 12px; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px;">%s</div>' % counts
                        + message('Не удалось сохранить копию — ничего не стёрто. Проверьте место на телефоне.', 'error')
                        + field('Слово', 'СТЕРЕТЬ', mono=True) + button('Стереть у всей семьи', 'danger', 'trash'))


# ---- Wallet import ----------------------------------------------------------------------------------------------

STEPS = ['Файл', 'Что в файле', 'Счета', 'Категории', 'Переводы без пары', 'Проверка', 'Готово']


def wizard(step, body, primary='Далее', back=True):
    n = len(STEPS)
    bars = ''.join('<span style="flex: 1; height: 4px; border-radius: 2px; background: %s;"></span>' % (LIME if i <= step else LINE2) for i in range(n))
    head = ('<div style="display: flex; gap: 4px; margin-top: 2px;">%s</div><div style="margin: 10px 4px 0; font-size: 13px; color: %s;">Шаг %d из %d · <b style="color: %s;">%s</b></div>'
            % (bars, MUTED, step + 1, n, INK, STEPS[step]))
    foot = ''
    if primary:
        foot = ('<div style="position: absolute; left: 16px; right: 16px; bottom: 24px; display: flex; gap: 8px;">%s<span style="flex: 1;">%s</span></div>'
                % ('<span style="width: 54px; height: 54px; border-radius: 50%%; border: 1px solid %s; display: flex; align-items: center; justify-content: center;">%s</span>' % (LINE2, svg('back', INK)) if back else '',
                   button(primary, mt=0)))
    return inner_screen('Импорт из Wallet', head + body) + foot


def import_start():
    past = group([row('wallet-2026-11.csv · операций: 1 284', 'с 3 янв. 2024 по 9 нояб. 2026', svg('file'), '<span style="color: %s; font-size: 13px; font-weight: 600;">Отменить</span>' % PINK)])
    return wizard(0, note('В Wallet: Настройки → Экспорт → CSV. Сохраните файл на телефон и выберите его здесь. Перед записью приложение само сделает резервную копию, а импорт можно отменить целиком.')
                  + button('Выбрать файл CSV', 'primary', 'file', 16) + note('Повторный импорт того же или более нового файла добавит только новое', 8)
                  + label('Прошлые импорты') + past, None)


def import_error():
    return wizard(0, message('Это не похоже на выгрузку Wallet: нет нужных колонок или строк.', 'error') + button('Выбрать другой файл', 'primary', 'file'), None)


def import_summary():
    facts = group([row(t, '', svg(i), '') for t, i in [('Записей: 1 312', 'list'), ('с 3 янв. 2024 по 10 нояб. 2026', 'calendar'), ('Счетов: 9 · валюты: ARS, AMD, USD, USDT', 'wallet'),
                                                       ('Категорий: 27', 'folder'), ('Переводов: 140, без пары: 3', 'transfer')]])
    errs = message('Строка 418: неверное значение «12,3.4» в колонке amount<br>Строка 977: незакрытая кавычка', 'warn', 'Не прочитано строк: 2 — они не попадут в импорт')
    return wizard(1, label('Что в файле', top=14) + facts + errs)


def import_accounts():
    def acc(name, rec, target, bal='', err=''):
        f = field('Баланс в Wallet сейчас', bal or '—', hint='Необязательно: разница уйдёт в начальный баланс', error=err, mono=True,
                  trail='<span style="width: 36px; height: 36px; border-radius: 10px; background: %s; display: flex; align-items: center; justify-content: center; %s">±</span>' % (CHIP, MONO))
        return card('<div style="display: flex; justify-content: space-between;"><b>%s</b><span style="font-size: 12px; color: %s;">%s</span></div>'
                    '<div style="margin-top: 10px;">%s</div>%s' % (name, MUTED, rec, chip(target + ' ▾', True), f), pad='14px', extra='margin-top: 10px;')
    return wizard(2, note('Каждый счёт Wallet можно перенести в новый счёт или слить с существующим той же валюты.') + acc('Galicia', 'Записей: 640 · ARS', '→ Galicia (есть)', '1 250 000')
                  + acc('Visa кредитка', 'Записей: 88 · USD', 'Новый счёт', '−420,5', 'Не похоже на сумму'))


def import_categories():
    rows = [row('Еда', 'Расход · записей: 512', badge('Е', LIME, ONLIME, 36, 18), chip('→ Продукты', True)),
            row('Restaurants', 'Расход · записей: 96', badge('R', WARN, '#2A1100', 36, 18), chip('Новая: Restaurants')),
            row('Salary', 'Доход · записей: 34', badge('S', CYAN, '#002A2E', 36, 18), chip('→ Зарплата', True))]
    return wizard(3, note('Нажмите на категорию, чтобы выбрать существующую вместо новой.') + '<div style="margin-top: 12px;">%s</div>' % group(rows))


def import_transfers():
    def one(t, a, opts):
        return card('<div style="display: flex; justify-content: space-between;"><span>%s</span>%s</div><div style="margin-top: 10px; display: flex; gap: 8px; flex-wrap: wrap;">%s</div>'
                    % (t, amount(a, '', INK, 14), opts), pad='14px', extra='margin-top: 10px;')
    return wizard(4, note('У этих переводов в файле нет второй половины. Обычно это выравнивание баланса в Wallet.')
                  + one('Galicia · 4 мар. 2025', '−12 000 ARS', chip('Корректировка', True) + chip('Пропустить') + chip('Перевод на счёт…'))
                  + one('Наличные · 9 июн. 2025', '+200 USD', chip('Корректировка') + chip('Пропустить') + chip('Перевод: Ameria', True)))


def import_preview():
    stats = group([row('Будет добавлено операций: 1 287', '', svg('plus', LIME)), row('Уже загружено раньше: 20 — пропускаются', '', svg('check', MUTED)),
                   row('Пропущено по вашему выбору: 1', '', svg('close', MUTED)), row('Требуют решения: 2 — не попадут в импорт', 'Строка 501: валюта не совпадает со счётом, курса нет', svg('alert', WARN))])
    bal = group([row('Galicia', '', badge('GA', '#F26E22', '#1A0A00'), amount('1 250 000', 'ARS')), row('Visa кредитка', 'новый', badge('VI', '#1A1F71', '#FFFFFF'), amount('−420,50', 'USD', PINK))])
    return wizard(5, label('Проверка', top=14) + stats + note('Новых счетов: 1, новых категорий: 4') + label('Балансы после импорта') + bal, 'Импортировать')


def import_done():
    return wizard(6, hero('check', 'Импорт завершён', 'Добавлено операций: 1 287. Перед этим сделана резервная копия.', LIME, True, 60), None) + \
        '<div style="position: absolute; left: 16px; right: 16px; bottom: 24px;">%s%s</div>' % (button('Готово', mt=0), button('Отменить импорт', 'text', None, 4))


BOARDS = [
    ('RatesRefreshing', '11 · Курсы: загружаем', lambda: rates('running')), ('RatesFailed', '11 · Курсы: не загрузились', lambda: rates('failed')),
    ('RatesEmpty', '11 · Курсы: ещё не загружались', rates_empty), ('RatesManual', '11 · Курсы: курс вручную', rates_manual),
    ('CurrencyAdd', '12 · Валюты отчётов: добавить', currency_add),
    ('FamilySignedOut', '13 · Семья: вход', family_signed_out), ('FamilyNoAccess', '13 · Семья: нет доступа', family_no_access),
    ('FamilyInvited', '13 · Семья: приглашение', family_invited), ('FamilyJoinMode', '13 · Семья: свои операции на телефоне', family_join_mode),
    ('FamilyInviteSent', '13 · Семья: приглашение отправлено', family_invite_sent), ('FamilyInviteDialog', '13 · Семья: пригласить, ошибка адреса', family_invite_dialog),
    ('FamilyOffline', '13 · Семья: нет связи', family_offline), ('FamilyNotConfigured', '13 · Семья: не настроено', family_not_configured),
    ('BackupAuto', '14 · Копия: автокопия, сохранено', lambda: backup_auto(message('Копия сохранена.', 'ok'))),
    ('BackupError', '14 · Копия: ошибка файла', lambda: backup_auto(message('Это не файл резервной копии Co-Shell.', 'error'))),
    ('BackupRestore', '14 · Копия: восстановить?', backup_restore),
    ('UpdateWhatsNew', '16 · «Что нового» после установки', whats_new),
    ('FeedbackSent', '5б · Отзыв: отправлено', feedback_sent), ('FeedbackError', '5б · Отзыв: не отправилось', feedback_error),
    ('WipeFailed', '18 · Стереть: копия не сохранилась', wipe_failed),
    ('ImportStart', '20 · Импорт: файл', import_start), ('ImportError', '20 · Импорт: не тот файл', import_error),
    ('ImportSummary', '20 · Импорт: что в файле', import_summary), ('ImportAccounts', '20 · Импорт: счета', import_accounts),
    ('ImportCategories', '20 · Импорт: категории', import_categories), ('ImportTransfers', '20 · Импорт: переводы без пары', import_transfers),
    ('ImportPreview', '20 · Импорт: проверка', import_preview), ('ImportDone', '20 · Импорт: готово', import_done),
]
