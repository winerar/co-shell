package app.coshell.core.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellHeatGrid
import app.coshell.core.designsystem.components.CoShellHeatLegend
import app.coshell.core.designsystem.components.HeatCell
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing

/** November 2026 on board `CalendarA`: spent per day in USD, 0 — nothing; days after the 23rd have not come yet. */
private val november = listOf(12, 48, 31, 0, 66, 22, 140, 9, 35, 54, 0, 41, 18, 612, 26, 73, 0, 44, 37, 95, 15, 28, 51)

/** Showcase of the spending calendar (#553): quartile levels, today, the chosen day, days to come. */
@Composable
fun HeatCatalog() {
    var selected by remember { mutableIntStateOf(17) }
    val positive = november.filter { it > 0 }.sorted()
    val bounds = (1..3).map { positive[(positive.size - 1) * it / 4] }
    val cells = (1..30).map { day ->
        val spent = november.getOrElse(day - 1) { 0 }
        HeatCell(
            label = day.toString(),
            level = if (spent <= 0) 0 else 1 + bounds.count { spent > it },
            description = "$day ноября, потрачено $spent USD",
            today = day == 23,
            quiet = day > 23,
        )
    }
    Column(Modifier.padding(Spacing.l)) {
        CoShellCard {
            Column(Modifier.padding(Spacing.l)) {
                CoShellHeatGrid(
                    weekdays = listOf("П", "В", "С", "Ч", "П", "С", "В"),
                    leadingBlanks = 6,
                    cells = cells,
                    selected = selected,
                    onSelect = { selected = it },
                )
                CoShellHeatLegend("меньше", "больше", "без трат: 4 дня", Modifier.padding(top = Spacing.m))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 411)
@Composable
private fun HeatCatalogLightPreview() = CoShellTheme(darkTheme = false) { Surface { HeatCatalog() } }

@Preview(showBackground = true, widthDp = 411)
@Composable
private fun HeatCatalogDarkPreview() = CoShellTheme(darkTheme = true) { Surface { HeatCatalog() } }
