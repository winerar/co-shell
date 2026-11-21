#!/usr/bin/env python3
"""Regenerate the «Воркфлоу» board (docs/design/mockups/Flows.dc.html) from the ROWS table below.

Drawn with mockup_bricks (board, colours) like every other board, so it follows the canvas theme too.
Usage: scripts/agent/gen-flows-board.py [out.html] — gen-mockups.py runs it and places the board.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_bricks import BG, BAR, CARD, INK, MUTED, MUTED2, LIME, ONLIME, LIMEPILL, LINE, WARNLINE, board  # noqa: E402

# kind: p — step without a board, l — opens a board (href), g — lime («+» and its answer), n — no board yet (dashed)
ROWS = [
    ("Вкладки", "Панель внизу; на внутренних экранах уезжает. Вкладки листаются свайпом (#112)", False, [
        ("Обзор", "баланс, плитки, последние", "Home.dc.html", "l"),
        ("Операции", "поиск, фильтры", "Operations.dc.html", "l"),
        ("«+»", "новая операция", "Main.dc.html", "g"),
        ("Отчёты", "куда ушли деньги", "RepA.dc.html", "l"),
        ("Ещё", "плитки разделов, «Счета» — первая", "More.dc.html", "l"),
    ]),
    ("Новая операция", "Одна шторка, всё под большим пальцем; встряхнуть телефон — тоже", True, [
        ("«+» на панели", "или встряхнуть на вкладке", None, "g"),
        ("Тип и счёт", "тип, ☰ «Шаблоны», лента значков счетов, «Все ›» — полный список", "Main.dc.html", "l"),
        ("Сумма и категория", "карточки и клавиатура на том же экране", "Main.dc.html", "l"),
        ("Чек в др. валюте", "по желанию: ARS → списано USDT", "EntryReceipt.dc.html", "l"),
        ("«Готово»", "на клавиатуре; на доске — тосты", "Main.dc.html", "l"),
        ("«+» → ✓", "тёплая вспышка", None, "g"),
    ]),
    ("QR чека", "Камера в шторке или ссылка из QR чека открывают шторку: сумма и день уже стоят (#526)", True, [
        ("Сканер QR", "значок камеры в шторке; разрешение при первом открытии; «Ввести вручную» — пустая шторка", "Qr526Scan.dc.html", "l"),
        ("Ссылка afip.gob.ar", "камера или сканер → «Открыть в co-shell»; читается в приложении", None, "g"),
        ("Шторка с суммой", "«Из QR чека · продавец …»; счёт в другой валюте — «Сумма покупки»", "Qr526Filled.dc.html", "l"),
        ("Чек уже записан", "окно: «Отмена» закрывает шторку, «Всё равно записать» оставляет", "Qr526Dup.dc.html", "l"),
        ("«Готово»", "операция помнит чек (можно сканировать снова после удаления)", None, "g"),
    ]),
    ("Чек через «Поделиться»", "PDF, фото QR-кода, ссылка или текст из другого приложения → шторка (#561)", True, [
        ("«Поделиться» → co-shell", "PDF из Telegram, скриншот с QR, текст; читается в приложении", None, "g"),
        ("Шторка с суммой", "«Из чека · Teleshka»; покупки — в «Заметке»", "Rc561Shared.dc.html", "l"),
        ("Формат не знаем", "окно: «Ввести вручную» — пустая шторка, «Отправить образец» — отзыв", "Rc561Unread.dc.html", "l"),
        ("Отзыв «Формат чека»", "файл уже приложен; «Отправить»", "Rc561Form.dc.html", "l"),
    ]),
    ("Удаление", "Ничего не теряется сразу. Строки без свайпов (#112, 19В)", True, [
        ("Операции", "удержать строку", "Operations.dc.html", "l"),
        ("Выбор нескольких", "тапами по строкам", "OpsSelect.dc.html", "l"),
        ("«Удалить · N»", "панель вместо навигации", "Operations.dc.html", "l"),
        ("Снэкбар", "«Отменить», полоска времени", "OpsUndo.dc.html", "l"),
    ]),
    ("Дубль", "Повтор покупки: из шторки или пачкой", True, [
        ("Операции", "тап по строке", "Operations.dc.html", "l"),
        ("Шторка операции", "«Дублировать» вверху", "EntryEdit.dc.html", "l"),
        ("Копия · сегодня", "та же сумма и категория", "Operations.dc.html", "l"),
        ("Сохранить", "", None, "p"),
    ]),
    ("Без категории", "Плитка «Внимание» ведёт к тому, что надо поправить", True, [
        ("Обзор", "плитка «Без категории»", "Home.dc.html", "l"),
        ("Операции", "фильтр «Без категории · Этот месяц»", "Operations.dc.html", "l"),
        ("Тап по строке", "шторка операции", "EntryEdit.dc.html", "l"),
        ("Категория", "сохранить", None, "p"),
        ("Чип ✕", "снять фильтр", None, "p"),
    ]),
    ("Курсы", "Нет курса — подсказка на «Обзоре»", True, [
        ("Обзор", "«Внимание»: нет курса", "HomeAttention.dc.html", "l"),
        ("Курсы валют", "загружены раз в день", "Rates.dc.html", "l"),
        ("Тап по валюте", "или «+ Добавить курс»", "Rates.dc.html", "l"),
        ("Ручной курс", "важнее загруженного", "RatesManual.dc.html", "l"),
        ("Валюты отчётов", "тап — основная на «Обзоре»", "ReportCurrencies.dc.html", "l"),
    ]),
    ("Первые шаги", "Новичку: строка на «Обзоре» и мягкие подсказки (#513), всё можно пропустить", True, [
        ("Обзор", "строка «Первые шаги»", "Hint513Home0.dc.html", "l"),
        ("Шторка шагов", "галочки по данным; «Скрыть шаги»", "Hint513Steps.dc.html", "l"),
        ("Подсказки", "слово под шапкой «✕», жест «Понятно», одна в день", "Hint513Word.dc.html", "l"),
        ("Тёплая плашка", "первая запись, первый лимит — один раз", "Hint513Moment.dc.html", "l"),
        ("Ещё → Приложение", "«Подсказки» выкл. / «Показать заново»", "Hint513Settings.dc.html", "l"),
    ]),
    ("Подсказки на шторках", "Слова на шторках (#513, опрос 2026-11-19): шторка ввода — карточка над ней, «Теги» и «Валюта покупки» — строка под заголовком", True, [
        ("Шторка ввода", "карточка «Понятно» в полоске над шторкой, форму не закрывает", "Hint513EntryCoach.dc.html", "l"),
        ("Теги", "строка под заголовком, «✕»", "Hint513Tags.dc.html", "l"),
        ("Валюта покупки", "строка под заголовком, «✕»", "Hint513Currency.dc.html", "l"),
    ]),
    ("Конфиденциальность", "PIN, отпечаток и скрытие сумм (#145, опрос 2026-11-20 — все А); всё выключено, пока не включите PIN", True, [
        ("Ещё → Конфиденциальность", "Вход · Суммы · Экран", "Priv145Settings.dc.html", "l"),
        ("Придумайте PIN", "4 цифры, затем «Повторите PIN»", "Priv145Set1.dc.html", "l"),
        ("Вход", "PIN своей клавиатурой; отпечаток — окно системы", "Priv145LockA.dc.html", "l"),
        ("Забыли PIN?", "блокировка телефона → новый PIN, данные не тронуты", "Priv145Forgot.dc.html", "l"),
        ("«Глаз» на «Обзоре»", "все суммы «•••• USD»; экраном вниз — то же", "Priv145HideA.dc.html", "l"),
    ]),
    ("Карточки-герои", "Нажатие переворачивает карточку вокруг горизонтальной оси (#519): оборот «Обзора» и «Бюджетов»", True, [
        ("Обзор", "лицо: итог; нажатие или «↻» — оборот", "Flip519HomeFront.dc.html", "l"),
        ("Оборот «Обзора»", "итоги месяца, «в день», суммы по валютам; снова нажатие — назад", "Flip519HomeA.dc.html", "l"),
        ("Бюджеты", "лицо: «осталось», темп, прогноз", "Flip519BudFront.dc.html", "l"),
        ("Оборот «Бюджетов»", "«можно в день» и три лимита ближе всего к концу", "Flip519BudA.dc.html", "l"),
    ]),
    ("Конвертер", "Сколько это в других валютах; пара или все валюты, запоминает выбор", True, [
        ("Обзор", "карточка «Конвертер» (или «Ещё → Деньги», «Курсы»)", "Home.dc.html", "l"),
        ("Шторка «Пара»", "набрать сумму; тап по другой стороне — считать из неё", "Conv2PairType.dc.html", "l"),
        ("Кнопка в шапке", "переключает на «Все валюты» и обратно", "Conv2AllType.dc.html", "l"),
        ("Тап по строке", "считать из этой валюты", None, "p"),
    ]),
    ("Счёт", "Цвет — от банка или из палитры", True, [
        ("Счета", "«+» или тап по счёту", "Accounts.dc.html", "l"),
        ("Редактор счёта", "тип, валюта, баланс", "AccountEdit.dc.html", "l"),
        ("Цвет", "«Похоже на банк» → Взять", "Colors.dc.html", "l"),
        ("Исправить баланс", "«Сохранить баланс» — сразу", "AccountAdjust.dc.html", "l"),
        ("Баланс исправлен", "плашка, шторка счёта открыта", "AccountAdjusted.dc.html", "l"),
        ("Сохранить", "", None, "p"),
    ]),
    ("Категории", "Один «+» для категорий и папок", True, [
        ("Ещё", "Категории", "More.dc.html", "l"),
        ("Категории", "Расходы | Доходы", "Categories.dc.html", "l"),
        ("«+ Добавить»", "одна кнопка", "Categories.dc.html", "l"),
        ("Категория | Папка", "переключатель в редакторе", "CategoryEdit.dc.html", "l"),
        ("Сохранить", "", None, "p"),
    ]),
    ("Обновление", "Без диалога при запуске", True, [
        ("Вышла версия", "проверка раз в 15 мин", None, "p"),
        ("Пилюля", "над панелью + точка на «Ещё»", "UpdatePill.dc.html", "l"),
        ("Экран обновления", "скачивание, проценты", "Update.dc.html", "l"),
        ("Установка APK", "системный диалог", None, "p"),
    ]),
    ("Семья", "Общий бюджет на двоих", True, [
        ("Ещё → Семья", "", "Family.dc.html", "l"),
        ("Вход Google", "", "FamilySignedOut.dc.html", "l"),
        ("Создать семью", "", None, "p"),
        ("Пригласить", "по адресу Google", "Family.dc.html", "l"),
        ("Второй телефон", "«Присоединиться»", "FamilyInvited.dc.html", "l"),
        ("Синхронизация", "за 2–3 с; запасной опрос — раз в минуту", "Family.dc.html", "l"),
    ]),
    ("Ошибка синка", "Причина словами, не тревожим из-за минутного сбоя", True, [
        ("Синк не прошёл", "сеть/сервер — 2 мин, вход/доступ — сразу", None, "p"),
        ("Плашка", "над балансом «Обзора»", "HomeSync.dc.html", "l"),
        ("Повторить", "или «Войти» → Семья", "HomeSync.dc.html", "l"),
        ("Отзыв", "форма с кодом ошибки", "Feedback.dc.html", "l"),
    ]),
    ("Резервная копия", "Один файл со всем; восстановление добавляет записи", True, [
        ("Ещё → Резервная копия", "", "Backup.dc.html", "l"),
        ("Автокопия", "раз в неделю в выбранную папку", "Backup.dc.html", "l"),
        ("Сохранить копию", "вручную, в папку или на Диск", "Backup.dc.html", "l"),
        ("Восстановить", "записи добавятся к текущим", "BackupRestore.dc.html", "l"),
    ]),
    ("Импорт из Wallet", "Мастер по шагам; можно отменить пакетом", True, [
        ("Ещё → Импорт", "выбрать файл выгрузки", "ImportStart.dc.html", "l"),
        ("Сводка", "сколько счетов и операций", "ImportSummary.dc.html", "l"),
        ("Счета и категории", "сопоставить или создать", "ImportAccounts.dc.html", "l"),
        ("Переводы", "без пары — решить", "ImportTransfers.dc.html", "l"),
        ("Проверка", "копия перед записью", "ImportPreview.dc.html", "l"),
        ("Готово", "«Отменить импорт»", "ImportDone.dc.html", "l"),
    ]),
    ("Стереть всё", "Чтобы загрузить Wallet на чистую", True, [
        ("Настройки → Данные", "Стереть все данные", "Wipe.dc.html", "l"),
        ("Копия", "в «Загрузки»", "Wipe.dc.html", "l"),
        ("Слово СТЕРЕТЬ", "подтверждение", "Wipe.dc.html", "l"),
        ("Стёрто у семьи", "второй телефон — при синке", "Wipe.dc.html", "l"),
        ("Импорт из Wallet", "следующий шаг", "ImportStart.dc.html", "l"),
    ]),
    ("Обратная связь", "Отзыв сразу становится задачей", True, [
        ("Ещё → Обратная связь", "", "Feedback.dc.html", "l"),
        ("Тип и текст", "до 500 знаков", "Feedback.dc.html", "l"),
        ("Отправить", "версия и экран — приложим", "FeedbackSent.dc.html", "l"),
        ("Задача", "issue «source:app» за несколько часов", None, "p"),
    ]),
    ("Настройки", "Группы; тема раскрывается кругом", True, [
        ("Ещё → Настройки", "шесть групп", "Set1Root.dc.html", "l"),
        ("Внешний вид", "язык, тема кругом, палитра", "Set1Look.dc.html", "l"),
        ("Деньги и даты", "валюты итогов", "Set1Money.dc.html", "l"),
        ("Ввод операции", "встряхнуть", "Set1Entry.dc.html", "l"),
        ("Уведомления", "напоминания о платежах", "Set1Notices.dc.html", "l"),
        ("Данные и копия", "копия, импорт, стереть", "Set1Data.dc.html", "l"),
    ]),
    ("ИИ на телефоне", "Дополнение и модель: один «Включить» в два шага", True, [
        ("Ещё → Настройки", "карточка «Умные функции»", "Set1Root.dc.html", "l"),
        ("ИИ на телефоне", "«Включить» → вопрос «Скачать 2,5 ГБ?»", "AiOff.dc.html", "l"),
        ("Шаг 1 из 2", "Android спросит про установку дополнения", "AiStep1.dc.html", "l"),
        ("Шаг 2 из 2", "модель скачивается, можно закрыть экран", "AiStep2.dc.html", "l"),
        ("Включён", "«Удалить модель» — дополнение остаётся", "AiOn.dc.html", "l"),
        ("Слабый телефон", "меньше 6 ГБ: причина, без кнопки", "AiWeak.dc.html", "l"),
    ]),
    ("Быстрые заметки", "Траты строками → черновики → операции (#586)", True, [
        ("Ещё → Деньги", "«Быстрые заметки», «Черновики · N»", "Notes586More.dc.html", "l"),
        ("Заметка", "строка — трата; «Обработать · N»", "Notes586Input.dc.html", "l"),
        ("Черновики", "готовые отмечены; «Записать · N»", "Notes586Drafts.dc.html", "l"),
        ("Не всё готово", "«Нужна сумма», «Выберите категорию»", "Notes586Unready.dc.html", "l"),
        ("Записано", "«Записано: N» · «Отменить»", "Notes586Posted.dc.html", "l"),
        ("Обзор", "строка «Черновики · N» под итогом", "Notes586Home.dc.html", "l"),
    ]),
    ("Настройки: экраны", "Что показывать и как открывать", True, [
        ("Настройки → Экраны", "обзор и ещё три", "Set1Screens.dc.html", "l"),
        ("Обзор", "карточки: порядок и скрытие", "Set1Home.dc.html", "l"),
        ("Подробности", "«Скоро» и «Последние»", "Set1HomeMore.dc.html", "l"),
        ("Правка на «Обзоре»", "удержание: ⠿ и ✕", "Set1HomeEdit.dc.html", "l"),
        ("Баланс", "какие счета входят", "Set1Balance.dc.html", "l"),
    ]),
    ("Настройки: операции, отчёты, счета", "Как строки и списки открываются", True, [
        ("Операции", "счёт, теги, заметка в строке", "Set1Ops.dc.html", "l"),
        ("Отчёты", "период и сравнение", "Set1Reports.dc.html", "l"),
        ("Счета", "группировка и архив", "Set1Accounts.dc.html", "l"),
        ("Ввод: счёт", "«Всегда этот» + счёт", "Set1EntryAcc.dc.html", "l"),
    ]),
    ("Экспорт в CSV", "Операции за период — файлом (#204)", True, [
        ("Данные → Экспорт в CSV", "рядом с копией и импортом", "Fb204Export.dc.html", "l"),
        ("Период и счета", "месяц, год или свой", "Fb204Export.dc.html", "l"),
        ("Выгрузить и поделиться", "системное «Поделиться»", None, "p"),
    ]),
    ("Шаблоны", "Частые покупки в один тап (выбран А)", True, [
        ("«+» на панели", "", None, "g"),
        ("Шаблоны", "☰ рядом с типом в шторке", "TplA.dc.html", "l"),
        ("Шторка из шаблона", "сумма и категория уже есть", "TplEntry.dc.html", "l"),
        ("«Готово»", "", None, "p"),
        ("Из операции", "«Сохранить как шаблон»", "TplSave.dc.html", "l"),
        ("Все шаблоны", "32 шт., поиск, правка", "TplAll.dc.html", "l"),
    ]),
    ("Бюджеты", "Конверты на месяц (#167)", True, [
        ("Первый запуск", "лимиты из прошлых месяцев", "BudFirst.dc.html", "l"),
        ("Конверты", "сколько осталось", "BudA.dc.html", "l"),
        ("Конверт", "категории, операции", "BudDetail.dc.html", "l"),
        ("Лимит", "сумма, подлимиты, перенос", "BudEdit.dc.html", "l"),
        ("«Обзор»", "плитка «горит»", "BudHome.dc.html", "l"),
    ]),
    ("Отчёты", "Куда ушли деньги (#168)", True, [
        ("Отчёты", "лента карточек", "RepA.dc.html", "l"),
        ("Период и валюта", "шторка", "RepPeriod.dc.html", "l"),
        ("Тап по категории", "из чего сложилось", "RepDrill.dc.html", "l"),
        ("«Другое»", "все 24 категории", "RepAll.dc.html", "l"),
    ]),
    ("Итог месяца", "Что вышло за месяц и лимиты (#236)", True, [
        ("Отчёты", "карточка итога", "RepA.dc.html", "l"),
        ("Итог месяца", "траты, доходы, сравнение", "Mon236Screen.dc.html", "l"),
        ("Лимиты месяца", "подробнее по конвертам", "Mon236Limits.dc.html", "l"),
        ("Капитал", "счета на конец месяца", "Rp189NwDrill.dc.html", "l"),
    ]),
    ("Регулярные платежи", "Повтор поверх шаблонов (#143)", True, [
        ("«Обзор»", "«Скоро · 7 дней»", "Rec143HomeA.dc.html", "l"),
        ("Список", "все платежи, пауза", "Rec143List.dc.html", "l"),
        ("Повтор в шаблоне", "каждый месяц, 17-го", "Rec143RepeatA.dc.html", "l"),
        ("Уведомление", "«Провести» / напомнить", "Rec143Notify.dc.html", "l"),
        ("«Провести»", "шторка с суммой", "Rec143Post.dc.html", "l"),
        ("Особые случаи", "курс, пропуски", "Rec143Edge.dc.html", "l"),
    ]),
]

TOP0, STEP, BAND_H, X0, DX, W, H = 184, 132, 112, 300, 256, 196, 76
height = TOP0 + STEP * len(ROWS) + 56
NODE = ("position: absolute; left: {x}px; top: {y}px; width: 196px; height: 76px; box-sizing: border-box; "
        "padding: 8px 12px; display: flex; align-items: center; justify-content: center; text-align: center; gap: 8px; "
        "background: {bg}; color: {ink}; border: {border}; border-radius: 16px; font-size: 15px; line-height: 20px; "
        "text-decoration: none;")


def node(x, y, title, sub, href, kind):
    lime = kind == "g"
    bg, ink, subc = (LIME, ONLIME, "#2D4A00") if lime else (BAR, INK, MUTED)
    border = {"g": "none", "l": "1px solid %s" % LIMEPILL, "p": "1px solid %s" % LINE, "n": "1px dashed %s" % WARNLINE}[kind]
    style = NODE.format(x=x, y=y, bg=bg, ink=ink, border=border)
    inner = f"<b>{title}</b>" + (f'<br><span style="font-size: 12px; color: {subc};">{sub}</span>' if sub else "")
    tag = "a" if href else "div"
    attr = f'href="{href}" ' if href else ""
    return f'<{tag} {attr}style="{style}"><span>{inner}</span></{tag}>'


def arrow(x, y):
    return (f'<svg width="59" height="10" viewBox="0 0 59 10" aria-hidden="true" style="position: absolute; left: {x}px; top: {y - 1}px;">'
            f'<path d="M1 5H52M48 1l5 4-5 4" fill="none" stroke="{MUTED2}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path></svg>')


bands, arrows, nodes, labels = [], [], [], []
for i, (title, sub, chain, steps) in enumerate(ROWS):
    top = TOP0 + STEP * i
    bands.append(f'<div style="position: absolute; left: 32px; top: {top}px; width: 1776px; height: {BAND_H}px; border-radius: 20px; background: {CARD if i % 2 == 0 else "#0A0F08"};"></div>')
    labels.append(f'<div style="position: absolute; left: 64px; top: {top + 24}px; width: 196px; font-size: 18px; line-height: 24px; font-weight: 700;">{title}</div>')
    labels.append(f'<div style="position: absolute; left: 64px; top: {top + 52}px; width: 196px; font-size: 13px; line-height: 18px; color: {MUTED};">{sub}</div>')
    assert len(steps) <= 6, title
    for k, (t, st, href, kind) in enumerate(steps):
        x = X0 + DX * k
        nodes.append(node(x, top + 16, t, st, href, kind))
        if chain and k:
            arrows.append(arrow(x - 60, top + 50))

text = (f'<div style="position: absolute; left: 64px; top: 56px; font-size: 34px; line-height: 40px; font-weight: 700;">Воркфлоу: как проходят главные действия</div>'
        f'<div style="position: absolute; left: 64px; top: 108px; width: 1500px; font-size: 15px; line-height: 22px; color: {MUTED};">'
        'Карточки с зелёной рамкой открывают макет экрана (кнопка Play; главные доски рядов живые — тапайте). Лайм — «+» и его ответ после сохранения. '
        'Пунктир — экран есть в приложении, но макета ещё нет. Сверено с кодом 10 ноября, с макетами — 12 ноября.</div>')
body = "\n".join(bands + arrows + nodes + labels) + text
html = board("Воркфлоу", f'<div style="position: relative; width: 1840px; height: {height}px; background: {BG};">{body}</div>', height=height)
html = html.replace("width: 390px; height: %dpx" % height, "width: 1840px; height: %dpx" % height).replace('"width":390,"height":%d' % height, '"width":1840,"height":%d' % height)
out = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(__file__), "../../docs/design/mockups/Flows.dc.html")
open(out, "w").write(html.replace("{{div}}", ""))
print("height", height)
