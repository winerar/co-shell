# Сборка и тесты

## Требования
- JDK 21 (байткод проекта — Java 17).
- Android SDK: `platforms;android-37.0`, `build-tools;36.0.0`. Путь — `ANDROID_HOME`
  или `sdk.dir` в `local.properties` (файл не коммитится).
- Gradle — только через `./gradlew` (версия зафиксирована во wrapper).

## Установка SDK вручную (если setup-скрипт окружения не отработал)
Нужен доступ к `dl.google.com` (окружение **co-shell**).
```bash
mkdir -p /opt/android-sdk/cmdline-tools && cd /opt/android-sdk
curl -sSo /tmp/clt.zip https://dl.google.com/android/repository/commandlinetools-linux-9862592_latest.zip
unzip -q /tmp/clt.zip -d cmdline-tools && mv cmdline-tools/cmdline-tools cmdline-tools/latest
yes | cmdline-tools/latest/bin/sdkmanager --sdk_root=/opt/android-sdk --licenses >/dev/null
cmdline-tools/latest/bin/sdkmanager --sdk_root=/opt/android-sdk "platforms;android-37.0" "build-tools;36.0.0"
echo "sdk.dir=/opt/android-sdk" > <repo>/local.properties
```

## Команды
| Команда | Что делает |
|---|---|
| `./gradlew check` | lint всех Android-модулей + все unit-тесты |
| `./gradlew :core:model:jvmTest` | тесты модели (быстро) |
| `./gradlew :core:domain:jvmTest` | тесты use cases (на фейках) |
| `./gradlew :core:data:jvmTest` | тесты Room на JVM + перегенерация `core/data/schemas/` |
| `./gradlew assembleDebug` | debug APK → `app/build/outputs/apk/debug/` |
| `./gradlew :app:testDebugUnitTest` | unit-тесты приложения |

Перед коммитом обязательно: `./gradlew check assembleDebug`. Агенту — через `scripts/check.sh`: то же самое,
но печатает только ошибки, упавшие тесты и итог (`Tests: N`, `OK`/`FAILED`) — экономит токены.

## Тесты
- Модель и домен — `kotlin.test` в `commonTest` (запускаются на JVM, позже и на iOS).
- Data — `core/data/src/jvmTest`, in-memory SQLite (подробности — `database.md`).
- ViewModel — JUnit + `kotlinx-coroutines-test` в `app/src/test`.
- Имена тестов — предложение в обратных кавычках: `` fun `refuses to mix currencies`() ``.

## Версии
Все версии — в `gradle/libs.versions.toml`. AGP 9 встроенно компилирует Kotlin, поэтому
плагин `org.jetbrains.kotlin.android` **не** подключается; нужен только `kotlin.compose` для Compose.

## CI
`.github/workflows/ci.yml`: на push в `main`, на PR и вручную (Actions → CI → Run workflow) — `check`, `assembleDebug`, debug APK в артефактах (14 дней).
На push в `main` — GitHub Release `build-<N>` с `co-shell-debug.apk` (и `co-shell.apk`, если включено
самообновление); последняя — `https://github.com/winerar/co-shell/releases/latest` (репозиторий приватный).
`versionCode` = номер запуска CI (`BUILD_NUMBER`, локально 1). Debug подписан общим ключом
`app/debug.keystore` из репозитория (не секрет), пакет `app.coshell.debug`, название «co-shell debug».

## Самообновление (release)
- Включается, когда заданы секреты `RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`,
  `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`, `FIREBASE_SERVICE_ACCOUNT` и переменная `FIREBASE_PROJECT_ID`
  (шаги владельца — `docs/setup/self-update.md`). Без них шаги release пропускаются, CI зелёный.
- CI: `assembleRelease` (ключ из секрета во временный файл, `UPDATE_BASE_URL=https://<id>.web.app`) →
  `scripts/make-update-site.py` → `update-site/` → деплой в Firebase Hosting (`firebase.json`).
- Локально release без ключа собирается неподписанным и с пустым `UPDATE_BASE_URL` (экран «не настроено»).
  Проверить R8/подпись: `keytool -genkeypair` во временный файл и те же переменные окружения.
- Текст «Что нового» — верхний раздел `docs/whats-new.md`.
