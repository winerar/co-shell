# Стиль кода

## Kotlin
- Официальный стиль Kotlin (`kotlin.code.style=official`), отступ 4 пробела, trailing commas.
- Код, идентификаторы и комментарии — на английском. Строки UI — только в `strings.xml` (русский).
- Неизменяемые данные: `val`, `data class`, `List` вместо `MutableList` в публичных API.
- Ошибки ввода — через sealed `Result`, а не исключения. Исключения — для нарушенных инвариантов
  (`require`, переполнение).
- Без `!!`. Без `GlobalScope`.

## Слои и поток данных (UDF)
```
Screen (stateless) --onAction--> ViewModel --> UseCase --> Repository --> Room / Remote
Screen <---- StateFlow<UiState> ---- ViewModel
```
- ViewModel отдаёт один `StateFlow<UiState>`; события от UI — методы или `onAction(Action)`.
- UiState содержит уже отформатированные строки — Compose не знает о `Money`-арифметике.
- Use case заводим только при наличии логики; простое чтение — сразу из репозитория.
- Репозиторий: интерфейс в `core:domain`, реализация в `core:data`.

## Compose
- `Screen` принимает state и лямбды, не ViewModel → его можно превьюить и тестировать.
- У каждого экрана есть `@Preview` в `CoShellTheme`.
- Цвета и типографика — только из `MaterialTheme` / `LocalAmountPalette`, без хардкода.
- Суммы — стилем `AmountTextStyle` (табличные цифры).

## DI (Hilt)
- `@HiltViewModel` + `@Inject constructor`. Модули — `app/.../di/` (позже в модулях data).
- Зависимости через интерфейсы там, где их надо подменять в тестах.
