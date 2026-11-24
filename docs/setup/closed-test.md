# Закрытое тестирование через APK: что сделать владельцу

Один список по порядку. Как устроено — `docs/research/closed-testing-plan.md`; подробности шагов — `invite.md` (коды), `landing.md` (страница).
Google Play сейчас не нужен (решение 2026-11-23). Всё бесплатно: GitHub Pages публичного `co-shell-releases` + Firestore (Spark).

## Решения (ответить в #670)
1. **Когда включать шлюз.** После включения **каждый** телефон, и ваш, и семьи, один раз попросит код. Данные не пропадают.
2. **Телефонов на код** — по умолчанию 3 (1–10). Код — один на человека.
3. **Кто первые** — вы и семья, затем 2–5 друзей.
4. **Адрес страницы** — `winerar.github.io/co-shell-releases` (бесплатно, сразу) или свой домен (Settings → Pages → Custom domain).

## Почему два релиза
Кнопки GitHub Actions «Run workflow» берутся только из `main`. Кнопки выдачи кодов и публикация лендинга пока лежат только в `dev`.
Поэтому: **релиз 1 без шлюза** (кнопки и страница попадают в `main`) → коды → **релиз 2 со шлюзом**.

## Шаг 0. Проверить один раз (5 минут)
- Секрет `FIREBASE_SERVICE_ACCOUNT` и `RELEASE_*`/`RELEASES_TOKEN` на месте:
  https://github.com/winerar/co-shell/settings/secrets/actions
- Переменная `FIREBASE_PROJECT_ID` на месте: https://github.com/winerar/co-shell/settings/variables/actions
- У токена `RELEASES_TOKEN` доступ **Contents: Read and write** к `co-shell-releases`: https://github.com/settings/personal-access-tokens
  (не истёк ли срок — там же).
- Правила Firestore опубликованы: https://console.firebase.google.com/ → ваш проект → Firestore Database → **Rules** — в тексте есть `match /access_codes/{code}`.
  Нет — перезапустить последний CI на `dev` (Re-run all jobs): https://github.com/winerar/co-shell/actions/workflows/ci.yml?query=branch%3Adev

## Шаг 1. Включить лендинг и выпустить релиз 1 (без шлюза)
1. https://github.com/winerar/co-shell/settings/variables/actions → **New repository variable**: `LANDING` = `true`.
   `ACCESS_GATE` **не** задавать.
2. Релиз: https://github.com/winerar/co-shell/actions/workflows/release.yml → **Run workflow** (ветка `main`) → Run.
   Или написать агенту «релиз». Через ~10–20 мин обновление придёт на телефоны как обычно.
3. Включить Pages: https://github.com/winerar/co-shell-releases/settings/pages → Source: **Deploy from a branch** → Branch **`gh-pages`** / **`(root)`** → Save.
4. Через 1–2 минуты открыть https://winerar.github.io/co-shell-releases/ — проверить язык (ru/en/es), кнопку «Скачать», версию.
   Политика: https://winerar.github.io/co-shell-releases/privacy/ru/ , удаление данных: https://winerar.github.io/co-shell-releases/delete/ru/
5. Ответить в #669: открылось или нет.

## Шаг 2. Выдать коды себе и семье
1. https://github.com/winerar/co-shell/actions/workflows/invite.yml → **Run workflow**: «Что сделать» — `выдать код`, «для кого» — имя, «сколько телефонов» — 3 → Run.
2. Открыть запуск → внизу страницы **Summary** — код вида `K7QX-M2PD-9WTA`. Сохранить в заметку или менеджер паролей.
3. Повторить для каждого человека. Передавать лично, не в общий чат.
4. Проверить список: тот же workflow, `показать все коды`.

## Шаг 3. Включить шлюз и выпустить релиз 2
1. https://github.com/winerar/co-shell/settings/variables/actions → **New repository variable**: `ACCESS_GATE` = `true`.
2. Релиз, как в шаге 1.2 (нужен новый раздел в `docs/whats-new.md` — агент добавит). С 1 декабря релизы выходят и сами по понедельникам — шлюз войдёт в каждый.
3. На телефоне после обновления — экран «Введите код приглашения»: ввести свой код (нужен интернет один раз). Неверный код — понятная ошибка.
4. Проверить отзыв: выдать тестовый код, ввести на втором телефоне, затем `отозвать код` → при следующей проверке (раз в 3 дня или при открытии, если пора) — «Доступ закрыт»,
   данные видны, «+» закрыт.
5. Ответить в #670: что сработало, что нет.

## Шаг 4. Позвать друзей
Отправить человеку две вещи: ссылку https://winerar.github.io/co-shell-releases/ и его код (лично). Дальше он сам: «Скачать» → разрешить установку из браузера →
на предупреждение Play Protect — «Всё равно установить» → ввести код. Обновления приходят внутри приложения.
Отзывы друзей придут задачами, как ваши («Ещё → Обратная связь»).

## Если что-то пошло не так
- Шлюз мешает (код потерян, не открывается): `ACCESS_GATE` = `false` и релиз — приложение снова открыто всем. Или выдать новый код.
- Страница не появилась: в последнем запуске CI на `main` шаг «Publish landing» — его лог; чаще всего у `RELEASES_TOKEN` нет записи в `co-shell-releases`.
- Выключить страницу: `LANDING` = `false` (уже опубликованная остаётся, пока не выключить Pages).

## Позже (не мешает начать)
Строка «Код приглашения» в «Ещё → Настройки», QR на странице, скриншоты установки для Xiaomi — #671; ночной канал dev — #655; Google Play — #651, #654, #657.
