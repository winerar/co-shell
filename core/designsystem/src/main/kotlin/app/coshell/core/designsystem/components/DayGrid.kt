package app.coshell.core.designsystem.components

import app.coshell.core.designsystem.theme.selectionColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Days 1..[days] seven to a row (#345, «Начало месяца»): 44 dp circles, [selected] — neon + 2 dp frame in ink + bold (board Style1),
 * [markedDay] (the default) carries a small dot under the number.
 */
@Composable
fun CoShellDayGrid(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, days: Int = 31, markedDay: Int? = null) {
    val colors = MaterialTheme.colorScheme
    val sel = selectionColors()
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        (1..days).chunked(DAYS_IN_ROW).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                week.forEach { day ->
                    val on = day == selected
                    Box(Modifier.weight(1f).height(Sizes.cellHeight), contentAlignment = Alignment.Center) {
                        Box(
                            Modifier
                                .size(Sizes.cellHeight)
                                .background(if (on) sel.fill else Color.Transparent, CircleShape)
                                .then(if (on) Modifier.fillEdge(sel.fill, CircleShape) else Modifier)
                                .semantics { this.selected = on }
                                .tactileClickable(Tactile.Control, CircleShape, role = Role.RadioButton) { onSelect(day) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                day.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                                color = if (on) sel.content else colors.onSurface,
                            )
                            if (day == markedDay && !on) {
                                Box(
                                    Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = Spacing.xs)
                                        .size(Sizes.dayMark)
                                        .background(colors.tertiary, CircleShape),
                                )
                            }
                        }
                    }
                }
                repeat(DAYS_IN_ROW - week.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

private const val DAYS_IN_ROW = 7
