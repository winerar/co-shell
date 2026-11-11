# Включить самообновление (делает владелец, один раз)

> Шаги 1–4 выполнены 2026-11-08. Осталось: шаг 5 на каждом телефоне.

Код готов: приложение при запуске (в сети — не чаще раза в час) читает `version.json` из последнего релиза **публичного**
репозитория `winerar/co-shell-releases`, скачивает APK, сверяет SHA-256 и ставит его через окно
Android. В этом репозитории только APK и `version.json` — ни кода, ни документов, ни данных семьи
(данные живут только на телефонах). Основной репозиторий остаётся приватным.

## 1. Ключ подписи (если ещё не сделан)
1. GitHub → `co-shell` → **Actions** → слева **Create release key** → **Run workflow**.
2. Открыть запуск → внизу **Artifacts** → скачать `co-shell-release-key`.
3. Сохранить архив на Google Диск. **Без него приложение нельзя будет обновить** — только
   переустановить с потерей данных. В GitHub архив удалится сам через сутки.
4. **Settings → Secrets and variables → Actions → Secrets → New repository secret**, четыре штуки
   со значениями из `README.txt` архива: `RELEASE_KEYSTORE_BASE64` (содержимое
   `RELEASE_KEYSTORE_BASE64.txt`), `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`.

## 2. Публичный репозиторий для обновлений
1. https://github.com/new → Repository name: **`co-shell-releases`** (точно так).
2. Видимость — **Public**. Отметить **Add a README file**. **Create repository**.

## 3. Токен, которым CI выкладывает обновления
1. https://github.com/settings/personal-access-tokens/new (Settings → Developer settings →
   Personal access tokens → **Fine-grained tokens** → **Generate new token**).
2. Token name: `co-shell releases`. Expiration — максимальный срок (запишите дату: после неё
   обновления перестанут выкладываться, токен нужно будет перевыпустить).
3. Repository access → **Only select repositories** → выбрать `co-shell-releases`.
4. Permissions → Repository permissions → **Contents** → **Read and write**.
5. **Generate token** → скопировать токен (показывается один раз).
6. В репозитории `co-shell` → Settings → Secrets and variables → Actions → **New repository secret**:
   Name `RELEASES_TOKEN`, Secret — скопированный токен.

## 4. Проверка
1. `co-shell` → Actions → **«Сборка и тесты (CI)»** → **Run workflow** → `main` → Run workflow. Через 5 минут должны
   пройти шаги «Build signed release APK» и «Publish update».
2. В `co-shell-releases` → **Releases** появится «Сборка N» с файлами `co-shell-N.apk` и `version.json`.
3. На телефоне открыть
   `https://github.com/winerar/co-shell-releases/releases/latest/download/version.json` —
   скачается/покажется текст с `versionCode` (без входа в GitHub).

## 5. Переход на новую сборку (один раз на каждом телефоне)
Release-сборка — отдельное приложение (другое имя пакета и ключ), поэтому данные переносим копией:
1. В старом co-shell: **Ещё → Резервная копия → Сохранить копию** (на Google Диск).
2. На телефоне открыть https://github.com/winerar/co-shell-releases/releases/latest → скачать
   `co-shell-N.apk` → установить (разрешить установку из браузера/Файлов, если спросит).
3. В новом co-shell: **Ещё → Резервная копия → Восстановить из копии**. Проверить счета и операции.
4. Пройти по всем экранам; если что-то закрывается с ошибкой — сообщить, старое приложение не удалять.
5. Если всё в порядке — удалить старое (в «Настройки → Приложения» у него версия с `-debug`).

Дальше обновления приходят сами: при каждом запуске, пока новая версия не установлена, появится «Доступна версия…». В первый раз
Android попросит «Разрешить установку из этого источника» для co-shell — разрешить.

Firebase-проект, созданный раньше, не удаляйте — он понадобится на этапе 4 (синхронизация).
Секрет `FIREBASE_SERVICE_ACCOUNT` и переменная `FIREBASE_PROJECT_ID` сейчас не используются.
