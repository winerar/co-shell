package app.coshell.core.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.charts.BulletTone
import app.coshell.core.designsystem.charts.ChartColumn
import app.coshell.core.designsystem.charts.ChartScale
import app.coshell.core.designsystem.charts.CoShellBarRow
import app.coshell.core.designsystem.charts.CoShellBullet
import app.coshell.core.designsystem.charts.CoShellColumns
import app.coshell.core.designsystem.charts.CoShellDonut
import app.coshell.core.designsystem.charts.DonutSlice
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing

/** Board `RepA` in numbers: November so far, the grey ghost — October. Name, USD, share, October USD, palette colour. */
private val structure = listOf(
    Triple("Дом", 290L to 300L, 9),
    Triple("Продукты", 121L to 180L, 2),
    Triple("Образование", 120L to 160L, 5),
    Triple("Кафе", 89L to 110L, 0),
    Triple("Аптека", 70L to 40L, 7),
)
private val months = listOf("июн" to 1180L, "июл" to 1320L, "авг" to 1090L, "сен" to 1410L, "окт" to 1350L, "ноя" to 760L)

/** Showcase of the chart components (#168): bars with the past period, columns by month, the ring, bullets. */
@Composable
fun ChartsCatalog() {
    Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        CoShellSectionLabel("На что ушли деньги · черта — октябрь")
        CoShellCard {
            Column(Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s)) {
                val scale = ChartScale.of(structure.flatMap { (_, values) -> listOf(values.first, values.second) })
                val total = structure.sumOf { it.second.first }
                structure.forEach { (name, values, colorIndex) ->
                    val (now, before) = values
                    CoShellBarRow(
                        label = name,
                        value = now.toString(),
                        share = "${now * 100 / total}%",
                        fraction = scale.of(now),
                        color = categoryColor(CategoryPalette.colors[colorIndex]),
                        ghost = scale.of(before),
                        ghostDescription = "в октябре $before",
                        leading = { CategoryBadge(null, CategoryPalette.colors[colorIndex]) },
                        onClick = {},
                    )
                }
            }
        }
        CoShellSectionLabel("По месяцам")
        CoShellCard {
            val scale = ChartScale.of(months.map { it.second })
            CoShellColumns(
                months.map { (label, value) -> ChartColumn(label, value.toString(), scale.of(value)) },
                Modifier.padding(Spacing.l),
                average = scale.of(months.sumOf { it.second } / months.size),
                onSelect = {},
            )
        }
        CoShellSectionLabel("Кольцо · до 6 + «Другое»")
        CoShellDonut(
            slices = structure.map { (_, values, colorIndex) -> DonutSlice(values.first, categoryColor(CategoryPalette.colors[colorIndex])) } +
                DonutSlice(70, MaterialTheme.colorScheme.outline),
            description = "Траты за ноябрь по категориям",
        ) { Text("760", style = MaterialTheme.typography.titleLarge) }
        CoShellSectionLabel("Bullet: в плане, обгон темпа, перерасход")
        CoShellBullet(0.3f, BulletTone.NORMAL, mark = 0.37f)
        CoShellBullet(0.75f, BulletTone.WARNING, mark = 0.37f)
        CoShellBullet(1.2f, BulletTone.DANGER, mark = 0.37f)
    }
}

@Preview(name = "Charts · dark", showBackground = true, heightDp = 1100)
@Composable
private fun ChartsCatalogDarkPreview() = CoShellTheme(darkTheme = true) { Surface { ChartsCatalog() } }

@Preview(name = "Charts · light", showBackground = true, heightDp = 1100)
@Composable
private fun ChartsCatalogLightPreview() = CoShellTheme(darkTheme = false) { Surface { ChartsCatalog() } }
