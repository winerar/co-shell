# Лендинг с загрузкой APK (GitHub Pages)

Страница на ru/en/es (вариант «Б» опроса 2026-11-25, `docs/design/poll-660.json`): кнопка «Скачать», версия, SHA-256, экран «Обзора» из эталонов скриншотов,
три пункта, «нужен код приглашения», QR-код на страницу (только на компьютере), шаги установки — один список для любого Android (владелец 2026-11-25, без отдельных инструкций для Xiaomi), «Как обновлять»,
«Что нового» (только на русской — `whats-new.md` по-русски), ссылки на политику и удаление данных. Собирает её `scripts/make_landing.py` из `site/index.template.html`
при каждом релизе (CI, шаг «Publish landing») и кладёт в ветку `gh-pages` публичного репозитория релизов `co-shell-releases`.
**Без релиза** (владелец 2026-11-25): изменили только страницу (`site/`, `scripts/landing_texts.py`, `make_landing.py`, `privacy_pages.py`, `qr_code.py`) — после мержа в `dev`
CI сам пересобирает её из `version.json` последнего релиза (шаг «Publish landing (dev)», `scripts/ci/landing-changed.sh` + `publish-landing.sh`); APK и телефоны не трогаются.
В Pages должен быть выбран источник **ветка `gh-pages`** (не `main`), иначе по адресу — служебная страница из README.
**Не для поисковиков** (владелец 2026-11-25: делимся только ссылкой): на каждой странице `<meta name="robots" content="noindex, nofollow">` (тест
`test_no_page_is_open_to_search_engines`); `robots.txt` не помогает — у Pages проекта он должен лежать в корне `<логин>.github.io`.
По умолчанию публикация **выключена**.

## Включить (один раз)
Общий порядок вместе с кодами — `closed-test.md`.
1. В репозитории `co-shell-releases`: **Settings → Pages → Build and deployment → Source: Deploy from a branch → Branch: `gh-pages` / `(root)` → Save.**
   (Ветка `gh-pages` появится после первой публикации — шаг 2 можно сделать раньше и вернуться.)
2. В `co-shell` → Settings → Secrets and variables → Actions → Variables → **New repository variable**: `LANDING` = `true`.
   Секрет `RELEASES_TOKEN` уже есть (самообновление): ему нужен доступ Contents: write к `co-shell-releases`.
3. Выпустить релиз. Адрес страницы: `https://<ваш-логин>.github.io/co-shell-releases/` (через минуту после публикации).
4. Собственный домен — по желанию: Settings → Pages → Custom domain.

## Что дальше
Политика и «удалить данные» (#652): `/privacy/<язык>/`, `/delete/<язык>/` (`scripts/privacy_pages.py`; текст сверять с реальным трафиком приложения и формой «Безопасность данных»). Ссылка из приложения — строка «Политика конфиденциальности» в «Ещё → Настройки → Помощь» (доска `Priv652A`): сборка на `main` получает адрес сайта (`LANDING_URL`, по умолчанию GitHub Pages репозитория релизов) только при `LANDING` = `true`, иначе строки нет (`BuildConfig.SITE_URL`, `settings/PolicyLink.kt`).
Блок «Бесплатно / подписка» — после #658.

## Где что
- Тексты — `scripts/landing_texts.py` (все языки сразу; тест следит за одинаковыми ключами и числом шагов); шаблон — `site/index.template.html`.
- QR — `scripts/qr_code.py` (свой кодировщик без зависимостей, тест — эталоны, сверенные с библиотекой `qrcode`). Адрес — переменная окружения `LANDING_URL`
  (свой домен), иначе `https://<владелец>.github.io/<репозиторий>/` из адреса APK.
- Экран приложения — `app/src/test/screenshots/home_{light,dark,en,es}.png` копируются в `img/`; поменялся эталон «Обзора» — поменяется и лендинг.
