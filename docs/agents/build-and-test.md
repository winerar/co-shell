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

Перед коммитом обязательно: `./gradlew check assembleDebug`. Агенту — через `scripts/agent/check.sh`: то же самое,
но печатает только ошибки, упавшие тесты и итог (`Tests: N`, `OK`/`FAILED`) — экономит токены.

## Тесты
- Модель и домен — `kotlin.test` в `commonTest` (запускаются на JVM, позже и на iOS).
- Data — `core/data/src/jvmTest`, in-memory SQLite (подробности — `database.md`).
- ViewModel — JUnit + `kotlinx-coroutines-test` в `app/src/test`.
- Правила Firestore и сквозной синк двух телефонов — в эмуляторе: `scripts/test-rules.sh` (правила) или
  `scripts/test-rules.sh --kotlin` (+ `FirestoreEmulatorTest`); нужны Node 20+ и Java, эмулятор скачивается
  сам. Поменял `firestore.rules` или `core:data/remote` — прогони. В обычном `check` этот тест пропускается.
- Python-скрипты — `python3 -m unittest discover -s scripts -p 'test_*.py'` (есть шаг в CI).
- Имена тестов — предложение в обратных кавычках: `` fun `refuses to mix currencies`() ``.

## Версии
Все версии — в `gradle/libs.versions.toml`. AGP 9 встроенно компилирует Kotlin, поэтому
плагин `org.jetbrains.kotlin.android` **не** подключается; нужен только `kotlin.compose` для Compose.

## CI
`.github/workflows/ci.yml`: на push в `main` и `dev`, на PR и вручную (Actions → CI → Run workflow).
- PR и `dev`: `check assembleDebug` одним вызовом, debug APK в артефактах (14 дней). Если изменены только
  `docs/` и `*.md` — Gradle пропускается (шаг «Detect code changes»), остаются тесты скриптов.
- `main` (релиз): lint и тесты **не** повторяются — то же дерево уже проверено в PR `dev` → `main`;
  только сборки APK. В `assembleRelease` выключен lintVital (`lint.checkReleaseBuilds = false`).
- Кэш Gradle пишут `main` и `dev` (PR в `dev` стартуют с тёплым кэшем). Необязательный секрет
  `GRADLE_ENCRYPTION_KEY` (`openssl rand -base64 16`) включает сохранение configuration cache.
- На push в `main` — GitHub Release `build-<N>` с `co-shell-debug.apk` (и `co-shell.apk`, если включено
  самообновление); последняя — `https://github.com/winerar/co-shell/releases/latest` (репозиторий приватный).
`versionCode` = номер запуска CI (`BUILD_NUMBER`, локально 1). Debug подписан общим ключом
`app/debug.keystore` из репозитория (не секрет), пакет `app.coshell.debug`, название «co-shell debug».

## Обратная связь
- CI передаёт сборке переменные репозитория `FIREBASE_PROJECT_ID`, `FIREBASE_API_KEY` (Variables, не
  секреты) → `BuildConfig`; локально пусто → форма «не настроено». `feedback.yml` раз в 6 ч создаёт
  issue из Firestore `feedback` (секрет `FIREBASE_SERVICE_ACCOUNT`). Настройка — `docs/setup/feedback.md`.

## Самообновление (release)
- Включается, когда заданы секреты `RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`,
  `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` и `RELEASES_TOKEN` (fine-grained PAT, Contents: write
  только на `co-shell-releases`). Шаги владельца — `docs/setup/self-update.md`. Без них шаги release
  пропускаются, CI зелёный.
- CI: `assembleRelease` (ключ из секрета во временный файл,
  `UPDATE_BASE_URL=https://github.com/<owner>/co-shell-releases/releases/latest/download`) →
  `scripts/make-update-files.py` → `update-files/` → `gh release create` в **публичном** `co-shell-releases`.
- Firebase Hosting не подходит: на Spark запрещены исполняемые файлы, в т.ч. `.apk`.
- Локально release без ключа собирается неподписанным и с пустым `UPDATE_BASE_URL` (экран «не настроено»).
  Проверить R8/подпись: `keytool -genkeypair` во временный файл и те же переменные окружения.
- Текст «Что нового» — верхний раздел `docs/whats-new.md` (он публичен — без личных данных).
