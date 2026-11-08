# Сборка и тесты

## Требования
- JDK 21 (байткод проекта — Java 17).
- Android SDK: `platforms;android-37.0`, `build-tools;36.0.0`. Путь — `ANDROID_HOME`
  или `sdk.dir` в `local.properties` (файл не коммитится).
- Gradle — только через `./gradlew` (версия зафиксирована во wrapper).

## Команды
| Команда | Что делает |
|---|---|
| `./gradlew check` | lint всех Android-модулей + все unit-тесты |
| `./gradlew :core:model:jvmTest` | тесты модели (быстро) |
| `./gradlew assembleDebug` | debug APK → `app/build/outputs/apk/debug/` |
| `./gradlew :app:testDebugUnitTest` | unit-тесты приложения |

Перед коммитом обязательно: `./gradlew check assembleDebug`.

## Тесты
- Модель и домен — `kotlin.test` в `commonTest` (запускаются на JVM, позже и на iOS).
- ViewModel — JUnit + `kotlinx-coroutines-test` в `app/src/test`.
- Имена тестов — предложение в обратных кавычках: `` fun `refuses to mix currencies`() ``.

## Версии
Все версии — в `gradle/libs.versions.toml`. AGP 9 встроенно компилирует Kotlin, поэтому
плагин `org.jetbrains.kotlin.android` **не** подключается; нужен только `kotlin.compose` для Compose.

## CI
`.github/workflows/ci.yml`: на push в `main` и на PR — `check`, `assembleDebug`,
debug APK в артефактах сборки (14 дней). Release-сборка и самообновление — этап 2.
