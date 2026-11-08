# Готовность к iOS (Kotlin Multiplatform)

iOS-версии пока нет, но код пишем так, чтобы её можно было добавить без переписывания.

## Правила
1. `core:model` и `core:domain` — модули `kotlin("multiplatform")`, код в `commonMain`.
   Сейчас таргет только `jvm()`; `iosArm64()` / `iosSimulatorArm64()` добавим, когда появится iPhone.
2. В `commonMain` запрещены `android.*`, `java.*`, `javax.*`. Компилятор это проверит: в общем
   коде этих API нет.
3. Время и даты — `kotlinx-datetime`; сериализация — `kotlinx-serialization`; не `java.time`.
4. Точная дробная арифметика (курсы) — KMP-библиотека (кандидат: `com.ionspin.kotlin:bignum`)
   или свой тип; **не** `java.math.BigDecimal`.
5. Платформенные вещи — за интерфейсами в domain, реализации в Android-модулях:
   `RemoteDataSource` (Firestore), `RatesProvider` (HTTP), фоновый планировщик (WorkManager),
   хранилище настроек.
6. Room — KMP-совместим: entity/DAO держим без Android-специфичных типов.
7. UI на Compose пишем как обычно; при переходе на Compose Multiplatform основная часть переносится.

## Чего не делаем заранее
- Не подключаем iOS-таргеты и не настраиваем сборку под Mac, пока нет iPhone.
- Не заменяем официальный Firebase Android SDK на KMP-обёртки — он спрятан за интерфейсом.
