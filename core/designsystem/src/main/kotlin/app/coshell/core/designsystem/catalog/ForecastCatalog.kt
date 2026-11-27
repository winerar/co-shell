package app.coshell.core.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.charts.CoShellForecastLine
import app.coshell.core.designsystem.charts.CoShellShareBar
import app.coshell.core.designsystem.charts.CoShellSparkline
import app.coshell.core.designsystem.charts.ShareSlice
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Showcase of the bars and lines added with «Счета», «Курсы» and «Отчёты»: the share bar of currencies (#192, board `Idea5A`),
 * the sparkline of a rate (#195, `Idea10A`) and the balance line with a look ahead (#770, `Forecast770`, `Forecast770Low`).
 */
@Composable
fun ForecastCatalog() {
    Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        CoShellSectionLabel("Доли: валюты общего баланса (#192)")
        CoShellShareBar(
            slices = listOf(
                ShareSlice("USD", "46 %", 0.46f, MaterialTheme.colorScheme.primary),
                ShareSlice("ARS", "31 %", 0.31f, MaterialTheme.colorScheme.secondary),
                ShareSlice("AMD", "23 %", 0.23f, MaterialTheme.colorScheme.tertiary),
            ),
            description = "Доли валют: USD 46 %, ARS 31 %, AMD 23 %",
        )
        CoShellSectionLabel("Спарклайн: курс за 30 дней (#195)")
        CoShellSparkline(listOf(1040f, 1030f, 1022f, 1018f, 1006f, 996f, 984f, 977f), MaterialTheme.colorScheme.secondary, "Курс за 30 дней")
        CoShellSectionLabel("Остаток вперёд: было и ожидаем (#770)")
        CoShellForecastLine(
            past = listOf(640L, 620L, 660L, 600L, 580L, 610L, 560L),
            future = listOf(560L, 520L, 560L, 620L, 680L, 720L, 780L, 740L, 700L, 640L),
            lowest = 6,
            warning = false,
            description = "Остаток вперёд: самая низкая точка в середине",
            startLabel = "14 ноября",
            todayLabel = "сегодня",
            endLabel = "26 января",
        )
        CoShellSectionLabel("Остаток вперёд: не хватает (цвет внимания)")
        CoShellForecastLine(
            past = listOf(640L, 620L, 660L, 600L, 580L, 610L, 560L),
            future = listOf(560L, 480L, 380L, 260L, 140L, -40L, -120L, -60L, 40L, 160L),
            lowest = 6,
            warning = true,
            description = "Остаток вперёд: уходит ниже нуля",
            startLabel = "14 ноября",
            todayLabel = "сегодня",
            endLabel = "26 января",
        )
    }
}

@Preview(name = "Forecast · dark", showBackground = true, heightDp = 640)
@Composable
private fun ForecastCatalogDarkPreview() = CoShellTheme(darkTheme = true) { Surface { ForecastCatalog() } }

@Preview(name = "Forecast · light", showBackground = true, heightDp = 640)
@Composable
private fun ForecastCatalogLightPreview() = CoShellTheme(darkTheme = false) { Surface { ForecastCatalog() } }
