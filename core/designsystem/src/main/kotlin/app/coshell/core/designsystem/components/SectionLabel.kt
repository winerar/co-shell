package app.coshell.core.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.coshell.core.designsystem.theme.accentInk

/** Look of a [CoShellSectionLabel]. */
enum class SectionTone {
    /** Grey, as written: «Тема», «Участники», «Мои отзывы · 5». */
    Muted,

    /** Accent capitals: «СЕГОДНЯ», «НЕДАВНИЕ», «9 НОЯБРЯ, ПН». */
    Accent,
}

/**
 * Heading over a group of rows (К4, review 2026-11-10): 13 sp, semibold, 0.04 em tracking — one style for
 * every list. [SectionTone.Accent] turns it into accent capitals (ink in the light theme). No padding of its
 * own: the screen places it.
 */
@Composable
fun CoShellSectionLabel(text: String, modifier: Modifier = Modifier, tone: SectionTone = SectionTone.Muted) {
    Text(
        text = if (tone == SectionTone.Accent) text.uppercase() else text,
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.04.em,
        ),
        color = if (tone == SectionTone.Accent) accentInk() else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
