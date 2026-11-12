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
        ("Счета", "группы, итоги", "Accounts.dc.html", "l"),
        ("Ещё", "плитки разделов", "More.dc.html", "l"),
    ]),
    ("Новая операция", "Одна шторка, всё под большим пальцем; встряхнуть телефон — тоже", True, [
        ("«+» на панели", "или встряхнуть на вкладке", None, "g"),
        ("Шаг 1", "счёт: шаблоны, недавние, группы", "Main.dc.html", "l"),
        ("Шаг 2", "тип, сумма, категория", "Main.dc.html", "l"),
        ("Чек в др. валюте", "по желанию: ARS → списано USDT", "EntryReceipt.dc.html", "l"),
        ("«Готово»", "на клавиатуре; на доске — тосты", "Main.dc.html", "l"),
        ("«+» → ✓", "тёплая вспышка", None, "g"),
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
    ("Счёт", "Цвет — от банка или из палитры", True, [
        ("Счета", "«+» или тап по счёту", "Accounts.dc.html", "l"),
        ("Редактор счёта", "тип, валюта, баланс", "AccountEdit.dc.html", "l"),
        ("Цвет", "«Похоже на банк» → Взять", "Colors.dc.html", "l"),
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
    ("Настройки", "Тема раскрывается кругом", True, [
        ("Ещё → Настройки", "", "Settings.dc.html", "l"),
        ("Тема", "светлая / тёмная / системная", "Settings.dc.html", "l"),
        ("Палитра", "6 карточек", "Settings.dc.html", "l"),
        ("Применено", "сразу; на холсте — переключатель «Тема макетов»", None, "p"),
    ]),
    ("Шаблоны", "Частые покупки в один тап (выбран А)", True, [
        ("«+» на панели", "", None, "g"),
        ("Полоса шаблонов", "над счетами в шторке", "TplA.dc.html", "l"),
        ("Шторка из шаблона", "сумма и категория уже есть", "TplEntry.dc.html", "l"),
        ("«Готово»", "", None, "p"),
        ("Из операции", "«Сохранить как шаблон»", "TplSave.dc.html", "l"),
        ("Все шаблоны", "32 шт., поиск, правка", "TplAll.dc.html", "l"),
    ]),
    ("Бюджеты", "Конверты на месяц — план (#167)", True, [
        ("Первый запуск", "лимиты из прошлых месяцев", "BudFirst.dc.html", "l"),
        ("Конверты", "сколько осталось", "BudA.dc.html", "l"),
        ("Конверт", "категории, операции", "BudDetail.dc.html", "l"),
        ("Лимит", "сумма, подлимиты, перенос", "BudEdit.dc.html", "l"),
        ("«Обзор»", "плитка «горит»", "BudHome.dc.html", "l"),
    ]),
    ("Отчёты", "Куда ушли деньги — план (#168)", True, [
        ("Отчёты", "лента карточек", "RepA.dc.html", "l"),
        ("Период и валюта", "шторка", "RepPeriod.dc.html", "l"),
        ("Тап по категории", "из чего сложилось", "RepDrill.dc.html", "l"),
        ("«Другое»", "все 24 категории", "RepAll.dc.html", "l"),
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
