package app.coshell.reports

import app.coshell.goals.GoalWhatIfUi
import app.coshell.goals.WhatIfOption
import app.coshell.goals.WhatIfResult
import app.coshell.reports.ReportsUiState.Mark
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** Numbers of the boards `Smart786…792` (#785): previews and screenshot tests of the smart cards. */
object SmartSamples {
    private const val NB = " "
    private const val SP = " "

    val unusual = listOf(
        UnusualLine("cafe", "Кафе", "cafe", 0xFFFF9800, "182${NB}000", 0.9f, 0.45f, Mark(up = true, percent = 102, expected = "90${NB}000", byDay = null)),
        UnusualLine("taxi", "Такси", "taxi", 0xFFFFEB3B, "41${NB}000", 0.2f, 0.29f, Mark(up = false, percent = 30, expected = "58${NB}000", byDay = null)),
    )

    val conversion = Conversion(
        total = "23,30${SP}USD",
        better = false,
        pair = ConversionPair(
            priceCurrency = "ARS", paidCurrency = "USDT",
            price = "1${NB}284${NB}000${SP}ARS", paid = "1${NB}288,40${SP}USDT", official = "1${NB}265,10${SP}USDT",
            rate = "1 USDT = 996,90 ARS", officialRate = "1${NB}015", percent = "1,8", worse = true,
        ),
        accounts = listOf(
            ConversionAccount("galicia", "Galicia", "USD", "3,9", worse = true),
            ConversionAccount("lemon", "Lemon", "USDT", "1,8", worse = true),
        ),
        purchases = listOf(
            ConversionPurchase("p1", LocalDate(2026, 11, 26), "Lemon", "48${NB}500${SP}ARS", "49,20${SP}USDT", "1 USDT = 985,77 ARS", "2,9", worse = true),
            ConversionPurchase("p2", LocalDate(2026, 11, 24), "Galicia", "120${NB}000${SP}ARS", "122,40${SP}USD", "1 USD = 980,39 ARS", "3,9", worse = true),
        ),
    )

    val inflation = Inflation(
        local = "ARS",
        hard = "USD",
        localPercent = 38,
        hardPercent = 4,
        months = (6..11).map { YearMonth(2026, it) },
        localIndex = listOf(1000L, 1060L, 1130L, 1210L, 1290L, 1380L),
        hardIndex = listOf(1000L, 990L, 1020L, 1010L, 1030L, 1040L),
        fastest = listOf(
            Growth("cafe", "Кафе", "cafe", 0xFFFF9800, 11),
            Growth("food", "Продукты", "food", 0xFF8BC34A, 6),
            Growth("taxi", "Такси", "taxi", 0xFFFFEB3B, -8),
        ),
    )

    private val cafe20 = WhatIfResult("cafe", "Кафе", 20, "90${SP}USD", "Октябрь 2027", "Июль 2027", 3, 8)

    val whatIf = GoalWhatIfUi(
        goalId = "trip",
        goalName = "Отпуск",
        target = "3${NB}000${SP}USD",
        categories = listOf(WhatIfOption("cafe", "Кафе"), WhatIfOption("taxi", "Такси"), WhatIfOption("fun", "Развлечения")),
        results = mapOf(GoalWhatIfUi.key("cafe", 20) to cafe20, GoalWhatIfUi.key("cafe", 30) to cafe20.copy(share = 30, saved = "135${SP}USD", with = "Июнь 2027", earlier = 4, months = 7)),
        line = cafe20,
    )
}
