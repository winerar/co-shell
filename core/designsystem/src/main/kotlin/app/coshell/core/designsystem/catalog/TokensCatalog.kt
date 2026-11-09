package app.coshell.core.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.theme.AmountHeroStyle
import app.coshell.core.designsystem.theme.AmountInputStyle
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Radii
import app.coshell.core.designsystem.tokens.Spacing

/** Showcase of design tokens for Android Studio previews; grows into the component catalog. */
@Composable
fun TokensCatalog() {
    Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
        Text("Суммы", style = MaterialTheme.typography.titleSmall)
        AmountText("1\u202F234\u202F567,89\u00A0USD", style = AmountHeroStyle)
        AmountText("850\u202F000\u00A0ARS", style = AmountTitleStyle)
        Text("1 049,69", style = AmountInputStyle)
        AmountText("−12,50\u00A0USDT", style = AmountTextStyle, color = LocalAmountPalette.current.expense)
        Text("1 USD = 1 234,5 ARS", style = RateTextStyle)

        Text("Текст", style = MaterialTheme.typography.titleSmall)
        Text("Обзор", style = MaterialTheme.typography.titleLarge)
        Text("Продукты и бытовая химия", style = MaterialTheme.typography.bodyLarge)
        Text("Карта · 5 ноября, среда", style = MaterialTheme.typography.bodyMedium)
        Text("Сохранить", style = MaterialTheme.typography.labelLarge)

        Text("Цвета", style = MaterialTheme.typography.titleSmall)
        ColorRow(
            listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.error,
            ),
        )
        ColorRow(
            listOf(
                LocalAmountPalette.current.income,
                LocalAmountPalette.current.expense,
                LocalAmountPalette.current.transfer,
                LocalStatusPalette.current.warning,
            ),
        )
        CategoryPalette.colors.chunked(9).forEach { row -> ColorRow(row.map { categoryColor(it) }) }

        Text("Отступы", style = MaterialTheme.typography.titleSmall)
        Spacing.scale.forEach { SpacingSample(it) }

        Text("Скругления", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            listOf(Radii.small, Radii.medium, Radii.large, Radii.extraLarge).forEach { RadiusSample(it) }
        }
    }
}

@Composable
private fun ColorRow(colors: List<Color>) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        colors.forEach { Box(Modifier.size(Spacing.xl).background(it, RoundedCornerShape(Radii.small))) }
    }
}

@Composable
private fun SpacingSample(value: Dp) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Box(Modifier.width(value).height(Spacing.l).background(MaterialTheme.colorScheme.primary))
        Text("$value", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun RadiusSample(radius: Dp) {
    Box(
        Modifier.size(56.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(radius)),
        contentAlignment = Alignment.Center,
    ) { Text("$radius", style = MaterialTheme.typography.labelSmall) }
}

@Preview(name = "Tokens · light", showBackground = true)
@Composable
private fun TokensCatalogLightPreview() = CoShellTheme(darkTheme = false) { Surface { TokensCatalog() } }

@Preview(name = "Tokens · dark", showBackground = true)
@Composable
private fun TokensCatalogDarkPreview() = CoShellTheme(darkTheme = true) { Surface { TokensCatalog() } }
